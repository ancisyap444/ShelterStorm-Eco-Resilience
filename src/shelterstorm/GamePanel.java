package shelterstorm;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * GamePanel.java - Primary graphics panel, game loop driver, and input listener.
 * 
 * <p><b>OOP Principles Demonstrated:</b></p>
 * <ul>
 *   <li><b>Encapsulation:</b> State machines, FPS regulation, and key event dispatches
 *       are managed cleanly within private helper methods.</li>
 *   <li><b>Separation of Concerns:</b> UI layout is delegated to dedicated screen classes
 *       ({@link MenuScreen}, {@link GameOverScreen}, {@link HUD}), while gameplay logic
 *       resides in {@link GameManager}.</li>
 * </ul>
 * 
 * @author ShelterStorm Team
 */
public class GamePanel extends JPanel implements Runnable, KeyListener {

    // ==================== Display Settings ====================
    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;
    public static final int TILE_SIZE = 48;
    public static final int MAP_COLS = 20;
    public static final int MAP_ROWS = 14;
    private static final int FPS = 60;

    // ==================== Game States ====================
    public enum GameState {
        MENU,
        PLAYING,
        PAUSED,
        GAME_OVER,
        LEVEL_COMPLETE
    }

    private GameState state = GameState.MENU;

    // ==================== Subsystems ====================
    private Thread gameThread;
    private boolean running = false;
    private int frameCount = 0;

    private GameManager gameManager;
    private MenuScreen menuScreen;
    private GameOverScreen gameOverScreen;
    private HUD hud;

    /**
     * Initializes the GamePanel with screen size, focusability, and event listeners.
     */
    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);

        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        // Instantiate game manager and UI subsystems
        this.gameManager = new GameManager();
        this.menuScreen = new MenuScreen();
        this.gameOverScreen = new GameOverScreen();
        this.hud = new HUD();
    }

    /**
     * Launches the background game thread.
     */
    public synchronized void startGameThread() {
        if (gameThread == null) {
            running = true;
            gameThread = new Thread(this, "GameLoop");
            gameThread.start();
        }
    }

    /**
     * Fixed-step 60 FPS game loop.
     */
    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (running) {
            update();
            repaint();

            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1_000_000.0;
                if (remainingTime < 0) {
                    remainingTime = 0;
                }
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    // ==================== Update Logic ====================

    /**
     * State-driven update dispatch.
     */
    private void update() {
        frameCount++;

        if (state == GameState.PLAYING) {
            gameManager.update();

            // Check game-over condition
            if (gameManager.checkGameOver()) {
                state = GameState.GAME_OVER;
            }
            // Check level-complete condition
            else if (gameManager.checkLevelComplete()) {
                state = GameState.LEVEL_COMPLETE;
            }
        }
    }

    // ==================== Rendering ====================

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch (state) {
            case MENU:
                menuScreen.render(g2, SCREEN_WIDTH, SCREEN_HEIGHT, frameCount);
                break;

            case PLAYING:
                renderPlayScene(g2);
                break;

            case PAUSED:
                renderPlayScene(g2);
                renderPauseOverlay(g2);
                break;

            case GAME_OVER:
                renderPlayScene(g2);
                gameOverScreen.render(g2, SCREEN_WIDTH, SCREEN_HEIGHT,
                                      gameManager.getShelter(), gameManager.getScore());
                break;

            case LEVEL_COMPLETE:
                renderPlayScene(g2);
                renderLevelCompleteOverlay(g2);
                break;
        }

        g2.dispose();
    }

    /**
     * Renders game world simulation and the bottom HUD panel.
     */
    private void renderPlayScene(Graphics2D g2) {
        // Render world entities (960x672)
        gameManager.render(g2);

        // Render HUD (1024x96) at bottom
        hud.render(g2,
                   gameManager.getShelter(),
                   gameManager.getPlayer(),
                   gameManager.getScore(),
                   gameManager.getCurrentLevel(),
                   gameManager.getRoundTimer());
    }

    /**
     * Renders a translucent pause modal.
     */
    private void renderPauseOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setFont(new Font("Arial", Font.BOLD, 44));
        g2.setColor(Color.WHITE);
        String txt = "GAME PAUSED";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, (SCREEN_WIDTH - fm.stringWidth(txt)) / 2, 320);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(new Color(200, 220, 255));
        String sub = "Press P to Resume  |  Press ESC for Menu";
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(sub, (SCREEN_WIDTH - fmSub.stringWidth(sub)) / 2, 370);
    }

    /**
     * Renders a level victory overlay.
     */
    private void renderLevelCompleteOverlay(Graphics2D g2) {
        g2.setColor(new Color(0, 40, 20, 190));
        g2.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g2.setFont(new Font("Arial", Font.BOLD, 46));
        g2.setColor(new Color(100, 255, 140));
        String txt = "LEVEL " + gameManager.getCurrentLevel() + " SURVIVED!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(txt, (SCREEN_WIDTH - fm.stringWidth(txt)) / 2, 260);

        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.setColor(Color.WHITE);
        String sub1 = "Shelter secured & evacuees protected.";
        String sub2 = (gameManager.getCurrentLevel() < 3) 
                    ? "Press ENTER to face the next storm level!" 
                    : "VICTORY! You saved the entire community!";
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(sub1, (SCREEN_WIDTH - fmSub.stringWidth(sub1)) / 2, 320);

        g2.setColor(new Color(255, 230, 80));
        g2.drawString(sub2, (SCREEN_WIDTH - fmSub.stringWidth(sub2)) / 2, 370);

        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(new Color(180, 190, 210));
        String mSub = "Press M for Menu";
        FontMetrics fmM = g2.getFontMetrics();
        g2.drawString(mSub, (SCREEN_WIDTH - fmM.stringWidth(mSub)) / 2, 430);
    }

    // ==================== Input Handling (KeyListener) ====================

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        switch (state) {
            case MENU:
                if (code == KeyEvent.VK_ENTER) {
                    gameManager.restartGame();
                    state = GameState.PLAYING;
                }
                break;

            case PLAYING:
                if (code == KeyEvent.VK_P) {
                    state = GameState.PAUSED;
                } else if (code == KeyEvent.VK_ESCAPE) {
                    state = GameState.MENU;
                } else {
                    gameManager.getPlayer().keyPressed(code);
                }
                break;

            case PAUSED:
                if (code == KeyEvent.VK_P) {
                    state = GameState.PLAYING;
                } else if (code == KeyEvent.VK_ESCAPE) {
                    state = GameState.MENU;
                }
                break;

            case GAME_OVER:
                if (code == KeyEvent.VK_R) {
                    gameManager.restartGame();
                    state = GameState.PLAYING;
                } else if (code == KeyEvent.VK_M || code == KeyEvent.VK_ESCAPE) {
                    state = GameState.MENU;
                }
                break;

            case LEVEL_COMPLETE:
                if (code == KeyEvent.VK_ENTER) {
                    if (gameManager.getCurrentLevel() < 3) {
                        gameManager.nextLevel();
                        state = GameState.PLAYING;
                    } else {
                        // Beat all levels! Go to menu
                        state = GameState.MENU;
                    }
                } else if (code == KeyEvent.VK_M || code == KeyEvent.VK_ESCAPE) {
                    state = GameState.MENU;
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (state == GameState.PLAYING) {
            gameManager.getPlayer().keyReleased(e.getKeyCode());
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}

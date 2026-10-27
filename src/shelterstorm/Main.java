package shelterstorm;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Main.java - Entry point for ShelterStorm: Eco-Resilience.
 * 
 * Creates the main application window (JFrame) and initializes the GamePanel
 * which contains the game loop and rendering logic.
 * 
 * Demonstrates: Application bootstrapping and Swing UI setup.
 * 
 * @author ShelterStorm Team
 */
public class Main {

    /** Window title displayed in the title bar. */
    public static final String TITLE = "ShelterStorm: Eco-Resilience";

    /** Window width in pixels. */
    public static final int WINDOW_WIDTH = 1024;

    /** Window height in pixels. */
    public static final int WINDOW_HEIGHT = 768;

    /**
     * Application entry point. Launches the game on the Event Dispatch Thread
     * to ensure thread-safe Swing operations.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // Use SwingUtilities.invokeLater to ensure thread safety with Swing
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            // Create the game panel and add it to the frame
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);

            // Pack the frame around the preferred size of the GamePanel
            frame.pack();
            frame.setLocationRelativeTo(null); // Center on screen
            frame.setVisible(true);

            // Start the game loop after the window is visible
            gamePanel.startGameThread();
        });
    }
}

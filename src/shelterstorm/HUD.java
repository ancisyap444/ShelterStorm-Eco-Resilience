package shelterstorm;

import java.awt.*;

/**
 * Heads-Up Display rendered at the bottom of the game screen.
 * 
 * Demonstrates ENCAPSULATION - all layout constants, colours, and
 * drawing helpers are private; the public API is a single render() call.
 * 
 * Layout (1024 × 96 px, starting at y = 672):
 * ┌──────────┬──────────────┬────────┬──────────────┬──────────┬──────────┐
 * │  WATER   │   PANIC      │ SCORE  │    LEVEL     │  TIMER   │  ITEM    │
 * │   BAR    │    BAR       │        │              │          │          │
 * └──────────┴──────────────┴────────┴──────────────┴──────────┴──────────┘
 */
public class HUD {

    // ── Layout constants ───────────────────────────────────────────────
    /** Y-coordinate where the HUD panel begins. */
    private static final int HUD_Y = 672;
    /** Total width of the HUD panel. */
    private static final int HUD_WIDTH = 1024;
    /** Total height of the HUD panel. */
    private static final int HUD_HEIGHT = 96;

    // Status-bar dimensions
    private static final int BAR_WIDTH = 140;
    private static final int BAR_HEIGHT = 20;

    // ── Colours ────────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(35, 35, 45);
    private static final Color BORDER_COLOR = new Color(80, 80, 100);
    private static final Color TEXT_COLOR = new Color(220, 220, 230);
    private static final Color BAR_OUTLINE = new Color(160, 160, 170);
    private static final Color WATER_NORMAL = new Color(50, 130, 220);
    private static final Color PANIC_NORMAL = new Color(230, 150, 30);
    private static final Color DANGER_RED = new Color(220, 40, 40);

    // ── Animation state ────────────────────────────────────────────────
    /** Frame counter used for the warning pulse effect. */
    private int frameCount = 0;

    // ── Level descriptions ─────────────────────────────────────────────
    /** Human-readable descriptions for each game level. */
    private static final String[] LEVEL_NAMES = {
        "",                          // index 0 (unused)
        "LEVEL 1 - Light Storm",     // index 1
        "LEVEL 2 - Heavy Typhoon",   // index 2
        "LEVEL 3 - Flash Flood"      // index 3
    };

    // ── Public API ─────────────────────────────────────────────────────

    /**
     * Renders the complete HUD panel.
     *
     * @param g2      Graphics context
     * @param shelter The community shelter (provides water & panic levels)
     * @param player  The player (provides held-item info)
     * @param score   Current score
     * @param level   Current game level (1–3)
     * @param timer   Remaining time in seconds
     */
    public void render(Graphics2D g2, Shelter shelter, Player player,
                       int score, int level, float timer) {
        frameCount++;

        // ── Background panel ───────────────────────────────────────────
        g2.setColor(BG_COLOR);
        g2.fillRect(0, HUD_Y, HUD_WIDTH, HUD_HEIGHT);
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(0, HUD_Y, HUD_WIDTH, HUD_Y); // top border
        g2.setStroke(new BasicStroke(1f));

        // Gather data from shelter (safe defaults if null)
        float waterPct = (shelter != null) ? shelter.getWaterLevel() : 0f;
        float panicPct = (shelter != null) ? shelter.getPanicLevel() : 0f;

        // ── Left section: Water Level bar ──────────────────────────────
        int secX = 16;
        int secY = HUD_Y + 14;
        drawStatusBar(g2, "WATER LEVEL", waterPct, secX, secY,
                      WATER_NORMAL, waterPct > 75f);

        // ── Centre-left: Panic bar ─────────────────────────────────────
        secX = 190;
        drawStatusBar(g2, "COMMUNITY PANIC", panicPct, secX, secY,
                      PANIC_NORMAL, panicPct > 75f);

        // ── Centre: Score ──────────────────────────────────────────────
        secX = 400;
        g2.setFont(new Font("Monospaced", Font.BOLD, 26));
        g2.setColor(TEXT_COLOR);
        g2.drawString("SCORE: " + score, secX, secY + 22);

        // ── Centre-right: Level ────────────────────────────────────────
        secX = 600;
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        int lvlIdx = Math.max(1, Math.min(3, level));
        g2.setColor(levelColor(lvlIdx));
        g2.drawString(LEVEL_NAMES[lvlIdx], secX, secY + 10);

        // Subtitle hint
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(180, 180, 190));
        g2.drawString("Sort debris • Comfort evacuees", secX, secY + 30);

        // ── Right: Timer ───────────────────────────────────────────────
        secX = 820;
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.setColor(TEXT_COLOR);
        int minutes = (int) (timer / 60);
        int seconds = (int) (timer % 60);
        String timeStr = String.format("TIME: %d:%02d", minutes, seconds);
        g2.drawString(timeStr, secX, secY + 10);

        // ── Far right: Held item indicator ─────────────────────────────
        secX = 950;
        drawHeldItem(g2, player, secX, secY);
    }

    // ── Private helpers ────────────────────────────────────────────────

    /**
     * Draws a labelled status bar (water or panic).
     *
     * @param g2        Graphics context
     * @param label     Text label above the bar
     * @param percent   Fill percentage (0–100)
     * @param x         X position
     * @param y         Y position
     * @param fillColor Normal fill colour
     * @param danger    If true, the bar pulses red as a warning
     */
    private void drawStatusBar(Graphics2D g2, String label, float percent,
                               int x, int y, Color fillColor, boolean danger) {
        // Clamp percent to 0–100
        percent = Math.max(0f, Math.min(100f, percent));

        // Label
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(TEXT_COLOR);
        g2.drawString(label, x, y);

        // Bar background
        int barY = y + 6;
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(x, barY, BAR_WIDTH, BAR_HEIGHT);

        // Fill
        int fillW = (int) (BAR_WIDTH * percent / 100f);
        if (danger) {
            // Pulse between red and the normal colour
            boolean pulse = (frameCount % 20 < 10);
            g2.setColor(pulse ? DANGER_RED : fillColor);
        } else {
            g2.setColor(fillColor);
        }
        g2.fillRect(x, barY, fillW, BAR_HEIGHT);

        // Outline
        g2.setColor(BAR_OUTLINE);
        g2.drawRect(x, barY, BAR_WIDTH, BAR_HEIGHT);

        // Percentage text inside bar
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        String pctText = (int) percent + "%";
        FontMetrics fm = g2.getFontMetrics();
        int textX = x + (BAR_WIDTH - fm.stringWidth(pctText)) / 2;
        int textY = barY + (BAR_HEIGHT + fm.getAscent()) / 2 - 2;
        g2.drawString(pctText, textX, textY);
    }

    /**
     * Draws the held-item indicator (coloured square or "EMPTY" text).
     *
     * @param g2 Graphics context
     * @param player The player
     * @param x  X position
     * @param y  Y position
     */
    private void drawHeldItem(Graphics2D g2, Player player, int x, int y) {
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(180, 180, 190));
        g2.drawString("HELD", x, y);

        int boxSize = 28;
        int boxY = y + 6;

        if (player != null && player.hasItem()) {
            // Draw a coloured square representing the held item
            Color itemColor = player.getHeldItemColor();
            if (itemColor == null) itemColor = Color.GRAY;
            g2.setColor(itemColor);
            g2.fillRect(x, boxY, boxSize, boxSize);
            g2.setColor(Color.WHITE);
            g2.drawRect(x, boxY, boxSize, boxSize);
        } else {
            // Empty slot
            g2.setColor(new Color(60, 60, 70));
            g2.fillRect(x, boxY, boxSize, boxSize);
            g2.setColor(BAR_OUTLINE);
            g2.drawRect(x, boxY, boxSize, boxSize);
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.setColor(new Color(140, 140, 150));
            g2.drawString("EMPTY", x + 1, boxY + boxSize + 12);
        }
    }

    /**
     * Returns a colour that corresponds to the given level for the
     * level-label display.
     */
    private Color levelColor(int level) {
        switch (level) {
            case 1: return new Color(100, 200, 100); // green – mild
            case 2: return new Color(240, 180, 40);  // amber – moderate
            case 3: return new Color(240, 60, 60);   // red   – severe
            default: return TEXT_COLOR;
        }
    }
}

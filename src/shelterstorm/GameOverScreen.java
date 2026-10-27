package shelterstorm;

import java.awt.*;

/**
 * Renders the Game Over screen when the shelter is overwhelmed.
 * 
 * The game ends when either:
 *   • Water level reaches 100% → "THE SHELTER WAS FLOODED"
 *   • Community panic reaches 100% → "THE COMMUNITY FELL INTO CHAOS"
 * 
 * Displays the final score, items sorted, evacuees comforted, and
 * restart/menu instructions.
 * 
 * Demonstrates ENCAPSULATION - all layout and drawing logic is private;
 * the only public method is render().
 */
public class GameOverScreen {

    // ── Colours ────────────────────────────────────────────────────────
    private static final Color OVERLAY_COLOR = new Color(0, 0, 0, 180);
    private static final Color FLOOD_TITLE_COLOR = new Color(220, 50, 50);
    private static final Color PANIC_TITLE_COLOR = new Color(240, 150, 30);
    private static final Color STAT_LABEL_COLOR = new Color(170, 170, 180);
    private static final Color STAT_VALUE_COLOR = new Color(240, 240, 250);
    private static final Color PROMPT_COLOR = new Color(180, 190, 210);
    private static final Color SCORE_COLOR = new Color(255, 220, 80);

    // ── Animation ──────────────────────────────────────────────────────
    /** Frame counter for subtle animation effects. */
    private int frameCount = 0;

    // ── Public API ─────────────────────────────────────────────────────

    /**
     * Renders the Game Over overlay on top of the frozen game state.
     *
     * @param g2      Graphics context
     * @param width   Screen width in pixels
     * @param height  Screen height in pixels
     * @param shelter The shelter (provides water/panic levels and stats)
     * @param score   The player's final score
     */
    public void render(Graphics2D g2, int width, int height,
                       Shelter shelter, int score) {
        frameCount++;

        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // ── 1. Dark overlay ────────────────────────────────────────────
        g2.setColor(OVERLAY_COLOR);
        g2.fillRect(0, 0, width, height);

        // ── 2. Determine cause of failure ──────────────────────────────
        boolean floodEnd = (shelter != null && shelter.getWaterLevel() >= 100f);
        // If not flood, it must be panic (or we default to panic)
        boolean panicEnd = !floodEnd;

        // ── 3. "GAME OVER" header ──────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        g2.setColor(new Color(200, 200, 210));
        String header = "GAME OVER";
        FontMetrics fmH = g2.getFontMetrics();
        int hx = (width - fmH.stringWidth(header)) / 2;
        // Subtle shadow
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawString(header, hx + 3, 153);
        g2.setColor(new Color(200, 200, 210));
        g2.drawString(header, hx, 150);

        // ── 4. Failure reason ──────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        String reason;
        if (floodEnd) {
            g2.setColor(FLOOD_TITLE_COLOR);
            reason = "THE SHELTER WAS FLOODED";
        } else {
            g2.setColor(PANIC_TITLE_COLOR);
            reason = "THE COMMUNITY FELL INTO CHAOS";
        }
        FontMetrics fmR = g2.getFontMetrics();
        g2.drawString(reason, (width - fmR.stringWidth(reason)) / 2, 210);

        // Dramatic underline
        int lineW = fmR.stringWidth(reason);
        int lineX = (width - lineW) / 2;
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(lineX, 218, lineX + lineW, 218);
        g2.setStroke(new BasicStroke(1f));

        // ── 5. Stats panel ─────────────────────────────────────────────
        int panelW = 320;
        int panelH = 180;
        int panelX = (width - panelW) / 2;
        int panelY = 250;

        // Panel background
        g2.setColor(new Color(20, 22, 35, 200));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 10, 10);
        g2.setColor(new Color(70, 75, 95));
        g2.drawRoundRect(panelX, panelY, panelW, panelH, 10, 10);

        // Final score (large, golden)
        g2.setFont(new Font("Monospaced", Font.BOLD, 32));
        g2.setColor(SCORE_COLOR);
        String scoreText = "SCORE: " + score;
        FontMetrics fmS = g2.getFontMetrics();
        g2.drawString(scoreText,
                      panelX + (panelW - fmS.stringWidth(scoreText)) / 2,
                      panelY + 45);

        // Divider line
        g2.setColor(new Color(70, 75, 95));
        g2.drawLine(panelX + 20, panelY + 60, panelX + panelW - 20, panelY + 60);

        // Detailed stats
        int statY = panelY + 90;
        int itemsSorted = (shelter != null) ? shelter.getItemsSorted() : 0;
        int evacueeComforted = (shelter != null) ? shelter.getEvacueesComforted() : 0;

        drawStat(g2, "Items Sorted:", String.valueOf(itemsSorted),
                 panelX + 30, statY, panelW - 60);
        drawStat(g2, "Evacuees Comforted:", String.valueOf(evacueeComforted),
                 panelX + 30, statY + 35, panelW - 60);

        // Water & panic final values
        float finalWater = (shelter != null) ? shelter.getWaterLevel() : 0f;
        float finalPanic = (shelter != null) ? shelter.getPanicLevel() : 0f;
        drawStat(g2, "Final Water:", (int) finalWater + "%",
                 panelX + 30, statY + 70, panelW - 60);
        // (Only show panic if it's the relevant failure)
        // Actually show both for informational purposes

        // ── 6. Restart / Menu prompt ───────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        boolean blink = (frameCount % 50 < 35); // mostly visible
        g2.setColor(blink ? PROMPT_COLOR : new Color(PROMPT_COLOR.getRed(),
                    PROMPT_COLOR.getGreen(), PROMPT_COLOR.getBlue(), 100));
        String prompt = "Press R to Restart  |  Press M for Menu";
        FontMetrics fmP = g2.getFontMetrics();
        g2.drawString(prompt, (width - fmP.stringWidth(prompt)) / 2,
                      height - 80);
    }

    // ── Private helpers ────────────────────────────────────────────────

    /**
     * Draws a single stat line: label on the left, value on the right.
     *
     * @param g2       Graphics context
     * @param label    Stat description (e.g. "Items Sorted:")
     * @param value    Stat value (e.g. "42")
     * @param x        Left X position
     * @param y        Baseline Y position
     * @param maxWidth Available width for alignment
     */
    private void drawStat(Graphics2D g2, String label, String value,
                          int x, int y, int maxWidth) {
        // Label (left-aligned)
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(STAT_LABEL_COLOR);
        g2.drawString(label, x, y);

        // Value (right-aligned)
        g2.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2.setColor(STAT_VALUE_COLOR);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(value, x + maxWidth - fm.stringWidth(value), y);
    }
}

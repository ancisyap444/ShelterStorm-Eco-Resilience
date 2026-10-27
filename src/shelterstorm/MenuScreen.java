package shelterstorm;

import java.awt.*;

/**
 * Renders the main menu / title screen for ShelterStorm.
 * 
 * All visuals are drawn procedurally with Graphics2D — no image assets
 * required. The screen features:
 *   • Game title and tagline
 *   • Control instructions
 *   • Animated rain background (reuses StormEvent utility)
 *   • Decorative shelter silhouette and debris
 *   • Blinking "Press ENTER to Start" prompt
 * 
 * Demonstrates ENCAPSULATION - internal drawing helpers are private;
 * the only public method is render().
 */
public class MenuScreen {

    // ── Colours ────────────────────────────────────────────────────────
    private static final Color BG_COLOR = new Color(25, 30, 50);
    private static final Color TITLE_COLOR = new Color(220, 230, 255);
    private static final Color SUBTITLE_COLOR = new Color(140, 180, 220);
    private static final Color TAGLINE_COLOR = new Color(160, 170, 190);
    private static final Color INSTRUCTION_BG = new Color(20, 25, 40, 200);
    private static final Color INSTRUCTION_BORDER = new Color(70, 80, 110);
    private static final Color KEY_COLOR = new Color(100, 200, 255);
    private static final Color DESC_COLOR = new Color(200, 200, 210);
    private static final Color PROMPT_COLOR = new Color(255, 220, 80);

    // ── Constants ──────────────────────────────────────────────────────
    /** Number of frames per blink cycle for the "Press ENTER" prompt. */
    private static final int BLINK_PERIOD = 60;
    /** Rain drops drawn on the menu background. */
    private static final int MENU_RAIN_DROPS = 120;

    // ── Public API ─────────────────────────────────────────────────────

    /**
     * Renders the complete menu screen.
     *
     * @param g2         Graphics context
     * @param width      Screen width in pixels
     * @param height     Screen height in pixels
     * @param frameCount Current frame number (drives animations)
     */
    public void render(Graphics2D g2, int width, int height, int frameCount) {
        // Enable anti-aliasing for smoother text
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        // ── 1. Background ──────────────────────────────────────────────
        g2.setColor(BG_COLOR);
        g2.fillRect(0, 0, width, height);

        // ── 2. Decorative elements (behind rain) ───────────────────────
        drawShelterSilhouette(g2, width, height);
        drawDebris(g2, width, height, frameCount);

        // ── 3. Rain overlay ────────────────────────────────────────────
        StormEvent.drawStaticRain(g2, width, height, frameCount,
                                  MENU_RAIN_DROPS, 80);

        // ── 4. Title ───────────────────────────────────────────────────
        drawTitle(g2, width, frameCount);

        // ── 5. Tagline ─────────────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(TAGLINE_COLOR);
        String tagline = "Survive the storm. Save your community.";
        FontMetrics fmTag = g2.getFontMetrics();
        g2.drawString(tagline, (width - fmTag.stringWidth(tagline)) / 2, 240);

        // ── 6. Instructions box ────────────────────────────────────────
        drawInstructions(g2, width, height);

        // ── 7. "Press ENTER to Start" (blinking) ──────────────────────
        boolean visible = (frameCount % BLINK_PERIOD) < (BLINK_PERIOD / 2);
        if (visible) {
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            g2.setColor(PROMPT_COLOR);
            String prompt = "Press ENTER to Start";
            FontMetrics fmP = g2.getFontMetrics();
            g2.drawString(prompt, (width - fmP.stringWidth(prompt)) / 2,
                          height - 60);
        }
    }

    // ── Private drawing helpers ────────────────────────────────────────

    /**
     * Draws the game title "SHELTERSTORM" and subtitle "Eco-Resilience".
     */
    private void drawTitle(Graphics2D g2, int width, int frameCount) {
        // Main title
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        g2.setColor(TITLE_COLOR);
        String title = "SHELTERSTORM";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (width - fm.stringWidth(title)) / 2;
        int ty = 150;

        // Subtle shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.drawString(title, tx + 3, ty + 3);
        g2.setColor(TITLE_COLOR);
        g2.drawString(title, tx, ty);

        // Subtitle
        g2.setFont(new Font("Arial", Font.ITALIC, 30));
        g2.setColor(SUBTITLE_COLOR);
        String subtitle = "Eco-Resilience";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(subtitle, (width - fm2.stringWidth(subtitle)) / 2, 195);
    }

    /**
     * Draws the controls/instructions panel centred on screen.
     */
    private void drawInstructions(Graphics2D g2, int width, int height) {
        int boxW = 340;
        int boxH = 200;
        int boxX = (width - boxW) / 2;
        int boxY = 280;

        // Background panel
        g2.setColor(INSTRUCTION_BG);
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g2.setColor(INSTRUCTION_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 12, 12);
        g2.setStroke(new BasicStroke(1f));

        // Header
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(TITLE_COLOR);
        String header = "— CONTROLS —";
        FontMetrics fmH = g2.getFontMetrics();
        g2.drawString(header, boxX + (boxW - fmH.stringWidth(header)) / 2,
                      boxY + 28);

        // Instruction entries
        String[][] controls = {
            {"WASD / Arrow Keys", "Move"},
            {"E", "Interact / Pick Up / Sort"},
            {"Q", "Drop Item"},
            {"P", "Pause"},
            {"ESC", "Quit"}
        };

        g2.setFont(new Font("Monospaced", Font.BOLD, 14));
        int lineY = boxY + 56;
        for (String[] entry : controls) {
            // Key
            g2.setColor(KEY_COLOR);
            g2.drawString(entry[0], boxX + 20, lineY);
            // Separator & description
            g2.setColor(DESC_COLOR);
            g2.drawString("  -  " + entry[1], boxX + 20 + g2.getFontMetrics()
                          .stringWidth(entry[0]), lineY);
            lineY += 26;
        }
    }

    /**
     * Draws a simple shelter silhouette in the background.
     * The silhouette is a basic house shape (rectangle + triangle roof).
     */
    private void drawShelterSilhouette(Graphics2D g2, int width, int height) {
        int houseW = 200;
        int houseH = 120;
        int houseX = width / 2 - houseW / 2;
        int houseY = height - 180;

        // Walls
        g2.setColor(new Color(15, 18, 35, 120));
        g2.fillRect(houseX, houseY, houseW, houseH);

        // Roof (triangle)
        int[] roofX = {houseX - 20, houseX + houseW / 2, houseX + houseW + 20};
        int[] roofY = {houseY, houseY - 60, houseY};
        g2.fillPolygon(roofX, roofY, 3);

        // Door
        g2.setColor(new Color(10, 12, 25, 100));
        g2.fillRect(houseX + houseW / 2 - 15, houseY + houseH - 50,
                    30, 50);

        // Windows (two small squares)
        g2.setColor(new Color(60, 80, 120, 60));
        g2.fillRect(houseX + 25, houseY + 25, 30, 25);
        g2.fillRect(houseX + houseW - 55, houseY + 25, 30, 25);
    }

    /**
     * Draws a few scattered debris pieces for atmosphere.
     * Positions are deterministic (based on screen size) so they don't
     * jump around between frames.
     */
    private void drawDebris(Graphics2D g2, int width, int height, int frameCount) {
        g2.setColor(new Color(80, 70, 50, 50));

        // Small rectangles at fixed positions
        int[][] debrisPos = {
            {100, height - 80, 20, 8},
            {width - 150, height - 70, 15, 10},
            {250, height - 60, 12, 6},
            {width - 250, height - 90, 18, 7},
            {width / 2 + 140, height - 65, 14, 9}
        };

        for (int[] d : debrisPos) {
            // Slight sway using sin for wind effect
            int offsetX = (int) (3 * Math.sin(frameCount * 0.03 + d[0]));
            g2.fillRect(d[0] + offsetX, d[1], d[2], d[3]);
        }

        // A couple of diagonal lines (branches / sticks)
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(90, 80, 60, 40));
        g2.drawLine(160, height - 75, 180, height - 90);
        g2.drawLine(width - 200, height - 85, width - 180, height - 95);
        g2.setStroke(new BasicStroke(1f));
    }
}

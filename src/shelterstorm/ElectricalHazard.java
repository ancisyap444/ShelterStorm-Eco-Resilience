package shelterstorm;

import java.awt.*;
import java.util.Random;

/**
 * Represents a downed power line or exposed wiring.
 * 
 * Demonstrates INHERITANCE - extends the abstract Hazard class with
 * its own unique effect and visual representation.
 * 
 * Demonstrates POLYMORPHISM - when the game loop iterates over a
 * List&lt;Hazard&gt; and calls applyEffect(), this subclass raises
 * community panic instead of water level (contrast with FloodHazard).
 * 
 * Visual: flickering yellow/white zigzag lightning bolt with a pulsing
 * warning circle, intermittent white flashes, and a "DANGER" label.
 */
public class ElectricalHazard extends Hazard {

    // ── Constants ──────────────────────────────────────────────────────
    /** Duration in frames (8 seconds at 60 fps). */
    private static final int DEFAULT_DURATION = 480;
    /** Base panic increase applied to the shelter each frame. */
    private static final float PANIC_INCREMENT = 0.08f;
    /** Extra panic when the player is inside the danger zone. */
    private static final float PLAYER_PANIC_EXTRA = 0.1f;
    /** Radius of the warning circle around the bolt (pixels). */
    private static final int WARNING_RADIUS = 44;

    // ── Animation state ────────────────────────────────────────────────
    /** Frame counter for animation timing. */
    private int frameCount = 0;
    /** Random generator for flicker and flash timing. */
    private final Random random = new Random();
    /** Whether the bolt is visually "on" this frame (flicker). */
    private boolean flickerOn = true;

    // ── Constructor ────────────────────────────────────────────────────

    /**
     * Creates an ElectricalHazard at the given pixel position.
     *
     * @param x X-coordinate in pixels
     * @param y Y-coordinate in pixels
     */
    public ElectricalHazard(float x, float y) {
        super(x, y, DEFAULT_DURATION, "Electrical");
    }

    // ── Overridden behaviour (Polymorphism) ────────────────────────────

    /**
     * Raises community panic each frame.
     * If the player is within the danger zone, panic rises even faster.
     */
    @Override
    public void applyEffect(Shelter shelter) {
        // Electrical hazards are terrifying - base panic increase
        shelter.addPanic(PANIC_INCREMENT);

        // Standing near a live wire is extra scary
        if (shelter.getPlayer() != null && isPlayerInDanger(shelter.getPlayer())) {
            shelter.addPanic(PLAYER_PANIC_EXTRA);
        }
    }

    /**
     * Updates animation state: toggles flicker randomly.
     */
    @Override
    public void update() {
        super.update();
        frameCount++;

        // Flicker: randomly toggle visibility every few frames
        if (frameCount % 3 == 0) {
            flickerOn = random.nextFloat() > 0.25f; // 75% chance to be visible
        }
    }

    // ── Rendering ──────────────────────────────────────────────────────

    /**
     * Draws a flickering lightning bolt with a warning circle.
     * <ul>
     *   <li>Pulsing yellow warning circle</li>
     *   <li>Zigzag lightning bolt shape (yellow / white)</li>
     *   <li>Intermittent bright white flash overlay</li>
     *   <li>"DANGER" label above the hazard</li>
     * </ul>
     */
    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        int cx = (int) x + 24; // centre of the hazard tile
        int cy = (int) y + 24;

        // ── 1. Pulsing warning circle ──────────────────────────────────
        int circleAlpha = (int) (100 + 50 * Math.sin(frameCount * 0.15));
        circleAlpha = Math.max(40, Math.min(200, circleAlpha));
        g2.setColor(new Color(255, 255, 0, circleAlpha));
        g2.setStroke(new BasicStroke(2.5f));
        g2.drawOval(cx - WARNING_RADIUS, cy - WARNING_RADIUS,
                    WARNING_RADIUS * 2, WARNING_RADIUS * 2);

        // Semi-transparent yellow fill
        g2.setColor(new Color(255, 200, 0, 30));
        g2.fillOval(cx - WARNING_RADIUS, cy - WARNING_RADIUS,
                    WARNING_RADIUS * 2, WARNING_RADIUS * 2);

        // ── 2. Lightning bolt (only when flicker is "on") ──────────────
        if (flickerOn) {
            // Determine bolt colour: mostly bright yellow, occasional white flash
            boolean whiteFlash = (frameCount % 12 < 2);
            Color boltColor = whiteFlash
                    ? new Color(255, 255, 255, 240)
                    : new Color(255, 255, 0, 230);
            Color boltGlow = whiteFlash
                    ? new Color(255, 255, 200, 80)
                    : new Color(255, 220, 0, 60);

            // Draw glow behind bolt
            g2.setColor(boltGlow);
            g2.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawBolt(g2, cx, cy);

            // Draw sharp bolt on top
            g2.setColor(boltColor);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            drawBolt(g2, cx, cy);
        }

        // ── 3. Intermittent bright flash overlay ───────────────────────
        if (frameCount % 40 < 3) {
            g2.setColor(new Color(255, 255, 220, 50));
            g2.fillRect((int) x - 10, (int) y - 10, 68, 68);
        }

        // ── 4. "DANGER" label ──────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        // Alternate between yellow and white for urgency
        g2.setColor(frameCount % 20 < 10
                ? new Color(255, 255, 0, 230)
                : new Color(255, 100, 0, 230));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth("DANGER");
        g2.drawString("DANGER", cx - textW / 2, (int) y - 8);

        // Reset stroke
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Helper: draw zigzag lightning bolt ─────────────────────────────

    /**
     * Draws a zigzag lightning bolt centred at (cx, cy).
     * The bolt consists of connected line segments forming a classic
     * cartoon-style lightning shape.
     *
     * @param g2 Graphics context (colour and stroke already set)
     * @param cx Centre X of the bolt
     * @param cy Centre Y of the bolt
     */
    private void drawBolt(Graphics2D g2, int cx, int cy) {
        // Define bolt points relative to centre (classic zigzag shape)
        int[] xPoints = {
            cx - 2, cx + 8, cx + 1, cx + 12, cx - 1, cx + 6, cx - 4
        };
        int[] yPoints = {
            cy - 28, cy - 10, cy - 6, cy + 10, cy + 6, cy + 22, cy + 30
        };

        // Draw connected line segments
        for (int i = 0; i < xPoints.length - 1; i++) {
            g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }
    }
}

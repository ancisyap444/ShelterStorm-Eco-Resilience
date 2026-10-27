package shelterstorm;

import java.awt.*;

/**
 * Represents a clogged drain causing localised flooding.
 * 
 * Demonstrates INHERITANCE - extends the abstract Hazard class and
 * provides concrete implementations for applyEffect() and render().
 * 
 * Demonstrates POLYMORPHISM - the game loop calls applyEffect() on any
 * Hazard reference; this subclass raises water level while
 * ElectricalHazard raises panic instead.
 * 
 * Visual: animated water ripple effect with concentric pulsing circles,
 * blue translucent overlay, wavy lines, and a "FLOOD" label.
 */
public class FloodHazard extends Hazard {

    // ── Constants ──────────────────────────────────────────────────────
    /** Duration in frames (10 seconds at 60 fps). */
    private static final int DEFAULT_DURATION = 600;
    /** Water-level increase applied to the shelter each frame. */
    private static final float WATER_INCREMENT = 0.05f;
    /** Panic increase when the player stands in the flood zone. */
    private static final float PANIC_INCREMENT = 0.02f;
    /** Side-length of the visible flood area (pixels). */
    private static final int FLOOD_SIZE = 96;

    // ── Animation state ────────────────────────────────────────────────
    /** Internal frame counter driving the ripple animation. */
    private int frameCount = 0;

    // ── Constructor ────────────────────────────────────────────────────

    /**
     * Creates a FloodHazard at the given tile position.
     *
     * @param x X-coordinate in pixels
     * @param y Y-coordinate in pixels
     */
    public FloodHazard(float x, float y) {
        super(x, y, DEFAULT_DURATION, "Flood");
    }

    // ── Overridden behaviour (Polymorphism) ────────────────────────────

    /**
     * Raises the shelter's water level each frame.
     * If the player is standing in the danger zone, also raises panic.
     */
    @Override
    public void applyEffect(Shelter shelter) {
        // Steadily raise water level while the flood is active
        shelter.addWater(WATER_INCREMENT);

        // Extra penalty if the player is too close
        if (shelter.getPlayer() != null && isPlayerInDanger(shelter.getPlayer())) {
            shelter.addPanic(PANIC_INCREMENT);
        }
    }

    /**
     * Tick the animation counter alongside the normal duration countdown.
     */
    @Override
    public void update() {
        super.update();
        frameCount++;
    }

    // ── Rendering ──────────────────────────────────────────────────────

    /**
     * Draws an animated water-ripple effect at the hazard location.
     * <ul>
     *   <li>Translucent blue 96×96 overlay</li>
     *   <li>Concentric circles that pulse via sin-wave modulation</li>
     *   <li>Wavy horizontal lines for surface texture</li>
     *   <li>"FLOOD" label centred above the area</li>
     * </ul>
     */
    @Override
    public void render(Graphics2D g2) {
        if (!active) return;

        int drawX = (int) x;
        int drawY = (int) y;
        int halfSize = FLOOD_SIZE / 2;
        int cx = drawX + halfSize;   // centre X of the flood area
        int cy = drawY + halfSize;   // centre Y of the flood area

        // ── 1. Translucent blue overlay that pulses ────────────────────
        int pulseAlpha = (int) (80 + 30 * Math.sin(frameCount * 0.08));
        pulseAlpha = Math.max(0, Math.min(255, pulseAlpha));
        g2.setColor(new Color(30, 100, 200, pulseAlpha));
        g2.fillRect(drawX, drawY, FLOOD_SIZE, FLOOD_SIZE);

        // ── 2. Concentric ripple circles ───────────────────────────────
        for (int ring = 1; ring <= 3; ring++) {
            // Radius oscillates with time; each ring has a phase offset
            double phase = frameCount * 0.1 - ring * 1.2;
            int radius = (int) (10 + ring * 14 + 6 * Math.sin(phase));
            int rippleAlpha = (int) (120 - ring * 30 + 20 * Math.sin(phase));
            rippleAlpha = Math.max(20, Math.min(200, rippleAlpha));

            g2.setColor(new Color(80, 160, 255, rippleAlpha));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }

        // ── 3. Wavy horizontal lines (water surface) ──────────────────
        g2.setColor(new Color(100, 180, 255, 100));
        g2.setStroke(new BasicStroke(1.5f));
        for (int row = 0; row < 5; row++) {
            int baseY = drawY + 12 + row * 18;
            int prevWX = drawX;
            int prevWY = baseY + (int) (3 * Math.sin(frameCount * 0.12 + row));
            for (int step = 1; step <= 8; step++) {
                int wx = drawX + step * (FLOOD_SIZE / 8);
                int wy = baseY + (int) (3 * Math.sin(frameCount * 0.12 + step * 0.8 + row));
                g2.drawLine(prevWX, prevWY, wx, wy);
                prevWX = wx;
                prevWY = wy;
            }
        }

        // ── 4. "FLOOD" label ───────────────────────────────────────────
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(200, 230, 255, 220));
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth("FLOOD");
        g2.drawString("FLOOD", cx - textW / 2, drawY - 6);

        // Reset stroke to default
        g2.setStroke(new BasicStroke(1f));
    }
}

package shelterstorm;

import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * Manages the visual / atmospheric storm effects overlaid on the game.
 * 
 * This is NOT a Hazard subclass — it controls ambient weather effects
 * (rain, thunder flashes, reduced visibility) rather than localised
 * tile-based dangers.
 * 
 * Demonstrates ENCAPSULATION - all storm state (rain positions, thunder
 * timers, intensity) is hidden behind a clean update/render API.
 * 
 * Storm intensity scales with the current game level:
 *   Level 1  →  Light Storm   (light rain, rare thunder)
 *   Level 2  →  Heavy Typhoon (moderate rain, occasional thunder)
 *   Level 3  →  Flash Flood   (heavy rain, frequent thunder, dark overlay)
 */
public class StormEvent {

    // ── Storm parameters ───────────────────────────────────────────────
    /** Current intensity level (1–3, matching the game level). */
    private int intensity;
    /** Overall alpha multiplier for rain rendering. */
    private float rainAlpha;
    /** Whether a thunder flash is currently being displayed. */
    private boolean thunderActive;
    /** Frames remaining for the current thunder flash. */
    private int thunderTimer;

    // ── Internal state ─────────────────────────────────────────────────
    /** Random generator shared across rain / thunder logic. */
    private final Random random = new Random();
    /** Frame counter for rain animation offset. */
    private int frameCount = 0;

    // ── Constants ──────────────────────────────────────────────────────
    /** Duration of a thunder flash in frames. */
    private static final int THUNDER_FLASH_DURATION = 5;
    /** Base number of rain drops drawn per frame at intensity 1. */
    private static final int BASE_RAIN_DROPS = 60;
    /** Anxiety added to every evacuee when thunder strikes. */
    private static final float THUNDER_ANXIETY_BOOST = 5f;

    // ── Constructor ────────────────────────────────────────────────────

    /**
     * Creates a StormEvent with the given intensity.
     *
     * @param intensity Storm level (1 = light, 2 = heavy, 3 = flash-flood)
     */
    public StormEvent(int intensity) {
        setIntensity(intensity);
        this.thunderActive = false;
        this.thunderTimer = 0;
    }

    // ── Intensity control ──────────────────────────────────────────────

    /**
     * Sets the storm intensity and adjusts the rain alpha accordingly.
     *
     * @param intensity 1–3
     */
    public void setIntensity(int intensity) {
        this.intensity = Math.max(1, Math.min(3, intensity));
        // Rain becomes more opaque at higher intensity
        switch (this.intensity) {
            case 1: rainAlpha = 0.25f; break;
            case 2: rainAlpha = 0.45f; break;
            case 3: rainAlpha = 0.65f; break;
            default: rainAlpha = 0.25f;
        }
    }

    /** @return Current storm intensity (1–3). */
    public int getIntensity() { return intensity; }

    // ── Update ─────────────────────────────────────────────────────────

    /**
     * Updates storm state each frame.
     * <ul>
     *   <li>Advances the frame counter (drives rain animation).</li>
     *   <li>Counts down an active thunder flash.</li>
     *   <li>Randomly triggers a new thunder flash based on intensity.</li>
     * </ul>
     *
     * @param evacuees List of evacuees to receive anxiety boost on thunder
     *                 (may be null or empty)
     */
    public void update(List<Evacuee> evacuees) {
        frameCount++;

        // ── Thunder flash countdown ────────────────────────────────────
        if (thunderActive) {
            thunderTimer--;
            if (thunderTimer <= 0) {
                thunderActive = false;
            }
        }

        // ── Random thunder trigger ─────────────────────────────────────
        // Probability per frame: level 1 → 0.2%, level 2 → 0.5%, level 3 → 1%
        double thunderChance;
        switch (intensity) {
            case 1: thunderChance = 0.002; break;
            case 2: thunderChance = 0.005; break;
            case 3: thunderChance = 0.01;  break;
            default: thunderChance = 0.002;
        }

        if (!thunderActive && random.nextDouble() < thunderChance) {
            thunderActive = true;
            thunderTimer = THUNDER_FLASH_DURATION;

            // Thunder scares ALL evacuees
            if (evacuees != null) {
                for (Evacuee e : evacuees) {
                    e.addAnxiety(THUNDER_ANXIETY_BOOST);
                }
            }
        }
    }

    // ── Rendering ──────────────────────────────────────────────────────

    /**
     * Renders the storm overlay: rain, thunder flash, and darkness.
     * Should be called AFTER all game entities are drawn so the weather
     * appears on top.
     *
     * @param g2           Graphics context
     * @param screenWidth  Total screen width in pixels
     * @param screenHeight Total screen height in pixels
     */
    public void render(Graphics2D g2, int screenWidth, int screenHeight) {
        drawRain(g2, screenWidth, screenHeight);
        drawThunder(g2, screenWidth, screenHeight);
        drawDarknessOverlay(g2, screenWidth, screenHeight);
    }

    // ── Rain ───────────────────────────────────────────────────────────

    /**
     * Draws diagonal rain streaks across the screen.
     * Density and opacity scale with intensity.
     */
    private void drawRain(Graphics2D g2, int screenWidth, int screenHeight) {
        int dropCount = BASE_RAIN_DROPS * intensity;
        int alphaValue = (int) (rainAlpha * 255);
        alphaValue = Math.max(0, Math.min(255, alphaValue));

        g2.setColor(new Color(180, 200, 255, alphaValue));
        g2.setStroke(new BasicStroke(1.2f));

        for (int i = 0; i < dropCount; i++) {
            // Pseudo-random but deterministic per frame for smooth animation
            int seed = i * 7919 + frameCount * 31;
            int rx = Math.abs(seed % screenWidth);
            int ry = Math.abs((seed * 4567) % screenHeight);

            // Rain falls diagonally (wind effect)
            int length = 10 + (intensity * 4);
            int windOffset = 3 + intensity;
            g2.drawLine(rx, ry, rx + windOffset, ry + length);
        }

        // Reset stroke
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Thunder flash ──────────────────────────────────────────────────

    /**
     * Briefly flashes the screen white when thunder is active.
     * Alpha ranges from 40–60 for a dramatic but non-blinding effect.
     */
    private void drawThunder(Graphics2D g2, int screenWidth, int screenHeight) {
        if (!thunderActive) return;

        // Alpha varies slightly each frame for a natural look
        int flashAlpha = 40 + random.nextInt(21); // 40–60
        g2.setColor(new Color(255, 255, 255, flashAlpha));
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    // ── Darkness overlay (intensity 3 only) ────────────────────────────

    /**
     * At intensity 3 (Flash Flood), draws a dark semi-transparent overlay
     * to simulate reduced visibility during extreme weather.
     */
    private void drawDarknessOverlay(Graphics2D g2, int screenWidth, int screenHeight) {
        if (intensity >= 3) {
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }
    }

    // ── Static utility for reuse (e.g. MenuScreen) ─────────────────────

    /**
     * Draws a simple rain effect that can be reused by other screens
     * (e.g. the main menu background). This is a static convenience
     * method that does not depend on instance state.
     *
     * @param g2           Graphics context
     * @param screenWidth  Width of the drawing area
     * @param screenHeight Height of the drawing area
     * @param frameCount   Current frame number (for animation)
     * @param dropCount    Number of rain drops to draw
     * @param alpha        Opacity of the rain (0–255)
     */
    public static void drawStaticRain(Graphics2D g2, int screenWidth,
                                      int screenHeight, int frameCount,
                                      int dropCount, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        g2.setColor(new Color(180, 200, 255, alpha));
        g2.setStroke(new BasicStroke(1.0f));

        for (int i = 0; i < dropCount; i++) {
            int seed = i * 7919 + frameCount * 31;
            int rx = Math.abs(seed % screenWidth);
            int ry = Math.abs((seed * 4567) % screenHeight);
            g2.drawLine(rx, ry, rx + 3, ry + 14);
        }

        g2.setStroke(new BasicStroke(1f));
    }
}

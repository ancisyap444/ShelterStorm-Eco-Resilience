package shelterstorm;

import java.awt.*;

/**
 * Abstract base class for environmental hazards.
 * 
 * Demonstrates INHERITANCE - specific hazards (FloodHazard, ElectricalHazard)
 * extend this base class and provide their own implementations.
 * 
 * Demonstrates ABSTRACTION - defines a contract (applyEffect, render) that
 * all hazards must fulfill, hiding implementation details.
 * 
 * Demonstrates POLYMORPHISM - each hazard subclass affects the shelter
 * differently through overridden applyEffect() and render() methods.
 */
public abstract class Hazard {

    // ── Protected fields accessible to subclasses ──────────────────────
    /** X-coordinate of the hazard on the map (in pixels). */
    protected float x;
    /** Y-coordinate of the hazard on the map (in pixels). */
    protected float y;
    /** Number of frames remaining before the hazard expires. */
    protected int duration;
    /** Whether this hazard is still active on the map. */
    protected boolean active = true;
    /** Human-readable name for HUD/log display. */
    protected String name;

    // ── Constants ──────────────────────────────────────────────────────
    /** Radius (in pixels) within which the player is considered "in danger". */
    private static final double DANGER_RADIUS = 80.0;

    // ── Constructor ───────────────────────────────────────────────────

    /**
     * Constructs a new Hazard at the given position.
     *
     * @param x        X-coordinate in pixels
     * @param y        Y-coordinate in pixels
     * @param duration Lifetime in frames (e.g. 600 = 10 s at 60 fps)
     * @param name     Display name of this hazard
     */
    public Hazard(float x, float y, int duration, String name) {
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.name = name;
    }

    // ── Abstract methods (Polymorphism) ───────────────────────────────

    /**
     * Apply this hazard's unique effect to the shelter.
     * Each subclass implements its own behaviour (Polymorphism).
     *
     * @param shelter The community shelter affected by this hazard
     */
    public abstract void applyEffect(Shelter shelter);

    /**
     * Render this hazard's unique visual representation.
     * Each subclass draws itself differently (Polymorphism).
     *
     * @param g2 The Graphics2D context to draw on
     */
    public abstract void render(Graphics2D g2);

    // ── Concrete methods shared by all hazards ────────────────────────

    /**
     * Decrements the duration counter each frame.
     * When duration reaches zero the hazard deactivates itself.
     */
    public void update() {
        duration--;
        if (duration <= 0) {
            active = false;
        }
    }

    /**
     * Checks whether the player is within the danger zone of this hazard
     * using simple Euclidean distance.
     *
     * @param player The player to test against
     * @return true if the player is within {@value #DANGER_RADIUS}px
     */
    public boolean isPlayerInDanger(Player player) {
        float dx = x - player.getX();
        float dy = y - player.getY();
        return Math.sqrt(dx * dx + dy * dy) < DANGER_RADIUS;
    }

    // ── Getters (Encapsulation) ───────────────────────────────────────

    /** @return true if this hazard is still active */
    public boolean isActive() { return active; }

    /** @return X-coordinate in pixels */
    public float getX() { return x; }

    /** @return Y-coordinate in pixels */
    public float getY() { return y; }

    /** @return Display name of this hazard */
    public String getName() { return name; }
}

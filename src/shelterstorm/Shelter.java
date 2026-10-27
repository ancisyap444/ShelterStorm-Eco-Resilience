package shelterstorm;

/**
 * Shelter.java - Represents the evacuation shelter and its status.
 * 
 * This class is a key demonstration of ENCAPSULATION:
 * - All fields are private, preventing direct external modification.
 * - Access is provided through controlled getter/setter methods.
 * - Methods enforce invariants (values clamped to 0-100 range).
 * - Internal state cannot be corrupted by external code.
 * 
 * The shelter has two critical meters:
 * - Water Level: rises with flooding, reduced by player actions
 * - Community Panic: rises when hazards are active, reduced by comforting evacuees
 * 
 * If either meter reaches 100, the game is over.
 * 
 * @author ShelterStorm Team
 */
public class Shelter {

    // =========================================================================
    // ENCAPSULATION: All fields are private - no direct external access
    // =========================================================================

    /** Water level inside the shelter (0 = dry, 100 = flooded). */
    private float waterLevel;

    /** Community panic level (0 = calm, 100 = total panic). */
    private float communityPanic;

    /** Whether the shelter's power generator is currently on. */
    private boolean powerOn;

    /** 
     * Drainage capacity of the shelter. Higher values mean the shelter
     * can passively drain more water per update cycle.
     */
    private int drainageCapacity;

    /** Current game score for the player. */
    private int score = 0;

    /** Total number of trash items sorted correctly. */
    private int itemsSorted = 0;

    /** Total number of times evacuees were comforted. */
    private int evacueesComforted = 0;

    /** Reference to the player (needed for hazards to check player proximity). */
    private Player player = null;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates a new Shelter with default initial values.
     * The shelter starts dry, calm, with power on, and base drainage.
     */
    public Shelter() {
        this.waterLevel = 0.0f;
        this.communityPanic = 0.0f;
        this.powerOn = true;
        this.drainageCapacity = 1;
    }

    /**
     * Creates a new Shelter with specified drainage capacity.
     * Used for higher difficulty levels where drainage may vary.
     *
     * @param drainageCapacity the initial drainage capacity (must be >= 0)
     */
    public Shelter(int drainageCapacity) {
        this();
        this.drainageCapacity = Math.max(0, drainageCapacity);
    }

    // =========================================================================
    // ENCAPSULATION: Controlled access methods with bounds checking
    // =========================================================================

    /**
     * Increases the water level inside the shelter.
     * The value is clamped so it never exceeds 100.
     *
     * @param amount the amount to increase (must be positive)
     */
    public void increaseWater(float amount) {
        if (amount > 0) {
            this.waterLevel = Math.min(100.0f, this.waterLevel + amount);
        }
    }

    /** Alias for increaseWater for compatibility. */
    public void addWater(float amount) {
        increaseWater(amount);
    }

    /**
     * Decreases the water level inside the shelter.
     * The value is clamped so it never goes below 0.
     *
     * @param amount the amount to decrease (must be positive)
     */
    public void decreaseWater(float amount) {
        if (amount > 0) {
            this.waterLevel = Math.max(0.0f, this.waterLevel - amount);
        }
    }

    /**
     * Increases the community panic level.
     * The value is clamped so it never exceeds 100.
     *
     * @param amount the amount to increase (must be positive)
     */
    public void increasePanic(float amount) {
        if (amount > 0) {
            this.communityPanic = Math.min(100.0f, this.communityPanic + amount);
        }
    }

    /** Alias for increasePanic for compatibility. */
    public void addPanic(float amount) {
        increasePanic(amount);
    }

    /** Adds points to the player's score. */
    public void addScore(int amount) {
        this.score += amount;
    }

    /** Gets the current score. */
    public int getScore() {
        return score;
    }

    /** Sets the current score. */
    public void setScore(int score) {
        this.score = score;
    }

    /** Increments items sorted count. */
    public void incrementItemsSorted() {
        this.itemsSorted++;
    }

    /** Gets total items sorted. */
    public int getItemsSorted() {
        return itemsSorted;
    }

    /** Increments evacuees comforted count. */
    public void incrementEvacueesComforted() {
        this.evacueesComforted++;
    }

    /** Gets total evacuees comforted. */
    public int getEvacueesComforted() {
        return evacueesComforted;
    }

    /** Gets the player reference. */
    public Player getPlayer() {
        return player;
    }

    /** Sets the player reference. */
    public void setPlayer(Player player) {
        this.player = player;
    }

    /** Alias for getCommunityPanic() */
    public float getPanicLevel() {
        return communityPanic;
    }

    /**
     * Decreases the community panic level.
     * The value is clamped so it never goes below 0.
     *
     * @param amount the amount to decrease (must be positive)
     */
    public void decreasePanic(float amount) {
        if (amount > 0) {
            this.communityPanic = Math.max(0.0f, this.communityPanic - amount);
        }
    }

    /**
     * Toggles the shelter's power on or off.
     * Power outages increase panic and disable certain facilities.
     */
    public void togglePower() {
        this.powerOn = !this.powerOn;
    }

    // =========================================================================
    // Getters - Read-only access to private fields
    // =========================================================================

    /**
     * Gets the current water level.
     * @return water level as a float (0.0 to 100.0)
     */
    public float getWaterLevel() {
        return waterLevel;
    }

    /**
     * Gets the current community panic level.
     * @return panic level as a float (0.0 to 100.0)
     */
    public float getCommunityPanic() {
        return communityPanic;
    }

    /**
     * Checks if the shelter's power is currently on.
     * @return true if power is on, false if there is an outage
     */
    public boolean isPowerOn() {
        return powerOn;
    }

    /**
     * Gets the shelter's drainage capacity.
     * @return drainage capacity as an integer
     */
    public int getDrainageCapacity() {
        return drainageCapacity;
    }

    /**
     * Sets the shelter's power state directly.
     * Used during level initialization.
     *
     * @param powerOn true to turn power on, false for outage
     */
    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }

    /**
     * Sets the shelter's drainage capacity.
     * Used during level initialization.
     *
     * @param capacity the new drainage capacity (clamped to >= 0)
     */
    public void setDrainageCapacity(int capacity) {
        this.drainageCapacity = Math.max(0, capacity);
    }

    // =========================================================================
    // Status checks
    // =========================================================================

    /**
     * Checks if the shelter is in a critical state (either meter above 80%).
     * @return true if water or panic is at 80 or above
     */
    public boolean isCritical() {
        return waterLevel >= 80.0f || communityPanic >= 80.0f;
    }

    /**
     * Checks if the shelter has been overwhelmed (game over condition).
     * @return true if water or panic has reached 100
     */
    public boolean isOverwhelmed() {
        return waterLevel >= 100.0f || communityPanic >= 100.0f;
    }

    /**
     * Resets the shelter to default safe values.
     * Used when starting a new level.
     */
    public void reset() {
        this.waterLevel = 0.0f;
        this.communityPanic = 0.0f;
        this.powerOn = true;
    }

    @Override
    public String toString() {
        return String.format("Shelter[water=%.1f%%, panic=%.1f%%, power=%s, drainage=%d]",
                waterLevel, communityPanic, powerOn ? "ON" : "OFF", drainageCapacity);
    }
}

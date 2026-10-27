package shelterstorm;

import java.awt.*;
import java.util.Random;

/**
 * Evacuee.java - An NPC evacuee that the player can comfort to reduce anxiety.
 * 
 * <p><b>OOP Principle: Abstraction</b> — Implements the {@link Interactable} interface,
 * fulfilling the contract for interactive game objects. The game engine treats all
 * Interactable objects uniformly without needing to know the specific type.</p>
 * 
 * <p><b>OOP Principle: Encapsulation</b> — Internal state such as {@code anxietyLevel},
 * {@code state}, and wander timers are private. Behavior is controlled exclusively
 * through public methods like {@link #comfort(int)} and {@link #interact(Player, Shelter)}.</p>
 * 
 * <p><b>Behavior:</b></p>
 * <ul>
 *   <li>Evacuees wander randomly around the map, picking a new direction every 2-3 seconds.</li>
 *   <li>Anxiety state determines color and movement pattern:
 *       CALM (green, slow), ANXIOUS (yellow, normal), PANICKING (red, erratic).</li>
 *   <li>The player can comfort an evacuee by pressing E when nearby, reducing anxiety by 25.</li>
 *   <li>A 3-second cooldown prevents spamming the comfort action.</li>
 * </ul>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public class Evacuee implements Interactable {

    // ==================== Private Fields (Encapsulation) ====================

    /** Position of this evacuee on the map (in pixels). */
    private float x, y;

    /** Anxiety level from 0 (calm) to 100 (maximum panic). */
    private int anxietyLevel;

    /**
     * Current emotional state string, derived from anxiety level.
     * "CALM" (0-29), "ANXIOUS" (30-79), "PANICKING" (80-100).
     */
    private String state;

    /** Timer that counts down before picking a new wander direction. */
    private float wanderTimer;

    /** Current wander direction components (normalized). */
    private float wanderDirX, wanderDirY;

    /** Cooldown timer to prevent rapid comfort spamming (in seconds). */
    private float comfortCooldown = 0;

    /** Shared random number generator for wander behavior. */
    private Random random = new Random();

    /** NPC hitbox dimensions. */
    private static final int WIDTH = 24;
    private static final int HEIGHT = 32;

    /** Interaction range in pixels. */
    private static final float INTERACTION_RANGE = 64f;

    /** Comfort cooldown duration in seconds. */
    private static final float COMFORT_COOLDOWN_DURATION = 3.0f;

    /** Visual shake offset for panicking evacuees. */
    private float shakeOffset = 0;

    // Map boundary constants
    private static final int MAP_WIDTH = 960;
    private static final int MAP_HEIGHT = 672;

    /**
     * Constructs a new Evacuee at the given position with the specified anxiety level.
     * The emotional state is automatically derived from the anxiety value.
     * 
     * @param x             the starting x-coordinate in pixels
     * @param y             the starting y-coordinate in pixels
     * @param anxietyLevel  the initial anxiety level (0-100)
     */
    public Evacuee(float x, float y, int anxietyLevel) {
        this.x = x;
        this.y = y;
        this.anxietyLevel = Math.max(0, Math.min(100, anxietyLevel)); // Clamp 0-100
        this.wanderTimer = 2.0f + random.nextFloat(); // Initial random timer
        this.wanderDirX = 0;
        this.wanderDirY = 0;
        updateState(); // Set initial state based on anxiety
    }

    // ==================== State Management ====================

    /**
     * Updates the emotional state string based on the current anxiety level.
     * This determines the evacuee's color and movement behavior.
     */
    private void updateState() {
        if (anxietyLevel < 30) {
            state = "CALM";
        } else if (anxietyLevel < 80) {
            state = "ANXIOUS";
        } else {
            state = "PANICKING";
        }
    }

    /**
     * Reduces the evacuee's anxiety by the given amount, clamped to [0, 100].
     * Automatically updates the emotional state afterward.
     * 
     * @param amount the amount to reduce anxiety by (positive integer)
     */
    public void comfort(int amount) {
        anxietyLevel = Math.max(0, anxietyLevel - amount);
        updateState();
    }

    /**
     * Increases the evacuee's anxiety by the given amount, clamped to [0, 100].
     * 
     * @param amount the amount to increase anxiety by
     */
    public void addAnxiety(float amount) {
        anxietyLevel = Math.min(100, Math.round(anxietyLevel + amount));
        updateState();
    }

    // ==================== Game Logic ====================

    /**
     * Updates the evacuee's wander behavior and cooldown timers.
     * Called once per game tick (~60 FPS, so deltaTime ≈ 1/60).
     * 
     * <ul>
     *   <li>CALM evacuees move slowly (speed 0.5).</li>
     *   <li>ANXIOUS evacuees move at normal speed (1.0).</li>
     *   <li>PANICKING evacuees move erratically and faster (2.0), with visual shake.</li>
     * </ul>
     * 
     * @param deltaTime the time elapsed since the last update (in seconds)
     */
    public void update(float deltaTime) {
        // Decrease comfort cooldown
        if (comfortCooldown > 0) {
            comfortCooldown -= deltaTime;
        }

        // Decrease wander timer
        wanderTimer -= deltaTime;

        // Pick a new random wander direction when the timer expires
        if (wanderTimer <= 0) {
            // Random direction between -1 and 1 on each axis
            wanderDirX = (random.nextFloat() * 2 - 1);
            wanderDirY = (random.nextFloat() * 2 - 1);

            // Normalize the direction vector
            float length = (float) Math.sqrt(wanderDirX * wanderDirX + wanderDirY * wanderDirY);
            if (length > 0) {
                wanderDirX /= length;
                wanderDirY /= length;
            }

            // If panicking, change direction more frequently (erratic behavior)
            if ("PANICKING".equals(state)) {
                wanderTimer = 0.5f + random.nextFloat() * 0.5f; // 0.5-1.0 seconds
            } else {
                wanderTimer = 2.0f + random.nextFloat(); // 2.0-3.0 seconds
            }
        }

        // Determine movement speed based on emotional state
        float moveSpeed;
        switch (state) {
            case "PANICKING":
                moveSpeed = 2.0f;
                // Calculate visual shake for panicking evacuees
                shakeOffset = (random.nextFloat() - 0.5f) * 4;
                break;
            case "ANXIOUS":
                moveSpeed = 1.0f;
                shakeOffset = 0;
                break;
            default: // CALM
                moveSpeed = 0.5f;
                shakeOffset = 0;
                break;
        }

        // Apply movement
        x += wanderDirX * moveSpeed;
        y += wanderDirY * moveSpeed;

        // Clamp to map bounds and bounce off walls
        if (x < 0) { x = 0; wanderDirX = Math.abs(wanderDirX); }
        if (y < 0) { y = 0; wanderDirY = Math.abs(wanderDirY); }
        if (x > MAP_WIDTH - WIDTH) { x = MAP_WIDTH - WIDTH; wanderDirX = -Math.abs(wanderDirX); }
        if (y > MAP_HEIGHT - HEIGHT) { y = MAP_HEIGHT - HEIGHT; wanderDirY = -Math.abs(wanderDirY); }
    }

    // ==================== Interactable Implementation ====================

    /**
     * Interacts with this evacuee by comforting them.
     * Reduces anxiety by 25 and adds 5 points to the shelter score.
     * Has a 3-second cooldown to prevent spamming.
     * 
     * @param player  the player performing the interaction
     * @param shelter the shelter instance (for score updates)
     */
    @Override
    public void interact(Player player, Shelter shelter) {
        // Only allow comfort if cooldown has expired
        if (comfortCooldown <= 0 && anxietyLevel > 0) {
            comfort(25); // Reduce anxiety by 25
            shelter.addScore(5); // Reward for helping an evacuee
            shelter.incrementEvacueesComforted();
            shelter.decreasePanic(5); // Also reduce community panic!
            comfortCooldown = COMFORT_COOLDOWN_DURATION; // Start 3-second cooldown
        }
    }

    /**
     * Checks if the player is within interaction range (64 pixels).
     * Uses Euclidean distance from center-to-center.
     * 
     * @param player the player to check distance against
     * @return {@code true} if the player is within 64 pixels
     */
    @Override
    public boolean isNear(Player player) {
        // Calculate center-to-center distance
        float cx = x + WIDTH / 2f;
        float cy = y + HEIGHT / 2f;
        float pcx = player.getX() + player.getWidth() / 2f;
        float pcy = player.getY() + player.getHeight() / 2f;
        float dx = cx - pcx;
        float dy = cy - pcy;
        return Math.sqrt(dx * dx + dy * dy) < INTERACTION_RANGE;
    }

    /**
     * Returns the interaction hint to display when the player is near.
     * Shows cooldown status if comfort was recently used.
     * 
     * @return the hint string
     */
    @Override
    public String getInteractHint() {
        if (comfortCooldown > 0) {
            return "Comfort on cooldown...";
        }
        if (anxietyLevel <= 0) {
            return "Evacuee is calm!";
        }
        return "Press E to comfort";
    }

    // ==================== Rendering ====================

    /**
     * Renders the evacuee as a simple person shape (circle head + rectangle body).
     * Color depends on emotional state:
     * <ul>
     *   <li>CALM: green</li>
     *   <li>ANXIOUS: yellow</li>
     *   <li>PANICKING: red with visual shake</li>
     * </ul>
     * An anxiety bar is drawn above the evacuee's head.
     * 
     * @param g2 the Graphics2D rendering context
     */
    public void render(Graphics2D g2) {
        // Apply shake offset for panicking evacuees
        int drawX = (int) (x + shakeOffset);
        int drawY = (int) y;

        // Determine color based on state
        Color bodyColor;
        switch (state) {
            case "PANICKING":
                bodyColor = new Color(220, 50, 50); // Red
                break;
            case "ANXIOUS":
                bodyColor = new Color(220, 200, 50); // Yellow
                break;
            default: // CALM
                bodyColor = new Color(50, 180, 50); // Green
                break;
        }

        // --- Draw body (rectangle) ---
        g2.setColor(bodyColor);
        g2.fillRect(drawX + 4, drawY + 12, WIDTH - 8, HEIGHT - 12);
        g2.setColor(bodyColor.darker());
        g2.drawRect(drawX + 4, drawY + 12, WIDTH - 8, HEIGHT - 12);

        // --- Draw head (circle) ---
        g2.setColor(bodyColor);
        g2.fillOval(drawX + 4, drawY, WIDTH - 8, 14);
        g2.setColor(bodyColor.darker());
        g2.drawOval(drawX + 4, drawY, WIDTH - 8, 14);

        // --- Draw anxiety bar above head ---
        int barWidth = WIDTH;
        int barHeight = 4;
        int barX = drawX;
        int barY = drawY - 8;

        // Background (dark gray)
        g2.setColor(new Color(60, 60, 60));
        g2.fillRect(barX, barY, barWidth, barHeight);

        // Anxiety fill — color transitions from green to red
        float anxietyRatio = anxietyLevel / 100f;
        int r = (int) (anxietyRatio * 255);
        int gr = (int) ((1 - anxietyRatio) * 255);
        g2.setColor(new Color(Math.min(255, r), Math.min(255, gr), 0));
        g2.fillRect(barX, barY, (int) (barWidth * anxietyRatio), barHeight);

        // Bar border
        g2.setColor(Color.BLACK);
        g2.drawRect(barX, barY, barWidth, barHeight);

        // --- Draw state label below body ---
        g2.setFont(new Font("Arial", Font.PLAIN, 9));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(state);
        g2.drawString(state, drawX + (WIDTH - textWidth) / 2, drawY + HEIGHT + 10);
    }

    // ==================== Getters ====================

    /** @return the evacuee's x-coordinate in pixels */
    public float getX() { return x; }

    /** @return the evacuee's y-coordinate in pixels */
    public float getY() { return y; }

    /** @return the current anxiety level (0-100) */
    public int getAnxietyLevel() { return anxietyLevel; }

    /** @return the current emotional state ("CALM", "ANXIOUS", or "PANICKING") */
    public String getState() { return state; }
}

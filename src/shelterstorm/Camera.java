package shelterstorm;

import java.awt.Graphics2D;

/**
 * Camera.java - A 2D camera system that follows the player with smooth movement.
 * 
 * Demonstrates ENCAPSULATION and separation of concerns:
 * - The camera's position and interpolation logic are self-contained.
 * - Other classes interact with the camera only through its public interface.
 * - The rendering system doesn't need to know how camera movement works.
 * 
 * The camera uses linear interpolation (lerp) to smoothly follow the player,
 * creating a polished, professional feel rather than rigid snapping.
 * 
 * Currently, the map fits entirely on screen (960x672 playable area),
 * so the camera acts as an identity transform. However, this class is included
 * for proper OOP structure and future extensibility (larger maps, zoom, etc.).
 * 
 * @author ShelterStorm Team
 */
public class Camera {

    // =========================================================================
    // Fields
    // =========================================================================

    /** Current camera X position (top-left corner of viewport). */
    private float x;

    /** Current camera Y position (top-left corner of viewport). */
    private float y;

    /** Target X position the camera is lerping toward. */
    private float targetX;

    /** Target Y position the camera is lerping toward. */
    private float targetY;

    /** 
     * Lerp speed factor (0.0 to 1.0). 
     * Lower values = smoother/slower following.
     * Higher values = snappier/faster following.
     */
    private float lerpSpeed;

    /** Width of the viewport (visible area) in pixels. */
    private int viewportWidth;

    /** Height of the viewport (visible area) in pixels. */
    private int viewportHeight;

    /** Total map width in pixels (for clamping). */
    private int mapWidth;

    /** Total map height in pixels (for clamping). */
    private int mapHeight;

    // =========================================================================
    // Constructor
    // =========================================================================

    /**
     * Creates a new Camera with default settings.
     * The camera starts at origin (0, 0) with a smooth lerp speed.
     */
    public Camera() {
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
        this.lerpSpeed = 0.1f; // Smooth following speed

        // Default to the game's playable area dimensions
        // 20 tiles * 48 pixels = 960 wide, 14 tiles * 48 pixels = 672 tall
        this.viewportWidth = GamePanel.TILE_SIZE * GamePanel.MAP_COLS;   // 960
        this.viewportHeight = GamePanel.TILE_SIZE * GamePanel.MAP_ROWS;  // 672
        this.mapWidth = viewportWidth;
        this.mapHeight = viewportHeight;
    }

    /**
     * Creates a new Camera with specified viewport and map dimensions.
     *
     * @param viewportWidth  width of the visible area in pixels
     * @param viewportHeight height of the visible area in pixels
     * @param mapWidth       total map width in pixels
     * @param mapHeight      total map height in pixels
     */
    public Camera(int viewportWidth, int viewportHeight, int mapWidth, int mapHeight) {
        this();
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    // =========================================================================
    // Core Methods
    // =========================================================================

    /**
     * Updates the camera position to smoothly follow the player.
     * Uses linear interpolation (lerp) for smooth movement.
     * 
     * The target is set to center the player in the viewport,
     * then clamped to prevent showing areas outside the map.
     *
     * @param player the Player entity to follow
     */
    public void update(Player player) {
        if (player == null) return;

        // Calculate target position to center the player in the viewport
        targetX = player.getX() + (player.getWidth() / 2.0f) - (viewportWidth / 2.0f);
        targetY = player.getY() + (player.getHeight() / 2.0f) - (viewportHeight / 2.0f);

        // Clamp target to map boundaries so we don't show empty space
        targetX = clamp(targetX, 0, Math.max(0, mapWidth - viewportWidth));
        targetY = clamp(targetY, 0, Math.max(0, mapHeight - viewportHeight));

        // Lerp (linear interpolation) toward the target for smooth movement
        // Formula: current = current + (target - current) * speed
        x += (targetX - x) * lerpSpeed;
        y += (targetY - y) * lerpSpeed;
    }

    /**
     * Applies the camera transform to the Graphics2D context.
     * This translates the rendering origin so that the world is drawn
     * relative to the camera's position.
     * 
     * Call this BEFORE rendering world objects, and restore the transform
     * AFTER rendering (or use Graphics2D.create() for a copy).
     *
     * @param g2 the Graphics2D context to transform
     */
    public void apply(Graphics2D g2) {
        // Translate the graphics context by the negative camera position
        // This effectively moves the "viewport" to the camera's location
        g2.translate(-(int) x, -(int) y);
    }

    /**
     * Resets the camera to the origin position (0, 0).
     * Used when starting a new level.
     */
    public void reset() {
        this.x = 0;
        this.y = 0;
        this.targetX = 0;
        this.targetY = 0;
    }

    // =========================================================================
    // Utility
    // =========================================================================

    /**
     * Clamps a value between a minimum and maximum.
     *
     * @param value the value to clamp
     * @param min   minimum allowed value
     * @param max   maximum allowed value
     * @return the clamped value
     */
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    // =========================================================================
    // Getters & Setters
    // =========================================================================

    public float getX() { return x; }
    public float getY() { return y; }

    public void setLerpSpeed(float lerpSpeed) {
        this.lerpSpeed = clamp(lerpSpeed, 0.01f, 1.0f);
    }

    public void setMapDimensions(int mapWidth, int mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }
}

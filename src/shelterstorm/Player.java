package shelterstorm;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Player.java - The player character controlled via keyboard input.
 * 
 * <p><b>OOP Principle: Encapsulation</b> — All internal state (position, keys,
 * held item, direction) is private. External classes interact only through
 * well-defined public methods like {@link #pickUp(TrashItem)}, {@link #dropItem()},
 * and {@link #getHeldItem()}. This prevents invalid state changes.</p>
 * 
 * <p>The player is rendered as a blue rectangle with a white hard-hat triangle.
 * A direction indicator shows which way the player is facing, and a colored
 * dot above the player's head indicates the type of item being carried.</p>
 * 
 * <p><b>Controls:</b></p>
 * <ul>
 *   <li>WASD / Arrow keys — Movement</li>
 *   <li>E — Interact (comfort evacuees / sort trash)</li>
 *   <li>Q — Drop held item</li>
 * </ul>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public class Player {

    // ==================== Private Fields (Encapsulation) ====================

    /** Player position in pixels. */
    private float x, y;

    /** Player hitbox dimensions in pixels. */
    private int width = 40, height = 40;

    /** Movement speed in pixels per frame. */
    private float speed = 3.0f;

    /** The trash item currently held by the player (null if hands are empty). */
    private TrashItem heldItem = null;

    /**
     * The direction the player is currently facing.
     * 0 = down, 1 = up, 2 = left, 3 = right.
     */
    private int direction = 0;

    /**
     * Tracks which keys are currently pressed.
     * Index mapping: 0=W/Up, 1=S/Down, 2=A/Left, 3=D/Right, 4=E(interact), 5=Q(drop).
     */
    private boolean[] keysPressed = new boolean[6];

    // Map boundary constants (20×14 tiles at 48px each)
    private static final int MAP_WIDTH = 960;   // 20 * 48
    private static final int MAP_HEIGHT = 672;  // 14 * 48

    /**
     * Constructs a new Player at the specified starting position.
     * 
     * @param startX the initial x-coordinate in pixels
     * @param startY the initial y-coordinate in pixels
     */
    public Player(float startX, float startY) {
        this.x = startX;
        this.y = startY;
    }

    // ==================== Input Handling ====================

    /**
     * Called when a key is pressed. Maps key codes to the internal key state array.
     * Supports both WASD and arrow key layouts for accessibility.
     * 
     * @param keyCode the {@link KeyEvent} key code
     */
    public void keyPressed(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    keysPressed[0] = true; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  keysPressed[1] = true; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  keysPressed[2] = true; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: keysPressed[3] = true; break;
            case KeyEvent.VK_E:                         keysPressed[4] = true; break;
            case KeyEvent.VK_Q:                         keysPressed[5] = true; break;
        }
    }

    /**
     * Called when a key is released. Clears the corresponding key state.
     * 
     * @param keyCode the {@link KeyEvent} key code
     */
    public void keyReleased(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_W: case KeyEvent.VK_UP:    keysPressed[0] = false; break;
            case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  keysPressed[1] = false; break;
            case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  keysPressed[2] = false; break;
            case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: keysPressed[3] = false; break;
            case KeyEvent.VK_E:                         keysPressed[4] = false; break;
            case KeyEvent.VK_Q:                         keysPressed[5] = false; break;
        }
    }

    /**
     * Checks if the interact key (E) is currently pressed.
     * @return {@code true} if E is being held down
     */
    public boolean isInteractPressed() {
        return keysPressed[4];
    }

    /**
     * Checks if the drop key (Q) is currently pressed.
     * @return {@code true} if Q is being held down
     */
    public boolean isDropPressed() {
        return keysPressed[5];
    }

    // ==================== Game Logic ====================

    /**
     * Updates the player's position based on currently pressed keys.
     * Movement is clamped to the map boundaries so the player cannot
     * walk off-screen. Direction is updated to reflect the last
     * movement input for the facing indicator.
     */
    public void update() {
        // Track movement for diagonal normalization
        float dx = 0, dy = 0;

        if (keysPressed[0]) { dy -= 1; direction = 1; } // Up
        if (keysPressed[1]) { dy += 1; direction = 0; } // Down
        if (keysPressed[2]) { dx -= 1; direction = 2; } // Left
        if (keysPressed[3]) { dx += 1; direction = 3; } // Right

        // Normalize diagonal movement to prevent faster diagonal speed
        if (dx != 0 && dy != 0) {
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= length;
            dy /= length;
        }

        // Apply speed
        x += dx * speed;
        y += dy * speed;

        // Clamp position to map bounds so player stays on screen
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > MAP_WIDTH - width) x = MAP_WIDTH - width;
        if (y > MAP_HEIGHT - height) y = MAP_HEIGHT - height;
    }

    // ==================== Item Management ====================

    /**
     * Picks up a trash item if the player isn't already holding one.
     * The item is marked as collected and stored in the player's inventory.
     * 
     * @param item the {@link TrashItem} to pick up
     */
    public void pickUp(TrashItem item) {
        if (heldItem == null && item != null) {
            heldItem = item;
            item.setCollected(true);
        }
    }

    /**
     * Drops the currently held item back onto the map at the player's position.
     * The item reappears slightly in front of the player based on facing direction.
     * 
     * @return the dropped {@link TrashItem}, or {@code null} if nothing was held
     */
    public TrashItem dropItem() {
        if (heldItem != null) {
            TrashItem dropped = heldItem;
            dropped.setCollected(false);

            // Place the item slightly in front of the player based on direction
            switch (direction) {
                case 0: dropped.y = y + height + 10; dropped.x = x; break; // Down
                case 1: dropped.y = y - 30;          dropped.x = x; break; // Up
                case 2: dropped.x = x - 30;          dropped.y = y; break; // Left
                case 3: dropped.x = x + width + 10;  dropped.y = y; break; // Right
            }

            heldItem = null;
            return dropped;
        }
        return null;
    }

    /**
     * Gets the item currently held by the player.
     * @return the held {@link TrashItem}, or {@code null} if empty-handed
     */
    public TrashItem getHeldItem() {
        return heldItem;
    }

    /**
     * Checks if the player is currently holding an item.
     * @return true if heldItem != null
     */
    public boolean hasItem() {
        return heldItem != null;
    }

    /**
     * Gets the color of the held item, or null if none.
     * @return item color
     */
    public Color getHeldItemColor() {
        return heldItem != null ? heldItem.getColor() : null;
    }

    /**
     * Clears the player's held item without dropping it on the map.
     * Used after successfully sorting an item into a bin.
     */
    public void clearHeldItem() {
        heldItem = null;
    }

    // ==================== Rendering ====================

    /**
     * Renders the player character using Graphics2D.
     * <ul>
     *   <li>Blue rectangle body</li>
     *   <li>White hard-hat triangle on top</li>
     *   <li>Direction indicator triangle</li>
     *   <li>Colored dot above head if carrying an item</li>
     * </ul>
     * 
     * @param g2 the Graphics2D rendering context
     */
    public void render(Graphics2D g2) {
        int px = (int) x;
        int py = (int) y;

        // --- Draw player body (blue rectangle) ---
        g2.setColor(new Color(50, 120, 220));
        g2.fillRect(px, py, width, height);

        // Darker border for visibility
        g2.setColor(new Color(30, 80, 170));
        g2.drawRect(px, py, width, height);

        // --- Draw white hard-hat triangle on top ---
        g2.setColor(Color.WHITE);
        int[] hatX = {px + 5, px + width / 2, px + width - 5};
        int[] hatY = {py + 2, py - 10, py + 2};
        g2.fillPolygon(hatX, hatY, 3);
        g2.setColor(new Color(200, 200, 200));
        g2.drawPolygon(hatX, hatY, 3);

        // --- Draw direction indicator (small triangle pointing movement direction) ---
        g2.setColor(new Color(255, 255, 100, 200)); // Semi-transparent yellow
        int cx = px + width / 2;  // Center x
        int cy = py + height / 2; // Center y
        int[] dirX, dirY;

        switch (direction) {
            case 1: // Up
                dirX = new int[]{cx - 5, cx, cx + 5};
                dirY = new int[]{py - 12, py - 20, py - 12};
                break;
            case 2: // Left
                dirX = new int[]{px - 2, px - 10, px - 2};
                dirY = new int[]{cy - 5, cy, cy + 5};
                break;
            case 3: // Right
                dirX = new int[]{px + width + 2, px + width + 10, px + width + 2};
                dirY = new int[]{cy - 5, cy, cy + 5};
                break;
            default: // Down (0)
                dirX = new int[]{cx - 5, cx, cx + 5};
                dirY = new int[]{py + height + 2, py + height + 10, py + height + 2};
                break;
        }
        g2.fillPolygon(dirX, dirY, 3);

        // --- Draw held item indicator (colored dot above head) ---
        if (heldItem != null) {
            g2.setColor(heldItem.getColor());
            g2.fillOval(cx - 5, py - 26, 10, 10);
            g2.setColor(Color.WHITE);
            g2.drawOval(cx - 5, py - 26, 10, 10);
        }
    }

    // ==================== Encapsulated Getters ====================

    /** @return the player's x-coordinate in pixels */
    public float getX() { return x; }

    /** @return the player's y-coordinate in pixels */
    public float getY() { return y; }

    /** @return the player's width in pixels */
    public int getWidth() { return width; }

    /** @return the player's height in pixels */
    public int getHeight() { return height; }

    /** @return the direction the player is facing (0=down, 1=up, 2=left, 3=right) */
    public int getDirection() { return direction; }
}

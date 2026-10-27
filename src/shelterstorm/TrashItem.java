package shelterstorm;

import java.awt.*;

/**
 * TrashItem.java - Abstract base class for all trash items in the game.
 * 
 * <p><b>OOP Principle: Inheritance</b> — Concrete subclasses ({@link PlasticItem},
 * {@link OrganicItem}, {@link HazardItem}) extend this class to inherit common
 * fields and methods while providing their own specialized behavior.</p>
 * 
 * <p><b>OOP Principle: Polymorphism</b> — The abstract methods {@link #process(String)},
 * {@link #render(Graphics2D)}, {@link #getColor()}, and {@link #getCorrectBinType()}
 * are overridden by each subclass. This allows the game engine to treat all trash
 * items uniformly through a {@code TrashItem} reference while each type behaves
 * differently at runtime (e.g., different point values, different penalty rules).</p>
 * 
 * <p><b>OOP Principle: Encapsulation</b> — Fields are {@code protected} so subclasses
 * can access them, but external classes use getters/setters.</p>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public abstract class TrashItem {

    /** X-coordinate of this trash item on the map (in pixels). */
    protected float x;

    /** Y-coordinate of this trash item on the map (in pixels). */
    protected float y;

    /** Points awarded for correctly sorting this item. */
    protected int points;

    /** Weight of the item (affects player movement when carrying). */
    protected float weight;

    /** Whether this item has been collected (picked up) by the player. */
    protected boolean collected = false;

    /** Display name of this trash item (e.g., "Plastic Bottle"). */
    protected String name;

    /**
     * Constructs a new TrashItem at the given position.
     * 
     * @param x      the x-coordinate in pixels
     * @param y      the y-coordinate in pixels
     * @param points the base points for correct sorting
     * @param name   the display name of the item
     */
    public TrashItem(float x, float y, int points, String name) {
        this.x = x;
        this.y = y;
        this.points = points;
        this.name = name;
        this.weight = 1.0f; // Default weight for all items
    }

    /**
     * Processes this trash item by sorting it into the given bin type.
     * 
     * <p><b>Polymorphism:</b> Each subclass defines its own scoring logic.
     * Correct sorting earns points; incorrect sorting applies a penalty.
     * Hazardous items have harsher penalties for mishandling.</p>
     * 
     * @param binType the type of bin this item is being placed into
     *                ("RECYCLABLE", "ORGANIC", or "HAZARDOUS")
     * @return positive points if correctly sorted, negative if incorrect
     */
    public abstract int process(String binType);

    /**
     * Renders this trash item on the game map using Graphics2D.
     * 
     * <p><b>Polymorphism:</b> Each subclass draws itself differently:
     * plastic as a bottle shape, organic as a blob, hazardous as a diamond.</p>
     * 
     * @param g2 the Graphics2D context to draw on
     */
    public abstract void render(Graphics2D g2);

    /**
     * Returns the color associated with this trash type.
     * Used for visual identification and the carry indicator above the player.
     * 
     * @return the Color representing this item type
     */
    public abstract Color getColor();

    /**
     * Returns the correct bin type for this item.
     * Used to check if the player sorted the item correctly.
     * 
     * @return the correct bin type string ("RECYCLABLE", "ORGANIC", or "HAZARDOUS")
     */
    public abstract String getCorrectBinType();

    /**
     * Checks if this item is near the player (within pickup range of 50 pixels).
     * Uses Euclidean distance for accurate proximity detection.
     * 
     * @param player the player to check distance against
     * @return {@code true} if this item is within 50 pixels of the player
     */
    public boolean isNearPlayer(Player player) {
        float dx = x - player.getX();
        float dy = y - player.getY();
        return Math.sqrt(dx * dx + dy * dy) < 50;
    }

    // ==================== Encapsulated Getters/Setters ====================

    /**
     * Gets the x-coordinate of this item.
     * @return x position in pixels
     */
    public float getX() { return x; }

    /**
     * Gets the y-coordinate of this item.
     * @return y position in pixels
     */
    public float getY() { return y; }

    /**
     * Checks if this item has been collected by the player.
     * @return {@code true} if collected
     */
    public boolean isCollected() { return collected; }

    /**
     * Sets the collected state of this item.
     * @param c {@code true} to mark as collected
     */
    public void setCollected(boolean c) { collected = c; }

    /**
     * Gets the display name of this item.
     * @return the item name
     */
    public String getName() { return name; }

    /**
     * Gets the base points value for correct sorting.
     * @return the point value
     */
    public int getPoints() { return points; }
}

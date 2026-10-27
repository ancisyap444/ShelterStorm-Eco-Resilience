package shelterstorm;

import java.awt.*;
import java.util.Random;

/**
 * PlasticItem.java - A recyclable plastic trash item.
 * 
 * <p><b>OOP Principle: Inheritance</b> — Extends {@link TrashItem}, inheriting
 * common fields (position, points, collected state) and methods (isNearPlayer,
 * getters/setters) while providing plastic-specific behavior.</p>
 * 
 * <p><b>OOP Principle: Polymorphism</b> — Overrides the abstract methods
 * {@link #process(String)}, {@link #render(Graphics2D)}, {@link #getColor()},
 * and {@link #getCorrectBinType()} with plastic-specific implementations.
 * When the game engine holds a {@code TrashItem} reference, calling these
 * methods dispatches to PlasticItem's versions at runtime.</p>
 * 
 * <p>Plastic items include: "Plastic Bottle", "Plastic Bag", "Plastic Cup".
 * They must be sorted into the RECYCLABLE bin for full points.</p>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public class PlasticItem extends TrashItem {

    /** The light-blue color representing plastic items. */
    private static final Color PLASTIC_COLOR = new Color(0, 150, 255);

    /** Possible names for plastic trash items. */
    private static final String[] PLASTIC_NAMES = {
        "Plastic Bottle", "Plastic Bag", "Plastic Cup"
    };

    /**
     * Constructs a new PlasticItem at the given position.
     * The name is randomly selected from the predefined plastic item names.
     * Base points: 10.
     * 
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     */
    public PlasticItem(float x, float y) {
        super(x, y, 10, PLASTIC_NAMES[new Random().nextInt(PLASTIC_NAMES.length)]);
    }

    /**
     * Returns the correct bin type for plastic items.
     * Plastic items belong in the RECYCLABLE bin.
     * 
     * @return "RECYCLABLE"
     */
    @Override
    public String getCorrectBinType() {
        return "RECYCLABLE";
    }

    /**
     * Processes this plastic item by sorting it into the given bin type.
     * 
     * <p><b>Polymorphism in action:</b> PlasticItem awards 10 points for
     * correct sorting (RECYCLABLE bin) and penalizes -5 for incorrect sorting.</p>
     * 
     * @param binType the bin type this item is being sorted into
     * @return +10 if correctly sorted into RECYCLABLE, -5 otherwise
     */
    @Override
    public int process(String binType) {
        if ("RECYCLABLE".equals(binType)) {
            return points; // +10 for correct recycling
        } else {
            return -5; // Penalty for putting plastic in the wrong bin
        }
    }

    /**
     * Returns the light-blue color associated with plastic items.
     * Used for the carry indicator above the player's head.
     * 
     * @return light-blue Color(0, 150, 255)
     */
    @Override
    public Color getColor() {
        return PLASTIC_COLOR;
    }

    /**
     * Renders this plastic item as a small light-blue bottle shape with a "P" label.
     * Only renders if the item has not been collected.
     * 
     * @param g2 the Graphics2D rendering context
     */
    @Override
    public void render(Graphics2D g2) {
        if (collected) return; // Don't render if picked up

        int px = (int) x;
        int py = (int) y;

        // --- Draw bottle body (light-blue rectangle) ---
        g2.setColor(PLASTIC_COLOR);
        g2.fillRect(px, py + 4, 16, 20);

        // --- Draw bottle neck (narrower rectangle on top) ---
        g2.fillRect(px + 4, py, 8, 6);

        // --- Draw bottle cap (darker blue) ---
        g2.setColor(PLASTIC_COLOR.darker());
        g2.fillRect(px + 5, py - 2, 6, 3);

        // --- Border for visibility ---
        g2.setColor(PLASTIC_COLOR.darker());
        g2.drawRect(px, py + 4, 16, 20);
        g2.drawRect(px + 4, py, 8, 6);

        // --- "P" label ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("P", px + 4, py + 19);
    }
}

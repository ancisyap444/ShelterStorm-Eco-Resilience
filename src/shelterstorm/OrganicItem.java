package shelterstorm;

import java.awt.*;
import java.util.Random;

/**
 * OrganicItem.java - An organic/compostable trash item.
 * 
 * <p><b>OOP Principle: Inheritance</b> — Extends {@link TrashItem}, inheriting
 * common fields and methods while providing organic-specific implementations.</p>
 * 
 * <p><b>OOP Principle: Polymorphism</b> — Overrides abstract methods with
 * organic-specific behavior. When sorted into the ORGANIC bin, full points
 * are awarded. Incorrect sorting incurs a -5 penalty.</p>
 * 
 * <p>Organic items include: "Banana Peel", "Food Scraps", "Fallen Leaves".
 * They must be sorted into the ORGANIC bin for full points.</p>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public class OrganicItem extends TrashItem {

    /** The brown color representing organic items. */
    private static final Color ORGANIC_COLOR = new Color(139, 90, 43);

    /** Possible names for organic trash items. */
    private static final String[] ORGANIC_NAMES = {
        "Banana Peel", "Food Scraps", "Fallen Leaves"
    };

    /**
     * Constructs a new OrganicItem at the given position.
     * The name is randomly selected from the predefined organic item names.
     * Base points: 10.
     * 
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     */
    public OrganicItem(float x, float y) {
        super(x, y, 10, ORGANIC_NAMES[new Random().nextInt(ORGANIC_NAMES.length)]);
    }

    /**
     * Returns the correct bin type for organic items.
     * Organic items belong in the ORGANIC bin.
     * 
     * @return "ORGANIC"
     */
    @Override
    public String getCorrectBinType() {
        return "ORGANIC";
    }

    /**
     * Processes this organic item by sorting it into the given bin type.
     * 
     * <p><b>Polymorphism in action:</b> OrganicItem awards 10 points for
     * correct sorting (ORGANIC bin) and penalizes -5 for incorrect sorting.</p>
     * 
     * @param binType the bin type this item is being sorted into
     * @return +10 if correctly sorted into ORGANIC, -5 otherwise
     */
    @Override
    public int process(String binType) {
        if ("ORGANIC".equals(binType)) {
            return points; // +10 for correct composting
        } else {
            return -5; // Penalty for incorrect sorting
        }
    }

    /**
     * Returns the brown color associated with organic items.
     * Used for the carry indicator above the player's head.
     * 
     * @return brown Color(139, 90, 43)
     */
    @Override
    public Color getColor() {
        return ORGANIC_COLOR;
    }

    /**
     * Renders this organic item as a small brown circle/blob with an "O" label.
     * Only renders if the item has not been collected.
     * 
     * @param g2 the Graphics2D rendering context
     */
    @Override
    public void render(Graphics2D g2) {
        if (collected) return; // Don't render if picked up

        int px = (int) x;
        int py = (int) y;

        // --- Draw organic blob (brown irregular oval) ---
        g2.setColor(ORGANIC_COLOR);
        g2.fillOval(px, py + 2, 20, 18);

        // --- Draw a smaller blob on top for organic texture ---
        g2.setColor(ORGANIC_COLOR.brighter());
        g2.fillOval(px + 3, py, 14, 10);

        // --- Border for visibility ---
        g2.setColor(ORGANIC_COLOR.darker());
        g2.drawOval(px, py + 2, 20, 18);

        // --- "O" label ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("O", px + 6, py + 17);
    }
}

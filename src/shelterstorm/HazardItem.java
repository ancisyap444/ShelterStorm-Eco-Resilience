package shelterstorm;

import java.awt.*;
import java.util.Random;

/**
 * HazardItem.java - A hazardous trash item requiring careful handling.
 * 
 * <p><b>OOP Principle: Inheritance</b> — Extends {@link TrashItem}, inheriting
 * the common trash item structure while adding hazardous-specific behavior.</p>
 * 
 * <p><b>OOP Principle: Polymorphism</b> — Overrides abstract methods with
 * hazardous-specific implementations. Notably, HazardItem has higher rewards
 * for correct sorting (+15) and harsher penalties for incorrect sorting (-10),
 * demonstrating how polymorphism enables different behavior through the same
 * {@link TrashItem#process(String)} interface.</p>
 * 
 * <p>Hazardous items include: "Old Battery", "Chemical Can", "Broken Glass".
 * They must be sorted into the HAZARDOUS bin. Mishandling is heavily penalized
 * to reflect the real-world danger of improper hazardous waste disposal.</p>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public class HazardItem extends TrashItem {

    /** The red color representing hazardous items. */
    private static final Color HAZARD_COLOR = new Color(255, 60, 60);

    /** Possible names for hazardous trash items. */
    private static final String[] HAZARD_NAMES = {
        "Old Battery", "Chemical Can", "Broken Glass"
    };

    /**
     * Constructs a new HazardItem at the given position.
     * The name is randomly selected from the predefined hazardous item names.
     * Base points: 15 (higher reward for correctly handling dangerous items).
     * 
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     */
    public HazardItem(float x, float y) {
        super(x, y, 15, HAZARD_NAMES[new Random().nextInt(HAZARD_NAMES.length)]);
    }

    /**
     * Returns the correct bin type for hazardous items.
     * Hazardous items belong in the HAZARDOUS bin.
     * 
     * @return "HAZARDOUS"
     */
    @Override
    public String getCorrectBinType() {
        return "HAZARDOUS";
    }

    /**
     * Processes this hazardous item by sorting it into the given bin type.
     * 
     * <p><b>Polymorphism in action:</b> HazardItem has a higher reward (+15)
     * for correct sorting and a much harsher penalty (-10) for incorrect
     * sorting, reflecting the real-world danger of mishandling hazardous waste.
     * Compare this with PlasticItem (+10/-5) and OrganicItem (+10/-5).</p>
     * 
     * @param binType the bin type this item is being sorted into
     * @return +15 if correctly sorted into HAZARDOUS, -10 otherwise
     */
    @Override
    public int process(String binType) {
        if ("HAZARDOUS".equals(binType)) {
            return points; // +15 for correct hazardous disposal
        } else {
            return -10; // Harsh penalty — hazardous waste is dangerous!
        }
    }

    /**
     * Returns the red color associated with hazardous items.
     * Used for the carry indicator above the player's head.
     * 
     * @return red Color(255, 60, 60)
     */
    @Override
    public Color getColor() {
        return HAZARD_COLOR;
    }

    /**
     * Renders this hazardous item as a small red diamond/warning shape
     * with an "H" label and an exclamation mark "!" for danger indication.
     * Only renders if the item has not been collected.
     * 
     * @param g2 the Graphics2D rendering context
     */
    @Override
    public void render(Graphics2D g2) {
        if (collected) return; // Don't render if picked up

        int px = (int) x;
        int py = (int) y;
        int cx = px + 10; // Center x of the diamond
        int cy = py + 10; // Center y of the diamond

        // --- Draw diamond/warning shape (rotated square) ---
        int[] diamondX = {cx, cx + 12, cx, cx - 12};
        int[] diamondY = {cy - 12, cy, cy + 12, cy};
        g2.setColor(HAZARD_COLOR);
        g2.fillPolygon(diamondX, diamondY, 4);

        // --- Diamond border ---
        g2.setColor(HAZARD_COLOR.darker());
        g2.drawPolygon(diamondX, diamondY, 4);

        // --- Inner warning border (darker) ---
        int[] innerX = {cx, cx + 8, cx, cx - 8};
        int[] innerY = {cy - 8, cy, cy + 8, cy};
        g2.setColor(new Color(180, 30, 30));
        g2.drawPolygon(innerX, innerY, 4);

        // --- "H" label ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.drawString("H", cx - 4, cy + 4);

        // --- Danger "!" indicator above the diamond ---
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("!", cx - 2, cy - 13);
    }
}

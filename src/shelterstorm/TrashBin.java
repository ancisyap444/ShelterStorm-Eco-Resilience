package shelterstorm;

import java.awt.*;

/**
 * TrashBin.java - A sorting bin where the player deposits collected trash items.
 * 
 * <p><b>OOP Principle: Abstraction</b> — Implements the {@link Interactable} interface,
 * providing a concrete interaction behavior (trash sorting). The game engine
 * treats TrashBin objects the same as any other Interactable.</p>
 * 
 * <p><b>OOP Principle: Polymorphism (usage)</b> — When a player deposits a
 * {@link TrashItem} into a bin, the bin calls {@link TrashItem#process(String)}.
 * Thanks to polymorphism, the correct subclass method is invoked at runtime:
 * {@link PlasticItem#process(String)}, {@link OrganicItem#process(String)}, or
 * {@link HazardItem#process(String)} — each with different scoring logic.</p>
 * 
 * <p><b>OOP Principle: Encapsulation</b> — Internal state (itemsSorted, binType)
 * is private, accessible only through public methods.</p>
 * 
 * <p>There are three bin types, each color-coded:</p>
 * <ul>
 *   <li><b>RECYCLABLE</b> — Blue bin for plastic items</li>
 *   <li><b>ORGANIC</b> — Green bin for organic/compostable items</li>
 *   <li><b>HAZARDOUS</b> — Red bin for dangerous waste items</li>
 * </ul>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public class TrashBin implements Interactable {

    // ==================== Private Fields (Encapsulation) ====================

    /** Position of this bin on the map (in pixels). */
    private float x, y;

    /** The type of waste this bin accepts ("RECYCLABLE", "ORGANIC", or "HAZARDOUS"). */
    private String binType;

    /** The display color of this bin, determined by its type. */
    private Color color;

    /** Tracks the total number of items correctly sorted into this bin. */
    private int itemsSorted = 0;

    /** Bin dimensions in pixels. */
    private static final int WIDTH = 44;
    private static final int HEIGHT = 48;

    /** Interaction range in pixels. */
    private static final float INTERACTION_RANGE = 60f;

    /**
     * Constructs a new TrashBin at the given position with the specified type.
     * The bin color is automatically determined by the type.
     * 
     * @param x       the x-coordinate in pixels
     * @param y       the y-coordinate in pixels
     * @param binType the type of waste accepted ("RECYCLABLE", "ORGANIC", or "HAZARDOUS")
     */
    public TrashBin(float x, float y, String binType) {
        this.x = x;
        this.y = y;
        this.binType = binType;

        // Assign color based on bin type
        switch (binType) {
            case "RECYCLABLE":
                this.color = new Color(30, 100, 200); // Blue
                break;
            case "ORGANIC":
                this.color = new Color(30, 160, 50);  // Green
                break;
            case "HAZARDOUS":
                this.color = new Color(200, 40, 40);  // Red
                break;
            default:
                this.color = Color.GRAY;               // Fallback
                break;
        }
    }

    // ==================== Interactable Implementation ====================

    /**
     * Interacts with this bin to sort the player's held trash item.
     * 
     * <p><b>Polymorphism demonstration:</b> The bin calls {@code item.process(this.binType)}
     * on the held TrashItem reference. At runtime, Java dispatches this to the correct
     * subclass method (PlasticItem, OrganicItem, or HazardItem), each with different
     * scoring rules — without the bin needing to know the specific item type.</p>
     * 
     * <p>If the item is correctly sorted:</p>
     * <ul>
     *   <li>Points are added to the shelter score</li>
     *   <li>Shelter flood water level decreases by 2 (eco-reward)</li>
     *   <li>The bin's sorted count increments</li>
     * </ul>
     * 
     * <p>If the item is incorrectly sorted, the penalty points are subtracted.</p>
     * 
     * @param player  the player depositing the item
     * @param shelter the shelter instance (for score and water level updates)
     */
    @Override
    public void interact(Player player, Shelter shelter) {
        TrashItem item = player.getHeldItem();

        if (item != null) {
            /*
             * Polymorphic call: item.process() dispatches to the correct
             * subclass implementation based on the actual runtime type.
             *   - PlasticItem.process("RECYCLABLE") → +10
             *   - PlasticItem.process("ORGANIC")    → -5
             *   - HazardItem.process("HAZARDOUS")   → +15
             *   - HazardItem.process("RECYCLABLE")  → -10
             */
            int result = item.process(this.binType);

            if (result > 0) {
                // Correct sort — reward the player
                shelter.addScore(result);
                shelter.decreaseWater(2); // Eco-benefit: proper sorting reduces flood level
                shelter.incrementItemsSorted();
                itemsSorted++;
            } else {
                // Wrong sort — apply penalty
                shelter.addScore(result); // result is negative, so this subtracts
            }

            // Remove the item from the player's hands regardless of result
            player.clearHeldItem();
        }
    }

    /**
     * Checks if the player is within interaction range (60 pixels) of this bin.
     * Uses Euclidean distance from center-to-center.
     * 
     * @param player the player to check distance against
     * @return {@code true} if the player is within 60 pixels
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
     * Returns a context-sensitive interaction hint.
     * If the player is holding an item, shows which bin type this is.
     * If the player has empty hands, prompts them to pick up trash first.
     * 
     * @return the hint string
     */
    @Override
    public String getInteractHint() {
        // Note: This method doesn't have access to the player directly,
        // so the game panel should check player state when displaying hints.
        // We return the bin-type prompt by default.
        return "Press E to sort into " + binType;
    }

    /**
     * Returns a hint that accounts for whether the player is holding an item.
     * This overloaded version allows context-sensitive messaging.
     * 
     * @param player the player to check held item status
     * @return appropriate hint based on player's held item
     */
    public String getInteractHint(Player player) {
        if (player.getHeldItem() != null) {
            return "Press E to sort into " + binType;
        } else {
            return "Pick up trash first!";
        }
    }

    // ==================== Rendering ====================

    /**
     * Renders this trash bin as a colored rectangle with a lid on top.
     * The bin type label and sorted item count are displayed on the bin.
     * 
     * @param g2 the Graphics2D rendering context
     */
    public void render(Graphics2D g2) {
        int px = (int) x;
        int py = (int) y;

        // --- Draw bin body (colored rectangle) ---
        g2.setColor(color);
        g2.fillRect(px, py + 8, WIDTH, HEIGHT - 8);

        // --- Draw darker border ---
        g2.setColor(color.darker());
        g2.drawRect(px, py + 8, WIDTH, HEIGHT - 8);

        // --- Draw bin lid (slightly wider, darker) ---
        g2.setColor(color.darker());
        g2.fillRect(px - 2, py, WIDTH + 4, 10);
        g2.setColor(color.darker().darker());
        g2.drawRect(px - 2, py, WIDTH + 4, 10);

        // --- Draw lid handle (small rectangle on top center) ---
        g2.setColor(color.darker().darker());
        g2.fillRect(px + WIDTH / 2 - 6, py - 3, 12, 4);

        // --- Draw bin type label (abbreviated for space) ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 9));
        FontMetrics fm = g2.getFontMetrics();

        // Use abbreviated label for smaller bins
        String label;
        switch (binType) {
            case "RECYCLABLE": label = "RECYCLE"; break;
            case "ORGANIC":    label = "ORGANIC"; break;
            case "HAZARDOUS":  label = "HAZARD";  break;
            default:           label = binType;   break;
        }

        int labelWidth = fm.stringWidth(label);
        g2.drawString(label, px + (WIDTH - labelWidth) / 2, py + 26);

        // --- Draw recycle/type icon symbol ---
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String symbol;
        switch (binType) {
            case "RECYCLABLE": symbol = "\u267B"; break; // ♻ recycle symbol
            case "ORGANIC":    symbol = "\u2618"; break; // ☘ shamrock
            case "HAZARDOUS":  symbol = "\u2622"; break; // ☢ radioactive
            default:           symbol = "?";       break;
        }
        int symWidth = g2.getFontMetrics().stringWidth(symbol);
        g2.drawString(symbol, px + (WIDTH - symWidth) / 2, py + 42);

        // --- Draw items sorted count below the bin ---
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(Color.WHITE);
        String countText = "Sorted: " + itemsSorted;
        int countWidth = g2.getFontMetrics().stringWidth(countText);
        g2.drawString(countText, px + (WIDTH - countWidth) / 2, py + HEIGHT + 12);
    }

    // ==================== Getters ====================

    /** @return the bin's x-coordinate in pixels */
    public float getX() { return x; }

    /** @return the bin's y-coordinate in pixels */
    public float getY() { return y; }

    /** @return the waste type this bin accepts */
    public String getBinType() { return binType; }

    /** @return the total number of items correctly sorted into this bin */
    public int getItemsSorted() { return itemsSorted; }
}

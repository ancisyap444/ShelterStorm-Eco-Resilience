package shelterstorm;

/**
 * Interactable.java - Interface for all interactive game objects.
 * 
 * <p><b>OOP Principle: Abstraction</b></p>
 * <p>This interface defines a contract that all interactive objects must follow.
 * The {@link Player} class doesn't need to know the implementation details of
 * each interactable object — it only needs to know that it can call
 * {@code interact()}, {@code isNear()}, and {@code getInteractHint()}.
 * This decouples the Player from specific game object types.</p>
 * 
 * <p>Classes implementing this interface include:</p>
 * <ul>
 *   <li>{@link Evacuee} — NPCs the player can comfort</li>
 *   <li>{@link TrashBin} — Bins the player can sort trash into</li>
 * </ul>
 * 
 * @author ShelterStorm Team
 * @version 1.0
 */
public interface Interactable {

    /**
     * Performs the interaction between the player and this object.
     * Each implementing class defines its own behavior:
     * <ul>
     *   <li>Evacuee: comforts the evacuee, reducing anxiety</li>
     *   <li>TrashBin: sorts the player's held trash item</li>
     * </ul>
     * 
     * @param player  the player performing the interaction
     * @param shelter the shelter instance (for updating score/resources)
     */
    void interact(Player player, Shelter shelter);

    /**
     * Checks whether the player is within interaction range of this object.
     * The standard interaction range is 64 pixels.
     * 
     * @param player the player to check proximity against
     * @return {@code true} if the player is within interaction range
     */
    boolean isNear(Player player);

    /**
     * Returns a context-sensitive hint string displayed to the player
     * when they are near this object. Examples:
     * <ul>
     *   <li>"Press E to comfort"</li>
     *   <li>"Press E to sort into RECYCLABLE"</li>
     * </ul>
     * 
     * @return the interaction hint text
     */
    String getInteractHint();
}

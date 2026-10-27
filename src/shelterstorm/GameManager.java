package shelterstorm;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GameManager.java - Central game state controller and simulation loop.
 * 
 * <p><b>OOP Principles Demonstrated:</b></p>
 * <ul>
 *   <li><b>Encapsulation:</b> Manages entities, timers, and level states internally.</li>
 *   <li><b>Polymorphism:</b> Handles collections of abstract types ({@link TrashItem},
 *       {@link Hazard}) and interfaces ({@link Interactable}) uniformly during update and render.</li>
 *   <li><b>Abstraction:</b> Shields the UI panel from the underlying simulation details.</li>
 * </ul>
 * 
 * @author ShelterStorm Team
 */
public class GameManager {

    // ==================== Game Entities & State ====================

    private Player player;
    private Shelter shelter;
    private List<Evacuee> evacuees;
    private List<TrashItem> trashItems;
    private List<Hazard> hazards;
    private List<TrashBin> trashBins;
    private StormEvent stormEvent;
    private Camera camera;

    private int currentLevel;
    private float roundTimer; // Countdown in seconds
    private int score;

    // Timers for spawning
    private float trashSpawnTimer;
    private float hazardSpawnTimer;

    // Helper random generator
    private final Random random = new Random();

    // Map boundary constants
    public static final int MAP_WIDTH = 960;
    public static final int MAP_HEIGHT = 672;

    // Interact key debounce
    private boolean interactKeyWasDown = false;
    private boolean dropKeyWasDown = false;

    /**
     * Constructs a new GameManager and initializes Level 1.
     */
    public GameManager() {
        this.camera = new Camera();
        initLevel(1);
    }

    /**
     * Initializes a level by creating all entities, bins, evacuees, and storm conditions.
     * 
     * @param level level number (1 = Light Storm, 2 = Heavy Typhoon, 3 = Flash Flood)
     */
    public void initLevel(int level) {
        this.currentLevel = Math.max(1, Math.min(3, level));
        this.roundTimer = 120.0f; // 2 minutes per level
        this.trashSpawnTimer = 0.0f;
        this.hazardSpawnTimer = 5.0f;

        // Create player at center
        this.player = new Player(MAP_WIDTH / 2f - 20, MAP_HEIGHT / 2f - 20);

        // Create or reset shelter
        int prevScore = (shelter != null) ? shelter.getScore() : 0;
        int prevSorted = (shelter != null) ? shelter.getItemsSorted() : 0;
        int prevComforted = (shelter != null) ? shelter.getEvacueesComforted() : 0;

        this.shelter = new Shelter();
        this.shelter.setScore(prevScore);
        // Retain lifetime stats
        for (int i = 0; i < prevSorted; i++) this.shelter.incrementItemsSorted();
        for (int i = 0; i < prevComforted; i++) this.shelter.incrementEvacueesComforted();
        this.shelter.setPlayer(player);

        // Setup storm
        this.stormEvent = new StormEvent(currentLevel);

        // Initialize entity collections
        this.trashItems = new ArrayList<>();
        this.hazards = new ArrayList<>();
        this.evacuees = new ArrayList<>();
        this.trashBins = new ArrayList<>();

        // Place the 3 recycling bins near the top of the shelter
        trashBins.add(new TrashBin(160, 60, "RECYCLABLE"));
        trashBins.add(new TrashBin(450, 60, "ORGANIC"));
        trashBins.add(new TrashBin(740, 60, "HAZARDOUS"));

        // Spawn evacuees according to level difficulty
        int evacueeCount = (currentLevel == 1) ? 3 : (currentLevel == 2) ? 5 : 7;
        for (int i = 0; i < evacueeCount; i++) {
            float ex = 100 + random.nextFloat() * (MAP_WIDTH - 200);
            float ey = 160 + random.nextFloat() * (MAP_HEIGHT - 260);
            int startAnxiety = 20 + random.nextInt(25 * currentLevel);
            evacuees.add(new Evacuee(ex, ey, startAnxiety));
        }

        // Spawn initial trash items
        int initialTrash = 3 + currentLevel * 2;
        for (int i = 0; i < initialTrash; i++) {
            spawnRandomTrash();
        }
    }

    /**
     * Resets the entire game back to Level 1.
     */
    public void restartGame() {
        if (shelter != null) {
            shelter.setScore(0);
        }
        this.score = 0;
        initLevel(1);
    }

    /**
     * Advances to the next level.
     */
    public void nextLevel() {
        if (currentLevel < 3) {
            initLevel(currentLevel + 1);
        }
    }

    // ==================== Update Loop ====================

    /**
     * Updates game state by one simulation frame (~60 FPS).
     */
    public void update() {
        float dt = 1.0f / 60.0f;

        // Update timer
        roundTimer = Math.max(0, roundTimer - dt);

        // Update player movement
        player.update();

        // Update camera
        camera.update(player);

        // Handle player interactions
        handlePlayerInput();

        // Update evacuees
        for (Evacuee e : evacuees) {
            e.update(dt);
        }

        // Update storm
        stormEvent.update(evacuees);

        // Update hazards (Polymorphic applyEffect call)
        Iterator<Hazard> hazardIter = hazards.iterator();
        while (hazardIter.hasNext()) {
            Hazard h = hazardIter.next();
            h.update();
            if (h.isActive()) {
                h.applyEffect(shelter);
            } else {
                hazardIter.remove();
            }
        }

        // Passive environmental simulation
        updateEnvironment();

        // Spawners
        updateSpawners(dt);

        // Sync score
        this.score = shelter.getScore();
    }

    /**
     * Handles E (interact/pickup/sort) and Q (drop) with debouncing.
     */
    private void handlePlayerInput() {
        boolean ePressed = player.isInteractPressed();
        boolean qPressed = player.isDropPressed();

        // Edge-triggered E key press
        if (ePressed && !interactKeyWasDown) {
            performInteraction();
        }
        interactKeyWasDown = ePressed;

        // Edge-triggered Q key press
        if (qPressed && !dropKeyWasDown) {
            if (player.hasItem()) {
                TrashItem dropped = player.dropItem();
                if (dropped != null) {
                    trashItems.add(dropped);
                }
            }
        }
        dropKeyWasDown = qPressed;
    }

    /**
     * Performs player interaction based on nearby targets:
     * 1. If near a bin and holding item -> sort into bin.
     * 2. If near an evacuee -> comfort evacuee.
     * 3. If near a trash item on the floor and hands empty -> pick up.
     */
    private void performInteraction() {
        // 1. Check TrashBins first
        for (TrashBin bin : trashBins) {
            if (bin.isNear(player)) {
                bin.interact(player, shelter);
                return;
            }
        }

        // 2. Check Evacuees next
        for (Evacuee ev : evacuees) {
            if (ev.isNear(player)) {
                ev.interact(player, shelter);
                return;
            }
        }

        // 3. Check Trash Items on ground (only if player has free hands)
        if (!player.hasItem()) {
            TrashItem closest = null;
            float minDist = Float.MAX_VALUE;
            for (TrashItem item : trashItems) {
                if (item.isNearPlayer(player)) {
                    float dx = item.getX() - player.getX();
                    float dy = item.getY() - player.getY();
                    float d = dx * dx + dy * dy;
                    if (d < minDist) {
                        minDist = d;
                        closest = item;
                    }
                }
            }

            if (closest != null) {
                player.pickUp(closest);
                trashItems.remove(closest);
            }
        }
    }

    /**
     * Passive shelter water & panic updates.
     */
    private void updateEnvironment() {
        // Base water creep from rainfall
        float baseRainRise = 0.008f * currentLevel;
        shelter.addWater(baseRainRise);

        // Community panic ticks upward if there are active hazards or flooding
        if (!hazards.isEmpty()) {
            shelter.addPanic(0.015f);
        }
        if (shelter.getWaterLevel() > 50.0f) {
            shelter.addPanic(0.01f);
        }

        // Evacuees in PANICKING state raise community panic
        for (Evacuee ev : evacuees) {
            if ("PANICKING".equals(ev.getState())) {
                shelter.addPanic(0.008f);
            }
        }
    }

    /**
     * Spawns trash and hazards periodically.
     */
    private void updateSpawners(float dt) {
        trashSpawnTimer -= dt;
        hazardSpawnTimer -= dt;

        // Trash spawn interval depends on level
        float trashInterval = (currentLevel == 1) ? 3.5f : (currentLevel == 2) ? 2.5f : 1.8f;
        if (trashSpawnTimer <= 0) {
            if (trashItems.size() < 12) {
                spawnRandomTrash();
            }
            trashSpawnTimer = trashInterval;
        }

        // Hazard spawn interval depends on level
        float hazardInterval = (currentLevel == 1) ? 12.0f : (currentLevel == 2) ? 8.0f : 5.5f;
        if (hazardSpawnTimer <= 0) {
            if (hazards.size() < 4) {
                spawnRandomHazard();
            }
            hazardSpawnTimer = hazardInterval;
        }
    }

    private void spawnRandomTrash() {
        float tx = 80 + random.nextFloat() * (MAP_WIDTH - 160);
        float ty = 140 + random.nextFloat() * (MAP_HEIGHT - 220);

        int type = random.nextInt(3);
        TrashItem item;
        switch (type) {
            case 0: item = new PlasticItem(tx, ty); break;
            case 1: item = new OrganicItem(tx, ty); break;
            default: item = new HazardItem(tx, ty); break;
        }
        trashItems.add(item);
    }

    private void spawnRandomHazard() {
        float hx = 100 + random.nextFloat() * (MAP_WIDTH - 200);
        float hy = 160 + random.nextFloat() * (MAP_HEIGHT - 240);

        // At level 1, only FloodHazards. Level 2 & 3 add ElectricalHazards.
        if (currentLevel == 1 || random.nextBoolean()) {
            hazards.add(new FloodHazard(hx, hy));
        } else {
            hazards.add(new ElectricalHazard(hx, hy));
        }
    }

    // ==================== Rendering ====================

    /**
     * Renders the game world, entities, hazards, storm, and interaction hints.
     * 
     * @param g2 Graphics2D context
     */
    public void render(Graphics2D g2) {
        // 1. Draw Shelter map background (flooring, walls, room areas)
        drawShelterMap(g2);

        // 2. Draw Trash Bins (Interactables)
        for (TrashBin bin : trashBins) {
            bin.render(g2);
        }

        // 3. Draw Hazards (Polymorphic rendering)
        for (Hazard h : hazards) {
            h.render(g2);
        }

        // 4. Draw Trash items on floor (Polymorphic rendering)
        for (TrashItem item : trashItems) {
            item.render(g2);
        }

        // 5. Draw Evacuees (Interactables)
        for (Evacuee ev : evacuees) {
            ev.render(g2);
        }

        // 6. Draw Player
        player.render(g2);

        // 7. Draw Storm atmospheric overlay (Rain + Thunder)
        stormEvent.render(g2, MAP_WIDTH, MAP_HEIGHT);

        // 8. Draw contextual interaction prompt above player head
        drawContextHint(g2);
    }

    /**
     * Draws the shelter interior layout.
     */
    private void drawShelterMap(Graphics2D g2) {
        // Concrete floor background
        g2.setColor(new Color(55, 60, 75));
        g2.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);

        // Floor tile lines
        g2.setColor(new Color(45, 50, 65));
        for (int x = 0; x < MAP_WIDTH; x += 48) {
            g2.drawLine(x, 0, x, MAP_HEIGHT);
        }
        for (int y = 0; y < MAP_HEIGHT; y += 48) {
            g2.drawLine(0, y, MAP_WIDTH, y);
        }

        // Outer reinforced walls
        g2.setColor(new Color(30, 35, 45));
        g2.fillRect(0, 0, MAP_WIDTH, 20); // Top wall
        g2.fillRect(0, MAP_HEIGHT - 20, MAP_WIDTH, 20); // Bottom wall
        g2.fillRect(0, 0, 20, MAP_HEIGHT); // Left wall
        g2.fillRect(MAP_WIDTH - 20, 0, 20, MAP_HEIGHT); // Right wall

        // Designated "SORTING STATION" header bar
        g2.setColor(new Color(40, 50, 70, 180));
        g2.fillRect(100, 25, 760, 30);
        g2.setColor(new Color(180, 200, 225));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.drawString("EMERGENCY RECYCLING & SALVAGE STATION", 310, 45);

        // If water level > 0, draw subtle blue water tint over entire room
        float water = shelter.getWaterLevel();
        if (water > 0) {
            int alpha = (int) (water * 0.9f); // up to 90 alpha
            g2.setColor(new Color(20, 100, 200, Math.min(120, alpha)));
            g2.fillRect(0, 0, MAP_WIDTH, MAP_HEIGHT);
        }
    }

    /**
     * Draws an interaction hint pill above the player's head if near an object.
     */
    private void drawContextHint(Graphics2D g2) {
        String hint = null;

        // Check bins
        for (TrashBin bin : trashBins) {
            if (bin.isNear(player)) {
                hint = bin.getInteractHint();
                break;
            }
        }

        // Check evacuees
        if (hint == null) {
            for (Evacuee ev : evacuees) {
                if (ev.isNear(player)) {
                    hint = ev.getInteractHint();
                    break;
                }
            }
        }

        // Check trash on floor
        if (hint == null && !player.hasItem()) {
            for (TrashItem item : trashItems) {
                if (item.isNearPlayer(player)) {
                    hint = "Press E to pick up " + item.getName();
                    break;
                }
            }
        }

        // Render hint bubble
        if (hint != null) {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(hint);
            int hx = (int) player.getX() + player.getWidth() / 2 - textW / 2;
            int hy = (int) player.getY() - 35;

            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRoundRect(hx - 8, hy - 14, textW + 16, 20, 8, 8);
            g2.setColor(new Color(255, 230, 100));
            g2.drawRoundRect(hx - 8, hy - 14, textW + 16, 20, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(hint, hx, hy);
        }
    }

    // ==================== Win / Loss Conditions ====================

    /**
     * Checks if the shelter was overwhelmed by floods or panic.
     * 
     * @return true if game is lost
     */
    public boolean checkGameOver() {
        return shelter != null && shelter.isOverwhelmed();
    }

    /**
     * Checks if the current level was survived successfully.
     * 
     * @return true if timer reached 0 and shelter is safe
     */
    public boolean checkLevelComplete() {
        return roundTimer <= 0 && !checkGameOver();
    }

    // ==================== Getters ====================

    public Player getPlayer() { return player; }
    public Shelter getShelter() { return shelter; }
    public int getCurrentLevel() { return currentLevel; }
    public float getRoundTimer() { return roundTimer; }
    public int getScore() { return score; }
}

package shelterstorm;

/**
 * SoundManager.java - Centralized audio management using the Singleton pattern.
 * 
 * Demonstrates the SINGLETON DESIGN PATTERN:
 * - Private constructor prevents external instantiation.
 * - A single static instance is created lazily and shared globally.
 * - All game audio is accessed through SoundManager.getInstance().
 * 
 * Why Singleton here?
 * - Only one audio system should exist (avoids duplicate/overlapping clips).
 * - All game objects need access to the same sound manager.
 * - Centralized control allows features like mute, volume, and stopAll().
 * 
 * Currently, methods are stubs awaiting audio file integration.
 * The pattern is fully implemented and ready for javax.sound.sampled clips.
 * 
 * @author ShelterStorm Team
 */
public class SoundManager {

    // =========================================================================
    // SINGLETON PATTERN: Private static instance and private constructor
    // =========================================================================

    /** 
     * The single, shared instance of SoundManager.
     * Lazily initialized on first access via getInstance().
     */
    private static SoundManager instance;

    /** Whether all sounds are globally muted. */
    private boolean muted;

    /** Master volume level (0.0 = silent, 1.0 = full volume). */
    private float masterVolume;

    // TODO: Add javax.sound.sampled Clip fields here for each sound effect
    // private Clip rainClip;
    // private Clip thunderClip;
    // private Clip pickupClip;
    // private Clip sortClip;
    // private Clip comfortClip;
    // private Clip alarmClip;

    /**
     * Private constructor - prevents direct instantiation from outside.
     * This is a key part of the Singleton pattern: only this class
     * can create an instance of itself.
     */
    private SoundManager() {
        this.muted = false;
        this.masterVolume = 0.8f;

        // TODO: Load audio clips here
        // Example:
        // try {
        //     AudioInputStream ais = AudioSystem.getAudioInputStream(
        //         getClass().getResourceAsStream("/sounds/rain.wav"));
        //     rainClip = AudioSystem.getClip();
        //     rainClip.open(ais);
        // } catch (Exception e) {
        //     System.err.println("Failed to load rain sound: " + e.getMessage());
        // }
    }

    /**
     * Returns the single shared instance of SoundManager.
     * Creates the instance on first call (lazy initialization).
     * 
     * This is the global access point for the Singleton pattern.
     *
     * @return the SoundManager instance
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    // =========================================================================
    // Sound Playback Methods (Stubs)
    // =========================================================================

    /**
     * Plays the ambient rain sound effect on loop.
     * Intensity can vary based on the current level.
     */
    public void playRain() {
        if (muted) return;
        // TODO: Add javax.sound.sampled clip playback here
        // if (rainClip != null) {
        //     rainClip.setFramePosition(0);
        //     rainClip.loop(Clip.LOOP_CONTINUOUSLY);
        // }
        System.out.println("[SoundManager] Playing rain ambience...");
    }

    /**
     * Plays a thunder sound effect.
     * Triggered randomly during storms for atmosphere.
     */
    public void playThunder() {
        if (muted) return;
        // TODO: Add javax.sound.sampled clip playback here
        // if (thunderClip != null) {
        //     thunderClip.setFramePosition(0);
        //     thunderClip.start();
        // }
        System.out.println("[SoundManager] Playing thunder crack...");
    }

    /**
     * Plays the item pickup sound effect.
     * Triggered when the player picks up a trash item.
     */
    public void playPickup() {
        if (muted) return;
        // TODO: Add javax.sound.sampled clip playback here
        // if (pickupClip != null) {
        //     pickupClip.setFramePosition(0);
        //     pickupClip.start();
        // }
        System.out.println("[SoundManager] Playing pickup sound...");
    }

    /**
     * Plays the sorting confirmation sound effect.
     * Triggered when the player correctly sorts trash into a bin.
     */
    public void playSort() {
        if (muted) return;
        // TODO: Add javax.sound.sampled clip playback here
        // if (sortClip != null) {
        //     sortClip.setFramePosition(0);
        //     sortClip.start();
        // }
        System.out.println("[SoundManager] Playing sort confirmation...");
    }

    /**
     * Plays the comforting sound effect.
     * Triggered when the player comforts a panicking evacuee.
     */
    public void playComfort() {
        if (muted) return;
        // TODO: Add javax.sound.sampled clip playback here
        // if (comfortClip != null) {
        //     comfortClip.setFramePosition(0);
        //     comfortClip.start();
        // }
        System.out.println("[SoundManager] Playing comfort sound...");
    }

    /**
     * Plays the alarm/warning sound effect.
     * Triggered when shelter meters reach critical levels.
     */
    public void playAlarm() {
        if (muted) return;
        // TODO: Add javax.sound.sampled clip playback here
        // if (alarmClip != null) {
        //     alarmClip.setFramePosition(0);
        //     alarmClip.start();
        // }
        System.out.println("[SoundManager] Playing alarm warning...");
    }

    /**
     * Stops all currently playing sound effects and ambient audio.
     * Called during state transitions (e.g., going to pause menu).
     */
    public void stopAll() {
        // TODO: Stop all javax.sound.sampled clips here
        // if (rainClip != null && rainClip.isRunning()) rainClip.stop();
        // if (thunderClip != null && thunderClip.isRunning()) thunderClip.stop();
        // if (pickupClip != null && pickupClip.isRunning()) pickupClip.stop();
        // if (sortClip != null && sortClip.isRunning()) sortClip.stop();
        // if (comfortClip != null && comfortClip.isRunning()) comfortClip.stop();
        // if (alarmClip != null && alarmClip.isRunning()) alarmClip.stop();
        System.out.println("[SoundManager] All sounds stopped.");
    }

    // =========================================================================
    // Volume & Mute Controls
    // =========================================================================

    /**
     * Toggles the global mute state.
     * When muted, no sound effects will play.
     */
    public void toggleMute() {
        this.muted = !this.muted;
        if (muted) {
            stopAll();
        }
        System.out.println("[SoundManager] Muted: " + muted);
    }

    /**
     * Checks if the sound manager is currently muted.
     * @return true if muted
     */
    public boolean isMuted() {
        return muted;
    }

    /**
     * Sets the master volume level.
     * @param volume volume level (0.0 to 1.0, clamped)
     */
    public void setMasterVolume(float volume) {
        this.masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        // TODO: Apply volume to all active clips via FloatControl
    }

    /**
     * Gets the current master volume level.
     * @return volume level (0.0 to 1.0)
     */
    public float getMasterVolume() {
        return masterVolume;
    }
}

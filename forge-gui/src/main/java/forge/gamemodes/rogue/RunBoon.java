package forge.gamemodes.rogue;

/**
 * Serializable snapshot of an active boon during a run.
 * Stores the boon ID, its rank, and remaining consumption charges.
 * Charges of -1 means permanent (never consumed).
 */
public class RunBoon {
    private String id;
    private int rank;
    private int charges;  // -1 = permanent, >0 = remaining charges

    public RunBoon() {} // XStream

    public RunBoon(String id, int rank, int charges) {
        this.id = id;
        this.rank = rank;
        this.charges = charges;
    }

    public String getId() { return id; }
    public int getRank() { return rank; }
    public int getCharges() { return charges; }

    /** Decrement charges. Returns true if fully consumed (charges hit 0). */
    public boolean consumeCharge() {
        if (charges < 0) return false; // permanent
        charges--;
        return charges <= 0;
    }
}

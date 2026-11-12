package cwrm.model;

/**
 * A waste collector who completes or cancels pickups.
 */
public class Collector {
    private final String id;
    private final String name;

    /**
     * Creates a collector.
     *
     * @param id   unique collector ID
     * @param name collector's display name
     */
    public Collector(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /** @return the unique collector ID */
    public String getId() {
        return id;
    }

    /** @return the collector's display name */
    public String getName() {
        return name;
    }
}

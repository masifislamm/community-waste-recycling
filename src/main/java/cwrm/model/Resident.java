package cwrm.model;

/**
 * A resident who books pickups and earns reward points.
 *
 * <p>Each resident owns exactly one {@link RewardAccount} (composition): the account
 * is created together with the resident and is never shared.
 */
public class Resident {
    private final String id;
    private final String name;
    private final RewardAccount account;

    /**
     * Creates a resident with a new, empty reward account.
     *
     * @param id   unique resident ID
     * @param name resident's display name
     */
    public Resident(String id, String name) {
        this.id = id;
        this.name = name;
        this.account = new RewardAccount();
    }

    /** @return the unique resident ID */
    public String getId() {
        return id;
    }

    /** @return the resident's display name */
    public String getName() {
        return name;
    }

    /** @return the resident's reward account */
    public RewardAccount getAccount() {
        return account;
    }
}

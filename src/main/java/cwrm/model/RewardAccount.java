package cwrm.model;

/**
 * A resident's reward-point balance. Points are added when a pickup is completed
 * and can be redeemed later. The balance can never go below zero.
 */
public class RewardAccount {
    private static final String NEGATIVE_POINTS = "Points must not be negative.";
    private static final String NOT_ENOUGH_POINTS = "Cannot redeem %d points: the balance is only %d.";

    private int balance;

    /** Creates an account with a balance of zero. */
    public RewardAccount() {
        this.balance = 0;
    }

    /** @return the current number of points */
    public int getBalance() {
        return balance;
    }

    /**
     * Adds points to the balance.
     *
     * @param points points to add; zero or more
     * @throws IllegalArgumentException if {@code points} is negative
     */
    public void add(int points) {
        requireNotNegative(points);
        balance += points;
    }

    /**
     * Takes points away from the balance.
     *
     * @param points points to redeem; zero or more, and not more than the balance
     * @throws IllegalArgumentException if {@code points} is negative or more than the balance
     */
    public void redeem(int points) {
        requireNotNegative(points);
        if (points > balance) {
            throw new IllegalArgumentException(String.format(NOT_ENOUGH_POINTS, points, balance));
        }
        balance -= points;
    }

    private static void requireNotNegative(int points) {
        if (points < 0) {
            throw new IllegalArgumentException(NEGATIVE_POINTS);
        }
    }
}

package cwrm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Tests for {@link RewardAccount}: adding and redeeming points. */
class RewardAccountTest {
    private static final int STARTING_POINTS = 10;
    private static final int SOME_POINTS = 4;

    @Test
    void getBalance_newAccount_returnsZero() {
        assertEquals(0, new RewardAccount().getBalance());
    }

    @Test
    void add_positivePoints_increasesBalance() {
        RewardAccount account = new RewardAccount();

        account.add(STARTING_POINTS);

        assertEquals(STARTING_POINTS, account.getBalance());
    }

    @Test
    void add_negativePoints_throws() {
        RewardAccount account = new RewardAccount();

        assertThrows(IllegalArgumentException.class, () -> account.add(-SOME_POINTS));
    }

    @Test
    void redeem_withinBalance_decreasesBalance() {
        RewardAccount account = new RewardAccount();
        account.add(STARTING_POINTS);

        account.redeem(SOME_POINTS);

        assertEquals(STARTING_POINTS - SOME_POINTS, account.getBalance());
    }

    @Test
    void redeem_moreThanBalance_throws() {
        RewardAccount account = new RewardAccount();
        account.add(SOME_POINTS);

        assertThrows(IllegalArgumentException.class, () -> account.redeem(STARTING_POINTS));
    }

    @Test
    void redeem_negativePoints_throws() {
        RewardAccount account = new RewardAccount();
        account.add(STARTING_POINTS);

        assertThrows(IllegalArgumentException.class, () -> account.redeem(-SOME_POINTS));
    }
}

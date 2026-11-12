package cwrm.model;

import static cwrm.model.TestData.REQUEST_ID;
import static cwrm.model.TestData.validBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests for the {@link PickupRequest} life cycle: pending, then completed or cancelled once. */
class PickupRequestTest {

    @Test
    void complete_pendingRequest_setsCompleted() {
        PickupRequest request = validBuilder().build();

        request.complete();

        assertEquals(PickupStatus.COMPLETED, request.getStatus());
    }

    @Test
    void complete_alreadyCompleted_throwsIllegalState() {
        PickupRequest request = validBuilder().build();
        request.complete();

        assertThrows(IllegalStateException.class, request::complete);
    }

    @Test
    void complete_cancelledRequest_throwsIllegalState() {
        PickupRequest request = validBuilder().build();
        request.cancel();

        assertThrows(IllegalStateException.class, request::complete);
    }

    @Test
    void complete_alreadyCompleted_messageNamesRequestAndStatus() {
        PickupRequest request = validBuilder().build();
        request.complete();

        IllegalStateException error = assertThrows(IllegalStateException.class, request::complete);

        assertTrue(error.getMessage().contains(REQUEST_ID));
        assertTrue(error.getMessage().contains(PickupStatus.COMPLETED.name()));
    }

    @Test
    void cancel_pendingRequest_setsCancelled() {
        PickupRequest request = validBuilder().build();

        request.cancel();

        assertEquals(PickupStatus.CANCELLED, request.getStatus());
    }

    @Test
    void cancel_completedRequest_throwsIllegalState() {
        PickupRequest request = validBuilder().build();
        request.complete();

        assertThrows(IllegalStateException.class, request::cancel);
    }

    @Test
    void cancel_alreadyCancelled_throwsIllegalState() {
        PickupRequest request = validBuilder().build();
        request.cancel();

        assertThrows(IllegalStateException.class, request::cancel);
    }
}

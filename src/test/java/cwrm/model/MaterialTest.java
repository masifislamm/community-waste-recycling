package cwrm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for {@link Material} and {@link TimeSlot}: every value has a usable label, hint and rate. */
class MaterialTest {

    @ParameterizedTest
    @EnumSource(Material.class)
    void getPointsPerKg_everyMaterial_isPositive(Material material) {
        assertTrue(material.getPointsPerKg() > 0);
    }

    @ParameterizedTest
    @EnumSource(Material.class)
    void getExamples_everyMaterial_isNotBlank(Material material) {
        assertFalse(material.getExamples().isBlank());
    }

    @ParameterizedTest
    @EnumSource(TimeSlot.class)
    void toString_everyTimeSlot_showsFriendlyLabel(TimeSlot slot) {
        assertEquals(slot.getLabel(), slot.toString());
    }

    @Test
    void toString_eWaste_returnsDisplayLabel() {
        assertEquals("E-waste", Material.E_WASTE.toString());
    }
}

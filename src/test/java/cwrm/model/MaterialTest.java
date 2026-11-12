package cwrm.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Tests for {@link Material}: every material has a usable label and reward rate. */
class MaterialTest {

    @ParameterizedTest
    @EnumSource(Material.class)
    void getPointsPerKg_everyMaterial_isPositive(Material material) {
        assertTrue(material.getPointsPerKg() > 0);
    }

    @Test
    void toString_eWaste_returnsDisplayLabel() {
        assertEquals("E-waste", Material.E_WASTE.toString());
    }
}

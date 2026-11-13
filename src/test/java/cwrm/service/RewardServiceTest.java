package cwrm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import cwrm.model.Material;
import org.junit.jupiter.api.Test;

/** Tests for {@link RewardService}: using the active rule and swapping it at runtime. */
class RewardServiceTest {
    private static final double TEN_KG = 10.0;
    private static final int PER_KG_POINTS = 20;
    private static final int PLASTIC_RATE_POINTS = 30;

    @Test
    void pointsFor_perKgRule_usesActiveRule() {
        RewardService service = new RewardService(new PerKgRule());

        assertEquals(PER_KG_POINTS, service.pointsFor(Material.PLASTIC, TEN_KG));
    }

    @Test
    void setRule_swapsStrategyAtRuntime_changesResult() {
        RewardService service = new RewardService(new PerKgRule());
        int before = service.pointsFor(Material.PLASTIC, TEN_KG);

        service.setRule(new PerMaterialRule());
        int after = service.pointsFor(Material.PLASTIC, TEN_KG);

        assertEquals(PER_KG_POINTS, before);
        assertEquals(PLASTIC_RATE_POINTS, after);
    }

    @Test
    void getRule_afterSetRule_returnsNewRule() {
        RewardService service = new RewardService(new PerKgRule());
        RewardRule newRule = new PerMaterialRule();

        service.setRule(newRule);

        assertSame(newRule, service.getRule());
    }

    @Test
    void setRule_null_throwsIllegalArgument() {
        RewardService service = new RewardService(new PerKgRule());

        assertThrows(IllegalArgumentException.class, () -> service.setRule(null));
    }

    @Test
    void constructor_nullRule_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> new RewardService(null));
    }
}

package cwrm.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cwrm.model.Material;
import java.util.stream.Stream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for the three reward strategies: {@link PerKgRule}, {@link PerMaterialRule} and {@link CampaignBonusRule}. */
class RewardRuleTest {
    private static final double TEN_KG = 10.0;
    private static final double FIVE_KG = 5.0;
    private static final double NEGATIVE_KG = -1.0;
    private static final int CAMPAIGN_MULTIPLIER = 3;
    private static final int PER_KG_POINTS_FOR_FIVE_KG = 10;

    private static CampaignBonusRule plasticCampaign() {
        return new CampaignBonusRule(new PerKgRule(), Material.PLASTIC, CAMPAIGN_MULTIPLIER);
    }

    static Stream<RewardRule> allRules() {
        return Stream.of(new PerKgRule(), new PerMaterialRule(), plasticCampaign());
    }

    @ParameterizedTest
    @CsvSource({"0, 0", "5, 10", "12.5, 25"})
    void calculatePoints_perKgRule_returnsExpected(double kg, int expectedPoints) {
        assertEquals(expectedPoints, new PerKgRule().calculatePoints(Material.PAPER, kg));
    }

    @ParameterizedTest
    @CsvSource({"PAPER, 20", "PLASTIC, 30", "GLASS, 20", "METAL, 40", "E_WASTE, 50", "BULKY, 10"})
    void calculatePoints_perMaterialRule_returnsExpected(Material material, int expectedPoints) {
        assertEquals(expectedPoints, new PerMaterialRule().calculatePoints(material, TEN_KG));
    }

    @Test
    void calculatePoints_campaignRuleTargetMaterial_appliesMultiplier() {
        int points = plasticCampaign().calculatePoints(Material.PLASTIC, FIVE_KG);

        assertEquals(PER_KG_POINTS_FOR_FIVE_KG * CAMPAIGN_MULTIPLIER, points);
    }

    @Test
    void calculatePoints_campaignRuleOtherMaterial_returnsBasePoints() {
        int points = plasticCampaign().calculatePoints(Material.PAPER, FIVE_KG);

        assertEquals(PER_KG_POINTS_FOR_FIVE_KG, points);
    }

    @ParameterizedTest
    @MethodSource("allRules")
    void calculatePoints_negativeWeight_throwsIllegalArgument(RewardRule rule) {
        assertThrows(IllegalArgumentException.class, () -> rule.calculatePoints(Material.PAPER, NEGATIVE_KG));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void constructor_campaignMultiplierBelowOne_throwsIllegalArgument(int multiplier) {
        assertThrows(IllegalArgumentException.class,
                () -> new CampaignBonusRule(new PerKgRule(), Material.PLASTIC, multiplier));
    }

    @Test
    void constructor_campaignWithoutBaseRule_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new CampaignBonusRule(null, Material.PLASTIC, CAMPAIGN_MULTIPLIER));
    }

    @Test
    void getName_campaignRule_namesBaseRuleMultiplierAndMaterial() {
        String name = plasticCampaign().getName();

        assertTrue(name.contains(new PerKgRule().getName()));
        assertTrue(name.contains(CAMPAIGN_MULTIPLIER + "x"));
        assertTrue(name.contains(Material.PLASTIC.getLabel()));
    }

    /*
     * Deliberately failing test (required by the brief). Excluded from the default build;
     * run it on its own with: mvn test -Pshow-limitation
     */
    @Tag("known-limitation")
    @Test
    void calculatePoints_perKgRuleFractionalWeight_roundsToNearestPoint() {
        // Expectation 0.9 kg -> 2 points (rounded) but PerKgRule truncates to int -> 1 point.
        // Exposes the limitation: integer truncation silently loses fractional points.
        assertEquals(2, new PerKgRule().calculatePoints(Material.PAPER, 0.9));
    }
}

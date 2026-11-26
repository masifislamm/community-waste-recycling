package cwrm.view;

import cwrm.controller.PickupController;
import cwrm.controller.StatisticsSummary;
import cwrm.model.Material;
import cwrm.model.PickupStatus;
import cwrm.service.RewardRule;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Municipal administrator screen. Figure cards and two bar charts show how well the pilot
 * is going (completion rate, average time to collect, kilograms per material, pickups per
 * status), and the administrator can switch the reward rule. The kilograms chart also
 * serves recycling partners.
 */
public class AdminPanel extends JPanel implements Refreshable {
    private static final String KG_CHART_TITLE = "Kilograms collected per material";
    private static final String STATUS_CHART_TITLE = "Pickups by status";
    private static final String RULE_TITLE = "Reward rule";
    private static final String RULE_INFO = "The reward rule decides how many points residents earn for each"
            + " completed pickup. A new rule applies to pickups completed from now on.";
    private static final String KG_FORMAT = "%.1f kg";
    private static final String COUNT_FORMAT = "%.0f";
    private static final int TEXT_WIDTH = 380;
    private static final Map<PickupStatus, String> STATUS_NAMES = new EnumMap<>(Map.of(
            PickupStatus.PENDING, "Pending",
            PickupStatus.COMPLETED, "Completed",
            PickupStatus.CANCELLED, "Cancelled"));

    private final PickupController controller;
    private final StatCard rateCard = new StatCard("Pickups completed", Theme.INFO);
    private final StatCard turnaroundCard = new StatCard("Average time to collect", Theme.INFO);
    private final StatCard kgCard = new StatCard("Recycled so far", Theme.PRIMARY);
    private final StatCard pointsCard = new StatCard("Points given to residents", Theme.PRIMARY);
    private final StatCard waitingCard = new StatCard("Waiting for a collector", Theme.PENDING);
    private final BarChartPanel kgChart = new BarChartPanel(KG_FORMAT, Theme.PRIMARY);
    private final BarChartPanel statusChart = new BarChartPanel(COUNT_FORMAT, Theme.INFO);
    private final JLabel activeRuleLabel = Widgets.text("", Widgets.SUBTITLE);
    private final JComboBox<RewardRule> ruleBox = new JComboBox<>();
    private final StatusLabel status = new StatusLabel();

    /**
     * Creates the administrator screen.
     *
     * @param controller supplies the statistics and changes the reward rule
     */
    public AdminPanel(PickupController controller) {
        super(new BorderLayout(Theme.GAP, Theme.GAP));
        this.controller = controller;
        setBorder(BorderFactory.createEmptyBorder(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP));
        controller.ruleOptions().forEach(ruleBox::addItem);
        ruleBox.setRenderer(new LabelRenderer<>(RewardRule.class, RewardRule::getName));
        activeRuleLabel.setForeground(Theme.PRIMARY_DARK);
        add(row(rateCard, turnaroundCard, kgCard, pointsCard, waitingCard), BorderLayout.NORTH);
        add(row(new CardPanel(KG_CHART_TITLE, kgChart), rightColumn()), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel rightColumn() {
        JPanel column = new JPanel(new GridLayout(0, 1, 0, Theme.GAP));
        column.setOpaque(false);
        column.add(new CardPanel(STATUS_CHART_TITLE, statusChart));
        column.add(new CardPanel(RULE_TITLE, Widgets.scrollable(ruleForm())));
        return column;
    }

    private JPanel ruleForm() {
        FormPanel form = new FormPanel();
        form.addRow("Active rule", activeRuleLabel);
        form.addRow("Switch to", ruleBox);
        JButton applyButton = Widgets.primaryButton("Apply reward rule");
        applyButton.addActionListener(event -> changeRule());
        form.addButtons(applyButton);
        form.addNote(new JLabel(Widgets.wrapped(RULE_INFO, TEXT_WIDTH)));
        return form;
    }

    private static JPanel row(JComponent... components) {
        JPanel row = new JPanel(new GridLayout(1, 0, Theme.GAP, 0));
        row.setOpaque(false);
        for (JComponent component : components) {
            row.add(component);
        }
        return row;
    }

    private void changeRule() {
        status.showOutcome(controller.changeRule((RewardRule) ruleBox.getSelectedItem()));
        refresh();
    }

    @Override
    public void refresh() {
        StatisticsSummary summary = controller.statistics();
        boolean anyCompleted = summary.count(PickupStatus.COMPLETED) > 0;
        rateCard.setValue(Formats.percent(summary.completionRatePercent()));
        turnaroundCard.setValue(Formats.turnaround(summary.averageTurnaround(), anyCompleted));
        kgCard.setValue(Formats.kg(summary.totalKg()));
        pointsCard.setValue(Formats.points(summary.pointsAwarded()));
        waitingCard.setValue(String.valueOf(summary.count(PickupStatus.PENDING)));
        kgChart.setBars(kgBars(summary.kgByMaterial()));
        statusChart.setBars(statusBars(summary.pickupsByStatus()));
        activeRuleLabel.setText(controller.activeRule().getName());
        ruleBox.setSelectedItem(controller.activeRule());
    }

    private static Map<String, Double> kgBars(Map<Material, Double> kgByMaterial) {
        Map<String, Double> bars = new LinkedHashMap<>();
        kgByMaterial.forEach((material, kg) -> bars.put(material.getLabel(), kg));
        return bars;
    }

    private static Map<String, Double> statusBars(Map<PickupStatus, Long> counts) {
        Map<String, Double> bars = new LinkedHashMap<>();
        counts.forEach((pickupStatus, count) -> bars.put(STATUS_NAMES.get(pickupStatus), count.doubleValue()));
        return bars;
    }
}

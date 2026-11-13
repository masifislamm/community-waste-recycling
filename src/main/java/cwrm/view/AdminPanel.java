package cwrm.view;

import cwrm.controller.PickupController;
import cwrm.controller.StatisticsSummary;
import cwrm.model.Material;
import cwrm.service.RewardRule;
import java.awt.BorderLayout;
import java.time.Duration;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Municipal administrator screen. Shows how well the pilot is going (completion rate,
 * average turnaround, kilograms collected per material) and lets the administrator
 * switch the reward rule. The kilograms table also serves recycling partners.
 */
public class AdminPanel extends JPanel implements Refreshable {
    private static final String TITLE = "Administrator: pilot statistics and reward rule";
    private static final String PERCENT_FORMAT = "%.1f %%";
    private static final String TURNAROUND_FORMAT = "%d h %02d min %02d s";
    private static final String KG_FORMAT = "%.1f";
    private static final String[] COLUMNS = {"Material", "Kilograms collected"};

    private final PickupController controller;
    private final JLabel completionLabel = new JLabel();
    private final JLabel turnaroundLabel = new JLabel();
    private final JLabel activeRuleLabel = new JLabel();
    private final JComboBox<RewardRule> ruleBox = new JComboBox<>();
    private final ReadOnlyTableModel kgRows = new ReadOnlyTableModel(COLUMNS);
    private final StatusLabel status = new StatusLabel();

    /**
     * Creates the administrator screen.
     *
     * @param controller supplies the statistics and changes the reward rule
     */
    public AdminPanel(PickupController controller) {
        super(new BorderLayout());
        this.controller = controller;
        setBorder(BorderFactory.createTitledBorder(TITLE));
        controller.ruleOptions().forEach(ruleBox::addItem);
        ruleBox.setRenderer(new LabelRenderer<>(RewardRule.class, RewardRule::getName));
        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(new JTable(kgRows)), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        refresh();
    }

    private JPanel buildForm() {
        FormPanel form = new FormPanel();
        form.addRow("Completion rate", completionLabel);
        form.addRow("Average turnaround", turnaroundLabel);
        form.addRow("Active reward rule", activeRuleLabel);
        form.addRow("Switch reward rule to", ruleBox);
        JButton applyButton = new JButton("Apply reward rule");
        applyButton.addActionListener(event -> changeRule());
        form.addButtons(applyButton);
        return form;
    }

    private void changeRule() {
        status.showOutcome(controller.changeRule((RewardRule) ruleBox.getSelectedItem()));
        refresh();
    }

    @Override
    public void refresh() {
        StatisticsSummary summary = controller.statistics();
        completionLabel.setText(String.format(PERCENT_FORMAT, summary.completionRatePercent()));
        turnaroundLabel.setText(formatDuration(summary.averageTurnaround()));
        activeRuleLabel.setText(controller.activeRule().getName());
        kgRows.setRows(summary.kgByMaterial().entrySet().stream().map(AdminPanel::toRow).toList());
    }

    private static String formatDuration(Duration duration) {
        return String.format(TURNAROUND_FORMAT, duration.toHours(), duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    private static Object[] toRow(Map.Entry<Material, Double> entry) {
        return new Object[] {entry.getKey(), String.format(KG_FORMAT, entry.getValue())};
    }
}

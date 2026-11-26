package cwrm.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * The green band at the top of the window with the app's name, what it is for,
 * and a tag showing which UN Sustainable Development Goal it supports.
 */
public class HeaderBar extends JPanel {
    private static final String NAME = "CWRM";
    private static final String DESCRIPTION = "Community Waste and Recycling Manager  |  Kuala Lumpur pilot";
    private static final String SDG_TAG = "Supports UN SDG 11.6: cleaner, greener cities";
    private static final Color SUBTLE_WHITE = new Color(0xDDEEDD);
    private static final int VERTICAL_PADDING = 14;
    private static final int SIDE_PADDING = 20;
    private static final int TAG_PADDING = 5;
    private static final int TAG_SIDE_PADDING = 14;

    /** Creates the header. */
    public HeaderBar() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(VERTICAL_PADDING, SIDE_PADDING, VERTICAL_PADDING, SIDE_PADDING));
        add(titleBlock(), BorderLayout.WEST);
        add(sdgTag(), BorderLayout.EAST);
    }

    private static JPanel titleBlock() {
        JPanel block = new JPanel(new GridLayout(0, 1));
        block.setOpaque(false);
        JLabel name = Widgets.text(NAME, Widgets.LARGE_TITLE);
        name.setForeground(Color.WHITE);
        JLabel description = new JLabel(DESCRIPTION);
        description.setForeground(SUBTLE_WHITE);
        block.add(name);
        block.add(description);
        return block;
    }

    /** The SDG tag: a light green pill, centred vertically so it keeps its own small height. */
    private static JPanel sdgTag() {
        JLabel tag = new JLabel(SDG_TAG) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(Theme.PRIMARY_LIGHT);
                g.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
                g.dispose();
                super.paintComponent(graphics);
            }
        };
        tag.setForeground(Theme.PRIMARY_DARK);
        tag.setBorder(BorderFactory.createEmptyBorder(TAG_PADDING, TAG_SIDE_PADDING, TAG_PADDING, TAG_SIDE_PADDING));
        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);
        holder.add(tag);
        return holder;
    }

    /** Fills the band with a gentle left-to-right green gradient. */
    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setPaint(new GradientPaint(0, 0, Theme.PRIMARY_DARK, getWidth(), 0, Theme.PRIMARY));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.dispose();
    }
}

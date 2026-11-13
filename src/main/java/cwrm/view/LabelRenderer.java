package cwrm.view;

import java.awt.Component;
import java.util.function.Function;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Shows each item of a drop-down list as readable text, for example a resident's
 * name instead of the object's default text.
 *
 * @param <T> the type of item in the list
 */
public class LabelRenderer<T> extends DefaultListCellRenderer {
    private final Class<T> itemType;
    private final transient Function<T, String> toText;

    /**
     * Creates a renderer.
     *
     * @param itemType the type of item in the list
     * @param toText   gives the text to show for an item
     */
    public LabelRenderer(Class<T> itemType, Function<T, String> toText) {
        this.itemType = itemType;
        this.toText = toText;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean selected, boolean focused) {
        Object text = itemType.isInstance(value) ? toText.apply(itemType.cast(value)) : value;
        return super.getListCellRendererComponent(list, text, index, selected, focused);
    }
}

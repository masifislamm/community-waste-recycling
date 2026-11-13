package cwrm.view;

/**
 * A screen that can reload what it shows from the controller.
 * The main window calls {@link #refresh()} whenever the user opens a tab.
 */
public interface Refreshable {

    /** Reloads the data shown on this screen. */
    void refresh();
}

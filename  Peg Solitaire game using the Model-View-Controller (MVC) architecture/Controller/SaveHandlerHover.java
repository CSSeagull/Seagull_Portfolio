package peggame.Controller;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 * This class is responsible for handling mouse hover events over a Label, usually
 * representing a save action in a graphical user interface. Upon hovering, it changes
 * the label's background and text colors to provide visual feedback to the user, indicating
 * that the label is an interactive element for saving.
 */
public class SaveHandlerHover implements EventHandler<MouseEvent> {
    private Label save; 
    /**
     * Constructs a SaveHandlerHover with the specified label.
     * 
     * @param save The Label that will change appearance on mouse hover, indicating it's a clickable save button.
     */
    public SaveHandlerHover(Label save) {
        this.save = save; 
    }

    /**
     * Handles the mouse hover event by modifying the Label's appearance. Sets the
     * background to a green-yellow color and changes the text color to magenta,
     * providing a clear visual cue that the label is interactive.
     *
     * @param arg0 The MouseEvent that triggers this handler. Not used directly.
     */
    @Override
    public void handle(MouseEvent arg0) {
        save.setBackground(new Background(new BackgroundFill(Color.GREENYELLOW, CornerRadii.EMPTY, Insets.EMPTY)));
        save.setTextFill(Color.MAGENTA);
    }
}

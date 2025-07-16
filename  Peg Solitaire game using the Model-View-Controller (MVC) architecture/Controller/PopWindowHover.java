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
 * Implements an event handler for mouse hover events on a specified label, 
 * typically used as an exit button in a pop-up window. When the mouse hovers over
 * the label, this handler updates its appearance to visually cue the user 
 * that the label is interactive.
 */
public class PopWindowHover implements EventHandler<MouseEvent> {
    private Label exit; // The label that will be modified on mouse hover.

    /**
     * Constructs a PopWindowHover handler with the specified label.
     *
     * @param exit The label to be modified when the mouse hovers over it.
     */
    public PopWindowHover(Label exit) {
        this.exit = exit; 
    }

    /**
     * Handles the mouse hover event by changing the label's background color to red
     * and its text color to white smoke. This method provides a visual indication that 
     * the label (exit button) is interactable.
     *
     * @param arg0 The mouse event that triggers this handler. Not used directly.
     */
    @Override
    public void handle(MouseEvent arg0) {
        exit.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        exit.setTextFill(Color.WHITESMOKE);
    }
}

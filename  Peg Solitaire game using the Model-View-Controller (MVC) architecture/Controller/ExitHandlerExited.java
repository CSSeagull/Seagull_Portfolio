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
 * Class to handle mouse exit events for a GUI label, specifically designed for an exit button.
 * Implements the EventHandler interface for mouse events, adjusting the label's appearance
 * to indicate that the mouse is no longer hovering over it
 */
public class ExitHandlerExited implements EventHandler<MouseEvent> {
    private Label exit; // The label that represents an exit button

    /**
     * Constructor for the ExitHandlerExited class
     * Initializes the label whose appearance will be modified on mouse exit events
     * 
     * @param exit The label to be modified when the mouse exits its area
     */
    public ExitHandlerExited(Label exit){
        this.exit = exit; // Initialize the exit label with the provided label
    }

    /**
     * Handles the mouse exit event by changing the label's background and text color
     * This method overrides the handle method in the EventHandler interface, setting
     * the label's background to white and its text color to black, indicating that
     * the mouse is not hovering over it
     * 
     * @param arg0 The mouse event that triggers the handler. Not used directly
     */
    @Override
    public void handle(MouseEvent arg0) {
        // Set the label's background to white with no special corner radii or insets
        exit.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        // Set the label's text color to black
        exit.setTextFill(Color.BLACK);
    }
    
}

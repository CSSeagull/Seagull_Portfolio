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
 * Class to handle mouse hover events for a GUI label, specifically aimed at enhancing user interaction feedback
 * for an exit button. This class implements the EventHandler interface for mouse events to change the label's
 * appearance, indicating that it is ready for interaction (e.g., to exit the application).
 */
public class ExitHoverHandler implements EventHandler<MouseEvent> {
    Label exit; // The label that functions as an exit button

    /**
     * Constructor for the ExitHoverHandler class.
     * Sets up the handler with the label that will have its appearance changed upon mouse hover.
     * 
     * @param exit The label that will change appearance when the mouse hovers over it.
     */
    public ExitHoverHandler(Label exit){
        this.exit = exit; // Initialize the exit label with the given label.
    }

    /**
     * Handles the mouse hover event by changing the label's background to red and text color to white smoke.
     * This method overrides the handle method in the EventHandler interface, visually indicating
     * that the label is an interactive exit button.
     * 
     * @param arg0 The mouse event that triggers this handler. Not used directly.
     */
    @Override
    public void handle(MouseEvent arg0) {
        // Set the label's background to red with no special corner radii or insets.
        exit.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
        // Change the label's text color to white smoke for better visibility and contrast.
        exit.setTextFill(Color.WHITESMOKE);
    }
    
}

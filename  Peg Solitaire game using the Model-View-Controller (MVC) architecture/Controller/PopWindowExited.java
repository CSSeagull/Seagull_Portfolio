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
 * This class implements the EventHandler interface for handling mouse exit events
 * on a specific Label, typically representing a button in a pop-up window. When
 * the mouse pointer exits the bounds of the Label, this handler updates the label's
 * appearance to provide visual feedback to the user.
 */
public class PopWindowExited implements EventHandler<MouseEvent>{
    private Label exit; // The Label instance that this handler is associated with.

    /**
     * Constructs a PopWindowExited event handler with a specific Label.
     * 
     * @param popExit The Label that will have its appearance changed upon the mouse exit event.
     */
    public PopWindowExited(Label popExit){
        this.exit = popExit; // Initialize the exit label with the provided Label instance.
    }

    /**
     * Handles the mouse exit event by changing the Label's (exit) background to Color.WHITESMOKE
     * and the text color to black. This method provides a visual cue to the user that the mouse
     * is no longer hovering over the Label.
     *
     * @param arg0 The MouseEvent that triggered this handler. This parameter is not used
     *             directly in the method but is required by the EventHandler interface.
     */
    @Override
    public void handle(MouseEvent arg0) {
        exit.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, CornerRadii.EMPTY, Insets.EMPTY)));
        exit.setTextFill(Color.BLACK);
    }
}

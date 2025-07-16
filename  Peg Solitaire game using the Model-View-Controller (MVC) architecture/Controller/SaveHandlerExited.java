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
 * This class implements an EventHandler for mouse exit events on a Label,
 * typically used as a Save button in the interface. It changes the Label's
 * background to white and the text color to black, indicating that the mouse
 * is no longer hovering over it, which can help in creating a responsive and
 * interactive user interface.
 */
public class SaveHandlerExited implements EventHandler<MouseEvent> {
    private Label save; 

    /**
     * Constructor to create a SaveHandlerExited event handler with the specified Label.
     * 
     * @param save The Label whose appearance will change when the mouse exits its area.
     */
    public SaveHandlerExited(Label save) {
        this.save = save; 
    }

    /**
     * Handles the mouse exit event by changing the Label's (save) background to white
     * and its text color to black. This method provides visual feedback indicating
     * the mouse is no longer hovering over the Label, enhancing user interaction cues.
     *
     * @param arg0 The MouseEvent that triggers this handler.
     */
    @Override
    public void handle(MouseEvent arg0) {
        save.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        save.setTextFill(Color.BLACK);
    }
}

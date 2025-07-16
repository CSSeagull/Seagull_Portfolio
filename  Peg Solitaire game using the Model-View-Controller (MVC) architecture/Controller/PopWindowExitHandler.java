package peggame.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Implements an event handler that listens for mouse click events to close a popup window
 * and terminate the application. This handler is associated with a specific Stage, which
 * represents the popup window in a JavaFX application.
 */
public class PopWindowExitHandler implements EventHandler<MouseEvent> {
    private Stage popWindow; // The Stage (popup window) that this handler will close.

    /**
     * Constructs a new PopWindowExitHandler for a given popup window.
     * 
     * @param popWindow The Stage object that represents the popup window to be closed.
     */
    public PopWindowExitHandler(Stage popWindow) {
        this.popWindow = popWindow; // Initialize the popWindow with the given Stage.
    }

    /**
     * Handles the mouse click event by closing the popup window and then exiting the application.
     * This method overrides the handle method in the EventHandler interface.
     * 
     * @param arg0 The MouseEvent that triggers this handler. Not used directly in the method.
     */
    @Override
    public void handle(MouseEvent arg0) {
       popWindow.close(); 
       System.exit(0); 
    }
}

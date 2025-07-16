package peggame.Controller;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 * Class to handle exit events for a GUI application.
 * This class implements the EventHandler interface for mouse events,
 * specifically to close the application window when a mouse event is detected
 */
public class ExitHandlerClick implements EventHandler<MouseEvent> {
    private Stage stage; // Reference to the application window stage

    /**
     * Constructor for ExitHandlerClick class.
     * Initializes the stage to be used for closing the application.
     * 
     * @param stage The stage (application window) that this handler will close
     */
    public ExitHandlerClick(Stage stage){
        this.stage = stage; // Initialize stage with the given application window
    }

    /**
     * Handles the mouse event by closing the application window
     * This method overrides the handle method in the EventHandler interface
     * 
     * @param arg0 The mouse event that triggers the handler. Not used directly
     */
    @Override
    public void handle(MouseEvent arg0) {
        stage.close(); // Close the application window
    }
    
}

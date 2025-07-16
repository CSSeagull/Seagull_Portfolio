package peggame.Controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import peggame.Model.BoardReader;
import peggame.Model.InvalidBoardException;
import peggame.Model.PegGameSquare;

/**
 * Handles action events for pop-up windows in a JavaFX application, specifically for
 * loading a game board from a file. This handler is linked to a TextField input, a game
 * state, a pop-up window, and a feedback label.
 */
public class PopWindowHandler implements EventHandler<ActionEvent> {
    private TextField txt; // TextField for inputting the file path.
    private PegGameSquare game; // Current game state to be updated.
    private Stage pop; // Pop-up window that will be closed upon successful loading.
    private Label l; // Label for displaying feedback (errors or success messages).

    /**
     * Constructs a PopWindowHandler with references to the necessary UI components
     * and game state.
     *
     * @param txt TextField for the file path input.
     * @param game Current game state.
     * @param pop Pop-up window.
     * @param l Feedback label.
     */
    public PopWindowHandler(TextField txt, PegGameSquare game, Stage pop, Label l){
        this.game = game;
        this.txt = txt;
        this.pop = pop;
        this.l = l;
    }

    /**
     * Handles the action event triggered by user interaction. It attempts to read the game board
     * from a file specified in the TextField, update the game state accordingly, and close the pop-up window.
     * If an error occurs (file not found or invalid board format), it displays an error message.
     *
     * @param arg0 The ActionEvent that triggers this handler.
     */
    @Override
    public void handle(ActionEvent arg0) {
        game.getBoard(); // Retrieve the current game board (not used explicitly here)
        try {
            PegGameSquare square = (PegGameSquare) BoardReader.readFromFile(txt.getText());
            game.setBoard(square.getBoard());
            game.setCols(square.getCols());
            game.setRows(square.getRows());
            pop.close(); 

        } catch(IOException e) {
            // Handle file not found or reading errors.
            l.setText("There is no such file");
            l.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            l.setTextFill(Color.WHITE);
        } catch(InvalidBoardException e) {
            // Handle errors related to the board's validity.
            l.setText(e.getMessage());
            l.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            l.setTextFill(Color.WHITE);
        }
    }
}

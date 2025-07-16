package peggame.Controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import peggame.Model.PegGameSquare;

/**
 * Handles mouse events to save the current state of a peg game to a text file.
 * The file name is specified by the user in a TextField. This handler ensures that
 * the file name ends with ".txt" before proceeding with the save operation
 */
public class SaveHandler implements EventHandler<MouseEvent> {
    private TextField field; 
    private PegGameSquare game; 
    private Label info; 
    /**
     * Constructs a SaveHandler with references to the necessary UI components and the game state.
     *
     * @param field The TextField for inputting the save file's name.
     * @param game The current game state.
     * @param info The Label for displaying save operation feedback.
     */
    public SaveHandler(TextField field, PegGameSquare game, Label info) {
        this.field = field;
        this.game = game;
        this.info = info;
    }

    /**
     * Handles the mouse event by attempting to save the game state to a file.
     * Validates the file name and updates the UI with success or error messages.
     *
     * @param arg0 The MouseEvent that triggers the save operation.
     */
    @Override
    public void handle(MouseEvent arg0) {
        String name = field.getText(); 
        if (!name.equals("") && name.endsWith(".txt")) {
            char[][] board = game.getBoard(); 
            File file = new File(name); 
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println(board.length); 
                for (int i = 0; i < board.length; i++) {
                    writer.println(new String(board[i])); 
                }
                // Provides feedback to the user that the game was successfully saved
                info.setText("The game has been saved!");
                info.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY)));
                info.setTextFill(Color.YELLOW);
            } catch (FileNotFoundException e) {
                // Handles the case where the file cannot be created or opened
                e.printStackTrace();
            }
        } else {
            // Provides feedback if the file name is invalid (not ending with ".txt")
            info.setText("Your file should end with .txt");
            info.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            info.setTextFill(Color.WHITE);
        }
    }
}

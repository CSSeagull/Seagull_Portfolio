package peggame.Controller;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import peggame.Model.Move;
import peggame.Model.Peg;
import peggame.Model.PegGameException;
import peggame.Model.PegGameSquare;

/**
 * Event handler for pegs in a peg game, responsible for managing peg interactions
 * through mouse events. It includes game logic to handle peg selection, movement,
 * and updating the game state visually and logically.
 */

public class PegHandler implements EventHandler<MouseEvent > {
    private Peg peg;
    private PegGameSquare game;
    public static Label status;
    public static VBox board;
    private Label info;
    /**
     * Constructor to create a PegHandler.
     * 
     * @param wrapper The HBox that contains the peg this handler is associated with.
     * @param game The current game state.
     * @param info The label to display information about moves and game status.
     */
    public PegHandler(HBox wrapper, PegGameSquare game, Label info){
        this.peg = (Peg)wrapper.getChildren().getFirst();
        this.game = game;
        this.info =  info;
    }

    /**
     * Handles mouse click events on pegs. Depending on the state of the peg (clicked, not clicked),
     * and the global click state, it either marks a peg for a potential move, executes a move,
     * or resets the selection. It also updates the game state and UI accordingly.
     * 
     * @param arg0 The mouse event that triggers this method.
     */
    @Override
    public void handle(MouseEvent arg0) {
        // If the peg is a hole and global click is not active, ignore the click.
        if(peg.isHole() && !Peg.isGLobalClick){
            System.out.println("ignore");
        }else{
            if(peg.isClicked()){
                peg.setRadius(peg.getRadius()-5);
                peg.setFill(Color.GREEN);
                peg.setClicked(false);
                Peg.isGLobalClick = false;
            }else if(!peg.isClicked() && !Peg.isGLobalClick){
                // Increases the peg's radius, changes its color to light coral, and marks it as clicked
                peg.setRadius(peg.getRadius()+5);
                peg.setFill(Color.LIGHTCORAL);
                peg.setClicked(true);
                Peg.isGLobalClick = true;
                Peg.globaLocation = peg.getLocation();
            }else if(!peg.isClicked() && Peg.isGLobalClick){
                // Attempts to make a move if another peg is already selected
                try {
                    Move move = new Move(Peg.globaLocation, peg.getLocation());
                    game.makeMove(move);
                    int sCol = Peg.globaLocation.getCol();
                    int sRow = Peg.globaLocation.getRow();
                    int nCol = peg.getLocation().getCol();
                    int nRow = peg.getLocation().getRow();
                    int midCol = (nCol - sCol)/2;
                    int midRow = (nRow - sRow)/2;
                    HBox row = (HBox)board.getChildren().get(sRow);
                    HBox wrapper = (HBox) row.getChildren().get(sCol); 
                    Peg peg = (Peg)wrapper.getChildren().getFirst();
                    peg.setHole(true);
                    Peg.updatePeg(peg);
                    row = (HBox)board.getChildren().get(sRow+midRow);
                    wrapper = (HBox) row.getChildren().get(sCol+midCol);
                    peg = (Peg)wrapper.getChildren().getFirst();
                    peg.setHole(true);
                    Peg.updatePeg(peg);
                    row = (HBox)board.getChildren().get(nRow);
                    wrapper = (HBox) row.getChildren().get(nCol);
                    peg = (Peg)wrapper.getChildren().getFirst();
                    peg.setHole(false);
                    Peg.updatePeg(peg);
                    Peg.isGLobalClick = false;
                    game.state(status);
                    info.setText("Your move: "+  move);
                    info.setBackground(new Background(new BackgroundFill(Color.MAGENTA, CornerRadii.EMPTY, Insets.EMPTY)));
                    info.setTextFill(Color.WHITE);
                } catch (PegGameException e) {
                    // Handles an invalid move attempt
                    info.setText("Invalid Move");
                    info.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
                    info.setTextFill(Color.WHITE);
                }
            }
        
        }
    }
    
}

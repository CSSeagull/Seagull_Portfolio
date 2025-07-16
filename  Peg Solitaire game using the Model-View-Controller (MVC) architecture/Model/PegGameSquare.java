package peggame.Model;

import java.util.ArrayList;
import java.util.Collection;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
/**
 * Class representing a PegGame on a square board
 */
public class PegGameSquare implements PegGame{
    private boolean isStarted;
    private char[][] board;
    private int rows;
    private int cols;

    @Override
    public Collection<Move> getPossibleMoves() {
        Collection<Move> moves = new ArrayList<>();// list to store possible moves
        for (int i = 0; i < rows; i++) {// iterate through each position on the board
            for (int j = 0; j < cols; j++) {
                if (board[i][j] == 'o') {// if the current position has a peg
                     /**
                    * Check for possible moves where a peg can jump upwards over another peg.
                    * If a peg is found two rows above the current position and the landing spot is empty,
                    * a move is added to the list of possible moves.
                    * 
                    * @param i The row index of the current peg.
                    * @param j The column index of the current peg.
                    */
                    if (i >= 2 && board[i - 1][j] == 'o' && board[i - 2][j] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i - 2, j)));
                    }
                    if (i < rows - 2 && board[i + 1][j] == 'o' && board[i + 2][j] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i + 2, j)));
                    }
                    if (j >= 2 && board[i][j - 1] == 'o' && board[i][j - 2] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i, j - 2)));
                    }
                    if (j < cols - 2 && board[i][j + 1] == 'o' && board[i][j + 2] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i, j + 2)));
                    }
                    /**
                    * The following piece of code checks for possible moves where a peg can jump diagonally over another peg.
                    * If a peg is found at the expected position for a jump and the landing spot is empty,
                    * a move is added to the list of possible moves.
                    * 
                    * @param i The row index of the current peg.
                    * @param j The column index of the current peg.
                    */
                    // 
                    if (i >= 2&& j<cols-2 && board[i - 1][j+1] == 'o' && board[i - 2][j+2] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i - 2, j+2)));
                    }
                    if (i >= 2&& j>=2 && board[i - 1][j-1] == 'o' && board[i - 2][j-2] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i - 2, j-2)));
                    }
                    if (i < rows - 2 && j<cols-2 && board[i + 1][j+1] == 'o' && board[i + 2][j+2] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i + 2, j+2)));
                    }
                    if (i < rows - 2 && j>=2 && board[i + 1][j-1] == 'o' && board[i + 2][j-2] == '.') {
                        moves.add(new Move(new Location(i, j), new Location(i + 2, j-2)));
                    }
                }
            }
        }
        return moves; // return list of possible moves
    }

    /**
    * Determines the current state of the peg game.
    * If there are no possible moves and only one peg remains, the game is won.
    * If there are no possible moves and more than one peg remains, the game is in a stalemate.
    * If there are possible moves remaining, the game is still in progress.
    * 
    * @return The current state of the peg game.
    */   


    @Override
    public GameState getGameState() {
        if (getPossibleMoves().isEmpty()) {  // Check if there are no possible moves
            if (countPegs() == 1) { // Check if there is only one peg remaining
                return GameState.WON; // If only one peg remains and no moves are possible, the game is won
            }else {
                return GameState.STALEMATE;  // If more than one peg remains and no moves are possible, the game is in a stalemate
            }
        }else if(!isStarted){
            return GameState.NOT_STARTED;
        } else {
            return GameState.IN_PROGRESS; // If there are possible moves remaining, the game is still in progress
        }
    }

    /**
    * Executes a move in the peg game.
    * 
    * @param move The move to be executed.
    * @throws PegGameException If the move is invalid.
    */

    @Override
    public void makeMove(Move move) throws PegGameException {
        // Extracting coordinates from the move
        Location from = move.getFrom();
        Location to = move.getTo();
        int fromRow = from.getRow();
        int fromCol = from.getCol();
        int toRow = to.getRow();
        int toCol = to.getCol();
        // Check if the move is a jump in the vertical direction
        if(board[toRow][toCol]=='o'){
            throw new PegGameException("Invalid move");
        }
        if (Math.abs(fromRow - toRow) == 2 && fromCol == toCol) {
            int jumpedRow = (fromRow + toRow) / 2; // Calculate the row of the jumped peg
            if (board[jumpedRow][fromCol] != 'o') {// Check if there is a peg to jump over
                throw new PegGameException("Invalid move: No peg to jump.");
            }
             // Update the board after the jump
            board[fromRow][fromCol] = '.';
            board[toRow][toCol] = 'o';
            board[jumpedRow][fromCol] = '.';
            isStarted =true;
        } else if (fromRow == toRow && Math.abs(fromCol - toCol) == 2) { // Check if the move is a jump in the horizontal direction
            int jumpedCol = (fromCol + toCol) / 2;  // Calculate the column of the jumped peg
            if (board[fromRow][jumpedCol] != 'o') {// Check if there is a peg to jump over
                throw new PegGameException("Invalid move: No peg to jump.");
            }
            // Update the board after the jump
            board[fromRow][fromCol] = '.';
            board[toRow][toCol] = 'o';
            board[fromRow][jumpedCol] = '.';
            // Check if the move is a jump in the diagonal direction
            isStarted =true;
        }else if ((Math.abs(fromRow - toRow)==2 && Math.abs(fromCol - toCol) == 2)) {
            int jumpedCol = (fromCol + toCol) / 2;
            int jumpedRaw = (fromRow + toRow)/2;
            if (board[jumpedRaw][jumpedCol] != 'o') {
                throw new PegGameException("Invalid move: No peg to jump.");
            }
            board[fromRow][fromCol] = '.';
            board[toRow][toCol] = 'o';
            board[jumpedRaw][jumpedCol] = '.';
            isStarted =true;
        }

         else {
            throw new PegGameException("Invalid move");
        }
    }


    /**
    * Constructs a PegGameSquare object with the specified number of rows, columns, and board configuration.
    * 
    * @param rows The number of rows in the peg game board.
    * @param cols The number of columns in the peg game board.
    * @param board The initial configuration of the peg game board.
    */

    public PegGameSquare(int rows, int cols, char[][] board){
        this.board = board;
        this.rows = rows;
        this.cols = cols;
    }

     /**
    * Returns a string representation of the peg game board.
    * 
    * @return A string representation of the peg game board.
    */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {// Iterate over each row and column of the board
            for (int j = 0; j < cols; j++) {
                sb.append(board[i][j]);// Append the character representing the state of each cell to the StringBuilder
            }
            sb.append('\n');// Append a newline character to separate rows
        }
        return sb.toString();// Convert the StringBuilder to a string and return
    }

     /**
    * Counts the number of pegs on the peg game board.
    * 
    * @return The number of pegs on the peg game board.
    */
    private int countPegs() {
        int pegCount = 0;
         // Iterate over each cell of the board
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {// Increment the pegCount if the cell contains a peg ('o')
                if (board[i][j] == 'o') {
                    pegCount++;
                }
            }
        }
        return pegCount;
    }

    public char[][] getBoard() {
        return board;
    }

    public void setBoard(char[][] board) {
        this.board = board;
    }

    public void state(Label status){
        GameState state = getGameState();
        if(state == GameState.IN_PROGRESS){
            status.setText("IN PROGRESS");
            status.setBackground(new Background(new BackgroundFill(Color.ORANGE, CornerRadii.EMPTY, Insets.EMPTY)));
            status.setTextFill(Color.GHOSTWHITE);
        }else if (state == GameState.STALEMATE){
            status.setText("STALEMATE");
            status.setBackground(new Background(new BackgroundFill(Color.MAGENTA, CornerRadii.EMPTY, Insets.EMPTY)));
            status.setTextFill(Color.WHITE);
        }else if(state == GameState.WON){
            status.setText("YOU WON!!!");
            status.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));
            status.setTextFill(Color.YELLOW);
        }else if(state == GameState.NOT_STARTED){
            status.setText("NOT STARTED");
            status.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            status.setTextFill(Color.WHITESMOKE);
        }
    }

    public void setCols(int cols) {
        this.cols = cols;
    }
    public void setRows(int rows) {
        this.rows = rows;
    }
    public int getCols() {
        return cols;
    }
    public int getRows() {
        return rows;
    }
        
}


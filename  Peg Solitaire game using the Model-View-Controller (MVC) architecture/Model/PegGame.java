package peggame.Model;

import java.util.Collection;

/**
 * Interface of the game
 */
public interface PegGame {
    /**
    * Gets a collection of possible moves in the peg game.
    * 
    * @return A collection of possible moves.
    */
    Collection<Move> getPossibleMoves();
    /**
     *  Gets the current game state of the peg game.
     */
    GameState getGameState();
    /**
     * moves the peg 
     * @param move
     * @throws PegGameException If the move is invalid.
     */
    void makeMove(Move move) throws PegGameException;
}

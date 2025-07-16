package peggame.Model;
/**
 * Class to represent a move on a board
 */
public class Move {
    private Location from;// From location
    private Location to;// To location

    /**
     * Constructor for Move class
     * @param from
     * @param to
     */
    public Move(Location from, Location to) {
        this.from = from; // initialize from
        this.to = to; // initialize to
    }

    /**
     * Getter for from location
     * @return
     */
    public Location getFrom() {
        return this.from;
    }
    /**
     *  Getter for to location
     * @return
     */
    public Location getTo() {
        return this.to;
    }

    /**
     * Override toString method
     * Used to represent the availible move of the peg
     */
    @Override
    public String toString() {
        return "r" + (from.getRow()+1) + " c" + (from.getCol()+1) + " -> " + "r" + (to.getRow()+1) + " c" + (to.getCol()+1);
    }
}

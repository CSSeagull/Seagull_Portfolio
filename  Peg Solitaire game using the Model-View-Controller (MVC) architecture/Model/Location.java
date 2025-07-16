package peggame.Model;

/**
 * This class represents the location of the peg
 */
public class Location {
    private int row;
    private int col;
    /**
     * Making Constructor for the location Class.
     * @param row 
     * @param col
     */
    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return this.row;
    }

    public int getCol() {
        return this.col;
    }
}

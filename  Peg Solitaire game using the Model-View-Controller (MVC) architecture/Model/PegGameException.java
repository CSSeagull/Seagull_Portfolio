package peggame.Model;

/**This class represents PegGame exception */
public class PegGameException extends Exception{
     /**
     * Constructs a new PegGameException with the specified error message.
     * 
     * @param message The error message describing the exception.
     */
    public PegGameException(String message) {
        super(message);
    }
}

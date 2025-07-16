package peggame.Model;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
/**
    * Reads a PegGame from a file.
    * 
    * @param filename The name of the file containing the PegGame board.
    * @return The PegGame read from the file.
    * @throws IOException If an I/O error occurs while reading the file.
    */

    // Read PegGame from a file
public class BoardReader {
    public static PegGame readFromFile(String filename) throws IOException, InvalidBoardException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));// Create BufferedReader for the file
        int rows = Integer.parseInt(reader.readLine().trim());// Get number of rows in the board
        char[][] board = new char[rows][rows];// Create 2D char array for the board
           // Read each row of the board
        for (int i = 0; i < rows; i++) {
            board[i] = reader.readLine().toCharArray();// Store the row as a char array
            if(board[i].length!= rows){
                throw new InvalidBoardException("Invalid Board: improper structure of the board");
            }
            for (int j = 0; j < board[i].length; j++) {
                if((board[i][j]!= '.') && (board[i][j]!= 'o')){
                    System.out.println(board[i][j] + " i " +i + " J " + j);
                    throw new InvalidBoardException("Invalid Board: the file contains inappropriate symbols");
                }
            }
        }
        reader.close();
        return new PegGameSquare(rows, board[0].length, board);
    }
}

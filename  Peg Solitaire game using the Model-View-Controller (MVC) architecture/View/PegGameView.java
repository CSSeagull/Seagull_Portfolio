
package peggame.View;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import peggame.Controller.ExitHandlerClick;
import peggame.Controller.ExitHandlerExited;
import peggame.Controller.ExitHoverHandler;
import peggame.Controller.PegHandler;
import peggame.Controller.PopWindowExitHandler;
import peggame.Controller.PopWindowExited;
import peggame.Controller.PopWindowHandler;
import peggame.Controller.PopWindowHover;
import peggame.Controller.SaveHandler;
import peggame.Controller.SaveHandlerExited;
import peggame.Controller.SaveHandlerHover;
import peggame.Model.Location;
import peggame.Model.Peg;
import peggame.Model.PegGameSquare;

/**
 * The main application class for the peg game, extending the JavaFX Application class.
 * This class sets up the game's graphical user interface, including the initial pop-up window
 * for loading a game from a file, and the main game window displaying the game state
 */
public class PegGameView extends Application{
    private char board[][];

    /**
     * Starts the primary stage of the application, setting up the user interface and event handlers
     * 
     * @param arg0 The primary stage for this application
     */
    @Override
    public void start(Stage arg0) throws Exception {
        // Setup for the primary game window and the initial pop-up window for loading a game
        arg0.setResizable(false);
        arg0.setTitle("$PEG GAME$");

        Stage popWindow = new Stage();
        popWindow.setResizable(false);
        popWindow.setTitle("File");
        
        // Various UI components for the pop-up window, including labels, text fields, and buttons
        Label l = createLabel("Type your file name", 300, 100);
        TextField file = new TextField();
        Button btn = new Button("Load Game");
        Label popExit = createLabel("Exit", 300, 30);

        // Game model and controllers setup
        PegGameSquare game = new PegGameSquare(5, 5, board);
        PopWindowHandler pop = new PopWindowHandler(file, game, popWindow, l);

        // Event handlers for the pop-up window's UI components
        PopWindowHover popWindowHover =  new PopWindowHover(popExit);
        PopWindowExited popWindowExited = new PopWindowExited(popExit);
        btn.setOnAction(pop);

        // Event registration for the pop-up window's exit label
        popExit.setOnMouseExited(popWindowExited);
        popExit.setOnMouseClicked(new PopWindowExitHandler(popWindow));
        popExit.setOnMouseEntered(popWindowHover);

        // Setup and display of the pop-up window
        file.setPromptText("Ex: src/peggame/Model/board.txt");
        VBox v2 = new VBox();
        v2.getChildren().addAll(popExit,l, file, btn);
        v2.setAlignment(Pos.CENTER);
        Scene s2 = new Scene(v2);
        popWindow.setScene(s2);
        popWindow.initModality(Modality.APPLICATION_MODAL);
        popWindow.showAndWait();

        // After the pop-up window is closed, the game board is updated and the main game window is set up
        board = game.getBoard();

        // Setup for the main game window
        VBox wrapper =  new VBox();
        VBox v = new VBox();  
        v.setMinSize(500, 530);
        HBox bar = new HBox();

        // UI components for saving the game and displaying information
        TextField input = new TextField();
        Label save = createLabel("SAVE", 50, 30);
        Label info = createLabel("", 50, 30);
        Label exit = createLabel("EXIT", 50,30);
        ExitHandlerClick exitHandlerClick = new ExitHandlerClick(arg0);
        ExitHoverHandler hover = new ExitHoverHandler(exit);
        ExitHandlerExited exited = new ExitHandlerExited(exit);
        SaveHandlerHover hoverSave =  new SaveHandlerHover(save);
        SaveHandlerExited exitedSave =  new SaveHandlerExited(save);
        SaveHandler handlerSave = new SaveHandler(input, game, info);

        // Setting up event handling for the exit label
        exit.setOnMouseClicked(exitHandlerClick);
        exit.setOnMouseEntered(hover);
        exit.setOnMouseExited(exited);

        // Setting up event handling for the save label
        save.setOnMouseExited(exitedSave);
        save.setOnMouseEntered(hoverSave);
        save.setOnMouseClicked(handlerSave);

        // Setting up the information label
        info.setMaxWidth(Double.MAX_VALUE);

        // Initializing and configuring additional UI components for game status and file input
        Label status = createLabel("", 100, 30);
        Label st = createLabel("GAME STATUS:", 100, 30);
        input.setPrefSize(100, 30);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Attempt to initialize the game state and set up the game board UI
        try{
        game.state(status);
        input.setPromptText("Type the file name");

        // Adding UI components to the layout containers
        wrapper.getChildren().addAll(bar, v);
        bar.setPrefWidth(5*Peg.wrap(Peg.createPeg(false, null)).getWidth());
        bar.getChildren().addAll(save, input, st, status, info, exit);

        // Styling and alignment for the game board container
        v.setBackground(new Background(new BackgroundFill(Color.CADETBLUE,CornerRadii.EMPTY,Insets.EMPTY)));
        v.setAlignment(Pos.CENTER);

        // Generating the game board UI based on the game state
        boolean isHole = false; // Flag to determine if the current peg is a hole
        for(int i = 0; i < board.length;i++)  {
            HBox h = new HBox();
            h.setAlignment(Pos.CENTER);
            for (int j = 0; j < board[i].length; j++) {
                // Determines if the current position is a hole
                if(board[i][j] == '.'){
                    isHole = true;
                }
                // Wraps a peg in a container and sets up event handling
                HBox n = Peg.wrap(Peg.createPeg(isHole, new Location(i, j)));
                PegHandler handler = new PegHandler(n, game, info);
                n.setOnMouseClicked(handler);
                h.getChildren().add(n);
                isHole = false;
            }
            v.getChildren().add(h);
            
            // Assigning static references in the PegHandler for shared access
            PegHandler.board = v;
            PegHandler.status = status;
        }
        // Setting the scene and displaying the main game window
        Scene s = new Scene(wrapper);
        arg0.setScene(s);
        arg0.show();
    }catch(Exception e){
        // Handles exceptions, such as invalid game board configurations
        System.out.println("Invalid board");
        System.exit(0);
    }
        
    }

    /**
    * Creates a styled label with specified text, width, and height.
     * 
     * @param s The text to be displayed in the label.
    * @param width The minimum width of the label.
    * @param height The minimum height of the label.
    * @return A styled Label object.
    */
    private static Label createLabel(String s, int width, int height){
        Label label = new Label(s);
        label.setMinSize(width, height);
        label.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY, Insets.EMPTY)));
        label.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID , CornerRadii.EMPTY, BorderStroke.THIN)));
        label.setAlignment(Pos.CENTER);
        return label;
    }

    /**
    * The main method that launches the JavaFX application.
    * 
    * @param args The command line arguments.
    */
    public static void main(String[] args) {
        launch(args);
    }
    
}

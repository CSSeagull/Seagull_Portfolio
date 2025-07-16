package peggame.Model;

import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Peg extends Circle{ 
    public static Location globaLocation;
    public static boolean isGLobalClick;
    private Location location;
    private boolean isHole;
    private boolean isClicked;

    public static Peg createPeg(boolean isHole, Location location){
        Peg peg = new Peg();
        if(isHole){
            peg.setFill(Color.BLACK);
            peg.setRadius(25);
        }else{
            peg.setFill(Color.GREEN);
            peg.setRadius(50);
        }
        peg.setStrokeWidth(5);
        peg.setStroke(Color.BLACK);
        peg.isHole = isHole;
        peg.location = location;
        return peg;
    }

    public static void updatePeg(Peg peg){
        if(peg.isHole){
            peg.setFill(Color.BLACK);
            peg.setRadius(25);
            peg.isClicked = false;
        }else{
            peg.setFill(Color.GREEN);
            peg.setRadius(50);
        }
    }

    public void setHole(boolean isHole) {
        this.isHole = isHole;
    }
    public boolean isHole() {
        return isHole;
    }
    public boolean isClicked() {
        return isClicked;
    }
    public void setClicked(boolean clicked) {
        if(!isHole){
            this.isClicked = clicked;
    }}

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    public static HBox wrap(Peg peg){
        HBox wrapper = new HBox();
        int size = (int)Peg.createPeg(false, null).getRadius()*2 + 20;
        wrapper.setPrefSize(size, size);
        wrapper.getChildren().add(peg);
        wrapper.setAlignment(Pos.CENTER);
        return wrapper;
    }
}

package com.goobers.evilpinball;

//import java.util.ArrayList;
import java.awt.Graphics2D;

public class Level {
    
    PolyNode[] poly;

    public Level(){}

    public void renderPoly(Graphics2D frame, Camera camera){
        for (PolyNode polygon : poly){
            polygon.render(frame, camera);
        }
    }

    public void setPoly(PolyNode[] poly){
        this.poly = poly;
    }

    public PolyNode[] getPoly(){
        return poly;
    }

    public String toString(){
        String output = "Level:\n";
        for (PolyNode polygon : poly){
            output += polygon.toString() + "\n";
        }
        return output;
    }
}

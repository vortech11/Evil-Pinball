package com.goobers.evilpinball;

//import java.util.ArrayList;

public class Level {
    
    PolyNode[] poly;

    public Level(){}

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

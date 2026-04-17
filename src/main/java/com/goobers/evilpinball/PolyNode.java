package com.goobers.evilpinball;

//import java.awt.Color;

public class PolyNode {
    
    Vector2[] verticies;
    Vector2 position;
    double rotation;

    double scale;

    int[] color;

    public PolyNode(){}

    public void setVerticies(Vector2[] verticies){
        this.verticies = verticies;
    }

    public void setPosition(Vector2 position){
        this.position = position;
    }

    public void setRotation(double rotation){
        this.rotation = rotation;
    }

    public void setScale(double scale){
        this.scale = scale;
    }

    public void setColor(int[] color){
        this.color = color;
    }

    public String toString(){
        String verts = "Verticies: [";
        for (Vector2 point : verticies){
            verts += " " + point.toString();
        }
        verts += " ]";
        String colors = "Color: [";
        for (int rgb : color){
            colors += " " + rgb;
        }
        colors += " ]";
        return "PolyNode: " + verts + 
        " Position: " + position.toString() + 
        " Rotation: " + rotation + 
        " Scale: " + scale + 
        " Color: " + colors;
    }

}

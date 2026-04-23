package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Graphics2D;

public class PolyNode {
    
    Vector2[] vertices;
    Vector2 position;
    double rotation;

    double bounciness;

    double scale;

    int[] color;

    public PolyNode(){}

    public Vector2[] getGlobalvertices(){
        Vector2[] transformed = new Vector2[vertices.length];
        for (int i = 0; i < vertices.length; i++){
            Vector2 point = new Vector2(vertices[i]);
            point.scale(this.scale);
            point.rotate_rad_ip(rotation);
            point.add(position);
            transformed[i] = point;
        }
        return transformed;
    }

    public void render(Graphics2D frame, Camera camera){
        Vector2[] renderPoints = this.getGlobalvertices();
        for (int i = 0; i < vertices.length; i++){
            renderPoints[i] = camera.transformPoint(renderPoints[i]);
        }
        int[] Xs = Vector2.vectorArrayToInt_X(renderPoints);
        int[] Ys = Vector2.vectorArrayToInt_Y(renderPoints);
        frame.setColor(new Color(color[0], color[1], color[2]));
        frame.fillPolygon(Xs, Ys, renderPoints.length);
    }

    public void setvertices(Vector2[] vertices){
        this.vertices = vertices;
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

    public void setBounciness(double bounciness){
        this.bounciness = bounciness;
    }

    public String toString(){
        String verts = "vertices: [";
        for (Vector2 point : vertices){
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
        " Color: " + colors +
        " Bounciness: " + bounciness;
    }

}

package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Graphics2D;

public class PhysicsObj {
    Vector2 position;
    Vector2[] vertices;
    double rotation;
    Color color;

    Vector2 velocity;
    double angularVel;

    double scale;

    public PhysicsObj(
        Vector2 pos, 
        Vector2[] verticies, 
        double rotation, 
        Vector2 vel, 
        double angularVel, 
        Color color,
        double scale
    ){
        this.position = pos;
        this.vertices = verticies;
        this.rotation = rotation;
        this.velocity = vel;
        this.angularVel = angularVel;
        this.color = color;
        this.scale = scale;
    }

    public void updateposition(double dt){
        position.add(Vector2.scale(velocity, dt));
        rotation += angularVel * dt;
    }

    public Vector2[] getGlobalVertices(){
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
        Vector2[] renderPoints = this.getGlobalVertices();
        for (int i = 0; i < vertices.length; i++){
            renderPoints[i] = camera.transformPoint(renderPoints[i]);
        }
        int[] Xs = Vector2.vectorArrayToInt_X(renderPoints);
        int[] Ys = Vector2.vectorArrayToInt_Y(renderPoints);
        frame.setColor(color);
        frame.fillPolygon(Xs, Ys, renderPoints.length);
    }

}

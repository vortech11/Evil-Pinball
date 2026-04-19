package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.geom.*;
import java.util.ArrayList;

public class PhysicsBall {
    Vector2 position = new Vector2(0, 0);
    Vector2 velocity = new Vector2(0, 0);
    double size = 50;
    double mass = 1;
    double bounciness = 0.5;

    Vector2 gravity = new Vector2(0, 10000);

    Color color = new Color(0);

    ArrayList<Vector2> impulses = new ArrayList<Vector2>();

    public PhysicsBall(Vector2 position, double size, Color color) {
        this.position.copy(position);
        this.size = size;
        this.color = color;
    }

    public PhysicsBall(double x, double y, double size, Color color) {
        position.x = x;
        position.y = y;
        this.size = size;
        this.color = color;
    }

    public void update(double dt){
        velocity.add(Vector2.scale(gravity, dt));
        for (Vector2 impulse : impulses){
            velocity.add(impulse);
        }
        impulses.clear();
        position.add(velocity);
        //Vector2[] globalVerts = paddle.getGlobalvertices();
        //if (ObjectHandler.polyCircle(position, size, globalVerts)){
        //    position.subtract(velocity);
        //    velocity.y = 0.0;
        //}
    }

    public void render(Graphics2D frame, Camera camera){
        frame.setColor(color);
        Vector2 renderPosition = camera.transformPoint(position);
        frame.fillOval(
            (int) (renderPosition.x - size), 
            (int) (renderPosition.y - size), 
            (int) (size * 2 / camera.zoom), 
            (int) (size * 2 / camera.zoom)
        );
    }
}

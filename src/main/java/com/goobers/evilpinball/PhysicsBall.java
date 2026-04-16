package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.geom.*;

public class PhysicsBall {
    Vector2 possition = new Vector2(0, 0);
    Vector2 velocity = new Vector2(0, 0);
    double size = 50;

    double gravity = 10000;

    Color color = new Color(0);

    public PhysicsBall(Vector2 position, double size, Color color) {
        this.possition.copy(position);
        this.size = size;
        this.color = color;
    }

    public PhysicsBall(double x, double y, double size, Color color) {
        possition.x = x;
        possition.y = y;
        this.size = size;
        this.color = color;
    }

    public void update(double dt, Paddle paddle){
        velocity.y += gravity * dt;
        possition.add(velocity);
        Vector2[] globalVerts = paddle.getGlobalVertices();
        if (Collision.polyCircle(possition, size, globalVerts)){
            possition.subtract(velocity);
            velocity.y = 0.0;
        }
    }

    public void render(Graphics2D frame, Camera camera){
        frame.setColor(color);
        Vector2 renderPosition = camera.transformPoint(possition);
        frame.fillOval(
            (int) (renderPosition.x - size), 
            (int) (renderPosition.y - size), 
            (int) (size * 2 / camera.zoom), 
            (int) (size * 2 / camera.zoom)
        );
    }
}

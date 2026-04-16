package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.geom.*;

public class PhysicsBall {
    Vector2 position = new Vector2(0, 0);
    Vector2 velocity = new Vector2(0, 0);
    double size = 50;

    Vector2 gravity = new Vector2(0, 10000);

    Color color = new Color(0);

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

    public void update(double dt, Paddle paddle){
        velocity.add(Vector2.scale(gravity, dt));
        position.add(velocity);
        Vector2[] globalVerts = paddle.getGlobalVertices();
        if (Collision.polyCircle(position, size, globalVerts)){
            position.subtract(velocity);
            velocity.y = 0.0;
        }
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

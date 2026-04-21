package com.goobers.evilpinball;

//import java.awt.Color;
//import java.awt.Graphics2D;

public class PhysicsObj extends PolyNode {
    //Vector2 position;
    //Vector2[] vertices;
    //double rotation;
    //Color color;
    //double scale;

    Vector2 velocity;
    double angularVel;
    double mass;
    double bounciness = 0.5;
    double momentOfInertia = 0.001;
    double velocityScalar = 1;


    public PhysicsObj(
        Vector2 pos, 
        Vector2[] vertices, 
        double rotation, 
        Vector2 vel, 
        double angularVel, 
        int[] color,
        double scale,
        double mass,
        double momentOfInertia
    ){
        super();
        this.position = pos;
        this.vertices = vertices;
        this.rotation = rotation;
        this.velocity = vel;
        this.angularVel = angularVel;
        this.color = color;
        this.scale = scale;
        this.mass = mass;
        this.momentOfInertia = momentOfInertia;
    }

    public void updateposition(double dt){
        velocity.scale(velocityScalar);
        position.add(Vector2.scale(velocity, dt));
        rotation += angularVel * dt;
    }

}

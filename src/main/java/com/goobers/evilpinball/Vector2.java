package com.goobers.evilpinball;

import java.awt.geom.Point2D;
//import java.util.ArrayList;

public class Vector2 
    extends Point2D.Double{
    //Point2D point = new Point2D.Double(0, 0);

    public Vector2(double x, double y){
        super();
        this.x = x;
        this.y = y;
    }

    public Vector2(Vector2 point){
        super();
        this.x = point.x;
        this.y = point.y;
    }

    
    // already implamented by Point2D.Double 
    // myVector.distance(0, 0);
    // myVector.distanceSq(0, 0);
    public double magnitude(){
        return distance(0, 0);
    }

    public double magnitudeSq(){
        return distanceSq(0, 0);
    }

    public void add(Vector2 vector){
        x += vector.x;
        y += vector.y;
    }

    public void subtract(Vector2 vector){
        x -= vector.x;
        y -= vector.y;
    }

    public void scale(double value){
        x *= value;
        y *= value;
    }

    public void normalize(){
        if (magnitudeSq() == 0) return;
        scale(1/magnitude());
    }

    public static double dot(Vector2 vector1, Vector2 vector2){
        return vector1.x * vector2.x + vector1.y * vector2.y;
    }

    public static double cross(Vector2 vector1, Vector2 vector2){
        return vector1.x * vector2.y - vector1.y * vector2.x;
    }

    public static Vector2 normalize(Vector2 vector){
        if (vector.magnitudeSq() == 0) return vector;
        return scale(vector, 1/vector.magnitude());
    }

    public static Vector2 add(Vector2 vector1, Vector2 vector2){
        return new Vector2(vector1.x + vector2.x, vector1.y + vector2.y);
    }

    public static Vector2 subtract(Vector2 vector1, Vector2 vector2){
        return new Vector2(vector1.x - vector2.x, vector1.y - vector2.y);
    }

    public static Vector2 scale(Vector2 vector, double value){
        return new Vector2(vector.x * value, vector.y * value);
    }

    public void copy(Vector2 vector){
        this.x = vector.x;
        this.y = vector.y;
    }

    public void rotate_rad_ip(double radian){
        double mag = magnitude();
        if (mag == 0) return;
        double angle = Math.atan(y/x);
        if (x < 0){
            angle = Math.PI - Math.atan(y/-x);
        }
        angle += radian;
        x = mag * Math.cos(angle);
        y = mag * Math.sin(angle);
    }

    public static int[] vectorArrayToInt_X(Vector2[] points){
        int[] Xs = new int[points.length];
        for (int i = 0; i < points.length; i++){
            Xs[i] = (int) points[i].x;
        }
        return Xs;
    }

    public static int[] vectorArrayToInt_Y(Vector2[] points){
        int[] Ys = new int[points.length];
        for (int i = 0; i < points.length; i++){
            Ys[i] = (int) points[i].y;
        }
        return Ys;
    }

    public static Vector2 getNormal(Vector2 p1, Vector2 p2){
        Vector2 edge = Vector2.subtract(p2, p1);
        Vector2 normal = new Vector2(-edge.y, edge.x);
        normal.normalize();
        return normal;
    }

    @Override
    public String toString(){
        return "Vector2 " + x + " " + y;
    }
}

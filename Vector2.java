import java.awt.geom.Point2D;

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

    public static double dot(Vector2 vector1, Vector2 vector2){
        return vector1.x * vector2.x + vector1.y * vector2.y;
    }

    public static double cross(Vector2 vector1, Vector2 vector2){
        return vector1.x * vector2.y + vector1.y * vector2.x;
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
        double angle = Math.atan(y/x);
        if (x < 0){
            angle = Math.PI - Math.atan(y/-x);
        }
        angle += radian;
        x = mag * Math.cos(angle);
        y = mag * Math.sin(angle);
    }

    @Override
    public String toString(){
        return "Vector2 " + x + " " + y;
    }
}

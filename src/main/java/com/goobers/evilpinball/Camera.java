package com.goobers.evilpinball;

public class Camera {
    Vector2 position = new Vector2(0, 0);

    double zoom = 1;

    // the radian rotation of the camera
    double rotation = 0;

    Vector2 screenSize;

    public Camera(Vector2 position, double zoom, Vector2 screenSize){
        this.screenSize = screenSize;
        this.position.copy(position);
        this.zoom = zoom;
    }

    public void updateScreenSize(Vector2 screenSize){
        //this.screenSize.copy(screenSize);
    }

    public Vector2 transformPoint(Vector2 point){
        Vector2 newPoint = new Vector2(point);
        newPoint.subtract(position);
        newPoint.rotate_rad_ip(rotation);
        newPoint.scale(zoom);
        newPoint.add(Vector2.scale(screenSize, 0.5));
        return newPoint;
    }

    public Vector2 unTransformPoint(Vector2 point){
        Vector2 newPoint = new Vector2(point);
        newPoint.subtract(Vector2.scale(screenSize, 0.5));
        newPoint.scale(1/zoom);
        newPoint.rotate_rad_ip(-rotation);
        newPoint.add(position);
        return newPoint;
    }
}

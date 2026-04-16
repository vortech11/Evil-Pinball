package com.goobers.evilpinball;

public class Collision {
    Engine gameEngine;

    public Collision(Engine gameEngine){
        this.gameEngine = gameEngine;
    }

    public static boolean pointCircle(Vector2 circlePos, double size, Vector2 point){
        return circlePos.distance(point) < size;
    }

    public static boolean linePoint(Vector2 point, Vector2 p1, Vector2 p2){
        double lineLen = p1.distance(p2);

        double d1 = point.distance(p1);
        double d2 = point.distance(p2);

        final double buffer = 0.05;

        if (d1 + d2 >= lineLen - buffer && d1 + d2 <= lineLen + buffer)
            return true;
        return false;
    }

    public static boolean lineCircle(Vector2 circlePos, double size, Vector2 p1, Vector2 p2){
        boolean inside1 = pointCircle(circlePos, size, p1);
        boolean inside2 = pointCircle(circlePos, size, p2);
        if (inside1 || inside2) return true;

        double dist = p1.distance(p2);

        double dot = Vector2.dot(Vector2.subtract(circlePos, p1), Vector2.subtract(p2, p1)) / Math.pow(dist, 2);

        Vector2 closest = Vector2.add(p1, Vector2.scale(Vector2.subtract(p2, p1), dot));

        boolean onSegment = linePoint(closest, p1, p2);
        if (!onSegment) return false;

        double finalDist = circlePos.distance(closest);
        if (finalDist <= size)
            return true;
        return false;
    }

    public static boolean polyPoint(Vector2 point, Vector2[] vertices){
        boolean collision = false;

        for (int i = 0; i < vertices.length; i++){
            Vector2 vc = vertices[i];
            Vector2 vn = vertices[(i + 1) % vertices.length];

            if (((vc.y >= point.y && vn.y < point.y) || (vc.y < point.y && vn.y >= point.y)) &&
                (point.x < (vn.x-vc.x)*(point.y-vc.y) / (vn.y-vc.y)+vc.x)) {
                collision = !collision;
            }
        }

        return collision;
    }

    public static boolean polyCircle(Vector2 circlePos, double size, Vector2[] points){
        for (int i = 0; i < points.length; i++){
            Vector2 current = points[i];
            Vector2 next = points[(i + 1) % points.length];
            boolean colliding = lineCircle(circlePos, size, current, next);
            if (colliding) return true;
        }

        //boolean centerInside = polyPoint(circlePos, points);
        //if (centerInside) return true;

        return false;
    }
}

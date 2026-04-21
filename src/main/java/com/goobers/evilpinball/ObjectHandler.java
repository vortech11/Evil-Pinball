package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class ObjectHandler {
    Engine gameEngine;

    Level level;

    ArrayList<PhysicsObj> physObjs;
    ArrayList<PhysicsBall> balls;

    public ObjectHandler(Engine gameEngine, Level level){
        this.gameEngine = gameEngine;
        this.level = level;
        physObjs = new ArrayList<PhysicsObj>();
        balls = new ArrayList<PhysicsBall>();
    }

    public void newPhysObj(PhysicsObj object){
        physObjs.add(object);
    }

    public void newBall(Vector2 position){
        balls.add(new PhysicsBall(position, 10, new Color(50, 0, 255)));
    }

    public void render(Graphics2D frame, Camera camera){
        level.renderPoly(frame, camera);
        for (PhysicsObj obj : physObjs){
            obj.render(frame, camera);
        }
        for (PhysicsBall ball : balls){
            ball.render(frame, camera);
        }
    }

    public void update(double dt){
        for (PhysicsObj obj : physObjs){
            obj.updateposition(dt);
        }
        for (PhysicsBall ball : balls){
            ball.update(dt);
        }

        for (PhysicsObj obj : physObjs){
            for (PhysicsObj obj2 : physObjs){
                if (obj == obj2) continue;
                Vector2 mtd = getResolvePolyPoly(obj.getGlobalvertices(), obj2.getGlobalvertices());
                if (mtd.magnitudeSq() == 0) continue; // No collision
                
                Vector2 centerDiff = Vector2.subtract(obj.position, obj2.position);
                if (Vector2.dot(mtd, centerDiff) < 0) {
                    mtd.scale(-1);
                }
                
                double totalMass = obj.mass + obj2.mass;
                obj.position.add(Vector2.scale(mtd, obj2.mass / totalMass));
                obj2.position.add(Vector2.scale(mtd, -obj.mass / totalMass));

                FaceSet faces = identifyFaces(obj.getGlobalvertices(), obj2.getGlobalvertices(), mtd);
                if (faces == null) continue;
                ArrayList<PositionDepth> contactManifold = getContactManifold(faces.reference, faces.incident);
                for (PositionDepth contact : contactManifold){
                    applyImpulse(obj, obj2, contact, contactManifold.size(), mtd);
                }

            }
        }
        /*
        for (PhysicsBall ball : balls){
            for (PolyNode polygon : level.poly){
                if (polyCircle(ball.position, ball.size, polygon.getGlobalvertices())){
                    ball.position.subtract(ball.velocity);
                    ball.velocity.copy(new Vector2(0, 0));
                }
            }
            for (PhysicsBall ball2 : balls){
                if (ball == ball2) continue;
                if (circleCircle(ball.position, ball.size, ball2.position, ball2.size)){
                    
                    double distance = ball.position.distance(ball2.position);
                    Vector2 normal = Vector2.subtract(ball.position, ball2.position);
                    normal.normalize();
                    Vector2 tangent = new Vector2(-normal.y, normal.x);

                    //double impulseScalar = -(1 - )Vector2.subtract(ball.velocity, ball2.velocity))

                    Vector2 relative = Vector2.subtract(ball.velocity, ball2.velocity);

                    //ball.position.subtract(Vector2.scale(Vector2.subtract(ball2.position, ball.position), 0.5));
                    //ball2.position.subtract(Vector2.scale(Vector2.subtract(ball.position, ball2.position), 0.5));
                }
            }
        }
        */
    }

    public record Face(Vector2 point1, Vector2 point2, Vector2 normal){}

    public static Face getBestFace(Vector2[] vertices, Vector2 direction){
        double bestProjection = Double.MIN_VALUE;
        int bestVertex = -1;
        for (int i = 0; i < vertices.length; i++){
            double projection = Vector2.dot(vertices[i], direction);
            if (projection > bestProjection){
                bestProjection = projection;
                bestVertex = i;
            }
        }
        if (bestVertex == -1) return null;

        Vector2 vertex = vertices[bestVertex];
        Vector2 next = vertices[(bestVertex + 1) % vertices.length];
        Vector2 previous = vertices[(bestVertex - 1 + vertices.length) % vertices.length];
        
        Vector2 leftEdge = Vector2.normalize(Vector2.subtract(vertex, next));
        Vector2 rightEdge = Vector2.normalize(Vector2.subtract(vertex, previous));

        if (Math.abs(Vector2.dot(leftEdge, direction)) <= Math.abs(Vector2.dot(rightEdge, direction))) {
            return new Face(vertex, next, Vector2.getNormal(vertex, next));
            //return new Vector2[] {vertex, next};
            //return { v1: v, v2: vNext, normal: getNormal(v, vNext) };
        } else {
            return new Face(previous, next, Vector2.getNormal(previous, vertex));
            //return new Vector2[] {previous, vertex};
            //return { v1: vPrev, v2: v, normal: getNormal(vPrev, v) };
        }
    }

    public record FaceSet(Face reference, Face incident, boolean flipped){}

    public static FaceSet identifyFaces(Vector2[] vertices1, Vector2[] vertices2, Vector2 normal){
        Face faceA = getBestFace(vertices1, normal);
        Face faceB = getBestFace(vertices2, Vector2.scale(normal, -1));
        
        if (faceA == null || faceB == null) {
            return null;
        }
        
        if (Math.abs(Vector2.dot(faceA.normal, normal)) <= Math.abs(Vector2.dot(faceB.normal, normal)))
            return new FaceSet(faceA, faceB, false);
        return new FaceSet(faceB, faceA, true);
        
    }

    public static ArrayList<Vector2> clip(Vector2 p1, Vector2 p2, Vector2 normal, double offset) {
        ArrayList<Vector2> contactPoints = new ArrayList<Vector2>();
        
        double d1 = Vector2.dot(normal, p1) - offset;
        double d2 = Vector2.dot(normal, p2) - offset;

        if (d1 <= 0) contactPoints.add(p1);
        if (d2 <= 0) contactPoints.add(p2);

        // If they are on opposite sides, find the intersection point
        if (d1 * d2 < 0) {
            double t = d1 / (d1 - d2);
            Vector2 intersect = Vector2.add(Vector2.scale(Vector2.subtract(p2, p1), t), p1);
            contactPoints.add(intersect);
        }

        return contactPoints;
    }

    public record PositionDepth(Vector2 position, double depth){}

    public static ArrayList<PositionDepth> getContactManifold(Face reference, Face incident) {
        // Calculate side plane normals (perpendicular to reference normal)
        Vector2 referenceVec = Vector2.normalize(Vector2.subtract(reference.point2, reference.point1));
        
        // Define offsets for the clipping planes
        double negSideOffset = -Vector2.dot(referenceVec, reference.point1);
        double posSideOffset = Vector2.dot(referenceVec, reference.point2);
        double refPlaneOffset = Vector2.dot(reference.normal, reference.point1);

        // Clip against negative side plane
        ArrayList<Vector2> points = clip(incident.point1, incident.point2, Vector2.scale(referenceVec, -1), negSideOffset);
        if (points.size() < 2) return new ArrayList<PositionDepth>();

        // Clip against positive side plane
        points = clip(points.get(0), points.get(1), referenceVec, posSideOffset);
        if (points.size() < 2) return new ArrayList<PositionDepth>();

        // Clip against the Reference Face Plane (keep only penetrating points)
        ArrayList<PositionDepth> finalContacts = new ArrayList<PositionDepth>();
        for (Vector2 p : points) {
            double separation = Vector2.dot(reference.normal, p) - refPlaneOffset;
            if (separation <= 0) {
                finalContacts.add( new PositionDepth(p, -separation) );
            }
        }

        return finalContacts;
    }

    public static void applyImpulse(PhysicsObj objA, PhysicsObj objB, PositionDepth contact, int contactNum, Vector2 normal){
        Vector2 rA = Vector2.subtract(contact.position, objA.position);
        Vector2 rB = Vector2.subtract(contact.position, objB.position);

        Vector2 vA = Vector2.add((new Vector2(rA.y * -objA.angularVel, rA.x * objA.angularVel)), objA.velocity);
        Vector2 vB = Vector2.add((new Vector2(rB.y * -objB.angularVel, rB.x * objB.angularVel)), objB.velocity);

        Vector2 vRel = Vector2.subtract(vB, vA);

        double velAlongNormal = Vector2.dot(vRel, normal);

        if (velAlongNormal > 0) return;

        double restitution = Math.min(objA.bounciness, objB.bounciness);

        double rACrossN = Vector2.cross(rA, normal);
        double rBCrossN = Vector2.cross(rB, normal);

        double inverseMassSum = 
            (1 / objA.mass) + (1 / objB.mass) + 
            (rACrossN * rACrossN) / objA.momentOfInertia + 
            (rBCrossN * rBCrossN) / objB.momentOfInertia;
        
        double j = -(1 + restitution) * velAlongNormal;
        j /= inverseMassSum;
        j /= contactNum;

        Vector2 impulse = Vector2.scale(normal, j);
        
        objA.velocity.subtract(Vector2.scale(impulse, 1/objA.mass));
        objA.angularVel -= rACrossN * j / objA.momentOfInertia;

        objB.velocity.add(Vector2.scale(impulse, 1/objB.mass));
        objB.angularVel += rBCrossN * j / objB.momentOfInertia;
    }


    /**
     * This projects an array of points for a line. It takes in the orthoginal line of two consecutave points
     * Plug the orthoginal of two points on a polygon and the full verticies into this and they will be projected.
     * @param points
     * @param axis
     * @return
     */
    public static double[] projectPointsLine(Vector2[] points, Vector2 axis){
        double[] projected = new double[points.length];
        for (int i = 0; i < points.length; i++){
            projected[i] = Vector2.dot(points[i], axis);
        }
        return projected;
    }

    public record MinMax(double min, double max){}

    public static MinMax getArrayMinMax(double[] points){
        double minimum = Double.MAX_VALUE;
        double maximum = Double.MIN_VALUE;

        for (double point : points){
            if (point < minimum) minimum = point;
            if (point > maximum) maximum = point;
        }

        return new MinMax(minimum, maximum);
    }

    public static double getOverlap(double[] points1, double[] points2){
        MinMax minMax1 = getArrayMinMax(points1);
        MinMax minMax2 = getArrayMinMax(points2);
        return Math.min(minMax1.max, minMax2.max) - Math.max(minMax1.min, minMax2.min);
    }

    public static Vector2 getResolvePolyPoly(Vector2[] vertices1, Vector2[] vertices2){
        double minOverlap = Double.MAX_VALUE;
        Vector2 minAxis = null;
        
        Vector2[][] allVertices = {vertices1, vertices2};

        for (Vector2[] vertices : allVertices){
            for (int i = 0; i < vertices.length; i++){
                Vector2 p1 = vertices[i];
                Vector2 p2 = vertices[(i + 1) % vertices.length];
                Vector2 axis = new Vector2(-(p2.y-p1.y), p2.x-p1.x);
                axis.normalize();
                double[] projec1 = projectPointsLine(vertices1, axis);
                double[] projec2 = projectPointsLine(vertices2, axis);
                double overlap = getOverlap(projec1, projec2);

                if (overlap <= 0) return new Vector2(0, 0);
                
                if (overlap < minOverlap){
                    minOverlap = overlap;
                    minAxis = axis;
                }
            }
        }
        return Vector2.scale(minAxis, minOverlap);
    }

    public static boolean pointCircle(Vector2 circlePos, double size, Vector2 point){
        return circlePos.distance(point) < size;
    }

    public static boolean circleCircle(Vector2 pos1, double size1, Vector2 pos2, double size2){
        return pos1.distance(pos2) < size1 + size2;
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

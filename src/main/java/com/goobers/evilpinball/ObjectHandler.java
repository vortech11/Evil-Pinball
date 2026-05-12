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
                Manifold collision = collide(obj.getGlobalvertices(), obj2.getGlobalvertices());
                if (collision == null) continue; // No collision
                Vector2 mtd = Vector2.scale(collision.normal, collision.penetration);
                
                double totalMass = obj.mass + obj2.mass;

                for (Vector2 contact : collision.contacts){
                    applyImpulse(obj, obj2, contact, collision.contacts.length, mtd);
                }

                //obj.position.add(Vector2.scale(mtd, 1));
                obj.position.subtract(Vector2.scale(mtd, obj.mass / totalMass));
                obj2.position.add(Vector2.scale(mtd, -obj2.mass / totalMass));
                
            }
            
            for (PolyNode hardObj : level.poly){
                Manifold collisionManifold = collide(obj.getGlobalvertices(), hardObj.getGlobalvertices());
                if (collisionManifold == null) continue; // No collision
                //System.out.println("\n======Start Debug=======");
                //System.out.println("Moving points: ");
                //for (Vector2 point : obj.getGlobalvertices()) System.out.println(point);
                //System.out.println("Static points: ");
                //for (Vector2 point : hardObj.getGlobalvertices()) System.out.println(point);
                Vector2 mtd = Vector2.scale(collisionManifold.normal, collisionManifold.penetration);
                System.out.println("contactManifold: " + collisionManifold.contacts.length);
                for (Vector2 contact : collisionManifold.contacts){
                    System.out.println(contact);
                    applyImpulse(obj, hardObj, contact, collisionManifold.contacts.length, mtd);
                }
                
                obj.position.subtract(mtd);
            }
        }
        
        for (PhysicsBall ball : balls){
            for (PolyNode polygon : level.poly){
                Manifold collisionManifold = circlePolyManifold(ball.position, ball.size, polygon.getGlobalvertices());
                if (collisionManifold == null){
                    continue;
                }
                Vector2 mtd = Vector2.scale(collisionManifold.normal, collisionManifold.penetration);
                ball.position.subtract(mtd);
                applyImpulse(ball, polygon, collisionManifold.contacts[0], 1, mtd);
            }
            /*
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
            */
        }
        
    }

    public static void applyImpulse(PhysicsObj objA, PhysicsObj objB, Vector2 contact, int contactNum, Vector2 normal){
        Vector2 normalUnit = Vector2.normalize(normal);
        if (normalUnit.magnitudeSq() == 0) return;

        Vector2 rA = Vector2.subtract(contact, objA.position);
        Vector2 rB = Vector2.subtract(contact, objB.position);

        Vector2 vA = Vector2.add((new Vector2(rA.y * -objA.angularVel, rA.x * objA.angularVel)), objA.velocity);
        Vector2 vB = Vector2.add((new Vector2(rB.y * -objB.angularVel, rB.x * objB.angularVel)), objB.velocity);

        Vector2 vRel = Vector2.subtract(vB, vA);

        double velAlongNormal = Vector2.dot(vRel, normalUnit);
        //if (velAlongNormal > 0) return;

        double restitution = Math.min(objA.bounciness, objB.bounciness);

        double rACrossN = Vector2.cross(rA, normalUnit);
        double rBCrossN = Vector2.cross(rB, normalUnit);

        double inverseMassSum = 
            (1 / objA.mass) + (1 / objB.mass) + 
            (rACrossN * rACrossN) / objA.momentOfInertia + 
            (rBCrossN * rBCrossN) / objB.momentOfInertia;
        
        double j = -(1 + restitution) * velAlongNormal;
        j /= inverseMassSum;
        j /= contactNum;

        Vector2 impulse = Vector2.scale(normalUnit, j);
        
        objA.velocity.subtract(Vector2.scale(impulse, 1/objA.mass));
        objA.angularVel -= rACrossN * j / objA.momentOfInertia;

        objB.velocity.add(Vector2.scale(impulse, 1/objB.mass));
        objB.angularVel += rBCrossN * j / objB.momentOfInertia;
    }

    public static void applyImpulse(PhysicsObj objA, PolyNode objB, Vector2 contact, int contactNum, Vector2 normal){
        Vector2 normalUnit = Vector2.normalize(normal);
        //Vector2 normalUnit = normal;
        if (normalUnit.magnitudeSq() == 0) return;

        Vector2 rA = Vector2.subtract(contact, objA.position);

        Vector2 vA = Vector2.add((new Vector2(rA.y * -objA.angularVel, rA.x * objA.angularVel)), objA.velocity);

        Vector2 vRel = Vector2.scale(vA, -1);

        double velAlongNormal = Vector2.dot(vRel, normalUnit);
        //if (velAlongNormal > 0) return;

        double restitution = Math.min(objA.bounciness, objB.bounciness);

        double rACrossN = Vector2.cross(rA, normalUnit);

        double inverseMassSum = 
            (1 / objA.mass) + 
            (rACrossN * rACrossN) / objA.momentOfInertia;
        
        double j = -(1 + restitution) * velAlongNormal;
        j /= inverseMassSum;
        j /= contactNum;

        Vector2 impulse = Vector2.scale(normalUnit, j);
        
        objA.velocity.subtract(Vector2.scale(impulse, 1/objA.mass));
        objA.angularVel -= rACrossN * j / objA.momentOfInertia;
    }

    public static void applyImpulse(PhysicsBall objA, PhysicsBall objB, Vector2 contact, int contactNum, Vector2 normal){
        Vector2 normalUnit = Vector2.normalize(normal);
        if (normalUnit.magnitudeSq() == 0) return;

        Vector2 rA = Vector2.subtract(contact, objA.position);
        Vector2 rB = Vector2.subtract(contact, objB.position);

        Vector2 vA = Vector2.add((new Vector2(rA.y * -objA.angularVel, rA.x * objA.angularVel)), objA.velocity);
        Vector2 vB = Vector2.add((new Vector2(rB.y * -objB.angularVel, rB.x * objB.angularVel)), objB.velocity);

        Vector2 vRel = Vector2.subtract(vB, vA);

        double velAlongNormal = Vector2.dot(vRel, normalUnit);
        //if (velAlongNormal > 0) return;

        double restitution = Math.min(objA.bounciness, objB.bounciness);

        double rACrossN = Vector2.cross(rA, normalUnit);
        double rBCrossN = Vector2.cross(rB, normalUnit);

        double inverseMassSum = 
            (1 / objA.mass) + (1 / objB.mass) + 
            (rACrossN * rACrossN) / objA.momentOfInertia + 
            (rBCrossN * rBCrossN) / objB.momentOfInertia;
        
        double j = -(1 + restitution) * velAlongNormal;
        j /= inverseMassSum;
        j /= contactNum;

        Vector2 impulse = Vector2.scale(normalUnit, j);
        
        objA.velocity.subtract(Vector2.scale(impulse, 1/objA.mass));
        objA.angularVel -= rACrossN * j / objA.momentOfInertia;

        objB.velocity.add(Vector2.scale(impulse, 1/objB.mass));
        objB.angularVel += rBCrossN * j / objB.momentOfInertia;
    }

    public static void applyImpulse(PhysicsBall ball, PolyNode objB, Vector2 contact, int contactNum, Vector2 normal){
        Vector2 normalUnit = Vector2.normalize(normal);
        //Vector2 normalUnit = normal;
        if (normalUnit.magnitudeSq() == 0) return;

        Vector2 rA = Vector2.subtract(contact, ball.position);

        Vector2 vA = Vector2.add((new Vector2(rA.y * -ball.angularVel, rA.x * ball.angularVel)), ball.velocity);

        Vector2 vRel = Vector2.scale(vA, -1);

        double velAlongNormal = Vector2.dot(vRel, normalUnit);
        //if (velAlongNormal > 0) return;

        double restitution = Math.min(ball.bounciness, objB.bounciness);

        double rACrossN = Vector2.cross(rA, normalUnit);

        double inverseMassSum = 
            (1 / ball.mass) + 
            (rACrossN * rACrossN) / ball.momentOfInertia;
        
        double j = -(1 + restitution) * velAlongNormal;
        j /= inverseMassSum;
        j /= contactNum;

        Vector2 impulse = Vector2.scale(normalUnit, j);
        
        ball.velocity.subtract(Vector2.scale(impulse, 1/ball.mass));
        ball.angularVel -= rACrossN * j / ball.momentOfInertia;
    }

    public static Manifold circlePolyManifold(Vector2 ballCenter, double radius, Vector2[] poly){
        Vector2 point = findClosestPoint(poly, ballCenter);
        Vector2 axis = Vector2.normalize(Vector2.perpendicular(Vector2.subtract(point, ballCenter)));
        double projected = Vector2.dot(ballCenter, axis);
        MinMax projA = project(poly, axis);
        MinMax projB = new MinMax(projected - radius, projected + radius);

        double overlap = Math.min(projA.max(), projB.max())
                           - Math.max(projA.min(), projB.min());

        if (overlap < -Double.MIN_VALUE){
            return null;
        }

        return new Manifold(axis, overlap, new Vector2[] {point});
    }

    public record MinMax(double min, double max){}

    public static MinMax project(Vector2[] verts, Vector2 axis) {
        double min = Double.MAX_VALUE;
        double max = Double.NEGATIVE_INFINITY;

        for (Vector2 v : verts) {
            double p = Vector2.dot(v, axis);
            if (p < min) min = p;
            if (p > max) max = p;
        }
        return new MinMax(min, max);
    }

    public static class AxisResult {
        Vector2 normal;
        double penetration;
        int edgeIndex;

        AxisResult(Vector2 n, double p, int i) {
            normal = n;
            penetration = p;
            edgeIndex = i;
        }
    }

    public static AxisResult findAxisLeastPenetration(Vector2[] A, Vector2[] B) {
        double minOverlap = Double.MAX_VALUE;
        Vector2 bestNormal = null;
        int bestEdge = -1;

        //Vector2 centerA = computeCenter(A);
        //Vector2 centerB = computeCenter(B);

        for (int i = 0; i < A.length; i++) {
            Vector2 p1 = A[i];
            Vector2 p2 = A[(i + 1) % A.length];

            Vector2 edge = Vector2.subtract(p1, p2);
            Vector2 axis = Vector2.normalize(Vector2.perpendicular(edge));

            // FORCE CONSISTENT DIRECTION
            //if (Vector2.dot(axis, Vector2.subtract(centerB, centerA)) < 0) {
            //    axis = Vector2.scale(axis, -1);
            //}

            //double maxB = -Double.MAX_VALUE;
            //for (Vector2 v : B){
            //    maxB = Math.max(maxB, Vector2.dot(v, axis));
            //}
            //
            //double minA = Double.MAX_VALUE;
            //for (Vector2 v : A){
            //    minA = Math.min(minA, Vector2.dot(v, axis));
            //}
            //
            //if (maxB < minA){
            //    axis = Vector2.scale(axis, -1);
            //}

            MinMax projA = project(A, axis);
            MinMax projB = project(B, axis);

            double overlap = Math.min(projA.max(), projB.max())
                           - Math.max(projA.min(), projB.min());

            if (overlap < -1e-5) return null; // separating axis

            if (overlap < minOverlap) {
                minOverlap = overlap;
                bestNormal = axis;
                bestEdge = i;
            }
        }

        return new AxisResult(bestNormal, minOverlap, bestEdge);
    }

    public static Vector2 findClosestPoint(Vector2[] poly, Vector2 point){
        double min = Double.MAX_VALUE;
        Vector2 minPoint = null;
        for (Vector2 polyPoint : poly){
            double distance = polyPoint.distanceSq(point);
            if (distance < min){
                min = distance;
                minPoint = polyPoint;
            }
        }
        return minPoint;
    }

    public record Edge(Vector2 vertex, Vector2 p1, Vector2 p2){}

    public static Edge findPointFarthest(Vector2[] poly, Vector2 normal){
        double max = -Double.MAX_VALUE;
        int index = -1;
        
        for (int i = 0; i < poly.length; i++){
            double projection = Vector2.dot(poly[i], normal);
            if (projection > max){
                max = projection;
                index = i;
            }
        }

        Vector2 vertex = poly[index];
        Vector2 next = poly[(index + 1) % poly.length];
        Vector2 previous = poly[(index - 1 + poly.length) % poly.length];

        Vector2 left = Vector2.subtract(vertex, next);
        Vector2 right = Vector2.subtract(vertex, previous);

        left.normalize();
        right.normalize();

        if (Vector2.dot(right, normal) <= Vector2.dot(left, normal)){
            return new Edge(vertex, previous, vertex);
        }
        
        return new Edge(vertex, vertex, next);
    }

    public static Vector2[] clip(Vector2 a, Vector2 b, Vector2 normal, double offset) {
        Vector2[] out = new Vector2[2];
        int count = 0;

        double da = Vector2.dot(normal, a) - offset;
        double db = Vector2.dot(normal, b) - offset;

        if (da >= 0) out[count++] = a;
        if (db >= 0) out[count++] = b;

        if (da * db < 0) {
            double t = da / (da - db);
            Vector2 p = Vector2.add(a, Vector2.scale(Vector2.subtract(b, a), t));
            out[count++] = p;
        }

        if (count == 0) return new Vector2[0];
        if (count == 1) return new Vector2[] { out[0] };

        return out;
    }

    public static Vector2 computeCenter(Vector2[] points) {
        double x = 0;
        double y = 0;

        for (Vector2 p : points) {
            x += p.x;
            y += p.y;
        }

        int count = points.length;
        return new Vector2(x / count, y / count);
    }

    public static void printStuff(Vector2[] A, Vector2[] B, Edge inc, Edge ref){
        System.out.println("A poly: ");
        for (Vector2 point : A) System.out.println(point);
        System.out.println("B poly: ");
        for (Vector2 point : B) System.out.println(point);

        System.out.println("Incident points:");
        System.out.println(inc.p1);
        System.out.println(inc.p2);
        System.out.println("Reference points:");
        System.out.println(ref.p1);
        System.out.println(ref.p2);
    }

    public static Vector2[] determineManifold(Vector2[] A, Vector2[] B, AxisResult refRes){
        Vector2 normal = refRes.normal;

        Edge aEdge = findPointFarthest(A, normal);
        Edge bEdge = findPointFarthest(B, Vector2.scale(normal, -1));

        Edge ref;
        Edge inc;
        Vector2[] refPoly;
        boolean flip = false;
        if (Math.abs(Vector2.dot(Vector2.subtract(aEdge.p1, aEdge.p2), normal)) <= Math.abs(Vector2.dot(Vector2.subtract(bEdge.p1, bEdge.p2), normal))){
            ref = aEdge;
            inc = bEdge;
            refPoly = A;
        } else {
            ref = bEdge;
            inc = aEdge;
            refPoly = B;
            // we need to set a flag indicating that the reference
            // and incident edge were flipped so that when we do the final
            // clip operation, we use the right edge normal
            flip = true;
        }

        // build side planes from reference face
        Vector2 edge = Vector2.subtract(ref.p2, ref.p1);
        Vector2 edgeDir = Vector2.normalize(edge);

        Vector2 sideNormal = Vector2.normalize(Vector2.perpendicular(edge));
        //Vector2 sideNormal = refNormal;

        double leftOffset = Vector2.dot(edgeDir, ref.p1);
        double rightOffset = Vector2.dot(Vector2.scale(edgeDir, -1), ref.p2);

        Vector2[] clip1 = clip(
            inc.p1,
            inc.p2,
            Vector2.scale(edgeDir, 1),
            leftOffset
        );

        if (clip1.length < 2) {
            System.out.println("EXIT 1");
            printStuff(A, B, inc, ref);
            return null;
        }

        Vector2[] clip2 = clip(
            clip1[0],
            clip1[1],
            Vector2.scale(edgeDir, -1),
            rightOffset
        );

        if (clip2.length < 2) {
            System.out.println("EXIT 2");
            System.out.println("Clips: ");
            System.out.println(clip1[0]);
            System.out.println(clip1[1]);
            printStuff(A, B, inc, ref);
            return null;
        }

        ///*
        if (flip) normal = Vector2.scale(normal, -1);
        double max = Vector2.dot(normal, ref.p1);

        Vector2[] contacts = new Vector2[2];
        int count = 0;

        for (Vector2 v : clip2) {
            double separation = Vector2.dot(normal, v) - max;
            if (separation <= 0) {
                contacts[count++] = v;
            }
        }
        //*/

        /*
        Vector2[] contacts = new Vector2[2];
        int count = 0;

        for (Vector2 v : clip2) {
            boolean intersecting = polyPoint(v, refPoly);
            if (intersecting) {
                contacts[count++] = v;
            }
        }
        */

        //int count = 2;
        //Vector2[] contacts = clip2;

        if (count == 0) {
            System.out.println("EXIT 3");
            System.out.println("Clip1: ");
            System.out.println(clip1[0]);
            System.out.println(clip1[1]);
            System.out.println("Clip2: ");
            System.out.println(clip2[0]);
            System.out.println(clip2[1]);
            printStuff(A, B, inc, ref);
            return null;
        }

        Vector2[] finalContacts = (count == 2)
            ? contacts
            : new Vector2[] { contacts[0] };
        
        return finalContacts;
    }

    public record Manifold(Vector2 normal, double penetration, Vector2[] contacts) {}

    public static Manifold collide(Vector2[] A, Vector2[] B) {

        AxisResult resA = findAxisLeastPenetration(A, B);
        if (resA == null) return null;

        AxisResult resB = findAxisLeastPenetration(B, A);
        if (resB == null) return null;

        Vector2[] refPoly, incPoly;
        AxisResult refRes;

        boolean flip;

        if (resA.penetration <= resB.penetration) {
            refPoly = A;
            incPoly = B;
            refRes = resA;
            flip = false;
        } else {
            refPoly = B;
            incPoly = A;
            refRes = resB;
            flip = true;
        }

        //Vector2 refNormal = refFace.normal;
        Vector2 refNormal = refRes.normal;

        if (flip) {
            refNormal = Vector2.scale(refRes.normal, -1);
        }

        Vector2[] finalContacts = determineManifold(refPoly, incPoly, refRes);

        if (finalContacts == null) return null;

        return new Manifold(refNormal, refRes.penetration, finalContacts);
    }

    public static boolean pointCircle(Vector2 circlePos, double size, Vector2 point){
        return circlePos.distance(point) < size;
    }

    public static Vector2 circleCircle(Vector2 pos1, double size1, Vector2 pos2, double size2){
        if (pos1.distance(pos2) < size1 + size2){
            return Vector2.subtract(pos2, pos1);
        }
        return null;
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

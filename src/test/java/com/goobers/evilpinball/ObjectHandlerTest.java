package com.goobers.evilpinball;

//import static com.goobers.evilpinball.ObjectHandler.collide;
import static com.goobers.evilpinball.ObjectHandler.clip;
import static com.goobers.evilpinball.ObjectHandler.collide;
import static com.goobers.evilpinball.ObjectHandler.computeCenter;
import static com.goobers.evilpinball.ObjectHandler.findAxisLeastPenetration;
import static com.goobers.evilpinball.ObjectHandler.findIncidentEdge;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.goobers.evilpinball.ObjectHandler.*;

public class ObjectHandlerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testSimpleOverlapRefFace() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(10, 0),
            new Vector2(10, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(1, 3),
            new Vector2(2, 1),
            new Vector2(4, 4),
        };

        AxisResult aTb = findAxisLeastPenetration(A, B);
        AxisResult bTa = findAxisLeastPenetration(B, A);

        assertNotNull(aTb);
        assertNotNull(bTa);

        assertTrue(aTb.penetration == 1);
        assertTrue(aTb.normal.equals(new Vector2(0, 1)));
        assertTrue(aTb.penetration <= bTa.penetration);

        Vector2[] refPoly;
        Vector2[] incPoly;
        AxisResult refRes;

        if (aTb.penetration <= bTa.penetration) {
            refPoly = A;
            incPoly = B;
            refRes = aTb;
        } else {
            refPoly = B;
            incPoly = A;
            refRes = bTa;
        }

        assertTrue(refPoly == A);

    }

    private void assertEdgeEquals(Vector2[] expected, Vector2[] actual) {
        assertNotNull(actual);
        assertEquals(2, actual.length);

        boolean direct =
            expected[0].distanceSq(actual[0]) < 1e-6 &&
            expected[1].distanceSq(actual[1]) < 1e-6;

        boolean swapped =
            expected[0].distanceSq(actual[1]) < 1e-6 &&
            expected[1].distanceSq(actual[0]) < 1e-6;

        assertTrue(direct || swapped, "Edges do not match");
    }

    @Test
    void testSquareIncidentEdge_TopNormal() {
        Vector2[] square = new Vector2[] {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2 refNormal = new Vector2(0, 1); // pointing up

        Vector2[] edge = findIncidentEdge(square, refNormal);

        // Expected: TOP edge (most opposite normal is downward face)
        Vector2[] expected = new Vector2[] {
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        assertEdgeEquals(expected, edge);
    }

    @Test
    void testSquareIncidentEdge_BottomNormal() {
        Vector2[] square = new Vector2[] {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2 refNormal = new Vector2(0, -1);

        Vector2[] edge = findIncidentEdge(square, refNormal);

        Vector2[] expected = new Vector2[] {
            new Vector2(0, 0),
            new Vector2(2, 0)
        };

        assertEdgeEquals(expected, edge);
    }

    private boolean edgeMatches(Vector2[] expected, Vector2[] actual) {
        return (
            (expected[0].distanceSq(actual[0]) < 1e-6 &&
            expected[1].distanceSq(actual[1]) < 1e-6)
        ||
            (expected[0].distanceSq(actual[1]) < 1e-6 &&
            expected[1].distanceSq(actual[0]) < 1e-6)
        );
    }

    @Test
    void testIncidentEdge_Symmetry() {
        Vector2[] poly = new Vector2[] {
            new Vector2(0, 0),
            new Vector2(3, 0),
            new Vector2(3, 1),
            new Vector2(0, 1)
        };

        Vector2 normal = new Vector2(0, 1);

        Vector2[] edge1 = findIncidentEdge(poly, normal);
        Vector2[] edge2 = findIncidentEdge(poly, Vector2.scale(normal, -1));

        assertNotNull(edge1);
        assertNotNull(edge2);

        // They should NOT be the same edge
        assertFalse(
            edge1[0].distanceSq(edge2[0]) < 1e-6 &&
            edge1[1].distanceSq(edge2[1]) < 1e-6
        );
    }

    @Test
    void testIncidentEdge_DiagonalNormal() {
        Vector2[] square = new Vector2[] {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2 refNormal = Vector2.normalize(new Vector2(1, 1));

        Vector2[] edge = findIncidentEdge(square, refNormal);

        assertNotNull(edge);
        assertEquals(2, edge.length);
    }

    private boolean approx(Vector2 a, Vector2 b) {
        return a.distanceSq(b) < 1e-6;
    }

    @Test
    void testClip_BothInside() {
        Vector2 a = new Vector2(0, 0);
        Vector2 b = new Vector2(1, 0);

        Vector2 normal = new Vector2(0, 1);
        double offset = 1; // everything below y=1 is inside

        Vector2[] result = clip(a, b, normal, offset);

        assertEquals(2, result.length);
        assertTrue(approx(result[0], a) || approx(result[1], a));
        assertTrue(approx(result[0], b) || approx(result[1], b));
    }

    @Test
    void testClip_BothOutside() {
        Vector2 a = new Vector2(0, 2);
        Vector2 b = new Vector2(1, 2);

        Vector2 normal = new Vector2(0, 1);
        double offset = 1;

        Vector2[] result = clip(a, b, normal, offset);

        assertEquals(0, result.length);
    }

    @Test
    void testClip_OneInsideOneOutside() {
        Vector2 a = new Vector2(0, 0);
        Vector2 b = new Vector2(0, 2);

        Vector2 normal = new Vector2(0, 1);
        double offset = 1;

        Vector2[] result = clip(a, b, normal, offset);

        assertEquals(2, result.length);

        Vector2 expectedInside = new Vector2(0, 0);
        Vector2 expectedIntersection = new Vector2(0, 1);

        boolean hasA = false;
        boolean hasI = false;

        for (Vector2 v : result) {
            if (v.distanceSq(expectedInside) < 1e-6) hasA = true;
            if (v.distanceSq(expectedIntersection) < 1e-6) hasI = true;
        }

        assertTrue(hasA);
        assertTrue(hasI);
    }

    @Test
    void testClip_CrossingPlane() {
        Vector2 a = new Vector2(0, 0);
        Vector2 b = new Vector2(0, 2);

        Vector2 normal = new Vector2(0, 1);
        double offset = 1;

        Vector2[] result = clip(a, b, normal, offset);

        assertEquals(2, result.length);

        // one must be intersection at y=1
        Vector2 expectedIntersection = new Vector2(0, 1);

        boolean foundIntersection = approx(result[0], expectedIntersection)
                                || approx(result[1], expectedIntersection);

        assertTrue(foundIntersection);
    }

    @Test
    void testClip_HorizontalCross() {
        Vector2 a = new Vector2(-1, 0);
        Vector2 b = new Vector2(1, 0);

        Vector2 normal = new Vector2(1, 0);
        double offset = 0;

        Vector2[] result = clip(a, b, normal, offset);

        assertEquals(2, result.length);

        Vector2 expected = new Vector2(0, 0);

        boolean found = approx(result[0], expected) || approx(result[1], expected);

        assertTrue(found);
    }

    @Test
    void testClip_EndpointOnPlane() {
        Vector2 a = new Vector2(0, 1);
        Vector2 b = new Vector2(1, 2);

        Vector2 normal = new Vector2(0, 1);
        double offset = 1;

        Vector2[] result = clip(a, b, normal, offset);

        // a is exactly on plane → should be kept
        assertTrue(result.length >= 1);

        boolean hasA = false;
        for (Vector2 v : result) {
            if (approx(v, a)) hasA = true;
        }

        assertTrue(hasA);
    }

    @Test
    void testClip_DiagonalCrossing() {
        Vector2 a = new Vector2(-1, -1);
        Vector2 b = new Vector2(1, 1);

        Vector2 normal = new Vector2(0, 1);
        double offset = 0;

        Vector2[] result = clip(a, b, normal, offset);

        assertTrue(result.length >= 1);

        // intersection should exist at y = 0
        boolean found = false;

        for (Vector2 v : result) {
            if (Math.abs(v.y) < 1e-6) {
                found = true;
            }
        }

        assertTrue(found);
    }

        public static Vector2[] determineManifold(Vector2[] refPoly, Vector2[] incPoly, Vector2 refNormal, AxisResult refRes){
        Face refFace = getFace(refPoly, refRes.edgeIndex);

        // incident edge MUST use reference face normal
        Vector2[] incident = findIncidentEdge(incPoly, refNormal);

        // build side planes from reference face
        Vector2 edge = Vector2.subtract(refFace.v2(), refFace.v1());
        Vector2 edgeDir = Vector2.normalize(edge);

        Vector2 sideNormal = Vector2.normalize(Vector2.perpendicular(edge));

        double leftOffset = Vector2.dot(sideNormal, refFace.v1());
        double rightOffset = Vector2.dot(Vector2.scale(sideNormal, -1), refFace.v2());

        Vector2[] clip1 = clip(
            incident[0],
            incident[1],
            Vector2.scale(sideNormal, -1),
            -leftOffset
        );

        if (clip1.length < 2) {
            System.out.println("EXIT 1");
            return null;
        }

        Vector2[] clip2 = clip(
            clip1[0],
            clip1[1],
            sideNormal,
            rightOffset
        );

        if (clip2.length < 2) {
            System.out.println("EXIT 2");
            return null;
        }

        double refOffset = Vector2.dot(refNormal, refFace.v1());

        Vector2[] contacts = new Vector2[2];
        int count = 0;

        for (Vector2 v : clip2) {
            double separation = Vector2.dot(refNormal, v) - refOffset;
            if (separation <= 0) {
                contacts[count++] = v;
            }
        }

        if (count == 0) {
            System.out.println("EXIT 3");
            return null;
        }

        Vector2[] finalContacts = (count == 2)
            ? contacts
            : new Vector2[] { contacts[0] };
        
        return finalContacts;
    }

    @Test
    void testManifold_SimpleOverlap() {

        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(1, 1),
            new Vector2(3, 1),
            new Vector2(3, 3),
            new Vector2(1, 3)
        };

        AxisResult res = findAxisLeastPenetration(A, B);
        assertNotNull(res);

        Vector2[] contacts = determineManifold(A, B, res.normal, res);

        assertNotNull(contacts);
        assertTrue(contacts.length >= 1);
    }

    @Test
    void testManifold_CornerTouching() {

        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(2, 2),
            new Vector2(4, 2),
            new Vector2(4, 4),
            new Vector2(2, 4)
        };

        AxisResult res = findAxisLeastPenetration(A, B);

        Vector2[] contacts = determineManifold(A, B, res.normal, res);

        assertNotNull(contacts);
        assertEquals(1, contacts.length);
    }

    @Test
    void testManifold_FaceOverlap() {

        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(3, 0),
            new Vector2(3, 3),
            new Vector2(0, 3)
        };

        Vector2[] B = {
            new Vector2(1, 1),
            new Vector2(4, 1),
            new Vector2(4, 4),
            new Vector2(1, 4)
        };

        AxisResult res = findAxisLeastPenetration(A, B);

        Vector2[] contacts = determineManifold(A, B, res.normal, res);

        assertNotNull(contacts);
        assertTrue(contacts.length == 1 || contacts.length == 2);
    }

    @Test
    void testManifold_NoCollision() {

        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(5, 5),
            new Vector2(7, 5),
            new Vector2(7, 7),
            new Vector2(5, 7)
        };

        AxisResult res = findAxisLeastPenetration(A, B);

        if (res == null) {
            assertTrue(true);
            return;
        }

        Vector2[] contacts = determineManifold(A, B, res.normal, res);

        assertNull(contacts);
    }

    @Test
    void testManifold_Symmetry() {

        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(1, 1),
            new Vector2(3, 1),
            new Vector2(3, 3),
            new Vector2(1, 3)
        };

        AxisResult res1 = findAxisLeastPenetration(A, B);
        AxisResult res2 = findAxisLeastPenetration(B, A);

        Vector2[] m1 = determineManifold(A, B, res1.normal, res1);
        Vector2[] m2 = determineManifold(B, A, res2.normal, res2);

        assertNotNull(m1);
        assertNotNull(m2);

        assertEquals(m1.length, m2.length);
    }

    @Test
    void testManifold_RotatedSquare() {

        Vector2[] A = {
            new Vector2(-1, 0),
            new Vector2(0, 1),
            new Vector2(1, 0),
            new Vector2(0, -1)
        };

        Vector2[] B = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        AxisResult res = findAxisLeastPenetration(A, B);

        Vector2[] contacts = determineManifold(A, B, res.normal, res);

        assertNotNull(contacts);
        assertTrue(contacts.length >= 1);
    }

    @Test
    public void testSimpleOverlap() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(1, 1),
            new Vector2(3, 1),
            new Vector2(3, 3),
            new Vector2(1, 3)
        };

        AxisResult aTb = findAxisLeastPenetration(A, B);
        AxisResult bTa = findAxisLeastPenetration(B, A);

        assertTrue(aTb.normal.equals(new Vector2(0, 1)));
        assertTrue(bTa.normal.equals(new Vector2(0, -1)));

        assertNotNull(aTb);
        assertNotNull(bTa);

        Manifold m = collide(A, B);

        System.out.println(m);

        assertNotNull(m);
        assertTrue(m.penetration() > 0);
        assertTrue(m.contacts().length >= 1);
    }

    @Test
    public void testNoCollision() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(1, 0),
            new Vector2(1, 1),
            new Vector2(0, 1)
        };

        Vector2[] B = {
            new Vector2(3, 3),
            new Vector2(4, 3),
            new Vector2(4, 4),
            new Vector2(3, 4)
        };

        Manifold m = collide(A, B);

        assertNull(m);
    }

    @Test
    public void testEdgeTouching() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(2, 0),
            new Vector2(4, 0),
            new Vector2(4, 2),
            new Vector2(2, 2)
        };

        Manifold m = collide(A, B);

        assertNotNull(m);
        assertEquals(0, m.penetration(), 1e-6); // touching
        assertTrue(m.contacts().length >= 1);
    }

    @Test
    public void testCornerTouching() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(2, 2),
            new Vector2(3, 2),
            new Vector2(3, 3),
            new Vector2(2, 3)
        };

        AxisResult aTb = findAxisLeastPenetration(A, B);
        AxisResult bTa = findAxisLeastPenetration(B, A);

        assertNotNull(aTb);
        assertNotNull(bTa);

        Manifold m = collide(A, B);

        assertNotNull(m);
        assertTrue(m.contacts().length >= 1);
    }

    @Test
    public void testNormalDirection() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(1, 0),
            new Vector2(3, 0),
            new Vector2(3, 2),
            new Vector2(1, 2)
        };

        AxisResult aTb = findAxisLeastPenetration(A, B);
        AxisResult bTa = findAxisLeastPenetration(B, A);

        assertNotNull(aTb);
        assertNotNull(bTa);

        Manifold m = collide(A, B);

        assertNotNull(m);

        Vector2 centerA = computeCenter(A);
        Vector2 centerB = computeCenter(B);

        Vector2 dir = Vector2.subtract(centerB, centerA);

        // normal should point from A → B
        assertTrue(Vector2.dot(m.normal(), dir) > 0);
    }

    @Test
    public void testIncidentEdgeSelection() {
        Vector2[] square = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2 normal = new Vector2(1, 0); // right-facing

        Vector2[] edge = findIncidentEdge(square, normal);

        assertNotNull(edge);
        assertEquals(2, edge.length);
    }

    @Test
    public void testClipAlwaysReturnsMax2Points() {
        Vector2 a = new Vector2(0, 0);
        Vector2 b = new Vector2(2, 0);

        Vector2 normal = new Vector2(1, 0);
        double offset = 1;

        Vector2[] result = clip(a, b, normal, offset);

        assertTrue(result.length <= 2);
    }

    @Test
    public void testSymmetry() {
        Vector2[] A = {
            new Vector2(0, 0),
            new Vector2(2, 0),
            new Vector2(2, 2),
            new Vector2(0, 2)
        };

        Vector2[] B = {
            new Vector2(1, 1),
            new Vector2(3, 1),
            new Vector2(3, 3),
            new Vector2(1, 3)
        };

        AxisResult aTb = findAxisLeastPenetration(A, B);
        AxisResult bTa = findAxisLeastPenetration(B, A);

        assertNotNull(aTb);
        assertNotNull(bTa);

        Manifold m1 = collide(A, B);
        Manifold m2 = collide(B, A);

        assertNotNull(m1);
        assertNotNull(m2);

        // penetration should match
        assertEquals(m1.penetration(), m2.penetration(), 1e-6);

        // normals should be opposite
        assertEquals(
            Vector2.dot(m1.normal(), m2.normal()),
            -1,
            1e-6
        );
    }
}

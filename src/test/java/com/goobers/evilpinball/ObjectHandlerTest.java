package com.goobers.evilpinball;

//import static com.goobers.evilpinball.ObjectHandler.collide;
import static com.goobers.evilpinball.ObjectHandler.clip;
import static com.goobers.evilpinball.ObjectHandler.collide;
import static com.goobers.evilpinball.ObjectHandler.computeCenter;
import static com.goobers.evilpinball.ObjectHandler.findAxisLeastPenetration;
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

    private boolean edgeMatches(Vector2[] expected, Vector2[] actual) {
        return (
            (expected[0].distanceSq(actual[0]) < 1e-6 &&
            expected[1].distanceSq(actual[1]) < 1e-6)
        ||
            (expected[0].distanceSq(actual[1]) < 1e-6 &&
            expected[1].distanceSq(actual[0]) < 1e-6)
        );
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

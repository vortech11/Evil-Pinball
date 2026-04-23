package com.goobers.evilpinball;

public class Paddle extends PhysicsObj {

    public Paddle(Vector2 position) {
        super(
            position,
            new Vector2[] {
                    new Vector2(-50, -50),
                    new Vector2(150, 0),
                    new Vector2(-50, 50)
            },
            1.1,
            new Vector2(0, 0),
            0,
            new int[] {255, 255, 255},
            1,
            1,
            calculateMomentOfInertia(1, 200, 50) // mass=1, width=150, height=50
        );
    }

    //some stupid aproximation
    private static double calculateMomentOfInertia(double mass, double width, double height) {
        return (1.0/12.0) * mass * (width * width + height * height);
    }
}

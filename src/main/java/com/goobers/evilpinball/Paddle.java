package com.goobers.evilpinball;

import java.awt.Color;

public class Paddle extends PhysicsObj {

    public Paddle(Vector2 position) {
        super(
                position,
                new Vector2[] {
                        new Vector2(0, 0),
                        new Vector2(150, 25),
                        new Vector2(0, 50)
                },
                0,
                new Vector2(0, 0),
                0,
                new Color(255, 255, 255),
                1);

    }
}

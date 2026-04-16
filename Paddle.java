import java.awt.Color;

public class Paddle extends PhysicsObj {

    public Paddle(Vector2 possition){
        Vector2[] verts = {
            new Vector2(0, 0),
            new Vector2(150, 25),
            new Vector2(0, 50)
        };
        Color color = new Color(255, 255, 255);

        super(possition, verts, 0, new Vector2(0, 0), 0, color, 1);

    }
}

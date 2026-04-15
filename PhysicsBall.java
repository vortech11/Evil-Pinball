import java.awt.Color;
import java.awt.Graphics2D;
//import java.awt.geom.*;

public class PhysicsBall {
    Vector2 possition = new Vector2(0, 0);
    Vector2 velocity = new Vector2(0, 0);
    double size = 50;

    Color color = new Color(0);

    public PhysicsBall(Vector2 position, double size, Color color) {
        this.possition.copy(position);
        this.size = size;
        this.color = color;
    }

    public PhysicsBall(double x, double y, double size, Color color) {
        possition.x = x;
        possition.y = y;
        this.size = size;
        this.color = color;
    }

    public void update(double dt){
        velocity.y += 10000 * dt;
        possition.add(velocity);
    }

    public void render(Graphics2D frame, Camera camera){
        frame.setColor(color);
        Vector2 renderPosition = camera.transformPoint(possition);
        frame.fillOval((int) renderPosition.x, (int) renderPosition.y, (int) (size / camera.zoom), (int) size);
    }
}

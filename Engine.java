import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

import java.util.ArrayList;

public class Engine extends Frame implements Runnable{
   private Thread gameThread;

   boolean running = false;

   private final int FPS = 30;
   private final long TARGET_TIME = 1000 / FPS;

   Vector2 WINDOWSIZE = new Vector2(400, 400);

   double dt = 0;

   ArrayList<PhysicsBall> balls = new ArrayList<PhysicsBall>();

   Camera camera = new Camera(new Vector2(0, 0), 1, WINDOWSIZE);

   BufferStrategy bs;

   //PhysicsBall ball = new PhysicsBall(50.0, 50.0, 40.0);
       
   public Engine(){
      super("AWT Game");
      prepareGUI();
      //prepareGUI();
   }

   public void start(){
      if (!running){
         running = true;
         gameThread = new Thread(this);
         gameThread.start();
      }
   }

   private void prepareGUI(){
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      WINDOWSIZE.x = screenSize.width;
      WINDOWSIZE.y = screenSize.height;
      setSize((int) WINDOWSIZE.x, (int) WINDOWSIZE.y);
      //setBackground(new Color(0, 0, 0));
      this.setVisible(true);
      this.createBufferStrategy(2);
      bs = this.getBufferStrategy();

      addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            // Use getX() and getY() to get click location
            //System.out.println("Mouse Clicked at: " + x + ", " + y);
            Vector2 clickPosition = new Vector2(e.getX(), e.getY());
            //System.out.println(clickPosition);
            balls.add(new PhysicsBall(camera.unTransformPoint(clickPosition), 40, new Color(0)));
         }
      });

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent){
            System.exit(0);
         }
      });
   }

   @Override
   public void run(){
      long startTime;
      long timeMillis;
      long waitTime;
      while (running){
         startTime = System.nanoTime();
         //update(getGraphics());
         render(bs.getDrawGraphics());
         update(dt);
         //paint(getGraphics());
         //System.out.println("hello world");
         
         timeMillis = (System.nanoTime() - startTime) / 1000000;
         waitTime = TARGET_TIME - timeMillis;
         dt = ((double) waitTime) / 1000000;
         
         try {
            if (waitTime > 0) Thread.sleep(waitTime);
         } catch (Exception e) {
            e.printStackTrace();
         }

      }
   }

   public void update(double dt){
      for (PhysicsBall ball : balls){
         ball.update(dt);
      }
   }

   public void render(Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      WINDOWSIZE.x = screenSize.width;
      WINDOWSIZE.y = screenSize.height;

      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, (int) WINDOWSIZE.x, (int) WINDOWSIZE.y);
      //Font font = new Font("Serif", Font.PLAIN, 24);
      //g2.setFont(font);
      //g2.drawString("Hello world", 50, 70);
      //ball.render(g2);
      for (PhysicsBall ball : balls){
         ball.render(g2, camera);
      }

      g2.dispose();
      bs.show();

   }
}

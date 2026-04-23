package com.goobers.evilpinball;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

public class Engine extends Frame{
   boolean running = true;

   private final int FPS = 30;
   private final long TARGET_TIME = 1000 / FPS;

   Vector2 WINDOWSIZE = new Vector2(400, 400);

   double dt = 0;

   //Paddle myPaddle = new Paddle(new Vector2(0, 0));

   Camera camera = new Camera(new Vector2(0, 0), 1, WINDOWSIZE);

   ObjectHandler objHandlr;

   //Level myLevel;

   BufferStrategy bs;

   public Engine(){
      super("AWT Game");
      prepareGUI();
      //prepareGUI();
   }

   private void prepareGUI(){
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      WINDOWSIZE.x = screenSize.width;
      WINDOWSIZE.y = screenSize.height;
      setSize((int) WINDOWSIZE.x, (int) WINDOWSIZE.y);
      setBackground(new Color(0, 0, 0));
      this.setVisible(true);
      this.createBufferStrategy(2);
      bs = this.getBufferStrategy();

      addMouseListener(new MouseAdapter() {
         @Override
         public void mouseClicked(MouseEvent e) {
            // Use getX() and getY() to get click location
            //System.out.println("Mouse Clicked at: " + x + ", " + y);
            Vector2 clickPosition = new Vector2(e.getX(), e.getY());
            clickPosition = camera.unTransformPoint(clickPosition);
            objHandlr.newBall(clickPosition);
         }
      });

      addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent){
            System.exit(0);
         }
      });
   }

   public void start(){
      long startTime;
      long timeMillis;
      long waitTime;

      Level myLevel = LevelLoader.loadLevel("src/main/resources/level1.json");
      objHandlr = new ObjectHandler(this, myLevel);
      //System.out.println(myLevel);

      Paddle moving = new Paddle(new Vector2(0, -200));
      Paddle other = new Paddle(new Vector2(0, -500));
      other.velocity = new Vector2(0, 200000);
      objHandlr.newPhysObj(moving);
      objHandlr.newPhysObj(other);
      //objHandlr.newPhysObj(new Paddle(new Vector2(0, 0)));

      while (running){
         startTime = System.nanoTime();
         render(bs.getDrawGraphics());
         objHandlr.update(dt);
         
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

   /*
   public void update(double dt){
      for (PhysicsBall ball : balls){
         ball.update(dt, myPaddle);
      }
   }
   */

   public void render(Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      WINDOWSIZE.x = screenSize.width;
      WINDOWSIZE.y = screenSize.height;

      g2.setColor(Color.BLACK);
      g2.fillRect(0, 0, (int) WINDOWSIZE.x, (int) WINDOWSIZE.y);

      objHandlr.render(g2, camera);

      //Font font = new Font("Serif", Font.PLAIN, 24);
      //g2.setFont(font);
      //g2.drawString("Hello world", 50, 70);

      g2.dispose();
      bs.show();

   }
}

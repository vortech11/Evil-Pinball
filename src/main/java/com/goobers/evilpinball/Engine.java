package com.goobers.evilpinball;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

import java.util.ArrayList;

public class Engine extends Frame{
   boolean running = true;

   private final int FPS = 30;
   private final long TARGET_TIME = 1000 / FPS;

   Vector2 WINDOWSIZE = new Vector2(400, 400);

   double dt = 0;

   ArrayList<PhysicsBall> balls = new ArrayList<PhysicsBall>();

   Paddle myPaddle = new Paddle(new Vector2(0, 0));

   Camera camera = new Camera(new Vector2(0, 0), 1, WINDOWSIZE);

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
            //System.out.println(clickPosition);
            balls.add(
               new PhysicsBall(
                  camera.unTransformPoint(clickPosition), 
                  20, 
                  new Color(255, 255, 255)
               )
            );
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
      System.out.println(myLevel);

      while (running){
         startTime = System.nanoTime();
         render(bs.getDrawGraphics());
         update(dt);
         
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
         ball.update(dt, myPaddle);
      }
   }

   public void render(Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      WINDOWSIZE.x = screenSize.width;
      WINDOWSIZE.y = screenSize.height;

      g2.setColor(Color.BLACK);
      g2.fillRect(0, 0, (int) WINDOWSIZE.x, (int) WINDOWSIZE.y);
      //Font font = new Font("Serif", Font.PLAIN, 24);
      //g2.setFont(font);
      //g2.drawString("Hello world", 50, 70);
      for (int i = 0; i < balls.size(); i++){
         balls.get(i).render(g2, camera);
      }

      myPaddle.render(g2, camera);

      g2.dispose();
      bs.show();

   }
}

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.*;

public class Main {
    // Window stuff
    static JFrame gameWindow;
    static GraphicsPanel canvas;
    static final int FRAME_WIDTH = 800;
    static final int FRAME_HEIGHT = 600;
    static final int FRAME_PERIOD = 20;  // how often to update in ms
    static GameKeyListener keyListener = new GameKeyListener();

    static boolean inMenu = true; // start in menu screen
    static boolean gameOver = false;
    static boolean gameEnded = false;
    static long startTime;
    static long endTime;

    // Background images and positions for scrolling
    static BufferedImage background1;
    static int background1Y = 0;
    static BufferedImage background2;
    static int background2Y = -FRAME_HEIGHT;
    static int backgroundStep = 1;  // speed of background scrolling

    // Images for cannon and aliens
    static BufferedImage ORIGINALcannon;
    static BufferedImage cannon;
    static BufferedImage sub1;
    static BufferedImage sub2;
    static BufferedImage titleImage;
    static BufferedImage titleImage2;
    static int cannonX = FRAME_WIDTH / 2;
    static int cannonY = FRAME_HEIGHT - 100;

    static BufferedImage alienImage;

    // Bullet position, movement and size
    static double bulletX;
    static double bulletY;
    static double bulletDx;
    static double bulletDy;
    static boolean bulletVisible = false;
    static int bulletTarget = -1;
    static int bulletW = 6;
    static int bulletH = 10;
    static Rectangle bulletBox = new Rectangle(0, 0, bulletW, bulletH);

    // Aliens info
    static final int numAliens = 5;
    static int[] alienX = new int[numAliens];
    static double[] alienY = new double[numAliens];
    static String[] alienWords = new String[numAliens];
    static boolean[] alienAlive = new boolean[numAliens];
    static Rectangle[] alienRects = new Rectangle[numAliens];

    // Word list aliens can have
    static String[] wordPool = {
            "apple", "banana", "grape", "kiwi", "lemon", "peach", "melon", "alien", "random",
            "absolutely", "homogenous", "heterogeneous", "avogadro", "biology", "chemistry", "physics",
            "computer", "science", "technology", "engineering", "mathematics", "language", "history",
            "geography", "culture", "universe", "galaxy", "nebula", "asteroid", "comet", "rocket",
            "spaceship", "planet", "moon", "satellite", "orbit", "gravity", "equation", "formula",
            "solution", "reaction", "atom", "molecule", "electron", "proton", "neutron", "energy",
            "velocity", "momentum"};
    static Random rand = new Random();

    static String typedWord = "";  // what player types
    static double alienFallSpeed = 0.2;  // speed aliens fall
    static int updateCounter = 0;

    // For background music
    static AudioInputStream audioStream;
    static Clip bg;

    public static void main(String[] args) {
        gameWindow = new JFrame("Typing Shooter Game");
        gameWindow.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        canvas = new GraphicsPanel();
        canvas.addKeyListener(keyListener);
        gameWindow.add(canvas);

        // Load images from files
        try {
            background1 = ImageIO.read(new File("/Users/mac/Desktop/galaxy1.jpg"));
            background2 = ImageIO.read(new File("/Users/mac/Desktop/galaxy1.jpg"));
            ORIGINALcannon = ImageIO.read(new File("/Users/mac/Desktop/cannon.png"));
            cannon = ImageIO.read(new File("/Users/mac/Desktop/cannon.png"));
            alienImage = ImageIO.read(new File("/Users/mac/Desktop/Alien.png"));
            titleImage = ImageIO.read(new File("/Users/mac/Desktop/Title.png"));
            titleImage2 = ImageIO.read(new File("/Users/mac/Desktop/ti.png"));
            sub1 = ImageIO.read(new File("/Users/mac/Desktop/sub1.png"));
            sub2 = ImageIO.read(new File("/Users/mac/Desktop/sub2.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Load and start background music
        try {
            File audioFile = new File("/Users/mac/Desktop/space-ambient-351305.wav");
            audioStream = AudioSystem.getAudioInputStream(audioFile);
            bg = AudioSystem.getClip();
            bg.open(audioStream);
            bg.addLineListener(new CowListener());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        spawnAliens(); // start aliens
        gameWindow.setVisible(true);
        startTime = System.currentTimeMillis();
        runGameLoop();  // start main game loop
    }

    // Set up aliens with random words and positions
    public static void spawnAliens() {
        Font font = new Font("Arial", Font.BOLD, 20);
        canvas.setFont(font);
        FontMetrics fm = canvas.getFontMetrics(font);

        int alienW     = 100;
        int alienH     = 80;
        int wordHeight = 25;

        for (int i = 0; i < numAliens; i++) {
            String word = wordPool[rand.nextInt(wordPool.length)];
            int wordW   = fm.stringWidth(word);
            int totalW  = Math.max(alienW, wordW);
            int totalH  = alienH + wordHeight;

            int x = rand.nextInt(FRAME_WIDTH  - totalW);
            int y = rand.nextInt(FRAME_HEIGHT/2 - totalH) + totalH;

            alienX[i]     = x;
            alienY[i]     = y;
            alienWords[i] = word;
            alienAlive[i] = true;
            alienRects[i] = new Rectangle(x, y - wordHeight, totalW, totalH);
        }
    }

    // The main game loop, runs forever
    public static void runGameLoop() {
        while (true) {
            gameWindow.repaint();  // redraw everything
            try {
                Thread.sleep(FRAME_PERIOD); // pause for a bit
            } catch (Exception e) {}

            // Start or loop music
            if (bg != null) {
                bg.loop(Clip.LOOP_CONTINUOUSLY);
            }

            // scroll backgrounds down
            background1Y += backgroundStep;
            if (background1Y >= FRAME_HEIGHT) background1Y = -FRAME_HEIGHT;
            background2Y += backgroundStep;
            if (background2Y >= FRAME_HEIGHT) background2Y = -FRAME_HEIGHT;

            if (!inMenu && !gameOver) {
                // speed up aliens slowly
                if (++updateCounter % 200 == 0 && alienFallSpeed < 5.0) {
                    alienFallSpeed += 0.05;
                }

                // move aliens down
                for (int i = 0; i < numAliens; i++) {
                    if (!alienAlive[i]) continue;
                    alienY[i] += alienFallSpeed;
                    alienRects[i].setLocation(alienX[i], (int)alienY[i]);
                    // if alien reaches bottom, game ends
                    if (alienY[i] + 30 >= FRAME_HEIGHT) {
                        gameOver = gameEnded = true;
                        endTime = System.currentTimeMillis();
                    }
                }

                // move bullet if visible
                if (bulletVisible) {
                    bulletX += bulletDx;
                    bulletY += bulletDy;
                    bulletBox.setLocation((int)bulletX, (int)bulletY);

                    // check if bullet hits target alien
                    if (bulletTarget >= 0 &&
                            alienAlive[bulletTarget] &&
                            bulletBox.intersects(alienRects[bulletTarget])) {

                        alienAlive[bulletTarget] = false;
                        bulletVisible = false;

                        // respawn alien with new word and position
                        alienWords[bulletTarget] = wordPool[rand.nextInt(wordPool.length)];
                        alienX[bulletTarget]     = rand.nextInt(FRAME_WIDTH - 100) + 50;
                        alienY[bulletTarget]     = rand.nextInt(100) + 50;
                        alienRects[bulletTarget].setLocation(alienX[bulletTarget], (int)alienY[bulletTarget]);
                        alienAlive[bulletTarget] = true;
                    }
                }
            }
        }
    }

    // restart game variables and aliens
    public static void restartGame() {
        gameOver = gameEnded = false;
        typedWord = "";
        alienFallSpeed = 0.2;
        updateCounter = 0;
        startTime = System.currentTimeMillis();
        spawnAliens();
        inMenu = false;
    }

    // panel where everything is drawn
    static class GraphicsPanel extends JPanel {
        public GraphicsPanel() {
            setFocusable(true);
            requestFocusInWindow();
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // draw scrolling background
            g.drawImage(background1, 0, background1Y, FRAME_WIDTH, FRAME_HEIGHT, this);
            g.drawImage(background2, 0, background2Y, FRAME_WIDTH, FRAME_HEIGHT, this);

            // menu screen
            if (inMenu) {
                g.drawImage(titleImage, (FRAME_WIDTH-400)/2, 20, 400, 300, this);
                g.drawImage(sub1, (FRAME_WIDTH-400)/2, 200, 400, 400, this);
                return;
            }

            // game over screen
            if (gameEnded) {
                long elapsed = (endTime - startTime) / 1000;
                g.drawImage(titleImage2, (FRAME_WIDTH-400)/2, 20, 400, 300, this);
                g.drawImage(sub2, (FRAME_WIDTH-400)/2, 200, 400, 300, this);
                g.setFont(new Font("Courier New", Font.PLAIN, 32));
                g.setColor(Color.WHITE);
                String msg = "Time: " + elapsed + " seconds";
                int w = g.getFontMetrics().stringWidth(msg);
                g.drawString(msg, (FRAME_WIDTH - w)/2, FRAME_HEIGHT/2);
                return;
            }

            // draw cannon
            g.drawImage(cannon, cannonX, cannonY, 100, 100, this);

            // draw bullet if there is one
            if (bulletVisible) {
                g.setColor(Color.RED);
                g.fillOval((int)bulletX, (int)bulletY, bulletW, bulletH);
            }

            // draw aliens and their words
            g.setFont(new Font("Arial", Font.BOLD, 20));
            for (int i = 0; i < numAliens; i++) {
                if (alienAlive[i]) {
                    g.drawImage(alienImage, alienX[i], (int)alienY[i], 100, 80, this);
                    g.setColor(Color.WHITE);
                    int tw = g.getFontMetrics().stringWidth(alienWords[i]);
                    g.drawString(alienWords[i], alienX[i] + 50 - tw/2, (int)alienY[i] - 5);
                }
            }

            // draw what player typed and timer
            g.setColor(Color.CYAN);
            g.setFont(new Font("Courier New", Font.PLAIN, 20));
            g.drawString("Typed: " + typedWord, 20, FRAME_HEIGHT - 30);

            long live = (System.currentTimeMillis() - startTime) / 1000;
            g.drawString("Time: " + live + "s", FRAME_WIDTH - 130, FRAME_HEIGHT - 30);
        }
    }

    // rotate the cannon image to face bullet target
    public static BufferedImage rotateImage(BufferedImage image, int angle) {
        double rad = Math.toRadians(angle + 90);
        double cx = image.getWidth()/2.0;
        double cy = image.getHeight()/2.0;
        AffineTransform at = AffineTransform.getRotateInstance(rad, cx, cy);
        BufferedImage rotated = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        op.filter(image, rotated);
        return rotated;
    }

    // keyboard input handler
    static class GameKeyListener implements KeyListener {
        public void keyPressed(KeyEvent e) {
            // in menu, press S to start game
            if (inMenu) {
                if (e.getKeyCode() == KeyEvent.VK_S) {
                    restartGame();
                }
                return;
            }

            // after game over, press R to restart
            if (gameEnded && e.getKeyCode() == KeyEvent.VK_R) {
                restartGame();
                return;
            }

            // type letters to build typedWord
            char c = e.getKeyChar();
            if (Character.isLetter(c)) {
                typedWord += c;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && typedWord.length() > 0) {
                typedWord = typedWord.substring(0, typedWord.length() - 1);
            }

            // check if typedWord matches any alien word
            for (int i = 0; i < numAliens; i++) {
                if (alienAlive[i] && typedWord.equals(alienWords[i])) {
                    int tx = alienX[i] + 25;
                    int ty = (int)alienY[i] + 15;
                    double dx = tx - (cannonX + 50);
                    double dy = ty - (cannonY + 50);
                    double ang = Math.atan2(dy, dx);
                    int deg = (int)Math.toDegrees(ang);

                    // rotate cannon toward alien
                    cannon = rotateImage(ORIGINALcannon, deg);

                    // shoot bullet if no bullet on screen
                    if (!bulletVisible) {
                        bulletX = cannonX + 50;
                        bulletY = cannonY + 50;
                        bulletVisible = true;

                        double dist = Math.hypot(dx, dy);
                        bulletDx = 10 * dx / dist;
                        bulletDy = 10 * dy / dist;
                        bulletTarget = i;
                        typedWord = "";
                    }
                }
            }
        }

        public void keyReleased(KeyEvent e) {}
        public void keyTyped(KeyEvent e) {}
    }

    // restart music when it ends
    static class CowListener implements LineListener {
        public void update(LineEvent event) {
            if (event.getType() == LineEvent.Type.STOP && bg != null) {
                bg.flush();
                bg.setFramePosition(0);
            }
        }
    }
}

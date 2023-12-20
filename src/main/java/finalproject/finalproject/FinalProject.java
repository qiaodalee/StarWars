/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package finalproject.finalproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Queue;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 *
 * @author matt0
 */
public class FinalProject extends JFrame {
    private int windowHeight = 700;
    private int windowWidth = 980;
    private int mainHeight = 500;
    private int mainWidth = 980;
    private int bulletHeight = 40;
    private int bulletWidth = 130;
    private int planeSize = 100;
    private int scoreSize = 30;
    private Clip backendMusic, scoreMusic;
    
    private Queue<BulletPanel> Bullet = new LinkedList<>();
    private Queue<FirePanel> Fire = new LinkedList<>();
    private Timer BulletHandler;
    
    private JPanel mainPanel;
    private PlanePanel plane;
    
    private ScorePanel scorePanel;
    private int score;
    
    
    public FinalProject(){
        
        setup();
        
        newPlanePanel();
        updateScorePanel();

        setMainPanel();

        
        bulletHandle();
        BulletHandler.start();
        
        setMusic();
        playBackendMusic();

        mainPanel.add(plane, BorderLayout.WEST);
                    
        add(mainPanel, BorderLayout.CENTER);
        add(scorePanel, BorderLayout.NORTH);
    }
    
    public void setMusic(){
        try {
            // Load backend music
            URL musicURL = new File("./backend.wav").toURI().toURL();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicURL);
            backendMusic = AudioSystem.getClip();
            backendMusic.open(audioInputStream);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
        
        try {
            // Load backend music
            URL musicURL = new File("./gotit.wav").toURI().toURL();
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicURL);
            scoreMusic = AudioSystem.getClip();
            scoreMusic.open(audioInputStream);
        } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public void playBackendMusic() {
        if (backendMusic != null && !backendMusic.isRunning()) {
            backendMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBackendMusic() {
        if (backendMusic != null && backendMusic.isRunning()) {
            backendMusic.stop();
        }
    }
    
    public void playScoreMusic() {
        if (scoreMusic != null) {
            scoreMusic.setFramePosition(0);
            scoreMusic.start();
        }
    }
    
    public void setMainPanel(){
        mainPanel = new JPanel();
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(mainWidth, mainHeight));
    }
    
    public void bulletHandle(){
        BulletHandler = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newBulletPanel();
            }
        });
    }
    
    // Game over or stop
    public void stopAll(){
        for (BulletPanel bullet : Bullet) {
            bullet.bulletRemove();
        }
        BulletHandler.stop();
    }
    
    // normalization JFram
    public void setup(){
        setTitle("Cat and Mouse Game");
        setSize(windowWidth, windowHeight);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setVisible(true);
        addKeyListener(new MyKeyListener());
    }
    
    // on fire
    public void fire(){
        FirePanel firePanel = new FirePanel();
        firePanel.setLocation(plane.getX() + (planeSize / 2), plane.getY() + (planeSize / 2));
        firePanel.setSize(bulletWidth, bulletHeight);
        mainPanel.add(firePanel);
        firePanel.fireMove();
        Fire.offer(firePanel);
        System.out.println(firePanel.getBounds());
    }
    
    // Create a new enemy fire
    public void newBulletPanel(){
        BulletPanel bullet = new BulletPanel();
        bullet.setLocation(mainWidth, 0);
        Bullet.offer(bullet);
        bullet.bulletMove();
        mainPanel.add(bullet, BorderLayout.EAST); 
    }
    
    // Create a new plane
    public void newPlanePanel(){
        plane = new PlanePanel();
        plane.setPreferredSize(new Dimension(planeSize, planeSize));
    }
    
    // Create a new plane
    public void updateScorePanel(){
        scorePanel = new ScorePanel();
        scorePanel.setBackground(Color.WHITE);
        scorePanel.setPreferredSize(new Dimension(scoreSize, scoreSize));
        Timer timer = new Timer(10, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scorePanel.repaint();
            }
        });
        timer.start();
    }
    
    private class FirePanel extends JPanel {
        Timer fireTimer;
        int org = plane.getY() + (planeSize / 2);
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("./bullet.png"), 0, 0, bulletWidth, bulletHeight, this);
        }
        
        public void fireMove(){
            fireTimer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLocation(getX() + 5, org);
                    setSize(bulletWidth, bulletHeight);
                    
                    if (getX() > mainWidth + getWidth()){
                        fireRemove();
                    }
                    
                    
                    for (BulletPanel bullet : Bullet) {
                        if (bullet.getBounds().intersects(getBounds())){
                            stopBackendMusic();
                            playScoreMusic();
                            
                            mainPanel.remove(bullet);
                            fireRemove();
                            
                            getParent().remove(FirePanel.this);
                            bullet.bulletRemove();
                            
                            Bullet.remove(bullet);
                            score++;
                            
                            mainPanel.repaint();
                            playBackendMusic();
                        }
                    }
                }
            });
            fireTimer.start();
        }
        
        public void fireRemove(){
            fireTimer.stop();
        }

    }
    
    private class BulletPanel extends JPanel {
        Timer timer;
        int org = scoreSize + (int)(Math.random()*mainHeight);
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("./bullet.png"), bulletWidth, 0, -bulletWidth, bulletHeight, this);
        }
        
        public void bulletMove(){
            timer = new Timer(10, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLocation(getX() - 5, org);
                    setSize(bulletWidth, bulletHeight);
                    if (getX() < 0 - getWidth()){
                        bulletRemove();
                        Bullet.poll();
                    }
                    
                    if (plane.getBounds().intersects(getBounds() )){
                        System.out.println("lose");
                        stopAll();
                    }
                }
            });
            timer.start();
        }
        
        public void bulletRemove(){
            timer.stop();
        }

    }
    
    // Draw a plane
    private class PlanePanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            setBackground(Color.WHITE);
            setSize(planeSize, planeSize);
            g.drawImage(Toolkit.getDefaultToolkit().getImage("./plane.jpg"), 0, 0, planeSize, planeSize, this);
        }
        
        public void gotoLocate(int x, int y){
            this.setLocation(x, y);
            this.setSize(planeSize, planeSize);
            return;
        }
        
        public void planeMove( int x, int y){
            x = this.getX() + x;
            y = this.getY() + y;
            this.gotoLocate(x, y);
            
            this.setSize(planeSize, planeSize);
            
            return;
        }
    }
    
    // Draw score
    private class ScorePanel extends JPanel {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setFont(new Font("Arial", Font.BOLD, scoreSize));
            g.drawString(("score: "+Integer.toString(score)), scoreSize, scoreSize);
        }
    }
    
    private class MyKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
            // Invoked when a key is typed, not used in this example
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            int distance = 20;

            switch (keyCode) {
                case KeyEvent.VK_UP:
                    System.out.println("Up");
                    // Handle up key pressed event
                    if (plane.getY() - distance >= 0){
                        plane.planeMove(0, -distance);
                    }
                    else{
                        plane.gotoLocate(plane.getX(), 0);
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    System.out.println("Down");
                    // Handle down key pressed event
                    if (plane.getY() + distance < mainHeight + 2*scoreSize){
                        plane.planeMove(0, distance);
                    }
                    else{
                        plane.gotoLocate(plane.getX(), plane.getY());
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    System.out.println("Left");
                    // Handle left key pressed event
                    if (plane.getX() - distance >= 0){
                        plane.planeMove(-distance, 0);
                    }
                    else{
                        plane.gotoLocate(0, plane.getY());
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    System.out.println("Right");
                    // Handle right key pressed event
                    if (plane.getX() + distance < mainWidth - (2*planeSize)){
                        plane.planeMove(distance, 0);
                    }
                    else{
                        plane.gotoLocate(mainWidth - (2*planeSize), plane.getY());
                    }
                    break;
                case KeyEvent.VK_CONTROL:
                    System.out.println("CTRL");
                    fire();
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Invoked when a key is released, not used in this example
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                FinalProject app = new FinalProject();
            }
        });
    }
}

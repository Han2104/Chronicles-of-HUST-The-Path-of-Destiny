package com.hust.game.ui.panels;

import com.hust.game.ui.GameWindow;
import com.hust.game.maps.b1.B1Engine;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import com.hust.game.models.entities.B1Bullet;
import com.hust.game.maps.d9.D9QuestionBank;
import com.hust.game.util.AssetLoader;
import java.awt.image.BufferedImage;

public class B1SurvivalPanel extends JPanel implements ActionListener {

    private GameWindow window;
    private B1Engine engine;
    private Timer timer;
    private BufferedImage bossImage;
    
    private boolean up, down, left, right, space;
    
    public B1SurvivalPanel(ArenaPanel arenaPanel, GameWindow window, String bossName, int bossHp) {

        this.window = window;
        this.engine = new B1Engine(bossName, bossHp);
        this.bossImage = AssetLoader.loadImage("assets/boss.png");
        
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W: up = true; break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S: down = true; break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A: left = true; break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D: right = true; break;
                    case KeyEvent.VK_SPACE: space = true; break;
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_W: up = false; break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_S: down = false; break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_A: left = false; break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_D: right = false; break;
                    case KeyEvent.VK_SPACE: space = false; break;
                }
            }
        });
        
        this.timer = new Timer(16, this);
        this.timer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (engine.getCurrentState() == B1Engine.State.PLAYING) {
            double aimDX = 0;
            double aimDY = space ? -2.0 : 0.0;
            engine.update(up, down, left, right, aimDX, aimDY);
        } else if (engine.getCurrentState() == B1Engine.State.QUIZ_MODE) {
            timer.stop();
            showQuizDialog();
            timer.start();
        } else if (engine.getCurrentState() == B1Engine.State.GAME_OVER) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Game Over!");
            if (window != null) window.showPanel("Home");
        } else if (engine.getCurrentState() == B1Engine.State.VICTORY) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Victory!");
            if (window != null) window.showPanel("Home");
        }
        repaint();
    }
    
    private void showQuizDialog() {
        D9QuestionBank.Question q = engine.getCurrentQuizQuestion();
        if (q != null && q.options != null && q.options.length > 0) {
            int answer = JOptionPane.showOptionDialog(this, q.question, "Quiz Time!", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, q.options, q.options[0]);
            engine.answerQuiz(answer);
        } else {
            engine.answerQuiz(-1);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Player
        g.setColor(Color.BLUE);
        g.fillRect((int)engine.getPlayer().getX(), (int)engine.getPlayer().getY(), 30, 30);
        
        // Boss
        if (bossImage != null) {
            g.drawImage(bossImage, (int)engine.getBoss().getX(), (int)engine.getBoss().getY(), engine.getBoss().getWidth(), engine.getBoss().getHeight(), this);
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect((int)engine.getBoss().getX(), (int)engine.getBoss().getY(), engine.getBoss().getWidth(), engine.getBoss().getHeight());
        }
        
        // Bullets
        for (B1Bullet b : engine.getBullets()) {
            g.setColor(b.isPlayerBullet() ? Color.CYAN : Color.RED);
            g.fillOval((int)(b.getX() - b.getRadius()), (int)(b.getY() - b.getRadius()), b.getRadius()*2, b.getRadius()*2);
        }
        
        // UI
        g.setColor(Color.WHITE);
        g.drawString("Boss HP: " + engine.getBoss().getHp() + "/" + engine.getBoss().getMaxHp(), 10, 20);
        g.drawString("State: " + engine.getCurrentState(), 10, 40);
    }
}

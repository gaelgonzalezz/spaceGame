package juego;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class spaceGame extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_WIDTH = 50;
    private static final int PLAYER_HEIGHT = 50;
    private static final int ENEMY_WIDTH = 40;
    private static final int ENEMY_HEIGHT = 40;
    private static final int BULLET_WIDTH = 5;
    private static final int BULLET_HEIGHT = 10;

    private JPanel gamePanel;
    private Timer gameTimer;
    private Timer enemySpawnTimer;
    private int playerX = WIDTH / 2;
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Explosion> explosions = new ArrayList<>();
    private Random random = new Random();
    private int score = 0;
    private int level = 1;
    private int lives = 3;
    private boolean gameOver = false;
    private boolean canShoot = true;
    private long lastShootTime = 0;
    private final long SHOOT_COOLDOWN = 100;

    public spaceGame() {
        setTitle("Advanced Space Game");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);
            }
        };
        gamePanel.setBackground(Color.BLACK);
        add(gamePanel);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            playerX = Math.max(0, playerX - 20);
                            break;
                        case KeyEvent.VK_RIGHT:
                            playerX = Math.min(WIDTH - PLAYER_WIDTH, playerX + 20);
                            break;
                        case KeyEvent.VK_SPACE:
                            if (canShoot) {
                                fireBullet();
                                canShoot = false;
                                lastShootTime = System.currentTimeMillis();
                            }
                            break;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    restartGame();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    canShoot = true;
                }
            }
        });

        gameTimer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameOver) {
                    updateGame();
                    gamePanel.repaint();
                }
            }
        });
        gameTimer.start();

        // Nuevo Timer para el spawn de enemigos
        enemySpawnTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spawnEnemy();
            }
        });
        enemySpawnTimer.start();

        setFocusable(true);
    }

    private void drawGame(Graphics g) {
        drawStaticBackground(g);
        drawPlayer(g);
        drawEnemies(g);
        drawBullets(g);
        drawExplosions(g);
        drawHUD(g);

        if (gameOver) {
            drawGameOver(g);
        }
    }

    private void drawStaticBackground(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawPlayer(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRoundRect(playerX, HEIGHT - PLAYER_HEIGHT - 20, PLAYER_WIDTH, PLAYER_HEIGHT, 20, 20);
        g.setColor(Color.WHITE);
        g.fillRect(playerX + 20, HEIGHT - PLAYER_HEIGHT, 10, 10);
    }

    private void drawEnemies(Graphics g) {
        g.setColor(Color.ORANGE);
        for (Enemy enemy : enemies) {
            g.fillOval(enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT);
            g.setColor(Color.WHITE);
            g.fillRect(enemy.x + 10, enemy.y + 10, 5, 5);
            g.fillRect(enemy.x + 25, enemy.y + 10, 5, 5);
            g.fillRect(enemy.x + 17, enemy.y + 25, 6, 10);
        }
    }

    private void drawBullets(Graphics g) {
        g.setColor(Color.YELLOW);
        for (Bullet bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);
        }
    }

    private void drawExplosions(Graphics g) {
        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 25);
        g.drawString("Level: " + level, 10, 50);
        g.drawString("Lives: " + lives, WIDTH - 100, 25);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("GAME OVER", WIDTH / 2 - 100, HEIGHT / 2);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Press ENTER to restart", WIDTH / 2 - 100, HEIGHT / 2 + 40);
    }

    private void updateGame() {
        moveEnemies();
        updateBullets();
        updateExplosions();
        checkCollisions();
        checkLevelComplete();
    }

    private void fireBullet() {
        bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - BULLET_WIDTH / 2, HEIGHT - PLAYER_HEIGHT - 20));
    }

    private void spawnEnemy() {
        int x = random.nextInt(WIDTH - ENEMY_WIDTH);
        int y = -ENEMY_HEIGHT;  // Aparecen fuera de la pantalla en la parte superior
        enemies.add(new Enemy(x, y));
    }

    private void moveEnemies() {
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.move();
            if (enemy.y > HEIGHT) {
                enemyIterator.remove();
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                    enemySpawnTimer.stop();  // Detiene el spawn de enemigos
                }
            }
        }
    }

    private void updateBullets() {
        Iterator<Bullet> bulletIterator = bullets.iterator();
        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.move();
            if (bullet.y < 0) {
                bulletIterator.remove();
            }
        }
    }

    private void updateExplosions() {
        Iterator<Explosion> explosionIterator = explosions.iterator();
        while (explosionIterator.hasNext()) {
            Explosion explosion = explosionIterator.next();
            explosion.update();
            if (explosion.isFinished()) {
                explosionIterator.remove();
            }
        }
    }

    private void checkCollisions() {
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            Iterator<Bullet> bulletIterator = bullets.iterator();
            while (bulletIterator.hasNext()) {
                Bullet bullet = bulletIterator.next();
                if (bullet.x < enemy.x + ENEMY_WIDTH &&
                    bullet.x + BULLET_WIDTH > enemy.x &&
                    bullet.y < enemy.y + ENEMY_HEIGHT &&
                    bullet.y + BULLET_HEIGHT > enemy.y) {
                    enemyIterator.remove();
                    bulletIterator.remove();
                    explosions.add(new Explosion(enemy.x, enemy.y));
                    score += 10;
                    break;
                }
            }
        }
    }

    private void checkLevelComplete() {
        if (enemies.isEmpty()) {
            level++;
            spawnEnemy();  // Inicia el siguiente nivel con un nuevo enemigo
        }
    }

    private void restartGame() {
        score = 0;
        level = 1;
        lives = 3;
        gameOver = false;
        canShoot = true;
        enemies.clear();
        bullets.clear();
        explosions.clear();
        playerX = WIDTH / 2;
        enemySpawnTimer.start();  // Reinicia el spawn de enemigos
        spawnEnemy();
    }

    private class Enemy {
        int x, y;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void move() {
            y += 2 + level * 0.5;  // Movimiento hacia abajo y aumento gradual de velocidad
        }
    }

    private class Bullet {
        int x, y;

        Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void move() {
            y -= 10;
        }
    }

    private class Explosion {
        int x, y;
        int size;
        int maxSize = 30;
        int growthRate = 2;

        Explosion(int x, int y) {
            this.x = x;
            this.y = y;
            this.size = 0;
        }

        void update() {
            size += growthRate;
        }

        void draw(Graphics g) {
            g.setColor(new Color(255, 200, 0, 255 - (size * 255 / maxSize)));
            g.fillOval(x - size / 2, y - size / 2, size, size);
        }

        boolean isFinished() {
            return size >= maxSize;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new spaceGame().setVisible(true);
            }
        });
    }
}

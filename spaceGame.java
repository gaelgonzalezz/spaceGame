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
    private Timer timer;
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
    private final long SHOOT_COOLDOWN = 200; // Tiempo de recarga en milisegundos

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
                            playerX = Math.max(0, playerX - 10);
                            break;
                        case KeyEvent.VK_RIGHT:
                            playerX = Math.min(WIDTH - PLAYER_WIDTH, playerX + 10);
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

        timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!gameOver) {
                    updateGame();
                    gamePanel.repaint();
                }
            }
        });
        timer.start();

        setFocusable(true);
        spawnEnemies();
    }

    private void drawGame(Graphics g) {
        // Dibujar fondo estático
        drawStaticBackground(g);

        // Dibujar jugador
        drawPlayer(g);

        // Dibujar enemigos
        drawEnemies(g);

        // Dibujar balas
        drawBullets(g);

        // Dibujar explosiones
        drawExplosions(g);

        // Dibujar puntuación, nivel y vidas
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
        // Dibujar nave del jugador
        g.setColor(Color.GREEN);
        g.fillRect(playerX, HEIGHT - PLAYER_HEIGHT - 20, PLAYER_WIDTH, PLAYER_HEIGHT);

        // Agregar diseño a la nave del jugador
        g.setColor(Color.WHITE);
        g.drawOval(playerX + 5, HEIGHT - PLAYER_HEIGHT - 15, 10, 10); // Luces
        g.drawLine(playerX + 10, HEIGHT - PLAYER_HEIGHT - 20, playerX + 10, HEIGHT - PLAYER_HEIGHT); // Cuerpo
        g.drawLine(playerX, HEIGHT - PLAYER_HEIGHT - 10, playerX + PLAYER_WIDTH, HEIGHT - PLAYER_HEIGHT - 10); // Alas
    }

    private void drawEnemies(Graphics g) {
        // Dibujar enemigos
        g.setColor(Color.RED);
        for (Enemy enemy : enemies) {
            // Dibujar diseño de los enemigos
            g.fillRect(enemy.x, enemy.y, ENEMY_WIDTH, ENEMY_HEIGHT);
            g.setColor(Color.WHITE);
            g.drawOval(enemy.x + 5, enemy.y + 5, 10, 10); // Ojos
            g.drawLine(enemy.x + 10, enemy.y, enemy.x + 10, enemy.y + ENEMY_HEIGHT); // Cuerpo
        }
    }

    private void drawBullets(Graphics g) {
        // Dibujar balas
        g.setColor(Color.YELLOW);
        for (Bullet bullet : bullets) {
            g.fillRect(bullet.x, bullet.y, BULLET_WIDTH, BULLET_HEIGHT);
        }
    }

    private void drawExplosions(Graphics g) {
        // Dibujar explosiones
        for (Explosion explosion : explosions) {
            explosion.draw(g);
        }
    }

    private void drawHUD(Graphics g) {
        // Dibujar puntuación, nivel y vidas
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
        // Mover enemigos
        moveEnemies();

        // Mover balas
        updateBullets();

        // Actualizar explosiones
        updateExplosions();

        // Verificar colisiones
        checkCollisions();

        // Comprobar si se ha completado el nivel
        checkLevelComplete();
    }

    private void fireBullet() {
        bullets.add(new Bullet(playerX + PLAYER_WIDTH / 2 - BULLET_WIDTH / 2, HEIGHT - PLAYER_HEIGHT - 20));
    }

    private void spawnEnemies() {
        for (int i = 0; i < 10 + level; i++) {
            for (int j = 0; j < 4; j++) {
                enemies.add(new Enemy(50 + i * 60, 50 + j * 60));
            }
        }
    }

    private void moveEnemies() {
        for (Enemy enemy : enemies) {
            enemy.move();
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

            // Comprobar si el enemigo ha alcanzado la parte inferior
            if (enemy.y + ENEMY_HEIGHT > HEIGHT - PLAYER_HEIGHT - 10) {
                enemyIterator.remove();
                lives--;
                if (lives <= 0) {
                    gameOver = true;
                }
            }
        }
    }

    private void checkLevelComplete() {
        if (enemies.isEmpty()) {
            level++;
            spawnEnemies();
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
        spawnEnemies();
    }

    private class Enemy {
        int x, y;
        int direction = 1;

        Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void move() {
            x += direction * (1 + level / 1.5);
            if (x <= 0 || x >= WIDTH - ENEMY_WIDTH) {
                direction *= -1;
                y += 10;
            }
        }
    }

    private class Bullet {
        int x, y;

        Bullet(int x, int y) {
            this.x = x;
            this.y = y;
        }

        void move() {
            y -= 7;
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
            g.fillOval(x - size/2, y - size/2, size, size);
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
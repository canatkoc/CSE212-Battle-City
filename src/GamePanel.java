import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GamePanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;
	private static final int TILE_SIZE = 40;
	private static final int PANEL_WIDTH = 640;
	private static final int PANEL_HEIGHT = 600;

	private static final int EAGLE_COL = 7;
	private static final int EAGLE_ROW = 14;

	private static final int[] SPAWN_X = {0, 320, 600};
	private int maxOnScreen = 4;

	private static final int GAMEOVER_SPRITE_W = 256;
	private static final int GAMEOVER_SPRITE_H = 64;
	private static final int GAMEOVER_TARGET_Y = (PANEL_HEIGHT - GAMEOVER_SPRITE_H * 2) / 2;

	private PlayerTank playerTank;
	private Eagle eagle;
	private ArrayList<Obstacle> obstacles;
	private ArrayList<Bullet> bullets;
	private ArrayList<EnemyTank> enemies;
	private ArrayList<Bullet> enemyBullets;
	private HUDPanel hud;

	private Thread gameThread;
	private boolean isRunning;
	private boolean isPaused = false;
	private boolean gameOver = false;
	private boolean stageClear = false;

	private int playerDir = 0;
	private int fireCooldown = 0;
	private int playerLives = 3;
	private int playerInvincibility = 0;
	private int score = 0;

	private int totalEnemies = 20;
	private int difficulty = 1;
	private int spawnedEnemies = 0;
	private int spawnCooldown = 120;

	private boolean upPressed = false;
	private boolean downPressed = false;
	private boolean leftPressed = false;
	private boolean rightPressed = false;
	private boolean shootPressed = false;

	private int introTimer = 0;
	private int currentLevel = 1;
	private int gameOverSlide = -1;
	private int gameOverDelay = 0;
	private int stageClearDelay = -1;
	private boolean nameAsked = false;
	private boolean waitingForNextLevel = false;
	private int infiniteRound = 0;

	private ArrayList<int[]> scorePopups;

	private ArrayList<PowerUp> powerUps;
	private static final int CLOCK_DURATION = 300;
	private static final int SHOVEL_DURATION = 600;
	private int clockTimer = 0;
	private int shovelTimer = 0;
	private int[][] savedEagleArea = null;

	private BufferedImage imgPlayerUp;
	private BufferedImage imgPlayerDown;
	private BufferedImage imgPlayerLeft;
	private BufferedImage imgPlayerRight;
	private BufferedImage imgEnemyUp;
	private BufferedImage imgEnemyDown;
	private BufferedImage imgEnemyLeft;
	private BufferedImage imgEnemyRight;
	private BufferedImage imgBrick;
	private BufferedImage imgSteel;
	private BufferedImage imgWater;
	private BufferedImage imgBush;
	private BufferedImage imgEagle;
	private BufferedImage imgEagleDestroyed;
	private BufferedImage imgTextGame;
	private BufferedImage imgTextOver;

	private int[][] map = {
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0},
		{0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0},
		{0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0},
		{0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1},
		{0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1},
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0},
		{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0},
		{0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0},
		{0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
	};

	public GamePanel(HUDPanel hud) {
		// TODO Auto-generated constructor stub
		this.hud = hud;

		setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
		setBackground(Color.BLACK);
		setFocusable(true);

		loadSprites();

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_W) {
					upPressed = true;
				}
				if(key == KeyEvent.VK_S) {
					downPressed = true;
				}
				if(key == KeyEvent.VK_A) {
					leftPressed = true;
				}
				if(key == KeyEvent.VK_D) {
					rightPressed = true;
				}
				if(key == KeyEvent.VK_UP) {
					upPressed = true;
				}
				if(key == KeyEvent.VK_DOWN) {
					downPressed = true;
				}
				if(key == KeyEvent.VK_LEFT) {
					leftPressed = true;
				}
				if(key == KeyEvent.VK_RIGHT) {
					rightPressed = true;
				}
				if(key == KeyEvent.VK_SPACE) {
					if(waitingForNextLevel) {
						waitingForNextLevel = false;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								advanceToNextLevel();
							}
						});
					} else {
						shootPressed = true;
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_W) {
					upPressed = false;
				}
				if(key == KeyEvent.VK_S) {
					downPressed = false;
				}
				if(key == KeyEvent.VK_A) {
					leftPressed = false;
				}
				if(key == KeyEvent.VK_D) {
					rightPressed = false;
				}
				if(key == KeyEvent.VK_UP) {
					upPressed = false;
				}
				if(key == KeyEvent.VK_DOWN) {
					downPressed = false;
				}
				if(key == KeyEvent.VK_LEFT) {
					leftPressed = false;
				}
				if(key == KeyEvent.VK_RIGHT) {
					rightPressed = false;
				}
				if(key == KeyEvent.VK_SPACE) {
					shootPressed = false;
				}
			}
		});

		obstacles = new ArrayList<Obstacle>();
		bullets = new ArrayList<Bullet>();
		enemies = new ArrayList<EnemyTank>();
		enemyBullets = new ArrayList<Bullet>();
		scorePopups = new ArrayList<int[]>();
		powerUps = new ArrayList<PowerUp>();
		playerTank = new PlayerTank(4, 3, 1);

		eagle = new Eagle(EAGLE_COL * TILE_SIZE, EAGLE_ROW * TILE_SIZE, TILE_SIZE, TILE_SIZE);

		generateMap(map);

		hud.setLives(playerLives);
		hud.setScore(score);
		hud.setEnemyCount(totalEnemies);
	}

	private void loadSprites() {
		try {
			imgPlayerUp = ImageIO.read(new File("images/playerUp.png"));
			imgPlayerDown = ImageIO.read(new File("images/playerDown.png"));
			imgPlayerLeft = ImageIO.read(new File("images/playerLeft.png"));
			imgPlayerRight = ImageIO.read(new File("images/playerRight.png"));
			imgEnemyUp = toGreyscale(ImageIO.read(new File("images/enemyUp.png")));
			imgEnemyDown = toGreyscale(ImageIO.read(new File("images/enemyDown.png")));
			imgEnemyLeft = toGreyscale(ImageIO.read(new File("images/enemyLeft.png")));
			imgEnemyRight = toGreyscale(ImageIO.read(new File("images/enemyRight.png")));
			imgBrick = ImageIO.read(new File("images/brick.png"));
			imgSteel = ImageIO.read(new File("images/steel.png"));
			imgWater = ImageIO.read(new File("images/water.png"));
			imgBush = ImageIO.read(new File("images/bush.png"));
			imgEagle = ImageIO.read(new File("images/eagle.png"));
			imgEagleDestroyed = ImageIO.read(new File("images/eagleDestroyed.png"));
			imgTextGame = ImageIO.read(new File("images/textGame.png"));
			imgTextOver = ImageIO.read(new File("images/textOver.png"));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	private BufferedImage toGreyscale(BufferedImage src) {
		if(src == null) {
			return null;
		}
		BufferedImage grey = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics gfx = grey.getGraphics();
		gfx.drawImage(src, 0, 0, null);
		gfx.dispose();
		return grey;
	}

	private void generateMap(int[][] mapData) {
		obstacles.clear();
		for(int row = 0; row < mapData.length; row++) {
			for(int col = 0; col < mapData[row].length; col++) {
				int pixelX = col * TILE_SIZE;
				int pixelY = row * TILE_SIZE;
				int tile = mapData[row][col];

				if(tile == 1) {
					obstacles.add(new BrickWall(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
				} else if(tile == 2) {
					obstacles.add(new SteelWall(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
				} else if(tile == 3) {
					obstacles.add(new Bush(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
				} else if(tile == 4) {
					obstacles.add(new Water(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
				}
			}
		}
	}

	public void loadMapFromFile(String filePath) {
		int[][] loadedMap = new int[15][16];
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line;
			int row = 0;
			while((line = reader.readLine()) != null && row < 15) {
				String[] parts = line.split(",");
				for(int col = 0; col < parts.length && col < 16; col++) {
					loadedMap[row][col] = Integer.parseInt(parts[col].trim());
				}
				row++;
			}
			generateMap(loadedMap);
			bullets.clear();
			enemyBullets.clear();
			enemies.clear();
			scorePopups.clear();
			powerUps.clear();
			eagle.setDestroyed(false);
			spawnedEnemies = 0;
			spawnCooldown = 120;
			score = 0;
			playerLives = 3;
			playerInvincibility = 0;
			clockTimer = 0;
			shovelTimer = 0;
			savedEagleArea = null;
			gameOver = false;
			stageClear = false;
			isPaused = false;
			gameOverSlide = -1;
			gameOverDelay = 0;
			stageClearDelay = -1;
			nameAsked = false;
			waitingForNextLevel = false;
			infiniteRound = 0;
			currentLevel = parseLevel(filePath);
			introTimer = 180;
			playerTank.resetStarLevel();
			respawnPlayer();
			hud.setLives(playerLives);
			hud.setScore(score);
			hud.setLevel(currentLevel);
			hud.setEnemyCount(totalEnemies);
		} catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Failed to load the map!", "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private int parseLevel(String filePath) {
		String name = new File(filePath).getName();
		if(name.startsWith("level")) {
			String digits = name.replace("level", "").replace(".csv", "");
			try {
				return Integer.parseInt(digits);
			} catch(NumberFormatException ex) {
				return 0;
			}
		}
		return 0;
	}

	public void startGameThread() {
		if(isRunning) {
			return;
		}
		gameThread = new Thread(this);
		isRunning = true;
		gameThread.start();
	}

	public void togglePause() {
		isPaused = !isPaused;
	}

	@Override
	public void run() {
		while(isRunning) {

			if(introTimer > 0) {
				introTimer--;
			}

			if(!isPaused && !gameOver && !stageClear && introTimer == 0) {
				update();
			}

			if(gameOver && gameOverSlide >= 0) {
				if(gameOverSlide > GAMEOVER_TARGET_Y) {
					gameOverSlide -= 4;
					if(gameOverSlide < GAMEOVER_TARGET_Y) {
						gameOverSlide = GAMEOVER_TARGET_Y;
					}
				} else if(gameOverDelay > 0) {
					gameOverDelay--;
				} else if(!nameAsked) {
					nameAsked = true;
					final int finalScore = score;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							String playerName = JOptionPane.showInputDialog(GamePanel.this,
								"Score: " + finalScore + "\nEnter your name to save your score:",
								"Game Over",
								JOptionPane.PLAIN_MESSAGE);
							if(playerName != null && !playerName.trim().isEmpty()) {
								saveScore(playerName.trim(), finalScore);
							}
						}
					});
				}
			}

			if(stageClear && stageClearDelay >= 0) {
				if(stageClearDelay > 0) {
					stageClearDelay--;
				} else if(!nameAsked) {
					nameAsked = true;
					final int finalScore = score;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							String playerName = JOptionPane.showInputDialog(GamePanel.this,
								"Stage Clear!\nScore: " + finalScore + "\nEnter your name to save your score:",
								"Stage Clear",
								JOptionPane.PLAIN_MESSAGE);
							if(playerName != null && !playerName.trim().isEmpty()) {
								saveScore(playerName.trim(), finalScore);
							}
							waitingForNextLevel = true;
						}
					});
				}
			}

			repaint();
			try {
				Thread.sleep(16);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean collidesWithSolid(int newX, int newY) {
		int tankWidth = playerTank.width;
		int tankHeight = playerTank.height;

		if(newX < 0 || newY < 0 || newX + tankWidth > getWidth() || newY + tankHeight > getHeight()) {
			return true;
		}

		if(eagle != null && !eagle.isDestroyed()) {
			if(newX < eagle.x + eagle.width && newX + tankWidth > eagle.x && newY < eagle.y + eagle.height && newY + tankHeight > eagle.y) {
				return true;
			}
		}

		for(int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			if(obstacle instanceof Bush) {
				continue;
			}
			if(newX < obstacle.x + obstacle.width && newX + tankWidth > obstacle.x && newY < obstacle.y + obstacle.height && newY + tankHeight > obstacle.y) {
				return true;
			}
		}

		return false;
	}

	private boolean enemyCollidesWithSolid(EnemyTank enemy, int newX, int newY) {
		int tankWidth = enemy.width;
		int tankHeight = enemy.height;

		if(newX < 0 || newY < 0 || newX + tankWidth > getWidth() || newY + tankHeight > getHeight()) {
			return true;
		}

		if(eagle != null && !eagle.isDestroyed()) {
			if(newX < eagle.x + eagle.width && newX + tankWidth > eagle.x && newY < eagle.y + eagle.height && newY + tankHeight > eagle.y) {
				return true;
			}
		}

		for(int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			if(obstacle instanceof Bush) {
				continue;
			}
			if(newX < obstacle.x + obstacle.width && newX + tankWidth > obstacle.x && newY < obstacle.y + obstacle.height && newY + tankHeight > obstacle.y) {
				return true;
			}
		}

		if(newX < playerTank.x + playerTank.width && newX + tankWidth > playerTank.x && newY < playerTank.y + playerTank.height && newY + tankHeight > playerTank.y) {
			return true;
		}

		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank other = enemies.get(i);
			if(other == enemy) {
				continue;
			}
			if(newX < other.x + other.width && newX + tankWidth > other.x && newY < other.y + other.height && newY + tankHeight > other.y) {
				return true;
			}
		}

		return false;
	}

	private void fireBullet() {
		if(bullets.size() >= playerTank.getMaxBulletsOnScreen()) {
			return;
		}
		int bulletSize = 8;
		int bulletSpeed = 6;
		int startX;
		int startY;

		if(playerDir == 0) {
			startX = playerTank.x + (playerTank.width / 2) - (bulletSize / 2);
			startY = playerTank.y - bulletSize;
		} else if(playerDir == 1) {
			startX = playerTank.x + (playerTank.width / 2) - (bulletSize / 2);
			startY = playerTank.y + playerTank.height;
		} else if(playerDir == 2) {
			startX = playerTank.x - bulletSize;
			startY = playerTank.y + (playerTank.height / 2) - (bulletSize / 2);
		} else {
			startX = playerTank.x + playerTank.width;
			startY = playerTank.y + (playerTank.height / 2) - (bulletSize / 2);
		}

		bullets.add(new Bullet(startX, startY, bulletSize, bulletSize, bulletSpeed, 1, playerDir));

		int baseCooldown = 30;
		fireCooldown = baseCooldown + playerTank.getFireCooldownModifier();
	}

	private void fireEnemyBullet(EnemyTank enemy) {
		int bulletSize = 8;
		int bulletSpeed = 4;
		int startX;
		int startY;

		if(enemy.direction == 0) {
			startX = enemy.x + (enemy.width / 2) - (bulletSize / 2);
			startY = enemy.y - bulletSize;
		} else if(enemy.direction == 1) {
			startX = enemy.x + (enemy.width / 2) - (bulletSize / 2);
			startY = enemy.y + enemy.height;
		} else if(enemy.direction == 2) {
			startX = enemy.x - bulletSize;
			startY = enemy.y + (enemy.height / 2) - (bulletSize / 2);
		} else {
			startX = enemy.x + enemy.width;
			startY = enemy.y + (enemy.height / 2) - (bulletSize / 2);
		}

		enemyBullets.add(new Bullet(startX, startY, bulletSize, bulletSize, bulletSpeed, 1, enemy.direction));
	}

	private void updateBullets() {
		for(int i = bullets.size() - 1; i >= 0; i--) {
			Bullet bullet = bullets.get(i);

			if(bullet.direction == 0) {
				bullet.y -= bullet.speed;
			} else if(bullet.direction == 1) {
				bullet.y += bullet.speed;
			} else if(bullet.direction == 2) {
				bullet.x -= bullet.speed;
			} else {
				bullet.x += bullet.speed;
			}

			if(bullet.x < 0 || bullet.y < 0 || bullet.x > getWidth() || bullet.y > getHeight()) {
				bullets.remove(i);
				continue;
			}

			boolean bulletRemoved = false;

			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obstacle = obstacles.get(j);
				if(obstacle instanceof Bush || obstacle instanceof Water) {
					continue;
				}

				if(bullet.x < obstacle.x + obstacle.width && bullet.x + bullet.width > obstacle.x && bullet.y < obstacle.y + obstacle.height && bullet.y + bullet.height > obstacle.y) {
					if(obstacle instanceof BrickWall) {
						obstacles.remove(j);
					} else if(obstacle instanceof SteelWall && playerTank.canDestroySteel()) {
						obstacles.remove(j);
					}
					bullets.remove(i);
					bulletRemoved = true;
					break;
				}
			}
			if(bulletRemoved) {
				continue;
			}

			if(eagle != null && !eagle.isDestroyed()) {
				if(bullet.x < eagle.x + eagle.width && bullet.x + bullet.width > eagle.x && bullet.y < eagle.y + eagle.height && bullet.y + bullet.height > eagle.y) {
					eagle.setDestroyed(true);
					bullets.remove(i);
					bulletRemoved = true;
				}
			}
			if(bulletRemoved) {
				continue;
			}

			for(int j = enemies.size() - 1; j >= 0; j--) {
				EnemyTank enemy = enemies.get(j);
				if(bullet.x < enemy.x + enemy.width && bullet.x + bullet.width > enemy.x && bullet.y < enemy.y + enemy.height && bullet.y + bullet.height > enemy.y) {
					enemy.hp -= bullet.damage;
					if(enemy.hp <= 0) {
						int popupX = enemy.x + (enemy.width / 2) - 12;
						int popupY = enemy.y + (enemy.height / 2);
						scorePopups.add(new int[]{popupX, popupY, 100, 60});
						if(Math.random() < 0.3) {
							spawnRandomPowerUp(enemy.x, enemy.y);
						}
						enemies.remove(j);
						score += 100;
						hud.setScore(score);
						hud.setEnemyCount(totalEnemies - spawnedEnemies + enemies.size());
					}
					bullets.remove(i);
					bulletRemoved = true;
					break;
				}
			}
		}
	}

	private void updateEnemyBullets() {
		for(int i = enemyBullets.size() - 1; i >= 0; i--) {
			Bullet bullet = enemyBullets.get(i);

			if(bullet.direction == 0) {
				bullet.y -= bullet.speed;
			} else if(bullet.direction == 1) {
				bullet.y += bullet.speed;
			} else if(bullet.direction == 2) {
				bullet.x -= bullet.speed;
			} else {
				bullet.x += bullet.speed;
			}

			if(bullet.x < 0 || bullet.y < 0 || bullet.x > getWidth() || bullet.y > getHeight()) {
				enemyBullets.remove(i);
				continue;
			}

			boolean bulletRemoved = false;

			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obstacle = obstacles.get(j);
				if(obstacle instanceof Bush || obstacle instanceof Water) {
					continue;
				}

				if(bullet.x < obstacle.x + obstacle.width && bullet.x + bullet.width > obstacle.x && bullet.y < obstacle.y + obstacle.height && bullet.y + bullet.height > obstacle.y) {
					if(obstacle instanceof BrickWall) {
						obstacles.remove(j);
					}
					enemyBullets.remove(i);
					bulletRemoved = true;
					break;
				}
			}
			if(bulletRemoved) {
				continue;
			}
			if(eagle != null && !eagle.isDestroyed()) {
				if(bullet.x < eagle.x + eagle.width && bullet.x + bullet.width > eagle.x && bullet.y < eagle.y + eagle.height && bullet.y + bullet.height > eagle.y) {
					eagle.setDestroyed(true);
					enemyBullets.remove(i);
					bulletRemoved = true;
				}
			}
			if(bulletRemoved) {
				continue;
			}
			if(playerInvincibility == 0 && !playerTank.isShieldActive()) {
				if(bullet.x < playerTank.x + playerTank.width && bullet.x + bullet.width > playerTank.x && bullet.y < playerTank.y + playerTank.height && bullet.y + bullet.height > playerTank.y) {
					playerLives--;
					hud.setLives(playerLives);
					enemyBullets.remove(i);
					respawnPlayer();
				}
			}
		}
	}

	private void updateScorePopups() {
		for(int i = scorePopups.size() - 1; i >= 0; i--) {
			int[] popup = scorePopups.get(i);
			popup[3]--;
			if(popup[3] <= 0) {
				scorePopups.remove(i);
			}
		}
	}

	public void setDifficulty(int diff) {
		this.difficulty = diff;
		if(diff == 0) {
			maxOnScreen = 3;
		} else if(diff == 2) {
			maxOnScreen = 6;
		} else {
			maxOnScreen = 4;
		}
	}

	private void advanceToNextLevel() {
		int savedScore = score;
		int savedLives = playerLives;
		int savedInfiniteRound;

		String nextPath;
		if(currentLevel < 3) {
			savedInfiniteRound = 0;
			nextPath = "maps" + File.separator + "level" + (currentLevel + 1) + ".csv";
		} else {
			savedInfiniteRound = infiniteRound + 1;
			nextPath = "maps" + File.separator + "level3.csv";
		}

		loadMapFromFile(nextPath);

		score = savedScore;
		playerLives = savedLives;
		infiniteRound = savedInfiniteRound;
		hud.setScore(score);
		hud.setLives(playerLives);
	}

	private int getEnemySpeedForLevel() {
		int baseSpeed;
		if(currentLevel == 2) {
			baseSpeed = 2;
		} else if(currentLevel >= 3) {
			baseSpeed = 3;
		} else {
			baseSpeed = 1;
		}
		int totalSpeed = baseSpeed + infiniteRound;
		if(totalSpeed > 6) {
			totalSpeed = 6;
		}
		return totalSpeed;
	}

	private void spawnEnemy() {
		int spawnX = SPAWN_X[spawnedEnemies % 3];
		int spawnY = 0;

		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank enemy = enemies.get(i);
			if(Math.abs(enemy.x - spawnX) < 40 && enemy.y < 40) {
				return;
			}
		}

		enemies.add(new EnemyTank(spawnX, spawnY, getEnemySpeedForLevel()));
		spawnedEnemies++;
		hud.setEnemyCount(totalEnemies - spawnedEnemies + enemies.size());
	}

	private int aimAtPlayer(EnemyTank enemy) {
		int diffX = playerTank.x - enemy.x;
		int diffY = playerTank.y - enemy.y;

		if(diffX < 0) {
			diffX = -diffX;
		}
		if(diffY < 0) {
			diffY = -diffY;
		}

		if(diffX > diffY) {
			if(playerTank.x > enemy.x) {
				return 3;
			} else {
				return 2;
			}
		} else {
			if(playerTank.y > enemy.y) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	private int pickDirectionTowardPlayer(EnemyTank enemy) {
		int randomChance = (int)(Math.random() * 100);
		if(randomChance < 35) {
			return (int)(Math.random() * 4);
		} else {
			return aimAtPlayer(enemy);
		}
	}

	private void updateEnemies() {
		if(clockTimer > 0) {
			return;
		}
		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank enemy = enemies.get(i);

			if(enemy.fireCooldown > 0) {
				enemy.fireCooldown--;
			}
			if(enemy.moveCooldown > 0) {
				enemy.moveCooldown--;
			}

			if(enemy.moveCooldown == 0) {
				enemy.direction = pickDirectionTowardPlayer(enemy);
				enemy.moveCooldown = 60 + (int)(Math.random() * 120);
			}

			int newX = enemy.x;
			int newY = enemy.y;

			if(enemy.direction == 0) {
				newY -= enemy.speed;
			} else if(enemy.direction == 1) {
				newY += enemy.speed;
			} else if(enemy.direction == 2) {
				newX -= enemy.speed;
			} else {
				newX += enemy.speed;
			}

			if(enemyCollidesWithSolid(enemy, newX, newY)) {
				enemy.direction = pickDirectionTowardPlayer(enemy);
				enemy.moveCooldown = 20 + (int)(Math.random() * 40);
			} else {
				enemy.x = newX;
				enemy.y = newY;
			}

			if(enemy.fireCooldown == 0) {
				enemy.direction = aimAtPlayer(enemy);
				fireEnemyBullet(enemy);
				if(difficulty == 0) {
					enemy.fireCooldown = 90 + (int)(Math.random() * 90);
				} else if(difficulty == 2) {
					enemy.fireCooldown = 20 + (int)(Math.random() * 30);
				} else {
					enemy.fireCooldown = 45 + (int)(Math.random() * 60);
				}
			}
		}
	}

	private void respawnPlayer() {
		if(playerLives <= 0) {
			triggerGameOver();
			return;
		}
		playerTank.x = 4 * TILE_SIZE;
		playerTank.y = 13 * TILE_SIZE;
		playerDir = 0;
		playerInvincibility = 120;
		playerTank.resetStarLevel();
		bullets.clear();
	}

	private void triggerGameOver() {
		if(gameOver) {
			return;
		}
		gameOver = true;
		gameOverSlide = PANEL_HEIGHT;
		gameOverDelay = 60;
	}

	private void saveScore(String playerName, int finalScore) {
		Date now = new Date();
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(now);
		String timeStr = new SimpleDateFormat("HH:mm:ss").format(now);

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("scores.csv", true));
			writer.write(playerName + "," + finalScore + "," + dateStr + "," + timeStr);
			writer.newLine();
		} catch(IOException ee) {
			ee.printStackTrace();
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch(IOException ee) {
					ee.printStackTrace();
				}
			}
		}
	}

	private void spawnRandomPowerUp(int pixelX, int pixelY) {
		int tileX = pixelX / TILE_SIZE;
		int tileY = pixelY / TILE_SIZE;
		if(tileX < 0) {
			tileX = 0;
		}
		if(tileY < 0) {
			tileY = 0;
		}
		if(tileX > 15) {
			tileX = 15;
		}
		if(tileY > 14) {
			tileY = 14;
		}
		int randomType = (int)(Math.random() * 6);
		powerUps.add(new PowerUp(tileX, tileY, randomType));
	}

	private void updatePowerUps() {
		for(int i = powerUps.size() - 1; i >= 0; i--) {
			PowerUp powerUp = powerUps.get(i);
			powerUp.update();
			if(powerUp.getBounds().intersects(
					new java.awt.Rectangle(playerTank.x, playerTank.y, playerTank.width, playerTank.height))) {
				applyPowerUp(powerUp.getPowerUpType());
				powerUps.remove(i);
			}
		}
	}

	private void applyPowerUp(int powerUpType) {
		if(powerUpType == PowerUp.TANK) {
			playerLives++;
			hud.setLives(playerLives);
		} else if(powerUpType == PowerUp.STAR) {
			playerTank.addStar();
		} else if(powerUpType == PowerUp.BOMB) {
			for(int i = enemies.size() - 1; i >= 0; i--) {
				EnemyTank enemy = enemies.get(i);
				int popupX = enemy.x + (enemy.width / 2) - 12;
				int popupY = enemy.y + (enemy.height / 2);
				scorePopups.add(new int[]{popupX, popupY, 100, 60});
				score += 100;
			}
			enemies.clear();
			enemyBullets.clear();
			hud.setScore(score);
			hud.setEnemyCount(totalEnemies - spawnedEnemies);
		} else if(powerUpType == PowerUp.CLOCK) {
			clockTimer = CLOCK_DURATION;
		} else if(powerUpType == PowerUp.SHOVEL) {
			activateShovel();
			shovelTimer = SHOVEL_DURATION;
		} else if(powerUpType == PowerUp.SHIELD) {
			playerTank.activateShield();
		}
	}

	private void activateShovel() {
		int baseCol = EAGLE_COL - 1;
		int topRow = EAGLE_ROW - 2;
		int sideRow = EAGLE_ROW - 1;
		int[][] shovelTiles = {
			{baseCol, topRow},
			{baseCol + 1, topRow},
			{baseCol + 2, topRow},
			{baseCol, sideRow},
			{baseCol + 2, sideRow}
		};
		savedEagleArea = new int[shovelTiles.length][2];
		for(int i = 0; i < shovelTiles.length; i++) {
			savedEagleArea[i][0] = shovelTiles[i][0];
			savedEagleArea[i][1] = shovelTiles[i][1];
		}
		for(int i = 0; i < shovelTiles.length; i++) {
			int tileX = shovelTiles[i][0];
			int tileY = shovelTiles[i][1];
			int pixelX = tileX * TILE_SIZE;
			int pixelY = tileY * TILE_SIZE;
			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obs = obstacles.get(j);
				if(obs.x == pixelX && obs.y == pixelY) {
					obstacles.remove(j);
				}
			}
			obstacles.add(new SteelWall(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
		}
	}

	private void deactivateShovel() {
		if(savedEagleArea == null) {
			return;
		}
		for(int i = 0; i < savedEagleArea.length; i++) {
			int tileX = savedEagleArea[i][0];
			int tileY = savedEagleArea[i][1];
			int pixelX = tileX * TILE_SIZE;
			int pixelY = tileY * TILE_SIZE;
			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obs = obstacles.get(j);
				if(obs.x == pixelX && obs.y == pixelY && obs instanceof SteelWall) {
					obstacles.remove(j);
				}
			}
			obstacles.add(new BrickWall(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
		}
		savedEagleArea = null;
	}

	private void triggerStageClear() {
		if(stageClear) {
			return;
		}
		stageClear = true;
		stageClearDelay = 150;
	}

	private void update() {
		int newX = playerTank.x;
		int newY = playerTank.y;

		if(upPressed) {
			playerDir = 0;
			if(!collidesWithSolid(newX, newY - playerTank.speed)) {
				playerTank.y -= playerTank.speed;
			}
		}
		if(downPressed) {
			playerDir = 1;
			if(!collidesWithSolid(newX, newY + playerTank.speed)) {
				playerTank.y += playerTank.speed;
			}
		}
		if(leftPressed) {
			playerDir = 2;
			if(!collidesWithSolid(newX - playerTank.speed, newY)) {
				playerTank.x -= playerTank.speed;
			}
		}
		if(rightPressed) {
			playerDir = 3;
			if(!collidesWithSolid(newX + playerTank.speed, newY)) {
				playerTank.x += playerTank.speed;
			}
		}

		if(fireCooldown > 0) {
			fireCooldown--;
		}
		if(playerInvincibility > 0) {
			playerInvincibility--;
		}

		playerTank.updatePowerUps();
		if(clockTimer > 0) {
			clockTimer--;
		}
		if(shovelTimer > 0) {
			shovelTimer--;
			if(shovelTimer == 0) {
				deactivateShovel();
			}
		}

		updatePowerUps();
		updateBullets();
		updateScorePopups();

		if(shootPressed && fireCooldown == 0) {
			fireBullet();
		}

		if(spawnedEnemies < totalEnemies && enemies.size() < maxOnScreen) {
			if(spawnCooldown > 0) {
				spawnCooldown--;
			} else {
				spawnEnemy();
				spawnCooldown = 120;
			}
		}

		updateEnemies();
		updateEnemyBullets();

		if(eagle != null && eagle.isDestroyed()) {
			triggerGameOver();
		}
		if(spawnedEnemies >= totalEnemies && enemies.isEmpty() && !gameOver && !stageClear) {
			triggerStageClear();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for(int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			if(obstacle instanceof BrickWall) {
				if(imgBrick != null) {
					g.drawImage(imgBrick, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
				} else {
					g.setColor(new Color(160, 82, 45));
					g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
				}
			} else if(obstacle instanceof SteelWall) {
				if(imgSteel != null) {
					g.drawImage(imgSteel, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
				} else {
					g.setColor(new Color(140, 140, 140));
					g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
				}
			} else if(obstacle instanceof Water) {
				if(imgWater != null) {
					g.drawImage(imgWater, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
				} else {
					g.setColor(new Color(30, 144, 255));
					g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
				}
			}
		}

		if(eagle != null) {
			if(!eagle.isDestroyed()) {
				if(imgEagle != null) {
					g.drawImage(imgEagle, eagle.x, eagle.y, eagle.width, eagle.height, null);
				} else {
					g.setColor(new Color(255, 215, 0));
					g.fillRect(eagle.x, eagle.y, eagle.width, eagle.height);
					g.setColor(Color.BLACK);
					g.setFont(new Font("Arial", Font.BOLD, 18));
					g.drawString("E", eagle.x + 13, eagle.y + 27);
				}
			} else {
				if(imgEagleDestroyed != null) {
					g.drawImage(imgEagleDestroyed, eagle.x, eagle.y, eagle.width, eagle.height, null);
				} else {
					g.setColor(Color.DARK_GRAY);
					g.fillRect(eagle.x, eagle.y, eagle.width, eagle.height);
					g.setColor(Color.RED);
					g.setFont(new Font("Arial", Font.BOLD, 14));
					g.drawString("X", eagle.x + 13, eagle.y + 27);
				}
			}
		}

		BufferedImage[] enemySprites = {imgEnemyUp, imgEnemyDown, imgEnemyLeft, imgEnemyRight};
		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank enemy = enemies.get(i);
			BufferedImage sprite = enemySprites[enemy.direction];
			if(sprite != null) {
				g.drawImage(sprite, enemy.x, enemy.y, enemy.width, enemy.height, null);
			} else {
				g.setColor(Color.GRAY);
				g.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
			}
		}

		for(int i = 0; i < powerUps.size(); i++) {
			powerUps.get(i).draw(g);
		}

		if(playerInvincibility == 0 || (playerInvincibility / 5) % 2 == 0) {
			BufferedImage[] playerSprites = {imgPlayerUp, imgPlayerDown, imgPlayerLeft, imgPlayerRight};
			BufferedImage currentSprite = playerSprites[playerDir];
			if(currentSprite != null) {
				g.drawImage(currentSprite, playerTank.x, playerTank.y, playerTank.width, playerTank.height, null);
			} else {
				g.setColor(Color.YELLOW);
				g.fillRect(playerTank.x, playerTank.y, playerTank.width, playerTank.height);
			}

			if(playerTank.isShieldActive()) {
				int margin = 4;
				g.setColor(new Color(0, 220, 255, 160));
				g.drawOval(playerTank.x - margin, playerTank.y - margin,
					playerTank.width + margin * 2, playerTank.height + margin * 2);
				g.setColor(new Color(0, 180, 255, 80));
				g.drawOval(playerTank.x - margin - 2, playerTank.y - margin - 2,
					playerTank.width + (margin + 2) * 2, playerTank.height + (margin + 2) * 2);
			}
		}

		g.setColor(Color.WHITE);
		for(int i = 0; i < bullets.size(); i++) {
			Bullet bullet = bullets.get(i);
			g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
		}

		g.setColor(Color.RED);
		for(int i = 0; i < enemyBullets.size(); i++) {
			Bullet bullet = enemyBullets.get(i);
			g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
		}

		for(int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			if(obstacle instanceof Bush) {
				if(imgBush != null) {
					g.drawImage(imgBush, obstacle.x, obstacle.y, obstacle.width, obstacle.height, null);
				} else {
					g.setColor(new Color(34, 139, 34));
					g.fillRect(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
				}
			}
		}

		g.setFont(new Font("Monospaced", Font.BOLD, 14));
		for(int i = 0; i < scorePopups.size(); i++) {
			int[] popup = scorePopups.get(i);
			int popupX = popup[0];
			int popupY = popup[1];
			int popupValue = popup[2];
			int framesLeft = popup[3];
			int driftY = (60 - framesLeft) / 3;
			int alpha;
			if(framesLeft < 20) {
				alpha = framesLeft * 255 / 20;
			} else {
				alpha = 255;
			}
			g.setColor(new Color(255, 220, 0, alpha));
			g.drawString("+" + popupValue, popupX, popupY - driftY);
		}

		if(stageClear) {
			g.setColor(new Color(0, 0, 0, 180));
			g.fillRect(0, PANEL_HEIGHT / 2 - 50, PANEL_WIDTH, 100);

			g.setColor(new Color(220, 200, 0));
			g.setFont(new Font("Monospaced", Font.BOLD, 32));
			FontMetrics clearMetrics = g.getFontMetrics();
			String clearText = "STAGE CLEAR!";
			g.drawString(clearText,
				(PANEL_WIDTH - clearMetrics.stringWidth(clearText)) / 2,
				PANEL_HEIGHT / 2 - 10);

			g.setColor(new Color(200, 200, 200));
			g.setFont(new Font("Monospaced", Font.BOLD, 16));
			FontMetrics scoreMetrics = g.getFontMetrics();
			String scoreText = "SCORE: " + String.format("%05d", score);
			g.drawString(scoreText,
				(PANEL_WIDTH - scoreMetrics.stringWidth(scoreText)) / 2,
				PANEL_HEIGHT / 2 + 24);

			g.setFont(new Font("Monospaced", Font.PLAIN, 11));
			FontMetrics hintMetrics = g.getFontMetrics();
			String hintText;
			if(waitingForNextLevel) {
				g.setColor(new Color(220, 200, 0));
				hintText = "PRESS SPACE TO CONTINUE";
			} else {
				g.setColor(new Color(160, 160, 160));
				hintText = "Start a new game from the Menu.";
			}
			g.drawString(hintText,
				(PANEL_WIDTH - hintMetrics.stringWidth(hintText)) / 2,
				PANEL_HEIGHT / 2 + 44);
		}

		if(gameOver && gameOverSlide >= 0) {
			g.setColor(new Color(0, 0, 0, 140));
			g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

			int spriteX = (PANEL_WIDTH - GAMEOVER_SPRITE_W) / 2;

			if(imgTextGame != null && imgTextOver != null) {
				g.drawImage(imgTextGame, spriteX, gameOverSlide,
					GAMEOVER_SPRITE_W, GAMEOVER_SPRITE_H, null);
				g.drawImage(imgTextOver, spriteX, gameOverSlide + GAMEOVER_SPRITE_H,
					GAMEOVER_SPRITE_W, GAMEOVER_SPRITE_H, null);
			} else {
				g.setColor(Color.RED);
				g.setFont(new Font("Monospaced", Font.BOLD, 56));
				FontMetrics fm = g.getFontMetrics();
				String fallback = "GAME OVER";
				g.drawString(fallback,
					(PANEL_WIDTH - fm.stringWidth(fallback)) / 2,
					PANEL_HEIGHT / 2 + 20);
			}
		}

		if(introTimer > 0) {
			g.setColor(new Color(0, 0, 0, 210));
			g.fillRect(0, PANEL_HEIGHT / 2 - 40, PANEL_WIDTH, 80);

			g.setColor(new Color(190, 190, 190));
			g.setFont(new Font("Monospaced", Font.BOLD, 14));
			FontMetrics stageLabelMetrics = g.getFontMetrics();
			String stageLabel = "STAGE";
			g.drawString(stageLabel,
				(PANEL_WIDTH - stageLabelMetrics.stringWidth(stageLabel)) / 2,
				PANEL_HEIGHT / 2 - 8);

			String levelStr;
			if(currentLevel > 0) {
				levelStr = String.valueOf(currentLevel);
			} else {
				levelStr = "?";
			}
			g.setColor(Color.WHITE);
			g.setFont(new Font("Monospaced", Font.BOLD, 28));
			FontMetrics levelMetrics = g.getFontMetrics();
			g.drawString(levelStr,
				(PANEL_WIDTH - levelMetrics.stringWidth(levelStr)) / 2,
				PANEL_HEIGHT / 2 + 26);
		}
	}
}

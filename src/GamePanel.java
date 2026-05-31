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
	private static final int TILE_SIZE    = 40;
	private static final int PANEL_WIDTH  = 640;
	private static final int PANEL_HEIGHT = 600;

	// Eagle fixed position: col 7, row 14
	private static final int EAGLE_COL = 7;
	private static final int EAGLE_ROW = 14;

	// Enemy spawn columns (left, center, right)
	private static final int[] SPAWN_X = {0, 320, 600};
	private int maxOnScreen = 4;

	// GAME OVER sprite dimensions (extracted at 256×64 each — 8× scale of original 32×8 text)
	private static final int GAMEOVER_SPRITE_W = 256;
	private static final int GAMEOVER_SPRITE_H = 64;
	// Target Y for the top of the combined "GAME / OVER" block when centered
	private static final int GAMEOVER_TARGET_Y = (PANEL_HEIGHT - GAMEOVER_SPRITE_H * 2) / 2;

	// --- Game objects ---
	private PlayerTank           playerTank;
	private Eagle                eagle;
	private ArrayList<Obstacle>  obstacles;
	private ArrayList<Bullet>    bullets;
	private ArrayList<EnemyTank> enemies;
	private ArrayList<Bullet>    enemyBullets;
	private HUDPanel             hud;

	// --- Thread and state ---
	private Thread  gameThread;
	private boolean isRunning;
	private boolean isPaused    = false;
	private boolean gameOver    = false;
	private boolean stageClear  = false;

	// --- Player state ---
	private int playerDir           = 0; // 0=Up 1=Down 2=Left 3=Right
	private int fireCooldown        = 0;
	private int playerLives         = 3;
	private int playerInvincibility = 0; // invincibility frames after getting hit
	private int score               = 0;

	// --- Enemy state ---
	private int totalEnemies   = 20;
	private int difficulty     = 1;   // 0=Easy 1=Medium 2=Hard
	private int spawnedEnemies = 0;
	private int spawnCooldown  = 120; // 2 sec before first spawn

	// --- Key states ---
	private boolean upPressed    = false;
	private boolean downPressed  = false;
	private boolean leftPressed  = false;
	private boolean rightPressed = false;
	private boolean shootPressed = false;

	// --- UI animation state ---
	private int     introTimer    = 0;     // >0 = show stage intro overlay; decrements each frame
	private int     currentLevel  = 1;    // number shown on the stage intro screen
	private int     gameOverSlide   = -1;  // -1 = inactive; otherwise Y of top of "GAME" sprite
	private int     gameOverDelay   = 0;   // wait frames after slide stops before showing dialog
	private int     stageClearDelay      = -1;   // -1 = inactive; counts down after stage clear before asking name
	private boolean nameAsked            = false; // prevents the name dialog from showing twice
	private boolean waitingForNextLevel  = false; // true after stage clear name dialog — waiting for SPACE
	private int     infiniteRound        = 0;     // how many times we have looped past level 3 (for speed scaling)

	// --- Score popup floaters: {x, y, value, framesLeft} ---
	private ArrayList<int[]> scorePopups;

	// --- Power-ups ---
	private ArrayList<PowerUp> powerUps;
	private static final int CLOCK_DURATION  = 300; // ~5 seconds at 60 ticks/s
	private static final int SHOVEL_DURATION = 600; // ~10 seconds
	private int clockTimer  = 0; // >0 = enemies frozen
	private int shovelTimer = 0; // >0 = steel walls around eagle are active
	// Saved eagle-area tiles (rows 12-14, cols 6-8) for shovel restoration
	private int[][] savedEagleArea = null;

	// --- Sprites ---
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
	private BufferedImage imgTextGame;   // "GAME" sprite (128×64)
	private BufferedImage imgTextOver;   // "OVER" sprite (128×64)

	// --- Default map (Level 1) ---
	private int[][] map = {
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row  0
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row  1
		{0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0}, // row  2
		{0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0}, // row  3
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row  4
		{0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0}, // row  5
		{0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 0}, // row  6
		{0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1}, // row  7
		{0, 1, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1}, // row  8
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // row  9
		{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0}, // row 10
		{0, 0, 1, 0, 0, 0, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0}, // row 11
		{0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0}, // row 12
		{0, 1, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0}, // row 13
		{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}  // row 14
	};

	// ---------------------------------------------------------------
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
				if(key == KeyEvent.VK_W)     { upPressed    = true; }
				if(key == KeyEvent.VK_S)     { downPressed  = true; }
				if(key == KeyEvent.VK_A)     { leftPressed  = true; }
				if(key == KeyEvent.VK_D)     { rightPressed = true; }
				if(key == KeyEvent.VK_UP)    { upPressed    = true; }
				if(key == KeyEvent.VK_DOWN)  { downPressed  = true; }
				if(key == KeyEvent.VK_LEFT)  { leftPressed  = true; }
				if(key == KeyEvent.VK_RIGHT) { rightPressed = true; }
				if(key == KeyEvent.VK_SPACE) {
					if(waitingForNextLevel) {
						// Advance to the next level instead of firing
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
				if(key == KeyEvent.VK_W)     { upPressed    = false; }
				if(key == KeyEvent.VK_S)     { downPressed  = false; }
				if(key == KeyEvent.VK_A)     { leftPressed  = false; }
				if(key == KeyEvent.VK_D)     { rightPressed = false; }
				if(key == KeyEvent.VK_UP)    { upPressed    = false; }
				if(key == KeyEvent.VK_DOWN)  { downPressed  = false; }
				if(key == KeyEvent.VK_LEFT)  { leftPressed  = false; }
				if(key == KeyEvent.VK_RIGHT) { rightPressed = false; }
				if(key == KeyEvent.VK_SPACE) { shootPressed = false; }
			}
		});

		obstacles    = new ArrayList<Obstacle>();
		bullets      = new ArrayList<Bullet>();
		enemies      = new ArrayList<EnemyTank>();
		enemyBullets = new ArrayList<Bullet>();
		scorePopups  = new ArrayList<int[]>();
		powerUps     = new ArrayList<PowerUp>();
		playerTank   = new PlayerTank(4, 3, 1);

		eagle = new Eagle(EAGLE_COL * TILE_SIZE, EAGLE_ROW * TILE_SIZE, TILE_SIZE, TILE_SIZE);

		generateMap(map);

		hud.setLives(playerLives);
		hud.setScore(score);
		hud.setEnemyCount(totalEnemies);
	}

	// ---------------------------------------------------------------
	private void loadSprites() {
		try {
			imgPlayerUp       = ImageIO.read(new File("images/playerUp.png"));
			imgPlayerDown     = ImageIO.read(new File("images/playerDown.png"));
			imgPlayerLeft     = ImageIO.read(new File("images/playerLeft.png"));
			imgPlayerRight    = ImageIO.read(new File("images/playerRight.png"));
			imgEnemyUp        = toGreyscale(ImageIO.read(new File("images/enemyUp.png")));
			imgEnemyDown      = toGreyscale(ImageIO.read(new File("images/enemyDown.png")));
			imgEnemyLeft      = toGreyscale(ImageIO.read(new File("images/enemyLeft.png")));
			imgEnemyRight     = toGreyscale(ImageIO.read(new File("images/enemyRight.png")));
			imgBrick          = ImageIO.read(new File("images/brick.png"));
			imgSteel          = ImageIO.read(new File("images/steel.png"));
			imgWater          = ImageIO.read(new File("images/water.png"));
			imgBush           = ImageIO.read(new File("images/bush.png"));
			imgEagle          = ImageIO.read(new File("images/eagle.png"));
			imgEagleDestroyed = ImageIO.read(new File("images/eagleDestroyed.png"));
			imgTextGame       = ImageIO.read(new File("images/textGame.png"));
			imgTextOver       = ImageIO.read(new File("images/textOver.png"));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	// ---------------------------------------------------------------
	// Converts a BufferedImage to greyscale by drawing it into a grey canvas
	private BufferedImage toGreyscale(BufferedImage src) {
		if(src == null) { return null; }
		BufferedImage grey = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics gfx = grey.getGraphics();
		gfx.drawImage(src, 0, 0, null);
		gfx.dispose();
		return grey;
	}

	// ---------------------------------------------------------------
	private void generateMap(int[][] mapData) {
		obstacles.clear();
		for(int row = 0; row < mapData.length; row++) {
			for(int col = 0; col < mapData[row].length; col++) {
				int pixelX = col * TILE_SIZE;
				int pixelY = row * TILE_SIZE;
				int tile   = mapData[row][col];

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

	// ---------------------------------------------------------------
	public void loadMapFromFile(String filePath) {
		// System.out.println("Loading map: " + filePath);
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
			spawnedEnemies      = 0;
			spawnCooldown       = 120;
			score               = 0;
			playerLives         = 3;
			playerInvincibility = 0;
			clockTimer          = 0;
			shovelTimer         = 0;
			savedEagleArea      = null;
			gameOver            = false;
			stageClear          = false;
			isPaused            = false;
			gameOverSlide       = -1;
			gameOverDelay       = 0;
			stageClearDelay     = -1;
			nameAsked           = false;
			waitingForNextLevel = false;
			infiniteRound       = 0;
			currentLevel        = parseLevel(filePath);
			introTimer          = 180; // 3 seconds of stage intro before play begins
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
				try { reader.close(); } catch(IOException ex) { ex.printStackTrace(); }
			}
		}
	}

	// ---------------------------------------------------------------
	// Parses the level number from a file path such as "maps/level2.csv" → 2
	private int parseLevel(String filePath) {
		String name = new File(filePath).getName(); // e.g. "level1.csv"
		if(name.startsWith("level")) {
			String digits = name.replace("level", "").replace(".csv", "");
			try {
				return Integer.parseInt(digits);
			} catch(NumberFormatException ex) {
				return 0;
			}
		}
		return 0; // custom map
	}

	// ---------------------------------------------------------------
	public void startGameThread() {
		if(isRunning) {
			return; // thread already running — loadMapFromFile() already reset state
		}
		gameThread = new Thread(this);
		isRunning  = true;
		gameThread.start();
	}

	public void togglePause() {
		isPaused = !isPaused;
	}

	// ---------------------------------------------------------------
	@Override
	public void run() {
		while(isRunning) {

			// Stage intro countdown — gameplay is frozen while intro is visible
			if(introTimer > 0) {
				introTimer--;
			}

			// Normal gameplay update
			if(!isPaused && !gameOver && !stageClear && introTimer == 0) {
				update();
			}

			// GAME OVER slide-in animation + name dialog
			if(gameOver && gameOverSlide >= 0) {
				if(gameOverSlide > GAMEOVER_TARGET_Y) {
					gameOverSlide -= 4; // slide upward by 4px per frame
					if(gameOverSlide < GAMEOVER_TARGET_Y) {
						gameOverSlide = GAMEOVER_TARGET_Y; // clamp to target
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

			// STAGE CLEAR — wait then ask for name
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
							// Show "PRESS SPACE" prompt on the overlay
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

	// ---------------------------------------------------------------
	// Returns true if player tank at (newX, newY) overlaps a solid object
	private boolean collidesWithSolid(int newX, int newY) {
		int tankWidth  = playerTank.width;
		int tankHeight = playerTank.height;

		// Panel boundary
		if(newX < 0 || newY < 0 || newX + tankWidth > getWidth() || newY + tankHeight > getHeight()) {
			return true;
		}

		// Eagle
		if(eagle != null && !eagle.isDestroyed()) {
			if(newX < eagle.x + eagle.width  && newX + tankWidth  > eagle.x &&
			   newY < eagle.y + eagle.height && newY + tankHeight > eagle.y) {
				return true;
			}
		}

		// Obstacles — Bush is passable for tanks, Water is NOT
		for(int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			if(obstacle instanceof Bush) { continue; }
			if(newX < obstacle.x + obstacle.width  && newX + tankWidth  > obstacle.x &&
			   newY < obstacle.y + obstacle.height && newY + tankHeight > obstacle.y) {
				return true;
			}
		}

		return false;
	}

	// ---------------------------------------------------------------
	// Returns true if the given enemy tank at (newX, newY) overlaps a solid object
	private boolean enemyCollidesWithSolid(EnemyTank enemy, int newX, int newY) {
		int tankWidth  = enemy.width;
		int tankHeight = enemy.height;

		// Panel boundary
		if(newX < 0 || newY < 0 || newX + tankWidth > getWidth() || newY + tankHeight > getHeight()) {
			return true;
		}

		// Eagle
		if(eagle != null && !eagle.isDestroyed()) {
			if(newX < eagle.x + eagle.width  && newX + tankWidth  > eagle.x &&
			   newY < eagle.y + eagle.height && newY + tankHeight > eagle.y) {
				return true;
			}
		}

		// Obstacles — Bush is passable for tanks, Water is NOT
		for(int i = 0; i < obstacles.size(); i++) {
			Obstacle obstacle = obstacles.get(i);
			if(obstacle instanceof Bush) { continue; }
			if(newX < obstacle.x + obstacle.width  && newX + tankWidth  > obstacle.x &&
			   newY < obstacle.y + obstacle.height && newY + tankHeight > obstacle.y) {
				return true;
			}
		}

		// Player tank
		if(newX < playerTank.x + playerTank.width  && newX + tankWidth  > playerTank.x &&
		   newY < playerTank.y + playerTank.height && newY + tankHeight > playerTank.y) {
			return true;
		}

		// Other enemies
		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank other = enemies.get(i);
			if(other == enemy) { continue; }
			if(newX < other.x + other.width  && newX + tankWidth  > other.x &&
			   newY < other.y + other.height && newY + tankHeight > other.y) {
				return true;
			}
		}

		return false;
	}

	// ---------------------------------------------------------------
	// Spawns a player bullet from the tank edge in the facing direction
	private void fireBullet() {
		// Star level 2+: allow 2 bullets on screen at once; default: 1
		if(bullets.size() >= playerTank.getMaxBulletsOnScreen()) {
			return;
		}
		int bulletSize  = 8;
		int bulletSpeed = 6;
		int startX;
		int startY;

		if(playerDir == 0) {
			startX = playerTank.x + (playerTank.width  / 2) - (bulletSize / 2);
			startY = playerTank.y - bulletSize;
		} else if(playerDir == 1) {
			startX = playerTank.x + (playerTank.width  / 2) - (bulletSize / 2);
			startY = playerTank.y + playerTank.height;
		} else if(playerDir == 2) {
			startX = playerTank.x - bulletSize;
			startY = playerTank.y + (playerTank.height / 2) - (bulletSize / 2);
		} else {
			startX = playerTank.x + playerTank.width;
			startY = playerTank.y + (playerTank.height / 2) - (bulletSize / 2);
		}

		bullets.add(new Bullet(startX, startY, bulletSize, bulletSize, bulletSpeed, 1, playerDir));

		// Fixed fire rate — space held fires continuously at this interval
		// Star level 1+: reduced cooldown via getFireCooldownModifier()
		int baseCooldown = 30;
		fireCooldown = baseCooldown + playerTank.getFireCooldownModifier();
	}

	// ---------------------------------------------------------------
	// Spawns an enemy bullet from the given enemy in its facing direction
	private void fireEnemyBullet(EnemyTank enemy) {
		int bulletSize  = 8;
		int bulletSpeed = 4;
		int startX;
		int startY;

		if(enemy.direction == 0) {
			startX = enemy.x + (enemy.width  / 2) - (bulletSize / 2);
			startY = enemy.y - bulletSize;
		} else if(enemy.direction == 1) {
			startX = enemy.x + (enemy.width  / 2) - (bulletSize / 2);
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

	// ---------------------------------------------------------------
	// Moves player bullets and checks all collisions
	private void updateBullets() {
		for(int i = bullets.size() - 1; i >= 0; i--) {
			Bullet bullet = bullets.get(i);

			if(bullet.direction == 0)      { bullet.y -= bullet.speed; }
			else if(bullet.direction == 1) { bullet.y += bullet.speed; }
			else if(bullet.direction == 2) { bullet.x -= bullet.speed; }
			else                           { bullet.x += bullet.speed; }

			if(bullet.x < 0 || bullet.y < 0 || bullet.x > getWidth() || bullet.y > getHeight()) {
				bullets.remove(i);
				continue;
			}

			boolean bulletRemoved = false;

			// Hit obstacles
			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obstacle = obstacles.get(j);
				if(obstacle instanceof Bush || obstacle instanceof Water) { continue; }

				if(bullet.x < obstacle.x + obstacle.width  && bullet.x + bullet.width  > obstacle.x &&
				   bullet.y < obstacle.y + obstacle.height && bullet.y + bullet.height > obstacle.y) {
					if(obstacle instanceof BrickWall) {
						obstacles.remove(j);
					} else if(obstacle instanceof SteelWall && playerTank.canDestroySteel()) {
						// Star level 3: player bullets can destroy steel walls
						obstacles.remove(j);
					}
					bullets.remove(i);
					bulletRemoved = true;
					break;
				}
			}
			if(bulletRemoved) { continue; }

			// Hit eagle
			if(eagle != null && !eagle.isDestroyed()) {
				if(bullet.x < eagle.x + eagle.width  && bullet.x + bullet.width  > eagle.x &&
				   bullet.y < eagle.y + eagle.height && bullet.y + bullet.height > eagle.y) {
					eagle.setDestroyed(true);
					bullets.remove(i);
					bulletRemoved = true;
				}
			}
			if(bulletRemoved) { continue; }

			// Hit enemies
			for(int j = enemies.size() - 1; j >= 0; j--) {
				EnemyTank enemy = enemies.get(j);
				if(bullet.x < enemy.x + enemy.width  && bullet.x + bullet.width  > enemy.x &&
				   bullet.y < enemy.y + enemy.height && bullet.y + bullet.height > enemy.y) {
					enemy.hp -= bullet.damage;
					if(enemy.hp <= 0) {
						// Show score popup at the center of the destroyed enemy
						int popupX = enemy.x + (enemy.width  / 2) - 12;
						int popupY = enemy.y + (enemy.height / 2);
						scorePopups.add(new int[]{popupX, popupY, 100, 60});
						// 30% chance to drop a power-up at the enemy's position
						if(Math.random() < 0.30) {
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

	// ---------------------------------------------------------------
	// Moves enemy bullets and checks all collisions
	private void updateEnemyBullets() {
		for(int i = enemyBullets.size() - 1; i >= 0; i--) {
			Bullet bullet = enemyBullets.get(i);

			if(bullet.direction == 0)      { bullet.y -= bullet.speed; }
			else if(bullet.direction == 1) { bullet.y += bullet.speed; }
			else if(bullet.direction == 2) { bullet.x -= bullet.speed; }
			else                           { bullet.x += bullet.speed; }

			if(bullet.x < 0 || bullet.y < 0 || bullet.x > getWidth() || bullet.y > getHeight()) {
				enemyBullets.remove(i);
				continue;
			}

			boolean bulletRemoved = false;

			// Hit obstacles
			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obstacle = obstacles.get(j);
				if(obstacle instanceof Bush || obstacle instanceof Water) { continue; }

				if(bullet.x < obstacle.x + obstacle.width  && bullet.x + bullet.width  > obstacle.x &&
				   bullet.y < obstacle.y + obstacle.height && bullet.y + bullet.height > obstacle.y) {
					if(obstacle instanceof BrickWall) { obstacles.remove(j); }
					enemyBullets.remove(i);
					bulletRemoved = true;
					break;
				}
			}
			if(bulletRemoved) { continue; }

			// Hit eagle
			if(eagle != null && !eagle.isDestroyed()) {
				if(bullet.x < eagle.x + eagle.width  && bullet.x + bullet.width  > eagle.x &&
				   bullet.y < eagle.y + eagle.height && bullet.y + bullet.height > eagle.y) {
					eagle.setDestroyed(true);
					enemyBullets.remove(i);
					bulletRemoved = true;
				}
			}
			if(bulletRemoved) { continue; }

			// Hit player (only if not invincible and shield is not active)
			if(playerInvincibility == 0 && !playerTank.isShieldActive()) {
				if(bullet.x < playerTank.x + playerTank.width  && bullet.x + bullet.width  > playerTank.x &&
				   bullet.y < playerTank.y + playerTank.height && bullet.y + bullet.height > playerTank.y) {
					playerLives--;
					hud.setLives(playerLives);
					enemyBullets.remove(i);
					respawnPlayer();
				}
			}
		}
	}

	// ---------------------------------------------------------------
	// Counts down each popup's lifetime and removes expired ones
	private void updateScorePopups() {
		for(int i = scorePopups.size() - 1; i >= 0; i--) {
			int[] popup = scorePopups.get(i);
			popup[3]--; // decrement framesLeft
			if(popup[3] <= 0) {
				scorePopups.remove(i);
			}
		}
	}

	// ---------------------------------------------------------------
	// Called from GameFrame when the player picks a difficulty in Options
	// 0 = Easy, 1 = Medium, 2 = Hard
	public void setDifficulty(int diff) {
		this.difficulty = diff;
		if(diff == 0) {
			maxOnScreen = 3; // Easy: fewer enemies visible at once
		} else if(diff == 2) {
			maxOnScreen = 6; // Hard: more pressure
		} else {
			maxOnScreen = 4; // Medium: default
		}
	}

	// ---------------------------------------------------------------
	// Advances to the next level carrying score and lives.
	// Levels 1-2 → next numbered level.
	// Level 3+ → repeats level 3 but infiniteRound increments so enemies speed up.
	private void advanceToNextLevel() {
		int savedScore        = score;
		int savedLives        = playerLives;
		int savedInfiniteRound;

		String nextPath;
		if(currentLevel < 3) {
			savedInfiniteRound = 0; // still in normal campaign
			nextPath = "maps" + File.separator + "level" + (currentLevel + 1) + ".csv";
		} else {
			savedInfiniteRound = infiniteRound + 1; // one more loop past level 3
			nextPath = "maps" + File.separator + "level3.csv";
		}

		loadMapFromFile(nextPath); // resets score, lives and infiniteRound

		// Restore carried state
		score         = savedScore;
		playerLives   = savedLives;
		infiniteRound = savedInfiniteRound;
		hud.setScore(score);
		hud.setLives(playerLives);
	}

	// ---------------------------------------------------------------
	// Returns enemy movement speed for the current level.
	// Level 1 → 1, Level 2 → 2, Level 3+ → 3.
	// Each extra infinite loop past level 3 adds +1 (capped at 6).
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
		if(totalSpeed > 6) { totalSpeed = 6; }
		return totalSpeed;
	}

	// ---------------------------------------------------------------
	// Spawns one enemy at the next spawn position (cycles through 3 points)
	private void spawnEnemy() {
		int spawnX = SPAWN_X[spawnedEnemies % 3];
		int spawnY = 0;

		// Skip if spawn point is occupied
		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank enemy = enemies.get(i);
			if(Math.abs(enemy.x - spawnX) < 40 && enemy.y < 40) {
				return;
			}
		}

		enemies.add(new EnemyTank(spawnX, spawnY, getEnemySpeedForLevel()));
		spawnedEnemies++;
		// System.out.println("Enemy spawned. On screen: " + enemies.size() + " / Spawned: " + spawnedEnemies);
		hud.setEnemyCount(totalEnemies - spawnedEnemies + enemies.size());
	}

	// ---------------------------------------------------------------
	// Returns the direction (0=Up 1=Down 2=Left 3=Right) that points toward the player
	private int aimAtPlayer(EnemyTank enemy) {
		int horizontalDiff = playerTank.x - enemy.x;
		int verticalDiff   = playerTank.y - enemy.y;

		if(Math.abs(horizontalDiff) > Math.abs(verticalDiff)) {
			return horizontalDiff > 0 ? 3 : 2; // right or left
		} else {
			return verticalDiff > 0 ? 1 : 0; // down or up
		}
	}

	// ---------------------------------------------------------------
	// Returns next movement direction — 65% toward the player, 35% random
	private int pickDirectionTowardPlayer(EnemyTank enemy) {
		if(Math.random() < 0.35) {
			return (int)(Math.random() * 4);
		}
		return aimAtPlayer(enemy);
	}

	// ---------------------------------------------------------------
	// Moves all enemies and handles their shooting
	private void updateEnemies() {
		// Clock power-up: skip all enemy movement and shooting while timer is active
		if(clockTimer > 0) {
			return;
		}
		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank enemy = enemies.get(i);

			if(enemy.fireCooldown > 0) { enemy.fireCooldown--; }
			if(enemy.moveCooldown > 0) { enemy.moveCooldown--; }

			// Time to pick a new direction — biased toward the player
			if(enemy.moveCooldown == 0) {
				enemy.direction    = pickDirectionTowardPlayer(enemy);
				enemy.moveCooldown = 60 + (int)(Math.random() * 120);
			}

			// Try to move in the current direction
			int newX = enemy.x;
			int newY = enemy.y;

			if(enemy.direction == 0)      { newY -= enemy.speed; }
			else if(enemy.direction == 1) { newY += enemy.speed; }
			else if(enemy.direction == 2) { newX -= enemy.speed; }
			else                          { newX += enemy.speed; }

			if(enemyCollidesWithSolid(enemy, newX, newY)) {
				// Blocked — try again toward the player with a short cooldown
				enemy.direction    = pickDirectionTowardPlayer(enemy);
				enemy.moveCooldown = 20 + (int)(Math.random() * 40);
			} else {
				enemy.x = newX;
				enemy.y = newY;
			}

			// Fire — always aimed directly at the player
			if(enemy.fireCooldown == 0) {
				enemy.direction    = aimAtPlayer(enemy);
				fireEnemyBullet(enemy);
				if(difficulty == 0) {
					enemy.fireCooldown = 90 + (int)(Math.random() * 90); // Easy: slow fire
				} else if(difficulty == 2) {
					enemy.fireCooldown = 20 + (int)(Math.random() * 30); // Hard: fast fire
				} else {
					enemy.fireCooldown = 45 + (int)(Math.random() * 60); // Medium: normal
				}
			}
		}
	}

	// ---------------------------------------------------------------
	// Resets player to spawn position with a short invincibility window
	private void respawnPlayer() {
		if(playerLives <= 0) {
			triggerGameOver();
			return;
		}
		playerTank.x        = 4 * TILE_SIZE;
		playerTank.y        = 13 * TILE_SIZE;
		playerDir           = 0;
		playerInvincibility = 120;
		playerTank.resetStarLevel();
		bullets.clear();
	}

	// ---------------------------------------------------------------
	private void triggerGameOver() {
		if(gameOver) { return; }
		gameOver      = true;
		gameOverSlide = PANEL_HEIGHT; // sprite starts below the visible area
		gameOverDelay = 60;           // 1 second pause after slide stops before dialog
	}

	// ---------------------------------------------------------------
	// Appends one line (name,score,date,time) to scores.csv in the working directory
	private void saveScore(String playerName, int finalScore) {
		Date now = new Date();
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(now);
		String timeStr = new SimpleDateFormat("HH:mm:ss").format(now);

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("scores.csv", true));
			writer.write(playerName + "," + finalScore + "," + dateStr + "," + timeStr);
			writer.newLine();
		} catch(IOException ex) {
			ex.printStackTrace();
		} finally {
			if(writer != null) {
				try { writer.close(); } catch(IOException ex) { ex.printStackTrace(); }
			}
		}
	}

	// ---------------------------------------------------------------
	// Spawns a power-up of a random type at the tile nearest to (pixelX, pixelY)
	private void spawnRandomPowerUp(int pixelX, int pixelY) {
		int tileX = pixelX / TILE_SIZE;
		int tileY = pixelY / TILE_SIZE;
		// Clamp to valid tile range
		if(tileX < 0) { tileX = 0; }
		if(tileY < 0) { tileY = 0; }
		if(tileX > 15) { tileX = 15; }
		if(tileY > 14) { tileY = 14; }
		int randomType = (int)(Math.random() * 6);
		powerUps.add(new PowerUp(tileX, tileY, randomType));
	}

	// ---------------------------------------------------------------
	// Updates all power-up blink animations and checks player collection
	private void updatePowerUps() {
		for(int i = powerUps.size() - 1; i >= 0; i--) {
			PowerUp powerUp = powerUps.get(i);
			powerUp.update();

			// Check collision with player tank
			if(powerUp.getBounds().intersects(
					new java.awt.Rectangle(playerTank.x, playerTank.y, playerTank.width, playerTank.height))) {
				applyPowerUp(powerUp.getPowerUpType());
				powerUps.remove(i);
			}
		}
	}

	// ---------------------------------------------------------------
	// Applies the collected power-up effect to the game state
	private void applyPowerUp(int powerUpType) {
		// System.out.println("Power-up collected: " + powerUpType);
		if(powerUpType == PowerUp.TANK) {
			// Tank: +1 life
			playerLives++;
			hud.setLives(playerLives);
		} else if(powerUpType == PowerUp.STAR) {
			// Star: upgrade player tank (up to level 3)
			playerTank.addStar();
		} else if(powerUpType == PowerUp.BOMB) {
			// Bomb: destroy all enemies currently on screen instantly
			for(int i = enemies.size() - 1; i >= 0; i--) {
				EnemyTank enemy = enemies.get(i);
				int popupX = enemy.x + (enemy.width  / 2) - 12;
				int popupY = enemy.y + (enemy.height / 2);
				scorePopups.add(new int[]{popupX, popupY, 100, 60});
				score += 100;
			}
			enemies.clear();
			enemyBullets.clear();
			hud.setScore(score);
			hud.setEnemyCount(totalEnemies - spawnedEnemies);
		} else if(powerUpType == PowerUp.CLOCK) {
			// Clock: freeze all enemies for CLOCK_DURATION ticks
			clockTimer = CLOCK_DURATION;
		} else if(powerUpType == PowerUp.SHOVEL) {
			// Shovel: surround eagle with steel walls temporarily
			activateShovel();
			shovelTimer = SHOVEL_DURATION;
		} else if(powerUpType == PowerUp.SHIELD) {
			// Shield (Helmet): player becomes invulnerable for a duration
			playerTank.activateShield();
		}
	}

	// ---------------------------------------------------------------
	// Replaces the protective ring around the eagle with steel walls temporarily
	private void activateShovel() {
		// Eagle is at EAGLE_COL=7, EAGLE_ROW=14.
		// Protective ring: 3 tiles on top (row EAGLE_ROW-2, cols 6-8)
		//                  2 tiles on sides (row EAGLE_ROW-1, cols 6 and 8)
		int baseCol = EAGLE_COL - 1; // col 6
		int topRow  = EAGLE_ROW - 2; // row 12
		int sideRow = EAGLE_ROW - 1; // row 13
		int[][] shovelTiles = {
			{baseCol,     topRow},  // {6, 12}
			{baseCol + 1, topRow},  // {7, 12}
			{baseCol + 2, topRow},  // {8, 12}
			{baseCol,     sideRow}, // {6, 13}
			{baseCol + 2, sideRow}  // {8, 13}
		};
		// Save originals so we can restore them
		savedEagleArea = new int[shovelTiles.length][2];
		for(int i = 0; i < shovelTiles.length; i++) {
			savedEagleArea[i][0] = shovelTiles[i][0];
			savedEagleArea[i][1] = shovelTiles[i][1];
		}
		// Remove existing brick/steel at these tiles then add steel walls
		for(int i = 0; i < shovelTiles.length; i++) {
			int tileX   = shovelTiles[i][0];
			int tileY   = shovelTiles[i][1];
			int pixelX  = tileX * TILE_SIZE;
			int pixelY  = tileY * TILE_SIZE;
			// Remove any existing obstacle at this position
			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obs = obstacles.get(j);
				if(obs.x == pixelX && obs.y == pixelY) {
					obstacles.remove(j);
				}
			}
			// Place steel wall
			obstacles.add(new SteelWall(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
		}
	}

	// ---------------------------------------------------------------
	// Restores the eagle area tiles to brick walls after shovel timer expires
	private void deactivateShovel() {
		if(savedEagleArea == null) { return; }
		for(int i = 0; i < savedEagleArea.length; i++) {
			int tileX   = savedEagleArea[i][0];
			int tileY   = savedEagleArea[i][1];
			int pixelX  = tileX * TILE_SIZE;
			int pixelY  = tileY * TILE_SIZE;
			// Remove the steel wall
			for(int j = obstacles.size() - 1; j >= 0; j--) {
				Obstacle obs = obstacles.get(j);
				if(obs.x == pixelX && obs.y == pixelY && obs instanceof SteelWall) {
					obstacles.remove(j);
				}
			}
			// Restore brick wall
			obstacles.add(new BrickWall(pixelX, pixelY, TILE_SIZE, TILE_SIZE));
		}
		savedEagleArea = null;
	}

	// ---------------------------------------------------------------
	private void triggerStageClear() {
		if(stageClear) { return; }
		stageClear      = true;
		stageClearDelay = 150; // ~2.5 seconds — lets the overlay show before the dialog appears
	}

	// ---------------------------------------------------------------
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

		if(fireCooldown > 0)        { fireCooldown--; }
		if(playerInvincibility > 0) { playerInvincibility--; }

		// Power-up timers
		playerTank.updatePowerUps();
		if(clockTimer > 0) { clockTimer--; }
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

		// Spawn next enemy when ready
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

		// Game state checks
		if(eagle != null && eagle.isDestroyed()) {
			triggerGameOver();
		}
		if(spawnedEnemies >= totalEnemies && enemies.isEmpty() && !gameOver && !stageClear) {
			triggerStageClear();
		}
	}

	// ---------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// 1) Brick, Steel, Water — drawn under everything
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

		// 2) Eagle
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

		// 3) Enemy tanks
		BufferedImage[] enemySprites = {imgEnemyUp, imgEnemyDown, imgEnemyLeft, imgEnemyRight};
		for(int i = 0; i < enemies.size(); i++) {
			EnemyTank enemy      = enemies.get(i);
			BufferedImage sprite = enemySprites[enemy.direction];
			if(sprite != null) {
				g.drawImage(sprite, enemy.x, enemy.y, enemy.width, enemy.height, null);
			} else {
				g.setColor(Color.GRAY);
				g.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
			}
		}

		// 4a) Power-ups — drawn above ground tiles, below player tank
		for(int i = 0; i < powerUps.size(); i++) {
			powerUps.get(i).draw(g);
		}

		// 4) Player tank — blinks every 5 frames when invincible
		if(playerInvincibility == 0 || (playerInvincibility / 5) % 2 == 0) {
			BufferedImage[] playerSprites = {imgPlayerUp, imgPlayerDown, imgPlayerLeft, imgPlayerRight};
			BufferedImage currentSprite   = playerSprites[playerDir];
			if(currentSprite != null) {
				g.drawImage(currentSprite, playerTank.x, playerTank.y, playerTank.width, playerTank.height, null);
			} else {
				g.setColor(Color.YELLOW);
				g.fillRect(playerTank.x, playerTank.y, playerTank.width, playerTank.height);
			}

			// Shield (Helmet) effect: pulsing cyan ring around the tank
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

		// 5) Player bullets (white)
		g.setColor(Color.WHITE);
		for(int i = 0; i < bullets.size(); i++) {
			Bullet bullet = bullets.get(i);
			g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
		}

		// 6) Enemy bullets (red)
		g.setColor(Color.RED);
		for(int i = 0; i < enemyBullets.size(); i++) {
			Bullet bullet = enemyBullets.get(i);
			g.fillRect(bullet.x, bullet.y, bullet.width, bullet.height);
		}

		// 7) Bush drawn last — hides tanks underneath
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

		// 8) Score popup floaters — yellow "+100" text that fades upward over each kill
		g.setFont(new Font("Monospaced", Font.BOLD, 14));
		for(int i = 0; i < scorePopups.size(); i++) {
			int[] popup = scorePopups.get(i);
			int popupX      = popup[0];
			int popupY      = popup[1];
			int popupValue  = popup[2];
			int framesLeft  = popup[3];
			// Drift upward as frames count down (starts at 60, drifts 20px total)
			int driftY = (60 - framesLeft) / 3;
			// Fade out in the last 20 frames
			int alpha = (framesLeft < 20) ? (framesLeft * 255 / 20) : 255;
			g.setColor(new Color(255, 220, 0, alpha));
			g.drawString("+" + popupValue, popupX, popupY - driftY);
		}

		// 9) Stage Clear overlay — drawn over the game when all enemies are defeated
		if(stageClear) {
			// Dark semi-transparent band across the center
			g.setColor(new Color(0, 0, 0, 180));
			g.fillRect(0, PANEL_HEIGHT / 2 - 50, PANEL_WIDTH, 100);

			g.setColor(new Color(220, 200, 0)); // yellow
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

		// 10) GAME OVER — sprite slide-in animation
		if(gameOver && gameOverSlide >= 0) {
			// Dark tint across the whole panel so the game dims behind the sliding banner
			g.setColor(new Color(0, 0, 0, 140));
			g.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);

			int spriteX = (PANEL_WIDTH - GAMEOVER_SPRITE_W) / 2;

			if(imgTextGame != null && imgTextOver != null) {
				g.drawImage(imgTextGame, spriteX, gameOverSlide,
					GAMEOVER_SPRITE_W, GAMEOVER_SPRITE_H, null);
				g.drawImage(imgTextOver, spriteX, gameOverSlide + GAMEOVER_SPRITE_H,
					GAMEOVER_SPRITE_W, GAMEOVER_SPRITE_H, null);
			} else {
				// Fallback: large red text centered on screen
				g.setColor(Color.RED);
				g.setFont(new Font("Monospaced", Font.BOLD, 56));
				FontMetrics fm = g.getFontMetrics();
				String fallback = "GAME OVER";
				g.drawString(fallback,
					(PANEL_WIDTH - fm.stringWidth(fallback)) / 2,
					PANEL_HEIGHT / 2 + 20);
			}
		}

		// 11) Stage intro overlay — shown for ~3 seconds when a new map loads
		if(introTimer > 0) {
			// Dark band across the center 80px tall
			g.setColor(new Color(0, 0, 0, 210));
			g.fillRect(0, PANEL_HEIGHT / 2 - 40, PANEL_WIDTH, 80);

			g.setColor(new Color(190, 190, 190));
			g.setFont(new Font("Monospaced", Font.BOLD, 14));
			FontMetrics stageLabelMetrics = g.getFontMetrics();
			String stageLabel = "STAGE";
			g.drawString(stageLabel,
				(PANEL_WIDTH - stageLabelMetrics.stringWidth(stageLabel)) / 2,
				PANEL_HEIGHT / 2 - 8);

			String levelStr = (currentLevel > 0) ? String.valueOf(currentLevel) : "?";
			g.setColor(Color.WHITE);
			g.setFont(new Font("Monospaced", Font.BOLD, 28));
			FontMetrics levelMetrics = g.getFontMetrics();
			g.drawString(levelStr,
				(PANEL_WIDTH - levelMetrics.stringWidth(levelStr)) / 2,
				PANEL_HEIGHT / 2 + 26);
		}
	}
}

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HUDPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final int HUD_WIDTH  = 160;
	private static final int HUD_HEIGHT = 600;
	private static final int MAX_ENEMIES = 20;

	private static final int BORDER_W   = 16;
	private static final int CONTENT_W  = HUD_WIDTH - BORDER_W;

	private static final int ICON_W     = 14;
	private static final int ICON_H     = 18;
	private static final int ICON_GAP   = 3;

	private static final int ENEMY_LABEL_Y = 16;
	private static final int GRID_Y        = 24;
	private static final int SEP1_Y        = 232;
	private static final int LABEL_1P_Y    = 250;
	private static final int SCORE_Y       = 268;
	private static final int SEP2_Y        = 282;
	private static final int LIVES_Y       = 308;
	private static final int SEP3_Y        = 332;
	private static final int FLAG_Y        = 352;
	private static final int STAGE_NUM_Y   = 400;
	private static final int SEP4_Y        = 430;
	private static final int PAUSE_BTN_Y   = 448;

	private int lives;
	private int score;
	private int level;
	private int enemyCount;

	private JButton pauseButton;

	private BufferedImage imgEnemyActive;
	private BufferedImage imgEnemyDead;
	private BufferedImage imgLifeIcon;
	private BufferedImage imgFlag;
	private BufferedImage imgBorderStrip;

	public HUDPanel(int lives, int score, int level) {
		// TODO Auto-generated constructor stub
		this.lives      = lives;
		this.score      = score;
		this.level      = level;
		this.enemyCount = MAX_ENEMIES;

		setPreferredSize(new Dimension(HUD_WIDTH, HUD_HEIGHT));
		setBackground(new Color(99, 99, 99));
		setLayout(null);

		loadSprites();

		pauseButton = new JButton("PAUSE");
		pauseButton.setBounds(8, PAUSE_BTN_Y, CONTENT_W - 16, 26);
		pauseButton.setBackground(new Color(50, 50, 50));
		pauseButton.setForeground(new Color(220, 200, 0));
		pauseButton.setFont(new Font("Monospaced", Font.BOLD, 10));
		pauseButton.setFocusPainted(false);
		pauseButton.setBorder(BorderFactory.createLineBorder(new Color(181, 49, 33), 1));
		add(pauseButton);
	}

	private void loadSprites() {
		try {
			imgEnemyActive = ImageIO.read(new File("images/hudEnemyTank.png"));
		} catch(IOException ex) {
			imgEnemyActive = null;
		}
		try {
			imgLifeIcon = ImageIO.read(new File("images/hudPlayerLife.png"));
		} catch(IOException ex) {
			imgLifeIcon = null;
		}
		try {
			imgFlag = ImageIO.read(new File("images/hudFlag.png"));
		} catch(IOException ex) {
			imgFlag = null;
		}
		try {
			imgBorderStrip = ImageIO.read(new File("images/hudBorderStrip.png"));
		} catch(IOException ex) {
			imgBorderStrip = null;
		}

		imgEnemyDead = makeDimCopy(imgEnemyActive);
	}

	private BufferedImage makeDimCopy(BufferedImage src) {
		if(src == null) {
			return null;
		}
		BufferedImage dim = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < src.getHeight(); y++) {
			for(int x = 0; x < src.getWidth(); x++) {
				int argb = src.getRGB(x, y);
				int a = (argb >> 24) & 0xFF;
				int r = (argb >> 16) & 0xFF;
				int gg = (argb >> 8)  & 0xFF;
				int b =   argb        & 0xFF;
				r  = r  / 6;
				gg = gg / 6;
				b  = b  / 6;
				dim.setRGB(x, y, (a << 24) | (r << 16) | (gg << 8) | b);
			}
		}
		return dim;
	}

	public JButton getPauseButton() {
		return pauseButton;
	}

	public void setLives(int lives) {
		this.lives = lives;
		repaint();
	}

	public void setScore(int score) {
		this.score = score;
		repaint();
	}

	public void setLevel(int level) {
		this.level = level;
		repaint();
	}

	public void setEnemyCount(int count) {
		this.enemyCount = count;
		repaint();
	}

	private void drawSep(Graphics g, int y) {
		g.setColor(new Color(55, 55, 55));
		g.fillRect(4, y, CONTENT_W - 8, 1);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int centerX = CONTENT_W / 2;

		if(imgBorderStrip != null) {
			g.drawImage(imgBorderStrip, CONTENT_W, 0, BORDER_W, HUD_HEIGHT, null);
		} else {
			for(int y = 0; y < HUD_HEIGHT; y += 8) {
				g.setColor(new Color(181, 49, 33));
				g.fillRect(CONTENT_W, y, BORDER_W, 4);
				g.setColor(new Color(80, 20, 10));
				g.fillRect(CONTENT_W, y + 4, BORDER_W, 4);
			}
		}

		g.setColor(new Color(210, 210, 210));
		g.setFont(new Font("Monospaced", Font.BOLD, 9));
		FontMetrics fm = g.getFontMetrics();
		String enemyLabel = "ENEMY";
		g.drawString(enemyLabel, centerX - fm.stringWidth(enemyLabel) / 2, ENEMY_LABEL_Y);

		int gridTotalW = ICON_W * 2 + ICON_GAP;
		int gridStartX = centerX - gridTotalW / 2;

		for(int slot = 0; slot < MAX_ENEMIES; slot++) {
			int col;
			if(slot % 2 == 0) {
				col = 1;
			} else {
				col = 0;
			}
			int row   = slot / 2;
			int iconX = gridStartX + col * (ICON_W + ICON_GAP);
			int iconY = GRID_Y + row * (ICON_H + 2);
			boolean active = slot < enemyCount;

			if(active && imgEnemyActive != null) {
				g.drawImage(imgEnemyActive, iconX, iconY, ICON_W, ICON_H, null);
			} else if(!active && imgEnemyDead != null) {
				g.drawImage(imgEnemyDead, iconX, iconY, ICON_W, ICON_H, null);
			} else {
				if(active) {
					g.setColor(new Color(181, 49, 33));
				} else {
					g.setColor(new Color(60, 60, 60));
				}
				g.fillRect(iconX + 1, iconY + 1, ICON_W - 2, ICON_H - 2);
			}
		}

		drawSep(g, SEP1_Y);

		g.setColor(new Color(220, 200, 0));
		g.setFont(new Font("Monospaced", Font.BOLD, 11));
		g.drawString("I-", 6, LABEL_1P_Y);

		FontMetrics hiMetrics = g.getFontMetrics();
		String hiLabel = "HI-20000";
		g.drawString(hiLabel, CONTENT_W - 4 - hiMetrics.stringWidth(hiLabel), LABEL_1P_Y);

		g.setColor(new Color(181, 49, 33));
		g.setFont(new Font("Monospaced", Font.BOLD, 13));
		String scoreStr = String.format("%06d", score);
		FontMetrics scoreMetrics = g.getFontMetrics();
		g.drawString(scoreStr, CONTENT_W - 4 - scoreMetrics.stringWidth(scoreStr), SCORE_Y);

		drawSep(g, SEP2_Y);

		int lifeIconY = LIVES_Y - 18;
		if(imgLifeIcon != null) {
			g.drawImage(imgLifeIcon, 8, lifeIconY, 18, 18, null);
		} else {
			g.setColor(new Color(220, 200, 0));
			g.fillRect(8, lifeIconY, 14, 14);
		}

		g.setColor(new Color(210, 210, 210));
		g.setFont(new Font("Monospaced", Font.BOLD, 13));
		g.drawString("x" + lives, 30, LIVES_Y);

		drawSep(g, SEP3_Y);

		if(imgFlag != null) {
			g.drawImage(imgFlag, centerX - 16, FLAG_Y, 32, 26, null);
		} else {
			g.setColor(new Color(181, 49, 33));
			g.fillRect(centerX - 8, FLAG_Y, 14, 12);
		}

		g.setColor(new Color(210, 210, 210));
		g.setFont(new Font("Monospaced", Font.BOLD, 20));
		FontMetrics stageMetrics = g.getFontMetrics();
		String levelStr = String.format("%02d", level);
		g.drawString(levelStr, centerX - stageMetrics.stringWidth(levelStr) / 2, STAGE_NUM_Y);

		drawSep(g, SEP4_Y);
	}
}

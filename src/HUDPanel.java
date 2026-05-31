import javax.swing.*;
import java.awt.*;

public class HUDPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	// HUD dimensions — sits to the right of the 640×600 game area
	private static final int HUD_WIDTH  = 160;
	private static final int HUD_HEIGHT = 600;

	// Mini tank icon dimensions (drawn with Java2D, matching original HUD style)
	private static final int ICON_W     = 16;
	private static final int ICON_H     = 14;
	private static final int ICON_GAP_X = 6;
	private static final int ICON_GAP_Y = 4;

	// Max enemy slots shown in the grid
	private static final int MAX_ENEMIES = 20;

	// Section start Y positions — computed once so they stay consistent
	private static final int SECTION_ENEMY_Y  = 20;   // "ENEMY" label baseline
	private static final int GRID_START_Y     = 28;   // icon grid top
	// grid takes 10 rows × (ICON_H + ICON_GAP_Y) = 10 × 18 = 180px → ends at y=208
	private static final int SEP1_Y           = 212;
	private static final int SECTION_1P_Y     = 228;  // "1P" baseline
	private static final int SEP2_Y           = 278;
	private static final int SECTION_LIVES_Y  = 292;  // lives baseline
	private static final int SEP3_Y           = 318;
	private static final int SECTION_STAGE_Y  = 332;  // "STAGE" baseline
	// stage box: y=338, h=36 → bottom=374
	private static final int SEP4_Y           = 382;
	private static final int PAUSE_BTN_Y      = 400;  // tight below last separator

	private int lives;
	private int score;
	private int level;
	private int enemyCount; // how many enemy slots are still active

	private JButton pauseButton;

	public HUDPanel(int lives, int score, int level) {
		// TODO Auto-generated constructor stub
		this.lives      = lives;
		this.score      = score;
		this.level      = level;
		this.enemyCount = MAX_ENEMIES;

		setPreferredSize(new Dimension(HUD_WIDTH, HUD_HEIGHT));
		setBackground(new Color(38, 38, 38));
		setLayout(null);

		// Pause button — placed right below the stage section
		pauseButton = new JButton("II  PAUSE");
		pauseButton.setBounds(16, PAUSE_BTN_Y, HUD_WIDTH - 32, 28);
		pauseButton.setBackground(new Color(55, 55, 55));
		pauseButton.setForeground(new Color(220, 200, 0));
		pauseButton.setFont(new Font("Monospaced", Font.BOLD, 11));
		pauseButton.setFocusPainted(false);
		pauseButton.setBorder(BorderFactory.createLineBorder(new Color(120, 110, 80), 1));
		add(pauseButton);
	}

	// ---------------------------------------------------------------
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

	// ---------------------------------------------------------------
	// Draws a mini enemy tank icon (facing downward) at (drawX, drawY)
	// active = orange (enemy still present), inactive = dark (slot used)
	private void drawEnemyIcon(Graphics g, int drawX, int drawY, boolean active) {
		Color bodyColor  = active ? new Color(210, 90, 0) : new Color(52, 52, 52);
		Color trackColor = active ? new Color(140, 55, 0) : new Color(38, 38, 38);

		// Tracks
		g.setColor(trackColor);
		g.fillRect(drawX,              drawY + 3, 3, ICON_H - 3);
		g.fillRect(drawX + ICON_W - 3, drawY + 3, 3, ICON_H - 3);

		// Hull
		g.setColor(bodyColor);
		g.fillRect(drawX + 3, drawY + 5, ICON_W - 6, ICON_H - 5);

		// Turret
		g.fillRect(drawX + 5, drawY + 1, ICON_W - 10, 6);

		// Cannon pointing downward
		g.fillRect(drawX + (ICON_W / 2) - 1, drawY + ICON_H - 4, 2, 4);
	}

	// Draws a mini player tank icon (facing upward) at (drawX, drawY)
	private void drawPlayerIcon(Graphics g, int drawX, int drawY) {
		Color bodyColor  = new Color(220, 200, 0);
		Color trackColor = new Color(160, 140, 0);

		g.setColor(trackColor);
		g.fillRect(drawX,              drawY + 3, 3, ICON_H - 3);
		g.fillRect(drawX + ICON_W - 3, drawY + 3, 3, ICON_H - 3);

		g.setColor(bodyColor);
		g.fillRect(drawX + 3, drawY + 3, ICON_W - 6, ICON_H - 3);
		g.fillRect(drawX + 5, drawY + 7, ICON_W - 10, 6);

		// Cannon pointing upward
		g.fillRect(drawX + (ICON_W / 2) - 1, drawY, 2, 5);
	}

	// ---------------------------------------------------------------
	// Draws a horizontal separator line
	private void drawSep(Graphics g, int y) {
		g.setColor(new Color(80, 80, 80));
		g.fillRect(10, y, HUD_WIDTH - 20, 1);
	}

	// ---------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
		                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int centerX = HUD_WIDTH / 2;

		// ---- LEFT ACCENT STRIP ----
		g.setColor(new Color(55, 50, 40));
		g.fillRect(0, 0, 4, HUD_HEIGHT);

		// ============================================================
		// ENEMY SECTION
		// ============================================================
		g.setColor(new Color(190, 190, 190));
		g.setFont(new Font("Monospaced", Font.BOLD, 10));
		FontMetrics enemyMetrics = g.getFontMetrics();
		String enemyLabel = "ENEMY";
		g.drawString(enemyLabel,
			centerX - enemyMetrics.stringWidth(enemyLabel) / 2,
			SECTION_ENEMY_Y);

		// 2-column grid — right column fills first (matches original NES style)
		int gridW      = ICON_W * 2 + ICON_GAP_X;
		int gridStartX = centerX - gridW / 2;

		for(int slot = 0; slot < MAX_ENEMIES; slot++) {
			// NES fills right column first: slot 0 → col 1 (right), slot 1 → col 0 (left)
			int col  = (slot % 2 == 0) ? 1 : 0;
			int row  = slot / 2;
			int iconX = gridStartX + col * (ICON_W + ICON_GAP_X);
			int iconY = GRID_START_Y + row * (ICON_H + ICON_GAP_Y);
			boolean active = slot < enemyCount;
			drawEnemyIcon(g, iconX, iconY, active);
		}

		drawSep(g, SEP1_Y);

		// ============================================================
		// 1P SECTION
		// ============================================================
		// "1P" label — yellow, left-aligned
		g.setColor(new Color(220, 200, 0));
		g.setFont(new Font("Monospaced", Font.BOLD, 14));
		g.drawString("1P", 16, SECTION_1P_Y);

		// "SCORE" sub-label — light grey
		g.setColor(new Color(160, 160, 160));
		g.setFont(new Font("Monospaced", Font.PLAIN, 9));
		g.drawString("SCORE", 16, SECTION_1P_Y + 14);

		// Score value — white, zero-padded to 6 digits, right-aligned
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.BOLD, 15));
		String scoreStr = String.format("%06d", score);
		FontMetrics scoreMetrics = g.getFontMetrics();
		g.drawString(scoreStr,
			HUD_WIDTH - 14 - scoreMetrics.stringWidth(scoreStr),
			SECTION_1P_Y + 32);

		drawSep(g, SEP2_Y);

		// ============================================================
		// LIVES SECTION
		// ============================================================
		drawPlayerIcon(g, 16, SECTION_LIVES_Y - 2);

		// "×" symbol
		g.setColor(new Color(180, 180, 180));
		g.setFont(new Font("Monospaced", Font.PLAIN, 11));
		g.drawString("x", 35, SECTION_LIVES_Y + 10);

		// Lives count
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.BOLD, 16));
		g.drawString(String.valueOf(lives), 47, SECTION_LIVES_Y + 12);

		drawSep(g, SEP3_Y);

		// ============================================================
		// STAGE SECTION
		// ============================================================
		g.setColor(new Color(170, 170, 170));
		g.setFont(new Font("Monospaced", Font.PLAIN, 9));
		FontMetrics stageLabelMetrics = g.getFontMetrics();
		String stageLabel = "STAGE";
		g.drawString(stageLabel,
			centerX - stageLabelMetrics.stringWidth(stageLabel) / 2,
			SECTION_STAGE_Y);

		// Stage number box
		int boxW = 52;
		int boxH = 34;
		int boxX = centerX - boxW / 2;
		int boxY = SECTION_STAGE_Y + 5;

		g.setColor(new Color(50, 50, 50));
		g.fillRect(boxX, boxY, boxW, boxH);
		g.setColor(new Color(100, 95, 70));
		g.drawRect(boxX, boxY, boxW, boxH);

		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.BOLD, 20));
		FontMetrics stageMetrics = g.getFontMetrics();
		String levelStr = String.format("%02d", level);
		g.drawString(levelStr,
			boxX + (boxW - stageMetrics.stringWidth(levelStr)) / 2,
			boxY + boxH - 7);

		drawSep(g, SEP4_Y);
	}
}

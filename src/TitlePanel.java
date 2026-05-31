import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class TitlePanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	// Panel fills the entire frame content area (game 640 + HUD 160 wide)
	private static final int PANEL_W       = 800;
	private static final int PANEL_H       = 600;

	// Title background: NES 256×224 scaled 2.5× = 640×560
	private static final int BG_W          = 640;
	private static final int BG_H          = 560;
	private static final int BG_OFFSET_X   = 0;
	private static final int BG_OFFSET_Y   = (PANEL_H - BG_H) / 2;  // = 20

	// Cursor sprite drawn size at 2.5×
	private static final int CURSOR_W      = 25;
	private static final int CURSOR_H      = 33;

	// Cursor X in panel coordinates (NES x=65 * 2.5 + BG_OFFSET_X = 162)
	private static final int CURSOR_X      = BG_OFFSET_X + 162;

	// Cursor Y (top edge) in panel coordinates for each menu row.
	// Formula: NES_top_y * 2.5 + BG_OFFSET_Y
	//   1 PLAYER      NES y=125 → 125*2.5 + 20 = 332
	//   2 PLAYERS     NES y=141 → 141*2.5 + 20 = 372
	//   CONSTRUCTION  NES y=157 → 157*2.5 + 20 = 412
	private static final int[] CURSOR_ROW_Y = { 332, 372, 412 };

	// Menu item indices
	private static final int ITEM_1PLAYER      = 0;
	private static final int ITEM_2PLAYERS     = 1;
	private static final int ITEM_CONSTRUCTION = 2;
	private static final int ITEM_COUNT        = 3;

	// Cursor blink interval in milliseconds
	private static final int BLINK_MS          = 500;

	// --- State ---
	private int     selectedItem  = ITEM_1PLAYER;
	private boolean cursorVisible = true;
	private boolean isRunning     = false;

	// --- Images ---
	private BufferedImage titleBackground;
	private BufferedImage cursorSprite;

	// --- Callback interface ---
	public interface TitleListener {
		void onOnePlayer();
		void onConstruction();
	}

	private TitleListener titleListener;

	// ------------------------------------------------------------------
	public TitlePanel(TitleListener listener) {
		// TODO Auto-generated constructor stub
		this.titleListener = listener;

		setPreferredSize(new Dimension(PANEL_W, PANEL_H));
		setBackground(Color.BLACK);
		setFocusable(true);

		loadImages();

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();
				if(key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
					selectedItem = (selectedItem - 1 + ITEM_COUNT) % ITEM_COUNT;
					cursorVisible = true;
					repaint();
				} else if(key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
					selectedItem = (selectedItem + 1) % ITEM_COUNT;
					cursorVisible = true;
					repaint();
				} else if(key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) {
					handleSelect();
				}
			}
		});
	}

	// ------------------------------------------------------------------
	// Loads title background and cursor sprite from the images/ folder
	private void loadImages() {
		try {
			titleBackground = ImageIO.read(new File("images/titleBackground.png"));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
		try {
			cursorSprite = ImageIO.read(new File("images/titleCursor.png"));
		} catch(IOException ex) {
			ex.printStackTrace();
		}
	}

	// ------------------------------------------------------------------
	// Fires the appropriate callback when the player presses Enter/Space
	private void handleSelect() {
		if(selectedItem == ITEM_1PLAYER) {
			titleListener.onOnePlayer();
		} else if(selectedItem == ITEM_2PLAYERS) {
			JOptionPane.showMessageDialog(this,
				"2 PLAYERS mode is not yet implemented.",
				"Not Available",
				JOptionPane.INFORMATION_MESSAGE);
		} else if(selectedItem == ITEM_CONSTRUCTION) {
			titleListener.onConstruction();
		}
	}

	// ------------------------------------------------------------------
	// Starts the background blink thread
	public void startThread() {
		if(isRunning) {
			return;
		}
		isRunning = true;
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
	}

	// Stops the blink thread
	public void stopThread() {
		isRunning = false;
	}

	// ------------------------------------------------------------------
	// Blink loop — toggles cursorVisible every BLINK_MS milliseconds
	@Override
	public void run() {
		while(isRunning) {
			cursorVisible = !cursorVisible;
			repaint();
			try {
				Thread.sleep(BLINK_MS);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// ------------------------------------------------------------------
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		// Black fill for the entire panel
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, PANEL_W, PANEL_H);

		// Title background scaled to fill panel
		if(titleBackground != null) {
			g.drawImage(titleBackground, BG_OFFSET_X, BG_OFFSET_Y, BG_W, BG_H, null);
		}

		// Blinking cursor next to the selected menu item
		if(cursorVisible && cursorSprite != null) {
			int cursorY = CURSOR_ROW_Y[selectedItem];
			g.drawImage(cursorSprite, CURSOR_X, cursorY, CURSOR_W, CURSOR_H, null);
		}
	}
}

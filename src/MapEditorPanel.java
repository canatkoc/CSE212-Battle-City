import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class MapEditorPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final int TILE_SIZE = 40;
	private static final int ROWS = 15;
	private static final int COLS = 16;

	private int[][] editorGrid;

	private int cursorRow = 0;
	private int cursorCol = 0;

	public MapEditorPanel() {
		// TODO Auto-generated constructor stub
		setPreferredSize(new Dimension(COLS * TILE_SIZE, ROWS * TILE_SIZE));
		setBackground(Color.BLACK);
		setFocusable(true);

		editorGrid = new int[ROWS][COLS];
		clearGrid();

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int key = e.getKeyCode();

				if(key == KeyEvent.VK_W && cursorRow > 0) {
					cursorRow--;
				} else if(key == KeyEvent.VK_S && cursorRow < ROWS - 1) {
					cursorRow++;
				} else if(key == KeyEvent.VK_A && cursorCol > 0) {
					cursorCol--;
				} else if(key == KeyEvent.VK_D && cursorCol < COLS - 1) {
					cursorCol++;
				} else if(key == KeyEvent.VK_SPACE) {
					cycleTile();
				}

				repaint();
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int col = e.getX() / TILE_SIZE;
				int row = e.getY() / TILE_SIZE;

				if(row >= 0 && row < ROWS && col >= 0 && col < COLS) {
					cursorRow = row;
					cursorCol = col;
					requestFocusInWindow();
					repaint();
				}
			}
		});
	}

	@Override
	public void addNotify() {
		super.addNotify();
		requestFocusInWindow();
	}

	private void cycleTile() {
		int current = editorGrid[cursorRow][cursorCol];

		if(current == 0) {
			editorGrid[cursorRow][cursorCol] = 1;
		} else if(current == 1) {
			editorGrid[cursorRow][cursorCol] = 2;
		} else if(current == 2) {
			editorGrid[cursorRow][cursorCol] = 4;
		} else if(current == 4) {
			editorGrid[cursorRow][cursorCol] = 3;
		} else if(current == 3) {
			editorGrid[cursorRow][cursorCol] = 0;
		}
	}

	public void clearGrid() {
		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				editorGrid[row][col] = 0;
			}
		}
		repaint();
	}

	public int[][] getGrid() {
		return editorGrid;
	}

	public void saveMap() {
		java.io.File mapsDir = new java.io.File("maps");
		if(!mapsDir.exists()) {
			mapsDir.mkdir();
		}

		JFileChooser fileChooser = new JFileChooser("maps");
		fileChooser.setDialogTitle("Save Map");
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Map File (*.csv)", "csv"));

		int result = fileChooser.showSaveDialog(this);
		if(result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		String filePath = fileChooser.getSelectedFile().getAbsolutePath();
		if(!filePath.endsWith(".csv")) {
			filePath = filePath + ".csv";
		}

		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filePath));
			for(int row = 0; row < ROWS; row++) {
				for(int col = 0; col < COLS; col++) {
					writer.write(String.valueOf(editorGrid[row][col]));
					if(col < COLS - 1) {
						writer.write(",");
					}
				}
				writer.newLine();
			}
			JOptionPane.showMessageDialog(this, "Map saved successfully.", "Saved",
					JOptionPane.INFORMATION_MESSAGE);
		} catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to save the map!", "Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			if(writer != null) {
				try {
					writer.close();
				} catch(IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public void loadMap() {
		JFileChooser fileChooser = new JFileChooser("maps");
		fileChooser.setDialogTitle("Load Map");
		fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Map File (*.csv)", "csv"));

		int result = fileChooser.showOpenDialog(this);
		if(result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		String filePath = fileChooser.getSelectedFile().getAbsolutePath();
		int[][] loadedGrid = new int[ROWS][COLS];

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line;
			int row = 0;
			while((line = reader.readLine()) != null && row < ROWS) {
				String[] parts = line.split(",");
				for(int col = 0; col < parts.length && col < COLS; col++) {
					loadedGrid[row][col] = Integer.parseInt(parts[col].trim());
				}
				row++;
			}
			editorGrid = loadedGrid;
			cursorRow = 0;
			cursorCol = 0;
			repaint();
		} catch(IOException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "Failed to load the map!", "Error", JOptionPane.ERROR_MESSAGE);
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

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		for(int row = 0; row < ROWS; row++) {
			for(int col = 0; col < COLS; col++) {
				int pixelX = col * TILE_SIZE;
				int pixelY = row * TILE_SIZE;
				int tile = editorGrid[row][col];

				if(tile == 1) {
					g.setColor(new Color(160, 82, 45));
				} else if(tile == 2) {
					g.setColor(new Color(140, 140, 140));
				} else if(tile == 3) {
					g.setColor(new Color(34, 139, 34));
				} else if(tile == 4) {
					g.setColor(new Color(30, 144, 255));
				} else {
					g.setColor(Color.BLACK);
				}
				g.fillRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);

				g.setColor(new Color(50, 50, 50));
				g.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
			}
		}

		int cursorX = cursorCol * TILE_SIZE;
		int cursorY = cursorRow * TILE_SIZE;
		g.setColor(Color.YELLOW);
		g.drawRect(cursorX, cursorY, TILE_SIZE - 1, TILE_SIZE - 1);
		g.setColor(Color.WHITE);
		g.drawRect(cursorX + 2, cursorY + 2, TILE_SIZE - 5, TILE_SIZE - 5);
	}
}

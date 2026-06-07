import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PowerUp extends GameObject {

	public static final int SHIELD = 0;
	public static final int CLOCK = 1;
	public static final int SHOVEL = 2;
	public static final int STAR = 3;
	public static final int BOMB = 4;
	public static final int TANK = 5;

	public static final int SIZE = 40;

	private static final int BLINK_TICKS = 20;

	private int powerUpType;
	private int tickCount;
	private boolean visible;

	private static BufferedImage[] images = null;
	private static final String[] IMAGE_FILES = {
		"images/powerup_shield.png",
		"images/powerup_clock.png",
		"images/powerup_shovel.png",
		"images/powerup_star.png",
		"images/powerup_bomb.png",
		"images/powerup_tank.png"
	};

	public PowerUp(int tileX, int tileY, int powerUpType) {
		// TODO Auto-generated constructor stub
		this.type = "PowerUp";
		this.powerUpType = powerUpType;
		this.x = tileX * SIZE;
		this.y = tileY * SIZE;
		this.width = SIZE;
		this.height = SIZE;
		this.tickCount = 0;
		this.visible = true;

		if(images == null) {
			loadImages();
		}
	}

	private static void loadImages() {
		images = new BufferedImage[IMAGE_FILES.length];
		for(int i = 0; i < IMAGE_FILES.length; i++) {
			try {
				images[i] = ImageIO.read(new File(IMAGE_FILES[i]));
			} catch(IOException ex) {
				images[i] = null;
			}
		}
	}

	public void update() {
		tickCount++;
		if(tickCount >= BLINK_TICKS) {
			tickCount = 0;
			visible = !visible;
		}
	}

	public void draw(Graphics g) {
		if(!visible) {
			return;
		}
		if(images != null && images[powerUpType] != null) {
			g.drawImage(images[powerUpType], x, y, width, height, null);
		} else {
			g.setColor(new Color(220, 180, 0));
			g.fillRect(x + 4, y + 4, width - 8, height - 8);
			g.setColor(Color.BLACK);
			g.drawRect(x + 4, y + 4, width - 8, height - 8);
		}
	}

	public int getPowerUpType() {
		return powerUpType;
	}

	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
}

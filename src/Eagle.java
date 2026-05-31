public class Eagle extends GameObject {

	private boolean destroyed = false;

	public Eagle(int x, int y, int width, int height) {
		// TODO Auto-generated constructor stub
		this.type = "Eagle";
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean isDestroyed() {
		return destroyed;
	}

	public void setDestroyed(boolean destroyed) {
		this.destroyed = destroyed;
	}
}

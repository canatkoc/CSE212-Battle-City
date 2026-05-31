public abstract class Obstacle extends GameObject{
	protected boolean isBreakable;
	
	public Obstacle(String type, int x, int y, int width, int height, boolean isBreakable) {
		// TODO Auto-generated constructor stub
		this.type = type;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.isBreakable = isBreakable;
	}
	
}
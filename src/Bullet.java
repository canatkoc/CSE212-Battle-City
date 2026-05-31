public class Bullet extends GameObject {

	protected int speed;
	protected int damage;
	protected int direction; // 0=Up 1=Down 2=Left 3=Right

	public Bullet(int x, int y, int width, int height, int speed, int damage, int direction) {
		// TODO Auto-generated constructor stub
		this.type      = "Bullet";
		this.x         = x;
		this.y         = y;
		this.width     = width;
		this.height    = height;
		this.speed     = speed;
		this.damage    = damage;
		this.direction = direction;
	}

}

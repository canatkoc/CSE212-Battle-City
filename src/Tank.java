public abstract class Tank extends GameObject {
	protected int speed;
	protected int hp;
	protected int damage;
	
	public Tank(String type, int x, int y, int width, int height, int speed, int hp, int damage) {
		// TODO Auto-generated constructor stub
		super();
		this.type = type;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.speed = speed;
		this.hp = hp;
		this.damage = damage;
	}
}
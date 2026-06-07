public class EnemyTank extends Tank {

	protected int direction;
	protected int fireCooldown;
	protected int moveCooldown;

	public EnemyTank(int x, int y, int speed) {
		// TODO Auto-generated constructor stub
		super("Enemy Tank", x, y, 40, 40, speed, 1, 1);
		this.direction    = 1;
		this.fireCooldown = 60 + (int)(Math.random() * 60);
		this.moveCooldown = 60 + (int)(Math.random() * 120);
	}

}

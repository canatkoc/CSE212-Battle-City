public class EnemyTank extends Tank {

	protected int direction;    // 0=Up 1=Down 2=Left 3=Right
	protected int fireCooldown;
	protected int moveCooldown;

	public EnemyTank(int x, int y, int speed) {
		// TODO Auto-generated constructor stub
		super("Enemy Tank", x, y, 40, 40, speed, 1, 1);
		this.direction    = 1; // face down toward player on spawn
		this.fireCooldown = 60 + (int)(Math.random() * 60);
		this.moveCooldown = 60 + (int)(Math.random() * 120);
	}

}

public class PlayerTank extends Tank {

	private int starLevel;

	private static final int SHIELD_DURATION = 300;
	private int shieldTimer;


	public PlayerTank(int speed, int hp, int damage) {
		super("Player Tank", 160, 520, 40, 40, speed, hp, damage);
		// TODO Auto-generated constructor stub
		this.starLevel   = 0;
		this.shieldTimer = 0;
	}

	public void updatePowerUps() {
		if(shieldTimer > 0) {
			shieldTimer--;
		}
	}

	public void addStar() {
		if(starLevel < 3) {
			starLevel++;
		}
	}

	public void activateShield() {
		shieldTimer = SHIELD_DURATION;
	}

	public boolean isShieldActive() {
		return shieldTimer > 0;
	}

	public int getStarLevel() {
		return starLevel;
	}

	public void resetStarLevel() {
		starLevel = 0;
	}

	public int getFireCooldownModifier() {
		if(starLevel >= 1) {
			return -10;
		}
		return 0;
	}

	public int getMaxBulletsOnScreen() {
		if(starLevel >= 2) {
			return 2;
		}
		return 1;
	}

	public boolean canDestroySteel() {
		return starLevel >= 3;
	}
}

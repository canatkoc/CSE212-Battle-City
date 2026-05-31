public class PlayerTank extends Tank {

	// ---------------------------------------------------------------
	// Star power-up level (0 = default, 1 = faster fire, 2 = two bullets, 3 = destroys steel)
	private int starLevel;

	// Shield (Helmet) power-up — invulnerability timer in game ticks
	private static final int SHIELD_DURATION = 300;  // ~5 seconds at 60 ticks/s
	private int shieldTimer;

	// Shovel power-up tracked in GamePanel (affects map tiles around eagle)
	// Clock power-up tracked in GamePanel (freezes enemy movement)
	// Bomb power-up handled instantly in GamePanel (no persistent state)
	// Tank power-up gives +1 life in GamePanel

	// ---------------------------------------------------------------
	public PlayerTank(int speed, int hp, int damage) {
		// PDF: Eagle at bottom-centre (col 7, row 14) — player starts left of it (col 4, row 13)
		super("Player Tank", 160, 520, 40, 40, speed, hp, damage);
		// TODO Auto-generated constructor stub
		this.starLevel   = 0;
		this.shieldTimer = 0;
	}

	// ---------------------------------------------------------------
	// Called every game tick — counts down the shield timer
	public void updatePowerUps() {
		if(shieldTimer > 0) {
			shieldTimer--;
		}
	}

	// ---------------------------------------------------------------
	// Star power-up — increments star level up to maximum 3
	public void addStar() {
		if(starLevel < 3) {
			starLevel++;
		}
	}

	// ---------------------------------------------------------------
	// Activates the shield (Helmet) power-up — resets the invulnerability timer
	public void activateShield() {
		shieldTimer = SHIELD_DURATION;
	}

	// ---------------------------------------------------------------
	// Returns true while the player tank is invulnerable
	public boolean isShieldActive() {
		return shieldTimer > 0;
	}

	// ---------------------------------------------------------------
	// Star level queries used by GamePanel for shooting logic
	public int getStarLevel() {
		return starLevel;
	}

	// Resets star level on player death
	public void resetStarLevel() {
		starLevel = 0;
	}

	// ---------------------------------------------------------------
	// Star level 1+: reduced fire cooldown (faster shooting)
	// Base cooldown is set in GamePanel; this modifier is applied there
	public int getFireCooldownModifier() {
		if(starLevel >= 1) {
			return -10;  // ticks faster
		}
		return 0;
	}

	// ---------------------------------------------------------------
	// Star level 2+: allows two bullets on screen simultaneously
	public int getMaxBulletsOnScreen() {
		if(starLevel >= 2) {
			return 2;
		}
		return 1;
	}

	// ---------------------------------------------------------------
	// Star level 3: bullets destroy steel walls
	public boolean canDestroySteel() {
		return starLevel >= 3;
	}
}

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;

public final class AsteroidGame extends JPanel implements ActionListener, KeyListener {
	// Constants
    private static final int WIDTH = 1200, HEIGHT = 700;
	
	// Collision Handling
	private List<Asteroid> newAsteroids;
	private List<Asteroid> asteroidsToRemove;
	private List<VoidLaser> lasersToRemove;
	private List<Bullet> bulletsToRemove;
	private List<VoidHazard> voidHazardsToRemove;
	private List<BlackHole> blackHolesToRemove;
	private List<CosmicEntity> cosmicEntitiesToRemove;
	
    // Leveling, Alpha Fades, and Timers
    private int level;
	private ShipLevel shipLevel;
	private int levelUpFrame = 0;
	private boolean showingLevelUp = false;
	private int deathCountdown;
    private int levelStartCountdown;
	private int levelDisplayAlpha;
	private int levelTimer;
	private int errorAlpha; // alpha for errorMessage display
	private int deltaAlpha; // change in alpha
	private boolean levelStarting = false; 
	private int voidCooldownTimer = 0;
	private final int voidMaxCooldown = 3 * 60;
	private int fadeCooldownTimer = 0;
	private final int fadeMaxCooldown = 8 * 60;
		
	// Game Objects
	private final List<Asteroid> asteroids = new ArrayList<>();
	private final List<Bullet> bullets = new ArrayList<>();
	private final List<VoidLaser> voidLasers = new ArrayList<>();
	private final List<VoidHazard> voidHazards = new ArrayList<>();
	private final List<BlackHole> blackHoles = new ArrayList<>();
	private final List<CosmicEntity> cosmicEntities = new ArrayList<>();
	
	
	// ====== UIs ======
	private final List<Meteor> meteors;
	private final StarField starField;
	private final ParticleSystem particleSystem;
	// Menu animation fields
	private float menuTitlePulse = 0;
	private int menuBackgroundShift = 0;
	// Death screen animation fields
	private int deathExplosionFrame;
	private boolean deathExplosionDone = false;    
    
	// ====== Ship ======
	// ------ Thrust ------
    private boolean thrusting;
    private boolean rotatingLeft;
    private boolean rotatingRight;
	// ------ Abilities ------
	// Ability Manager
	private AbilityManager abilityManager;
	// Fade
	private boolean fadeActive;
	private int fadeTimer;
	private final int maxFadeTime = 5 * 60;
	// Teleport
	private TeleportAnchor teleportAnchor;
	private int teleportFlashFrame = 0;


	// ====== Reward Systems ======
	private int xpGain;
	private int fuelGain;
	private int scoreGain;
	// Score and asteroid destruction counters
	private int score;
	private int largeDestroyed;
	private int mediumDestroyed;
	private int smallDestroyed;
	// Best Score & Duration
	private int highestScore, highestDuration;


    // ====== Game General ======
    private final Random rand = new Random();
    private boolean noSavedGame;
    private Ship ship;
    private final Timer timer;
    private String gameState = "menu"; // "menu", "playing", "paused", "death"
	private int hoveredButton;
	private boolean showingInstructions;
	private int instructionPage = 0;
	private final int totalInstructionPages = 3;
	private FloatingTextManager floatingTextManager;
	// Deaths
	private boolean voidDeath;
	private boolean bulletDeath;
    private boolean asteroidDeath;
	private boolean voidHazardDeath;
	private boolean blackHoleDeath;
	private boolean cosmicEntityDeath;
    // Save system
    private final File saveFile = new File("save.dat");
    private final File leaderBoardDataFile = new File("leaderBoardData.dat");
	// Control Config
	private ControlConfig controlConfig;
	private AbilityLoadout abilityLoadout;
	private ControlPanel controlPanel;
	private boolean showingControlPanel = false;


	public AsteroidGame() {
		timer = new Timer(16, this); // ~60 FPS
		resetVars();

		starField = new StarField(WIDTH, HEIGHT, 150);
		particleSystem = new ParticleSystem();
		meteors = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			meteors.add(new Meteor(WIDTH, HEIGHT));
		}

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setFocusable(true);

		setupMenu();
		addKeyListener(this);

		timer.start();
	}

	private void resetVars() {
	    errorAlpha = 255;
	    deltaAlpha = 0;
	    score = 0;
	    largeDestroyed = 0;
	    mediumDestroyed = 0;
	    smallDestroyed = 0;
		level = 0;
	    deathCountdown = 5 * 60;		// 5 seconds at 60 FPS
	    levelStartCountdown = 3 * 60; 	// countdown before level starts (1 second)
	    levelDisplayAlpha = 255; 		// for fade out effect
	    levelTimer = 30 * 60; 			// 30 seconds * 60 FPS (assuming 60 FPS)
	    levelStarting = false;
	    thrusting = false;
	    rotatingLeft = false;
	    rotatingRight = false;
	    voidDeath = false;
	    bulletDeath = false;
	    asteroidDeath = false;
	    deathExplosionFrame = 0;
	    deathExplosionDone = false;
		fadeActive = false;
		fadeTimer = 5 * 60; // 5 seconds
		ship = new Ship();
		shipLevel = new ShipLevel();
		teleportAnchor = new TeleportAnchor();
		abilityManager = new AbilityManager();
		floatingTextManager = new FloatingTextManager();
		voidHazards.clear();
		blackHoles.clear();
		cosmicEntities.clear();
		teleportFlashFrame = 0;
		levelUpFrame = 0;
		showingLevelUp = false;
		voidCooldownTimer = 0;
		fadeCooldownTimer = 0;
		hoveredButton = -1;
		showingInstructions = false;
		noSavedGame = true;
		abilityLoadout = new AbilityLoadout();
		controlConfig = new ControlConfig();
		controlPanel = new ControlPanel(abilityLoadout, controlConfig);
		controlPanel.setBounds(0, 0, WIDTH, HEIGHT);
		showingControlPanel = false;
	}

    void setupMenu() {
		setLayout(null);
		setBackground(Color.BLACK);
				
		// Drawing custom buttons in paintComponent
		// Add mouse listener for custom button clicks
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!gameState.equals("menu") || showingInstructions) return;
				
				int mx = e.getX();
				int my = e.getY();
				
				// New Game button bounds
				if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 &&
					my >= HEIGHT/2 - 20 && my <= HEIGHT/2 + 20) {
					startNewGame();
				}
				
				// Resume button bounds
				if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 &&
					my >= HEIGHT/2 + 50 && my <= HEIGHT/2 + 90) {
					resumeGame();
				}

				// Instructions button bounds
				if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 &&
					my >= HEIGHT/2 + 120 && my <= HEIGHT/2 + 160) {
					if (showingInstructions) {
						showingInstructions = false;
						instructionPage = 0; // Reset to first page
					} else {
						showingInstructions = true;
					}
				}

				// Controls button bounds
				if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 &&
					my >= HEIGHT/2 + 190 && my <= HEIGHT/2 + 230) {
					showingControlPanel = !showingControlPanel;
					if (showingControlPanel) {
						add(controlPanel);
						controlPanel.requestFocusInWindow();
					} else {
						remove(controlPanel);
						requestFocusInWindow();
					}
					revalidate();
					repaint();
				}
			}
			
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (!gameState.equals("menu") || showingInstructions) return;
				
				int mx = e.getX();
				int my = e.getY();
				
				// Check if hovering over any button
				hoveredButton = -1;
				if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 && 
					my >= HEIGHT/2 - 20 && my <= HEIGHT/2 + 20) {
					hoveredButton = 0; // New Game
				} else if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 && 
					my >= HEIGHT/2 + 50 && my <= HEIGHT/2 + 90) {
					hoveredButton = 1; // Resume
				} else if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 && 
					my >= HEIGHT/2 + 120 && my <= HEIGHT/2 + 160) {
					hoveredButton = 2; // Instructions
				} else if (mx >= WIDTH/2 - 100 && mx <= WIDTH/2 + 100 && 
					my >= HEIGHT/2 + 190 && my <= HEIGHT/2 + 230) {
					hoveredButton = 3; // Controls
				}
				repaint();
			}
		});
	}
    
	void startNewGame() {
	    gameState = "playing";
	    resetVars();
	    ship = new Ship();
	    asteroids.clear();
	    bullets.clear();
		voidLasers.clear();
	
	    levelStarting = true;
	    
	    // Clear Previous Game Progress
	    try (FileWriter writer = new FileWriter(saveFile, false)) {} 
		catch (IOException e) { e.printStackTrace(); }

	    revalidate();
	    repaint();
	    requestFocusInWindow();
	}
	
	private void drawMenuButton(Graphics2D g2, String text, int y, boolean hovered, boolean enabled) {
		int buttonWidth = 200;
		int buttonHeight = 40;
		int buttonX = WIDTH / 2 - buttonWidth / 2;
		
		// Button background with glow when hovered
		if (hovered) {
			// Outer glow
			g2.setColor(new Color(100, 150, 255, 80));
			g2.fillRoundRect(buttonX - 5, y - 5, buttonWidth + 10, buttonHeight + 10, 15, 15);
		}
		
		// Main button
		Color bgColor = enabled ? 
			(hovered ? new Color(60, 80, 120, 220) : new Color(30, 40, 60, 200)) :
			new Color(40, 40, 40, 150);
		g2.setColor(bgColor);
		g2.fillRoundRect(buttonX, y, buttonWidth, buttonHeight, 10, 10);
		
		// Border
		Color borderColor = enabled ?
			(hovered ? new Color(120, 180, 255) : new Color(80, 120, 180)) :
			new Color(80, 80, 80);
		g2.setColor(borderColor);
		g2.setStroke(new BasicStroke(2));
		g2.drawRoundRect(buttonX, y, buttonWidth, buttonHeight, 10, 10);
		g2.setStroke(new BasicStroke(1));
		
		// Text
		g2.setFont(new Font("Serif", Font.BOLD, 22));
		FontMetrics fm = g2.getFontMetrics();
		Color textColor = enabled ?
			(hovered ? Color.WHITE : new Color(200, 220, 255)) :
			new Color(120, 120, 120);
		g2.setColor(textColor);
		g2.drawString(text, 
			buttonX + buttonWidth/2 - fm.stringWidth(text)/2,
			y + buttonHeight/2 + fm.getAscent()/2 - 2);
	}

	private void drawInstructionsPopup(Graphics2D g2) {
		// Semi-transparent backdrop
		g2.setColor(new Color(0, 0, 0, 180));
		g2.fillRect(0, 0, WIDTH, HEIGHT);
		
		// Popup panel
		int panelWidth = 700;
		int panelHeight = 550;
		int panelX = WIDTH / 2 - panelWidth / 2;
		int panelY = HEIGHT / 2 - panelHeight / 2;
		int padding = 15;
		
		// Panel background
		g2.setColor(new Color(20, 25, 40, 250));
		g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
		
		// Border
		g2.setColor(new Color(100, 150, 255, 200));
		g2.setStroke(new BasicStroke(2));
		g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
		g2.setStroke(new BasicStroke(1));
		
		// Page Title
		g2.setFont(new Font("Arial", Font.BOLD, 28));
		g2.setColor(new Color(150, 200, 255));
		String title;
		switch (instructionPage) {
			case 0 -> title = "QUICK START";
			case 1 -> title = "GAME MECHANICS";
			case 2 -> title = "ADVANCED TIPS";
			default -> title = "Unknown";
		}
		FontMetrics titleFm = g2.getFontMetrics();
		g2.drawString(title, panelX + panelWidth/2 - titleFm.stringWidth(title)/2, panelY + padding + titleFm.getAscent());
		
		// Page Indicator
		g2.setFont(new Font("Arial", Font.PLAIN, 14));
		String pageNum = (instructionPage + 1) + " / " + totalInstructionPages;
		FontMetrics pageFm = g2.getFontMetrics();
		g2.setColor(new Color(150, 150, 180));
		g2.drawString(pageNum, panelX + panelWidth - padding - pageFm.stringWidth(pageNum), panelY + padding + titleFm.getAscent());
		
		// Separator
		g2.setColor(new Color(100, 150, 255, 100));
		g2.drawLine(panelX + 50, panelY + 60, panelX + panelWidth - 50, panelY + 60);
		
		// Content based on page
		int lineHeight = 25;
		switch (instructionPage) {
			case 0 -> drawQuickStartPage(g2, panelX, panelY, panelWidth, panelHeight, lineHeight);
			case 1 -> drawMechanicsPage(g2, panelX, panelY, panelWidth, panelHeight, lineHeight);
			case 2 -> drawAdvancedPage(g2, panelX, panelY, panelWidth, panelHeight, lineHeight);
		}
		
		// Navigation arrows
		g2.setFont(new Font("Arial", Font.BOLD, 16));
    
    // Left arrow button
    if (instructionPage > 0) {
        int leftBtnX = panelX + 50;
        int leftBtnY = panelY + panelHeight - 50;
        int btnWidth = 100;
        int btnHeight = 35;
        
        // Background
        g2.setColor(new Color(50, 60, 80, 150));
        g2.fillRoundRect(leftBtnX, leftBtnY, btnWidth, btnHeight, 8, 8);
        
        // Border
        g2.setColor(new Color(100, 150, 255));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(leftBtnX, leftBtnY, btnWidth, btnHeight, 8, 8);
        
        // Text
        g2.setColor(new Color(150, 200, 255));
        String leftArrow = "◄ PREV";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(leftArrow, leftBtnX + btnWidth/2 - fm.stringWidth(leftArrow)/2, leftBtnY + 23);
    }
    
    // Right arrow button
    if (instructionPage < totalInstructionPages - 1) {
        int rightBtnX = panelX + panelWidth - 150;
        int rightBtnY = panelY + panelHeight - 50;
        int btnWidth = 100;
        int btnHeight = 35;
        
        // Background
        g2.setColor(new Color(50, 60, 80, 150));
        g2.fillRoundRect(rightBtnX, rightBtnY, btnWidth, btnHeight, 8, 8);
        
        // Border
        g2.setColor(new Color(100, 150, 255));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(rightBtnX, rightBtnY, btnWidth, btnHeight, 8, 8);
        
        // Text
        g2.setColor(new Color(150, 200, 255));
        String rightArrow = "NEXT ►";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(rightArrow, rightBtnX + btnWidth/2 - fm.stringWidth(rightArrow)/2, rightBtnY + 23);
    }
    
    g2.setStroke(new BasicStroke(1));  // Reset stroke
    
    // Close hint (matching control panel instructions style)
    g2.setFont(new Font("Arial", Font.PLAIN, 12));
    g2.setColor(new Color(150, 150, 180));
    String closeHint = "• Press [I] or [ESC] to close • Use Arrow Keys to navigate";
    int hintWidth = g2.getFontMetrics().stringWidth(closeHint);
    g2.drawString(closeHint, panelX + panelWidth/2 - hintWidth/2, panelY + panelHeight - 15);
	}

	private void drawQuickStartPage(Graphics2D g2, int panelX, int panelY, int panelWidth, int panelHeight, int lineHeight) {
		int textY = panelY + 85;
		int textX = panelX + 80;
		
		// Movement Instructions
		g2.setFont(new Font("Arial", Font.BOLD, 14));
		g2.setColor(new Color(255, 200, 100));
		g2.drawString("MOVEMENT:", textX, textY);
		textY += lineHeight;
		
		String[][] movementControlDisplay = {
			{controlConfig.getKeyName(controlConfig.getKey("thrust")), "Thrust"},
			{controlConfig.getKeyName(controlConfig.getKey("left")) + " " + controlConfig.getKeyName(controlConfig.getKey("right")), "Rotate"},
			{controlConfig.getKeyName(controlConfig.getKey("hyper")), "Hyper Mode"},
			{controlConfig.getKeyName(controlConfig.getKey("shoot")), "Fire Weapon"}
		};
		
		g2.setFont(new Font("Arial", Font.PLAIN, 13));
		for (String[] line : movementControlDisplay) {
			g2.setColor(new Color(100, 255, 150));
			g2.drawString(line[0], textX, textY);
			g2.setColor(new Color(200, 200, 240));
			g2.drawString(line[1], textX + 100, textY);
			textY += lineHeight;
		}
		
		// Shortcuts
		String[][] shortcutDisplay = {
			{"I", "Show/Hide instructions"},
			{"C", "Open Control Panel"},
			{"ENTER", "Start game / Return to menu"},
			{"ESC", "Exit Popups & Pause and Save Game"}
		};
		g2.setFont(new Font("Arial", Font.BOLD, 14));
		g2.setColor(new Color(255, 200, 100));
		g2.drawString("SHORTCUTS:", textX, textY);
		textY += lineHeight;
		
		g2.setFont(new Font("Arial", Font.PLAIN, 13));
		for (String[] line : shortcutDisplay) {
			g2.setColor(new Color(100, 255, 150));
			g2.drawString(line[0], textX, textY);
			g2.setColor(new Color(200, 200, 240));
			g2.drawString(line[1], textX + 100, textY);
			textY += lineHeight;
		}
		
		// ALL Abilities with current keys
		String[][] allAbilities = {
			{controlConfig.getKeyName(controlConfig.getKey("ability1")), 
			abilityLoadout.getSlot(0) + " (Slot 1)"},
			{controlConfig.getKeyName(controlConfig.getKey("ability2")), 
			abilityLoadout.getSlot(1) + " (Slot 2)"},
			{controlConfig.getKeyName(controlConfig.getKey("ability3")), 
			abilityLoadout.getSlot(2) + " (Slot 3)"},
			{controlConfig.getKeyName(controlConfig.getKey("ability4")), 
			abilityLoadout.getSlot(3) + " (Slot 4)"}
		};
		
		g2.setFont(new Font("Arial", Font.BOLD, 14));
		g2.setColor(new Color(255, 200, 100));
		g2.drawString("EQUIPPED ABILITIES:", textX, textY);
		textY += lineHeight;
		
		g2.setFont(new Font("Arial", Font.PLAIN, 13));
		for (String[] line : allAbilities) {
			g2.setColor(new Color(100, 255, 150));
			g2.drawString(line[0], textX, textY);
			g2.setColor(new Color(200, 200, 240));
			g2.drawString(line[1], textX + 100, textY);
			textY += lineHeight;
		}
	}

	private void drawMechanicsPage(Graphics2D g2, int panelX, int panelY, int panelWidth, int panelHeight, int lineHeight) {
		g2.setFont(new Font("Arial", Font.PLAIN, 14));
		int textY = panelY + 100;
		
		String[] mechanics = {
			"VOID DIMENSION (D) - Unlocks at Level 3",
			"  • Enter a parallel dimension where asteroids are ghostly",
			"  • Absorb void energy while inside",
			"  • If void energy reaches 100, you DIE instantly",
			"  • Void hazards spawn inside - they're harmless outside",
			"",
			"FADE MODE (F) - Unlocks at Level 5",
			"  • Become invulnerable for 5 seconds",
			"  • Drains void energy faster than normal",
			"  • Cannot fire weapons while faded",
			"",
			"TELEPORT (T) - Unlocks at Level 7",
			"  • Press once to place an anchor",
			"  • Press again to teleport back to anchor",
			"  • Anchor expires after 10 seconds"
		};
		
		for (String line : mechanics) {
			if (line.isEmpty()) {
				textY += lineHeight / 2;
				continue;
			}
			
			if (line.contains("Unlocks")) {
				g2.setColor(new Color(255, 200, 100));
				g2.setFont(new Font("Arial", Font.BOLD, 14));
			} else if (line.startsWith("  ")) {
				g2.setColor(new Color(200, 200, 220));
				g2.setFont(new Font("Arial", Font.PLAIN, 13));
			} else {
				g2.setColor(new Color(220, 220, 240));
				g2.setFont(new Font("Arial", Font.PLAIN, 14));
			}
			
			g2.drawString(line, panelX + 60, textY);
			textY += lineHeight;
		}
	}

	private void drawAdvancedPage(Graphics2D g2, int panelX, int panelY, int panelWidth, int panelHeight, int lineHeight) {
		g2.setFont(new Font("Arial", Font.PLAIN, 14));
		int textY = panelY + 100;
		
		String[] advanced = {
			"ADVANCED ABILITIES",
			"",
			"SHIELD BURST (Q) - Level 9",
			"  • Destroys all nearby asteroids in expanding wave",
			"  • Grants temporary invulnerability",
			"",
			"TIME SLOW (R) - Level 12",
			"  • Slows everything except your ship to 30% speed",
			"  • Duration: 5 seconds",
			"",
			"COMBAT DRONE (E) - Level 15",
			"  • Deploys auto-firing companion that orbits ship",
			"  • Automatically targets nearest asteroid",
			"  • Has 3 HP - destroyed if hit 3 times",
			"",
			"NOVA BLAST (X) - Level 20 ULTIMATE",
			"  • Requires 75+ void energy to activate",
			"  • Clears entire screen after 1 second charge",
			"  • Consumes all void energy"
		};
		
		for (String line : advanced) {
			if (line.isEmpty()) {
				textY += lineHeight / 2;
				continue;
			}
			
			if (line.contains("Level") || line.equals("ADVANCED ABILITIES")) {
				g2.setColor(new Color(255, 200, 100));
				g2.setFont(new Font("Arial", Font.BOLD, 14));
			} else if (line.startsWith("  ")) {
				g2.setColor(new Color(200, 200, 220));
				g2.setFont(new Font("Arial", Font.PLAIN, 13));
			} else {
				g2.setColor(new Color(220, 220, 240));
				g2.setFont(new Font("Arial", Font.PLAIN, 14));
			}
			
			g2.drawString(line, panelX + 60, textY);
			textY += lineHeight;
		}
	}

    void resumeGame() {
        if (!saveFile.exists() || saveFile.length() == 0) { noSavedGame = true; return; }
        try (DataInputStream dis = new DataInputStream(new FileInputStream(saveFile))) {
            // skip highest score & duration
            dis.skip(4);
            dis.skip(4);
            
            // retreive data
            level = dis.readInt();
            score = dis.readInt();
            ship.setFuel(dis.readDouble());
            levelStartCountdown = dis.readInt();
            levelDisplayAlpha = dis.readInt();
            levelStarting = dis.readBoolean();
            ship.setX(dis.readDouble());
            ship.setY(dis.readDouble());
            ship.setAngle(dis.readDouble());
			shipLevel.setLevel(dis.readInt());
			shipLevel.setXP(dis.readInt());
			shipLevel.setXPToNextLevel(dis.readInt());
            
            gameState = "playing";
            requestFocusInWindow();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveGame() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(saveFile))) {
            dos.writeInt(highestScore);
            dos.writeInt(highestDuration);
            dos.writeInt(level);
            dos.writeInt(score);
            dos.writeDouble(ship.getFuel());
            dos.writeInt(levelStartCountdown);
            dos.writeInt(levelDisplayAlpha);
            dos.writeBoolean(levelStarting);
            dos.writeDouble(ship.getX());
            dos.writeDouble(ship.getY());
            dos.writeDouble(ship.getAngle());
			dos.writeInt(shipLevel.getLevel());
			dos.writeInt(shipLevel.getXP());
			dos.writeInt(shipLevel.getXPToNextLevel());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startDeathSequence() {
	    // check for highest score & duration
	    int savedHighScore = 0;
		int savedHighDuration = 0;
		
		if (saveFile.exists() && saveFile.length() >= 8) {
		    try (DataInputStream dis = new DataInputStream(new FileInputStream(saveFile))) {
		        savedHighScore = dis.readInt();
		        savedHighDuration = dis.readInt();
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
		
		// calculate current session's score & duration
		int currentScore = score;
		int currentDuration = (level - 1) * 33 + (30 * 60 - levelTimer) / 60;
		
		// compare and set highest
		highestScore = Math.max(savedHighScore, currentScore);
		highestDuration = Math.max(savedHighDuration, currentDuration);

	    deathCountdown = 5 * 60;  // reset to 5 seconds at 60 FPS
	    gameState = "death";  // new game state for death screen
	    //bullets.clear();
	    //asteroids.clear();
	}	
    
	@Override
	public void actionPerformed(ActionEvent e) {
        switch (gameState) {
            case "menu" -> {
                // Menu animations
                menuTitlePulse += 0.05f;
                menuBackgroundShift = (menuBackgroundShift + 1) % WIDTH;
                for (Meteor m : meteors) {
                    m.update();
                }
                if (noSavedGame) {
                    if (errorAlpha >= 255)
                    	deltaAlpha = -2;
                    else if (errorAlpha <= 255 / 2)
                    	deltaAlpha = 2;
                    errorAlpha+=deltaAlpha;
                }
            }
            case "playing" -> {
                // Update fade timer
				if (fadeActive) {
					fadeTimer--;
					if (fadeTimer <= 0) {
						fadeActive = false;
						fadeTimer = maxFadeTime;
					}
					
					// Dissipate energy while in fade (acts as if in reality)
					if (ship.getVoidEnergy().getEnergy() > 0) {
						ship.getVoidEnergy().dissipateForAction(0.05);
					}
				}
				if (fadeActive && fadeTimer <= 0) {
					fadeActive = false;
					fadeTimer = maxFadeTime;
					fadeCooldownTimer = fadeMaxCooldown;
					abilityManager.startFadeCooldown();
				}

				ship.setFadeMode(fadeActive);

				// Update ability systems
				abilityManager.update();
				teleportAnchor.update();
				floatingTextManager.update();

				// Update cooldowns
				if (voidCooldownTimer > 0) voidCooldownTimer--;
				if (fadeCooldownTimer > 0) fadeCooldownTimer--;

				// Sync ability manager with actual states
				abilityManager.setVoidUnlocked(shipLevel.isAbilityUnlocked("void"));
				abilityManager.setFadeUnlocked(shipLevel.isAbilityUnlocked("fade"));
				abilityManager.setTeleportUnlocked(shipLevel.isAbilityUnlocked("teleport"));
				abilityManager.setShieldUnlocked(shipLevel.isAbilityUnlocked("shield"));
				abilityManager.setTimeSlowUnlocked(shipLevel.isAbilityUnlocked("timeslow"));
				abilityManager.setDroneUnlocked(shipLevel.isAbilityUnlocked("drone"));
				abilityManager.setNovaUnlocked(shipLevel.isAbilityUnlocked("nova"));
				
				abilityManager.setVoidActive(ship.getVoidEnergy().isActive());
				abilityManager.setFadeActive(fadeActive);
				abilityManager.setTeleportActive(teleportAnchor.isAnchorPlaced());

				// Update teleport flash
				if (teleportFlashFrame > 0) teleportFlashFrame--;

				// Update level up animation
				if (showingLevelUp) {
					levelUpFrame++;
					if (levelUpFrame >= 60) {
						showingLevelUp = false;
						levelUpFrame = 0;
					}
				}

				// Spawn void hazards when in void world
				if (ship.getVoidEnergy().isActive() && rand.nextInt(150) < Math.min(level, 10)) {
					VoidHazard.Type[] types = VoidHazard.Type.values();
					VoidHazard.Type randomType = types[rand.nextInt(types.length)];
					voidHazards.add(new VoidHazard(WIDTH, HEIGHT, randomType));
				}

				// Update void hazards
				Iterator<VoidHazard> vhIt = voidHazards.iterator();
				while (vhIt.hasNext()) {
					VoidHazard vh = vhIt.next();
					vh.update();
					if (!vh.isAlive()) {
						vhIt.remove();
					} else if (!fadeActive && vh.intersects(ship.getBounds()) && ship.getVoidEnergy().isActive()) {
						abilityManager.setVoidActive(false);
						voidHazardDeath = true;
						startDeathSequence();
						return;
					}
				}

				// Update black holes
				for (BlackHole bh : blackHoles) {
					bh.update();
					
					// Apply gravity to ship
					Point2D.Double gravity = bh.applyGravity(ship.getX(), ship.getY());
					ship.setX(ship.getX() + gravity.x);
					ship.setY(ship.getY() + gravity.y);
					
					// Check if consumed
					if (!fadeActive && bh.isShipConsumed(ship.getX(), ship.getY())) {
						blackHoleDeath = true;
						startDeathSequence();
						return;
					}
				}

				// Update cosmic entities
				for (CosmicEntity ce : cosmicEntities) {
					ce.update(ship.getX(), ship.getY());
					
					if (!fadeActive && !ship.getVoidEnergy().isActive() && ce.isNearShip(ship.getBounds())) {
						cosmicEntityDeath = true;
						startDeathSequence();
						return;
					}
				}

				// Update visual effects
                starField.setVoidMode(ship.getVoidEnergy().isActive());
                starField.update(ship.getX() - WIDTH / 2.0, ship.getY() - HEIGHT / 2.0,
                        ship.getVelocity() * Math.sin(ship.getAngle()),
                        -ship.getVelocity() * Math.cos(ship.getAngle()));
                particleSystem.update();
                
                // Update void energy
                ship.getVoidEnergy().update();
                // Check if void energy exceeded threshold
                if (ship.getVoidEnergy().isOverThreshold()) {
                    voidDeath = true;
                    startDeathSequence();
                    return;
                }
                // Deactivate void mode if energy depleted
                if (ship.getVoidEnergy().getEnergy() <= 0) { abilityManager.setVoidActive(false); }
                
                // Decrement the level timer every tick (frame)
                if (levelStarting) {
                    levelStartCountdown--;
                    if (levelStartCountdown <= 0) {
                        levelStarting = false;
                        level++;
                        levelStartCountdown = 3 * 60;
                        // Generate more asteroids than before
                        for (int i = 0; i < level + 2; i++) {
                            Asteroid newAsteroid = Asteroid.randomAsteroid(WIDTH, HEIGHT);
							if (level > 3 && level < 10) { newAsteroid.setMultiplier(level * 0.5); }
							asteroids.add(newAsteroid);
                        }

						// Introduce First Black Holes at level 5
						if (level == 1) {
    						blackHoles.add(new BlackHole(WIDTH * 0.3, HEIGHT * 0.5));
						}

						// Introduce First Cosmic Entity at level 10
						if (level == 1) {
    						cosmicEntities.add(new CosmicEntity(WIDTH, HEIGHT));
						}
                        
						// Multiple Threats at level 10+
						if (level >= 10) {
							if (level % 3 == 0) {
								double bx = rand.nextDouble() * WIDTH;
								double by = rand.nextDouble() * HEIGHT;
								blackHoles.add(new BlackHole(bx, by));
							}
							if (level % 4 == 0) {
								cosmicEntities.add(new CosmicEntity(WIDTH, HEIGHT));
							}
						}
                    }
                    levelDisplayAlpha = (int) (255 * ((float)levelStartCountdown / 180));
                    if (levelDisplayAlpha <= 0) {
                        levelDisplayAlpha = 255;
                    }
                    //bullets.clear();
                    //asteroids.clear();
                } else {
                    if (rand.nextInt(100) < level) {  // increase spawn rate with level
                        asteroids.add(Asteroid.randomAsteroid(WIDTH, HEIGHT));
                    }
                    // Now countdown level timer after start countdown finished
                    if (levelTimer > 0) {
                        levelTimer--;
                    } else {
                        levelTimer = 30 * 60;  // reset 30 second countdown for next level
                        // Show level text for 3 seconds (180 frames)
                        levelStarting = true;
                    }
                }
                // Thrust & Rotation
                if (thrusting) ship.applyThrust();
                if (rotatingLeft) ship.rotateLeft();
                if (rotatingRight) ship.rotateRight();
                ship.updatePhysics();
                
				// ====== Ability Effects ======
                // Asteroid Collision with Ship & Time Slow effect
				
				if (abilityManager.isTimeSlowActive())
					for (Asteroid a : asteroids) {
						a.setSlowedVelocity(abilityManager.getTimeScale());
						a.toggleTimeSlow(abilityManager.isTimeSlowActive());
					}
				
				// Shield destroys asteroids
				if (abilityManager.isShieldActive()) {
					Iterator<Asteroid> shieldIt = asteroids.iterator();
					while (shieldIt.hasNext()) {
						Asteroid a = shieldIt.next();
						double dx = a.getBounds().getBounds().getCenterX() - ship.getX();
						double dy = a.getBounds().getBounds().getCenterY() - ship.getY();
						double dist = Math.sqrt(dx * dx + dy * dy);
						
						if (dist < abilityManager.getShieldRadius()) {
							// Destroy asteroid
							particleSystem.createAsteroidExplosion(
								a.getBounds().getBounds().getCenterX(),
								a.getBounds().getBounds().getCenterY(),
								a.getSize()
							);
							
							// Award points
							switch (a.getSize()) {
								case LARGE -> { scoreGain = 20; xpGain = 10; fuelGain = 20; }
								case MEDIUM -> { scoreGain = 10; xpGain = 5; fuelGain = 10; }
								case SMALL -> { scoreGain = 5; xpGain = 3; fuelGain = 5; }
							}
							score += scoreGain;
							shipLevel.addXP(xpGain);
							ship.addFuel(fuelGain);
							
							shieldIt.remove();
							if (a.getSize() != Asteroid.Size.SMALL) {
								newAsteroids.addAll(a.split());
							}
						}
					}
				}

				// Drone auto-fire
				if (abilityManager.isDroneDeployed() && abilityManager.canDroneFire()) {
					Point dronePos = abilityManager.getDronePosition(ship.getX(), ship.getY());
					if (dronePos != null) {
						// Find nearest asteroid within 200 pixels
						Asteroid nearest = null;
						double nearestDist = 200;
						
						for (Asteroid a : asteroids) {
							double dx = a.getBounds().getBounds().getCenterX() - dronePos.x;
							double dy = a.getBounds().getBounds().getCenterY() - dronePos.y;
							double dist = Math.sqrt(dx * dx + dy * dy);
							
							if (dist < nearestDist) {
								nearest = a;
								nearestDist = dist;
							}
						}
						
						if (nearest != null) {
							// Calculate angle to target
							double dx = nearest.getBounds().getBounds().getCenterX() - dronePos.x;
							double dy = nearest.getBounds().getBounds().getCenterY() - dronePos.y;
							double angleToTarget = Math.atan2(dx, -dy);
							
							// Fire bullet from drone
							bullets.add(new Bullet(dronePos.x + 30, dronePos.y + 30, angleToTarget));
							abilityManager.droneDidFire();
						}
					}
				}

				// ====== Collisions & Physics ======
				asteroidsToRemove = new ArrayList<>();
                newAsteroids = new ArrayList<>();
                lasersToRemove = new ArrayList<>(); 
                bulletsToRemove = new ArrayList<>();
                voidHazardsToRemove = new ArrayList<>();
                blackHolesToRemove = new ArrayList<>();
                cosmicEntitiesToRemove = new ArrayList<>();
				// ------ Asteroid Physics & Collision with Ship ------
				for (Asteroid asteroid : asteroids) {
					asteroid.update();
					if (!fadeActive && !ship.getVoidEnergy().isActive() && asteroid.intersects(ship.getBounds())) {
						asteroidDeath = true;
						startDeathSequence();
						return;
					}
					// Drone hit by Asteroid
					Polygon droneBounds = abilityManager.getDroneBounds(ship.getX(), ship.getY());
	                if (abilityManager.isDroneDeployed() 
	                	&& asteroid.intersects(droneBounds)) {
	                	abilityManager.damageDrone();
	                	
	                	asteroidsToRemove.add(asteroid);
	                	
	                	// Add particle effect
                        particleSystem.createAsteroidExplosion(
                        	asteroid.getBounds().getBounds().getCenterX(),
                            asteroid.getBounds().getBounds().getCenterY(),
                            asteroid.getSize()
                        );
                        particleSystem.createDroneExplosion(
	                		droneBounds.getBounds().getCenterX(),
	                		droneBounds.getBounds().getCenterY()
	                	);
	                	/*
	                	// Handle asteroid destruction
						if (asteroid.getSize() == Asteroid.Size.SMALL) {
							asteroidsToRemove.add(asteroid);
						} else {
							asteroidsToRemove.add(asteroid);
							newAsteroids.addAll(asteroid.split());
						}
						*/
						
						switch (asteroid.getSize()) {
							case LARGE -> {
								xpGain = 10;
								fuelGain = 20;
								scoreGain = 20;
								largeDestroyed++;
							}
							case MEDIUM -> {
								xpGain = 5;
								fuelGain = 10;
								scoreGain = 10;
								mediumDestroyed++;
							}
							case SMALL -> {
								xpGain = 3;
								fuelGain = 5;
								scoreGain = 5;
								smallDestroyed++;
							}
						}
						// Track old level
						int oldLevel = shipLevel.getLevel();
							
						// Award rewards
						score += scoreGain;
						shipLevel.addXP(xpGain);
						ship.addFuel(fuelGain);
						
						// Show floating text at collision point
						Rectangle asteroidBounds = asteroid.getBounds().getBounds();
						double centerX = asteroidBounds.getCenterX();
						double centerY = asteroidBounds.getCenterY();
						
						floatingTextManager.add(FloatingTextManager.createFuel(fuelGain, centerX, centerY + 15));
						
						// Show XP gain near ship
						floatingTextManager.add(FloatingTextManager.createXP(xpGain, ship.getX() + 30, ship.getY() - 20));
								

						// Check for level up
						if (shipLevel.getLevel() > oldLevel) {
							showingLevelUp = true;
							levelUpFrame = 0;
							
							// Create level up particle effect
							particleSystem.createExplosion(
								ship.getX(), ship.getY(),
								shipLevel.getLevelColor(),
								100, 8.0
							);
						}
	                }
				}
				
                // ------ Void Laser Collisions ------
                for (VoidLaser laser : voidLasers) {
                    if (!laser.isAlive()) {
                        lasersToRemove.add(laser);
                        continue;
                    }
                    // Drone hit by laser
	                if (abilityManager.isDroneDeployed() 
	                	&& laser.intersects(abilityManager.getDroneBounds(ship.getX(), ship.getY()))) {
	                	abilityManager.damageDrone();
	                }
                    
					if (!ship.getVoidEnergy().isActive()) {
						for (Asteroid asteroid : asteroids) {
							if (laser.intersects(asteroid.getBounds())) {
								// Add particle effect
								particleSystem.createLaserExplosion(
										asteroid.getBounds().getBounds().getCenterX(),
										asteroid.getBounds().getBounds().getCenterY(),
										asteroid.getSize()
								);
								
								// Handle asteroid destruction
								if (asteroid.getSize() == Asteroid.Size.SMALL) {
									asteroidsToRemove.add(asteroid);
								} else {
									asteroidsToRemove.add(asteroid);
									newAsteroids.addAll(asteroid.split());
								}
								
								switch (asteroid.getSize()) {
									case LARGE -> {
										xpGain = 10;
										fuelGain = 20;
										scoreGain = 20;
										largeDestroyed++;
									}
									case MEDIUM -> {
										xpGain = 5;
										fuelGain = 10;
										scoreGain = 10;
										mediumDestroyed++;
									}
									case SMALL -> {
										xpGain = 3;
										fuelGain = 5;
										scoreGain = 5;
										smallDestroyed++;
									}
								}

								// Track old level
								int oldLevel = shipLevel.getLevel();

								// Award rewards
								score += scoreGain;
								shipLevel.addXP(xpGain);
								ship.addFuel(fuelGain);

								// Show floating text at collision point
								Rectangle asteroidBounds = asteroid.getBounds().getBounds();
								double centerX = asteroidBounds.getCenterX();
								double centerY = asteroidBounds.getCenterY();

								floatingTextManager.add(FloatingTextManager.createFuel(fuelGain, centerX, centerY + 15));

								// Show XP gain near ship
								floatingTextManager.add(FloatingTextManager.createXP(xpGain, ship.getX() + 30, ship.getY() - 20));
								

								// Check for level up
								if (shipLevel.getLevel() > oldLevel) {
									showingLevelUp = true;
									levelUpFrame = 0;
									
									// Create level up particle effect
									particleSystem.createExplosion(
										ship.getX(), ship.getY(),
										shipLevel.getLevelColor(),
										100, 8.0
									);
								}
							}
						}
					}
                }
                voidLasers.removeAll(lasersToRemove);
                
                // ------ Bullet Collision ------
				// Update bullets and check for bullet collisions
                for (Bullet bullet : bullets) {
                    bullet.update(WIDTH, HEIGHT);
                    // Ship hit by bullet
	                if (!fadeActive && !ship.getVoidEnergy().isActive() 
	                	&& bullet.getBounds().intersects(ship.getBounds().getBounds2D())) {
	                    bulletDeath = true;
	                    startDeathSequence();
	                    return;
	                }
	                
	                // Drone hit by bullet
	                if (abilityManager.isDroneDeployed() 
	                	&& bullet.getBounds().intersects(abilityManager.getDroneBounds(ship.getX(), ship.getY()).getBounds2D())) {
	                	abilityManager.damageDrone();
	                	bulletsToRemove.add(bullet);
	                }
                    
                    for (Asteroid asteroid : asteroids) {
                        if (asteroid.getBounds().intersects(bullet.getBounds().getBounds2D())) {
                            bulletsToRemove.add(bullet);
                            
                            // Add particle effect
                            particleSystem.createAsteroidExplosion(
                                    asteroid.getBounds().getBounds().getCenterX(),
                                    asteroid.getBounds().getBounds().getCenterY(),
                                    asteroid.getSize()
                            );
                            
                            // Handle asteroid destruction
                            if (asteroid.getSize() == Asteroid.Size.SMALL) {
                                asteroidsToRemove.add(asteroid);
                            } else {
                                asteroidsToRemove.add(asteroid);
                                newAsteroids.addAll(asteroid.split());
                            }
                            switch (asteroid.getSize()) {
								case LARGE -> {
									xpGain = 10;
									fuelGain = 20;
									scoreGain = 20;
									largeDestroyed++;
								}
								case MEDIUM -> {
									xpGain = 5;
									fuelGain = 10;
									scoreGain = 10;
									mediumDestroyed++;
								}
								case SMALL -> {
									xpGain = 3;
									fuelGain = 5;
									scoreGain = 5;
									smallDestroyed++;
								}
							}
                            // Track old level
							int oldLevel = shipLevel.getLevel();

							// Award rewards
							score += scoreGain;
							shipLevel.addXP(xpGain);
							ship.addFuel(fuelGain);

							// Show floating text at collision point
							Rectangle asteroidBounds = asteroid.getBounds().getBounds();
							double centerX = asteroidBounds.getCenterX();
							double centerY = asteroidBounds.getCenterY();

							floatingTextManager.add(FloatingTextManager.createFuel(fuelGain, centerX, centerY + 15));

							// Show XP gain near ship
							floatingTextManager.add(FloatingTextManager.createXP(xpGain, ship.getX() + 30, ship.getY() - 20));
								

							// Check for level up
							if (shipLevel.getLevel() > oldLevel) {
								showingLevelUp = true;
								levelUpFrame = 0;
									
								// Create level up particle effect
								particleSystem.createExplosion(
									ship.getX(), ship.getY(),
									shipLevel.getLevelColor(),
									100, 8.0
								);
							}
                        }
                    }
                }
					
				// ------ Nova Blast Collisions ------
				// Nova Blast Destroying Asteroids
				if (abilityManager.isNovaExploding()) {
					double novaRadius = abilityManager.getNovaExplosionRadius();
					for (Asteroid a : asteroids) {
						double dx = a.getBounds().getBounds().getCenterX() - ship.getX();
						double dy = a.getBounds().getBounds().getCenterY() - ship.getY();
						double dist = Math.sqrt(dx * dx + dy * dy);
						if (dist < novaRadius) {
							particleSystem.createAsteroidExplosion(
								a.getBounds().getBounds().getCenterX(),
								a.getBounds().getBounds().getCenterY(),
								a.getSize()
							);
								
							switch (a.getSize()) {
								case LARGE -> { scoreGain = 20; xpGain = 10; fuelGain = 20; largeDestroyed++; }
								case MEDIUM -> { scoreGain = 10; xpGain = 5; fuelGain = 10; mediumDestroyed++; }
								case SMALL -> { scoreGain = 5; xpGain = 3; fuelGain = 5; smallDestroyed++; }
							}
							score += scoreGain;
							shipLevel.addXP(xpGain);
							ship.addFuel(fuelGain);
								
							// Track old level
							int oldLevel = shipLevel.getLevel();
							
							// Show floating text at collision point
							Rectangle asteroidBounds = a.getBounds().getBounds();
							double centerX = asteroidBounds.getCenterX();
							double centerY = asteroidBounds.getCenterY();

							floatingTextManager.add(FloatingTextManager.createFuel(fuelGain, centerX, centerY + 15));

							// Show XP gain near ship
							floatingTextManager.add(FloatingTextManager.createXP(xpGain, ship.getX() + 30, ship.getY() - 20));
							
							// Check for level up
							if (shipLevel.getLevel() > oldLevel) {
								showingLevelUp = true;
								levelUpFrame = 0;
									
								// Create level up particle effect
								particleSystem.createExplosion(
									ship.getX(), ship.getY(),
									shipLevel.getLevelColor(),
									100, 8.0
								);
							}
							
							asteroidsToRemove.add(a);
						}
					}
					
					// Nova Blast Destroying Bullets
					for (Bullet b : bullets) {
						double dx = b.getBounds().getBounds().getCenterX() - ship.getX();
						double dy = b.getBounds().getBounds().getCenterY() - ship.getY();
						double dist = Math.sqrt(dx * dx + dy * dy);
						if (dist < novaRadius) {
							particleSystem.createBulletExplosion(
								b.getBounds().getBounds().getCenterX(),
								b.getBounds().getBounds().getCenterY()
							);
							bulletsToRemove.add(b);
						}
					}
					
					// Nova Blast Destroying Void Hazards
					for (VoidHazard v : voidHazards) {
						double dx = v.getCenterX() - ship.getX();
						double dy = v.getCenterY() - ship.getY();
						double dist = Math.sqrt(dx * dx + dy * dy);
						if (dist < novaRadius) {
							particleSystem.createVoidExplosion(
								v.getCenterX(),
								v.getCenterY()
							);
							voidHazardsToRemove.add(v);
						}
					}
					
					// Nova Blast Destroying Black Holes
					for (BlackHole bh : blackHoles) {
						double dx = bh.getX() - ship.getX();
						double dy = bh.getY() - ship.getY();
						double dist = Math.sqrt(dx * dx + dy * dy);
						if (dist < novaRadius) {
							particleSystem.createBlackHoleExplosion(
								bh.getX(),
								bh.getY()
							);
							blackHolesToRemove.add(bh);
						}
					}
					
					// Nova Blast Destroying Cosmic Entities
					for (CosmicEntity ce : cosmicEntities) {
						double dx = ce.getX() - ship.getX();
						double dy = ce.getY() - ship.getY();
						double dist = Math.sqrt(dx * dx + dy * dy);
						if (dist < novaRadius) {
							particleSystem.createCosmicEntityExplosion(
								ce.getX(),
								ce.getY()
							);
							cosmicEntitiesToRemove.add(ce);
						}
					}
					
					// Consume void energy
					if (abilityManager.getNovaExplosionRadius() > 50) { // First frame
						ship.getVoidEnergy().reset();
					}
				}
				// Check Fuel
				if (ship.getFuel() > 1000) {
					ship.setFuel(1000);
				}
                
                bullets.removeAll(bulletsToRemove);
                asteroids.removeAll(asteroidsToRemove);
                asteroids.addAll(newAsteroids);
                voidHazards.removeAll(voidHazardsToRemove);
                blackHoles.removeAll(blackHolesToRemove);
                cosmicEntities.removeAll(cosmicEntitiesToRemove);
            	
            	repaint();
            }
            case "death" -> {
                deathCountdown--;
                if (deathCountdown <= 0) {
                    gameState = "menu";
                    setupMenu();
                }
                // Death explosion animation
                if (!deathExplosionDone && deathExplosionFrame < 60) {
                    if (deathExplosionFrame % 5 == 0) {
                        particleSystem.createShipExplosion(ship.getX(), ship.getY());
                    }
                    deathExplosionFrame++;
                } else {
                    deathExplosionDone = true;
                }
                particleSystem.update();
            }
            default -> {
            }
        }
	    repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	
        switch (gameState) {
            case "menu" -> {
				// Animated gradient background
				drawGradientBackground(g2, new Color(10, 5, 30), new Color(30, 10, 50));
				
				// Draw meteors
				for (Meteor m : meteors) { m.draw(g2);}
				
				// Draw starfield
				starField.draw(g2);
				
				// Animated moving grid lines
				g2.setColor(new Color(100, 50, 150, 30));
				g2.setStroke(new BasicStroke(1));
				for (int i = 0; i < 20; i++) {
					int y = (i * 50 + menuBackgroundShift) % HEIGHT;
					g2.drawLine(0, y, WIDTH, y);
				}
				
				// Title with glow and pulse effect
				Font titleFont = new Font("Serif", Font.BOLD, 80);
				g2.setFont(titleFont);
				FontMetrics titleFm = g2.getFontMetrics();
				String title = "ASTEROIDS";
				int titleX = WIDTH / 2 - titleFm.stringWidth(title) / 2;
				int titleY = 180;
				
				// Pulsing scale effect
				float pulseScale = 1.0f + (float) Math.sin(menuTitlePulse) * 0.05f;
				AffineTransform oldTransform = g2.getTransform();
				g2.translate(WIDTH / 2, titleY);
				g2.scale(pulseScale, pulseScale);
				g2.translate(-WIDTH / 2, -titleY);
				
				// Outer glow layers
				for (int i = 5; i > 0; i--) {
					int alpha = 30 - i * 5;
					g2.setColor(new Color(150, 50, 255, alpha));
					g2.drawString(title, titleX - i, titleY - i);
					g2.drawString(title, titleX + i, titleY + i);
				}
				
				// Main title with gradient effect
				GradientPaint titleGradient = new GradientPaint(
					titleX, titleY - 40, new Color(200, 150, 255),
					titleX, titleY + 40, new Color(150, 100, 255)
				);
				g2.setPaint(titleGradient);
				g2.drawString(title, titleX, titleY);
				
				// Subtitle
				g2.setTransform(oldTransform);
				g2.setFont(new Font("Serif", Font.ITALIC, 24));
				g2.setColor(new Color(180, 150, 255));
				String subtitle = "Navigate the Void";
				int subtitleWidth = g2.getFontMetrics().stringWidth(subtitle);
				g2.drawString(subtitle, WIDTH / 2 - subtitleWidth / 2, titleY + 50);
				
				// Instructions popup
				if (showingInstructions) { drawInstructionsPopup(g2); }
				// Control Panel popup
				if (showingControlPanel) { controlPanel.paintComponent(g2); }
				
				// Buttons
				if (!showingInstructions && !showingControlPanel) {
					drawMenuButton(g2, "New Game", HEIGHT/2 - 20, hoveredButton == 0, true);
					drawMenuButton(g2, "Resume", HEIGHT/2 + 50, hoveredButton == 1, !noSavedGame);
					drawMenuButton(g2, "Instructions", HEIGHT/2 + 120, hoveredButton == 2, true);
					drawMenuButton(g2, "Controls", HEIGHT/2 + 190, hoveredButton == 3, true);
				}
				
				// Error message with fade effect
				if (noSavedGame && !showingInstructions && !showingControlPanel) {
					g2.setFont(new Font("Serif", Font.BOLD, 20));
					g2.setColor(new Color(255, 80, 80, errorAlpha));
					String errorMessage = "⚠ No saved progress found";
					int errorX = WIDTH / 2 - g2.getFontMetrics().stringWidth(errorMessage) / 2;
					g2.drawString(errorMessage, errorX, HEIGHT / 2 - 60);
				}
            }
            case "playing" -> {
				// Background
				if (ship.getVoidEnergy().isActive()) {
					drawGradientBackground(g2, 
						new Color(20, 5, 40), 
						new Color(40, 10, 60)
					);
				} else {
					drawGradientBackground(g2, 
						new Color(5, 5, 15), 
						new Color(15, 10, 25)
					);
				}
				
				// Draw screen overlays (simulate ship's HUD/window)
				if (fadeActive) {
					drawFadeScreenOverlay(g2);
				} else if (ship.getVoidEnergy().isActive()) {
					drawVoidScreenOverlay(g2);
				}
				
				// Draw void hazards
				for (VoidHazard vh : voidHazards) { vh.draw(g2, ship.getVoidEnergy().isActive()); }

				// Draw black holes
				for (BlackHole bh : blackHoles) { bh.draw(g2); }

				// Draw cosmic entities
				for (CosmicEntity ce : cosmicEntities) { ce.draw(g2); }
				
				// Draw starfield
				starField.draw(g2);
				
				// Draw particles
				particleSystem.draw(g2);

				//Draw teleport anchor
				teleportAnchor.drawAnchor(g2);

				// Draw shield effect
				abilityManager.drawShieldEffect(g2, ship.getX(), ship.getY());

				// Draw time slow effect
				abilityManager.drawTimeSlowEffect(g2, WIDTH, HEIGHT);

				// Draw drone
				abilityManager.drawDrone(g2, ship.getX(), ship.getY());

				// Draw nova blast
				abilityManager.drawNovaBlast(g2, WIDTH, HEIGHT, ship.getX(), ship.getY());

				// Draw floating texts
				floatingTextManager.draw(g2);

				// Draw ability bar
				abilityManager.drawAbilityBar(g2, WIDTH, HEIGHT);

				// Draw level UI
				shipLevel.drawLevelUI(g2, WIDTH, HEIGHT);

				// Draw teleport flash
				if (teleportFlashFrame > 0) { teleportAnchor.drawTeleportFlash(g2, WIDTH, HEIGHT, teleportFlashFrame); }

				// Draw level up effect
				if (showingLevelUp) { shipLevel.drawLevelUpEffect(g2, (int) ship.getX(), (int) ship.getY(), WIDTH, HEIGHT, levelUpFrame); }

				// Draw asteroids with enhanced effects
				if (ship.getVoidEnergy().isActive()) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
				}
				for (Asteroid a : asteroids) { 
					a.draw(g2);
					
					// Add glow to asteroids in void mode
					if (ship.getVoidEnergy().isActive()) {
						Rectangle bounds = a.getBounds().getBounds();
						int glowSize = bounds.width + 10;
						g2.setColor(new Color(100, 0, 150, 30));
						g2.fillOval(
							bounds.x + bounds.width / 2 - glowSize / 2,
							bounds.y + bounds.height / 2 - glowSize / 2,
							glowSize, glowSize
						);
					}
				}
				if (ship.getVoidEnergy().isActive()) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}

				// Draw ship
				ship.draw(g);

				// Draw bullets
				if (ship.getVoidEnergy().isActive()) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
				}
				for (Bullet b : bullets) { 
					b.draw(g2); 
				}
				if (ship.getVoidEnergy().isActive()) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				}

				// Draw void lasers
				for (VoidLaser laser : voidLasers) {
					laser.draw(g2);
				}
				
				// Enhanced HUD
				drawEnhancedHUD(g2);
				
				// Level starting countdown
				if (levelStarting) {
					drawLevelTransition(g2);
				}
				
				// Animated void overlay effect
				if (ship.getVoidEnergy().isActive()) {
					// Animated void overlay with wave effect
					long time = System.currentTimeMillis();
					for (int i = 0; i < HEIGHT; i += 10) {
						int waveOffset = (int) (Math.sin((i + time / 50.0) / 20.0) * 5);
						int alpha = 50 + (int) (Math.sin((i + time / 100.0) / 15.0) * 20);
						
						// Clamp alpha to valid range
						alpha = Math.max(0, Math.min(255, alpha));
						
						g2.setColor(new Color(80, 0, 120, alpha));
						g2.fillRect(0, i + waveOffset, WIDTH, 10);
					}
				}
            }
            case "paused" -> {
                // Dark overlay
				g2.setColor(new Color(0, 0, 0, 200));
				g2.fillRect(0, 0, WIDTH, HEIGHT);
				
				// Centered panel
				int panelWidth = 400;
				int panelHeight = 300;
				int panelX = WIDTH / 2 - panelWidth / 2;
				int panelY = HEIGHT / 2 - panelHeight / 2;
				
				g2.setColor(new Color(20, 25, 40, 250));
				g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
				
				g2.setColor(new Color(100, 150, 255));
				g2.setStroke(new BasicStroke(2));
				g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
				g2.setStroke(new BasicStroke(1));
				
				// Title
				Font pausedFont = new Font("Arial", Font.BOLD, 40);
				g.setFont(pausedFont);
				g.setColor(new Color(150, 200, 255));
				String pausedText = "PAUSED";
				FontMetrics pfm = g.getFontMetrics();
				g.drawString(pausedText, WIDTH / 2 - pfm.stringWidth(pausedText) / 2, panelY + 80);
				
				// Stats
				g2.setFont(new Font("Arial", Font.PLAIN, 16));
				g2.setColor(new Color(200, 200, 220));
				int statsY = panelY + 140;
				
				String[] stats = {
					"Level: " + level,
					"Score: " + score,
					"Fuel: " + (int)ship.getFuel(),
					"Ship Level: " + shipLevel.getLevel()
				};
				
				for (String stat : stats) {
					FontMetrics fm = g2.getFontMetrics();
					g2.drawString(stat, WIDTH / 2 - fm.stringWidth(stat) / 2, statsY);
					statsY += 25;
				}
				
				// Resume instruction
				Font resumeFont = new Font("Arial", Font.PLAIN, 15);
				g.setFont(resumeFont);
				g.setColor(new Color(150, 200, 255));
				String resumeText = "Press ESC to Resume";
				FontMetrics metrics = g.getFontMetrics(resumeFont);
				g.drawString(resumeText, WIDTH / 2 - metrics.stringWidth(resumeText) / 2, panelY + panelHeight - 40);
				String menuText = "Press ENTER to Return to Menu";
				FontMetrics menuMetrics = g.getFontMetrics(resumeFont);
				g.drawString(menuText, WIDTH / 2 - menuMetrics.stringWidth(menuText) / 2, panelY + panelHeight - 15);
            }
            case "death" -> {
				// Dark gradient background
				drawGradientBackground(g2, 
					new Color(20, 5, 10), 
					new Color(40, 10, 20)
				);
				
				// Draw particles (explosion debris)
				particleSystem.draw(g2);
				
				// Flashing red overlay for first second
				if (deathCountdown > 4 * 60) {
					int flashAlpha = (int) (100 * Math.sin((5 * 60 - deathCountdown) * 0.5));
					// Clamp alpha to valid range
					flashAlpha = Math.max(0, Math.min(255, flashAlpha));
					g2.setColor(new Color(255, 0, 0, flashAlpha));
					g2.fillRect(0, 0, WIDTH, HEIGHT);
				}
				
				// "YOU DIED" with dramatic effect
				g2.setFont(new Font("Serif", Font.BOLD, 90));
				FontMetrics deathFm = g2.getFontMetrics();
				String deathText = "YOU DIED";
				int deathTextX = WIDTH / 2 - deathFm.stringWidth(deathText) / 2;
				int deathTextY = HEIGHT / 4;
				
				// Glitch effect for first 2 seconds
				if (deathCountdown > 3 * 60) {
					int glitchOffset = rand.nextInt(10) - 5;
					g2.setColor(new Color(255, 0, 0, 100));
					g2.drawString(deathText, deathTextX + glitchOffset, deathTextY);
					g2.setColor(new Color(0, 255, 255, 100));
					g2.drawString(deathText, deathTextX - glitchOffset, deathTextY);
				}
				
				// Main death text with shadow
				g2.setColor(new Color(100, 0, 0));
				g2.drawString(deathText, deathTextX + 4, deathTextY + 4);
				g2.setColor(new Color(255, 50, 50));
				g2.drawString(deathText, deathTextX, deathTextY);
				
				// Death cause with color coding
				g2.setFont(new Font("Serif", Font.PLAIN, 28));
				FontMetrics causeFm = g2.getFontMetrics();
				
				String deathCause;
				Color causeColor;
				if (voidDeath) {
					deathCause = "CONSUMED BY THE VOID";
					causeColor = new Color(180, 0, 255);
				} else if (bulletDeath) {
					deathCause = "FRIENDLY FIRE";
					causeColor = new Color(255, 200, 0);
				} else if (asteroidDeath) {
					deathCause = "ASTEROID IMPACT";
					causeColor = new Color(150, 150, 150);
				} else if (voidHazardDeath) {
					deathCause = "CONSUMED BY VOID HAZARD";
					causeColor = new Color(180, 0, 255);
				} else if (blackHoleDeath) {
					deathCause = "CONSUMED BY BLACK HOLE";
					causeColor = new Color(0, 0, 0);
				} else if (cosmicEntityDeath) {
					deathCause = "CONSUMED BY COSMIC ENTITY";
					causeColor = new Color(255, 0, 255);
				} else {
					deathCause = "UNKNOWN CAUSE";
					causeColor = new Color(200, 200, 200);
				}
				
				int causeX = WIDTH / 2 - causeFm.stringWidth(deathCause) / 2;
				int causeY = deathTextY + 60;
				
				// Cause with glow
				for (int i = 3; i > 0; i--) {
					g2.setColor(new Color(causeColor.getRed(), causeColor.getGreen(), causeColor.getBlue(), 30));
					g2.drawString(deathCause, causeX - i, causeY - i);
					g2.drawString(deathCause, causeX + i, causeY + i);
				}
				g2.setColor(causeColor);
				g2.drawString(deathCause, causeX, causeY);
				
				// Stats panel with improved styling
				int panelY = HEIGHT / 2 + 20;
				int panelWidth = 500;
				int panelHeight = 250;
				int panelX = WIDTH / 2 - panelWidth / 2;
				
				// Panel background with border
				g2.setColor(new Color(30, 20, 40, 200));
				g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
				g2.setColor(new Color(150, 100, 200));
				g2.setStroke(new BasicStroke(2));
				g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
				g2.setStroke(new BasicStroke(1));
				
				// Stats header
				g2.setFont(new Font("Serif", Font.BOLD, 26));
				g2.setColor(new Color(200, 180, 255));
				String statsHeader = "MISSION STATISTICS";
				int headerWidth = g2.getFontMetrics().stringWidth(statsHeader);
				g2.drawString(statsHeader, WIDTH / 2 - headerWidth / 2, panelY + 35);
				
				// Separator line
				g2.setColor(new Color(150, 100, 200));
				g2.drawLine(panelX + 30, panelY + 50, panelX + panelWidth - 30, panelY + 50);
				
				// Stats content
				g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
				int statsStartY = panelY + 80;
				int lineSpacing = 30;
				
				String[][] stats = {
					{"Final Score:", String.valueOf(score), "255, 215, 0"},
					{"Large Destroyed:", String.valueOf(largeDestroyed), "255, 100, 100"},
					{"Medium Destroyed:", String.valueOf(mediumDestroyed), "100, 255, 100"},
					{"Small Destroyed:", String.valueOf(smallDestroyed), "100, 200, 255"},
					{"Fuel Remaining:", String.valueOf((int) ship.getFuel()), "255, 150, 50"}
				};
				
				for (int i = 0; i < stats.length; i++) {
					String label = stats[i][0];
					String value = stats[i][1];
					String[] rgb = stats[i][2].split(", ");
					Color valueColor = new Color(
						Integer.parseInt(rgb[0]), 
						Integer.parseInt(rgb[1]), 
						Integer.parseInt(rgb[2])
					);
					
					int y = statsStartY + i * lineSpacing;
					
					// Label
					g2.setColor(new Color(180, 180, 200));
					g2.drawString(label, panelX + 40, y);
					
					// Value with highlight
					FontMetrics statsFm = g2.getFontMetrics();
					int labelWidth = statsFm.stringWidth(label);
					g2.setColor(valueColor);
					g2.drawString(value, panelX + 40 + labelWidth + 10, y);
				}
				
				// Countdown timer at bottom
				g2.setFont(new Font("Serif", Font.PLAIN, 20));
				int secondsLeft = deathCountdown / 60 + 1;
				String countdown = "Returning in " + secondsLeft + "s (Press ENTER to skip)";
				FontMetrics countdownFm = g2.getFontMetrics();
				
				// Pulsing countdown
				float pulseAlpha = 0.5f + (float) Math.sin(deathCountdown * 0.1) * 0.3f;
				g2.setColor(new Color(200, 200, 200, (int) (255 * pulseAlpha)));
				g2.drawString(countdown, 
					WIDTH / 2 - countdownFm.stringWidth(countdown) / 2, 
					HEIGHT - 60);
            }
            default -> {
            }
        }
	}

	private void drawEnhancedHUD(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (!levelStarting) {
			// Top bar
			g2.setColor(new Color(20, 10, 40, 160));
			g2.fillRect(10, 10, WIDTH - 20, 40);
			g2.setColor(new Color(100, 50, 150));
			g2.setStroke(new BasicStroke(1));
			g2.drawRect(10, 10, WIDTH - 20, 40);

			// Level text
			g2.setFont(new Font("Arial", Font.BOLD, 20));
			g2.setColor(new Color(255, 215, 0));
			g2.drawString("LVL " + level, 30, 40);
			
			// --- Time bar (center) ---
			int timerBarWidth = 250;
			int timerBarHeight = 20;
			int timerBarX = WIDTH / 2 - timerBarWidth / 2;
			int timerBarY = 20;
			
			// Fill based on time remaining
			float timePercent = (float) levelTimer / (30 * 60);
			int fillWidth = (int) (timerBarWidth * timePercent);
			int secondsLeft = levelTimer / 60;

			// Outer border with glow
			g2.setColor(new Color(0, 200, 255, 150));
			g2.setStroke(new BasicStroke(2));
			g2.drawRect(timerBarX - 2, timerBarY - 2, timerBarWidth + 4, timerBarHeight + 4);

			// Corner accents
			int cornerSize = 8;
			g2.setColor(new Color(0, 255, 255, 200));
			g2.fillRect(timerBarX - 2, timerBarY - 2, cornerSize, 2);
			g2.fillRect(timerBarX - 2, timerBarY - 2, 2, cornerSize);
			g2.fillRect(timerBarX + timerBarWidth - cornerSize + 2, timerBarY - 2, cornerSize, 2);
			g2.fillRect(timerBarX + timerBarWidth, timerBarY - 2, 2, cornerSize);
			g2.fillRect(timerBarX - 2, timerBarY + timerBarHeight - cornerSize + 2, 2, cornerSize);
			g2.fillRect(timerBarX - 2, timerBarY + timerBarHeight, cornerSize, 2);
			g2.fillRect(timerBarX + timerBarWidth, timerBarY + timerBarHeight - cornerSize + 2, 2, cornerSize);
			g2.fillRect(timerBarX + timerBarWidth - cornerSize + 2, timerBarY + timerBarHeight, cornerSize, 2);
			
			// Background with grid pattern
			g2.setColor(new Color(10, 20, 30, 200));
			g2.fillRect(timerBarX, timerBarY, timerBarWidth, timerBarHeight);

			// Grid lines
			g2.setColor(new Color(0, 100, 150, 50));
			for (int i = 0; i < timerBarWidth; i += 20) {
				g2.drawLine(timerBarX + i, timerBarY, timerBarX + i, timerBarY + timerBarHeight);
			}

			// Determine color based on time
			Color timeColor;
			if (timePercent > 0.5f) {
				timeColor = new Color(0, 200, 255); // Cyan
			} else if (timePercent > 0.25f) {
				timeColor = new Color(255, 150, 0); // Orange
			} else {
				// Pulsing red when critical
				int pulse = (int) (150 + 105 * Math.sin(System.currentTimeMillis() / 200.0));
				timeColor = new Color(255, 0, 0, pulse);
			}

			// Filled portion with gradient
			GradientPaint fillGradient = new GradientPaint(
				timerBarX, timerBarY, timeColor,
				timerBarX, timerBarY + timerBarHeight, new Color(timeColor.getRed() / 2, timeColor.getGreen() / 2, timeColor.getBlue() / 2)
			);
			g2.setPaint(fillGradient);
			g2.fillRect(timerBarX, timerBarY, fillWidth, timerBarHeight);
			
			// Animated scan line
			long time = System.currentTimeMillis();
			int scanPos = (int) ((time / 10) % timerBarWidth);
			g2.setColor(new Color(0, 255, 255, 100));
			g2.fillRect(timerBarX + scanPos, timerBarY, 2, timerBarHeight);
			
			// Animated clock icon
			int iconX = timerBarX - 20;
			int iconY = timerBarY + timerBarHeight / 2;

			// Clock circle
			g2.setColor(new Color(90, 110, 70));
			g2.fillOval(iconX - 10, iconY - 10, 20, 20);
			g2.setColor(new Color(200, 200, 180));
			g2.fillOval(iconX - 9, iconY - 9, 18, 18);

			// Rotating clock hand (pointer)
			double handAngle = (1.0 - timePercent) * 2 * Math.PI - Math.PI / 2; // Start at 12 o'clock, go clockwise
			int handLength = 5;
			int handX = iconX + (int)(handLength * Math.cos(handAngle));
			int handY = iconY + (int)(handLength * Math.sin(handAngle));

			g2.setColor(new Color(90, 110, 70));
			g2.setStroke(new BasicStroke(1.5f));
			g2.drawLine(iconX, iconY, handX, handY);
			g2.setStroke(new BasicStroke(1));

			// Time text with digital style
			g2.setFont(new Font("Times New Roman", Font.BOLD, 20));
			//g2.setColor(new Color(0, 255, 255));
			String timeText = String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60);
			FontMetrics fm = g2.getFontMetrics();

			// Time Text
			g2.setColor(new Color(200, 200, 180));
			g2.drawString(timeText, timerBarX + timerBarWidth + 8, timerBarY + timerBarHeight - 4);

			g2.setStroke(new BasicStroke(1)); // Reset stroke
			
			// --- Score and Fuel (right) ---
			g2.setFont(new Font("Arial", Font.PLAIN, 15));
			String scoreText = "Score: " + score;
			String fuelText = "Fuel: " + (int) ship.getFuel();

			int rightX = WIDTH - 30;
			FontMetrics rightFm = g2.getFontMetrics();

			g2.setColor(new Color(255, 215, 0));
			g2.drawString(scoreText, rightX - rightFm.stringWidth(scoreText), 25);

			g2.setColor(new Color(255, 150, 50));
			g2.drawString(fuelText, rightX - rightFm.stringWidth(fuelText), 40);
		}
		
		// --- Void energy bar (always show if > 0) ---
		ship.getVoidEnergy().drawBar(g2, WIDTH, HEIGHT);

		// --- Fade timer display ---
		if (fadeActive) {
			g2.setFont(new Font("Serif", Font.BOLD, 20));
			int fadeSecondsLeft = fadeTimer / 60 + 1;
			String fadeText = "FADE: " + fadeSecondsLeft + "s";
			
			// Pulsing effect
			int pulse = (int) (200 + 55 * Math.sin(System.currentTimeMillis() / 150.0));
			g2.setColor(new Color(255, 255, 0, pulse));
			
			FontMetrics fadeFm = g2.getFontMetrics();
			int fadeTextX = WIDTH / 2 - fadeFm.stringWidth(fadeText) / 2;
			int fadeTextY = HEIGHT - 100;
			
			// Background
			g2.setColor(new Color(0, 0, 0, 150));
			g2.fillRoundRect(fadeTextX - 10, fadeTextY - fadeFm.getAscent(), 
				fadeFm.stringWidth(fadeText) + 20, fadeFm.getHeight(), 10, 10);
			
			// Text
			g2.setColor(new Color(255, 255, 0, pulse));
			g2.drawString(fadeText, fadeTextX, fadeTextY);
			}
		}

		// Level transitions
		private void drawLevelTransition(Graphics2D g2) {
		// Minimal dark overlay
		int overlayAlpha = Math.min(200, levelDisplayAlpha);
		g2.setColor(new Color(0, 0, 0, overlayAlpha / 2));
		g2.fillRect(0, 0, WIDTH, HEIGHT);
		
		// Clean level text
		String levelText = "LEVEL " + (level + 1);
		g2.setFont(new Font("Times New Roman", Font.BOLD, 80));
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, levelDisplayAlpha / 255f));
		
		FontMetrics levelFm = g2.getFontMetrics();
		int textWidth = levelFm.stringWidth(levelText);
		int levelY = HEIGHT / 2;
		
		g2.setColor(new Color(255, 215, 0));
		g2.drawString(levelText, WIDTH / 2 - textWidth / 2, levelY);
		
		// Simple countdown
		g2.setFont(new Font("Arial", Font.PLAIN, 20));
		int secondsLeft = (levelStartCountdown / 60) + 1;
		String countdownText = "Starting in " + secondsLeft;
		FontMetrics fm = g2.getFontMetrics();
		g2.setColor(Color.WHITE);
		g2.drawString(countdownText, WIDTH / 2 - fm.stringWidth(countdownText) / 2, levelY + 60);
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			
		// Speed warning
		if (level >= 3 && level <= 10) {
			g2.setFont(new Font("Serif", Font.ITALIC, 30));
			String warning = "⚠ Asteroids accelerating...";
			FontMetrics warnFm = g2.getFontMetrics();
			g2.setColor(new Color(255, 100, 100, levelDisplayAlpha));
			g2.drawString(warning, 
				WIDTH / 2 - warnFm.stringWidth(warning) / 2, 
				levelY + 70);
		}
		
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
	}

	private void toggleVoidWorld() {
		VoidEnergy ve = ship.getVoidEnergy();
		abilityManager.setVoidActive(!ve.isActive());
		if (ve.isActive()) {		// Check immediate collision with asteroids
			ve.deactivate();
			for (Asteroid a : asteroids) {
				if (a.intersects(ship.getBounds())) {
					asteroidDeath = true;
					startDeathSequence();
					return;
				}
			}
		} else {
			// Entering void world
			ve.activate();
		}
	}

	private void drawGradientBackground(Graphics2D g2d, Color top, Color bottom) {
		GradientPaint gradient = new GradientPaint(
			0, 0, top,
			0, HEIGHT, bottom
		);
		g2d.setPaint(gradient);
		g2d.fillRect(0, 0, WIDTH, HEIGHT);
	}

	private void drawVoidScreenOverlay(Graphics2D g2) {
		long time = System.currentTimeMillis();
		
		// Purple hexagonal grid pattern
		g2.setColor(new Color(150, 0, 255, 30));
		g2.setStroke(new BasicStroke(1));
		
		int hexSize = 40;
		for (int y = 0; y < HEIGHT + hexSize; y += hexSize * 1.5) {
			for (int x = 0; x < WIDTH + hexSize; x += hexSize * 1.732) {
				int offsetX = (y / (int)(hexSize * 1.5)) % 2 == 0 ? 0 : (int)(hexSize * 0.866);
				drawHexagon(g2, x + offsetX, y, hexSize / 2);
			}
		}
		
		// Animated purple scan lines
		int scanY = (int) ((time / 15) % HEIGHT);
		for (int i = 0; i < 5; i++) {
			int y = (scanY + i * 20) % HEIGHT;
			int alpha = 100 - i * 15;
			g2.setColor(new Color(180, 0, 255, alpha));
			g2.drawLine(0, y, WIDTH, y);
		}
		
		// Corner HUD elements (purple)
		drawCornerHUD(g2, 20, 80, new Color(150, 0, 255, 150), "VOID");
		
		// Vignette effect
		RadialGradientPaint vignette = new RadialGradientPaint(
			WIDTH / 2, HEIGHT / 2, 
			Math.max(WIDTH, HEIGHT) / 1.5f,
			new float[]{0.0f, 1.0f},
			new Color[]{new Color(0, 0, 0, 0), new Color(80, 0, 120, 100)}
		);
		g2.setPaint(vignette);
		g2.fillRect(0, 0, WIDTH, HEIGHT);
	}

	private void drawFadeScreenOverlay(Graphics2D g2) {
		long time = System.currentTimeMillis();
		
		// Golden circuit-like pattern
		g2.setColor(new Color(255, 215, 0, 40));
		g2.setStroke(new BasicStroke(1));
		
		// Horizontal lines with nodes
		for (int y = 50; y < HEIGHT; y += 80) {
			int alpha = (int) (30 + 20 * Math.sin(time / 200.0 + y / 50.0));
			g2.setColor(new Color(255, 215, 0, alpha));
			g2.drawLine(0, y, WIDTH, y);
			
			// Nodes
			for (int x = 100; x < WIDTH; x += 150) {
				g2.fillOval(x - 3, y - 3, 6, 6);
			}
		}
		
		// Vertical lines with nodes
		for (int x = 80; x < WIDTH; x += 100) {
			int alpha = (int) (30 + 20 * Math.sin(time / 250.0 + x / 50.0));
			g2.setColor(new Color(255, 215, 0, alpha));
			g2.drawLine(x, 0, x, HEIGHT);
			
			// Nodes
			for (int y = 100; y < HEIGHT; y += 150) {
				g2.fillOval(x - 3, y - 3, 6, 6);
			}
		}
		
		// Animated yellow scan lines (faster than void)
		int scanY = (int) ((time / 10) % HEIGHT);
		for (int i = 0; i < 3; i++) {
			int y = (scanY + i * 30) % HEIGHT;
			int alpha = 80 - i * 20;
			g2.setColor(new Color(255, 255, 0, alpha));
			g2.drawLine(0, y, WIDTH, y);
		}
		
		// Corner HUD elements (yellow/golden)
		drawCornerHUD(g2, 20, 80, new Color(255, 215, 0, 150), "FADE");
		
		// Lighter vignette for fade
		RadialGradientPaint vignette = new RadialGradientPaint(
			WIDTH / 2, HEIGHT / 2,
			Math.max(WIDTH, HEIGHT) / 1.3f,
			new float[]{0.0f, 1.0f},
			new Color[]{new Color(0, 0, 0, 0), new Color(255, 215, 0, 60)}
		);
		g2.setPaint(vignette);
		g2.fillRect(0, 0, WIDTH, HEIGHT);
	}

	private void drawHexagon(Graphics2D g2, int centerX, int centerY, int radius) {
		Polygon hexagon = new Polygon();
		for (int i = 0; i < 6; i++) {
			double angle = Math.PI / 3 * i;
			int x = (int) (centerX + radius * Math.cos(angle));
			int y = (int) (centerY + radius * Math.sin(angle));
			hexagon.addPoint(x, y);
		}
		g2.drawPolygon(hexagon);
	}

	private void drawCornerHUD(Graphics2D g2, int cornerSize, int offset, Color color, String mode) {
		// Top-left
		g2.setColor(color);
		g2.setStroke(new BasicStroke(2));
		g2.drawLine(offset, offset, offset + cornerSize, offset);
		g2.drawLine(offset, offset, offset, offset + cornerSize);
		
		// Top-right
		g2.drawLine(WIDTH - offset, offset, WIDTH - offset - cornerSize, offset);
		g2.drawLine(WIDTH - offset, offset, WIDTH - offset, offset + cornerSize);
		
		// Bottom-left
		g2.drawLine(offset, HEIGHT - offset, offset + cornerSize, HEIGHT - offset);
		g2.drawLine(offset, HEIGHT - offset, offset, HEIGHT - offset - cornerSize);
		
		// Bottom-right
		g2.drawLine(WIDTH - offset, HEIGHT - offset, WIDTH - offset - cornerSize, HEIGHT - offset);
		g2.drawLine(WIDTH - offset, HEIGHT - offset, WIDTH - offset, HEIGHT - offset - cornerSize);
		
		// Mode indicator (top-left corner)
		g2.setFont(new Font("Monospaced", Font.BOLD, 12));
		g2.drawString(mode, offset + 5, offset + cornerSize + 15);
		
		g2.setStroke(new BasicStroke(1));
	}

	private void activateAbility(int slot) {
		String ability = abilityLoadout.getSlot(slot);
		if (ability == null) return;
		
		switch (ability) {
			case "void" -> { 
				// Void toggle - only if unlocked and not on cooldown
				if (shipLevel.isAbilityUnlocked("void") && voidCooldownTimer == 0) {
					toggleVoidWorld();
					if (!ship.getVoidEnergy().isActive()) {
						// Started cooldown when exiting
						voidCooldownTimer = voidMaxCooldown;
						abilityManager.startVoidCooldown();
					}
				}
			}
			case "fade" -> { 
				// Fade toggle - only if unlocked and not on cooldown
				if (shipLevel.isAbilityUnlocked("fade") && !fadeActive && fadeCooldownTimer == 0) {
					fadeActive = true;
					fadeTimer = maxFadeTime;
				}
			}
			case "teleport" -> {
				if (shipLevel.isAbilityUnlocked("teleport")) {
					if (!teleportAnchor.isAnchorPlaced() && !teleportAnchor.isOnCooldown()) {
						// Place anchor
						teleportAnchor.placeAnchor(ship.getX(), ship.getY());
					} else if (teleportAnchor.isAnchorPlaced()) {
						// Teleport to anchor
						if (teleportAnchor.teleportToAnchor(ship)) {
							teleportFlashFrame = 15;
							abilityManager.startTeleportCooldown();
						}
					}
				}
			}
			case "shield" -> { if (shipLevel.isAbilityUnlocked("shield")) abilityManager.activateShield(); }
			case "timeslow" -> { if (shipLevel.isAbilityUnlocked("timeslow")) abilityManager.activateTimeSlow(); }
			case "drone" -> { if (shipLevel.isAbilityUnlocked("drone")) abilityManager.toggleDrone(); }
			case "nova" -> { if (shipLevel.isAbilityUnlocked("nova")) abilityManager.activateNova(ship.getVoidEnergy()); }
		}
	}

	private void handleShoot() {
		if (fadeActive) return;
		if (ship.getVoidEnergy().getEnergy() > 0) {
			Point2D.Double tip = ship.getTipPosition();
			voidLasers.add(new VoidLaser(tip.x, tip.y, ship.getAngle(), WIDTH, HEIGHT));
			if (ship.getVoidEnergy().isActive()) ship.getVoidEnergy().absorbForAction(2.0);
			else ship.getVoidEnergy().dissipateForAction(5.0);
		} else {
			Point2D.Double tip = ship.getTipPosition();
			double spawnX = tip.x + 30 * Math.sin(ship.getAngle());
			double spawnY = tip.y - 30 * Math.cos(ship.getAngle());
			bullets.add(new Bullet(spawnX, spawnY, ship.getAngle()));
		}
	}

    @Override
    public void keyPressed(KeyEvent e) {
        switch (gameState) {
            case "menu" -> {
				switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER -> startNewGame();
                    case KeyEvent.VK_I -> showingInstructions = !showingInstructions;
					case KeyEvent.VK_C -> {
						if (showingControlPanel) {
							showingControlPanel = false;
							remove(controlPanel);
							requestFocusInWindow();
							revalidate();
							repaint();
						} else {
							showingControlPanel = true;
							add(controlPanel);
							controlPanel.requestFocusInWindow();
							revalidate();
							repaint();
						}
					}
					case KeyEvent.VK_ESCAPE -> {
						if (showingInstructions) {
							showingInstructions = false;
							instructionPage = 0;
						}
						if (showingControlPanel) {
							showingControlPanel = false;
							remove(controlPanel);
							requestFocusInWindow();
							revalidate();
							repaint();
						}
					}
					case KeyEvent.VK_LEFT -> {
						if (showingInstructions && instructionPage > 0) {
							instructionPage--;
						}
					}
					case KeyEvent.VK_RIGHT -> {
						if (showingInstructions && instructionPage < totalInstructionPages - 1) {
							instructionPage++;
						}
					}
					case KeyEvent.VK_S -> {
						if (noSavedGame) {
							// No saved game to load
							break;
						}
						resumeGame();
						gameState = "playing";
                	}
				}
            }
			case "playing" -> {
				int key = e.getKeyCode();
				if (key == controlConfig.getKey("thrust")) thrusting = true;
				else if (key == controlConfig.getKey("left")) rotatingLeft = true;
				else if (key == controlConfig.getKey("right")) rotatingRight = true;
				else if (key == controlConfig.getKey("hyper")) ship.setHyper(true);
				else if (key == controlConfig.getKey("shoot")) handleShoot();
				else if (key == controlConfig.getKey("ability1")) activateAbility(0);
				else if (key == controlConfig.getKey("ability2")) activateAbility(1);
				else if (key == controlConfig.getKey("ability3")) activateAbility(2);
				else if (key == controlConfig.getKey("ability4")) activateAbility(3);
				else if (key == KeyEvent.VK_ESCAPE) {
					gameState = "paused";
                    noSavedGame = false;
                    saveGame();
				}
			}
            case "paused" -> {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ESCAPE -> gameState = "playing";
					case KeyEvent.VK_ENTER -> {
						gameState = "menu";
						setupMenu();
						revalidate();
						repaint();
					}
                }
            }
            case "death" -> {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    deathCountdown = 0;
                    if (deathCountdown < 0) deathCountdown = 0;
                }
            }
            default -> {
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    	if (gameState.equals("playing")) {
    	    int key = e.getKeyCode();
	        if (key == controlConfig.getKey("thrust")) thrusting = false;
	        else if (key == controlConfig.getKey("left")) rotatingLeft = false;
	        else if (key == controlConfig.getKey("right")) rotatingRight = false;
	        else if (key == controlConfig.getKey("hyper")) ship.setHyper(false);
    	}
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Asteroid Game");
        AsteroidGame game = new AsteroidGame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(game);
        frame.pack();
        frame.setVisible(true);
    }
}
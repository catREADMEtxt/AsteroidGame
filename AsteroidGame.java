
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.awt.geom.Point2D;

public class AsteroidGame extends JPanel implements ActionListener, KeyListener {
	// Constants
    private static final int WIDTH = 1200, HEIGHT = 700;
   
    // Score and asteroid destruction counters
	private int score = 0;
	private int largeDestroyed = 0;
	private int mediumDestroyed = 0;
	private int smallDestroyed = 0;
	
    // Levels, Fades, and Countdown Timers
    private int deathCountdown = 5 * 60;  // 5 seconds at 60 FPS
    private int levelStartCountdown = 3 * 60; // countdown before level starts (1 second)
	private int levelDisplayAlpha = 255; // for fade out effect
	private int levelTimer = 30 * 60; // 30 seconds * 60 FPS (assuming 60 FPS)
	private int errorMessageCountdown = 3 * 60; // countdown for errorMessage display
	private boolean deathScreenActive = false;
	private boolean levelStarting = false; 
		
	// Asteroids
	private List<Asteroid> asteroids = new ArrayList<>();
	
	// Bullets
	private List<Bullet> bullets = new ArrayList<>();

	//thrust
    private boolean thrusting = false;
    private boolean rotatingLeft = false;
    private boolean rotatingRight = false;
    
    // Game General
    private Random rand = new Random();
    private boolean noSavedGame = false;
    private Ship ship;
    private Timer timer;
    private String gameState = "menu"; // "menu", "playing", "paused", "death"
    private int vx = 0, vy = 0;
    private int level = 0;
    private boolean bulletDeath;
    private boolean asteroidDeath;

    // UI Buttons
    private JButton newGameBtn, resumeBtn, backToMenuBtn;
	
	// Best Score & Duration
	private int highestScore, highestDuration;
    // Save system
    private File saveFile = new File("save.dat");

    public AsteroidGame() {
        ship = new Ship();
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        setupMenu();
    }

    void setupMenu() {
        setLayout(null);
        setBackground(Color.BLACK);
        
        // Buttons
        newGameBtn = new JButton("New Game"); // New Game Button
        newGameBtn.setBackground(Color.DARK_GRAY);
        newGameBtn.setForeground(Color.WHITE);
        newGameBtn.setFont(new Font("Times New Roman", Font.PLAIN, 25));
        newGameBtn.setBounds(WIDTH / 2 - 75, HEIGHT / 2 - 40, 150, 30);
        newGameBtn.addActionListener(e -> startNewGame());
        newGameBtn.setToolTipText("press to start a new game");        
        add(newGameBtn);

        resumeBtn = new JButton("Resume"); // Resume Button
        resumeBtn.setBackground(Color.DARK_GRAY);
        resumeBtn.setForeground(Color.WHITE);
        resumeBtn.setFont(new Font("Times New Roman", Font.PLAIN, 25));
        resumeBtn.setBounds(WIDTH / 2 - 75, HEIGHT / 2 + 10, 150, 30);
        resumeBtn.addActionListener(e -> resumeGame());
        resumeBtn.setToolTipText("press to resume a started game");
        add(resumeBtn);
        
        resumeBtn.addMouseListener(new MouseAdapter() {
        	public void mouseEntered(MouseEvent e) {resumeBtn.setBackground(Color.LIGHT_GRAY); resumeBtn.setForeground(Color.DARK_GRAY);}
        	public void mouseExited(MouseEvent e) {resumeBtn.setBackground(Color.DARK_GRAY); resumeBtn.setForeground(Color.WHITE);}
        	public void mousePressed(MouseEvent e) {}
        	public void mouseReleased(MouseEvent e) {}        	
        });
        
        newGameBtn.addMouseListener(new MouseAdapter() {
        	public void mouseEntered(MouseEvent e) {newGameBtn.setBackground(Color.LIGHT_GRAY); newGameBtn.setForeground(Color.DARK_GRAY);}
        	public void mouseExited(MouseEvent e) {newGameBtn.setBackground(Color.DARK_GRAY); newGameBtn.setForeground(Color.WHITE);}
        	public void mousePressed(MouseEvent e) {}
        	public void mouseReleased(MouseEvent e) {}        	
        });
    }
    
	void startNewGame() {
	    gameState = "playing";
	    level = 0;
		score = 0;
	    ship = new Ship();
	    asteroids.clear();
	    bullets.clear();
	    thrusting = rotatingLeft = rotatingRight = false;
	    bulletDeath = asteroidDeath = false;
	
	    // Remove menu buttons if present
	    if (newGameBtn.getParent() != null) remove(newGameBtn);
	    if (resumeBtn.getParent() != null) remove(resumeBtn);
	
	    levelStartCountdown = 3 * 60;
	    levelStarting = true;
	    levelDisplayAlpha = 255;
	    
	    // Clear Previous Game Progress
	    try (FileWriter writer = new FileWriter(saveFile, false)) {} catch (IOException e) { e.printStackTrace(); }

	    revalidate();
	    repaint();
	    requestFocusInWindow();
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
            
            gameState = "playing";
            remove(newGameBtn);
            remove(resumeBtn);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void setupPauseMenu() {
	    setLayout(null);
	
	    backToMenuBtn = new JButton("Menu");
	    backToMenuBtn.setBackground(Color.DARK_GRAY);
	    backToMenuBtn.setForeground(Color.WHITE);
	    backToMenuBtn.setFont(new Font("Times New Roman", Font.PLAIN, 25));
	    backToMenuBtn.setBounds(WIDTH / 2 - 75, HEIGHT / 2 + 100, 150, 30);
	    backToMenuBtn.setToolTipText("Press to return to Menu");
	
	    backToMenuBtn.addActionListener(e -> {
	        gameState = "menu";
	        remove(backToMenuBtn);  // clean up
	        setupMenu();
	        revalidate();
	        repaint();
	    });
	
	    backToMenuBtn.addMouseListener(new MouseAdapter() {
	        public void mouseEntered(MouseEvent e) {
	            backToMenuBtn.setBackground(Color.LIGHT_GRAY);
	            backToMenuBtn.setForeground(Color.DARK_GRAY);
	        }
	
	        public void mouseExited(MouseEvent e) {
	            backToMenuBtn.setBackground(Color.DARK_GRAY);
	            backToMenuBtn.setForeground(Color.LIGHT_GRAY);
	        }
	    });
	
	    add(backToMenuBtn);
	}
    
    public void checkAsteroidCollision() {
    	Iterator<Asteroid> it = asteroids.iterator();
		while (it.hasNext()) {
		    Asteroid a = it.next();
		    a.update();
		
		    if (a.intersects(ship.getBounds())) {
			    asteroidDeath = true;
			    startDeathSequence();
			    return;
			}
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

	    
	    deathScreenActive = true;
	    deathCountdown = 5 * 60;  // reset to 5 seconds at 60 FPS
	    gameState = "death";  // new game state for death screen
	    //bullets.clear();
	    //asteroids.clear();
	}	
    
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (gameState.equals("menu")) {
	    	if (noSavedGame) {
	    		errorMessageCountdown--;
	    		if (errorMessageCountdown <= 0) {
	    			noSavedGame = false;
	    			errorMessageCountdown = 3 * 60;
	    		}
	    	}
	    } else if (gameState.equals("playing")) {
			// Decrement the level timer every tick (frame)
		    if (levelStarting) {
	            levelStartCountdown--;
	            if (levelStartCountdown <= 0) {
	                levelStarting = false;
	                level++;
	                levelStartCountdown = 3 * 60;
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
	
	                // Generate more asteroids than before
	                for (int i = 0; i < level + 2; i++) {
	                    asteroids.add(Asteroid.randomAsteroid(WIDTH, HEIGHT));
	                    if (level > 3 && level < 10) { asteroids.get(0).setMultiplier(level * 0.5); }
	                }
	
	                // Show level text for 3 seconds (180 frames)
	                levelStarting = true;
	            }
	        }
	
	        // Thrust & Rotation
	        if (thrusting) ship.applyThrust();
	        if (rotatingLeft) ship.rotateLeft();
	        if (rotatingRight) ship.rotateRight();
	        ship.updatePhysics();
	
	        // Bullet & Asteroid Collision, Addition, and Removal
	        checkAsteroidCollision();
	
	        List<Bullet> toRemove = new ArrayList<>();
	        List<Asteroid> newAsteroids = new ArrayList<>();
	
	        for (Bullet bullet : bullets) {
	            bullet.update(WIDTH, HEIGHT);
	
	            for (Asteroid asteroid : asteroids) {
	                if (asteroid.getBounds().intersects(bullet.getBounds().getBounds2D())) {
	                    toRemove.add(bullet);
	                    	
	                    if (asteroid.getSize() == Asteroid.Size.SMALL) {
	                        asteroids.remove(asteroid);
	                    } else {
	                        asteroids.remove(asteroid);
	                        newAsteroids.addAll(asteroid.split());
	                    }
	                    switch (asteroid.getSize()) {
						    case LARGE:
						        score += 20;
						        largeDestroyed++;
						        ship.addFuel(20);
						        break;
						    case MEDIUM:
						        score += 10;
						        mediumDestroyed++;
						        ship.addFuel(10);
						        break;
						    case SMALL:
						        score += 5;
						        smallDestroyed++;
						        ship.addFuel(5);
						        break;
						}
					    break;
	                }
	            }
	
	            // Ship hit by bullet
	            if (bullet.getBounds().intersects(ship.getBounds().getBounds2D())) {
	                bulletDeath = true;
	                startDeathSequence();
	                return;
	            }
	        }
	
	        bullets.removeAll(toRemove);
	        asteroids.addAll(newAsteroids);
		} else if (gameState.equals("death")) {
			deathCountdown--;
		    if (deathCountdown <= 0) {
		        deathScreenActive = false;
		        gameState = "menu";
		        setupMenu();
		    }
		}
	    repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
	    super.paintComponent(g);
	    Graphics2D g2 = (Graphics2D) g;
	
	    if (gameState.equals("menu")) {
	        g.setColor(Color.LIGHT_GRAY);
	        String text = "ASTEROIDS";
	        Font font = new Font("Serif", Font.BOLD, 65);
	        g.setFont(font);
	        g.drawString(text, WIDTH / 2 - g.getFontMetrics(font).stringWidth(text) / 2, 200);
	        
	        // --- INSTRUCTION BOX (bottom center) ---
	        Graphics2D g2d = (Graphics2D) g;
	        Font instructionFont = new Font("Times New Roman", Font.PLAIN, 16);
	        g2d.setFont(instructionFont);
	        g2d.setColor(Color.WHITE);
	
	        String[] instructions = {
	            "PLEASE READ BEFORE START!",
	            "", "",
	            "1. Up Arrow: thrust",
	            "2. Left and right arrows: rotate",
	            "3. Space: Hyper mode (thrust faster)",
	            "4. [S]: shoot bullets",
	            "5. [ESC]: pause and save game, press again to restart the game, game progress will be saved",
	            "", "",
	            "press [ENTER] here at home page to immediately start a new game"
	        };
	
	        FontMetrics fm = g2d.getFontMetrics();
	        int padding = 10;
	        int lineHeight = fm.getHeight();
	        int boxWidth = 0;
	
	        // Calculate widest line width
	        for (String line : instructions) {
	            int lineWidth = fm.stringWidth(line);
	            if (lineWidth > boxWidth) {
	                boxWidth = lineWidth;
	            }
	        }
	        boxWidth += padding * 2;
	        int boxHeight = lineHeight * instructions.length + padding * 2;
	
	        // Clamp box to bottom center
	        int boxX = WIDTH / 2 - boxWidth / 2;
	        int boxY = HEIGHT - boxHeight - 30;  // 30 px margin from bottom
	
	        // Draw background box (semi-transparent)
	        g2d.setColor(new Color(0, 0, 0, 150));
	        g2d.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);
	
	        // Draw border
	        g2d.setColor(Color.WHITE);
	        g2d.drawRoundRect(boxX, boxY, boxWidth, boxHeight, 15, 15);
	
	        // Draw instruction text inside box
	        g2d.setColor(Color.WHITE);
	        Map<String, Integer> centeredInstruction = new HashMap<>();
	        centeredInstruction.put("PLEASE READ BEFORE START!", WIDTH / 2 - fm.stringWidth("PLEASE READ BEFORE START!") / 2);
	        centeredInstruction.put("press [ENTER] here at home page to immediately start a new game", WIDTH / 2 - fm.stringWidth("press [ENTER] here at home page to immediately start a new game") / 2);
	        for (int i = 0; i < instructions.length; i++) {
	            if (centeredInstruction.containsKey(instructions[i])) {
	            	g2d.drawString(instructions[i], centeredInstruction.get(instructions[i]), boxY + padding + fm.getAscent() + i * lineHeight);
	            } else {
	            	g2d.drawString(instructions[i], boxX + padding, boxY + padding + fm.getAscent() + i * lineHeight);
	            }
	        }
	        
	        // Draw Error Message if no saved game progress found
	        if (noSavedGame) {
	        	g.setFont(new Font("Serif", Font.BOLD, 18));
	        	fm = g.getFontMetrics();
	        	g.setColor(Color.RED);
	        	
	        	String errorMessage = "[error] no game progress saved";
	        	g.drawString(errorMessage, WIDTH / 2 - fm.stringWidth(errorMessage) / 2, HEIGHT - fm.getAscent() - boxHeight - padding * 2);
	        }
	    } else if (gameState.equals("playing")) {
		    // Asteroids
	        for (Asteroid a : asteroids) { a.draw(g2); }
	
	        // Ship
	        ship.draw(g);
	
	        // Bullets
	        for (Bullet b : bullets) { b.draw(g2); }	
			
			// Level Starting Countdown
			if (levelStarting) {
			    // Starting Countdoan Display
			    g2.setColor(Color.WHITE);
			    g2.setFont(new Font("Serif", Font.PLAIN, 24));
			
			    int secondsLeft = (levelStartCountdown / 60) + 1;
			    String countdownText = "Starting in: " + secondsLeft;
			
			    int textWidth = g2.getFontMetrics().stringWidth(countdownText);
			    g2.drawString(countdownText, WIDTH / 2 - textWidth / 2, 40);
			    
			    // Center Level Display
			    String levelText = "LEVEL " + (level + 1);
	            g2.setFont(new Font("Serif", Font.BOLD, 80));

	            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, levelDisplayAlpha / 255f));
	            textWidth = g2.getFontMetrics().stringWidth(levelText);
	            int textHeight = g2.getFontMetrics().getAscent();
	
	            g2.setColor(Color.YELLOW);
	            g2.drawString(levelText, WIDTH / 2 - textWidth / 2, HEIGHT / 2 + textHeight / 4);
	            
	            if (level >= 3 && level <= 10) {
	            	g2.setFont(new Font("Serif", Font.PLAIN, 40));
	            	String incSpeedDisplay = "(seems like the speed of the asteroids has increased slightly...)";
	            	textWidth = g2.getFontMetrics().stringWidth(incSpeedDisplay);
	            	textHeight = g2.getFontMetrics().getAscent();
	            	g2.drawString(incSpeedDisplay, WIDTH / 2 - textWidth / 2, HEIGHT / 2 + textHeight * 2);
	            }
	
	            // Reset composite for other drawing
	            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
			} else {
				g2.setColor(Color.WHITE);
		        g2.setFont(new Font("Serif", Font.PLAIN, 24));
		
		        int secondsLeft = levelTimer / 60; // frames to seconds
		        String timerText = "Time Left Until Next Level: " + secondsLeft;
		
		        int timerTextWidth = g2.getFontMetrics().stringWidth(timerText);
		        g2.drawString(timerText, WIDTH / 2 - timerTextWidth / 2, 40);
		        g.drawString("Level: " + level, 20, 40);
		        
		        // Show Stats top right
		        FontMetrics fm = g.getFontMetrics();
				String scoreText = "Score: " + score;
				String fuelText = "Fuel: " + (int) ship.getFuel();
				
				g.drawString(scoreText, WIDTH - fm.stringWidth(scoreText) - 20, 40);
				g.drawString(fuelText, WIDTH - fm.stringWidth(fuelText) - 20, 40 + fm.getAscent());
			}	
	    } else if (gameState.equals("paused")) {
	    	g.setColor(Color.WHITE);
	    	Font resumeFont = new Font("Serif", Font.BOLD, 18);
	        g.setFont(resumeFont);
	        FontMetrics metrics = g.getFontMetrics(resumeFont);
	
	        String pausedText = "PAUSED";
	        String resumeText = "Press ESC again to Resume";
	
	        // Draw "Paused" 
	        Font pausedFont = new Font("Serif", Font.PLAIN, 40);
	        g.setFont(pausedFont);
	        g.drawString(pausedText, WIDTH / 2 - g.getFontMetrics().stringWidth(pausedText) / 2, HEIGHT / 2);
	
	        // Draw instruction text
	        g.setFont(resumeFont);
	        g.drawString(resumeText, WIDTH / 2 - metrics.stringWidth(resumeText) / 2, HEIGHT / 2 + 50);	        
		} else if (gameState.equals("death")) {
			g2.setColor(Color.RED);
			g2.setFont(new Font("Serif", Font.PLAIN, 50));
			String deathText = "YOU DIED";
			int textHeight = g2.getFontMetrics().getAscent();
			g2.drawString(deathText, WIDTH / 2 - g2.getFontMetrics().stringWidth(deathText) / 2, HEIGHT / 3 - textHeight / 2 - 80);
			
			g2.setFont(new Font("Serif", Font.PLAIN, 30));
			String deathCause = bulletDeath ? "YOU WERE SHOT BY YOUR OWN BULLET" : asteroidDeath ? "YOU WERE HIT BY AN ASTEROID" : "UNKOWN DEATH";
			textHeight = g2.getFontMetrics().getAscent();
			g2.drawString(deathCause, WIDTH / 2 - g2.getFontMetrics().stringWidth(deathCause) / 2, HEIGHT / 3 - textHeight);
	
			g2.setFont(new Font("Serif", Font.PLAIN, 24));
			    			
			String countdown = "Returning to Menu in..." + (deathCountdown / 60 + 1) + "s (Press ENTER to skip)";
			int cdWidth = g2.getFontMetrics().stringWidth(countdown);
			g2.drawString(countdown, WIDTH / 2 - cdWidth / 2, HEIGHT / 3 + textHeight);
				
			g2.setColor(Color.WHITE);
			int statsY = HEIGHT / 2;
			String scoreText = "Score: " + score;
			String largeText = "Large Asteroids Destroyed: " + largeDestroyed;
			String mediumText = "Medium Asteroids Destroyed: " + mediumDestroyed;
			String smallText = "Small Asteroids Destroyed: " + smallDestroyed;
			String fuelText = "Fuel Remained: " + (int) ship.getFuel();
				
			g2.drawString(scoreText, WIDTH / 2 - g2.getFontMetrics().stringWidth(scoreText) / 2, statsY);
			g2.drawString(largeText, WIDTH / 2 - g2.getFontMetrics().stringWidth(largeText) / 2, statsY + 30);
			g2.drawString(mediumText, WIDTH / 2 - g2.getFontMetrics().stringWidth(mediumText) / 2, statsY + 60);
			g2.drawString(smallText, WIDTH / 2 - g2.getFontMetrics().stringWidth(smallText) / 2, statsY + 90);
			g2.drawString(fuelText, WIDTH / 2 - g2.getFontMetrics().stringWidth(fuelText) / 2, statsY + 120);
		}
	}
	
    @Override
    public void keyPressed(KeyEvent e) {
    	if (gameState.equals("menu")){
    		switch (e.getKeyCode()) {
    			case KeyEvent.VK_ENTER -> startNewGame();
    		}
    	} else if (gameState.equals("playing")) {
            switch (e.getKeyCode()) {
	            case KeyEvent.VK_UP -> thrusting = true;
	            case KeyEvent.VK_LEFT -> rotatingLeft = true;
	            case KeyEvent.VK_RIGHT -> rotatingRight = true;
	            case KeyEvent.VK_SPACE -> ship.setHyper(true);
	            case KeyEvent.VK_ESCAPE -> {
	            	gameState = "paused";
	                saveGame();
	                setupPauseMenu();
	            }
	            case KeyEvent.VK_S -> {
				    Point2D.Double tip = ship.getTipPosition();

					// Push bullet spawn point a bit further forward along the ship's facing direction
					double bulletSpawnDistance = 30;  // tweak this value if needed
					
					double spawnX = tip.x + bulletSpawnDistance * Math.sin(ship.getAngle());
					double spawnY = tip.y - bulletSpawnDistance * Math.cos(ship.getAngle());
					
					bullets.add(new Bullet(spawnX, spawnY, ship.getAngle()));
				}
			}
        } else if (gameState.equals("paused")) {
        	switch (e.getKeyCode()) {
        		case KeyEvent.VK_ESCAPE -> gameState = "playing";
        	}
        } else if (gameState.equals("death")) {
        	if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        		deathCountdown = 0;
        		if (deathCountdown < 0) deathCountdown = 0;
        	}
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    	if (gameState.equals("playing")) {
    	    switch (e.getKeyCode()) {
	            case KeyEvent.VK_UP -> thrusting = false;
	            case KeyEvent.VK_LEFT -> rotatingLeft = false;
	            case KeyEvent.VK_RIGHT -> rotatingRight = false;
	            case KeyEvent.VK_SPACE -> ship.setHyper(false);
	        }
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
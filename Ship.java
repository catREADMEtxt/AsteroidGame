import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.PathIterator;
import java.util.List;
import java.awt.geom.Point2D;

public class Ship {
    // Constants
    private int bodyWidth = 12;
	private int bodyHeight = 20;
    private final int universeWidth = 1200;
	private final int universeHeight = 700;
	
    // Position and velocity
    private double x = universeWidth / 2;
    private double y = universeHeight / 2;
    private double vx = 0;
    private double vy = 0;
    
    // Ship Bounds
    Polygon shipBody;

    // Rotation and thrust
    private double angle = 0; // in radians
    private final double thrustPower = 0.05;
    private final double rotationSpeed = Math.toRadians(2.5); // radians per update
    private int thrustDuration = 0; // How long thrust is being applied (in frames)
	private final int maxThrustDuration = 30; // Duration to reach max flame size
	private boolean isThrusting = false;
	
	//hyper
	private boolean hyper = false;
	private final double hyperMultiplier = 4;
	private final double hyperFuelCost = 4;

    // Fuel
    private double fuel = 200;
    
    public void addFuel(int gain) { fuel += gain; }
	
	public void setHyper(boolean active) { hyper = active; }
	
	public void applyThrust() {
	    if (fuel <= 0) {
	        isThrusting = false;
	        thrustDuration = 0;
	        return;
	    }
	    
	    double multiplier = hyper ? hyperMultiplier : 1;
	    double ax = multiplier * thrustPower * Math.sin(angle);
	    double ay = -multiplier * thrustPower * Math.cos(angle);
	    vx += ax;
	    vy += ay;
	    fuel -= hyper ? hyperFuelCost : 1;
	    if (fuel < 0) fuel = 0;
	    isThrusting = true;
	    if (thrustDuration < maxThrustDuration) thrustDuration++;
	}
    public void rotateLeft() {
        double multiplier = hyper ? hyperMultiplier/2 : 1;
        angle -= multiplier * rotationSpeed;
    }

    public void rotateRight() {
        double multiplier = hyper ? hyperMultiplier/2 : 1;
        angle += multiplier * rotationSpeed;
    }

	public void draw(Graphics g) {
	    Graphics2D g2d = (Graphics2D) g;
	
	    // Apply rotation and translation for angle and position
	    g2d.translate(x, y);
	    g2d.rotate(angle);
	
	    // --- Draw the spaceship body shape ---
	    g2d.setColor(Color.WHITE);
	    shipBody = new Polygon();
	    shipBody.addPoint(0, -bodyHeight / 2);
	    shipBody.addPoint(-bodyWidth / 2, bodyHeight / 4);
	    shipBody.addPoint(-bodyWidth / 4, bodyHeight / 2);
	    shipBody.addPoint(bodyWidth / 4, bodyHeight / 2);
	    shipBody.addPoint(bodyWidth / 2, bodyHeight / 4);
	    g2d.fillPolygon(shipBody);
		
	    // --- Draw dynamic flame ---
	    if (thrustDuration > 0) {
	        double flameProgress = (double) thrustDuration / maxThrustDuration;
	        int flameWidth = 4;
	        int flameHeight = (int) (8 * flameProgress) + 2; // Start small, grow
	
	        if (hyper) {
			    g2d.setColor(Color.CYAN);
			    flameHeight *= 2; // longer flame
			} else {
			    g2d.setColor(Color.ORANGE);
			}
	        Polygon flame = new Polygon();
	        flame.addPoint(-flameWidth, bodyHeight / 2);               // Left base
	        flame.addPoint(flameWidth, bodyHeight / 2);                // Right base
	        flame.addPoint(0, bodyHeight / 2 + flameHeight);           // Tip
	        g2d.fillPolygon(flame);
	    }
	    
	    // Undo transformations
	    g2d.rotate(-angle);
	    g2d.translate(-x, -y);
	}

	public void updatePhysics() {		
		//clamp speed
		double maxSpeed = hyper ? 8.0 : 2.5;
		vx = Math.max(-maxSpeed, Math.min(maxSpeed, vx));
		vy = Math.max(-maxSpeed, Math.min(maxSpeed, vy));

		x += vx;
	    y += vy;
	    
	    if (!isThrusting && thrustDuration > 0) {
	        thrustDuration--; // Smooth fade out
	    }
	
	    isThrusting = false; // Reset after each frame unless applyThrust is called again
	    
	    //loop universe
		x = (x + universeWidth) % universeWidth;
		y = (y + universeHeight) % universeHeight;

	}

	public Polygon getBounds() {
	    // Define the ship's shape centered at origin
	    Polygon ship = new Polygon();
	    ship.addPoint(0, -bodyHeight / 2);
	    ship.addPoint(-bodyWidth / 2, bodyHeight / 4);
	    ship.addPoint(-bodyWidth / 4, bodyHeight / 2);
	    ship.addPoint(bodyWidth / 4, bodyHeight / 2);
	    ship.addPoint(bodyWidth / 2, bodyHeight / 4);
	
	    // Apply rotation and translation to get ship's position in world space
	    AffineTransform transform = new AffineTransform();
	    transform.translate(x, y);
	    transform.rotate(angle);
	
	    Shape transformed = transform.createTransformedShape(ship);
	
	    // Convert transformed shape back to polygon
	    Polygon transformedPoly = new Polygon();
	    for (PathIterator pi = transformed.getPathIterator(null); !pi.isDone(); pi.next()) {
	        double[] coords = new double[6];
	        int type = pi.currentSegment(coords);
	        if (type != PathIterator.SEG_CLOSE) {
	            transformedPoly.addPoint((int) coords[0], (int) coords[1]);
	        }
	    }
	
	    return transformedPoly;
	}


    // Getters for position, fuel, etc.
    public double getX() { return x; }
    public double getY() { return y; }
    public double getFuel() { return fuel; }
    public double getVelocity() { return Math.sqrt(vx * vx + vy * vy); }
    public int getShipWidth() { return bodyWidth; }
    public int getShipHeight() { return bodyHeight; }
	public Point2D.Double getTipPosition() {
	    double noseOffset = -bodyHeight / 2.0;
	    double tipX = x + noseOffset * Math.sin(angle);
	    double tipY = y - noseOffset * Math.cos(angle);
	    return new Point2D.Double(tipX, tipY);
	}
	public double getAngle() { return angle; }
	
	// Set Values
	public void setFuel(double newFuel) { fuel = newFuel; }
	public void setX(double newX) { x = newX; }
	public void setY(double newY) { y = newY; }
	public void setAngle(double newAngle) { angle = newAngle; }
}
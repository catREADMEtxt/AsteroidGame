import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ParticleSystem {
    private static class Particle {
        double x, y;
        double vx, vy;
        int lifetime;
        int maxLifetime;
        Color color;
        int size;
        
        Particle(double x, double y, double vx, double vy, Color color, int lifetime, int size) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.color = color;
            this.maxLifetime = lifetime;
            this.lifetime = lifetime;
            this.size = size;
        }
        
        void update() {
            x += vx;
            y += vy;
            vx *= 0.98; // friction
            vy *= 0.98;
            lifetime--;
        }
        
        boolean isAlive() {
            return lifetime > 0;
        }
        
        void draw(Graphics2D g2d) {
            float alpha = (float) lifetime / maxLifetime;
            Color drawColor = new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                (int) (color.getAlpha() * alpha)
            );
            g2d.setColor(drawColor);
            g2d.fillOval((int) x - size / 2, (int) y - size / 2, size, size);
        }
    }
    
    private final List<Particle> particles = new ArrayList<>();
    private final Random rand = new Random();
    
    public void createExplosion(double x, double y, Color color, int count, double speed) {
        for (int i = 0; i < count; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            double velocity = speed * (0.5 + rand.nextDouble());
            double vx = Math.cos(angle) * velocity;
            double vy = Math.sin(angle) * velocity;
            int lifetime = 30 + rand.nextInt(30);
            int size = 2 + rand.nextInt(3);
            particles.add(new Particle(x, y, vx, vy, color, lifetime, size));
        }
    }
    
    public void createAsteroidExplosion(double x, double y, Asteroid.Size size) {
        int count = switch (size) {
            case LARGE -> 30;
            case MEDIUM -> 20;
            case SMALL -> 10;
        };
        createExplosion(x, y, new Color(180, 180, 180, 255), count, 3.0);
    }
    
    public void createVoidExplosion(double x, double y, Asteroid.Size size) {
        int count = switch (size) {
            case LARGE -> 40;
            case MEDIUM -> 25;
            case SMALL -> 15;
        };
        createExplosion(x, y, new Color(180, 0, 255, 255), count, 4.0);
    }
    
    public void createShipExplosion(double x, double y) {
        // Multiple layers of explosion
        createExplosion(x, y, new Color(255, 100, 0, 255), 50, 5.0);
        createExplosion(x, y, new Color(255, 200, 0, 255), 40, 3.0);
        createExplosion(x, y, new Color(255, 255, 255, 255), 30, 2.0);
    }
    
    public void update() {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.update();
            if (!p.isAlive()) {
                it.remove();
            }
        }
    }
    
    public void draw(Graphics2D g2d) {
        for (Particle p : particles) {
            p.draw(g2d);
        }
    }
    
    public void clear() {
        particles.clear();
    }
}
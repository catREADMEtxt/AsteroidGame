import java.awt.*;

public class VoidEnergy {
    private double energy = 0;
    private double maxEnergy = 100;
    private double consumptionRate = 0.1; // Base passive consumption
    private double actionMultiplier = 1.0; // Increases with actions
    private boolean active = false;
    
    public void activate() {
        active = true;
    }
    
    public void deactivate() {
        active = false;
        actionMultiplier = 1.0;
    }
    
    public void update() {
        if (active) {
            // Passive consumption
            energy += consumptionRate * actionMultiplier;
            if (energy > maxEnergy) {
                energy = maxEnergy;
            }
        } else if (energy > 0) {
            // Slowly dissipate when not in void world
            energy -= 0.05;
            if (energy < 0) energy = 0;
        }
    }
    
    public void consumeForAction(double amount) {
        if (active) {
            energy += amount;
            // Increase action multiplier (caps at 3x)
            actionMultiplier = Math.min(actionMultiplier + 0.05, 3.0);
        }
    }
    
    public boolean isOverThreshold() {
        return energy >= maxEnergy;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public double getEnergy() {
        return energy;
    }
    
    public double getMaxEnergy() {
        return maxEnergy;
    }
    
    public double getEnergyPercent() {
        return energy / maxEnergy;
    }
    
    public void reset() {
        energy = 0;
        active = false;
        actionMultiplier = 1.0;
    }
    
    public void drawBar(Graphics2D g2d, int screenWidth, int screenHeight) {
        if (energy <= 0) return;
        
        int barWidth = 300;
        int barHeight = 25;
        int barX = screenWidth / 2 - barWidth / 2;
        int barY = screenHeight - 60;
        
        // Background
        g2d.setColor(new Color(40, 0, 60, 200));
        g2d.fillRect(barX, barY, barWidth, barHeight);
        
        // Border
        g2d.setColor(new Color(150, 0, 255));
        g2d.drawRect(barX, barY, barWidth, barHeight);
        
        // Fill (gradient from purple to red as it fills)
        int fillWidth = (int) (barWidth * getEnergyPercent());
        Color startColor = new Color(150, 0, 255);
        Color endColor = new Color(255, 0, 100);
        
        float ratio = (float) getEnergyPercent();
        int r = (int) (startColor.getRed() * (1 - ratio) + endColor.getRed() * ratio);
        int g = (int) (startColor.getGreen() * (1 - ratio) + endColor.getGreen() * ratio);
        int b = (int) (startColor.getBlue() * (1 - ratio) + endColor.getBlue() * ratio);
        
        g2d.setColor(new Color(r, g, b, 220));
        g2d.fillRect(barX + 2, barY + 2, fillWidth - 4, barHeight - 4);
        
        // Text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Serif", Font.BOLD, 14));
        String text = String.format("VOID ENERGY: %.1f / %.0f", energy, maxEnergy);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(text, barX + barWidth / 2 - fm.stringWidth(text) / 2, barY + barHeight / 2 + fm.getAscent() / 2 - 2);
    }
}
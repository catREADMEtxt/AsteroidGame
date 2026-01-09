import java.awt.*;

public class AbilityManager {
    private Ship ship;
    private static class Ability {
        String name;
        String key;
        boolean unlocked;
        boolean active;
        boolean onCooldown;
        int cooldownTimer;
        int maxCooldown;
        Color color;
        
        Ability(String name, String key, int maxCooldown, Color color) {
            this.name = name;
            this.key = key;
            this.maxCooldown = maxCooldown;
            this.color = color;
            this.unlocked = false;
            this.active = false;
            this.onCooldown = false;
            this.cooldownTimer = 0;
        }
        
        void startCooldown() {
            onCooldown = true;
            cooldownTimer = maxCooldown;
        }
        
        void update() {
            if (onCooldown) {
                cooldownTimer--;
                if (cooldownTimer <= 0) {
                    onCooldown = false;
                    cooldownTimer = 0;
                }
            }
        }
        
        float getCooldownPercent() {
            return onCooldown ? (float)cooldownTimer / maxCooldown : 0f;
        }
    }
    
    private Ability voidAbility;
    private Ability fadeAbility;
    private Ability teleportAbility;
    private Ability shieldAbility;
    private Ability timeSlowAbility;
    private Ability droneAbility;
    private Ability novaAbility;
    
    // Shield Burst fields
    private boolean shieldActive = false;
    private int shieldDuration = 0;
    private final int maxShieldDuration = 2 * 60;
    private double shieldRadius = 0;
    private final double maxShieldRadius = 150;
    
    // Time Slow fields
    private boolean timeSlowActive = false;
    private int timeSlowDuration = 0;
    private final int maxTimeSlowDuration = 5 * 60;
    
    // Drone fields
    private boolean droneDeployed = false;
    private double droneOrbitAngle = 0;
    private int droneHealth = 3;
    private int droneFireTimer = 0;
    
    // Nova Blast fields
    public enum NovaState { READY, CHARGING, EXPLODING, COOLDOWN }
    private NovaState novaState = NovaState.READY;
    private int novaChargeTimer = 0;
    private int novaExplosionTimer = 0;
    private double novaExplosionRadius = 0;
    private final int novaChargeDuration = 1 * 60; // 1 second charge
    
    public AbilityManager(Ship shipObject) {
    	ship = shipObject;
        voidAbility = new Ability("Void", "D", 3 * 60, new Color(180, 0, 255));
        fadeAbility = new Ability("Fade", "F", 8 * 60, new Color(255, 215, 0));
        teleportAbility = new Ability("Teleport", "T", 5 * 60, new Color(0, 255, 200));
        shieldAbility = new Ability("Shield", "Q", 12 * 60, new Color(100, 200, 255));
        timeSlowAbility = new Ability("Time", "R", 15 * 60, new Color(150, 255, 255));
        droneAbility = new Ability("Drone", "E", 20 * 60, new Color(255, 150, 100));
        novaAbility = new Ability("Nova", "X", 60 * 60, new Color(255, 100, 255));
    }
    
    public void update() {
        voidAbility.update();
        fadeAbility.update();
        teleportAbility.update();
        shieldAbility.update();
        timeSlowAbility.update();
        droneAbility.update();
        novaAbility.update();
        
        // Update shield
        if (shieldActive) {
            shieldDuration++;
            shieldRadius = maxShieldRadius * ((double)shieldDuration / maxShieldDuration);
            
            if (shieldDuration >= maxShieldDuration) {
                shieldActive = false;
                shieldDuration = 0;
                shieldRadius = 0;
            }
        }
        
        // Update time slow
        if (timeSlowActive) {
            timeSlowDuration--;
            if (timeSlowDuration <= 0) {
                timeSlowActive = false;
                timeSlowDuration = 0;
            }
        }
        
        // Update drone
        if (droneDeployed) {
            droneOrbitAngle += 0.05;
            if (droneFireTimer > 0) droneFireTimer--;
        }
        
        // Update nova blast
        if (novaState == NovaState.CHARGING) {
            novaChargeTimer++;
            if (novaChargeTimer >= novaChargeDuration) {
                novaState = NovaState.EXPLODING;
                novaChargeTimer = 0;
                novaExplosionTimer = 0;
            }
        } else if (novaState == NovaState.EXPLODING) {
            novaExplosionTimer++;
            novaExplosionRadius = 1000 * ((double)novaExplosionTimer / 60);
            
            if (novaExplosionTimer >= 60) {
                novaState = NovaState.COOLDOWN;
                novaAbility.startCooldown();
                novaExplosionRadius = 0;
            }
        }
    }
    
    // Shield Burst methods
    public boolean activateShield() {
        if (shieldAbility.unlocked && !shieldAbility.onCooldown && !shieldActive) {
            shieldActive = true;
            shieldDuration = 0;
            shieldRadius = 0;
            shieldAbility.startCooldown();
            return true;
        }
        return false;
    }
    
    public boolean isShieldActive() { return shieldActive; }
    public double getShieldRadius() { return shieldRadius; }
    
    // Time Slow methods
    public boolean activateTimeSlow() {
        if (timeSlowAbility.unlocked && !timeSlowAbility.onCooldown && !timeSlowActive) {
            timeSlowActive = true;
            timeSlowDuration = maxTimeSlowDuration;
            timeSlowAbility.startCooldown();
            return true;
        }
        return false;
    }
    
    public boolean isTimeSlowActive() { return timeSlowActive; }
    public float getTimeScale() { return timeSlowActive ? 0.3f : 1.0f; }
    
    // Drone methods
    public boolean toggleDrone() {
        if (!droneAbility.unlocked) return false;
        
        if (droneDeployed) {
            // Recall drone
            droneDeployed = false;
            droneAbility.startCooldown();
            return true;
        } else if (!droneAbility.onCooldown) {
            // Deploy drone
            droneDeployed = true;
            droneHealth = 3;
            droneOrbitAngle = 0;
            return true;
        }
        return false;
    }
    
    public boolean isDroneDeployed() { return droneDeployed; }
    public void damageDrone() {
        droneHealth--;
        if (droneHealth <= 0) {
            droneDeployed = false;
            droneAbility.startCooldown();
        }
    }
    
    public Point getDronePosition(double shipX, double shipY) {
        if (!droneDeployed) return null;
        int orbitRadius = 80;
        int dx = (int)(shipX + orbitRadius * Math.cos(droneOrbitAngle));
        int dy = (int)(shipY + orbitRadius * Math.sin(droneOrbitAngle));
        return new Point(dx, dy);
    }
    
    public boolean canDroneFire() {
        return droneDeployed && droneFireTimer <= 0;
    }
    
    public void droneDidFire() {
        droneFireTimer = 30; // 0.5 second cooldown
    }
    
    // Nova Blast methods
    public boolean activateNova(VoidEnergy voidEnergy) {
        if (!novaAbility.unlocked || novaState != NovaState.READY) return false;
        if (voidEnergy.getEnergy() < 75) return false;
        
        novaState = NovaState.CHARGING;
        novaChargeTimer = 0;
        return true;
    }
    
    public boolean isNovaCharging() { return novaState == NovaState.CHARGING; }
    public boolean isNovaExploding() { return novaState == NovaState.EXPLODING; }
    public double getNovaExplosionRadius() { return novaExplosionRadius; }
    public float getNovaChargePercent() {
        return novaState == NovaState.CHARGING ? (float)novaChargeTimer / novaChargeDuration : 0f;
    }
    
    public void drawAbilityBar(Graphics2D g2d, int screenWidth, int screenHeight) {
        int barWidth = 600;
        int barHeight = 70;
        int barX = screenWidth / 2 - barWidth / 2;
        int barY = screenHeight - barHeight - 20;
        
        // Background panel
        g2d.setColor(new Color(20, 25, 35, 220));
        g2d.fillRoundRect(barX, barY, barWidth, barHeight, 12, 12);
        
        // Border
        g2d.setColor(new Color(80, 100, 130));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(barX, barY, barWidth, barHeight, 12, 12);
        g2d.setStroke(new BasicStroke(1));
        
        // Draw each ability
        Ability[] abilities = {voidAbility, fadeAbility, teleportAbility, 
                              shieldAbility, timeSlowAbility, droneAbility, novaAbility};
        int abilitySpacing = barWidth / 7;
        
        for (int i = 0; i < abilities.length; i++) {
            Ability ability = abilities[i];
            int abilityX = barX + i * abilitySpacing + abilitySpacing / 2;
            int abilityY = barY + barHeight / 2;
            
            drawAbilityIcon(g2d, ability, abilityX, abilityY);
        }
    }
    
    private void drawAbilityIcon(Graphics2D g2d, Ability ability, int centerX, int centerY) {
        int iconSize = 45;
        int x = centerX - iconSize / 2;
        int y = centerY - iconSize / 2;
        
        // Background circle
        Color bgColor;
        if (!ability.unlocked) {
            bgColor = new Color(40, 40, 40, 180);
        } else if (ability.active) {
            bgColor = new Color(
                ability.color.getRed(), 
                ability.color.getGreen(), 
                ability.color.getBlue(), 
                200
            );
        } else if (ability.onCooldown) {
            bgColor = new Color(60, 60, 70, 200);
        } else {
            bgColor = new Color(50, 60, 80, 200);
        }
        
        g2d.setColor(bgColor);
        g2d.fillOval(x, y, iconSize, iconSize);
        
        // Cooldown overlay (arc)
        if (ability.onCooldown) {
            float percent = ability.getCooldownPercent();
            int arcAngle = (int)(360 * percent);
            
            g2d.setColor(new Color(30, 30, 40, 180));
            g2d.fillArc(x, y, iconSize, iconSize, 90, -arcAngle);
        }
        
        // Active glow
        if (ability.active && ability.unlocked) {
            int glowSize = iconSize + 8;
            int glowAlpha = (int)(100 + 80 * Math.sin(System.currentTimeMillis() / 200.0));
            g2d.setColor(new Color(
                ability.color.getRed(),
                ability.color.getGreen(),
                ability.color.getBlue(),
                glowAlpha
            ));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawOval(x - 4, y - 4, glowSize, glowSize);
        }
        
        // Border
        Color borderColor = ability.unlocked ? ability.color : new Color(80, 80, 80);
        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(ability.active ? 3 : 2));
        g2d.drawOval(x, y, iconSize, iconSize);
        g2d.setStroke(new BasicStroke(1));
        
        // Key letter
        g2d.setFont(new Font("Serif", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        Color textColor = ability.unlocked ? Color.WHITE : new Color(100, 100, 100);
        g2d.setColor(textColor);
        g2d.drawString(
            ability.key,
            centerX - fm.stringWidth(ability.key) / 2,
            centerY + fm.getAscent() / 2 - 2
        );
        
        // Cooldown timer
        if (ability.onCooldown) {
            g2d.setFont(new Font("Monospaced", Font.BOLD, 9));
            String timeText = String.format("%.1fs", ability.cooldownTimer / 60.0);
            fm = g2d.getFontMetrics();
            g2d.setColor(new Color(255, 150, 150));
            g2d.drawString(
                timeText,
                centerX - fm.stringWidth(timeText) / 2,
                centerY + 16
            );
        }
        
        // "LOCKED" text if not unlocked
        if (!ability.unlocked) {
            g2d.setFont(new Font("Serif", Font.BOLD, 8));
            fm = g2d.getFontMetrics();
            g2d.setColor(new Color(150, 50, 50));
            String lockText = "LOCKED";
            g2d.drawString(
                lockText,
                centerX - fm.stringWidth(lockText) / 2,
                centerY + 20
            );
        }
    }
    
    public void drawShieldEffect(Graphics2D g2d, double shipX, double shipY) {
        if (!shieldActive) return;
        
        // Expanding wave
        int alpha = (int)(150 * (1.0 - (double)shieldDuration / maxShieldDuration));
        g2d.setColor(new Color(100, 200, 255, alpha));
        g2d.setStroke(new BasicStroke(4));
        int r = (int)shieldRadius;
        g2d.drawOval((int)shipX - r, (int)shipY - r, r * 2, r * 2);
        
        // Inner glow
        g2d.setColor(new Color(150, 220, 255, alpha / 2));
        g2d.fillOval((int)shipX - r, (int)shipY - r, r * 2, r * 2);
        
        g2d.setStroke(new BasicStroke(1));
    }
    
    public void drawTimeSlowEffect(Graphics2D g2d, int screenWidth, int screenHeight) {
        if (!timeSlowActive) return;
        
        // Blue overlay
        g2d.setColor(new Color(0, 100, 200, 30));
        g2d.fillRect(0, 0, screenWidth, screenHeight);
        
        // Scan lines
        long time = System.currentTimeMillis();
        for (int i = 0; i < screenHeight; i += 3) {
            int alpha = (int)(20 + 10 * Math.sin(time / 100.0 + i / 10.0));
            g2d.setColor(new Color(0, 150, 255, alpha));
            g2d.drawLine(0, i, screenWidth, i);
        }
    }
    
    public void drawDrone(Graphics2D g2d, double shipX, double shipY) {
        if (!droneDeployed) return;
        
        Point pos = getDronePosition(shipX, shipY);
        if (pos == null) return;
        
        // Drone body
        int droneSize = 12;
        g2d.setColor(new Color(100, 200, 255));
        int[] xs = {pos.x, pos.x - droneSize/2, pos.x, pos.x + droneSize/2};
        int[] ys = {pos.y - droneSize/2, pos.y + droneSize/2, pos.y + droneSize/4, pos.y + droneSize/2};
        g2d.fillPolygon(xs, ys, 4);
        
        // Health indicator
        for (int i = 0; i < droneHealth; i++) {
            g2d.setColor(new Color(0, 255, 0));
            g2d.fillRect(pos.x - 12 + i * 8, pos.y - 20, 6, 3);
        }
    }
    
    public void drawNovaBlast(Graphics2D g2d, int screenWidth, int screenHeight, double shipX, double shipY) {
        if (novaState == NovaState.CHARGING) {
            // Charging effect around ship
            float chargePercent = (float)novaChargeTimer / novaChargeDuration;
            int pulseRadius = (int)(30 + 20 * Math.sin(chargePercent * Math.PI * 4));
            int alpha = (int)(200 * chargePercent);
            
            g2d.setColor(new Color(255, 0, 255, alpha));
            g2d.setStroke(new BasicStroke(5));
            g2d.drawOval((int)shipX - pulseRadius, (int)shipY - pulseRadius, 
                        pulseRadius * 2, pulseRadius * 2);
            g2d.setStroke(new BasicStroke(1));
            
            // "CHARGING" text
            g2d.setFont(new Font("Serif", Font.BOLD, 30));
            g2d.setColor(new Color(255, 100, 255));
            String text = "CHARGING...";
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(text, screenWidth/2 - fm.stringWidth(text)/2, 100);
            
        } else if (novaState == NovaState.EXPLODING) {
            // Massive explosion
            float explosionPercent = (float)novaExplosionTimer / 60;
            int alpha = (int)(255 * (1.0f - explosionPercent));
            
            // Multiple expanding rings
            for (int i = 0; i < 5; i++) {
                int r = (int)(novaExplosionRadius - i * 50);
                if (r > 0) {
                    g2d.setColor(new Color(255, 100, 255, alpha / (i + 1)));
                    g2d.setStroke(new BasicStroke(8 - i));
                    g2d.drawOval((int)shipX - r, (int)shipY - r, r * 2, r * 2);
                }
            }
            
            // Screen flash
            g2d.setColor(new Color(255, 200, 255, alpha));
            g2d.fillRect(0, 0, screenWidth, screenHeight);
            
            // "NOVA BLAST" text
            if (novaExplosionTimer < 30) {
                g2d.setFont(new Font("Serif", Font.BOLD, 80));
                g2d.setColor(new Color(255, 255, 255, alpha));
                String text = "NOVA BLAST";
                FontMetrics fm = g2d.getFontMetrics();
                g2d.drawString(text, screenWidth/2 - fm.stringWidth(text)/2, screenHeight/2);
            }
            
            g2d.setStroke(new BasicStroke(1));
        }
    }
    
    // Getters and setters
    public void setVoidUnlocked(boolean unlocked) { voidAbility.unlocked = unlocked; }
    public void setFadeUnlocked(boolean unlocked) { fadeAbility.unlocked = unlocked; }
    public void setTeleportUnlocked(boolean unlocked) { teleportAbility.unlocked = unlocked; }
    public void setShieldUnlocked(boolean unlocked) { shieldAbility.unlocked = unlocked; }
    public void setTimeSlowUnlocked(boolean unlocked) { timeSlowAbility.unlocked = unlocked; }
    public void setDroneUnlocked(boolean unlocked) { droneAbility.unlocked = unlocked; }
    public void setNovaUnlocked(boolean unlocked) { novaAbility.unlocked = unlocked; }
    
    public void setVoidActive(boolean active) { voidAbility.active = active; }
    public void setFadeActive(boolean active) { fadeAbility.active = active; }
    public void setTeleportActive(boolean active) { teleportAbility.active = active; }
    
    public void startVoidCooldown() { voidAbility.startCooldown(); }
    public void startFadeCooldown() { fadeAbility.startCooldown(); }
    public void startTeleportCooldown() { teleportAbility.startCooldown(); }
    
    public boolean isVoidOnCooldown() { return voidAbility.onCooldown; }
    public boolean isFadeOnCooldown() { return fadeAbility.onCooldown; }
    public boolean isTeleportOnCooldown() { return teleportAbility.onCooldown; }
    
    public void resetNova() {
        novaState = NovaState.READY;
        novaChargeTimer = 0;
        novaExplosionTimer = 0;
        novaExplosionRadius = 0;
    }
}
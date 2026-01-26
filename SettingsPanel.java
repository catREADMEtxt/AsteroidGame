import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class SettingsPanel {
    private String selectedCategory = "Controller";
    private String hoveredCategory = null;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private Rectangle closeButtonBounds;
    private Map<String, Rectangle> categoryBounds = new LinkedHashMap<>();
    
    // Settings values
    private boolean invertX = false;
    private boolean vibration = true;
    private int vibrationStrength = 100;
    private int movementSensitivity = 80;
    private int sfxVolume = 70;
    private int musicVolume = 50;
    private boolean effects = true;
    private boolean vSync = true;
    private int fps = 60;
    private boolean screenShake = true;
    private boolean windowedMode = false;
    private String colorBlindMode = "NORMAL";
    private String language = "English";
    
    // Slider tracking
    private String activeSlider = null;
    
    public SettingsPanel() {
        categoryBounds.put("Controller", new Rectangle());
        categoryBounds.put("Key bindings", new Rectangle());
        categoryBounds.put("Graphics", new Rectangle());
        categoryBounds.put("Audio", new Rectangle());
        categoryBounds.put("Language", new Rectangle());
        categoryBounds.put("Instructions", new Rectangle());
        categoryBounds.put("Credits", new Rectangle());
    }
    
    public void handleClick(int mx, int my, AsteroidGame game) {
        // Check close button
        if (closeButtonBounds != null && closeButtonBounds.contains(mx, my)) {
            return; // Close handled by game
        }
        
        // Check category selection
        for (Map.Entry<String, Rectangle> entry : categoryBounds.entrySet()) {
            if (entry.getValue().contains(mx, my)) {
                selectedCategory = entry.getKey();
                scrollOffset = 0;
                return;
            }
        }
        
        // Check content interactions based on category
        switch (selectedCategory) {
            case "Controller" -> handleControllerClick(mx, my);
            case "Key bindings" -> handleKeyBindingsClick(mx, my, game);
            case "Graphics" -> handleGraphicsClick(mx, my);
            case "Audio" -> handleAudioClick(mx, my);
            case "Language" -> handleLanguageClick(mx, my);
        }
    }
    
    public void handleMouseMove(int mx, int my) {
        hoveredCategory = null;
        for (Map.Entry<String, Rectangle> entry : categoryBounds.entrySet()) {
            if (entry.getValue().contains(mx, my)) {
                hoveredCategory = entry.getKey();
                break;
            }
        }
    }
    
    public void handleScroll(int rotation) {
        scrollOffset -= rotation * 20;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScrollOffset));
    }
    
    public void handleMouseDrag(int mx, int my, int panelX, int contentX) {
        if (activeSlider != null) {
            int sliderWidth = 150;
            int sliderX = contentX + 550;
            float percent = Math.max(0, Math.min(1, (mx - sliderX) / (float)sliderWidth));
            
            switch (activeSlider) {
                case "vibrationStrength" -> vibrationStrength = (int)(percent * 100);
                case "movementSensitivity" -> movementSensitivity = (int)(percent * 100);
                case "sfxVolume" -> sfxVolume = (int)(percent * 100);
                case "musicVolume" -> musicVolume = (int)(percent * 100);
            }
        }
    }
    
    public void handleMouseRelease() {
        activeSlider = null;
    }
    
    private void handleControllerClick(int mx, int my) {
        // Implement toggle and slider clicks
        // This is handled in the main click handler
    }
    
    private void handleKeyBindingsClick(int mx, int my, AsteroidGame game) {
        // Handled by game's key binding system
    }
    
    private void handleGraphicsClick(int mx, int my) {
        // Implement graphics option clicks
    }
    
    private void handleAudioClick(int mx, int my) {
        // Implement audio slider clicks
    }
    
    private void handleLanguageClick(int mx, int my) {
        // Implement language selection
    }
    
    public void draw(Graphics2D g2, int WIDTH, int HEIGHT, ControlConfig controlConfig, AbilityLoadout loadout) {
        // Calculate responsive dimensions
        int panelWidth = (int)(WIDTH * 0.85);
        int panelHeight = (int)(HEIGHT * 0.75);
        int panelX = (WIDTH - panelWidth) / 2;
        int panelY = (HEIGHT - panelHeight) / 2;
        
        int leftColumnWidth = (int)(panelWidth * 0.25);
        int contentWidth = panelWidth - leftColumnWidth - 60;
        
        // Draw backdrop
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Draw main panel
        g2.setColor(new Color(15, 10, 35, 250));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Draw border
        g2.setColor(new Color(0, 255, 200));
        g2.setStroke(new BasicStroke(3));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Draw horizontal divider line
        int dividerX = panelX + leftColumnWidth + 20;
        g2.setColor(new Color(0, 255, 200));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(dividerX, panelY + 20, dividerX, panelY + panelHeight - 20);
        g2.setStroke(new BasicStroke(1));
        
        // Draw left column categories
        drawCategories(g2, panelX, panelY, leftColumnWidth, panelHeight);
        
        // Draw content area
        int contentX = dividerX + 20;
        drawContent(g2, contentX, panelY, contentWidth, panelHeight, controlConfig, loadout, WIDTH, HEIGHT);
        
        // Draw close button
        drawCloseButton(g2, panelX, panelY, panelWidth);
    }
    
    private void drawCategories(Graphics2D g2, int panelX, int panelY, int columnWidth, int panelHeight) {
        int y = panelY + 60;
        int itemHeight = 50;
        int categoryX = panelX + 20;
        
        for (String category : categoryBounds.keySet()) {
            boolean isSelected = category.equals(selectedCategory);
            boolean isHovered = category.equals(hoveredCategory);
            
            Rectangle bounds = new Rectangle(categoryX, y, columnWidth - 20, itemHeight);
            categoryBounds.put(category, bounds);
            
            // Draw background for selected/hovered
            if (isSelected) {
                g2.setColor(new Color(0, 200, 180, 200));
                g2.fillRoundRect(categoryX, y, columnWidth - 20, itemHeight - 5, 8, 8);
            } else if (isHovered) {
                g2.setColor(new Color(0, 150, 150, 100));
                g2.fillRoundRect(categoryX, y, columnWidth - 20, itemHeight - 5, 8, 8);
            }
            
            // Draw text
            Font font = new Font("Arial", isSelected ? Font.BOLD : Font.PLAIN, 16);
            g2.setFont(font);
            g2.setColor(isSelected ? new Color(10, 10, 40) : Color.WHITE);
            g2.drawString(category, categoryX + 15, y + 28);
            
            y += itemHeight;
        }
    }
    
    private void drawContent(Graphics2D g2, int contentX, int panelY, int contentWidth, int panelHeight, 
                            ControlConfig controlConfig, AbilityLoadout loadout, int WIDTH, int HEIGHT) {
        // Enable clipping for scroll
        Shape oldClip = g2.getClip();
        g2.setClip(contentX, panelY + 20, contentWidth, panelHeight - 40);
        
        int y = panelY + 60 - scrollOffset;
        
        switch (selectedCategory) {
            case "Controller" -> y = drawControllerContent(g2, contentX, y, contentWidth);
            case "Key bindings" -> y = drawKeyBindingsContent(g2, contentX, y, contentWidth, controlConfig, loadout);
            case "Graphics" -> y = drawGraphicsContent(g2, contentX, y, contentWidth, WIDTH, HEIGHT);
            case "Audio" -> y = drawAudioContent(g2, contentX, y, contentWidth);
            case "Language" -> y = drawLanguageContent(g2, contentX, y, contentWidth);
            case "Instructions" -> y = drawInstructionsContent(g2, contentX, y, contentWidth, controlConfig, loadout);
            case "Credits" -> y = drawCreditsContent(g2, contentX, y, contentWidth);
        }
        
        // Calculate max scroll
        maxScrollOffset = Math.max(0, y - (panelY + panelHeight - 100));
        
        g2.setClip(oldClip);
        
        // Draw scrollbar if needed
        if (maxScrollOffset > 0) {
            drawScrollbar(g2, contentX + contentWidth + 10, panelY + 20, panelHeight - 40);
        }
    }
    
    private int drawControllerContent(Graphics2D g2, int x, int y, int width) {
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // Invert X
        drawToggle(g2, "Invert X", invertX, x, y);
        y += 60;
        
        // Vibration
        drawToggle(g2, "Vibration", vibration, x, y);
        y += 60;
        
        // Vibration Strength
        if (vibration) {
            drawSlider(g2, "Vibration Strength", vibrationStrength, x, y, "vibrationStrength");
            y += 60;
        }
        
        // Movement sensitivity
        drawSlider(g2, "Movement sensitivity", movementSensitivity, x, y, "movementSensitivity");
        y += 60;
        
        // Restore to default button
        drawButton(g2, "Restore to default", x, y, 200);
        y += 80;
        
        return y;
    }
    
    private int drawKeyBindingsContent(Graphics2D g2, int x, int y, int width, 
                                      ControlConfig controlConfig, AbilityLoadout loadout) {
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Player 1", x, y);
        y += 40;
        
        String[] actions = {"ability1", "ability2", "ability3", "ability4", "shoot", "thrust", "left", "right", "hyper"};
        String[] labels = {"Slot 1", "Slot 2", "Slot 3", "Slot 4", "Shoot", "Thrust", "Move Left", "Move Right", "Hyper"};
        
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        for (int i = 0; i < actions.length; i++) {
            String label = labels[i];
            if (i < 4) {
                String ability = loadout.getSlot(i);
                label = ability != null ? ability.toUpperCase() : "Empty";
            }
            
            g2.setColor(new Color(0, 255, 200));
            g2.drawString(label, x, y);
            
            // Key box
            String keyName = controlConfig.getKeyName(controlConfig.getKey(actions[i]));
            int boxX = x + 400;
            g2.setColor(new Color(40, 60, 90, 200));
            g2.fillRoundRect(boxX, y - 20, 80, 30, 8, 8);
            g2.setColor(new Color(0, 200, 180));
            g2.drawRoundRect(boxX, y - 20, 80, 30, 8, 8);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(keyName, boxX + 40 - fm.stringWidth(keyName)/2, y);
            
            y += 45;
        }
        
        y += 20;
        drawButton(g2, "Restore to default", x, y, 200);
        y += 80;
        
        return y;
    }
    
    private int drawGraphicsContent(Graphics2D g2, int x, int y, int width, int WIDTH, int HEIGHT) {
        // Fullscreen resolution
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(new Color(0, 255, 200));
        g2.drawString("Fullscreen resolution", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(WIDTH + "x" + HEIGHT, x + 500, y);
        y += 60;
        
        // Effects
        drawToggle(g2, "Effects", effects, x, y);
        y += 60;
        
        // V-Sync
        drawToggle(g2, "V-Sync", vSync, x, y);
        y += 60;
        
        // FPS
        g2.setColor(new Color(0, 255, 200));
        g2.drawString("FPS", x, y);
        g2.setColor(new Color(0, 200, 255));
        g2.drawString(String.valueOf(fps), x + 500, y);
        y += 60;
        
        // Screen shake
        drawToggle(g2, "Screen shake", screenShake, x, y);
        y += 60;
        
        // Windowed mode
        drawToggle(g2, "Windowed mode", windowedMode, x, y);
        y += 60;
        
        // Color blind mode
        g2.setColor(new Color(0, 255, 200));
        g2.drawString("Color blind mode", x, y);
        g2.setColor(new Color(0, 200, 255));
        g2.drawString(colorBlindMode, x + 500, y);
        y += 60;
        
        drawButton(g2, "Restore to default", x, y, 200);
        y += 80;
        
        return y;
    }
    
    private int drawAudioContent(Graphics2D g2, int x, int y, int width) {
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        
        // SFX Volume
        drawSlider(g2, "Sound Effects Volume", sfxVolume, x, y, "sfxVolume");
        y += 60;
        
        // Music Volume
        drawSlider(g2, "Background Music Volume", musicVolume, x, y, "musicVolume");
        y += 60;
        
        drawButton(g2, "Restore to default", x, y, 200);
        y += 80;
        
        return y;
    }
    
    private int drawLanguageContent(Graphics2D g2, int x, int y, int width) {
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(new Color(0, 255, 200));
        g2.drawString("Language", x, y);
        g2.setColor(Color.WHITE);
        g2.drawString(language, x + 400, y);
        y += 80;
        
        return y;
    }
    
    private int drawInstructionsContent(Graphics2D g2, int x, int y, int width, 
                                       ControlConfig controlConfig, AbilityLoadout loadout) {
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(new Color(255, 200, 100));
        g2.drawString("HOW TO PLAY", x, y);
        y += 40;
        
        g2.setFont(new Font("Arial", Font.PLAIN, 14));
        String[] instructions = {
            "OBJECTIVE:",
            "Survive as long as possible by destroying asteroids and avoiding hazards.",
            "Collect fuel and XP to level up and unlock powerful abilities.",
            "",
            "BASIC CONTROLS:",
            controlConfig.getKeyName(controlConfig.getKey("thrust")) + " - Thrust forward",
            controlConfig.getKeyName(controlConfig.getKey("left")) + "/" + 
                controlConfig.getKeyName(controlConfig.getKey("right")) + " - Rotate left/right",
            controlConfig.getKeyName(controlConfig.getKey("shoot")) + " - Fire weapon",
            controlConfig.getKeyName(controlConfig.getKey("hyper")) + " - Hyper mode (faster movement, uses fuel)",
            "",
            "ABILITIES:",
            "Unlock abilities by leveling up. Equip them in the Key bindings section.",
            "",
            "VOID DIMENSION:",
            "Enter an alternate dimension where asteroids become ghostly.",
            "Absorb void energy but don't let it reach 100 or you'll die!",
            "",
            "TIPS:",
            "• Manage your fuel carefully - it's needed for thrust and hyper mode",
            "• Use abilities strategically - they have cooldowns",
            "• Higher levels unlock more powerful abilities",
            "• Watch out for black holes and cosmic entities at higher levels"
        };
        
        for (String line : instructions) {
            if (line.isEmpty()) {
                y += 20;
                continue;
            }
            
            if (line.endsWith(":")) {
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.setColor(new Color(255, 200, 100));
            } else if (line.startsWith("•")) {
                g2.setFont(new Font("Arial", Font.PLAIN, 13));
                g2.setColor(new Color(200, 200, 220));
            } else {
                g2.setFont(new Font("Arial", Font.PLAIN, 14));
                g2.setColor(Color.WHITE);
            }
            
            g2.drawString(line, x + (line.startsWith("•") ? 20 : 0), y);
            y += 25;
        }
        
        return y;
    }
    
    private int drawCreditsContent(Graphics2D g2, int x, int y, int width) {
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(255, 200, 100));
        g2.drawString("ASTEROIDS: Navigate the Void", x, y);
        y += 60;
        
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        String[] credits = {
            "Created by: [Your Name]",
            "",
            "Programming: Java/Swing",
            "Graphics: Custom 2D Rendering",
            "",
            "Special Thanks:",
            "• Classic Asteroids (1979) for inspiration",
            "• Java community for support",
            "",
            "Version 1.0 - 2025"
        };
        
        for (String line : credits) {
            if (line.isEmpty()) {
                y += 15;
            } else {
                g2.drawString(line, x, y);
                y += 30;
            }
        }
        
        return y;
    }
    
    private void drawToggle(Graphics2D g2, String label, boolean value, int x, int y) {
        g2.setColor(new Color(0, 255, 200));
        g2.drawString(label, x, y);
        
        // Toggle switch
        int toggleX = x + 550;
        int toggleWidth = 50;
        int toggleHeight = 24;
        
        g2.setColor(value ? new Color(0, 255, 200) : new Color(100, 100, 100));
        g2.fillRoundRect(toggleX, y - 16, toggleWidth, toggleHeight, 12, 12);
        
        // Toggle circle
        int circleX = value ? toggleX + toggleWidth - 20 : toggleX + 4;
        g2.setColor(Color.WHITE);
        g2.fillOval(circleX, y - 12, 16, 16);
        
        // Status text
        g2.setColor(value ? new Color(0, 255, 200) : new Color(150, 150, 150));
        g2.drawString(value ? "ON" : "OFF", toggleX + toggleWidth + 20, y);
    }
    
    private void drawSlider(Graphics2D g2, String label, int value, int x, int y, String sliderName) {
        g2.setColor(new Color(0, 255, 200));
        g2.drawString(label, x, y);
        
        // Slider track
        int sliderX = x + 550;
        int sliderWidth = 150;
        g2.setColor(new Color(60, 80, 100));
        g2.fillRoundRect(sliderX, y - 8, sliderWidth, 8, 4, 4);
        
        // Slider fill
        int fillWidth = (int)(sliderWidth * (value / 100.0));
        g2.setColor(new Color(0, 255, 200));
        g2.fillRoundRect(sliderX, y - 8, fillWidth, 8, 4, 4);
        
        // Slider handle
        g2.setColor(Color.WHITE);
        g2.fillOval(sliderX + fillWidth - 8, y - 12, 16, 16);
        
        // Value text
        g2.setColor(new Color(0, 200, 255));
        g2.drawString("[" + value + "]", sliderX + sliderWidth + 20, y);
    }
    
    private void drawButton(Graphics2D g2, String label, int x, int y, int width) {
        g2.setColor(new Color(40, 60, 90, 200));
        g2.fillRoundRect(x, y, width, 35, 8, 8);
        g2.setColor(new Color(0, 200, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width, 35, 8, 8);
        g2.setStroke(new BasicStroke(1));
        
        g2.setColor(new Color(0, 255, 200));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + width/2 - fm.stringWidth(label)/2, y + 22);
    }
    
    private void drawScrollbar(Graphics2D g2, int x, int y, int height) {
        // Track
        g2.setColor(new Color(40, 60, 80, 150));
        g2.fillRoundRect(x, y, 8, height, 4, 4);
        
        // Thumb
        if (maxScrollOffset > 0) {
            int thumbHeight = Math.max(30, (int)(height * (height / (float)(height + maxScrollOffset))));
            int thumbY = y + (int)((height - thumbHeight) * (scrollOffset / (float)maxScrollOffset));
            
            g2.setColor(new Color(0, 255, 200));
            g2.fillRoundRect(x, thumbY, 8, thumbHeight, 4, 4);
        }
    }
    
    private void drawCloseButton(Graphics2D g2, int panelX, int panelY, int panelWidth) {
        int buttonSize = 40;
        int buttonX = panelX + panelWidth - buttonSize - 15;
        int buttonY = panelY + 15;
        
        closeButtonBounds = new Rectangle(buttonX, buttonY, buttonSize, buttonSize);
        
        g2.setColor(new Color(255, 100, 100, 150));
        g2.fillOval(buttonX, buttonY, buttonSize, buttonSize);
        g2.setColor(new Color(255, 150, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(buttonX, buttonY, buttonSize, buttonSize);
        
        // X mark
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3));
        int offset = 12;
        g2.drawLine(buttonX + offset, buttonY + offset, 
                    buttonX + buttonSize - offset, buttonY + buttonSize - offset);
        g2.drawLine(buttonX + buttonSize - offset, buttonY + offset, 
                    buttonX + offset, buttonY + buttonSize - offset);
        g2.setStroke(new BasicStroke(1));
    }
    
    // Getters
    public int getSFXVolume() { return sfxVolume; }
    public int getMusicVolume() { return musicVolume; }
    public boolean isEffectsEnabled() { return effects; }
    public boolean isScreenShakeEnabled() { return screenShake; }
}
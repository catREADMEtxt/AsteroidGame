import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ControlPanel extends JPanel {
    private AbilityLoadout loadout;
    private ControlConfig config;
    private String configuringAction = null;
    private boolean waitingForKey = false;
    private int selectedSlot = -1;
    private int hoveredSlot = -1;
    private int hoveredAbility = -1;
    private boolean hasChanges = false;
    
    // UI Colors matching instructions panel
    private final Color PANEL_BG = new Color(20, 25, 40, 250);
    private final Color BORDER_COLOR = new Color(100, 150, 255, 200);
    private final Color TEXT_COLOR = new Color(150, 200, 255);
    private final Color SECONDARY_TEXT = new Color(200, 200, 220);
    private final Color HIGHLIGHT_COLOR = new Color(100, 150, 255, 80);
    private final Color SLOT_EMPTY = new Color(40, 40, 40, 150);
    private final Color SLOT_FILLED = new Color(60, 80, 120, 220);
    
    public ControlPanel(AbilityLoadout loadout, ControlConfig config) {
        this.loadout = loadout;
        this.config = config;
        setLayout(null);
        setOpaque(false);
        
        setupMouseListeners();
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        });
        setFocusable(true);
    }
    
    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e);
            }
        });
    }
    
    private void handleMouseClick(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        // Check ability slot clicks (for selection)
        for (int i = 0; i < 4; i++) {
            Rectangle slotBounds = getSlotBounds(i);
            if (slotBounds.contains(mx, my)) {
                selectedSlot = i;
                repaint();
                return;
            }
        }
        
        // Check available ability clicks (for assigning to selected slot)
        if (selectedSlot != -1) {
            String[] allAbilities = loadout.getAllAbilities();
            for (int i = 0; i < allAbilities.length; i++) {
                Rectangle abilityBounds = getAvailableAbilityBounds(i);
                if (abilityBounds.contains(mx, my)) {
                    loadout.setSlot(selectedSlot, allAbilities[i]);
                    selectedSlot = -1;
                    hasChanges = true;
                    repaint();
                    return;
                }
            }
        }
        
        // Check control key clicks
        String[] actions = {"ability1", "ability2", "ability3", "ability4", "shoot", "thrust", "left", "right", "hyper"};
        for (int i = 0; i < actions.length; i++) {
            Rectangle keyBounds = getKeyConfigBounds(i);
            if (keyBounds.contains(mx, my)) {
                startKeyConfig(actions[i]);
                repaint();
                return;
            }
        }
        
        // Deselect if clicking elsewhere
        selectedSlot = -1;
        repaint();
    }
    
    private void handleMouseMove(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        
        // Update hovered slot
        int oldHoveredSlot = hoveredSlot;
        hoveredSlot = -1;
        for (int i = 0; i < 4; i++) {
            if (getSlotBounds(i).contains(mx, my)) {
                hoveredSlot = i;
                break;
            }
        }
        
        // Update hovered ability
        int oldHoveredAbility = hoveredAbility;
        hoveredAbility = -1;
        String[] allAbilities = loadout.getAllAbilities();
        for (int i = 0; i < allAbilities.length; i++) {
            if (getAvailableAbilityBounds(i).contains(mx, my)) {
                hoveredAbility = i;
                break;
            }
        }
        
        // Repaint if hover state changed
        if (oldHoveredSlot != hoveredSlot || oldHoveredAbility != hoveredAbility) {
            repaint();
        }
    }
    
    private void handleKeyPress(KeyEvent e) {
        if (waitingForKey && configuringAction != null) {
            int keyCode = e.getKeyCode();
            
            // Prevent ESC and ENTER from being bound
            if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER) {
                waitingForKey = false;
                configuringAction = null;
                repaint();
                return;
            }
            
            if (config.setControl(configuringAction, keyCode)) {
                hasChanges = true;
                waitingForKey = false;
                configuringAction = null;
                repaint();
            } else {
                // Key already in use - flash error
                waitingForKey = false;
                configuringAction = null;
                repaint();
            }
        }
    }
    
    private void startKeyConfig(String action) {
        configuringAction = action;
        waitingForKey = true;
        requestFocusInWindow();
    }
    
    private Rectangle getSlotBounds(int slot) {
        int panelWidth = 700;
        int panelX = (getWidth() - panelWidth) / 2;
        int slotWidth = 140;
        int slotHeight = 80;
        int slotY = 100;
        int spacing = 20;
        int startX = panelX + 50;
        
        return new Rectangle(startX + slot * (slotWidth + spacing), slotY, slotWidth, slotHeight);
    }
    
    private Rectangle getAvailableAbilityBounds(int index) {
        int panelWidth = 700;
        int panelX = (getWidth() - panelWidth) / 2;
        int itemWidth = 180;
        int itemHeight = 35;
        int startY = 220;
        int startX = panelX + 50;
        
        return new Rectangle(startX, startY + index * (itemHeight + 5), itemWidth, itemHeight);
    }
    
    private Rectangle getKeyConfigBounds(int index) {
        int panelWidth = 700;
        int panelX = (getWidth() - panelWidth) / 2;
        int itemWidth = 260;
        int itemHeight = 35;
        int startY = 220;
        int startX = panelX + 390;
        
        return new Rectangle(startX, startY + index * (itemHeight + 5), itemWidth, itemHeight);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Semi-transparent backdrop
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, getWidth(), getHeight());
        
        // Main panel
        int panelWidth = 700;
        int panelHeight = 550;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;
        
        // Panel background
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        
        // Border
        g2.setColor(BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 15, 15);
        g2.setStroke(new BasicStroke(1));
        
        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.setColor(TEXT_COLOR);
        String title = "CONTROL CONFIGURATION";
        FontMetrics titleFm = g2.getFontMetrics();
        g2.drawString(title, panelX + panelWidth/2 - titleFm.stringWidth(title)/2, panelY + 40);
        
        // Separator
        g2.setColor(new Color(100, 150, 255, 100));
        g2.drawLine(panelX + 50, panelY + 60, panelX + panelWidth - 50, panelY + 60);
        
        // Draw sections
        drawAbilitySlots(g2, panelX, panelY);
        drawAvailableAbilities(g2, panelX, panelY);
        drawKeyConfiguration(g2, panelX, panelY);
        drawInstructions(g2, panelX, panelY, panelWidth, panelHeight);
    }
    
    private void drawAbilitySlots(Graphics2D g2, int panelX, int panelY) {
        // Section header
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(255, 200, 100));
        g2.drawString("EQUIPPED ABILITIES", panelX + 50, panelY + 90);
        
        // Draw 4 slots
        for (int i = 0; i < 4; i++) {
            Rectangle bounds = getSlotBounds(i);
            String ability = loadout.getSlot(i);
            String key = config.getKeyName(config.getKey("ability" + (i + 1)));
            
            // Slot background
            boolean isSelected = (i == selectedSlot);
            boolean isHovered = (i == hoveredSlot);
            
            if (isSelected) {
                g2.setColor(new Color(100, 150, 255, 150));
            } else if (isHovered) {
                g2.setColor(HIGHLIGHT_COLOR);
            } else {
                g2.setColor(ability != null && !ability.isEmpty() ? SLOT_FILLED : SLOT_EMPTY);
            }
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
            
            // Border
            g2.setColor(isSelected ? new Color(150, 200, 255) : BORDER_COLOR);
            g2.setStroke(new BasicStroke(isSelected ? 3 : 2));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
            g2.setStroke(new BasicStroke(1));
            
            // Key label
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(new Color(255, 215, 0));
            g2.drawString("[" + key + "]", bounds.x + 10, bounds.y + 20);
            
            // Slot number
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.setColor(new Color(150, 150, 180));
            g2.drawString("Slot " + (i + 1), bounds.x + 10, bounds.y + bounds.height - 10);
            
            // Ability name
            if (ability != null && !ability.isEmpty()) {
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.setColor(Color.WHITE);
                FontMetrics fm = g2.getFontMetrics();
                String displayName = ability.toUpperCase();
                g2.drawString(displayName, 
                    bounds.x + bounds.width/2 - fm.stringWidth(displayName)/2, 
                    bounds.y + bounds.height/2 + 5);
            } else {
                g2.setFont(new Font("Arial", Font.ITALIC, 12));
                g2.setColor(new Color(120, 120, 120));
                String emptyText = "Empty";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(emptyText, 
                    bounds.x + bounds.width/2 - fm.stringWidth(emptyText)/2, 
                    bounds.y + bounds.height/2 + 5);
            }
        }
        
        // Selection hint
        if (selectedSlot != -1) {
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(new Color(255, 215, 0));
            String hint = "▼ Select an ability below to assign to Slot " + (selectedSlot + 1);
            g2.drawString(hint, panelX + 50, panelY + 200);
        }
    }
    
    private void drawAvailableAbilities(Graphics2D g2, int panelX, int panelY) {
        // Section header
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(255, 200, 100));
        g2.drawString("AVAILABLE ABILITIES:", panelX + 50, panelY + 210);
        
        String[] allAbilities = loadout.getAllAbilities();
        for (int i = 0; i < allAbilities.length; i++) {
            Rectangle bounds = getAvailableAbilityBounds(i);
            boolean isHovered = (i == hoveredAbility && selectedSlot != -1);
            
            // Background
            if (isHovered) {
                g2.setColor(HIGHLIGHT_COLOR);
                g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            }
            
            // Border
            g2.setColor(new Color(80, 120, 180, 100));
            g2.setStroke(new BasicStroke(1));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            
            // Ability name
            g2.setFont(new Font("Arial", Font.PLAIN, 13));
            g2.setColor(isHovered ? Color.WHITE : SECONDARY_TEXT);
            g2.drawString("• " + allAbilities[i].toUpperCase(), bounds.x + 10, bounds.y + 22);
        }
    }
    
    private void drawKeyConfiguration(Graphics2D g2, int panelX, int panelY) {
        // Section header
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(255, 200, 100));
        g2.drawString("KEY BINDINGS:", panelX + 400, panelY + 210);
        
        String[] actions = {"ability1", "ability2", "ability3", "ability4", "shoot", "thrust", "left", "right", "hyper"};
        String[] labels = {"Ability 1", "Ability 2", "Ability 3", "Ability 4", "Shoot", "Thrust", "Rotate Left", "Rotate Right", "Hyper"};
        
        for (int i = 0; i < actions.length; i++) {
            Rectangle bounds = getKeyConfigBounds(i);
            String action = actions[i];
            boolean isConfiguring = waitingForKey && action.equals(configuringAction);
            
            // Background
            g2.setColor(isConfiguring ? new Color(255, 215, 0, 100) : new Color(50, 60, 80, 150));
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            
            // Border
            g2.setColor(isConfiguring ? new Color(255, 215, 0) : new Color(80, 120, 180));
            g2.setStroke(new BasicStroke(isConfiguring ? 2 : 1));
            g2.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8);
            g2.setStroke(new BasicStroke(1));
            
            // Label
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(SECONDARY_TEXT);
            g2.drawString(labels[i] + ":", bounds.x + 10, bounds.y + 22);
            
            // Key name
            g2.setFont(new Font("Arial", Font.BOLD, 13));
            String keyName = isConfiguring ? "Press key..." : config.getKeyName(config.getKey(action));
            g2.setColor(isConfiguring ? new Color(255, 215, 0) : new Color(100, 255, 150));
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(keyName, bounds.x + bounds.width - fm.stringWidth(keyName) - 10, bounds.y + 22);
        }
    }
    
    private void drawInstructions(Graphics2D g2, int panelX, int panelY, int panelWidth, int panelHeight) {
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.setColor(new Color(150, 150, 180));
        
        String[] instructions = {
            "• Click a slot to select it, then click an ability to assign",
            "• Click a key binding to rebind it (ESC/ENTER cannot be rebound)",
            "• Press C or ESC to close and save changes"
        };
        
        int y = panelY + panelHeight - 60;
        for (String instruction : instructions) {
            g2.drawString(instruction, panelX + 50, y);
            y += 20;
        }
        
        // Changes indicator
        if (hasChanges) {
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(new Color(100, 255, 150));
            g2.drawString("✓ Changes will be saved", panelX + panelWidth - 180, panelY + panelHeight - 20);
        }
    }
    
    public boolean hasChanges() {
        return hasChanges;
    }
    
    public void resetChanges() {
        hasChanges = false;
    }
}
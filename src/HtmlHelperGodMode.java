// ===============================
// IMPORTS
// ===============================
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.geom.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

// ===============================
// MAIN FRAME CLASS
// ===============================
public class HtmlHelperGodMode extends JFrame {

    // ===============================
    // UI THEME COLORS
    // ===============================
    // Dark background colors for application styling
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SIDEBAR_COLOR = new Color(28, 28, 28);
    private final Color BTN_COLOR = new Color(40, 40, 40);
    private final Color ACCENT_COLOR = new Color(0, 120, 215);

    // Border colors for different shape types
    private final Color RECT_BORDER = new Color(0, 255, 120);
    private final Color OVAL_BORDER = new Color(255, 215, 0);
    private final Color RHOMB_BORDER = new Color(255, 60, 60);

    // ===============================
    // CORE DATA FIELDS
    // ===============================
    // Loaded image
    private BufferedImage img;

    // Drawing state
    private Point startPoint, endPoint;

    // All created shapes
    private ArrayList<ShapeData> shapes = new ArrayList<>();

    // List model for HTML output
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> coordsJList = new JList<>(listModel);

    // Current drawing mode (RECT, OVAL, RHOMBUS)
    private String currentMode = "RECT";

    // Zoom and camera position
    private double zoomFactor = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastMousePos;

    // Selected shape logic
    private ShapeData selectedShape = null;
    private int currentHandle = -1;
    private Point dragStartOffset = null;

    // ===============================
    // CONSTRUCTOR
    // ===============================
    public HtmlHelperGodMode() {
        try {
            // Use system look & feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("HTML Image Map Ultra Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        getContentPane().setBackground(BG_COLOR);

        setLocationRelativeTo(null);
        
        
     // ===============================
     // SIDEBAR & TOP PANEL UI
     // ===============================

     // --- SIDEBAR ---
     JPanel sidebar = new JPanel(new GridLayout(6, 1, 10, 10));
     sidebar.setBackground(SIDEBAR_COLOR);
     sidebar.setPreferredSize(new Dimension(180, 0));
     sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

     // Buttons for selecting shape type
     sidebar.add(createModeButton("RECTANGLE", "RECT", RECT_BORDER));
     sidebar.add(createModeButton("CIRCLE", "OVAL", OVAL_BORDER));
     sidebar.add(createModeButton("RHOMBUS", "RHOMBUS", RHOMB_BORDER));

     // --- TOPBAR ---
     JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
     topPanel.setBackground(SIDEBAR_COLOR);
     topPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));

     // File loading and zoom controls
     topPanel.add(createActionButton("📂 LOAD IMAGE", "LOAD", true));
     topPanel.add(createActionButton("🔍 +", "Z_IN", true));
     topPanel.add(createActionButton("🔍 -", "Z_OUT", true));

     add(sidebar, BorderLayout.WEST);
     add(topPanel, BorderLayout.NORTH);

        
        
        
    }
     
}

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

public class HtmlHelperGodMode extends JFrame {
    private final Color BG_COLOR = new Color(18, 18, 18);
    private final Color SIDEBAR_COLOR = new Color(28, 28, 28);
    private final Color BTN_COLOR = new Color(40, 40, 40);
    private final Color ACCENT_COLOR = new Color(0, 120, 215);
    
    private final Color RECT_BORDER = new Color(0, 255, 120);
    private final Color OVAL_BORDER = new Color(255, 215, 0);
    private final Color RHOMB_BORDER = new Color(255, 60, 60);

    private BufferedImage img;
    private Point startPoint, endPoint;
    private ArrayList<ShapeData> shapes = new ArrayList<>();
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> coordsJList = new JList<>(listModel);
    
    private String currentMode = "RECT";
    private double zoomFactor = 1.0;
    private int offsetX = 0, offsetY = 0;
    private Point lastMousePos;
    
    private ShapeData selectedShape = null;
    private int currentHandle = -1;
    private Point dragStartOffset = null;

    public HtmlHelperGodMode() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        setTitle("HTML Image Map Ultra Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        getContentPane().setBackground(BG_COLOR);

        // --- SIDEBAR ---
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 10, 10));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        sidebar.add(createModeButton("RECTANGLE", "RECT", RECT_BORDER));
        sidebar.add(createModeButton("CIRCLE", "OVAL", OVAL_BORDER));
        sidebar.add(createModeButton("RHOMBUS", "RHOMBUS", RHOMB_BORDER));

        // --- TOPBAR ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        topPanel.setBackground(SIDEBAR_COLOR);
        topPanel.setBorder(new MatteBorder(0, 0, 1, 0, Color.BLACK));

        topPanel.add(createActionButton("📂 LOAD IMAGE", "LOAD", true)); 
        topPanel.add(createActionButton("🔍 +", "Z_IN", true));
        topPanel.add(createActionButton("🔍 -", "Z_OUT", true));

        // --- CANVAS ---
        JPanel canvas = new JPanel() {
            { 
                setBackground(BG_COLOR);
                setupDragAndDrop(this);
            }
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (img == null) {
                    g2.setColor(Color.DARK_GRAY);
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 20));
                    g2.drawString("Load or Drop an image here", getWidth()/2 - 130, getHeight()/2);
                    return;
                }
                
                constrainOffsets();
                AffineTransform old = g2.getTransform();
                g2.translate(offsetX, offsetY);
                g2.scale(zoomFactor, zoomFactor);
                g2.drawImage(img, 0, 0, null);

                for (ShapeData s : shapes) drawShape(g2, s, s == selectedShape);

                if (startPoint != null && endPoint != null && selectedShape == null) {
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke((float)(1.5/zoomFactor)));
                    drawShape(g2, new ShapeData(currentMode, startPoint, endPoint), false);
                }
                g2.setTransform(old);
            }
        };

        initMouseLogic(canvas);

        // --- RIGHT PANEL ---
        JPanel rightPanel = new JPanel(new BorderLayout(0, 10));
        rightPanel.setPreferredSize(new Dimension(320, 0));
        rightPanel.setBackground(SIDEBAR_COLOR);
        rightPanel.setBorder(new EmptyBorder(15, 10, 15, 10));

        coordsJList.setBackground(new Color(20, 20, 20));
        coordsJList.setForeground(Color.LIGHT_GRAY);
        
        JButton btnCopy = createActionButton("📋 COPY HTML", "COPY", true);
        btnCopy.setBackground(ACCENT_COLOR);

        rightPanel.add(new JLabel("MAPPED AREAS:") {{ setForeground(Color.GRAY); }}, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(coordsJList) {{ setBorder(null); }}, BorderLayout.CENTER);
        rightPanel.add(btnCopy, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.WEST);
        add(topPanel, BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        setupKeyboard();
        setLocationRelativeTo(null);
    }

    private void styleDarkButton(JButton b) {
        b.setContentAreaFilled(false);
        b.setOpaque(true);
        b.setBackground(BTN_COLOR);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(Color.BLACK, 1));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(60, 60, 60)); }
            public void mouseExited(MouseEvent e) { b.setBackground(BTN_COLOR); }
        });
    }

    private JButton createModeButton(String label, String mode, Color ind) {
        JButton b = new JButton("<html><center>" + label + "<br><font size='5' color='#" + Integer.toHexString(ind.getRGB()).substring(2) + "'>●</font></center></html>");
        styleDarkButton(b);
        b.setFont(new Font("SansSerif", Font.BOLD, 14)); 
        b.addActionListener(e -> currentMode = mode);
        return b;
    }

    private JButton createActionButton(String text, String cmd, boolean enlarge) {
        JButton b = new JButton(text);
        styleDarkButton(b);
        if (enlarge) {
            b.setMargin(new Insets(15, 30, 15, 30)); 
            b.setFont(new Font("SansSerif", Font.BOLD, 13));
        }
        b.addActionListener(e -> {
            switch(cmd) {
                case "LOAD" -> loadFile();
                case "Z_IN" -> { zoomFactor *= 1.2; repaint(); }
                case "Z_OUT" -> { zoomFactor /= 1.2; repaint(); }
                case "COPY" -> copyToClipboard();
            }
        });
        return b;
    }

    private void setupDragAndDrop(JPanel panel) {
        new DropTarget(panel, new DropTargetAdapter() {
            @Override
            public void drop(DropTargetDropEvent dtde) {
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    Transferable t = dtde.getTransferable();
                    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                        if (!files.isEmpty()) openFile(files.get(0));
                    }
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Error processing drop!");
                }
            }
        });
    }

    private void openFile(File file) {
        try {
            BufferedImage newImg = ImageIO.read(file);
            if (newImg == null) {
                JOptionPane.showMessageDialog(this, "File is not a valid image!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            img = newImg;
            zoomFactor = 1.0; offsetX = 0; offsetY = 0;
            shapes.clear(); updateList(); repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading image!");
        }
    }

    private void loadFile() {
        JFileChooser jfc = new JFileChooser();
        jfc.setAcceptAllFileFilterUsed(false);
        jfc.addChoosableFileFilter(new FileNameExtensionFilter("Images (jpg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"));
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            openFile(jfc.getSelectedFile());
        }
    }

    private void drawShape(Graphics2D g2, ShapeData s, boolean selected) {
        Rectangle r = s.getRect();
        Color c = selected ? ACCENT_COLOR : (s.type.equals("OVAL") ? OVAL_BORDER : s.type.equals("RHOMBUS") ? RHOMB_BORDER : RECT_BORDER);
        g2.setColor(c);
        g2.setStroke(new BasicStroke((float)((selected ? 3 : 2) / zoomFactor)));
        Shape obj;
        if (s.type.equals("RECT")) obj = r;
        else if (s.type.equals("OVAL")) obj = new Ellipse2D.Double(r.x, r.y, r.width, r.height);
        else {
            int[] xs = {r.x + r.width/2, r.x + r.width, r.x + r.width/2, r.x};
            int[] ys = {r.y, r.y + r.height/2, r.y + r.height, r.y + r.height/2};
            obj = new Polygon(xs, ys, 4);
        }
        g2.draw(obj);
        if (selected) {
            int hSize = (int)(10 / zoomFactor);
            g2.setColor(Color.WHITE);
            int[] hX = {r.x, r.x + r.width/2, r.x + r.width, r.x, r.x + r.width, r.x, r.x + r.width/2, r.x + r.width};
            int[] hY = {r.y, r.y, r.y, r.y + r.height/2, r.y + r.height/2, r.y + r.height, r.y + r.height, r.y + r.height};
            for (int i = 0; i < 8; i++) {
                g2.fill(new Rectangle2D.Double(hX[i] - hSize/2.0, hY[i] - hSize/2.0, hSize, hSize));
                g2.setColor(Color.BLACK);
                g2.draw(new Rectangle2D.Double(hX[i] - hSize/2.0, hY[i] - hSize/2.0, hSize, hSize));
                g2.setColor(Color.WHITE);
            }
        }
    }

    private void constrainOffsets() {
        if (img == null) return;
        int viewW = getContentPane().getWidth() - 180 - 320;
        int viewH = getContentPane().getHeight() - 60;
        int imgW = (int)(img.getWidth() * zoomFactor);
        int imgH = (int)(img.getHeight() * zoomFactor);
        if (imgW < viewW) offsetX = (viewW - imgW) / 2;
        else offsetX = Math.min(0, Math.max(offsetX, viewW - imgW));
        if (imgH < viewH) offsetY = (viewH - imgH) / 2;
        else offsetY = Math.min(0, Math.max(offsetY, viewH - imgH));
    }

    private void initMouseLogic(JPanel p) {
        p.addMouseWheelListener(e -> {
            if (img == null) return;
            if (e.getWheelRotation() < 0) zoomFactor *= 1.1;
            else zoomFactor /= 1.1;
            zoomFactor = Math.max(0.1, Math.min(zoomFactor, 10.0));
            repaint();
        });

        p.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastMousePos = e.getPoint();
                Point world = screenToWorld(e.getPoint());
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (selectedShape != null) {
                        currentHandle = selectedShape.getHandleAt(world, zoomFactor);
                        if (currentHandle != -1) return;
                    }
                    ShapeData found = null;
                    for (int i = shapes.size() - 1; i >= 0; i--) if (shapes.get(i).contains(world)) { found = shapes.get(i); break; }
                    if (found != null) {
                        selectedShape = found;
                        dragStartOffset = new Point(world.x - found.p1.x, world.y - found.p1.y);
                    } else { selectedShape = null; startPoint = world; }
                }
                p.requestFocusInWindow();
                repaint();
            }
            public void mouseReleased(MouseEvent e) {
                if (startPoint != null && endPoint != null && startPoint.distance(endPoint) > 5) {
                    shapes.add(new ShapeData(currentMode, startPoint, endPoint));
                    updateList();
                }
                startPoint = null; endPoint = null; currentHandle = -1;
                repaint();
            }
        });
        p.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point world = screenToWorld(e.getPoint());
                if (SwingUtilities.isRightMouseButton(e)) {
                    offsetX += (e.getX() - lastMousePos.x); offsetY += (e.getY() - lastMousePos.y);
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (selectedShape != null) {
                        if (currentHandle != -1) selectedShape.resize(currentHandle, world);
                        else if (dragStartOffset != null) selectedShape.move(world.x - selectedShape.p1.x - dragStartOffset.x, world.y - selectedShape.p1.y - dragStartOffset.y);
                        updateList();
                    } else endPoint = world;
                }
                lastMousePos = e.getPoint(); repaint();
            }
        });
    }

    private Point screenToWorld(Point p) {
        return new Point((int)((p.x - offsetX) / zoomFactor), (int)((p.y - offsetY) / zoomFactor));
    }

    private void updateList() {
        listModel.clear();
        for (ShapeData s : shapes) {
            Rectangle r = s.getRect();
            String type = s.type.equals("OVAL") ? "circle" : (s.type.equals("RHOMBUS") ? "poly" : "rect");
            String c = type.equals("circle") ? (r.x+r.width/2)+","+(r.y+r.height/2)+","+(r.width/2) :
                       (type.equals("rect") ? r.x+","+r.y+","+(r.x+r.width)+","+(r.y+r.height) : 
                       (r.x+r.width/2)+","+r.y+","+(r.x+r.width)+","+(r.y+r.height/2)+","+(r.x+r.width/2)+","+(r.y+r.height)+","+r.x+","+(r.y+r.height/2));
            s.tag = "<area shape=\""+type+"\" coords=\""+c+"\" href=\"#\">";
            listModel.addElement(s.tag);
        }
    }

    private void copyToClipboard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < listModel.size(); i++) sb.append(listModel.getElementAt(i)).append("\n");
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        JOptionPane.showMessageDialog(this, "Copied!");
    }

    private void setupKeyboard() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_DELETE) {
                if (selectedShape != null) { shapes.remove(selectedShape); selectedShape = null; updateList(); repaint(); }
                return true;
            }
            return false;
        });
    }

    static class ShapeData {
        String type, tag; Point p1, p2;
        ShapeData(String t, Point p1, Point p2) { this.type = t; this.p1 = new Point(p1); this.p2 = new Point(p2); }
        Rectangle getRect() { return new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), Math.max(1, Math.abs(p1.x - p2.x)), Math.max(1, Math.abs(p1.y - p2.y))); }
        void move(int dx, int dy) { p1.translate(dx, dy); p2.translate(dx, dy); }
        int getHandleAt(Point p, double z) {
            Rectangle r = getRect();
            int s = (int)(12 / z);
            int[] hX = {r.x, r.x + r.width/2, r.x + r.width, r.x, r.x + r.width, r.x, r.x + r.width/2, r.x + r.width};
            int[] hY = {r.y, r.y, r.y, r.y + r.height/2, r.y + r.height/2, r.y + r.height, r.y + r.height, r.y + r.height};
            for (int i = 0; i < 8; i++) if (new Rectangle(hX[i]-s/2, hY[i]-s/2, s, s).contains(p)) return i;
            return -1;
        }
        void resize(int h, Point p) {
            if (h==0||h==3||h==5) p1.x=p.x; if (h==2||h==4||h==7) p2.x=p.x;
            if (h==0||h==1||h==2) p1.y=p.y; if (h==5||h==6||h==7) p2.y=p.y;
        }
        boolean contains(Point p) {
            Rectangle r = getRect();
            if (type.equals("OVAL")) return new Ellipse2D.Double(r.x, r.y, r.width, r.height).contains(p);
            if (type.equals("RHOMBUS")) {
                int[] xs = {r.x + r.width/2, r.x + r.width, r.x + r.width/2, r.x};
                int[] ys = {r.y, r.y + r.height/2, r.y + r.height, r.y + r.height/2};
                return new Polygon(xs, ys, 4).contains(p);
            }
            return r.contains(p);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HtmlHelperGodMode().setVisible(true));
    }
}
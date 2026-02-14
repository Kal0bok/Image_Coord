

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.ArrayList;

public class HtmlHelperApp extends JFrame {
    private BufferedImage img;
    private Point startPoint;
    private Point endPoint;
    
    // Список для хранения объектов (тип фигуры + координаты)
    private ArrayList<ShapeData> shapes = new ArrayList<>();
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> coordsJList = new JList<>(listModel);
    
    private String currentMode = "RECT"; // Режим по умолчанию

    public HtmlHelperApp() {
        setTitle("HTML Helper Pro - Java Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLayout(new BorderLayout());

        // --- ВЕРХНЯЯ ПАНЕЛЬ (Кнопки) ---
        JPanel topPanel = new JPanel();
        JButton btnLoad = new JButton("Загрузить фото");
        JButton btnRect = new JButton("Прямоугольник");
        JButton btnOval = new JButton("Овал");
        topPanel.add(btnLoad);
        topPanel.add(new JLabel("| Выбор фигуры:"));
        topPanel.add(btnRect);
        topPanel.add(btnOval);
        add(topPanel, BorderLayout.NORTH);

        // --- ЦЕНТРАЛЬНАЯ ПАНЕЛЬ (Рисование) ---
        JPanel drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (img != null) g.drawImage(img, 0, 0, null);
                
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(2));

                // Рисуем старые фигуры
                for (ShapeData s : shapes) {
                    g2.setColor(Color.GREEN);
                    drawShape(g2, s);
                }

                // Рисуем текущую (активную) фигуру
                if (startPoint != null && endPoint != null) {
                    g2.setColor(Color.RED);
                    drawShape(g2, new ShapeData(currentMode, getRect(startPoint, endPoint)));
                }
            }
        };

        // --- ПРАВАЯ ПАНЕЛЬ (Координаты) ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setPreferredSize(new Dimension(250, 0));
        rightPanel.setBorder(BorderFactory.createTitledBorder("Координаты (Enter - стоп)"));
        rightPanel.add(new JScrollPane(coordsJList), BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // --- ЛОГИКА МЫШИ ---
        drawingPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { startPoint = e.getPoint(); }
            public void mouseReleased(MouseEvent e) {
                if (startPoint != null && endPoint != null) {
                    Rectangle r = getRect(startPoint, endPoint);
                    shapes.add(new ShapeData(currentMode, r));
                    listModel.addElement(currentMode + ": x=" + r.x + " y=" + r.y + " w=" + r.width + " h=" + r.height);
                }
                startPoint = null;
                endPoint = null;
                repaint();
            }
        });

        drawingPanel.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endPoint = e.getPoint();
                repaint();
            }
        });

        // --- ГОРЯЧИЕ КЛАВИШИ (Enter) ---
        drawingPanel.setFocusable(true);
        drawingPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    JOptionPane.showMessageDialog(null, "Выбор завершен! Всего объектов: " + shapes.size());
                    // Здесь можно добавить сохранение в файл
                }
            }
        });

        // Слушатели кнопок
        btnLoad.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    img = ImageIO.read(chooser.getSelectedFile());
                    drawingPanel.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
                    drawingPanel.revalidate();
                    drawingPanel.requestFocus(); // Чтобы Enter работал сразу
                    repaint();
                } catch (Exception ex) { ex.printStackTrace(); }
            }
        });

        btnRect.addActionListener(e -> { currentMode = "RECT"; drawingPanel.requestFocus(); });
        btnOval.addActionListener(e -> { currentMode = "OVAL"; drawingPanel.requestFocus(); });

        add(new JScrollPane(drawingPanel), BorderLayout.CENTER);
    }

    private void drawShape(Graphics2D g2, ShapeData s) {
        if (s.type.equals("RECT")) g2.drawRect(s.rect.x, s.rect.y, s.rect.width, s.rect.height);
        else g2.drawOval(s.rect.x, s.rect.y, s.rect.width, s.rect.height);
    }

    private Rectangle getRect(Point p1, Point p2) {
        return new Rectangle(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y), 
                             Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
    }

    // Вспомогательный класс для хранения данных
    static class ShapeData {
        String type;
        Rectangle rect;
        ShapeData(String t, Rectangle r) { this.type = t; this.rect = r; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HtmlHelperApp().setVisible(true));
    }
}
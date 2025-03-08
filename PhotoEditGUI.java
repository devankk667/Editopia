import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

public class PhotoEditGUI extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage editedImage;
    private final JLabel imageLabel;
    private final JComboBox<String> filterComboBox;
    private final JSlider exposureSlider, saturationSlider;
    private final Stack<BufferedImage> undoStack;
    private String inputPath;
    private static final int MAX_UNDO = 10;
    private static final Dimension WINDOW_SIZE = new Dimension(1024, 768);
    private static final Dimension IMAGE_SIZE = new Dimension(800, 600);

    public PhotoEditGUI() {
        setTitle("Editopia");
        setSize(WINDOW_SIZE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        undoStack = new Stack<>();

        // Create main panels
        JPanel controlPanel = createControlPanel();
        JPanel imagePanel = createImagePanel();
        
        // Add panels to frame
        add(imagePanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        
        // Initialize components
        imageLabel = new JLabel("No Image Loaded", JLabel.CENTER);
        imageLabel.setPreferredSize(IMAGE_SIZE);
        imagePanel.add(new JScrollPane(imageLabel));

        // Initialize filter controls
        filterComboBox = createFilterComboBox();
        exposureSlider = createSlider("Exposure", 10, 300, 100);
        saturationSlider = createSlider("Saturation", 0, 200, 100);

        // Add control components
        JPanel adjustmentPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        adjustmentPanel.setBorder(BorderFactory.createTitledBorder("Adjustments"));
        adjustmentPanel.add(new JLabel("Filter:"));
        adjustmentPanel.add(filterComboBox);
        adjustmentPanel.add(new JLabel("Exposure:"));
        adjustmentPanel.add(exposureSlider);
        adjustmentPanel.add(new JLabel("Saturation:"));
        adjustmentPanel.add(saturationSlider);
        controlPanel.add(adjustmentPanel);

        // Add buttons
        addButtons(controlPanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setPreferredSize(new Dimension(200, WINDOW_SIZE.height));
        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return panel;
    }

    private JComboBox<String> createFilterComboBox() {
        JComboBox<String> combo = new JComboBox<>(new String[]{"None", "Warm", "Cool", "Vintage", "Sepia", "B&W"});
        combo.addActionListener(e -> applyFilter((String) combo.getSelectedItem()));
        return combo;
    }

    private JSlider createSlider(String name, int min, int max, int value) {
        JSlider slider = new JSlider(min, max, value);
        slider.setMajorTickSpacing((max - min) / 4);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        
        if (name.equals("Exposure")) {
            slider.addChangeListener(e -> {
                if (!slider.getValueIsAdjusting()) {
                    adjustExposure(slider.getValue() / 100.0f);
                }
            });
        } else {
            slider.addChangeListener(e -> {
                if (!slider.getValueIsAdjusting()) {
                    adjustSaturation(slider.getValue() / 100.0f);
                }
            });
        }
        
        return slider;
    }

    private void addButtons(JPanel panel) {
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        JButton loadButton = new JButton("Load Image");
        JButton undoButton = new JButton("Undo");
        JButton saveButton = new JButton("Save Image");
        JButton resetButton = new JButton("Reset All");

        loadButton.addActionListener(e -> loadImage());
        undoButton.addActionListener(e -> undo());
        saveButton.addActionListener(e -> saveImage());
        resetButton.addActionListener(e -> resetImage());

        buttonPanel.add(loadButton);
        buttonPanel.add(undoButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        
        panel.add(buttonPanel);
    }

    private void saveToUndoStack() {
        if (editedImage != null) {
            undoStack.push(deepCopy(editedImage));
            if (undoStack.size() > MAX_UNDO) {
                undoStack.remove(0);
            }
        }
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            editedImage = undoStack.pop();
            displayImage(editedImage);
        }
    }

    private void loadImage() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            inputPath = file.getAbsolutePath();
            try {
                originalImage = ImageIO.read(file);
                editedImage = deepCopy(originalImage);
                displayImage(editedImage);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to load image.");
            }
        }
    }

    private void applyFilter(String filterType) {
        if (editedImage == null) return;
        saveToUndoStack();
        
        if (filterType.equals("None")) {
            resetImage();
            return;
        }

        BufferedImage temp = deepCopy(originalImage);
        
        switch (filterType) {
            case "Warm":
                applyWarmFilter(temp);
                break;
            case "Cool":
                applyCoolFilter(temp);
                break;
            case "Vintage":
                applyVintageFilter(temp);
                break;
            case "Sepia":
                applySepiaFilter(temp);
                break;
            case "B&W":
                applyBWFilter(temp);
                break;
        }
        
        editedImage = temp;
        displayImage(editedImage);
    }

    private void applyWarmFilter(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color color = new Color(img.getRGB(x, y));
                int r = Math.min(255, (int)(color.getRed() * 1.2));
                int g = color.getGreen();
                int b = Math.max(0, (int)(color.getBlue() * 0.8));
                img.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
    }

    private void applyCoolFilter(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color color = new Color(img.getRGB(x, y));
                int r = Math.max(0, (int)(color.getRed() * 0.8));
                int g = color.getGreen();
                int b = Math.min(255, (int)(color.getBlue() * 1.2));
                img.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
    }

    private void applyVintageFilter(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color color = new Color(img.getRGB(x, y));
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                
                // Shift hue slightly towards yellow/orange
                hsb[0] = (hsb[0] + 0.05f) % 1.0f;
                // Reduce saturation
                hsb[1] = Math.min(1.0f, hsb[1] * 0.8f);
                // Add slight fade effect
                hsb[2] = Math.min(1.0f, hsb[2] * 0.9f + 0.1f);
                
                img.setRGB(x, y, Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
            }
        }
    }

    private void applySepiaFilter(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color color = new Color(img.getRGB(x, y));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                
                int tr = (int)(0.393*r + 0.769*g + 0.189*b);
                int tg = (int)(0.349*r + 0.686*g + 0.168*b);
                int tb = (int)(0.272*r + 0.534*g + 0.131*b);
                
                tr = Math.min(255, tr);
                tg = Math.min(255, tg);
                tb = Math.min(255, tb);
                
                img.setRGB(x, y, new Color(tr, tg, tb).getRGB());
            }
        }
    }

    private void applyBWFilter(BufferedImage img) {
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color color = new Color(img.getRGB(x, y));
                // Using luminance formula for better B&W conversion
                int gray = (int)(0.299 * color.getRed() + 
                               0.587 * color.getGreen() +
                               0.114 * color.getBlue());
                // Add slight contrast
                gray = gray < 128 ? gray - 10 : gray + 10;
                gray = Math.max(0, Math.min(255, gray));
                img.setRGB(x, y, new Color(gray, gray, gray).getRGB());
            }
        }
    }

    private void adjustExposure(float value) {
        if (editedImage == null) return;
        RescaleOp op = new RescaleOp(value, 0, null);
        editedImage = op.filter(deepCopy(originalImage), null);  // Always use original as base
        displayImage(editedImage);
    }

    private void adjustSaturation(float value) {
        if (editedImage == null) return;
        BufferedImage temp = deepCopy(originalImage);

        for (int x = 0; x < temp.getWidth(); x++) {
            for (int y = 0; y < temp.getHeight(); y++) {
                Color color = new Color(temp.getRGB(x, y));
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
                hsb[1] = Math.min(Math.max(hsb[1] * value, 0), 1);
                temp.setRGB(x, y, Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
            }
        }
        editedImage = temp;
        displayImage(editedImage);
    }

    private void resetImage() {
        if (originalImage == null) return;
        editedImage = deepCopy(originalImage);
        filterComboBox.setSelectedIndex(0);
        exposureSlider.setValue(100);
        saturationSlider.setValue(100);
        displayImage(editedImage);
    }

    private void saveImage() {
        if (editedImage == null) return;
        String outputPath = "edited_" + new File(inputPath).getName();
        try {
            ImageIO.write(editedImage, "jpg", new File(outputPath));
            JOptionPane.showMessageDialog(this, "Image saved as " + outputPath);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save image.");
        }
    }

    private void displayImage(BufferedImage img) {
        ImageIcon icon = new ImageIcon(img.getScaledInstance(600, 400, Image.SCALE_SMOOTH));
        imageLabel.setIcon(icon);
        imageLabel.setText("");
    }

    private BufferedImage deepCopy(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(source, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PhotoEditGUI::new);
    }
}


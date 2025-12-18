package budgettracker;

import javax.swing.*;
import java.awt.*;

public class SplashScreen extends JWindow {

    private int duration;

    public SplashScreen(int duration) {
        this.duration = duration;
    }

    public void showSplash() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.decode("#121212"));
        content.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));

        try {
            ImageIcon logo = null;
            String[] paths = {
                "logo.png", // Current directory
                "src/assets/logo.png", // In source folder
                "assets/logo.png", // In package folder
                System.getProperty("user.dir") + "/logo.png", // Working directory
                System.getProperty("user.dir") + "/src/assets/logo.png"
            };

            for (String path : paths) {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    logo = new ImageIcon(path);
                    break;
                }
            }

            if (logo == null || logo.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) {
                throw new Exception("Image not found in any location");
            }

            Image scaledImage = logo.getImage().getScaledInstance(500, 400, Image.SCALE_SMOOTH);
            JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            content.add(imageLabel, BorderLayout.CENTER);

            // Loading bar
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            progressBar.setForeground(Color.GREEN);
            progressBar.setBackground(Color.decode("#2a2a2a"));
            progressBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            content.add(progressBar, BorderLayout.SOUTH);

        } catch (Exception e) {
            JPanel errorPanel = new JPanel();
            errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
            errorPanel.setBackground(Color.decode("#121212"));

            JLabel fallback = new JLabel("Budget Tracker", SwingConstants.CENTER);
            fallback.setFont(new Font("Arial", Font.BOLD, 48));
            fallback.setForeground(Color.GREEN);
            fallback.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel errorMsg = new JLabel("Logo image not found", SwingConstants.CENTER);
            errorMsg.setForeground(Color.RED);
            errorMsg.setFont(new Font("Arial", Font.PLAIN, 14));
            errorMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel pathMsg = new JLabel("Looking in: " + System.getProperty("user.dir"), SwingConstants.CENTER);
            pathMsg.setForeground(Color.YELLOW);
            pathMsg.setFont(new Font("Arial", Font.PLAIN, 10));
            pathMsg.setAlignmentX(Component.CENTER_ALIGNMENT);

            errorPanel.add(Box.createVerticalGlue());
            errorPanel.add(fallback);
            errorPanel.add(Box.createVerticalStrut(10));
            errorPanel.add(errorMsg);
            errorPanel.add(Box.createVerticalStrut(5));
            errorPanel.add(pathMsg);
            errorPanel.add(Box.createVerticalGlue());

            content.add(errorPanel, BorderLayout.CENTER);
        }

        setContentPane(content);
        setSize(500, 450);
        setLocationRelativeTo(null);
        setVisible(true);

        // Wait for duration
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        setVisible(false);
        dispose();
    }
}

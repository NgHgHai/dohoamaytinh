package vn.edu.hcmuaf;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("Solar System - JOGL + Swing");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Cấu hình OpenGL
        GLProfile profile = GLProfile.get(GLProfile.GL2);
        GLCapabilities caps = new GLCapabilities(profile);

        // Panel OpenGL
        SolarSystemPanel solarPanel = new SolarSystemPanel(caps);
        Animator animator = new Animator(solarPanel);
        animator.start();
        // Panel điều khiển bên phải
        JPanel controlPanel = new JPanel();
        controlPanel.setPreferredSize(new Dimension(200, 0));
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        JButton stopAnimation = new JButton("Stop Animation");
        stopAnimation.addActionListener(e -> {
            if (animator.isAnimating()) {
                animator.stop();
                stopAnimation.setText("Start Animation");
            } else {
                animator.start();
                stopAnimation.setText("Stop Animation");
            }
        });

        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(stopAnimation);

        add(solarPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainWindow win = new MainWindow();
            win.setVisible(true);
        });
    }
}

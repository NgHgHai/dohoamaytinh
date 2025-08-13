package vn.edu.hcmuaf;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.Animator;

import javax.swing.*;

public class Main implements GLEventListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Cấu hình OpenGL profile
            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLCapabilities capabilities = new GLCapabilities(profile);

            // Tạo GLJPanel thay cho GLCanvas
            GLJPanel panel = new GLJPanel(capabilities);
            Main renderer = new Main();
            panel.addGLEventListener(renderer);

            // Tạo JFrame và add GLJPanel vào
            JFrame frame = new JFrame("JOGL + Swing Example");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new java.awt.BorderLayout());
            frame.add(panel, java.awt.BorderLayout.CENTER);
            frame.setVisible(true);

            // Animator để vẽ liên tục
            Animator animator = new Animator(panel);
            animator.start();
        });
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0f, 0f, 0f, 1f);
    }

    @Override
    public void dispose(GLAutoDrawable drawable) { }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();
        gl.glTranslatef(-0.5f, 0.0f, -6.0f);
        gl.glColor3f(0.0f, 1.0f, 0.0f);

        // Vẽ hình vuông
        gl.glBegin(GL2.GL_QUADS);
        gl.glVertex3f(-1.0f, -1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 0.0f);
        gl.glVertex3f(1.0f, 1.0f, 0.0f);
        gl.glVertex3f(-1.0f, 1.0f, 0.0f);
        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        if (height <= 0) height = 1;
        float aspect = (float) width / height;

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU glu = new GLU();
        glu.gluPerspective(45.0, aspect, 1.0, 100.0);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
}
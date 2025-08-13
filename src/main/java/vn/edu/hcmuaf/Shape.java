package Shape;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class Shape extends GLJPanel implements GLEventListener, KeyListener {

    private GLUT glut = new GLUT();
    private Texture floorTexture;

    // Vị trí vật thể
    private float posX = 0f;
    private float posY = 0f;
    private float posZ = 0f;
    // Biến cho cơ chế nhảy
    private float velocityY = 0f; // Vận tốc theo trục Y
    private final float GRAVITY = -0.02f; // Gia tốc trọng lực
    private final float JUMP_VELOCITY = 0.5f; // Vận tốc nhảy ban đầu cho phím cách
    private final float SMALL_JUMP_VELOCITY = 0.3f; // Vận tốc nhảy nhẹ cho phím số
    private final float SMALL_MOVE_DISTANCE = 0.2f; // Khoảng cách di chuyển nhẹ
    private boolean isOnGround = true; // Trạng thái chạm đất

    public Shape() {
        super(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
        this.addGLEventListener(this);
        this.addKeyListener(this);
        this.setFocusable(true); // Để nhận sự kiện phím
        this.requestFocusInWindow();
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClearColor(0.8f, 0.9f, 1.0f, 1.0f);  // Đặt màu nền (clear color) là màu xanh nhạt trời
        gl.glEnable(GL2.GL_DEPTH_TEST); // Bật kiểm tra chiều sâu (depth test) để 3D đúng lớp che phủ
        gl.glEnable(GL2.GL_LIGHTING);  // Bật chế độ ánh sáng trong OpenGL
        gl.glEnable(GL2.GL_LIGHT0);   // Bật nguồn sáng số 0 (một trong các nguồn sáng có thể có)
        gl.glEnable(GL2.GL_COLOR_MATERIAL); // Cho phép màu vật liệu theo màu được đặt (glColor)
        gl.glColorMaterial(GL2.GL_FRONT, GL2.GL_AMBIENT_AND_DIFFUSE);// Chỉ định màu áp dụng cho mặt trước, ảnh hưởng cả ánh sáng môi trường và khuếch tán

        float[] lightPos = {0, 5, 5, 1};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);

        try {
            java.net.URL textureURL = getClass().getResource("/texture/flower.jpg");
            if (textureURL != null) {
                floorTexture = TextureIO.newTexture(textureURL, true, "jpg");
                floorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MIN_FILTER, GL2.GL_LINEAR);
                floorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_MAG_FILTER, GL2.GL_LINEAR);
                floorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
                floorTexture.setTexParameteri(gl, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {}

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluLookAt(
                9.0, 6.0, 5.0,
                0.0, 0.0, 0.0,
                0.0, 1.0, 0.0
        );

        // Cập nhật vị trí Y (nhảy)
        if (!isOnGround) {
            velocityY += GRAVITY; // Áp dụng trọng lực
            posY += velocityY; // Cập nhật vị trí Y
            if (posY <= 0) { // Kiểm tra chạm đất
                posY = 0;
                velocityY = 0;
                isOnGround = true;
            }
        }

        // Vẽ nền
        drawFloor(gl);

        // Dịch chuyển toàn bộ vật thể theo posX, posY, posZ
        gl.glPushMatrix();
        gl.glTranslatef(posX, posY, posZ);

        // Thân
        gl.glPushMatrix();
        gl.glTranslatef(0f, 1.0f, 0f);
        gl.glColor3f(0f, 1f, 0f);
        glut.glutSolidSphere(1.0, 40, 40); //1: bán kính, 40: số phân đoạn ngang, 40: số phân đoạn dọc
        gl.glPopMatrix();

        // Mũ
        gl.glPushMatrix();
        gl.glTranslatef(0f, 2.0f, 0f);
        gl.glRotatef(-90, 1, 0, 0);
        gl.glColor3f(0.6f, 0f, 1f);
        glut.glutSolidCone(1.0, 1.2, 40, 40);//bán kính, chiều cao,số phân đoạn ngang, số phân đoạn dọc
        gl.glPopMatrix();

        // Mũi
        gl.glPushMatrix();
        gl.glTranslatef(1.0f, 1.0f, 0f);
        gl.glRotatef(90, 0, 1, 0);
        gl.glColor3f(0f, 1f, 0f);
        glut.glutSolidCone(0.3, 0.8, 20, 20);
        gl.glPopMatrix();

        // Mắt trái
        gl.glPushMatrix();
        gl.glTranslatef(0.4f, 1.6f, 0.6f);
        gl.glColor3f(0f, 1f, 0f);
        glut.glutSolidSphere(0.2, 20, 20);
        gl.glPopMatrix();

        // Mắt phải
        gl.glPushMatrix();
        gl.glTranslatef(0.4f, 1.6f, -0.6f); // Sửa lỗi vị trí mắt phải
        gl.glColor3f(0f, 1f, 0f);
        glut.glutSolidSphere(0.2, 20, 20);
        gl.glPopMatrix();

        gl.glPopMatrix(); // Kết thúc dịch chuyển vật thể
    }

    private void drawFloor(GL2 gl) {
        if (floorTexture != null) {
            gl.glEnable(GL2.GL_TEXTURE_2D);
            floorTexture.enable(gl);
            floorTexture.bind(gl);
        }

        gl.glPushMatrix();
        gl.glBegin(GL2.GL_QUADS);
        gl.glColor3f(1f, 1f, 1f);
        gl.glNormal3f(0, 1, 0);
        for (int x = -5; x < 5; x++) {
            for (int z = -5; z < 5; z++) {
                gl.glTexCoord2f(0, 0); gl.glVertex3f(x, 0, z);
                gl.glTexCoord2f(1, 0); gl.glVertex3f(x + 1, 0, z);
                gl.glTexCoord2f(1, 1); gl.glVertex3f(x + 1, 0, z + 1);
                gl.glTexCoord2f(0, 1); gl.glVertex3f(x, 0, z + 1);
            }
        }
        gl.glEnd();
        gl.glPopMatrix();

        if (floorTexture != null) {
            floorTexture.disable(gl);
            gl.glDisable(GL2.GL_TEXTURE_2D);
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        if (height <= 0) height = 1;
        float aspect = (float) width / height;
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        new GLU().gluPerspective(45.0, aspect, 1.0, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    // Xử lý bàn phím
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_DOWN:  posX -= 0.2f; break;  // Sang trái (trục X giảm)
            case KeyEvent.VK_UP: posX += 0.2f; break;  // Sang phải (trục X tăng)
            case KeyEvent.VK_RIGHT: posZ -= 0.2f; break;  // Ra trước (trục Z giảm)
            case KeyEvent.VK_LEFT:  posZ += 0.2f; break;  // Ra sau (trục Z tăng)
            case KeyEvent.VK_SPACE: // Nhấn phím cách để nhảy
                if (isOnGround) { // Chỉ nhảy khi đang chạm đất
                    velocityY = JUMP_VELOCITY;
                    isOnGround = false;
                }
                break;
            case KeyEvent.VK_1: // Nhảy nhẹ về trước (Z giảm)
                if (isOnGround) {
                    velocityY = SMALL_JUMP_VELOCITY;
                    posZ -= SMALL_MOVE_DISTANCE;
                    isOnGround = false;
                }
                break;
            case KeyEvent.VK_2: // Nhảy nhẹ về sau (Z tăng)
                if (isOnGround) {
                    velocityY = SMALL_JUMP_VELOCITY;
                    posZ += SMALL_MOVE_DISTANCE;
                    isOnGround = false;
                }
                break;
            case KeyEvent.VK_3: // Nhảy nhẹ sang phải (X tăng)
                if (isOnGround) {
                    velocityY = SMALL_JUMP_VELOCITY;
                    posX += SMALL_MOVE_DISTANCE;
                    isOnGround = false;
                }
                break;
            case KeyEvent.VK_4: // Nhảy nhẹ sang trái (X giảm)
                if (isOnGround) {
                    velocityY = SMALL_JUMP_VELOCITY;
                    posX -= SMALL_MOVE_DISTANCE;
                    isOnGround = false;
                }
                break;
        }
        repaint();
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Funny Object JOGL - Move with Arrows, Jump with Space, Small Jumps with 1-4");
        Shape panel = new Shape();
        frame.getContentPane().add(panel);
        frame.setSize(600, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Yêu cầu panel nhận focus để nhận bàn phím
        panel.requestFocusInWindow();

        FPSAnimator animator = new FPSAnimator(panel, 60);
        animator.start();
    }
}
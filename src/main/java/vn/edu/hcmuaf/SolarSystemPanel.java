package vn.edu.hcmuaf;


import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;

import java.awt.event.*;

public class SolarSystemPanel extends GLJPanel implements GLEventListener, MouseMotionListener, MouseListener, MouseWheelListener {

    private float earthOrbitAngle = 0f;
    private float venusOrbitAngle = 0f;
    private float marsOrbitAngle = 0f;

    private float satureOrbitAngle = 0f;
    private float earthRotation = 0f;
    private float sceneRotX = 20f, sceneRotY = 0f;
    private float cameraDistance = 15f;

    private GLU glu = new GLU();

    private String hoveredPlanet = "";
    private String selectedPlanet = "";

    private Texture sunTexture, earthTexture, moonTexture ,venusTexture,marsTexture,
            mercuryTexture, jupiterTexture, saturnTexture, uranusTexture, neptuneTexture;

    private int lastMouseX, lastMouseY;
    private static final int STAR_COUNT = 500; // Số lượng sao
    private float[] starPositions = new float[STAR_COUNT * 3]; // mỗi sao 3 float x,y,z
    private int numSegments = 100; // Number of segments for approximation

    public SolarSystemPanel(GLCapabilities caps) {
        super(caps);
        this.addGLEventListener(this);
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.addMouseWheelListener(this);
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glClearColor(0f, 0f, 0f, 1f);

        try {
            earthTexture = TextureLoader.loadTexture(gl, "/textures/earth.jpg");
            sunTexture = TextureLoader.loadTexture(gl, "/textures/sun.jpg");
            moonTexture = TextureLoader.loadTexture(gl, "/textures/moon.jpg");
            venusTexture = TextureLoader.loadTexture(gl, "/textures/venus.jpg");
            marsTexture = TextureLoader.loadTexture(gl, "/textures/mars.jpg");
            saturnTexture = TextureLoader.loadTexture(gl, "/textures/saturn.jpg");
        }catch (Exception e) {
            System.out.println("Error loading textures: " + e.getMessage());
        }
        generateStarPositions();
        generateSaturnRing(2f, 3000); // Tạo vành đai Sao Thổ
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL2 gl = drawable.getGL().getGL2();
        if (h <= 0) h = 1;
        float aspect = (float) w / h;

        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45, aspect, 1, 100);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();

        glu.gluLookAt(0, 0, cameraDistance, 0, 0, 0, 0, 1, 0);
//        setCameraOrbitSaturn(gl, glu);
//        float[] arr = getSaturnWorldPosition(gl);
//        System.out.println("Sao Thổ tọa độ thế giới: " + arr[0] + ", " + arr[1] + ", " + arr[2]);
//        gl.glTranslatef(-arr[0], -arr[1], -arr[2]); // Dịch tâm sao Thổ về gốc

        gl.glRotatef(sceneRotX, 1, 0, 0);
        gl.glRotatef(sceneRotY, 0, 1, 0);

//        gl.glTranslatef(arr[0], arr[1], arr[2]); // Dịch tâm sao Thổ về gốc


        // Vẽ các ngôi sao cố định
        drawFixedStars(gl);

        // Vẽ trục XYZ
//        gl.glDisable(GL2.GL_LIGHTING);
        drawAxes(gl, 100f); // độ dài trục = 100f
//        gl.glEnable(GL2.GL_LIGHTING);
        // Vẽ Mặt Trời
        drawSun(gl);

        // Trái Đất
        drawEarth(gl);

        // Sao Kim
        drawVenus(gl);

        // Sao Hỏa
        drawMars(gl);

        // Sao Thổ
        drawSature(gl);

        // Update animation
        earthOrbitAngle += 0.01f;
        earthRotation += 0.2f;
        venusOrbitAngle -= 0.013f; // Tốc độ quay của Sao Kim
        marsOrbitAngle += 0.005f; // Tốc độ quay của Sao Hỏa
        satureOrbitAngle += 0.003f; // Tốc độ quay của Sao Thổ
    }

    private void drawEarth(GL2 gl) {

        // Quỹ đạo trái đất ( quay quanh trục y của Mặt Trời, nằm trên trục xz )
        gl.glEnable(GL2.GL_LINE_STIPPLE);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(1f, 1f, 1f);
        gl.glLineStipple(1, (short) 0xF000);
        gl.glLineWidth(0.1f);

        float radius = 8f;
        float centerX = 0.0f;
        float centerY = 0.0f;

        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < numSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / numSegments);

            float x = centerX + radius * (float) Math.cos(angle);
            float y = 0.0f;
            float z = centerY + radius * (float) Math.sin(angle);
            gl.glVertex3f(x, y, z);
        }
        gl.glEnd();
        gl.glDisable(GL2.GL_LINE_STIPPLE);
        gl.glLineWidth(1f);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glPushMatrix();
        // Quay quanh Mặt Trời
        gl.glRotatef(earthOrbitAngle, 0, 1, 0);
        gl.glTranslatef(radius, 0, 0);

        // Tự quay quanh trục
        // xoay trái đất 90 độ lên trên và +23 độ lệch tự quay quanh trục so với quỹ đạo quay quanh mặt trời
        gl.glRotatef(90 + 23, 1, 0, 0);
        gl.glRotatef(earthRotation, 0, 0, 1);


        // Vẽ trục Bắc - Nam
        gl.glLineWidth(1f);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(0.3f, 1.0f, 0.7f);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex3f(0f, 0f, -100f);
        gl.glVertex3f(0f, 0f, 100f);
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);

        // Đặt vật liệu
        float[] matAmb = {0.5f, 0.5f, 0.5f, 1f};
        float[] matDiff = {1f, 1f, 1f, 1f};
        float[] matSpec = {1f, 1f, 1f, 1f};
        float shininess = 50f;

        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, matAmb, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, matDiff, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, matSpec, 0);
        gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, shininess);
        // Vẽ quả cầu với texture trái đất
        drawTexturedSphere(gl, earthTexture, 0.8f, 30, 30);

        gl.glDisable(GL2.GL_LIGHTING);
        // Vẽ quỹ đạo Mặt Trăng quanh Trái Đất
        float moonOrbitRadius = 1.5f;
        gl.glColor3f(1f, 1f, 1f);
        // net dut
        gl.glEnable(GL2.GL_LINE_STIPPLE);
        gl.glLineStipple(1, (short) 0x00FF);
        gl.glLineWidth(0.5f);

        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < numSegments; i++) {
            double angle = 2.0 * Math.PI * i / numSegments;
            float x = moonOrbitRadius * (float) Math.cos(angle);
            float y = moonOrbitRadius * (float) Math.sin(angle);
            float z = 0f;
            gl.glVertex3f(x, y, z);
        }
        gl.glEnd();
        gl.glDisable(GL2.GL_LINE_STIPPLE);
        gl.glEnable(GL2.GL_LIGHTING);

        // vẽ mặt trăng
        drawMoon(gl);

        gl.glPopMatrix();
    }

    private void drawSun(GL2 gl) {
        gl.glPushMatrix();
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glColor3f(1f, 1f, 1f);
        drawTexturedSphere(gl, sunTexture, 1.5f, 30, 30);
        gl.glPopMatrix();
        // Bật ánh sáng
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_NORMALIZE);

        // Định nghĩa ánh sáng Mặt Trời
        float[] lightPos = {0f, 0f, 0f, 1f};
        float[] lightAmb = {0.5f, 0.5f, 0.5f, 1f};
        float[] lightDiff = {1f, 1f, 1f, 1f};
        float[] lightSpec = {1f, 1f, 1f, 1f};

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiff, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpec, 0);


    }

    private void drawVenus(GL2 gl) {


        // Vẽ quỹ đạo Sao Kim (ellipse)
        gl.glDisable(GL2.GL_LIGHTING); // không bị ảnh hưởng bởi ánh sáng
        gl.glEnable(GL2.GL_LINE_STIPPLE);
        gl.glColor3f(1f, 1f, 1f);
        gl.glLineStipple(1, (short) 0xF000);
        gl.glLineWidth(1.0f);


        gl.glRotatef(3.39f, 1f, 0f, 0f); // nghiêng 10° quanh trục X

        gl.glBegin(GL.GL_LINE_LOOP);
        float semiMajorAxis = 5.0f; // bán trục lớn (X)
        float semiMinorAxis = 4.5f; // bán trục nhỏ (Z)
        float c = (float) Math.sqrt(semiMajorAxis * semiMajorAxis - semiMinorAxis * semiMinorAxis);

        int numSegments = 200; // số đoạn

        for (int i = 0; i < numSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / numSegments);
            float x = semiMajorAxis * (float) Math.cos(angle) - c + 1.0f ;
            float y = 0.0f; // quỹ đạo trên mặt phẳng XZ
            float z = semiMinorAxis * (float) Math.sin(angle);
            gl.glVertex3f(x, y, z);
        }
        gl.glEnd();
        gl.glDisable(GL2.GL_LINE_STIPPLE);

        gl.glEnable(GL2.GL_LIGHTING); // bật lại ánh sáng
        // Vẽ Sao Kim
        float x = semiMajorAxis * (float) Math.cos(Math.toRadians(venusOrbitAngle)) - c + 1.0f;
        float y = 0.0f;
        float z = semiMinorAxis * (float) Math.sin(Math.toRadians(venusOrbitAngle));

        gl.glPushMatrix();
        gl.glTranslatef(x, y, z); // dịch tới vị trí quỹ đạo
        gl.glRotatef(venusOrbitAngle * 5.0f, 0, 1, 0); // xoay quanh trục riêng (tùy tốc độ)
        drawTexturedSphere(gl, venusTexture, 0.8f, 30, 30);
        gl.glPopMatrix();
    }

    // Vẽ sao Hỏa
    private void drawMars(GL2 gl) {
        float radius =15f; // Bán kính quỹ đạo Sao Hỏa

        // Vẽ quỹ đạo Sao Hỏa
        gl.glDisable(GL2.GL_LIGHTING); // không bị ảnh hưởng bởi ánh sáng
        gl.glEnable(GL2.GL_LINE_STIPPLE);
        gl.glColor3f(1f, 1f, 1f);
        gl.glLineStipple(1, (short) 0xF000);
        gl.glLineWidth(1.0f);
        gl.glRotatef(1.85f, 1f, 0f, 0f); // nghiêng 10° quanh trục X
        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < numSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / numSegments);
            float x = radius * (float) Math.cos(angle); // Bán kính quỹ đạo Sao Hỏa
            float y = 0.0f; // Quỹ đạo trên mặt phẳng XZ
            float z = radius * (float) Math.sin(angle);
            gl.glVertex3f(x, y, z);
        }
        gl.glEnd();
        gl.glDisable(GL2.GL_LINE_STIPPLE);
        gl.glEnable(GL2.GL_LIGHTING); // bật lại ánh sáng


        // Vẽ Sao Hỏa
        gl.glPushMatrix();
        // Quay quanh Mặt Trời
        gl.glRotatef(marsOrbitAngle * 1.88f, 0, 1, 0); // Mặt Trời quay quanh Mặt Trời
        gl.glTranslatef(radius, 0, 0); // Khoảng cách từ Mặt Trời đến Sao Hỏa
        gl.glRotatef(-earthRotation* 0.5f, 0, 1, 0); // Tốc độ quay của Sao Hỏa
        gl.glRotatef(90 , 1, 0.5f, 0);
        drawTexturedSphere(gl, marsTexture, 2f, 30, 30);


        // Quay quanh trục của Sao Hỏa

        gl.glPopMatrix();
    }
    private void drawSature(GL2 gl){
        float radius = 50f; // Bán kính quỹ đạo Sao Thổ
        float saturnRadius = 2f; // Bán kính Sao Thổ
        // Vẽ quỹ đạo Sao Thổ
        gl.glDisable(GL2.GL_LIGHTING); // không bị ảnh hưởng bởi ánh sáng
        gl.glEnable(GL2.GL_LINE_STIPPLE);
        gl.glColor3f(1f, 1f, 1f);
        gl.glLineStipple(1, (short) 0xF000);
        gl.glLineWidth(1.0f);
        gl.glRotatef(26.73f, 1f, 0f, 0f); // nghiêng 26.73° quanh trục X
        gl.glBegin(GL.GL_LINE_LOOP);
        for (int i = 0; i < numSegments; i++) {
            float angle = (float) (2.0 * Math.PI * i / numSegments);
            float x = radius * (float) Math.cos(angle); // Bán kính quỹ đạo Sao Thổ
            float y = 0.0f; // Quỹ đạo trên mặt phẳng XZ
            float z = radius * (float) Math.sin(angle);
            gl.glVertex3f(x, y, z);
        }
        gl.glEnd();
        gl.glDisable(GL2.GL_LINE_STIPPLE);
        gl.glEnable(GL2.GL_LIGHTING); // bật lại ánh sáng

        // Vẽ Sao Thổ
        gl.glPushMatrix();
        // Quay quanh Mặt Trời
        gl.glRotatef(satureOrbitAngle * 0.34f, 0, 1, 0); // Mặt Trời quay quanh Mặt Trời
        gl.glTranslatef(radius, 0, 0); // Khoảng cách từ Mặt Trời đến Sao Thổ
        gl.glRotatef(-earthRotation * 0.5f, 0, 1, 0); // Tốc độ quay của Sao Thổ
        gl.glRotatef(90, 1, 0.5f, 0);
        drawTexturedSphere(gl, saturnTexture, saturnRadius, 30, 30);
        // vành đai sao thổ
        drawSaturnRing(gl); // Vẽ vành đai Sao Thổ

        gl.glPopMatrix();

    }

    private float cameraAngle = 0;

    private void setCameraOrbitSaturn(GL2 gl, GLU glu) {
        // Lấy tọa độ tâm Sao Thổ (hàm này bạn cần có)
        float[] saturnPos = getSaturnWorldPosition(gl);

        float camX = saturnPos[0] + (float) Math.cos(Math.toRadians(cameraAngle)) * cameraDistance;
        float camZ = saturnPos[2] + (float) Math.sin(Math.toRadians(cameraAngle)) * cameraDistance;
        float camY = saturnPos[1] + 3.0f;

        glu.gluLookAt(
                camX, camY, camZ,
                saturnPos[0], saturnPos[1], saturnPos[2],
                0.0f, 1.0f, 0.0f
        );

        cameraAngle += 0.01f; // điều chỉnh tốc độ quay
    }

    private float[] getSaturnWorldPosition(GL2 gl) {
        // Lưu lại ma trận hiện tại
        gl.glPushMatrix();

        // Giống hệt các phép biến đổi khi vẽ Sao Thổ
        gl.glRotatef(26.73f, 1f, 0f, 0f); // nghiêng quỹ đạo
        gl.glRotatef(satureOrbitAngle * 0.34f, 0, 1, 0); // quay quanh Mặt Trời
        gl.glTranslatef(50f, 0, 0); // bán kính quỹ đạo

        // Lấy ma trận MODELVIEW
        float[] modelview = new float[16];
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);

        // Giải nén vị trí từ ma trận (cột 4 của ma trận)
        float worldX = modelview[12];
        float worldY = modelview[13];
        float worldZ = modelview[14];

        // Khôi phục ma trận ban đầu
        gl.glPopMatrix();

        return new float[]{worldX, worldY, worldZ};
    }

    private float[][] saturnRingPoints;


    private void generateSaturnRing(float saturnRadius, int numPoints) {
        saturnRingPoints = new float[numPoints][3];

        float innerRadius = saturnRadius + 1.0f;
        float outerRadius = saturnRadius + 3.0f;

        for (int i = 0; i < numPoints; i++) {
            double angle = Math.random() * 2.0 * Math.PI;
            double r = innerRadius + Math.random() * (outerRadius - innerRadius);

            saturnRingPoints[i][0] = (float) (r * Math.cos(angle)); // X
            saturnRingPoints[i][1] = 0.0f;                          // Y
            saturnRingPoints[i][2] = (float) (r * Math.sin(angle)); // Z

        }
    }
    private void drawSaturnRing(GL2 gl) {
//        gl.glDisable(GL2.GL_LIGHTING);
        gl.glPointSize(2.0f);
        gl.glRotatef(-90, 1, 0, 0); // Quay vành đai sao thổ nằm ngang
        gl.glBegin(GL.GL_POINTS);
        for (int i = 0; i < saturnRingPoints.length; i++) {
            gl.glVertex3fv(saturnRingPoints[i], 0);
        }
        gl.glEnd();

//        gl.glEnable(GL2.GL_LIGHTING);
    }
    private void drawMoon(GL2 gl) {
        gl.glPushMatrix();
        // Quay quanh Trái Đất
        gl.glRotatef(earthOrbitAngle * 13.4f, 0, 0, 1); // Mặt Trăng quay quanh Trái Đất
        gl.glTranslatef(1.5f, 0, 0); // Khoảng cách từ Trái Đất đến Mặt Trăng
        drawTexturedSphere(gl, moonTexture, 0.2f, 30, 30);
        gl.glPopMatrix();

    }

    private void generateStarPositions() {
        for (int i = 0; i < STAR_COUNT; i++) {
            // Tạo tọa độ trong một khối không gian lớn, ví dụ -50 đến +50
            starPositions[i * 3] = (float) (Math.random() * 100 - 50);     // x
            starPositions[i * 3 + 1] = (float) (Math.random() * 100 - 50); // y
            starPositions[i * 3 + 2] = (float) (Math.random() * 100 - 50); // z
        }
    }

    private void drawFixedStars(GL2 gl) {
        gl.glDisable(GL2.GL_TEXTURE_2D);
        gl.glPointSize(2f);
        gl.glBegin(GL2.GL_POINTS);
        gl.glColor3f(1f, 1f, 1f);
        for (int i = 0; i < STAR_COUNT; i++) {
            gl.glVertex3f(starPositions[i * 3], starPositions[i * 3 + 1], starPositions[i * 3 + 2]);
        }
        gl.glEnd();
        gl.glEnable(GL2.GL_TEXTURE_2D);
    }

    private void drawAxes(GL2 gl, float length) {
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glBegin(GL2.GL_LINES);
        // Trục X (Đỏ)
        gl.glColor3f(0.7f, 0.2f, 0.2f);
        gl.glVertex3f(length, 0, 0);
        gl.glVertex3f(-length, 0, 0);
        // Trục Y (Xanh lá)
        gl.glColor3f(0.2f, 0.7f, 0.2f);
        gl.glVertex3f(0, length, 0);
        gl.glVertex3f(0, -length, 0);
        // Trục Z (Xanh dương)
        gl.glColor3f(0.2f, 0.2f, 0.7f);
        gl.glVertex3f(0, 0, length);
        gl.glVertex3f(0, 0, -length);
        gl.glEnd();
        gl.glEnable(GL2.GL_LIGHTING);
    }


    private void drawTexturedSphere(GL2 gl, Texture texture, float radius, int slices, int stacks) {
        if (texture != null) {
            texture.enable(gl);
            texture.bind(gl);
        }
        var sphere = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
        glu.gluQuadricTexture(sphere, true); // Quan trọng: bật texture mapping
        glu.gluQuadricNormals(sphere, GLU.GLU_SMOOTH);
        glu.gluSphere(sphere, radius, slices, stacks);
        glu.gluDeleteQuadric(sphere);
        if (texture != null) {
            texture.disable(gl);
        }
    }

    private void drawSphere(GL2 gl, float radius, int slices, int stacks) {
        var sphere = glu.gluNewQuadric();
        glu.gluQuadricDrawStyle(sphere, GLU.GLU_FILL);
        glu.gluQuadricNormals(sphere, GLU.GLU_SMOOTH);
        glu.gluSphere(sphere, radius, slices, stacks);
        glu.gluDeleteQuadric(sphere);
    }

    /**
     * ================== Mouse Handling ==================
     **/

    @Override
    public void mouseMoved(MouseEvent e) {
        // Tính vị trí chuột → xác định hành tinh gần nhất
        float mouseX = (float) e.getX();
        float mouseY = (float) e.getY();

        // Giả sử chỉ hover 2 hành tinh: Sun (0,0,0) và Earth (5,0,0)
        hoveredPlanet = ""; // reset

        // Tính khoảng cách tới Sun
        if (distanceToPlanet(mouseX, mouseY, 0, 0, 0) < 80) {
            hoveredPlanet = "Sun";
        }
        // Khoảng cách tới Earth (Earth đang quay)
        double earthX = Math.cos(Math.toRadians(earthOrbitAngle)) * 5;
        double earthZ = Math.sin(Math.toRadians(earthOrbitAngle)) * 5;
        if (distanceToPlanet(mouseX, mouseY, earthX, 0, earthZ) < 80) {
            hoveredPlanet = "Earth";
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - lastMouseX;
        int dy = e.getY() - lastMouseY;

        float sensitivity = 0.5f; // độ nhạy xoay, bạn điều chỉnh tùy ý

        // Xoay quanh trục Y theo chiều ngang (kéo chuột qua trái/phải)
        sceneRotY += dx * sensitivity;

        // Xoay quanh trục X theo chiều dọc (kéo chuột lên/xuống)
        // Đảo chiều dy để kéo lên là góc tăng (xoay lên)
        sceneRotX += dy * sensitivity;

        // Cập nhật lại vị trí chuột cuối cùng
        lastMouseX = e.getX();
        lastMouseY = e.getY();

        // Giới hạn góc xoay quanh X (để tránh lật quá)
        sceneRotX = Math.max(-90f, Math.min(90f, sceneRotX));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!hoveredPlanet.isEmpty()) {
            selectedPlanet = hoveredPlanet;
            if (selectedPlanet.equals("Sun")) cameraDistance = 8f;
            else if (selectedPlanet.equals("Earth")) cameraDistance = 6f;
        }
    }

    /**
     * Khoảng cách 2D giả định giữa chuột và hành tinh (dùng cho hover đơn giản)
     **/
    private double distanceToPlanet(float mouseX, float mouseY, double px, double py, double pz) {
        double dx = px - 0; // đơn giản hóa, chưa convert từ 3D sang 2D
        double dz = pz - 0;
        return Math.sqrt(dx * dx + dz * dz) * 50; // scale tạm
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMouseX = e.getX();
        lastMouseY = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // Thu phóng camera khi cuộn chuột
        int notches = e.getWheelRotation();
        cameraDistance += notches * 1f; // điều chỉnh độ nhạy thu phóng
        cameraDistance = Math.max(5f, Math.min(50f, cameraDistance)); // giới hạn khoảng cách
    }
}
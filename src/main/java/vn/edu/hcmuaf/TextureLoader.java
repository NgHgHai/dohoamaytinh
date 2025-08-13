package vn.edu.hcmuaf;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import java.io.IOException;
import java.io.InputStream;

public class TextureLoader {
    public static Texture loadTexture(GL2 gl, String path) {
        try (InputStream stream = TextureLoader.class.getResourceAsStream(path)) {
            if (stream == null) {
                System.err.println("Không tìm thấy file: " + path);
                return null;
            }
            return TextureIO.newTexture(stream, true, TextureIO.JPG);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

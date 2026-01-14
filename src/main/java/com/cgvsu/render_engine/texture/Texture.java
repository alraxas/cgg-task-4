package com.cgvsu.render_engine.texture;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import java.util.HashMap;
import java.util.Map;

public class Texture {
    private final Image image;
    private final PixelReader pixelReader;
    private final int width;
    private final int height;

    private static final Map<String, Texture> loadedTextures = new HashMap<>();

    public Texture(Image image) {
        this.image = image;
        this.pixelReader = image.getPixelReader();
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();
    }

    public static Texture loadTexture(String path) {
        if (loadedTextures.containsKey(path)) {
            return loadedTextures.get(path);
        }

        try {
            Image image = new Image("file:" + path);
            Texture texture = new Texture(image);
            loadedTextures.put(path, texture);
            return texture;
        } catch (Exception e) {
            System.err.println("Failed to load texture: " + path);
            return null;
        }
    }

    public javafx.scene.paint.Color getColor(float u, float v) {
        // Обработка координат текстуры (wrap или clamp)
        u = u - (float) Math.floor(u); // Wrap
        v = v - (float) Math.floor(v); // Wrap

        // Преобразование в координаты пикселей
        int x = (int) (u * (width - 1));
        int y = (int) ((1 - v) * (height - 1)); // Инвертируем V координату

        // Ограничиваем координаты
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        return pixelReader.getColor(x, y);
    }

    public javafx.scene.paint.Color getColorBilinear(float u, float v) {
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        float x = u * (width - 1);
        float y = (1 - v) * (height - 1);

        int x1 = (int) Math.floor(x);
        int y1 = (int) Math.floor(y);
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);

        float dx = x - x1;
        float dy = y - y1;

        javafx.scene.paint.Color c00 = pixelReader.getColor(x1, y1);
        javafx.scene.paint.Color c10 = pixelReader.getColor(x2, y1);
        javafx.scene.paint.Color c01 = pixelReader.getColor(x1, y2);
        javafx.scene.paint.Color c11 = pixelReader.getColor(x2, y2);

        // Билинейная интерполяция
        return bilinearInterpolate(c00, c10, c01, c11, dx, dy);
    }

    private javafx.scene.paint.Color bilinearInterpolate(
            javafx.scene.paint.Color c00, javafx.scene.paint.Color c10,
            javafx.scene.paint.Color c01, javafx.scene.paint.Color c11,
            float dx, float dy) {

        double r00 = c00.getRed();
        double g00 = c00.getGreen();
        double b00 = c00.getBlue();

        double r10 = c10.getRed();
        double g10 = c10.getGreen();
        double b10 = c10.getBlue();

        double r01 = c01.getRed();
        double g01 = c01.getGreen();
        double b01 = c01.getBlue();

        double r11 = c11.getRed();
        double g11 = c11.getGreen();
        double b11 = c11.getBlue();

        // Интерполяция по X
        double r0 = r00 * (1 - dx) + r10 * dx;
        double g0 = g00 * (1 - dx) + g10 * dx;
        double b0 = b00 * (1 - dx) + b10 * dx;

        double r1 = r01 * (1 - dx) + r11 * dx;
        double g1 = g01 * (1 - dx) + g11 * dx;
        double b1 = b01 * (1 - dx) + b11 * dx;

        // Интерполяция по Y
        double r = r0 * (1 - dy) + r1 * dy;
        double g = g0 * (1 - dy) + g1 * dy;
        double b = b0 * (1 - dy) + b1 * dy;

        return new javafx.scene.paint.Color(r, g, b, 1.0);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getImage() { return image; }
}
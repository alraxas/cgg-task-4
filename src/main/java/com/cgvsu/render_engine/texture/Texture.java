package com.cgvsu.render_engine.texture;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class Texture {
    private final Image image;
    private final PixelReader pixelReader;
    private final int width;
    private final int height;

    public Texture(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        this.image = image;
        this.pixelReader = image.getPixelReader();
        this.width = (int) image.getWidth();
        this.height = (int) image.getHeight();

        if (pixelReader == null) {
            throw new IllegalStateException("Cannot get PixelReader from image");
        }
    }

    public Texture(String path) {
        this(new Image("file:" + path));
    }

    public Color getColor(float u, float v) {
        return getColor(u, v, false);
    }

    // Метод с выбором фильтрации
    public Color getColor(float u, float v, boolean useBilinear) {
        if (useBilinear) {
            return getColorBilinear(u, v);
        }
        return getColorNearest(u, v);
    }

    // Метод ближайшего соседа (быстрее)
    private Color getColorNearest(float u, float v) {
        // Обработка координат (wrap mode)
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        // Конвертируем в координаты пикселей
        int x = Math.min((int) (u * width), width - 1);
        int y = Math.min((int) ((1 - v) * height), height - 1);

        // Ограничение на случай ошибок округления
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        return pixelReader.getColor(x, y);
    }

    // Билинейная фильтрация
    public Color getColorBilinear(float u, float v) {
        // Wrap coordinates
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        // Convert to pixel coordinates with sub-pixel precision
        float x = u * (width - 1);
        float y = (1 - v) * (height - 1);

        // Get four surrounding pixels
        int x1 = (int) Math.floor(x);
        int y1 = (int) Math.floor(y);
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);

        // Calculate interpolation factors
        float dx = x - x1;
        float dy = y - y1;
        float dx1 = 1 - dx;
        float dy1 = 1 - dy;

        // Get colors of four pixels
        Color c00 = pixelReader.getColor(x1, y1);
        Color c10 = pixelReader.getColor(x2, y1);
        Color c01 = pixelReader.getColor(x1, y2);
        Color c11 = pixelReader.getColor(x2, y2);

        // Bilinear interpolation
        double r = c00.getRed() * dx1 * dy1 +
                c10.getRed() * dx * dy1 +
                c01.getRed() * dx1 * dy +
                c11.getRed() * dx * dy;

        double g = c00.getGreen() * dx1 * dy1 +
                c10.getGreen() * dx * dy1 +
                c01.getGreen() * dx1 * dy +
                c11.getGreen() * dx * dy;

        double b = c00.getBlue() * dx1 * dy1 +
                c10.getBlue() * dx * dy1 +
                c01.getBlue() * dx1 * dy +
                c11.getBlue() * dx * dy;

        // Alpha (if needed)
        double a = c00.getOpacity();

        return new Color(
                Math.max(0, Math.min(1, r)),
                Math.max(0, Math.min(1, g)),
                Math.max(0, Math.min(1, b)),
                a
        );
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getImage() { return image; }
    public PixelReader getPixelReader() { return pixelReader; }

    public boolean isValid() {
        return image != null && !image.isError() && pixelReader != null;
    }
}
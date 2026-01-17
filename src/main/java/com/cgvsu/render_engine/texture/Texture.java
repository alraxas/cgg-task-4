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

    public Color getColor(int x, int y) {
        if (pixelReader == null) {
            return null;
        }


        x = Math.max(0, Math.min(width - 1, x));
//        x = width - x - 1;
        y = Math.max(0, Math.min(height - 1, y));

        try {
            return pixelReader.getColor(x, y);
        } catch (Exception e) {
            System.err.println("Ошибка чтения цвета текстуры: " + x + ", " + y);
            return null;
        }
    }

    public Color getColor(float u, float v) {
        return getColor(u, v, false);
    }

    public Color getColor(float u, float v, boolean useBilinear) {
        if (useBilinear) {
            return getColorBilinear(u, v);
        }
        return getColorNearest(u, v);
    }


    private Color getColorNearest(float u, float v) {
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

        int x = Math.min((int) ((1 - u) * width), width - 1);
//        int x = Math.min((int) (u * width), width - 1);
        int y = Math.min((int) ((1 - v) * height), height - 1);
//        int y = Math.min((int) ((v) * height), height - 1);


        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        return pixelReader.getColor(x, y);
    }

    public Color getColorBilinear(float u, float v) {
        u = u - (float) Math.floor(u);
        v = v - (float) Math.floor(v);

//        float x = u * (width - 1);
        float x = (1 - u) * (width - 1);
        float y = (1 - v) * (height - 1);
//        float y = (v) * (height - 1);

        int x1 = (int) Math.floor(x);
        int y1 = (int) Math.floor(y);
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);

        float dx = x - x1;
        float dy = y - y1;
        float dx1 = 1 - dx;
        float dy1 = 1 - dy;

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

        // Alpha
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
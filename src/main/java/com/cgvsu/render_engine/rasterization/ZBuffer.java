package com.cgvsu.render_engine.rasterization;

public class ZBuffer {
    private final double[][] buffer;
    private final int width;
    private final int height;

    public ZBuffer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new double[width][height];
        clear();
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                buffer[x][y] = Float.MAX_VALUE;
            }
        }
    }

    public boolean testAndSet(int x, int y, double depth) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return false;
        }

        if (depth < buffer[x][y]) {
            buffer[x][y] = depth;
            return true;
        }

        return false;
    }

    public double getDepth(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Double.MAX_VALUE;
        }
        return buffer[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
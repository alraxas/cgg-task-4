package com.cgvsu.render_engine.texture;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static TextureManager instance;
    private final Map<String, Texture> textures;
    private final Map<String, Image> images;

    private TextureManager() {
        textures = new HashMap<>();
        images = new HashMap<>();
    }

    public static synchronized TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    public Texture loadTexture(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Texture path cannot be null or empty");
        }

        if (textures.containsKey(path)) {
            return textures.get(path);
        }

        try {
            Image image;
            if (images.containsKey(path)) {
                image = images.get(path);
            } else {
                image = new Image("file:" + path);
                if (image.isError()) {
                    throw new RuntimeException("Failed to load image: " + path);
                }
                images.put(path, image);
            }

            Texture texture = new Texture(image);
            textures.put(path, texture);

            return texture;

        } catch (Exception e) {
            System.err.println("Error loading texture '" + path + "': " + e.getMessage());
            return createFallbackTexture();
        }
    }

    public Texture getTexture(String path) {
        return textures.get(path);
    }

    public Texture loadTexture(String path, boolean useBilinearDefault) {
        Texture texture = loadTexture(path);
        // Можно добавить флаг для текстуры, если нужно
        return texture;
    }

    private Texture createFallbackTexture() {
        int size = 64;
        javafx.scene.canvas.Canvas canvas = new javafx.scene.canvas.Canvas(size, size);
        javafx.scene.canvas.GraphicsContext gc = canvas.getGraphicsContext2D();

        // Рисуем шахматную доску
        int tileSize = size / 8;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if ((x + y) % 2 == 0) {
                    gc.setFill(javafx.scene.paint.Color.MAGENTA);
                } else {
                    gc.setFill(javafx.scene.paint.Color.BLACK);
                }
                gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        // Конвертируем Canvas в Image
        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        Image image = canvas.snapshot(params, null);

        return new Texture(image);
    }

    public void unloadTexture(String path) {
        textures.remove(path);
        images.remove(path);
    }

    public void clearAll() {
        textures.clear();
        images.clear();
    }

    public java.util.Set<String> getLoadedTextures() {
        return textures.keySet();
    }

    public boolean isTextureLoaded(String path) {
        return textures.containsKey(path);
    }

    public void preloadTextures(String... paths) {
        for (String path : paths) {
            if (!isTextureLoaded(path)) {
                loadTexture(path);
            }
        }
    }
}
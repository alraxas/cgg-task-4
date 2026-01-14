package com.cgvsu.render_engine.texture;

import java.util.HashMap;
import java.util.Map;

public class TextureManager {
    private static final TextureManager instance = new TextureManager();
    private final Map<String, Texture> textures;

    private TextureManager() {
        textures = new HashMap<>();
    }

    public static TextureManager getInstance() {
        return instance;
    }

    public Texture loadTexture(String path) {
        if (textures.containsKey(path)) {
            return textures.get(path);
        }

        Texture texture = Texture.loadTexture(path);
        if (texture != null) {
            textures.put(path, texture);
        }
        return texture;
    }

    public Texture getTexture(String path) {
        return textures.get(path);
    }

    public void unloadTexture(String path) {
        textures.remove(path);
    }

    public void clearAll() {
        textures.clear();
    }
}
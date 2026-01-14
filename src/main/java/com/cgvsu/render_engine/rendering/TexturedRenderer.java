package com.cgvsu.render_engine.rendering;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.lighting.LightingModel;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import com.cgvsu.render_engine.texture.Texture;
import javafx.scene.canvas.GraphicsContext;

public class TexturedRenderer {
    private ZBuffer zBuffer;
    private Texture currentTexture;
    private LightingModel lightingModel;

    public void renderTextured(
            GraphicsContext graphicsContext,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Texture texture) {
        //TODO: Рендеринг с текстурой
    }

    public void renderTexturedWithLighting(
            GraphicsContext graphicsContext,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Texture texture) {
        //TODO: Рендеринг с текстурой и освещением
    }
}
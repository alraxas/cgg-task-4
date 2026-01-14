package com.cgvsu.render_engine.rendering;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.lighting.LightingModel;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class LightingRenderer {
    private ZBuffer zBuffer;
    private LightingModel lightingModel;

    public void renderWithLighting(
            GraphicsContext graphicsContext,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Color baseColor) {
        //TODO: Рендеринг с различными моделями освещения
    }
}
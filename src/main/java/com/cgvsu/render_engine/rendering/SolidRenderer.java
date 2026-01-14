package com.cgvsu.render_engine.rendering;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.rasterization.TriangleRasterizer;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class SolidRenderer {
    private ZBuffer zBuffer;
    private TriangleRasterizer rasterizer;

    public SolidRenderer() {
        this.rasterizer = new TriangleRasterizer();
    }

    public void renderSolid(
            GraphicsContext graphicsContext,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Color fillColor) {
        //TODO: Рендеринг сплошных полигонов одним цветом
    }
}
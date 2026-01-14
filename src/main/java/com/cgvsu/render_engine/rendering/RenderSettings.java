package com.cgvsu.render_engine.rendering;

import javafx.scene.paint.Color;

public class RenderSettings {
    private boolean drawWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;
    private Color solidColor = Color.LIGHTBLUE;

    public boolean isDrawWireframe() { return drawWireframe; }
    public void setDrawWireframe(boolean drawWireframe) { this.drawWireframe = drawWireframe; }

    public boolean isUseTexture() { return useTexture; }
    public void setUseTexture(boolean useTexture) { this.useTexture = useTexture; }

    public boolean isUseLighting() { return useLighting; }
    public void setUseLighting(boolean useLighting) { this.useLighting = useLighting; }

    public Color getSolidColor() { return solidColor; }
    public void setSolidColor(Color solidColor) { this.solidColor = solidColor; }

    public RenderMode getCurrentMode() {
        if (!drawWireframe && !useTexture && !useLighting) {
            return RenderMode.SOLID;
        } else if (drawWireframe && !useTexture && !useLighting) {
            return RenderMode.WIREFRAME_SOLID;
        } else if (!drawWireframe && useTexture && !useLighting) {
            return RenderMode.TEXTURED;
        } else if (!drawWireframe && !useTexture && useLighting) {
            return RenderMode.LIT;
        } else if (!drawWireframe && useTexture && useLighting) {
            return RenderMode.TEXTURED_LIT;
        } else if (drawWireframe && useTexture && useLighting) {
            return RenderMode.ALL;
        } else if (drawWireframe && !useTexture && useLighting) {
            return RenderMode.WIREFRAME; // Каркас + освещение
        }
        return RenderMode.SOLID;
    }
}
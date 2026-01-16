package com.cgvsu.render_engine.rendering;

import javafx.scene.paint.Color;

public class RenderSettings {
    private boolean drawWireframe = false;
    private boolean useTexture = false;
    private boolean useLighting = false;

    private Color solidColor = Color.LIGHTBLUE;
    private Color wireframeColor = Color.BLACK;
    private float wireframeThickness = 1.0f;
    private boolean useZBuffer = true;
    private boolean backfaceCulling = true;
    private boolean smoothShading = true;
    private boolean useBilinearFiltering = true;

    public boolean isDrawWireframe() { return drawWireframe; }
    public void setDrawWireframe(boolean drawWireframe) { this.drawWireframe = drawWireframe; }

    public boolean isUseTexture() { return useTexture; }
    public void setUseTexture(boolean useTexture) { this.useTexture = useTexture; }

    public boolean isUseLighting() { return useLighting; }
    public void setUseLighting(boolean useLighting) { this.useLighting = useLighting; }

    public Color getSolidColor() { return solidColor; }
    public void setSolidColor(Color solidColor) { this.solidColor = solidColor; }

    public Color getWireframeColor() {
        return wireframeColor;
    }
    public void setWireframeColor(Color wireframeColor) {
        this.wireframeColor = wireframeColor;
    }

    public float getWireframeThickness() {
        return wireframeThickness;
    }
    public void setWireframeThickness(float wireframeThickness) {
        this.wireframeThickness = wireframeThickness;
    }

    public boolean isUseZBuffer() {
        return useZBuffer;
    }
    public void setUseZBuffer(boolean useZBuffer) {
        this.useZBuffer = useZBuffer;
    }

    public boolean isBackfaceCulling() {
        return backfaceCulling;
    }
    public void setBackfaceCulling(boolean backfaceCulling) {
        this.backfaceCulling = backfaceCulling;
    }

    public boolean isSmoothShading() {
        return smoothShading;
    }
    public void setSmoothShading(boolean smoothShading) {
        this.smoothShading = smoothShading;
    }

    public boolean isUseBilinearFiltering() {
        return useBilinearFiltering;
    }
    public void setUseBilinearFiltering(boolean useBilinearFiltering) {
        this.useBilinearFiltering = useBilinearFiltering;
    }

    public RenderMode getCurrentMode() {
        if (!drawWireframe && !useTexture && !useLighting) {
            return RenderMode.SOLID;
        } else if (drawWireframe && !useTexture && !useLighting) {
            return RenderMode.WIREFRAME;
        } else if (!drawWireframe && useTexture && !useLighting) {
            return RenderMode.TEXTURED;
        } else if (!drawWireframe && !useTexture && useLighting) {
            return RenderMode.LIT_SOLID;
        } else if (!drawWireframe && useTexture && useLighting) {
            return RenderMode.LIT_TEXTURED;
        } else if (drawWireframe && !useTexture && useLighting) {
            return RenderMode.WIREFRAME_LIT_SOLID;
        } else if (drawWireframe && useTexture && !useLighting) {
            return RenderMode.WIREFRAME_TEXTURED;
        } else if (drawWireframe && useTexture && useLighting) {
            return RenderMode.ALL;
        }
        return RenderMode.SOLID;
    }

    public void validate() {
        if (useTexture && !useLighting) {
            // Для текстурирования без освещения можно использовать плоское затенение
            smoothShading = false;
        }
    }

    public RenderSettings copy() {
        RenderSettings copy = new RenderSettings();
        copy.drawWireframe = this.drawWireframe;
        copy.useTexture = this.useTexture;
        copy.useLighting = this.useLighting;
        copy.solidColor = this.solidColor;
        copy.wireframeColor = this.wireframeColor;
        copy.wireframeThickness = this.wireframeThickness;
        copy.useZBuffer = this.useZBuffer;
        copy.backfaceCulling = this.backfaceCulling;
        copy.smoothShading = this.smoothShading;
        copy.useBilinearFiltering = this.useBilinearFiltering;
        return copy;
    }
}
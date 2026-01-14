package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.processing.ModelProcessor;
import com.cgvsu.render_engine.rendering.*;
import com.cgvsu.render_engine.texture.Texture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RenderManager {
    private RenderEngine wireframeRenderer;
    private SolidRenderer solidRenderer;
    private TexturedRenderer texturedRenderer;
    private LightingRenderer lightingRenderer;
    private ModelProcessor modelProcessor;
    private RenderSettings renderSettings;

    public RenderManager() {
        this.wireframeRenderer = new RenderEngine();
        this.solidRenderer = new SolidRenderer();
        this.texturedRenderer = new TexturedRenderer();
        this.lightingRenderer = new LightingRenderer();
        this.modelProcessor = new ModelProcessor();
        this.renderSettings = new RenderSettings();
    }

    public Model processModel(Model model) {
        return modelProcessor.processModel(model);
    }

    // Новый метод для комбинированного рендеринга
    public void render(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Texture texture) {

        // Очищаем холст
        gc.clearRect(0, 0, width, height);

        // Определяем текущий режим
        RenderMode mode = renderSettings.getCurrentMode();

        switch (mode) {
            case SOLID:
                renderSolid(gc, camera, mesh, width, height, renderSettings.getSolidColor());
                break;

            case WIREFRAME:
                renderWireframe(gc, camera, mesh, width, height);
                break;

            case TEXTURED:
                if (texture != null) {
                    renderTextured(gc, camera, mesh, width, height, texture);
                } else {
                    renderSolid(gc, camera, mesh, width, height, renderSettings.getSolidColor());
                }
                break;

            case LIT:
                renderWithLighting(gc, camera, mesh, width, height, renderSettings.getSolidColor());
                break;

            case WIREFRAME_SOLID:
                // Сначала заливка, потом каркас поверх
                renderSolid(gc, camera, mesh, width, height, renderSettings.getSolidColor());
                renderWireframe(gc, camera, mesh, width, height);
                break;

            case TEXTURED_LIT:
                if (texture != null) {
                    renderTexturedWithLighting(gc, camera, mesh, width, height, texture);
                } else {
                    renderWithLighting(gc, camera, mesh, width, height, renderSettings.getSolidColor());
                }
                break;

            case ALL:
                if (texture != null) {
                    // Текстура + освещение + каркас
                    renderTexturedWithLighting(gc, camera, mesh, width, height, texture);
                } else {
                    // Заливка + освещение + каркас
                    renderWithLighting(gc, camera, mesh, width, height, renderSettings.getSolidColor());
                }
                renderWireframe(gc, camera, mesh, width, height);
                break;
        }
    }

    // Старые методы для обратной совместимости
    public void renderWireframe(
            GraphicsContext gc, Camera camera, Model mesh,
            int width, int height) {
        wireframeRenderer.renderWireframe(gc, camera, mesh, width, height);
    }

    public void renderSolid(
            GraphicsContext gc, Camera camera, Model mesh,
            int width, int height, Color color) {
        solidRenderer.renderSolid(gc, camera, mesh, width, height, color);
    }

    public void renderTextured(
            GraphicsContext gc, Camera camera, Model mesh,
            int width, int height, Texture texture) {
        texturedRenderer.renderTextured(gc, camera, mesh, width, height, texture);
    }

    public void renderTexturedWithLighting(
            GraphicsContext gc, Camera camera, Model mesh,
            int width, int height, Texture texture) {
        texturedRenderer.renderTexturedWithLighting(gc, camera, mesh, width, height, texture);
    }

    public void renderWithLighting(
            GraphicsContext gc, Camera camera, Model mesh,
            int width, int height, Color color) {
        lightingRenderer.renderWithLighting(gc, camera, mesh, width, height, color);
    }

    // Геттеры и сеттеры для настроек
    public RenderSettings getRenderSettings() {
        return renderSettings;
    }

    public void setRenderSettings(RenderSettings settings) {
        this.renderSettings = settings;
    }
}
package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.lighting.SceneLighting;
import com.cgvsu.render_engine.rendering.RenderSettings;
import com.cgvsu.render_engine.rendering.UnifiedRenderer;
import com.cgvsu.render_engine.texture.Texture;
import com.cgvsu.render_engine.texture.TextureManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class RenderManager {
    private UnifiedRenderer renderer;
    private RenderSettings renderSettings;
    private SceneLighting sceneLighting;
    private Texture currentTexture;

    public RenderManager() {
        this.renderer = new UnifiedRenderer();
        this.renderSettings = renderer.getRenderSettings();
        this.sceneLighting = renderer.getSceneLighting();
    }

    // Основной метод рендеринга
    public void render(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height) {

        // Устанавливаем текстуру в модель (если есть)
        if (renderSettings.isUseTexture() && currentTexture != null) {
            if (model.getMaterial() == null) {
                model.setMaterial(new com.cgvsu.render_engine.lighting.Material());
            }
            model.getMaterial().setDiffuseTexture(currentTexture);
        }

        // Выполняем рендеринг
        renderer.render(graphicsContext, camera, model, width, height);
    }

    // Быстрые настройки

    public void setSolidColor(Color color) {
        renderSettings.setSolidColor(color);
    }

    public void setWireframe(boolean enabled) {
        renderSettings.setDrawWireframe(enabled);
    }

    public void setTexture(boolean enabled) {
        renderSettings.setUseTexture(enabled);
    }

    public void setLighting(boolean enabled) {
        renderSettings.setUseLighting(enabled);
    }

    public void loadTexture(String path) {
        this.currentTexture = TextureManager.getInstance().loadTexture(path);
    }

    public void setTexture(Texture texture) {
        this.currentTexture = texture;
    }

    // Управление освещением

    public void setLightFollowsCamera(boolean follow) {
        sceneLighting.setLightFollowsCamera(follow);
    }

    // Геттеры

    public RenderSettings getRenderSettings() {
        return renderSettings;
    }

    public UnifiedRenderer getRenderer() {
        return renderer;
    }

    public SceneLighting getSceneLighting() {
        return sceneLighting;
    }

    // Очистка ресурсов
    public void cleanup() {
        if (renderer != null) {
            // Можно добавить очистку кэшей если нужно
        }
    }
}
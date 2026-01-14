package com.cgvsu.render_engine.scene;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderManager;
import com.cgvsu.render_engine.rendering.RenderSettings;
import com.cgvsu.render_engine.texture.Texture;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

public class SceneRenderer {
    private RenderManager renderManager;
    private Scene scene;
    private boolean showCameras = true;

    public SceneRenderer() {
        this.renderManager = new RenderManager();
        this.scene = new Scene();
    }

    public void renderScene(GraphicsContext gc, int width, int height) {
        // Очищаем холст
        gc.clearRect(0, 0, width, height);

        // Получаем активную камеру
        Camera activeCamera = scene.getActiveCamera();
        if (activeCamera == null) {
            return; // Нет камер для рендеринга
        }

        // Рендерим все модели сцены
        for (Model model : scene.getModels()) {
            Texture texture = scene.getTextures().isEmpty() ? null : scene.getTextures().get(0);
            renderManager.render(gc, activeCamera, model, width, height, texture);
        }

        // Рендерим модели камер, если включено
        if (showCameras) {
            renderCameraModels(gc, activeCamera, width, height);
        }
    }

    private void renderCameraModels(GraphicsContext gc, Camera activeCamera, int width, int height) {
        // Получаем все камеры кроме активной
        for (int i = 0; i < scene.getCameras().size(); i++) {
            if (i != scene.getActiveCameraIndex()) {
                Camera camera = scene.getCameras().get(i);
                // Здесь нужно получить модель камеры и отрендерить её
                // Модели камер хранятся в CameraManager
            }
        }
    }

    // Методы для управления сценой
    public void addModel(Model model) {
        scene.addModel(model);
    }

    public void addCamera(Camera camera) {
        scene.addCamera(camera);
    }

    public void setActiveCamera(int index) {
        scene.setActiveCamera(index);
    }

    public Camera getActiveCamera() {
        return scene.getActiveCamera();
    }

    public List<Camera> getCameras() {
        return scene.getCameras();
    }

    public void setShowCameras(boolean show) {
        this.showCameras = show;
    }

    public RenderSettings getRenderSettings() {
        return renderManager.getRenderSettings();
    }

    public void setRenderSettings(RenderSettings settings) {
        renderManager.setRenderSettings(settings);
    }

    public Scene getScene() {
        return scene;
    }
}
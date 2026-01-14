package com.cgvsu.render_engine.scene;

import com.cgvsu.render_engine.Camera;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraManager {
    private List<Camera> cameras;
    private int activeCameraIndex;

    // Модели для визуализации камер
    private List<com.cgvsu.model.Model> cameraModels;

    public CameraManager() {
        this.cameras = new ArrayList<>();
        this.cameraModels = new ArrayList<>();
        this.activeCameraIndex = 0;

        // Создаем камеру по умолчанию
        Camera defaultCamera = createDefaultCamera();
        addCamera(defaultCamera);
    }

    private Camera createDefaultCamera() {
        return new Camera(
                new Vector3f(0, 0, 100),
                new Vector3f(0, 0, 0),
                1.0F, 1.0F,  // aspectRatio будет обновляться
                0.01F, 100.0F
        );
    }

    public Camera createCamera(
            Vector3f position,
            Vector3f target,
            float fov,
            float aspectRatio,
            float nearPlane,
            float farPlane) {

        Camera camera = new Camera(position, target, fov, aspectRatio, nearPlane, farPlane);
        addCamera(camera);
        return camera;
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);

        // Создаем 3D модель для камеры (пирамида камеры)
        com.cgvsu.model.Model cameraModel = createCameraModel(camera);
        cameraModels.add(cameraModel);
    }

    public void removeCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            cameras.remove(index);
            cameraModels.remove(index);

            // Корректируем индекс активной камеры
            if (activeCameraIndex >= cameras.size()) {
                activeCameraIndex = Math.max(0, cameras.size() - 1);
            }
        }
    }

    public Camera getActiveCamera() {
        if (cameras.isEmpty()) {
            return null;
        }
        return cameras.get(activeCameraIndex);
    }

    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            activeCameraIndex = index;
        }
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public int getActiveCameraIndex() {
        return activeCameraIndex;
    }

    public List<com.cgvsu.model.Model> getCameraModels() {
        return cameraModels;
    }

    public void updateCameraAspectRatio(float aspectRatio) {
        for (Camera camera : cameras) {
            camera.setAspectRatio(aspectRatio);
        }
    }

    // Создание 3D модели камеры (пирамида видимости)
    private com.cgvsu.model.Model createCameraModel(Camera camera) {
        com.cgvsu.model.Model model = new com.cgvsu.model.Model();

        // Вершины пирамиды камеры
        // Основание пирамиды (квадрат на позиции камеры)
        float size = 5.0f; // Размер пирамиды

        model.vertices.add(new com.cgvsu.math.Vector3f(0, 0, 0)); // 0 - вершина (позиция камеры)
        model.vertices.add(new com.cgvsu.math.Vector3f(-size, -size, -size * 2)); // 1
        model.vertices.add(new com.cgvsu.math.Vector3f(size, -size, -size * 2));  // 2
        model.vertices.add(new com.cgvsu.math.Vector3f(size, size, -size * 2));   // 3
        model.vertices.add(new com.cgvsu.math.Vector3f(-size, size, -size * 2));  // 4

        // Полигоны для пирамиды
        com.cgvsu.model.Polygon poly1 = new com.cgvsu.model.Polygon();
        poly1.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        model.polygons.add(poly1);

        com.cgvsu.model.Polygon poly2 = new com.cgvsu.model.Polygon();
        poly2.setVertexIndices(new ArrayList<>(Arrays.asList(0, 2, 3)));
        model.polygons.add(poly2);

        com.cgvsu.model.Polygon poly3 = new com.cgvsu.model.Polygon();
        poly3.setVertexIndices(new ArrayList<>(Arrays.asList(0, 3, 4)));
        model.polygons.add(poly3);

        com.cgvsu.model.Polygon poly4 = new com.cgvsu.model.Polygon();
        poly4.setVertexIndices(new ArrayList<>(Arrays.asList(0, 4, 1)));
        model.polygons.add(poly4);

        // Основание пирамиды
        com.cgvsu.model.Polygon base = new com.cgvsu.model.Polygon();
        base.setVertexIndices(new ArrayList<>(Arrays.asList(1, 2, 3, 4)));
        model.polygons.add(base);

        return model;
    }

    // Обновление позиций моделей камер
    public void updateCameraModels() {
        for (int i = 0; i < cameras.size(); i++) {
            Camera camera = cameras.get(i);
            com.cgvsu.model.Model cameraModel = cameraModels.get(i);

            // Обновляем позицию модели камеры
            // (в реальном приложении нужно применять трансформации)
        }
    }
}
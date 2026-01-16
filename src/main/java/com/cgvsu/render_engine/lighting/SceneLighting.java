package com.cgvsu.render_engine.lighting;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import java.util.ArrayList;
import java.util.List;

public class SceneLighting {
    private final List<LightSource> lights;
    private LightSource ambientLight;
    private boolean lightFollowsCamera = true;

    public SceneLighting() {
        lights = new ArrayList<>();

        // Создаем ambient light по умолчанию
        ambientLight = new LightSource();
        ambientLight.setType(LightSource.LightType.AMBIENT);
        ambientLight.setColor(javafx.scene.paint.Color.WHITE);
        ambientLight.setIntensity(0.2f);

        // Создаем основной источник света по умолчанию
        LightSource mainLight = new LightSource();
        mainLight.setType(LightSource.LightType.DIRECTIONAL);
        mainLight.setDirection(new Vector3f(-1, -1, -1));
        mainLight.setColor(javafx.scene.paint.Color.WHITE);
        mainLight.setIntensity(0.8f);

        lights.add(mainLight);
    }

    // Обновление позиций источников света при движении камеры
    public void updateForCamera(Camera camera) {
        if (!lightFollowsCamera || camera == null) {
            return;
        }

        // Пример: привязка первого направленного света к камере
        if (!lights.isEmpty()) {
            LightSource mainLight = lights.get(0);
            if (mainLight.getType() == LightSource.LightType.DIRECTIONAL) {
                // Направляем свет от камеры на сцену
                Vector3f cameraToTarget = camera.getTarget().subtract(camera.getPosition());
                mainLight.setDirection(cameraToTarget.normalize());
            }
        }
    }

    // Расчет освещения для точки с несколькими источниками
    public javafx.scene.paint.Color calculateLighting(
            Material material,
            Vector3f position,
            Vector3f normal,
            Vector3f viewDir,
            javafx.scene.paint.Color baseColor) {

        // Начинаем с ambient света
        javafx.scene.paint.Color result = LightingModel.calculate(
                ambientLight, material, position, normal, viewDir, baseColor
        );

        // Добавляем все остальные источники света
        for (LightSource light : lights) {
            if (light.getType() != LightSource.LightType.AMBIENT) {
                javafx.scene.paint.Color lightContribution = LightingModel.calculate(
                        light, material, position, normal, viewDir, baseColor
                );

                // Смешиваем цвета (аддитивное смешивание)
                result = blendAdditive(result, lightContribution);
            }
        }

        return result;
    }

    // Аддитивное смешивание цветов
    private javafx.scene.paint.Color blendAdditive(
            javafx.scene.paint.Color a,
            javafx.scene.paint.Color b) {

        double r = Math.min(1.0, a.getRed() + b.getRed());
        double g = Math.min(1.0, a.getGreen() + b.getGreen());
        double b1 = Math.min(1.0, a.getBlue() + b.getBlue());
        double alpha = Math.max(a.getOpacity(), b.getOpacity());

        return new javafx.scene.paint.Color(r, g, b1, alpha);
    }

    // Геттеры и сеттеры
    public List<LightSource> getLights() { return lights; }

    public void addLight(LightSource light) {
        lights.add(light);
    }

    public void removeLight(LightSource light) {
        lights.remove(light);
    }

    public void clearLights() {
        lights.clear();
    }

    public LightSource getAmbientLight() { return ambientLight; }
    public void setAmbientLight(LightSource ambientLight) {
        this.ambientLight = ambientLight;
    }

    public boolean isLightFollowsCamera() { return lightFollowsCamera; }
    public void setLightFollowsCamera(boolean follow) {
        this.lightFollowsCamera = follow;
    }
}
package com.cgvsu.render_engine.lighting;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import javafx.scene.paint.Color;
//import javax.vecmath.Vector3f;

public class LightingModel {

    public enum LightingType {
        FLAT,          // Плоское освещение
        GOURAUD,       // Освещение по Гуро
        PHONG,         // Освещение по Фонгу (упрощенное)
        AMBIENT_DIFFUSE // Фоновое + рассеянное
    }

    private LightingType lightingType = LightingType.AMBIENT_DIFFUSE;
    private Camera camera;

    private float ambientIntensity = 0.2f;    // Фоновое освещение
    private float diffuseIntensity = 0.7f;    // Рассеянное освещение
    private float specularIntensity = 0.5f;   // Зеркальное освещение
    private float shininess = 32.0f;          // Блеск

    private Color ambientColor = Color.WHITE;
    private Color diffuseColor = Color.WHITE;
    private Color specularColor = Color.WHITE;

    private Vector3f lightPosition;
    private Vector3f lightDirection;

    public LightingModel() {
        this.lightPosition = new Vector3f(0, 10, 10);
        this.lightDirection = new Vector3f(0, -1, -1);
        this.lightDirection.normalize();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        // Привязываем источник света к позиции камеры
        if (camera != null) {
            this.lightPosition = new Vector3f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ());
            // Направляем свет от камеры на цель
            this.lightDirection = new Vector3f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ());
            this.lightDirection.subtract(camera.getPosition());
            this.lightDirection.normalize();
        }
    }

    public float calculateLightIntensity(Vector3f normal, Vector3f position) {
        if (lightingType == LightingType.FLAT) {
            return 1.0f;
        }

        // Нормализуем нормаль
        normal.normalize();

        // Рассеянное освещение (Lambert)
        float diffuse = (float) Math.max(0, -lightDirection.dot(normal));

        // Зеркальное освещение (Phong)
        float specular = 0.0f;
        if (camera != null && specularIntensity > 0) {
            // Вектор от точки к камере
            Vector3f viewDir = new Vector3f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ());
            viewDir.subtract(position);
            viewDir.normalize();

            // Вектор отражения
            Vector3f reflectDir = reflect(lightDirection, normal);
            reflectDir.normalize();

            specular = (float) Math.pow(Math.max(0, viewDir.dot(reflectDir)), shininess);
        }

        // Итоговая интенсивность
        return ambientIntensity +
                diffuseIntensity * diffuse +
                specularIntensity * specular;
    }

    public Color applyLighting(Color baseColor, float intensity) {
        // Ограничиваем интенсивность
        intensity = Math.max(0.1f, Math.min(1.0f, intensity));

        // Применяем интенсивность к цвету
        return Color.color(
                baseColor.getRed() * intensity,
                baseColor.getGreen() * intensity,
                baseColor.getBlue() * intensity,
                baseColor.getOpacity()
        );
    }

    public Color applyFullLighting(Color baseColor, Vector3f normal, Vector3f position) {
        if (lightingType == LightingType.FLAT) {
            return baseColor;
        }

        normal.normalize();

        // Фоновое освещение
        double ambientR = baseColor.getRed() * ambientIntensity;
        double ambientG = baseColor.getGreen() * ambientIntensity;
        double ambientB = baseColor.getBlue() * ambientIntensity;

        // Рассеянное освещение
        float diffuse = (float) Math.max(0, -lightDirection.dot(normal));
        double diffuseR = baseColor.getRed() * diffuse * diffuseIntensity;
        double diffuseG = baseColor.getGreen() * diffuse * diffuseIntensity;
        double diffuseB = baseColor.getBlue() * diffuse * diffuseIntensity;

        // Зеркальное освещение
        double specularR = 0, specularG = 0, specularB = 0;
        if (camera != null && specularIntensity > 0) {
            Vector3f viewDir = new Vector3f(camera.getPosition().getX(), camera.getPosition().getY(), camera.getPosition().getZ());
            viewDir.subtract(position);
            viewDir.normalize();

            Vector3f reflectDir = reflect(lightDirection, normal);
            reflectDir.normalize();

            float specular = (float) Math.pow(Math.max(0, viewDir.dot(reflectDir)), shininess);
            specularR = specularColor.getRed() * specular * specularIntensity;
            specularG = specularColor.getGreen() * specular * specularIntensity;
            specularB = specularColor.getBlue() * specular * specularIntensity;
        }

        // Суммируем все компоненты
        double finalR = Math.min(1.0, ambientR + diffuseR + specularR);
        double finalG = Math.min(1.0, ambientG + diffuseG + specularG);
        double finalB = Math.min(1.0, ambientB + diffuseB + specularB);

        return new Color(finalR, finalG, finalB, baseColor.getOpacity());
    }

    private Vector3f reflect(Vector3f lightDir, Vector3f normal) {
        // r = 2 * (n·l) * n - l
        float dot = (float) normal.dot(lightDir);
        Vector3f result = new Vector3f(normal.getX(), normal.getY(), normal.getZ());
        result.scale(2 * dot);
        result.subtract(lightDir);
        return result;
    }

    public void setLightingType(LightingType type) {
        this.lightingType = type;
    }

    public void setAmbientIntensity(float intensity) {
        this.ambientIntensity = intensity;
    }

    public void setDiffuseIntensity(float intensity) {
        this.diffuseIntensity = intensity;
    }

    public void setSpecularIntensity(float intensity) {
        this.specularIntensity = intensity;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    public void setAmbientColor(Color color) {
        this.ambientColor = color;
    }

    public void setDiffuseColor(Color color) {
        this.diffuseColor = color;
    }

    public void setSpecularColor(Color color) {
        this.specularColor = color;
    }

    public void setLightPosition(Vector3f position) {
        this.lightPosition = position;
    }

    public void setLightDirection(Vector3f direction) {
        this.lightDirection = direction;
        this.lightDirection.normalize();
    }
}
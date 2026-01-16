package com.cgvsu.render_engine.lighting;

import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.Camera;
import javafx.scene.paint.Color;

public class LightingModel {

    public enum LightingType {
        FLAT,          // Плоское освещение
        GOURAUD,       // Освещение по Гуро
        PHONG,         // Освещение по Фонгу (упрощенное)
        AMBIENT_DIFFUSE, // Фоновое + рассеянное
        NONE
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

    private boolean lightFollowsCamera = true;

    private Vector3f lightPosition;
    private Vector3f lightDirection;

    private static final float MIN_LIGHT = 0.1f;
    private static final float MAX_LIGHT = 1.0f;

    public LightingModel() {
        this.lightPosition = new Vector3f(0, 10, 10);
        this.lightDirection = new Vector3f(0, -1, -1);
        this.lightDirection.normalize();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        // Привязываем источник света к позиции камеры
        if (camera != null && lightFollowsCamera) {
            this.lightPosition = new Vector3f(
                    camera.getPosition().getX(),
                    camera.getPosition().getY(),
                    camera.getPosition().getZ()
            );
            // Направляем свет от камеры на цель
            this.lightDirection = new Vector3f(
                    camera.getTarget().getX(),
                    camera.getTarget().getY(),
                    camera.getTarget().getZ()
            );
            this.lightDirection.sub(camera.getPosition());
            this.lightDirection.normalize();
        }
    }

    public float calculateLightIntensity(Vector3f normal, Vector3f position) {
        if (lightingType == LightingType.FLAT) {
            return 1.0f;
        }

        // Нормализуем нормаль
        normal = normal.normalize();

        // Рассеянное освещение (Lambert)
        // Исправлено: свет направлен ОТ источника
        float diffuse = (float) Math.max(0, lightDirection.scale(-1).dot(normal));

        // Зеркальное освещение (Phong)
        float specular = 0.0f;
        if (camera != null && specularIntensity > 0) {
            // Вектор от точки к камере
            Vector3f viewDir = new Vector3f(
                    camera.getPosition().getX(),
                    camera.getPosition().getY(),
                    camera.getPosition().getZ()
            );
            viewDir.sub(position);
            viewDir.normalize();

            // Вектор отражения
            Vector3f reflectDir = reflect(lightDirection.scale(-1), normal);
            reflectDir.normalize();

            specular = (float) Math.pow(Math.max(0, viewDir.dot(reflectDir)), shininess);
        }

        // Итоговая интенсивность
        float intensity = ambientIntensity + diffuseIntensity * diffuse + specularIntensity * specular;

        // Ограничиваем интенсивность
        return Math.max(MIN_LIGHT, Math.min(MAX_LIGHT, intensity));
    }

    public Color applyLighting(Color baseColor, float intensity) {
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

        normal = normal.normalize();

        // Фоновое освещение
        double ambientR = baseColor.getRed() * ambientIntensity;
        double ambientG = baseColor.getGreen() * ambientIntensity;
        double ambientB = baseColor.getBlue() * ambientIntensity;

        // Рассеянное освещение (исправлено направление света)
        float diffuse = (float) Math.max(0, lightDirection.scale(-1).dot(normal));
        double diffuseR = baseColor.getRed() * diffuse * diffuseIntensity;
        double diffuseG = baseColor.getGreen() * diffuse * diffuseIntensity;
        double diffuseB = baseColor.getBlue() * diffuse * diffuseIntensity;

        // Зеркальное освещение
        double specularR = 0, specularG = 0, specularB = 0;
        if (camera != null && specularIntensity > 0) {
            Vector3f viewDir = new Vector3f(
                    camera.getPosition().getX(),
                    camera.getPosition().getY(),
                    camera.getPosition().getZ()
            );
            viewDir.sub(position);
            viewDir.normalize();

            Vector3f reflectDir = reflect(lightDirection.scale(-1), normal);
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

    // Геттеры и сеттеры
    public LightingType getLightingType() {
        return lightingType;
    }

    public void setLightingType(LightingType type) {
        this.lightingType = type;
    }

    public void setAmbientIntensity(float intensity) {
        this.ambientIntensity = Math.max(0, Math.min(1, intensity));
    }

    public void setDiffuseIntensity(float intensity) {
        this.diffuseIntensity = Math.max(0, Math.min(1, intensity));
    }

    public void setSpecularIntensity(float intensity) {
        this.specularIntensity = Math.max(0, Math.min(1, intensity));
    }

    public void setShininess(float shininess) {
        this.shininess = Math.max(1, shininess);
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
        this.lightDirection = direction.normalize();
    }

    public boolean isLightFollowsCamera() {
        return lightFollowsCamera;
    }

    public void setLightFollowsCamera(boolean lightFollowsCamera) {
        this.lightFollowsCamera = lightFollowsCamera;
    }

    public Camera getCamera() {
        return camera;
    }

    public float getAmbientIntensity() {
        return ambientIntensity;
    }

    public float getDiffuseIntensity() {
        return diffuseIntensity;
    }

    public float getSpecularIntensity() {
        return specularIntensity;
    }

    public float getShininess() {
        return shininess;
    }

    public Color getAmbientColor() {
        return ambientColor;
    }

    public Color getDiffuseColor() {
        return diffuseColor;
    }

    public Color getSpecularColor() {
        return specularColor;
    }

    public Vector3f getLightPosition() {
        return lightPosition;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    // Чистая функция расчета освещения
    public static Color calculate(
            LightSource light,
            Material material,
            Vector3f position,
            Vector3f normal,
            Vector3f viewDir,
            Color baseColor) {

        // Нормализуем векторы
        normal = normal.normalize();
        viewDir = viewDir.normalize();

        // Получаем цвет материала
        Color materialColor = baseColor;

        // Компоненты освещения
        Color ambient = calculateAmbient(light, material, materialColor);
        Color diffuse = calculateDiffuse(light, material, normal, materialColor);
        Color specular = calculateSpecular(light, material, normal, viewDir);

        // Затухание и фактор прожектора
        float attenuation = 1.0f;
        float spotFactor = 1.0f;

        if (light.getType() != LightSource.LightType.DIRECTIONAL) {
            float distance = (float) position.distance(light.getPosition());
            attenuation = light.getAttenuation(distance);

            if (light.getType() == LightSource.LightType.SPOT) {
                Vector3f lightToPoint = position.subtract(light.getPosition());
                spotFactor = light.getSpotFactor(lightToPoint);
            }
        }

        // Суммируем компоненты
        double r = ambient.getRed() +
                (diffuse.getRed() + specular.getRed()) * attenuation * spotFactor;
        double g = ambient.getGreen() +
                (diffuse.getGreen() + specular.getGreen()) * attenuation * spotFactor;
        double b = ambient.getBlue() +
                (diffuse.getBlue() + specular.getBlue()) * attenuation * spotFactor;

        // Ограничиваем значения
        r = Math.min(1.0, Math.max(0, r));
        g = Math.min(1.0, Math.max(0, g));
        b = Math.min(1.0, Math.max(0, b));

        return new Color(r, g, b, materialColor.getOpacity());
    }

    private static Color calculateAmbient(LightSource light, Material material, Color materialColor) {
        double r = materialColor.getRed() * material.getAmbientCoefficient();
        double g = materialColor.getGreen() * material.getAmbientCoefficient();
        double b = materialColor.getBlue() * material.getAmbientCoefficient();
        return new Color(r, g, b, materialColor.getOpacity());
    }

    private static Color calculateDiffuse(LightSource light, Material material,
                                          Vector3f normal, Color materialColor) {
        Vector3f lightDir = light.getDirectionFrom(normal.scale(-1));

        // Ламбертовское рассеивание
        float NdotL = (float) Math.max(0, normal.dot(lightDir));

        if (NdotL <= 0) {
            return Color.rgb(0, 0, 0, materialColor.getOpacity());
        }

        double lightR = light.getColor().getRed() * light.getIntensity();
        double lightG = light.getColor().getGreen() * light.getIntensity();
        double lightB = light.getColor().getBlue() * light.getIntensity();

        double r = materialColor.getRed() * lightR * material.getDiffuseCoefficient() * NdotL;
        double g = materialColor.getGreen() * lightG * material.getDiffuseCoefficient() * NdotL;
        double b = materialColor.getBlue() * lightB * material.getDiffuseCoefficient() * NdotL;

        return new Color(r, g, b, materialColor.getOpacity());
    }

    private static Color calculateSpecular(LightSource light, Material material,
                                           Vector3f normal, Vector3f viewDir) {
        if (material.getSpecularCoefficient() <= 0) {
            return Color.rgb(0, 0, 0, 1.0);
        }

        Vector3f lightDir = light.getDirectionFrom(normal.scale(-1));
        float NdotL = (float) normal.dot(lightDir);

        if (NdotL <= 0) {
            return Color.rgb(0, 0, 0, 1.0);
        }

        // Вектор отражения (по модели Фонга)
        Vector3f reflectDir = reflect(lightDir.scale(-1), normal);

        // Скалярное произведение отражения и направления взгляда
        float RdotV = (float) Math.max(0, reflectDir.dot(viewDir));

        // Зеркальная составляющая
        float specularFactor = (float) Math.pow(RdotV, material.getShininess());

        double lightR = light.getColor().getRed() * light.getIntensity();
        double lightG = light.getColor().getGreen() * light.getIntensity();
        double lightB = light.getColor().getBlue() * light.getIntensity();

        double materialR = material.getSpecularColor().getRed();
        double materialG = material.getSpecularColor().getGreen();
        double materialB = material.getSpecularColor().getBlue();

        double r = lightR * materialR * material.getSpecularCoefficient() * specularFactor;
        double g = lightG * materialG * material.getSpecularCoefficient() * specularFactor;
        double b = lightB * materialB * material.getSpecularCoefficient() * specularFactor;

        return new Color(r, g, b, 1.0);
    }

    private static Vector3f reflect(Vector3f incident, Vector3f normal) {
        // r = i - 2*(i·n)*n
        float dot = (float) incident.dot(normal);
        return incident.subtract(normal.scale(2 * dot));
    }

    // Упрощенный метод для одного источника света
    public static Color calculateSimple(
            Vector3f lightDir,
            Material material,
            Vector3f normal,
            Vector3f viewDir,
            Color baseColor) {

        normal = normal.normalize();
        viewDir = viewDir.normalize();
        lightDir = lightDir.normalize();

        // Ambient
        double ambientR = baseColor.getRed() * material.getAmbientCoefficient();
        double ambientG = baseColor.getGreen() * material.getAmbientCoefficient();
        double ambientB = baseColor.getBlue() * material.getAmbientCoefficient();

        // Diffuse (Lambert)
        float NdotL = (float) Math.max(0, normal.dot(lightDir.scale(-1)));
        double diffuseR = baseColor.getRed() * material.getDiffuseCoefficient() * NdotL;
        double diffuseG = baseColor.getGreen() * material.getDiffuseCoefficient() * NdotL;
        double diffuseB = baseColor.getBlue() * material.getDiffuseCoefficient() * NdotL;

        // Specular (Blinn-Phong упрощенный)
        double specularR = 0, specularG = 0, specularB = 0;
        if (NdotL > 0 && material.getSpecularCoefficient() > 0) {
            Vector3f halfway = lightDir.scale(-1).add(viewDir).normalize();
            float NdotH = (float) Math.max(0, normal.dot(halfway));
            float specularFactor = (float) Math.pow(NdotH, material.getShininess());

            specularR = material.getSpecularColor().getRed() *
                    material.getSpecularCoefficient() * specularFactor;
            specularG = material.getSpecularColor().getGreen() *
                    material.getSpecularCoefficient() * specularFactor;
            specularB = material.getSpecularColor().getBlue() *
                    material.getSpecularCoefficient() * specularFactor;
        }

        // Итоговый цвет
        double r = Math.min(1.0, ambientR + diffuseR + specularR);
        double g = Math.min(1.0, ambientG + diffuseG + specularG);
        double b = Math.min(1.0, ambientB + diffuseB + specularB);

        return new Color(r, g, b, baseColor.getOpacity());
    }
}
package com.cgvsu.render_engine.lighting;

import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

public class LightSource {
    public enum LightType {
        DIRECTIONAL,    // Направленный источник
        POINT,          // Точечный источник
        SPOT,           // Прожектор
        AMBIENT         // Фоновое освещение
    }

    private LightType type = LightType.DIRECTIONAL;
    private Vector3f position = new Vector3f(0, 10, 10);
    private Vector3f direction = new Vector3f(0, -1, -1).normalize();
    private Color color = Color.WHITE;
    private float intensity = 1.0f;
    private float range = 100.0f; // Для точечного источника
    private float spotAngle = 45.0f; // Для прожектора

    public LightSource() {
    }

    public LightSource(LightType type, Vector3f position, Vector3f direction, Color color, float intensity) {
        this.type = type;
        this.position = position;
        this.direction = direction.normalize();
        this.color = color;
        this.intensity = intensity;
    }

    public LightType getType() { return type; }
    public void setType(LightType type) { this.type = type; }

    public Vector3f getPosition() { return position; }
    public void setPosition(Vector3f position) { this.position = position; }

    public Vector3f getDirection() { return direction; }
    public void setDirection(Vector3f direction) {
        this.direction = direction.normalize();
    }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public float getIntensity() { return intensity; }
    public void setIntensity(float intensity) {
        this.intensity = Math.max(0, Math.min(1, intensity));
    }

    public float getRange() { return range; }
    public void setRange(float range) { this.range = Math.max(0, range); }

    public float getSpotAngle() { return spotAngle; }
    public void setSpotAngle(float spotAngle) {
        this.spotAngle = Math.max(0, Math.min(90, spotAngle));
    }

    // Для направленного света
    public Vector3f getDirectionFrom(Vector3f point) {
        if (type == LightType.DIRECTIONAL) {
            return direction.scale(-1); // Направление от источника к точке
        } else {
            return position.subtract(point).normalize();
        }
    }

    // Расчет затухания для точечного источника
    public float getAttenuation(float distance) {
        if (type != LightType.POINT && type != LightType.SPOT) {
            return 1.0f;
        }

        if (distance > range) return 0.0f;

        // Квадратичное затухание
        float attenuation = 1.0f / (1.0f + 0.1f * distance + 0.01f * distance * distance);
        return Math.max(0, Math.min(1, attenuation));
    }

    // Расчет угла для прожектора
    public float getSpotFactor(Vector3f lightToPoint) {
        if (type != LightType.SPOT) {
            return 1.0f;
        }

        float cosAngle = (float) Math.cos(Math.toRadians(spotAngle));
        float currentCos = (float) lightToPoint.normalize().dot(direction.scale(-1));

        if (currentCos < cosAngle) {
            return 0.0f;
        }

        // Плавное затухание к краю прожектора
        return (currentCos - cosAngle) / (1.0f - cosAngle);
    }
}
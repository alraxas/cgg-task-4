package com.cgvsu.render_engine.lighting;

import com.cgvsu.render_engine.texture.Texture;
import javafx.scene.paint.Color;

public class Material {
    private Color baseColor = Color.WHITE;
    private Texture diffuseTexture = null;
    private float ambientCoefficient = 0.2f;
    private float diffuseCoefficient = 0.7f;
    private float specularCoefficient = 0.5f;
    private float shininess = 32.0f;
    private Color specularColor = Color.WHITE;

    public Material() {
    }

    public Material(Color baseColor) {
        this.baseColor = baseColor;
    }

    // Получить цвет в точке (с учетом текстуры)
    public Color getColorAt(float u, float v) {
        if (diffuseTexture != null) {
            return diffuseTexture.getColorBilinear(u, v);
        }
        return baseColor;
    }

    // Геттеры и сеттеры
    public Color getBaseColor() { return baseColor; }
    public void setBaseColor(Color baseColor) { this.baseColor = baseColor; }

    public Texture getDiffuseTexture() { return diffuseTexture; }
    public void setDiffuseTexture(Texture diffuseTexture) {
        this.diffuseTexture = diffuseTexture;
    }

    public float getAmbientCoefficient() { return ambientCoefficient; }
    public void setAmbientCoefficient(float coefficient) {
        this.ambientCoefficient = Math.max(0, Math.min(1, coefficient));
    }

    public float getDiffuseCoefficient() { return diffuseCoefficient; }
    public void setDiffuseCoefficient(float coefficient) {
        this.diffuseCoefficient = Math.max(0, Math.min(1, coefficient));
    }

    public float getSpecularCoefficient() { return specularCoefficient; }
    public void setSpecularCoefficient(float coefficient) {
        this.specularCoefficient = Math.max(0, Math.min(1, coefficient));
    }

    public float getShininess() { return shininess; }
    public void setShininess(float shininess) {
        this.shininess = Math.max(1, shininess);
    }

    public Color getSpecularColor() { return specularColor; }
    public void setSpecularColor(Color specularColor) {
        this.specularColor = specularColor;
    }

    public boolean hasTexture() {
        return diffuseTexture != null && diffuseTexture.isValid();
    }
}
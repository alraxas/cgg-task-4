package com.cgvsu.render_engine.lighting;

import com.cgvsu.math.Vector3f;
import javafx.scene.paint.Color;

public class LightingModel {

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
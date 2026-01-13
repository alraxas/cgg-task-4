package com.cgvsu.render_engine.transformation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector2f;

import java.awt.*;

public class CoordinateTransformer {

    public Point convertToScreen(Vector3f vertex, int width, int height) {
        // Простая ортографическая проекция
        int x = (int) ((vertex.getX() + 1) * width / 2);
        int y = (int) ((1 - vertex.getY()) * height / 2);

        // Ограничиваем координаты размерами экрана
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        return new Point(x, y);
    }

    public Vector2f convertToScreen2f(Vector3f vertex, int width, int height) {
        float x = (float) ((vertex.getX() + 1) * width / 2);
        float y = (float) ((1 - vertex.getY()) * height / 2);

        // Ограничиваем координаты
        x = Math.max(0, Math.min(x, width - 1));
        y = Math.max(0, Math.min(y, height - 1));

        return new Vector2f(x, y);
    }

    public Vector3f applyTransformations(Vector3f vertex,
                                         Vector3f translation,
                                         Vector3f rotation,
                                         Vector3f scale) {
        // Применяем масштабирование
        Vector3f transformed = new Vector3f(
                vertex.getX() * scale.getX(),
                vertex.getY() * scale.getY(),
                vertex.getZ() * scale.getZ()
        );

        // Применяем вращение (упрощенное)
        // TODO: Реализовать матричное вращение

        // Применяем перемещение
        transformed.setX(transformed.getX() + translation.getX());
        transformed.setY(transformed.getY() + translation.getY());
        transformed.setZ(transformed.getZ() + translation.getZ());

        return transformed;
    }
}
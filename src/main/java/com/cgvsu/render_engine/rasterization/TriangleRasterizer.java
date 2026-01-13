package com.cgvsu.render_engine.rasterization;

//import com.cgvsu.math.Vector2f;
//import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class TriangleRasterizer {
    public static final float EPSILON = 1e-6f;

    public void rasterizeTriangle(
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f v1, Vector3f v2, Vector3f v3,
            GraphicsContext gc, ZBuffer zBuffer, Color fillColor) {

        // Находим ограничивающий прямоугольник
        int minX = Math.max(0, (int) Math.min(Math.min(p1.x, p2.x), p3.x));
        int maxX = Math.min((int) gc.getCanvas().getWidth() - 1, (int) Math.max(Math.max(p1.x, p2.x), p3.x));
        int minY = Math.max(0, (int) Math.min(Math.min(p1.y, p2.y), p3.y));
        int maxY = Math.min((int) gc.getCanvas().getHeight() - 1, (int) Math.max(Math.max(p1.y, p2.y), p3.y));
        // Вычисляем площадь треугольника (удвоенную)
        float area = edgeFunction(p1, p2, p3);

        if (Math.abs(area) < EPSILON) {
            return; // Вырожденный треугольник
        }

        // Проходим по всем пикселям в ограничивающем прямоугольнике
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector2f p = new Vector2f(x, y);

                // Вычисляем барицентрические координаты
                float w1 = edgeFunction(p2, p3, p) / area;
                float w2 = edgeFunction(p3, p1, p) / area;
                float w3 = edgeFunction(p1, p2, p) / area;

                // Проверяем, находится ли точка внутри треугольника
                if (w1 >= -EPSILON && w2 >= -EPSILON && w3 >= -EPSILON) {
                    // Интерполируем Z-координату
                    float z = (float) (w1 * v1.z + w2 * v2.z + w3 * v3.z);

                    // Проверяем Z-буфер и рисуем пиксель
                    if (zBuffer.testAndSet(x, y, z)) {
                        gc.getPixelWriter().setColor(x, y, fillColor);
                    }
                }
            }
        }
    }

    private float edgeFunction(Vector2f a, Vector2f b, Vector2f c) {
        return (float) ((c.x - a.x) * (b.y - a.y) -
                        (c.y - a.y) * (b.x - a.x));
    }
}
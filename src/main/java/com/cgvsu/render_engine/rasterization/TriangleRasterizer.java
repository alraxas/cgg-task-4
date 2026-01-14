package com.cgvsu.render_engine.rasterization;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

//import javax.vecmath.Vector2f;
//import javax.vecmath.Vector3f;

public class TriangleRasterizer {
    public static final float EPSILON = 1e-6f;

    public void rasterizeTriangle(
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f v1, Vector3f v2, Vector3f v3,
            GraphicsContext gc, ZBuffer zBuffer, Color fillColor) {

        // Находим ограничивающий прямоугольник
        int minX = Math.max(0, (int) Math.min(Math.min(p1.getX(), p2.getX()), p3.getX()));
        int maxX = Math.min((int) gc.getCanvas().getWidth() - 1, (int) Math.max(Math.max(p1.getX(), p2.getX()), p3.getX()));
        int minY = Math.max(0, (int) Math.min(Math.min(p1.getY(), p2.getY()), p3.getY()));
        int maxY = Math.min((int) gc.getCanvas().getHeight() - 1, (int) Math.max(Math.max(p1.getY(), p2.getY()), p3.getY()));
        // Вычисляем площадь треугольника (удвоенную)
        double area = Vector2f.edgeFunction(p1, p2, p3);

        if (Math.abs(area) < EPSILON) {
            return; // Вырожденный треугольник
        }

        // Проходим по всем пикселям в ограничивающем прямоугольнике
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector2f p = new Vector2f(x, y);

                // Вычисляем барицентрические координаты
                double w1 = Vector2f.edgeFunction(p2, p3, p) / area;
                double w2 = Vector2f.edgeFunction(p3, p1, p) / area;
                double w3 = Vector2f.edgeFunction(p1, p2, p) / area;

                // Проверяем, находится ли точка внутри треугольника
                if (w1 >= -EPSILON && w2 >= -EPSILON && w3 >= -EPSILON) {
                    double z = (w1 * v1.getZ() + w2 * v2.getZ() + w3 * v3.getZ());

                    // Проверяем Z-буфер и рисуем пиксель
                    if (zBuffer.testAndSet(x, y, z)) {
                        gc.getPixelWriter().setColor(x, y, fillColor);
                    }
                }
            }
        }
    }
}
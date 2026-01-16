package com.cgvsu.render_engine.rasterization;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.lighting.*;
import com.cgvsu.render_engine.texture.Texture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.Comparator;

public class TriangleRasterizer {
    /**
     * Растеризация треугольника с Z-буфером (базовый метод)
     */
    public void rasterizeTriangleWithZBuffer(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            GraphicsContext gc, ZBuffer zBuffer, Color color) {

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        // Преобразуем в экранные координаты
        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        // Вычисляем барицентрические координаты для всей области
        float area = edgeFunction(x1, y1, x2, y2, x3, y3);

        if (Math.abs(area) < 0.0001f) return; // Вырожденный треугольник

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // Барицентрические координаты
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                // Проверяем, находится ли точка внутри треугольника
                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    // Интерполяция Z
                    float z = z1 * w1 + z2 * w2 + z3 * w3;

                    // Проверка Z-буфера
                    if (zBuffer.testAndSet(x, y, z)) {
                        pw.setColor(x, y, color);
                    }
                }
            }
        }
    }

    /**
     * Растеризация текстурированного треугольника с Z-буфером
     */
    public void rasterizeTexturedTriangleWithZBuffer(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            Vector2f uv1, Vector2f uv2, Vector2f uv3,
            Texture texture, boolean bilinearFiltering,
            GraphicsContext gc, ZBuffer zBuffer) {

        if (texture == null) return;

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    float z = z1 * w1 + z2 * w2 + z3 * w3;

                    if (zBuffer.testAndSet(x, y, z)) {
                        // Интерполяция текстурных координат
                        float u = (float) (uv1.getX() * w1 + uv2.getX() * w2 + uv3.getX() * w3);
                        float v = (float) (uv1.getY() * w1 + uv2.getY() * w2 + uv3.getY() * w3);

                        // Получение цвета текстуры
                        Color texColor = bilinearFiltering ?
                                texture.getColorBilinear(u, v) :
                                texture.getColor(u, v);

                        pw.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    /**
     * Растеризация освещенного треугольника с Z-буфером
     */
    public void rasterizeLitTriangleWithZBuffer(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material, SceneLighting sceneLighting,
            boolean smoothShading,
            GraphicsContext gc, ZBuffer zBuffer) {

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    float z = z1 * w1 + z2 * w2 + z3 * w3;

                    if (zBuffer.testAndSet(x, y, z)) {
                        // Интерполяция мировых координат и нормалей
                        Vector3f worldPos = interpolateVector3(world1, world2, world3, w1, w2, w3);
                        Vector3f normal;

                        if (smoothShading) {
                            normal = interpolateVector3(n1, n2, n3, w1, w2, w3).normalize();
                        } else {
                            // Flat shading - используем нормаль первого треугольника
                            normal = n1;
                        }

                        // Расчет освещения
                        Color baseColor = material.getBaseColor();
                        Color finalColor = sceneLighting.calculateLighting(
                                material, worldPos, normal,
                                new Vector3f(0, 0, -1), // Направление взгляда (упрощенно)
                                baseColor
                        );

                        pw.setColor(x, y, finalColor);
                    }
                }
            }
        }
    }

    /**
     * Растеризация текстурированного и освещенного треугольника с Z-буфером
     */
    public void rasterizeLitTexturedTriangleWithZBuffer(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Vector2f uv1, Vector2f uv2, Vector2f uv3,
            Material material, SceneLighting sceneLighting,
            boolean smoothShading, boolean bilinearFiltering,
            GraphicsContext gc, ZBuffer zBuffer) {

        if (!material.hasTexture()) {
            // Если нет текстуры, рисуем с освещением но без текстуры
            rasterizeLitTriangleWithZBuffer(p1, p2, p3, z1, z2, z3,
                    world1, world2, world3, n1, n2, n3,
                    material, sceneLighting, smoothShading, gc, zBuffer);
            return;
        }

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    float z = z1 * w1 + z2 * w2 + z3 * w3;

                    if (zBuffer.testAndSet(x, y, z)) {
                        // Интерполяция текстурных координат
                        float u = (float) (uv1.getX() * w1 + uv2.getX() * w2 + uv3.getX() * w3);
                        float v = (float) (uv1.getY() * w1 + uv2.getY() * w2 + uv3.getY() * w3);

                        // Получение цвета текстуры
                        Color texColor = bilinearFiltering ?
                                material.getDiffuseTexture().getColorBilinear(u, v) :
                                material.getDiffuseTexture().getColor(u, v);

                        // Интерполяция мировых координат и нормалей
                        Vector3f worldPos = interpolateVector3(world1, world2, world3, w1, w2, w3);
                        Vector3f normal;

                        if (smoothShading) {
                            normal = interpolateVector3(n1, n2, n3, w1, w2, w3).normalize();
                        } else {
                            normal = n1;
                        }

                        // Расчет освещения с учетом текстуры
                        Color finalColor = sceneLighting.calculateLighting(
                                material, worldPos, normal,
                                new Vector3f(0, 0, -1),
                                texColor
                        );

                        pw.setColor(x, y, finalColor);
                    }
                }
            }
        }
    }

    // ==================== МЕТОДЫ БЕЗ Z-БУФЕРА (ОПТИМИЗАЦИЯ) ====================

    /**
     * Простая растеризация треугольника без Z-буфера
     */
    public void rasterizeTriangle(
            Vector2f p1, Vector2f p2, Vector2f p3,
            GraphicsContext gc, Color color) {

        gc.setFill(color);
        gc.fillPolygon(
                new double[]{p1.getX(), p2.getX(), p3.getX()},
                new double[]{p1.getY(), p2.getY(), p3.getY()},
                3
        );
    }

    /**
     * Растеризация текстурированного треугольника без Z-буфера
     */
    public void rasterizeTexturedTriangle(
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector2f uv1, Vector2f uv2, Vector2f uv3,
            Texture texture, boolean bilinearFiltering,
            GraphicsContext gc) {

        if (texture == null) return;

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    // Интерполяция текстурных координат
                    float u = (float) (uv1.getX() * w1 + uv2.getX() * w2 + uv3.getX() * w3);
                    float v = (float) (uv1.getY() * w1 + uv2.getY() * w2 + uv3.getY() * w3);

                    // Получение цвета текстуры
                    Color texColor = bilinearFiltering ?
                            texture.getColorBilinear(u, v) :
                            texture.getColor(u, v);

                    pw.setColor(x, y, texColor);
                }
            }
        }
    }

    /**
     * Растеризация освещенного треугольника без Z-буфера
     */
    public void rasterizeLitTriangle(
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material, SceneLighting sceneLighting,
            boolean smoothShading,
            GraphicsContext gc) {

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    // Интерполяция мировых координат и нормалей
                    Vector3f worldPos = interpolateVector3(world1, world2, world3, w1, w2, w3);
                    Vector3f normal;

                    if (smoothShading) {
                        normal = interpolateVector3(n1, n2, n3, w1, w2, w3).normalize();
                    } else {
                        normal = n1;
                    }

                    // Расчет освещения
                    Color baseColor = material.getBaseColor();
                    Color finalColor = sceneLighting.calculateLighting(
                            material, worldPos, normal,
                            new Vector3f(0, 0, -1),
                            baseColor
                    );

                    pw.setColor(x, y, finalColor);
                }
            }
        }
    }

    /**
     * Растеризация текстурированного и освещенного треугольника без Z-буфера
     */
    public void rasterizeLitTexturedTriangle(
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Vector2f uv1, Vector2f uv2, Vector2f uv3,
            Material material, SceneLighting sceneLighting,
            boolean smoothShading, boolean bilinearFiltering,
            GraphicsContext gc) {

        if (!material.hasTexture()) {
            rasterizeLitTriangle(p1, p2, p3, world1, world2, world3,
                    n1, n2, n3, material, sceneLighting, smoothShading, gc);
            return;
        }

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    // Интерполяция текстурных координат
                    float u = (float) (uv1.getX() * w1 + uv2.getX() * w2 + uv3.getX() * w3);
                    float v = (float) (uv1.getY() * w1 + uv2.getY() * w2 + uv3.getY() * w3);

                    // Получение цвета текстуры
                    Color texColor = bilinearFiltering ?
                            material.getDiffuseTexture().getColorBilinear(u, v) :
                            material.getDiffuseTexture().getColor(u, v);

                    // Интерполяция мировых координат и нормалей
                    Vector3f worldPos = interpolateVector3(world1, world2, world3, w1, w2, w3);
                    Vector3f normal;

                    if (smoothShading) {
                        normal = interpolateVector3(n1, n2, n3, w1, w2, w3).normalize();
                    } else {
                        normal = n1;
                    }

                    // Расчет освещения с учетом текстуры
                    Color finalColor = sceneLighting.calculateLighting(
                            material, worldPos, normal,
                            new Vector3f(0, 0, -1),
                            texColor
                    );

                    pw.setColor(x, y, finalColor);
                }
            }
        }
    }

    // ==================== СПЕЦИАЛЬНЫЕ МЕТОДЫ (GOURAUD, PHONG) ====================

    /**
     * Растеризация треугольника с затенением по Гуро
     */
    public void rasterizeTriangleGouraud(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material, SceneLighting sceneLighting,
            GraphicsContext gc, ZBuffer zBuffer) {

        // Вычисляем освещение в вершинах
        Color c1 = calculateVertexLighting(world1, n1, material, sceneLighting);
        Color c2 = calculateVertexLighting(world2, n2, material, sceneLighting);
        Color c3 = calculateVertexLighting(world3, n3, material, sceneLighting);

        // Интерполируем цвет по треугольнику
        rasterizeTriangleWithColorInterpolation(p1, p2, p3, z1, z2, z3, c1, c2, c3, gc, zBuffer);
    }

    /**
     * Растеризация текстурированного треугольника с затенением по Гуро
     */
    public void rasterizeTexturedTriangleGouraud(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Vector2f uv1, Vector2f uv2, Vector2f uv3,
            Texture texture, Material material, SceneLighting sceneLighting,
            boolean bilinearFiltering,
            GraphicsContext gc, ZBuffer zBuffer) {

        if (texture == null) {
            rasterizeTriangleGouraud(p1, p2, p3, z1, z2, z3,
                    world1, world2, world3, n1, n2, n3,
                    material, sceneLighting, gc, zBuffer);
            return;
        }

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        // Вычисляем освещение в вершинах
        Color c1 = calculateVertexLighting(world1, n1, material, sceneLighting);
        Color c2 = calculateVertexLighting(world2, n2, material, sceneLighting);
        Color c3 = calculateVertexLighting(world3, n3, material, sceneLighting);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    float z = z1 * w1 + z2 * w2 + z3 * w3;

                    if (zBuffer.testAndSet(x, y, z)) {
                        // Интерполяция текстурных координат
                        float u = (float) (uv1.getX() * w1 + uv2.getX() * w2 + uv3.getX() * w3);
                        float v = (float) (uv1.getY() * w1 + uv2.getY() * w2 + uv3.getY() * w3);

                        // Получение цвета текстуры
                        Color texColor = bilinearFiltering ?
                                texture.getColorBilinear(u, v) :
                                texture.getColor(u, v);

                        // Интерполяция освещения (Гуро)
                        Color lightColor = interpolateColor(c1, c2, c3, w1, w2, w3);

                        // Комбинирование текстуры и освещения
                        Color finalColor = Color.color(
                                Math.min(1.0, texColor.getRed() * lightColor.getRed()),
                                Math.min(1.0, texColor.getGreen() * lightColor.getGreen()),
                                Math.min(1.0, texColor.getBlue() * lightColor.getBlue()),
                                texColor.getOpacity()
                        );

                        pw.setColor(x, y, finalColor);
                    }
                }
            }
        }
    }

    /**
     * Растеризация треугольника с интерполяцией цвета (для Гуро)
     */
    private void rasterizeTriangleWithColorInterpolation(
            Vector2f p1, Vector2f p2, Vector2f p3,
            float z1, float z2, float z3,
            Color c1, Color c2, Color c3,
            GraphicsContext gc, ZBuffer zBuffer) {

        PixelWriter pw = gc.getPixelWriter();
        int[] bounds = calculateBoundingBox(p1, p2, p3);

        int minX = bounds[0];
        int minY = bounds[1];
        int maxX = bounds[2];
        int maxY = bounds[3];

        int x1 = (int) p1.getX(), y1 = (int) p1.getY();
        int x2 = (int) p2.getX(), y2 = (int) p2.getY();
        int x3 = (int) p3.getX(), y3 = (int) p3.getY();

        float area = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(area) < 0.0001f) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y) / area;
                float w2 = edgeFunction(x3, y3, x1, y1, x, y) / area;
                float w3 = edgeFunction(x1, y1, x2, y2, x, y) / area;

                if (w1 >= -0.0001f && w2 >= -0.0001f && w3 >= -0.0001f) {
                    float z = z1 * w1 + z2 * w2 + z3 * w3;

                    if (zBuffer.testAndSet(x, y, z)) {
                        // Интерполяция цвета
                        Color finalColor = interpolateColor(c1, c2, c3, w1, w2, w3);
                        pw.setColor(x, y, finalColor);
                    }
                }
            }
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Вычисление ограничивающего прямоугольника треугольника
     */
    private int[] calculateBoundingBox(Vector2f p1, Vector2f p2, Vector2f p3) {
        int minX = (int) Math.max(0, Math.floor(Math.min(p1.getX(), Math.min(p2.getX(), p3.getX()))));
        int minY = (int) Math.max(0, Math.floor(Math.min(p1.getY(), Math.min(p2.getY(), p3.getY()))));
        int maxX = (int) Math.min(Integer.MAX_VALUE, Math.ceil(Math.max(p1.getX(), Math.max(p2.getX(), p3.getX()))));
        int maxY = (int) Math.min(Integer.MAX_VALUE, Math.ceil(Math.max(p1.getY(), Math.max(p2.getY(), p3.getY()))));

        return new int[]{minX, minY, maxX, maxY};
    }

    /**
     * Функция определения ориентации ребра (edge function)
     */
    private float edgeFunction(int ax, int ay, int bx, int by, int cx, int cy) {
        return (cx - ax) * (by - ay) - (cy - ay) * (bx - ax);
    }

    /**
     * Интерполяция вектора
     */
    private Vector3f interpolateVector3(Vector3f v1, Vector3f v2, Vector3f v3, float w1, float w2, float w3) {
        float x = (float) (v1.getX() * w1 + v2.getX() * w2 + v3.getX() * w3);
        float y = (float) (v1.getY() * w1 + v2.getY() * w2 + v3.getY() * w3);
        float z = v1.getZ() * w1 + v2.getZ() * w2 + v3.getZ() * w3;
        return new Vector3f(x, y, z);
    }

    /**
     * Интерполяция цвета
     */
    private Color interpolateColor(Color c1, Color c2, Color c3, float w1, float w2, float w3) {
        double r = c1.getRed() * w1 + c2.getRed() * w2 + c3.getRed() * w3;
        double g = c1.getGreen() * w1 + c2.getGreen() * w2 + c3.getGreen() * w3;
        double b = c1.getBlue() * w1 + c2.getBlue() * w2 + c3.getBlue() * w3;
        double a = c1.getOpacity() * w1 + c2.getOpacity() * w2 + c3.getOpacity() * w3;

        return Color.color(
                Math.max(0, Math.min(1, r)),
                Math.max(0, Math.min(1, g)),
                Math.max(0, Math.min(1, b)),
                Math.max(0, Math.min(1, a))
        );
    }

    /**
     * Расчет освещения в вершине
     */
    private Color calculateVertexLighting(
            Vector3f position,
            Vector3f normal,
            Material material,
            SceneLighting sceneLighting) {

        return sceneLighting.calculateLighting(
                material, position, normal.normalize(),
                new Vector3f(0, 0, -1), // Направление взгляда
                material.getBaseColor()
        );
    }

    /**
     * Сортировка вершин по Y
     */
    private Vector2f[] sortVerticesByY(Vector2f p1, Vector2f p2, Vector2f p3) {
        Vector2f[] vertices = {p1, p2, p3};
        Arrays.sort(vertices, Comparator.comparingDouble(Vector2f::getY));
        return vertices;
    }

    /**
     * Проверка, является ли треугольник валидным для рендеринга
     */
    public boolean isValidTriangle(Vector2f p1, Vector2f p2, Vector2f p3) {
        // Проверка на вырожденность
        float area = (float) ((p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) -
                        (p2.getY() - p1.getY()) * (p3.getX() - p1.getX()));

        return Math.abs(area) > 0.0001f;
    }

    /**
     * Метод для отладки - рисует контур треугольника
     */
    public void drawTriangleOutline(
            Vector2f p1, Vector2f p2, Vector2f p3,
            GraphicsContext gc, Color color) {

        gc.setStroke(color);
        gc.setLineWidth(1);
        gc.strokePolygon(
                new double[]{p1.getX(), p2.getX(), p3.getX()},
                new double[]{p1.getY(), p2.getY(), p3.getY()},
                3
        );
    }
}
package com.cgvsu.render_engine;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.rasterization.TriangleRasterizer;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;
import static com.cgvsu.render_engine.rasterization.TriangleRasterizer.EPSILON;

public class RenderEngine {
    private ZBuffer zBuffer;
    private TriangleRasterizer rasterizer;

    public RenderEngine() {
        this.rasterizer = new TriangleRasterizer();
    }

    public static void renderWireframe(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height) {
        // Существующий метод рендеринга каркаса
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                javax.vecmath.Vector3f vertexVecmath = new javax.vecmath.Vector3f(
                        (float) vertex.getX(),
                        (float) vertex.getY(),
                        (float) vertex.getZ()
                );

                Point2f resultPoint = vertexToPoint(
                        multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath),
                        width, height
                );
                resultPoints.add(resultPoint);
            }

            // Рисуем линии полигона
            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                graphicsContext.strokeLine(
                        resultPoints.get(vertexInPolygonInd - 1).x,
                        resultPoints.get(vertexInPolygonInd - 1).y,
                        resultPoints.get(vertexInPolygonInd).x,
                        resultPoints.get(vertexInPolygonInd).y
                );
            }

            if (nVerticesInPolygon > 0) {
                graphicsContext.strokeLine(
                        resultPoints.get(nVerticesInPolygon - 1).x,
                        resultPoints.get(nVerticesInPolygon - 1).y,
                        resultPoints.get(0).x,
                        resultPoints.get(0).y
                );
            }
        }
    }

    public void renderSolid(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Color fillColor) {

        // Инициализируем Z-буфер
        zBuffer = new ZBuffer(width, height);

        // Очищаем холст
        graphicsContext.clearRect(0, 0, width, height);

        // Получаем матрицы преобразования
        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
        modelViewMatrix.mul(viewMatrix);

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelViewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // Проходим по всем полигонам (должны быть треугольниками после триангуляции)
        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            Polygon polygon = mesh.polygons.get(polygonInd);
            List<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() != 3) {
                continue; // Пропускаем не треугольники
            }

            // Получаем вершины треугольника
            Vector3f v1 = mesh.vertices.get(vertexIndices.get(0));
            Vector3f v2 = mesh.vertices.get(vertexIndices.get(1));
            Vector3f v3 = mesh.vertices.get(vertexIndices.get(2));

            // Преобразуем в vecmath.Vector3f
            javax.vecmath.Vector3f vec1 = new javax.vecmath.Vector3f(
                    (float) v1.getX(), (float) v1.getY(), (float) v1.getZ()
            );
            javax.vecmath.Vector3f vec2 = new javax.vecmath.Vector3f(
                    (float) v2.getX(), (float) v2.getY(), (float) v2.getZ()
            );
            javax.vecmath.Vector3f vec3 = new javax.vecmath.Vector3f(
                    (float) v3.getX(), (float) v3.getY(), (float) v3.getZ()
            );

            // Применяем преобразования
            javax.vecmath.Vector3f transformed1 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1);
            javax.vecmath.Vector3f transformed2 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2);
            javax.vecmath.Vector3f transformed3 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec3);

            // Преобразуем в экранные координаты
            Point2f screen1 = vertexToPoint(transformed1, width, height);
            Point2f screen2 = vertexToPoint(transformed2, width, height);
            Point2f screen3 = vertexToPoint(transformed3, width, height);

            // Преобразуем Point2f в Vector2f
            javax.vecmath.Vector2f p1 = new javax.vecmath.Vector2f(screen1.x, screen1.y);
            javax.vecmath.Vector2f p2 = new javax.vecmath.Vector2f(screen2.x, screen2.y);
            javax.vecmath.Vector2f p3 = new javax.vecmath.Vector2f(screen3.x, screen3.y);

            // Растеризуем треугольник
            //TODO: заметить классы после добавления метода multiplyMatrix4ByVector3
            rasterizer.rasterizeTriangle(p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    graphicsContext, zBuffer, fillColor);
        }
    }

    // Метод для рендеринга с освещением (основываясь на нормалях)
    public void renderWithLighting(
            final GraphicsContext graphicsContext,
            final Camera camera,
            final Model mesh,
            final int width,
            final int height,
            final Color baseColor,
            final javax.vecmath.Vector3f lightDirection) {

        zBuffer = new ZBuffer(width, height);
        graphicsContext.clearRect(0, 0, width, height);

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
        modelViewMatrix.mul(viewMatrix);

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelViewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // Нормальная матрица (транспонированная обратная к model-view матрице)
        Matrix4f normalMatrix = new Matrix4f(modelViewMatrix);
        normalMatrix.invert();
        normalMatrix.transpose();

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            Polygon polygon = mesh.polygons.get(polygonInd);
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            if (vertexIndices.size() != 3 || normalIndices.size() != 3) {
                continue;
            }

            // Вершины
            Vector3f v1 = mesh.vertices.get(vertexIndices.get(0));
            Vector3f v2 = mesh.vertices.get(vertexIndices.get(1));
            Vector3f v3 = mesh.vertices.get(vertexIndices.get(2));

            // Нормали
            Vector3f n1 = mesh.normals.get(normalIndices.get(0));
            Vector3f n2 = mesh.normals.get(normalIndices.get(1));
            Vector3f n3 = mesh.normals.get(normalIndices.get(2));

            javax.vecmath.Vector3f vec1 = new javax.vecmath.Vector3f(
                    (float) v1.getX(), (float) v1.getY(), (float) v1.getZ()
            );
            javax.vecmath.Vector3f vec2 = new javax.vecmath.Vector3f(
                    (float) v2.getX(), (float) v2.getY(), (float) v2.getZ()
            );
            javax.vecmath.Vector3f vec3 = new javax.vecmath.Vector3f(
                    (float) v3.getX(), (float) v3.getY(), (float) v3.getZ()
            );

            // Нормали в vecmath
            javax.vecmath.Vector3f norm1 = new javax.vecmath.Vector3f(
                    (float) n1.getX(), (float) n1.getY(), (float) n1.getZ()
            );
            javax.vecmath.Vector3f norm2 = new javax.vecmath.Vector3f(
                    (float) n2.getX(), (float) n2.getY(), (float) n2.getZ()
            );
            javax.vecmath.Vector3f norm3 = new javax.vecmath.Vector3f(
                    (float) n3.getX(), (float) n3.getY(), (float) n3.getZ()
            );

            // Преобразуем вершины и нормали
            javax.vecmath.Vector3f transformed1 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1);
            javax.vecmath.Vector3f transformed2 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2);
            javax.vecmath.Vector3f transformed3 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec3);

            javax.vecmath.Vector3f transformedNorm1 = multiplyMatrix4ByVector3(normalMatrix, norm1);
            javax.vecmath.Vector3f transformedNorm2 = multiplyMatrix4ByVector3(normalMatrix, norm2);
            javax.vecmath.Vector3f transformedNorm3 = multiplyMatrix4ByVector3(normalMatrix, norm3);

            // Нормализуем нормали
            transformedNorm1.normalize();
            transformedNorm2.normalize();
            transformedNorm3.normalize();

            // Экранные координаты
            Point2f screen1 = vertexToPoint(transformed1, width, height);
            Point2f screen2 = vertexToPoint(transformed2, width, height);
            Point2f screen3 = vertexToPoint(transformed3, width, height);

            Vector2f p1 = new Vector2f(screen1.x, screen1.y);
            Vector2f p2 = new Vector2f(screen2.x, screen2.y);
            Vector2f p3 = new Vector2f(screen3.x, screen3.y);

            // Вычисляем освещение для каждой вершины
            float light1 = Math.max(0, -transformedNorm1.dot(lightDirection));
            float light2 = Math.max(0, -transformedNorm2.dot(lightDirection));
            float light3 = Math.max(0, -transformedNorm3.dot(lightDirection));

            // Создаем модифицированный растеризатор для освещения
            rasterizeTriangleWithLighting(p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    light1, light2, light3,
                    graphicsContext, zBuffer, baseColor);
        }
    }

    private void rasterizeTriangleWithLighting(
            Vector2f p1, Vector2f p2, Vector2f p3,
            javax.vecmath.Vector3f v1, javax.vecmath.Vector3f v2, javax.vecmath.Vector3f v3,
            float light1, float light2, float light3,
            GraphicsContext gc, ZBuffer zBuffer, Color baseColor) {

        int minX = Math.max(0, (int) Math.min(Math.min(p1.getX(), p2.getX()), p3.getX()));
        int maxX = Math.min((int) gc.getCanvas().getWidth() - 1,
                (int) Math.max(Math.max(p1.getX(), p2.getX()), p3.getX()));
        int minY = Math.max(0, (int) Math.min(Math.min(p1.getY(), p2.getY()), p3.getY()));
        int maxY = Math.min((int) gc.getCanvas().getHeight() - 1,
                (int) Math.max(Math.max(p1.getY(), p2.getY()), p3.getY()));

        float area = edgeFunction(p1, p2, p3);
        if (Math.abs(area) < EPSILON) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector2f p = new Vector2f(x, y);

                float w1 = edgeFunction(p2, p3, p) / area;
                float w2 = edgeFunction(p3, p1, p) / area;
                float w3 = edgeFunction(p1, p2, p) / area;

                if (w1 >= -EPSILON && w2 >= -EPSILON && w3 >= -EPSILON) {
                    float z = w1 * v1.z + w2 * v2.z + w3 * v3.z;

                    if (zBuffer.testAndSet(x, y, z)) {
                        // Интерполируем освещение
                        float light = w1 * light1 + w2 * light2 + w3 * light3;
                        light = Math.max(0.2f, Math.min(1.0f, light)); // Ограничиваем

                        Color shadedColor = Color.color(
                                baseColor.getRed() * light,
                                baseColor.getGreen() * light,
                                baseColor.getBlue() * light
                        );

                        gc.getPixelWriter().setColor(x, y, shadedColor);
                    }
                }
            }
        }
    }

    private float edgeFunction(Vector2f a, Vector2f b, Vector2f c) {
        return (float) ((c.getX() - a.getX()) * (b.getY() - a.getY()) -
                        (c.getY() - a.getY()) * (b.getX() - a.getX()));
    }
}
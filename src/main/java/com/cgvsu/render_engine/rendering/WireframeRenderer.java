package com.cgvsu.render_engine.rendering;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.rotateScaleTranslate;

public class WireframeRenderer {

    public void renderWireframeWithZBuffer(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Color lineColor,
            float lineThickness,
            ZBuffer zBuffer) {

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);

        // Настройка контекста
        gc.setStroke(lineColor);
        gc.setLineWidth(lineThickness);

        // Рендерим все полигоны
        for (Polygon polygon : mesh.getPolygons()) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            int nVertices = vertexIndices.size();

            if (nVertices < 2) continue;

            // Получаем преобразованные вершины
            List<Point2f> screenPoints = new ArrayList<>();
            List<Float> depths = new ArrayList<>();

            for (int i = 0; i < nVertices; i++) {
                Vector3f vertex = mesh.getVertices().get(vertexIndices.get(i));
                Vector3f transformed = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                Point2f screenPoint = Vector2f.vertexToPoint(transformed, width, height);

                screenPoints.add(screenPoint);
                depths.add(transformed.getZ());
            }

            // Рисуем линии полигона с Z-буфером
            for (int i = 0; i < nVertices; i++) {
                int next = (i + 1) % nVertices;
                drawLineWithZBuffer(
                        screenPoints.get(i), depths.get(i),
                        screenPoints.get(next), depths.get(next),
                        gc, lineColor, zBuffer
                );
            }
        }
    }

    private void drawLineWithZBuffer(
            Point2f p1, float z1,
            Point2f p2, float z2,
            GraphicsContext gc,
            Color color,
            ZBuffer zBuffer) {

        int x1 = (int) Math.round(p1.x);
        int y1 = (int) Math.round(p1.y);
        int x2 = (int) Math.round(p2.x);
        int y2 = (int) Math.round(p2.y);

        // Используем алгоритм Брезенхема с Z-буфером
        bresenhamWithZBuffer(x1, y1, z1, x2, y2, z2, gc, color, zBuffer);
    }

    private void bresenhamWithZBuffer(
            int x1, int y1, float z1,
            int x2, int y2, float z2,
            GraphicsContext gc,
            Color color,
            ZBuffer zBuffer) {

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;

        int err = dx - dy;
        int e2;
        int x = x1;
        int y = y1;

        float t = 0;
        float step = 1.0f / Math.max(dx, dy);

        while (true) {
            // Линейная интерполяция Z
            float z = z1 * (1 - t) + z2 * t;

            // Проверка Z-буфера
            if (zBuffer == null || zBuffer.testAndSet(x, y, z)) {
                gc.getPixelWriter().setColor(x, y, color);
            }

            if (x == x2 && y == y2) break;

            e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }

            t += step;
        }
    }
}
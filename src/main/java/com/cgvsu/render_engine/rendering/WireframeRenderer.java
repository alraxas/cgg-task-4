package com.cgvsu.render_engine.rendering;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;

public class WireframeRenderer {
    private ZBuffer lineZBuffer;

    public void renderWireframe(
            GraphicsContext gc,
            Camera camera,
            Model mesh,
            int width,
            int height,
            Color lineColor) {

        lineZBuffer = new ZBuffer(width, height);

        Matrix4f modelMatrix = rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.mul(viewMatrix);
        modelViewProjectionMatrix.mul(projectionMatrix);

        // Рендерим все полигоны
        for (Polygon polygon : mesh.polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            int nVertices = vertexIndices.size();

            if (nVertices < 2) continue;

            // Получаем преобразованные вершины
            List<Point2f> screenPoints = new ArrayList<>();
            List<Float> depths = new ArrayList<>();

            for (int i = 0; i < nVertices; i++) {
                Vector3f vertex = mesh.vertices.get(vertexIndices.get(i));
                javax.vecmath.Vector3f vec = new javax.vecmath.Vector3f(
                        (float) vertex.getX(),
                        (float) vertex.getY(),
                        (float) vertex.getZ()
                );

                javax.vecmath.Vector3f transformed = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec);
                Point2f screenPoint = Vector2f.vertexToPoint(transformed, width, height);

                screenPoints.add(screenPoint);
                depths.add(transformed.z);
            }

            // Рисуем линии полигона
            for (int i = 0; i < nVertices; i++) {
                int next = (i + 1) % nVertices;
                drawLineWithDepth(
                        screenPoints.get(i), depths.get(i),
                        screenPoints.get(next), depths.get(next),
                        gc, lineColor
                );
            }
        }
    }

    private void drawLineWithDepth(
            Point2f p1, float z1,
            Point2f p2, float z2,
            GraphicsContext gc,
            Color color) {

        // Алгоритм Брезенхема с Z-буфером
        int x1 = (int) p1.x;
        int y1 = (int) p1.y;
        int x2 = (int) p2.x;
        int y2 = (int) p2.y;

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
            // Интерполируем Z
            float z = z1 * (1 - t) + z2 * t;

            // Проверяем Z-буфер
            if (lineZBuffer.testAndSet(x, y, z)) {
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
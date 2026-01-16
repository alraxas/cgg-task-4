package com.cgvsu.render_engine.rendering;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.texture.Texture;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.util.List;

import static com.cgvsu.math.Matrix4f.multiplyMatrix4ByVector3;

public class SimpleRenderer {

    public void render(
            GraphicsContext gc,
            Camera camera,
            Model model,
            int width,
            int height,
            Color solidColor,
            boolean drawWireframe,
            boolean useLighting,
            Texture texture) {

        // Очистка
        gc.clearRect(0, 0, width, height);

        if (model == null || model.getVertices() == null || model.getVertices().isEmpty()) {
            return;
        }

        // Матрицы преобразования
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);

        List<Polygon> polygons = model.getPolygons();
        List<Vector3f> vertices = model.getVertices();

        // Если только каркас
        if (drawWireframe && !useLighting && texture == null) {
            renderWireframe(gc, model, modelViewProjectionMatrix, width, height, Color.BLACK);
            return;
        }

        // Рендерим треугольники
        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() != 3) continue;

            // Вершины
            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));

            // Преобразуем вершины
            Vector3f transformed1 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, v1);
            Vector3f transformed2 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, v2);
            Vector3f transformed3 = multiplyMatrix4ByVector3(modelViewProjectionMatrix, v3);

            // В экранные координаты
            Point2f screen1 = Vector2f.vertexToPoint(transformed1, width, height);
            Point2f screen2 = Vector2f.vertexToPoint(transformed2, width, height);
            Point2f screen3 = Vector2f.vertexToPoint(transformed3, width, height);

            Vector2f p1 = new Vector2f(screen1.x, screen1.y);
            Vector2f p2 = new Vector2f(screen2.x, screen2.y);
            Vector2f p3 = new Vector2f(screen3.x, screen3.y);

            // Определяем цвет
            Color triangleColor = solidColor;

            // Применяем освещение (упрощенное)
            if (useLighting) {
                // Простое затемнение для эффекта освещения
                triangleColor = triangleColor.deriveColor(0, 1.0, 0.7, 1.0);
            }

            // Если есть текстура, используем цвет текстуры
            if (texture != null) {
                // Упрощенная текстура - берем цвет из центра текстуры
                Color texColor = texture.getColor(0.5f, 0.5f);
                triangleColor = texColor;

                // Если включено освещение, смешиваем
                if (useLighting) {
                    triangleColor = Color.color(
                            triangleColor.getRed() * 0.8,
                            triangleColor.getGreen() * 0.8,
                            triangleColor.getBlue() * 0.8
                    );
                }
            }

            // Рисуем треугольник
            renderSolidTriangle(gc, p1, p2, p3, triangleColor);

            // Если нужен каркас поверх
            if (drawWireframe) {
                renderTriangleWireframe(gc, p1, p2, p3, Color.BLACK);
            }
        }
    }

    private void renderSolidTriangle(GraphicsContext gc, Vector2f p1, Vector2f p2, Vector2f p3, Color color) {
        gc.setFill(color);
        gc.fillPolygon(
                new double[]{p1.getX(), p2.getX(), p3.getX()},
                new double[]{p1.getY(), p2.getY(), p3.getY()},
                3
        );
    }

    private void renderTriangleWireframe(GraphicsContext gc, Vector2f p1, Vector2f p2, Vector2f p3, Color color) {
        gc.setStroke(color);
        gc.setLineWidth(1.0);
        gc.strokePolygon(
                new double[]{p1.getX(), p2.getX(), p3.getX()},
                new double[]{p1.getY(), p2.getY(), p3.getY()},
                3
        );
    }

    private void renderWireframe(GraphicsContext gc, Model model,
                                 Matrix4f mvpMatrix, int width, int height, Color color) {

        List<Polygon> polygons = model.getPolygons();
        List<Vector3f> vertices = model.getVertices();

        gc.setStroke(color);
        gc.setLineWidth(1.0);

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            int n = vertexIndices.size();

            if (n < 2) continue;

            // Сохраняем преобразованные точки
            Point2f[] points = new Point2f[n];
            for (int i = 0; i < n; i++) {
                Vector3f vertex = vertices.get(vertexIndices.get(i));
                Vector3f transformed = multiplyMatrix4ByVector3(mvpMatrix, vertex);
                points[i] = Vector2f.vertexToPoint(transformed, width, height);
            }

            // Рисуем линии полигона
            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                gc.strokeLine(points[i].x, points[i].y, points[next].x, points[next].y);
            }
        }
    }
}
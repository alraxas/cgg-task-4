package com.cgvsu.render_engine.rendering;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.GraphicConveyor;
import com.cgvsu.render_engine.lighting.*;
import com.cgvsu.render_engine.rasterization.TriangleRasterizer;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import com.cgvsu.render_engine.texture.Texture;
import com.cgvsu.render_engine.texture.TextureManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.util.List;

import static com.cgvsu.math.Matrix4f.multiplyMatrix4ByVector3;
import static com.cgvsu.math.Matrix4f.vertexToPoint;

public class UnifiedRenderer {
    private final TriangleRasterizer triangleRasterizer;
    private final WireframeRenderer wireframeRenderer;
    private final SceneLighting sceneLighting;
    private RenderSettings renderSettings;

    private static final float EPSILON = 0.0001f;

    private Canvas canvasBuffer;
    private GraphicsContext gcBuffer;
    private PixelWriter pixelWriter;

    // Кэши для оптимизации
    private ZBuffer triangleZBuffer;
    private ZBuffer wireframeZBuffer;
    private Matrix4f cachedViewProjectionMatrix;
    private Matrix4f cachedNormalMatrix;
    private Texture currentTexture;
    private Vector3f cameraPosition;

    public UnifiedRenderer() {
        this.triangleRasterizer = new TriangleRasterizer();
        this.wireframeRenderer = new WireframeRenderer();
        this.sceneLighting = new SceneLighting();
        this.renderSettings = new RenderSettings();
    }

    public UnifiedRenderer(RenderSettings settings) {
        this();
        this.renderSettings = settings;
    }

    public void render(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height) {

        // Проверяем, есть ли модель для рендеринга
        if (model == null || model.getVertices() == null || model.getVertices().isEmpty()) {
            graphicsContext.clearRect(0, 0, width, height);
            return;
        }
        this.cameraPosition = camera.getPosition();

        graphicsContext.clearRect(0, 0, width, height);

        // Инициализация Z-буферов если нужно
        if (renderSettings.isUseZBuffer()) {
            triangleZBuffer = new ZBuffer(width, height);
            wireframeZBuffer = new ZBuffer(width, height);
        }

        // Обновление освещения при движении камеры
        sceneLighting.updateForCamera(camera);

        // Получаем текущий режим для оптимизации
        RenderMode mode = renderSettings.getCurrentMode();

        // Оптимизация: если только каркас - рисуем только его
        if (mode == RenderMode.WIREFRAME) {
            renderWireframeOnly(graphicsContext, camera, model, width, height);
            return;
        }

        // Полный рендеринг
        renderFull(graphicsContext, camera, model, width, height, mode);
    }

    private void renderFull(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height,
            RenderMode mode) {

        // Предварительные вычисления матриц
        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
        modelViewMatrix.multiply(viewMatrix);

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelViewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);

        Matrix4f normalMatrix = new Matrix4f(modelViewMatrix);
        normalMatrix.invert();
        normalMatrix.transpose();

        // Кэшируем для повторного использования
        cachedViewProjectionMatrix = modelViewProjectionMatrix;
        cachedNormalMatrix = normalMatrix;

        // Рендерим треугольники (если нужно)
        if (mode != RenderMode.WIREFRAME) {
            renderTriangles(graphicsContext, model, width, height, mode);
        }

        // Рендерим каркас поверх (если нужно)
        if (renderSettings.isDrawWireframe()) {
            renderWireframeOverlay(graphicsContext, camera, model, width, height);
        }
    }

    private void renderTriangles(
            GraphicsContext graphicsContext,
            Model model,
            int width,
            int height,
            RenderMode mode) {


        List<Polygon> polygons = model.getPolygons();
        List<Vector3f> vertices = model.getVertices();
        List<Vector3f> normals = model.getNormals();
        List<Vector2f> textureVertices = model.getTextureVertices();

        // Подготовка материала
        Material material = new Material(renderSettings.getSolidColor());

        // Загрузка текстуры если нужно
        if (renderSettings.isUseTexture() && currentTexture != null) {
            material.setDiffuseTexture(currentTexture);
        }

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();

            // Пропускаем не треугольники
            if (vertexIndices.size() != 3) {
                continue;
            }

            // Отсечение задних граней
            if (renderSettings.isBackfaceCulling() && isBackface(polygon, vertices)) {
                continue;
            }

            // Получаем вершины
            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));

            // Преобразуем вершины
            Vector3f transformed1 = multiplyMatrix4ByVector3(cachedViewProjectionMatrix, v1);
            Vector3f transformed2 = multiplyMatrix4ByVector3(cachedViewProjectionMatrix, v2);
            Vector3f transformed3 = multiplyMatrix4ByVector3(cachedViewProjectionMatrix, v3);

            // Преобразуем в экранные координаты
            Point2f screen1 = vertexToPoint(transformed1, width, height);
            Point2f screen2 = vertexToPoint(transformed2, width, height);
            Point2f screen3 = vertexToPoint(transformed3, width, height);

            Vector2f p1 = new Vector2f(screen1.x, screen1.y);
            Vector2f p2 = new Vector2f(screen2.x, screen2.y);
            Vector2f p3 = new Vector2f(screen3.x, screen3.y);

            // Получаем нормали
            Vector3f n1, n2, n3;
            List<Integer> normalIndices = polygon.getNormalIndices();
            if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
                n1 = normals.get(normalIndices.get(0));
                n2 = normals.get(normalIndices.get(1));
                n3 = normals.get(normalIndices.get(2));
            } else {
                Vector3f flatNormal = Vector3f.calculatePolygonNormal(v1, v2, v3);
                n1 = flatNormal;
                n2 = flatNormal;
                n3 = flatNormal;
            }

            // Преобразуем нормали
            n1 = multiplyMatrix4ByVector3(cachedNormalMatrix, n1).normalize();
            n2 = multiplyMatrix4ByVector3(cachedNormalMatrix, n2).normalize();
            n3 = multiplyMatrix4ByVector3(cachedNormalMatrix, n3).normalize();

            // Получаем текстурные координаты
            Vector2f uv1 = null, uv2 = null, uv3 = null;
            if (renderSettings.isUseTexture() && !textureVertices.isEmpty()) {
                List<Integer> textureIndices = polygon.getTextureVertexIndices();
                if (textureIndices.size() >= 3) {
                    uv1 = textureVertices.get(textureIndices.get(0));
                    uv2 = textureVertices.get(textureIndices.get(1));
                    uv3 = textureVertices.get(textureIndices.get(2));
                }
            }

            // ВЫБОР РЕЖИМА РЕНДЕРИНГА
            switch (mode) {
                case SOLID:
                    renderSolidTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            material.getBaseColor());
                    break;

                case TEXTURED:
                    renderTexturedTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            material, uv1, uv2, uv3);
                    break;

                case LIT_SOLID:
                    renderLitTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            v1, v2, v3, n1, n2, n3,
                            material, null, null, null);
                    break;

                case LIT_TEXTURED:
                    renderLitTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            v1, v2, v3, n1, n2, n3,
                            material, uv1, uv2, uv3);
                    break;

                case WIREFRAME_LIT_SOLID:
                case ALL:
                    // Сначала рисуем закрашенный треугольник
                    renderLitTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            v1, v2, v3, n1, n2, n3,
                            material, uv1, uv2, uv3);
                    break;

                default:
                    renderSolidTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            material.getBaseColor());
                    break;
            }
        }
    }

    // ==================== БАЗОВЫЕ МЕТОДЫ РЕНДЕРИНГА ====================

    /**
     * Рендеринг сплошного треугольника с интерполяцией глубины
     */
    private void renderSolidTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Color color) {

        if (renderSettings.isUseZBuffer()) {
            renderSolidTriangleWithZBuffer(gc, p1, p2, p3, world1, world2, world3, color);
        } else {
            // Простая версия без Z-буфера
            gc.setFill(color);
            gc.fillPolygon(
                    new double[]{p1.getX(), p2.getX(), p3.getX()},
                    new double[]{p1.getY(), p2.getY(), p3.getY()},
                    3
            );
        }

        // Обводка
        if (renderSettings.getWireframeThickness() > 0) {
            gc.setStroke(renderSettings.getWireframeColor());
            gc.setLineWidth(0.5);
            gc.strokePolygon(
                    new double[]{p1.getX(), p2.getX(), p3.getX()},
                    new double[]{p1.getY(), p2.getY(), p3.getY()},
                    3
            );
        }
    }

    /**
     * Рендеринг сплошного треугольника с Z-буфером
     */
    private void renderSolidTriangleWithZBuffer(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Color color) {

        // Преобразуем координаты для удобства
        float x1 = p1.getX();
        float y1 = p1.getY();
        float x2 = p2.getX();
        float y2 = p2.getY();
        float x3 = p3.getX();
        float y3 = p3.getY();

        float z1 = world1.getZ();
        float z2 = world2.getZ();
        float z3 = world3.getZ();

        // Находим ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(x1, Math.min(x2, x3))));
        int maxX = (int) Math.min(triangleZBuffer.getWidth() - 1, Math.ceil(Math.max(x1, Math.max(x2, x3))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y1, Math.min(y2, y3))));
        int maxY = (int) Math.min(triangleZBuffer.getHeight() - 1, Math.ceil(Math.max(y1, Math.max(y2, y3))));

        // Площадь треугольника для барицентрических координат
        float triangleArea = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(triangleArea) < EPSILON) return;

        // Предварительные вычисления
        float invArea = 1.0f / triangleArea;

        // Проходим по ограничивающему прямоугольнику
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // Вычисляем барицентрические координаты
                float w1 = edgeFunction(x2, y2, x3, y3, x, y);
                float w2 = edgeFunction(x3, y3, x1, y1, x, y);
                float w3 = edgeFunction(x1, y1, x2, y2, x, y);

                // Проверяем, находится ли точка внутри треугольника
                if (w1 >= -EPSILON && w2 >= -EPSILON && w3 >= -EPSILON) {
                    // Нормализуем барицентрические координаты
                    float alpha = w1 * invArea;
                    float beta = w2 * invArea;
                    float gamma = w3 * invArea;

                    // Интерполируем глубину
                    float depth = alpha * z1 + beta * z2 + gamma * z3;

                    // Проверяем Z-буфер
                    if (triangleZBuffer.testAndSet(x, y, depth)) {
                        // Рисуем пиксель
                        gc.getPixelWriter().setColor(x, y, color);
                    }
                }
            }
        }
    }

    /**
     * Рендеринг текстурированного треугольника
     */
    private void renderTexturedTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        if (uv1 == null || uv2 == null || uv3 == null || !material.hasTexture()) {
            renderSolidTriangle(gc, p1, p2, p3, world1, world2, world3, material.getBaseColor());
            return;
        }

        Texture texture = material.getDiffuseTexture();
        if (texture == null) {
            renderSolidTriangle(gc, p1, p2, p3, world1, world2, world3, material.getBaseColor());
            return;
        }

        if (renderSettings.isUseZBuffer()) {
            renderTexturedTriangleWithZBuffer(gc, p1, p2, p3, world1, world2, world3, texture, uv1, uv2, uv3);
        } else {
            renderTexturedTriangleSimple(gc, p1, p2, p3, texture, uv1, uv2, uv3);
        }
    }

    /**
     * Простой текстурированный треугольник (без Z-буфера)
     */
    private void renderTexturedTriangleSimple(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Texture texture,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        // Преобразуем координаты
        float x1 = p1.getX();
        float y1 = p1.getY();
        float x2 = p2.getX();
        float y2 = p2.getY();
        float x3 = p3.getX();
        float y3 = p3.getY();

        // Получаем текстурные координаты
        float u1 = uv1.getX() * (texture.getWidth() - 1);
        float v1 = (1 - uv1.getY()) * (texture.getHeight() - 1);
        float u2 = uv2.getX() * (texture.getWidth() - 1);
        float v2 = (1 - uv2.getY()) * (texture.getHeight() - 1);
        float u3 = uv3.getX() * (texture.getWidth() - 1);
        float v3 = (1 - uv3.getY()) * (texture.getHeight() - 1);

        // Находим ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(x1, Math.min(x2, x3))));
        int maxX = (int) Math.min(gc.getCanvas().getWidth() - 1, Math.ceil(Math.max(x1, Math.max(x2, x3))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y1, Math.min(y2, y3))));
        int maxY = (int) Math.min(gc.getCanvas().getHeight() - 1, Math.ceil(Math.max(y1, Math.max(y2, y3))));

        // Площадь треугольника
        float triangleArea = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(triangleArea) < EPSILON) return;

        float invArea = 1.0f / triangleArea;
        PixelWriter pw = gc.getPixelWriter();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                // Барицентрические координаты
                float w1 = edgeFunction(x2, y2, x3, y3, x, y);
                float w2 = edgeFunction(x3, y3, x1, y1, x, y);
                float w3 = edgeFunction(x1, y1, x2, y2, x, y);

                if (w1 >= -EPSILON && w2 >= -EPSILON && w3 >= -EPSILON) {
                    float alpha = w1 * invArea;
                    float beta = w2 * invArea;
                    float gamma = w3 * invArea;

                    // Интерполируем текстурные координаты
                    float u = alpha * u1 + beta * u2 + gamma * u3;
                    float v = alpha * v1 + beta * v2 + gamma * v3;

                    // Получаем цвет текстуры
                    Color texColor = texture.getColor((int) u, (int) v);
                    if (texColor != null) {
                        pw.setColor(x, y, texColor);
                    }
                }
            }
        }
    }

    /**
     * Текстурированный треугольник с Z-буфером и перспективно-корректной интерполяцией
     */
    private void renderTexturedTriangleWithZBuffer(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Texture texture,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        float x1 = p1.getX();
        float y1 = p1.getY();
        float x2 = p2.getX();
        float y2 = p2.getY();
        float x3 = p3.getX();
        float y3 = p3.getY();

        float z1 = world1.getZ();
        float z2 = world2.getZ();
        float z3 = world3.getZ();

        // Преобразуем в однородные координаты для перспективной коррекции
        float w1 = 1.0f / z1;
        float w2 = 1.0f / z2;
        float w3 = 1.0f / z3;

        // Масштабируем текстурные координаты
        float u1 = uv1.getX() * w1;
        float v1 = uv1.getY() * w1;
        float u2 = uv2.getX() * w2;
        float v2 = uv2.getY() * w2;
        float u3 = uv3.getX() * w3;
        float v3 = uv3.getY() * w3;

        // Ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(x1, Math.min(x2, x3))));
        int maxX = (int) Math.min(triangleZBuffer.getWidth() - 1, Math.ceil(Math.max(x1, Math.max(x2, x3))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y1, Math.min(y2, y3))));
        int maxY = (int) Math.min(triangleZBuffer.getHeight() - 1, Math.ceil(Math.max(y1, Math.max(y2, y3))));

        // Площадь треугольника
        float triangleArea = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(triangleArea) < EPSILON) return;

        float invArea = 1.0f / triangleArea;
        PixelWriter pw = gc.getPixelWriter();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1_b = edgeFunction(x2, y2, x3, y3, x, y);
                float w2_b = edgeFunction(x3, y3, x1, y1, x, y);
                float w3_b = edgeFunction(x1, y1, x2, y2, x, y);

                if (w1_b >= -EPSILON && w2_b >= -EPSILON && w3_b >= -EPSILON) {
                    float alpha = w1_b * invArea;
                    float beta = w2_b * invArea;
                    float gamma = w3_b * invArea;

                    // Интерполируем 1/z для перспективной коррекции
                    float interpolatedW = alpha * w1 + beta * w2 + gamma * w3;
                    if (interpolatedW < EPSILON) continue;

                    // Интерполируем глубину
                    float depth = 1.0f / interpolatedW;

                    // Проверяем Z-буфер
                    if (triangleZBuffer.testAndSet(x, y, depth)) {
                        // Интерполируем текстурные координаты с учетом перспективы
                        float interpolatedU = (alpha * u1 + beta * u2 + gamma * u3) / interpolatedW;
                        float interpolatedV = (alpha * v1 + beta * v2 + gamma * v3) / interpolatedW;

                        // Приводим к диапазону [0, 1]
                        interpolatedU = Math.max(0, Math.min(1, interpolatedU));
                        interpolatedV = Math.max(0, Math.min(1, interpolatedV));

                        // Получаем цвет из текстуры
                        int texX = (int) (interpolatedU * (texture.getWidth() - 1));
                        int texY = (int) ((1 - interpolatedV) * (texture.getHeight() - 1));

                        Color texColor = texture.getColor(texX, texY);
                        if (texColor != null) {
                            pw.setColor(x, y, texColor);
                        }
                    }
                }
            }
        }
    }

    /**
     * Рендеринг освещенного треугольника (модель освещения Фонга/Гуро)
     */
    private void renderLitTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        // Получаем базовый цвет
        Color baseColor = material.getBaseColor();
        Texture texture = material.getDiffuseTexture();

        if (renderSettings.isUseTexture() && texture != null &&
                uv1 != null && uv2 != null && uv3 != null) {
            // Текстурированный с освещением
            renderLitTexturedTriangle(gc, p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    world1, world2, world3,
                    n1, n2, n3,
                    texture, uv1, uv2, uv3);
        } else {
            // Сплошной цвет с освещением
            renderLitSolidTriangle(gc, p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    world1, world2, world3,
                    n1, n2, n3,
                    baseColor);
        }
    }

    /**
     * Освещенный сплошной треугольник (интерполяция Гуро)
     */
    private void renderLitSolidTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Color baseColor) {

        // Вычисляем освещение для каждой вершины
        Color c1 = calculateVertexLighting(world1, n1, baseColor, null);
        Color c2 = calculateVertexLighting(world2, n2, baseColor, null);
        Color c3 = calculateVertexLighting(world3, n3, baseColor, null);

        // Рисуем треугольник с интерполяцией цветов
        renderGouraudTriangle(gc, p1, p2, p3,
                transformed1, transformed2, transformed3,
                c1, c2, c3);
    }

    /**
     * Освещенный текстурированный треугольник
     */
    private void renderLitTexturedTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Texture texture,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        float x1 = p1.getX();
        float y1 = p1.getY();
        float x2 = p2.getX();
        float y2 = p2.getY();
        float x3 = p3.getX();
        float y3 = p3.getY();

        float z1 = transformed1.getZ();
        float z2 = transformed2.getZ();
        float z3 = transformed3.getZ();

        // Однородные координаты для перспективной коррекции
        float w1 = 1.0f / z1;
        float w2 = 1.0f / z2;
        float w3 = 1.0f / z3;

        // Текстурные координаты с учетом перспективы
        float u1 = uv1.getX() * w1;
        float v1 = uv1.getY() * w1;
        float u2 = uv2.getX() * w2;
        float v2 = uv2.getY() * w2;
        float u3 = uv3.getX() * w3;
        float v3 = uv3.getY() * w3;

        // Нормали с учетом перспективы
        Vector3f n1w = new Vector3f(n1.getX() * w1, n1.getY() * w1, n1.getZ() * w1);
        Vector3f n2w = new Vector3f(n2.getX() * w2, n2.getY() * w2, n2.getZ() * w2);
        Vector3f n3w = new Vector3f(n3.getX() * w3, n3.getY() * w3, n3.getZ() * w3);

        // Ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(x1, Math.min(x2, x3))));
        int maxX = (int) Math.min(gc.getCanvas().getWidth() - 1, Math.ceil(Math.max(x1, Math.max(x2, x3))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y1, Math.min(y2, y3))));
        int maxY = (int) Math.min(gc.getCanvas().getHeight() - 1, Math.ceil(Math.max(y1, Math.max(y2, y3))));

        // Площадь треугольника
        float triangleArea = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(triangleArea) < EPSILON) return;

        float invArea = 1.0f / triangleArea;
        PixelWriter pw = gc.getPixelWriter();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1_b = edgeFunction(x2, y2, x3, y3, x, y);
                float w2_b = edgeFunction(x3, y3, x1, y1, x, y);
                float w3_b = edgeFunction(x1, y1, x2, y2, x, y);

                if (w1_b >= -EPSILON && w2_b >= -EPSILON && w3_b >= -EPSILON) {
                    float alpha = w1_b * invArea;
                    float beta = w2_b * invArea;
                    float gamma = w3_b * invArea;

                    // Интерполируем 1/z
                    float interpolatedW = alpha * w1 + beta * w2 + gamma * w3;
                    if (interpolatedW < EPSILON) continue;

                    // Интерполируем глубину
                    float depth = 1.0f / interpolatedW;

                    // Проверяем Z-буфер (если используется)
                    if (renderSettings.isUseZBuffer()) {
                        if (!triangleZBuffer.testAndSet(x, y, depth)) {
                            continue;
                        }
                    }

                    // Интерполируем текстурные координаты
                    float interpolatedU = (alpha * u1 + beta * u2 + gamma * u3) / interpolatedW;
                    float interpolatedV = (alpha * v1 + beta * v2 + gamma * v3) / interpolatedW;

                    interpolatedU = Math.max(0, Math.min(1, interpolatedU));
                    interpolatedV = Math.max(0, Math.min(1, interpolatedV));

                    // Получаем цвет текстуры
                    int texX = (int) (interpolatedU * (texture.getWidth() - 1));
                    int texY = (int) ((1 - interpolatedV) * (texture.getHeight() - 1));

                    Color texColor = texture.getColor(texX, texY);
                    if (texColor == null) continue;

                    // Интерполируем нормаль
                    Vector3f interpolatedNormal = new Vector3f(
                            (alpha * n1w.getX() + beta * n2w.getX() + gamma * n3w.getX()) / interpolatedW,
                            (alpha * n1w.getY() + beta * n2w.getY() + gamma * n3w.getY()) / interpolatedW,
                            (alpha * n1w.getZ() + beta * n2w.getZ() + gamma * n3w.getZ()) / interpolatedW
                    );
                    interpolatedNormal.normalize1();

                    // Интерполируем позицию в мировых координатах
                    Vector3f interpolatedPos = interpolatePosition(alpha, beta, gamma,
                            world1, world2, world3);

                    // Вычисляем освещение для пикселя
                    Color finalColor = calculatePixelLighting(
                            interpolatedPos, interpolatedNormal, texColor);

                    // Рисуем пиксель
                    pw.setColor(x, y, finalColor);
                }
            }
        }
    }

    /**
     * Треугольник Гуро с интерполяцией цветов
     */
    private void renderGouraudTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Color c1, Color c2, Color c3) {

        float x1 = p1.getX();
        float y1 = p1.getY();
        float x2 = p2.getX();
        float y2 = p2.getY();
        float x3 = p3.getX();
        float y3 = p3.getY();

        float z1 = transformed1.getZ();
        float z2 = transformed2.getZ();
        float z3 = transformed3.getZ();

        // Ограничивающий прямоугольник
        int minX = (int) Math.max(0, Math.floor(Math.min(x1, Math.min(x2, x3))));
        int maxX = (int) Math.min(gc.getCanvas().getWidth() - 1, Math.ceil(Math.max(x1, Math.max(x2, x3))));
        int minY = (int) Math.max(0, Math.floor(Math.min(y1, Math.min(y2, y3))));
        int maxY = (int) Math.min(gc.getCanvas().getHeight() - 1, Math.ceil(Math.max(y1, Math.max(y2, y3))));

        // Площадь треугольника
        float triangleArea = edgeFunction(x1, y1, x2, y2, x3, y3);
        if (Math.abs(triangleArea) < EPSILON) return;

        float invArea = 1.0f / triangleArea;
        PixelWriter pw = gc.getPixelWriter();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                float w1 = edgeFunction(x2, y2, x3, y3, x, y);
                float w2 = edgeFunction(x3, y3, x1, y1, x, y);
                float w3 = edgeFunction(x1, y1, x2, y2, x, y);

                if (w1 >= -EPSILON && w2 >= -EPSILON && w3 >= -EPSILON) {
                    float alpha = w1 * invArea;
                    float beta = w2 * invArea;
                    float gamma = w3 * invArea;

                    // Интерполируем глубину
                    float depth = alpha * z1 + beta * z2 + gamma * z3;

                    // Проверяем Z-буфер
                    if (renderSettings.isUseZBuffer()) {
                        if (!triangleZBuffer.testAndSet(x, y, depth)) {
                            continue;
                        }
                    }

                    // Интерполируем цвет
                    float r = (float) (alpha * c1.getRed() + beta * c2.getRed() + gamma * c3.getRed());
                    float g = (float) (alpha * c1.getGreen() + beta * c2.getGreen() + gamma * c3.getGreen());
                    float b = (float) (alpha * c1.getBlue() + beta * c2.getBlue() + gamma * c3.getBlue());

                    r = Math.max(0, Math.min(1, r));
                    g = Math.max(0, Math.min(1, g));
                    b = Math.max(0, Math.min(1, b));

                    // Рисуем пиксель
                    pw.setColor(x, y, Color.color(r, g, b));
                }
            }
        }
    }

    /**
     * Вычисление освещения для вершины
     */
    private Color calculateVertexLighting(Vector3f position, Vector3f normal,
                                          Color baseColor, Texture texture) {
        // Направление от вершины к камере
        Vector3f viewDir = new Vector3f(
                cameraPosition.getX() - position.getX(),
                cameraPosition.getY() - position.getY(),
                cameraPosition.getZ() - position.getZ()
        ).normalize();

        // Создаем временный материал
        Material tempMaterial = new Material(baseColor);
        if (texture != null) {
            tempMaterial.setDiffuseTexture(texture);
        }

        // Используем SceneLighting для расчета
        return sceneLighting.calculateLighting(
                tempMaterial, position, normal, viewDir, baseColor
        );
    }

    /**
     * Вычисление освещения для пикселя (модель Фонга)
     */
    private Color calculatePixelLighting(Vector3f position, Vector3f normal, Color baseColor) {
        Vector3f viewDir = new Vector3f(
                cameraPosition.getX() - position.getX(),
                cameraPosition.getY() - position.getY(),
                cameraPosition.getZ() - position.getZ()
        ).normalize();

        Material tempMaterial = new Material(baseColor);

        return sceneLighting.calculateLighting(
                tempMaterial, position, normal, viewDir, baseColor
        );
    }

    /**
     * Интерполяция позиции по барицентрическим координатам
     */
    private Vector3f interpolatePosition(float alpha, float beta, float gamma,
                                         Vector3f v1, Vector3f v2, Vector3f v3) {
        return new Vector3f(
                alpha * v1.getX() + beta * v2.getX() + gamma * v3.getX(),
                alpha * v1.getY() + beta * v2.getY() + gamma * v3.getY(),
                alpha * v1.getZ() + beta * v2.getZ() + gamma * v3.getZ()
        );
    }

    /**
     * Функция ребра для барицентрических координат
     */
    private float edgeFunction(float x1, float y1, float x2, float y2, float x, float y) {
        return (x2 - x1) * (y - y1) - (y2 - y1) * (x - x1);
    }

    /**
     * Создание буфера для рисования
     */
    private void createBuffer(int width, int height) {
        if (canvasBuffer == null ||
                canvasBuffer.getWidth() != width ||
                canvasBuffer.getHeight() != height) {

            canvasBuffer = new Canvas(width, height);
            gcBuffer = canvasBuffer.getGraphicsContext2D();
            pixelWriter = gcBuffer.getPixelWriter();
        }

        // Очищаем буфер
        gcBuffer.clearRect(0, 0, width, height);
    }

    // ==================== КАРКАСНЫЙ РЕНДЕРИНГ ====================

    private void renderWireframeOnly(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height) {

        // Упрощенная версия - используем старый метод
        renderWireframeSimple(graphicsContext, camera, model, width, height);
    }

    private void renderWireframeOverlay(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height) {

        // Упрощенная версия - используем старый метод
        renderWireframeSimple(graphicsContext, camera, model, width, height);
    }

    /**
     * Упрощенный рендеринг каркаса
     */
    private void renderWireframeSimple(
            GraphicsContext gc,
            Camera camera,
            Model model,
            int width,
            int height) {

        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();

        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);

        List<Polygon> polygons = model.getPolygons();
        List<Vector3f> vertices = model.getVertices();

        gc.setStroke(renderSettings.getWireframeColor());
        gc.setLineWidth(renderSettings.getWireframeThickness());

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            int n = vertexIndices.size();

            if (n < 2) continue;

            // Сохраняем преобразованные точки
            Point2f[] points = new Point2f[n];
            for (int i = 0; i < n; i++) {
                Vector3f vertex = vertices.get(vertexIndices.get(i));
                Vector3f transformed = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                points[i] = vertexToPoint(transformed, width, height);
            }

            // Рисуем линии полигона
            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                gc.strokeLine(points[i].x, points[i].y, points[next].x, points[next].y);
            }
        }
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private boolean isBackface(Polygon polygon, List<Vector3f> vertices) {
        if (polygon.getVertexIndices().size() < 3) return false;

        Vector3f v1 = vertices.get(polygon.getVertexIndices().get(0));
        Vector3f v2 = vertices.get(polygon.getVertexIndices().get(1));
        Vector3f v3 = vertices.get(polygon.getVertexIndices().get(2));

        // Вычисляем нормаль полигона
        Vector3f normal = Vector3f.calculatePolygonNormal(v1, v2, v3);

        // Вектор от камеры к полигону (упрощенно)
        Vector3f viewDir = new Vector3f(0, 0, -1);

        // Если скалярное произведение положительное - это задняя грань
        return normal.dot(viewDir) > 0;
    }

    // ==================== МЕТОДЫ ДЛЯ РАБОТЫ С ТЕКСТУРАМИ ====================

    /**
     * Загрузка текстуры
     */
    public void loadTexture(String path) {
        this.currentTexture = TextureManager.getInstance().loadTexture(path);
    }

    /**
     * Установка текстуры
     */
    public void setTexture(Texture texture) {
        this.currentTexture = texture;
    }

    /**
     * Получение текущей текстуры
     */
    public Texture getCurrentTexture() {
        return currentTexture;
    }

    /**
     * Установка текстуры в модель
     */
    public void setTextureToModel(Model model, String texturePath) {
        if (model != null) {
            if (model.getMaterial() == null) {
                model.setMaterial(new Material());
            }
            if (texturePath != null) {
                Texture texture = TextureManager.getInstance().loadTexture(texturePath);
                if (texture != null) {
                    model.getMaterial().setDiffuseTexture(texture);
                }
            }
        }
    }

    // ==================== ГЕТТЕРЫ И СЕТТЕРЫ ====================

    public RenderSettings getRenderSettings() {
        return renderSettings;
    }

    public void setRenderSettings(RenderSettings settings) {
        this.renderSettings = settings;
    }

    public SceneLighting getSceneLighting() {
        return sceneLighting;
    }

    public void setSolidColor(Color color) {
        renderSettings.setSolidColor(color);
    }

    public void setWireframeColor(Color color) {
        renderSettings.setWireframeColor(color);
    }

    // Методы для быстрого переключения режимов
    public void enableWireframe() {
        renderSettings.setDrawWireframe(true);
    }

    public void disableWireframe() {
        renderSettings.setDrawWireframe(false);
    }

    public void toggleWireframe() {
        renderSettings.setDrawWireframe(!renderSettings.isDrawWireframe());
    }

    public void enableTexture() {
        renderSettings.setUseTexture(true);
    }

    public void disableTexture() {
        renderSettings.setUseTexture(false);
    }

    public void toggleTexture() {
        renderSettings.setUseTexture(!renderSettings.isUseTexture());
    }

    public void enableLighting() {
        renderSettings.setUseLighting(true);
    }

    public void disableLighting() {
        renderSettings.setUseLighting(false);
    }

    public void toggleLighting() {
        renderSettings.setUseLighting(!renderSettings.isUseLighting());
    }
}
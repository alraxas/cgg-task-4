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
import javafx.scene.canvas.GraphicsContext;
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

    // Кэши для оптимизации
    private ZBuffer triangleZBuffer;
    private ZBuffer wireframeZBuffer;
    private Matrix4f cachedViewProjectionMatrix;
    private Matrix4f cachedNormalMatrix;
    private Texture currentTexture;

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
        if (renderSettings.isUseTexture() && model.getTexturePath() != null) {
            Texture texture = TextureManager.getInstance().loadTexture(model.getTexturePath());
            if (texture != null) {
                material.setDiffuseTexture(texture);
            }
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
     * Рендеринг сплошного треугольника
     */
    private void renderSolidTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Color color) {

        // УПРОЩЕННАЯ ВЕРСИЯ - используем встроенные возможности JavaFX
        gc.setFill(color);
        gc.fillPolygon(
                new double[]{p1.getX(), p2.getX(), p3.getX()},
                new double[]{p1.getY(), p2.getY(), p3.getY()},
                3
        );

        // Можно добавить обводку для четкости
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
     * Рендеринг текстурированного треугольника (упрощенная версия)
     */
    private void renderTexturedTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        if (uv1 == null || uv2 == null || uv3 == null || !material.hasTexture()) {
            // Если нет текстурных координат или текстуры, рисуем сплошным цветом
            renderSolidTriangle(gc, p1, p2, p3, world1, world2, world3, material.getBaseColor());
            return;
        }

        // УПРОЩЕННАЯ ВЕРСИЯ - пока просто сплошной цвет
        renderSolidTriangle(gc, p1, p2, p3, world1, world2, world3, material.getBaseColor());
    }

    /**
     * Рендеринг освещенного треугольника (упрощенная версия)
     */
    private void renderLitTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        // УПРОЩЕННАЯ ВЕРСИЯ - просто затемняем цвет для эффекта освещения
        Color baseColor = material.getBaseColor();
        Color shadedColor;

        if (renderSettings.isUseTexture() && material.hasTexture() &&
                uv1 != null && uv2 != null && uv3 != null) {
            // Текстурированный с освещением
            shadedColor = baseColor.deriveColor(0, 1.0, 0.8, 1.0);
        } else {
            // Сплошной цвет с освещением
            shadedColor = baseColor.deriveColor(0, 1.0, 0.7, 1.0);
        }

        renderSolidTriangle(gc, p1, p2, p3, transformed1, transformed2, transformed3, shadedColor);
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
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
    private final SceneLighting sceneLighting;
    private RenderSettings renderSettings;

    private ZBuffer triangleZBuffer;
    private Matrix4f cachedViewProjectionMatrix;
    private Matrix4f cachedNormalMatrix;
    private Texture currentTexture;
    private Vector3f cameraPosition;

    public UnifiedRenderer() {
        this.triangleRasterizer = new TriangleRasterizer();
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

        if (model == null || model.getVertices() == null || model.getVertices().isEmpty()) {
            graphicsContext.clearRect(0, 0, width, height);
            return;
        }

        this.cameraPosition = camera.getPosition();
        graphicsContext.clearRect(0, 0, width, height);

        if (renderSettings.isUseZBuffer()) {
            triangleZBuffer = new ZBuffer(width, height);
        }

        sceneLighting.updateForCamera(camera);

        RenderMode mode = renderSettings.getCurrentMode();

        if (mode == RenderMode.WIREFRAME) {
            renderWireframeOnly(graphicsContext, camera, model, width, height);
            return;
        }

        renderFull(graphicsContext, camera, model, width, height, mode);
    }

    private void renderFull(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height,
            RenderMode mode) {

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

        cachedViewProjectionMatrix = modelViewProjectionMatrix;
        cachedNormalMatrix = normalMatrix;

        if (mode != RenderMode.WIREFRAME) {
            renderTriangles(graphicsContext, model, width, height, mode);
        }

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

        Material material = new Material(renderSettings.getSolidColor());

        if (renderSettings.isUseTexture() && currentTexture != null) {
            material.setDiffuseTexture(currentTexture);
        }

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();

            if (vertexIndices.size() != 3) {
                continue;
            }

            // Отсечение задних граней (опционально)
            if (renderSettings.isBackfaceCulling() && isBackface(polygon, vertices)) {
                continue;
            }

            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));

            Vector3f transformed1 = multiplyMatrix4ByVector3(cachedViewProjectionMatrix, v1);
            Vector3f transformed2 = multiplyMatrix4ByVector3(cachedViewProjectionMatrix, v2);
            Vector3f transformed3 = multiplyMatrix4ByVector3(cachedViewProjectionMatrix, v3);

            Point2f screen1 = vertexToPoint(transformed1, width, height);
            Point2f screen2 = vertexToPoint(transformed2, width, height);
            Point2f screen3 = vertexToPoint(transformed3, width, height);

            Vector2f p1 = new Vector2f(screen1.x, screen1.y);
            Vector2f p2 = new Vector2f(screen2.x, screen2.y);
            Vector2f p3 = new Vector2f(screen3.x, screen3.y);

            // Получение нормалей
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

            // Преобразование нормалей в пространство камеры
            n1 = multiplyMatrix4ByVector3(cachedNormalMatrix, n1).normalize();
            n2 = multiplyMatrix4ByVector3(cachedNormalMatrix, n2).normalize();
            n3 = multiplyMatrix4ByVector3(cachedNormalMatrix, n3).normalize();

            // Получение текстурных координат
            Vector2f uv1 = null, uv2 = null, uv3 = null;
            if (renderSettings.isUseTexture() && !textureVertices.isEmpty()) {
                List<Integer> textureIndices = polygon.getTextureVertexIndices();
                if (textureIndices.size() >= 3) {
                    uv1 = textureVertices.get(textureIndices.get(0));
                    uv2 = textureVertices.get(textureIndices.get(1));
                    uv3 = textureVertices.get(textureIndices.get(2));

//                     uv1 = new Vector2f(uv1.getX(), 1.0f - uv1.getY());
//                     uv2 = new Vector2f(uv2.getX(), 1.0f - uv2.getY());
//                     uv3 = new Vector2f(uv3.getX(), 1.0f - uv3.getY());

                    // ИЛИ, если нужно инвертировать, делайте это аккуратно:
                    uv1 = new Vector2f(uv1.getX(), uv1.getY());
                    uv2 = new Vector2f(uv2.getX(), uv2.getY());
                    uv3 = new Vector2f(uv3.getX(), uv3.getY());
                }
            }

            // Выбор метода рендеринга в зависимости от режима
            switch (mode) {
                case SOLID:
                    renderSolidTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3, material);
                    break;

                case TEXTURED:
                    renderTexturedTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            material, uv1, uv2, uv3);
                    break;

                case LIT_SOLID:
                    renderLitSolidTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            v1, v2, v3, n1, n2, n3, material);
                    break;

                case LIT_TEXTURED:
                    renderLitTexturedTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            v1, v2, v3, n1, n2, n3,
                            material, uv1, uv2, uv3);
                    break;

                case WIREFRAME_LIT_SOLID:
                case ALL:
                    renderAllFeatures(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3,
                            v1, v2, v3, n1, n2, n3,
                            material, uv1, uv2, uv3);
                    break;

                default:
                    renderSolidTriangle(graphicsContext, p1, p2, p3,
                            transformed1, transformed2, transformed3, material);
                    break;
            }
        }
    }

    /**
     * Рендеринг сплошного треугольника
     */
    private void renderSolidTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Material material) {

        Color color = material.getBaseColor();

        if (renderSettings.isUseZBuffer()) {
            triangleRasterizer.rasterizeTriangleWithZBuffer(
                    p1, p2, p3,
                    transformed1.getZ(), transformed2.getZ(), transformed3.getZ(),
                    gc, triangleZBuffer, color
            );
        } else {
            triangleRasterizer.rasterizeTriangle(p1, p2, p3, gc, color);
        }

        // Обводка для контраста
//        if (renderSettings.getWireframeThickness() > 0) {
//            drawWireframe(gc, p1, p2, p3);
//        }
    }

    /**
     * Рендеринг текстурированного треугольника
     */
    private void renderTexturedTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        Texture texture = currentTexture != null ? currentTexture : material.getDiffuseTexture();

        if (uv1 == null || uv2 == null || uv3 == null || texture == null) {
            // Если нет текстурных координат или текстуры, рисуем сплошным цветом
            renderSolidTriangle(gc, p1, p2, p3,
                    transformed1, transformed2, transformed3, material);
            return;
        }

        boolean bilinearFiltering = renderSettings.isBilinearFiltering();

        if (renderSettings.isUseZBuffer()) {
            triangleRasterizer.rasterizeTexturedTriangleWithZBuffer(
                    p1, p2, p3,
                    transformed1.getZ(), transformed2.getZ(), transformed3.getZ(),
                    uv1, uv2, uv3,
                    texture, bilinearFiltering,
                    gc, triangleZBuffer
            );
        } else {
            triangleRasterizer.rasterizeTexturedTriangle(
                    p1, p2, p3,
                    uv1, uv2, uv3,
                    texture, bilinearFiltering,
                    gc
            );
        }

        // Обводка для контраста
//        if (renderSettings.getWireframeThickness() > 0) {
//            drawWireframe(gc, p1, p2, p3);
//        }
    }

    /**
     * Рендеринг освещенного сплошного треугольника
     */
    private void renderLitSolidTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material) {

        boolean smoothShading = renderSettings.isSmoothShading();

        if (renderSettings.isUseZBuffer()) {
            triangleRasterizer.rasterizeLitTriangleWithZBuffer(
                    p1, p2, p3,
                    transformed1.getZ(), transformed2.getZ(), transformed3.getZ(),
                    world1, world2, world3,
                    n1, n2, n3,
                    material, sceneLighting,
                    smoothShading,
                    gc, triangleZBuffer
            );
        } else {
            triangleRasterizer.rasterizeLitTriangle(
                    p1, p2, p3,
                    world1, world2, world3,
                    n1, n2, n3,
                    material, sceneLighting,
                    smoothShading,
                    gc
            );
        }

        // Обводка для контраста
//        if (renderSettings.getWireframeThickness() > 0) {
//            drawWireframe(gc, p1, p2, p3);
//        }
    }

    /**
     * Рендеринг освещенного текстурированного треугольника
     */
    private void renderLitTexturedTriangle(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        Texture texture = currentTexture != null ? currentTexture : material.getDiffuseTexture();

        if (uv1 == null || uv2 == null || uv3 == null || texture == null) {
            // Если нет текстурных координат или текстуры, рисуем освещенный сплошной треугольник
            renderLitSolidTriangle(gc, p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    world1, world2, world3,
                    n1, n2, n3, material);
            return;
        }

        boolean smoothShading = renderSettings.isSmoothShading();
        boolean bilinearFiltering = renderSettings.isBilinearFiltering();

        if (renderSettings.isUseZBuffer()) {
            triangleRasterizer.rasterizeLitTexturedTriangleWithZBuffer(
                    p1, p2, p3,
                    transformed1.getZ(), transformed2.getZ(), transformed3.getZ(),
                    world1, world2, world3,
                    n1, n2, n3,
                    uv1, uv2, uv3,
                    material, sceneLighting,
                    smoothShading, bilinearFiltering,
                    gc, triangleZBuffer
            );
        } else {
            triangleRasterizer.rasterizeLitTexturedTriangle(
                    p1, p2, p3,
                    world1, world2, world3,
                    n1, n2, n3,
                    uv1, uv2, uv3,
                    material, sceneLighting,
                    smoothShading, bilinearFiltering,
                    gc
            );
        }

        // Обводка для контраста
//        if (renderSettings.getWireframeThickness() > 0) {
//            drawWireframe(gc, p1, p2, p3);
//        }
    }

    /**
     * Рендеринг со всеми функциями (освещение + текстуры + каркас)
     */
    private void renderAllFeatures(
            GraphicsContext gc,
            Vector2f p1, Vector2f p2, Vector2f p3,
            Vector3f transformed1, Vector3f transformed2, Vector3f transformed3,
            Vector3f world1, Vector3f world2, Vector3f world3,
            Vector3f n1, Vector3f n2, Vector3f n3,
            Material material,
            Vector2f uv1, Vector2f uv2, Vector2f uv3) {

        Texture texture = currentTexture != null ? currentTexture : material.getDiffuseTexture();

        if (uv1 != null && uv2 != null && uv3 != null && texture != null) {
            // С текстурами
            renderLitTexturedTriangle(gc, p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    world1, world2, world3,
                    n1, n2, n3,
                    material, uv1, uv2, uv3);
        } else {
            // Без текстур
            renderLitSolidTriangle(gc, p1, p2, p3,
                    transformed1, transformed2, transformed3,
                    world1, world2, world3,
                    n1, n2, n3, material);
        }

        // Всегда рисуем каркас в этом режиме
        drawWireframe(gc, p1, p2, p3);
    }

    /**
     * Рисование каркаса треугольника
     */
    private void drawWireframe(GraphicsContext gc, Vector2f p1, Vector2f p2, Vector2f p3) {
        gc.setStroke(renderSettings.getWireframeColor());
        gc.setLineWidth(renderSettings.getWireframeThickness());
        gc.strokePolygon(
                new double[]{p1.getX(), p2.getX(), p3.getX()},
                new double[]{p1.getY(), p2.getY(), p3.getY()},
                3
        );
    }

    /**
     * Рендеринг только каркаса
     */
    private void renderWireframeOnly(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height) {

        renderWireframeSimple(graphicsContext, camera, model, width, height);
    }

    /**
     * Рендеринг каркаса поверх остального
     */
    private void renderWireframeOverlay(
            GraphicsContext graphicsContext,
            Camera camera,
            Model model,
            int width,
            int height) {

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

            Point2f[] points = new Point2f[n];
            for (int i = 0; i < n; i++) {
                Vector3f vertex = vertices.get(vertexIndices.get(i));
                Vector3f transformed = multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertex);
                points[i] = vertexToPoint(transformed, width, height);
            }

            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                gc.strokeLine(points[i].x, points[i].y, points[next].x, points[next].y);
            }
        }
    }

    /**
     * Проверка на заднюю грань
     */
    private boolean isBackface(Polygon polygon, List<Vector3f> vertices) {
        if (polygon.getVertexIndices().size() < 3) return false;

        Vector3f v1 = vertices.get(polygon.getVertexIndices().get(0));
        Vector3f v2 = vertices.get(polygon.getVertexIndices().get(1));
        Vector3f v3 = vertices.get(polygon.getVertexIndices().get(2));

        // Вычисляем нормаль полигона
        Vector3f normal = Vector3f.calculatePolygonNormal(v1, v2, v3);

        // Вектор от камеры к полигону
        Vector3f cameraToPolygon = cameraPosition.subtract(v1);

        // Если нормаль направлена от камеры - это задняя грань
        return normal.dot(cameraToPolygon) < 0;
    }

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

    public void enableZBuffer() {
        renderSettings.setUseZBuffer(true);
    }

    public void disableZBuffer() {
        renderSettings.setUseZBuffer(false);
    }

    public void toggleZBuffer() {
        renderSettings.setUseZBuffer(!renderSettings.isUseZBuffer());
    }

    public void setSmoothShading(boolean smoothShading) {
        renderSettings.setSmoothShading(smoothShading);
    }

    public void setBilinearFiltering(boolean bilinearFiltering) {
        renderSettings.setBilinearFiltering(bilinearFiltering);
    }
}
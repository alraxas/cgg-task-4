//package com.cgvsu.render_engine;
//
//import com.cgvsu.math.Matrix4f;
//import com.cgvsu.math.Vector2f;
//import com.cgvsu.math.Vector3f;
//import com.cgvsu.model.Model;
//import com.cgvsu.model.Polygon;
//import com.cgvsu.render_engine.lighting.LightingModel;
//import com.cgvsu.render_engine.rasterization.TriangleRasterizer;
//import com.cgvsu.render_engine.rasterization.ZBuffer;
//import javafx.scene.canvas.GraphicsContext;
//import javafx.scene.paint.Color;
//
//import javax.vecmath.Point2f;
//import java.util.ArrayList;
//import java.util.List;
//
//import static com.cgvsu.render_engine.GraphicConveyor.*;
//
//public class RenderEngine {
//    private ZBuffer zBuffer;
//    private TriangleRasterizer rasterizer;
//    private LightingModel lightingModel;
//
//    public void setLightingModel(LightingModel lightingModel) {
//        this.lightingModel = lightingModel;
//    }
//
//    public RenderEngine() {
//        this.rasterizer = new TriangleRasterizer();
//    }
//
//    public void renderSolid(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final int width,
//            final int height,
//            final Color fillColor) {
//
//        zBuffer = new ZBuffer(width, height);
//
//        graphicsContext.clearRect(0, 0, width, height);
//
//        if (lightingModel != null) {
//            lightingModel.setCamera(camera);
//        }
//
//        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
//        modelViewMatrix.multiply(viewMatrix);
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelViewMatrix);
//        modelViewProjectionMatrix.multiply(projectionMatrix);
//
//        // Проходим по всем полигонам (должны быть треугольниками)
//        List<Polygon> polygons = mesh.getPolygons();
//
//        for (Polygon polygon : polygons) {
//            List<Integer> vertexIndices = polygon.getVertexIndices();
//
//            if (vertexIndices.size() != 3) {
//                continue; // Пропускаем не треугольники
//            }
//
//            // Получаем вершины треугольника
//            Vector3f v1 = mesh.getVertices().get(vertexIndices.get(0));
//            Vector3f v2 = mesh.getVertices().get(vertexIndices.get(1));
//            Vector3f v3 = mesh.getVertices().get(vertexIndices.get(2));
//
//            Vector3f vec1 = new Vector3f((float) v1.getX(), (float) v1.getY(), (float) v1.getZ());
//            Vector3f vec2 = new Vector3f((float) v2.getX(), (float) v2.getY(), (float) v2.getZ());
//            Vector3f vec3 = new Vector3f((float) v3.getX(), (float) v3.getY(), (float) v3.getZ());
//
//            // Применяем преобразования
//            Vector3f transformed1 = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1);
//            Vector3f transformed2 = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2);
//            Vector3f transformed3 = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec3);
//
//            // Преобразуем в экранные координаты
//            Point2f screen1 = Vector2f.vertexToPoint(transformed1, width, height);
//            Point2f screen2 = Vector2f.vertexToPoint(transformed2, width, height);
//            Point2f screen3 = Vector2f.vertexToPoint(transformed3, width, height);
//
//            // Преобразуем Point2f в Vector2f
//            Vector2f p1 = new Vector2f(screen1.x, screen1.y);
//            Vector2f p2 = new Vector2f(screen2.x, screen2.y);
//            Vector2f p3 = new Vector2f(screen3.x, screen3.y);
//
//            rasterizer.rasterizeTriangle(p1, p2, p3,
//                    transformed1, transformed2, transformed3,
//                    graphicsContext, zBuffer, fillColor);
//        }
//    }
//
//    // Метод для рендеринга с освещением
//    public void renderWithLighting(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final int width,
//            final int height,
//            final Color baseColor,
//            final Vector3f lightDirection) {
//
//        if (lightingModel == null) {
//            // Если нет модели освещения, рисуем обычную заливку
//            renderSolid(graphicsContext, camera, mesh, width, height, baseColor);
//            return;
//        }
//
//        zBuffer = new ZBuffer(width, height);
//        graphicsContext.clearRect(0, 0, width, height);
//
//        // Настраиваем освещение
//        lightingModel.setCamera(camera);
//        lightingModel.setLightDirection(lightDirection);
//
//        Matrix4f modelMatrix = GraphicConveyor.rotateScaleTranslate();
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
//        modelViewMatrix.multiply(viewMatrix);
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelViewMatrix);
//        modelViewProjectionMatrix.multiply(projectionMatrix);
//
//        // Матрица для нормалей
//        Matrix4f normalMatrix = new Matrix4f(modelViewMatrix);
//        normalMatrix.invert();
//        normalMatrix.transpose();
//
//        List<Polygon> polygons = mesh.getPolygons();
//        List<Vector3f> normals = mesh.getNormals();
//
//        for (Polygon polygon : polygons) {
//            List<Integer> vertexIndices = polygon.getVertexIndices();
//            List<Integer> normalIndices = polygon.getNormalIndices();
//
//            if (vertexIndices.size() != 3) {
//                continue;
//            }
//
//            // Вершины
//            Vector3f v1 = mesh.getVertices().get(vertexIndices.get(0));
//            Vector3f v2 = mesh.getVertices().get(vertexIndices.get(1));
//            Vector3f v3 = mesh.getVertices().get(vertexIndices.get(2));
//
//            Vector3f vec1 = new Vector3f((float) v1.getX(), (float) v1.getY(), (float) v1.getZ());
//            Vector3f vec2 = new Vector3f((float) v2.getX(), (float) v2.getY(), (float) v2.getZ());
//            Vector3f vec3 = new Vector3f((float) v3.getX(), (float) v3.getY(), (float) v3.getZ());
//
//            // Нормали (если есть, иначе вычисляем)
//            Vector3f n1, n2, n3;
//            if (!normalIndices.isEmpty() && normalIndices.size() >= 3) {
//                n1 = normals.get(normalIndices.get(0));
//                n2 = normals.get(normalIndices.get(1));
//                n3 = normals.get(normalIndices.get(2));
//            } else {
//                // Вычисляем нормаль треугольника
//                Vector3f edge1 = new Vector3f();
//                Vector3f edge2 = new Vector3f();
//                edge1.sub(v2, v1);
//                edge2.sub(v3, v1);
//                n1 = edge1.multiplyVectorVector(edge2).normalize();
//                n2 = n1;
//                n3 = n1;
//            }
//
//            Vector3f norm1 = new Vector3f((float) n1.getX(), (float) n1.getY(), (float) n1.getZ());
//            Vector3f norm2 = new Vector3f((float) n2.getX(), (float) n2.getY(), (float) n2.getZ());
//            Vector3f norm3 = new Vector3f((float) n3.getX(), (float) n3.getY(), (float) n3.getZ());
//
//            // Преобразуем вершины и нормали
//            Vector3f transformed1 = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec1);
//            Vector3f transformed2 = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec2);
//            Vector3f transformed3 = Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vec3);
//
//            Vector3f transformedNorm1 = Matrix4f.multiplyMatrix4ByVector3(normalMatrix, norm1);
//            Vector3f transformedNorm2 = Matrix4f.multiplyMatrix4ByVector3(normalMatrix, norm2);
//            Vector3f transformedNorm3 = Matrix4f.multiplyMatrix4ByVector3(normalMatrix, norm3);
//
//            // Нормализуем нормали
//            transformedNorm1.normalize();
//            transformedNorm2.normalize();
//            transformedNorm3.normalize();
//
//            // Экранные координаты
//            Point2f screen1 = Vector2f.vertexToPoint(transformed1, width, height);
//            Point2f screen2 = Vector2f.vertexToPoint(transformed2, width, height);
//            Point2f screen3 = Vector2f.vertexToPoint(transformed3, width, height);
//
//            Vector2f p1 = new Vector2f(screen1.x, screen1.y);
//            Vector2f p2 = new Vector2f(screen2.x, screen2.y);
//            Vector2f p3 = new Vector2f(screen3.x, screen3.y);
//
//            // Вычисляем освещение для каждой вершины
//            float light1 = lightingModel.calculateLightIntensity(transformedNorm1, transformed1);
//            float light2 = lightingModel.calculateLightIntensity(transformedNorm2, transformed2);
//            float light3 = lightingModel.calculateLightIntensity(transformedNorm3, transformed3);
//
//            // Растеризуем с освещением
//            rasterizeTriangleWithLighting(p1, p2, p3,
//                    transformed1, transformed2, transformed3,
//                    light1, light2, light3,
//                    graphicsContext, zBuffer, baseColor);
//        }
//    }
//
//    private void rasterizeTriangleWithLighting(
//            Vector2f p1, Vector2f p2, Vector2f p3,
//            Vector3f v1, Vector3f v2, Vector3f v3,
//            float light1, float light2, float light3,
//            GraphicsContext gc, ZBuffer zBuffer, Color baseColor) {
//
//        int minX = Math.max(0, (int) Math.min(Math.min(p1.getX(), p2.getX()), p3.getX()));
//        int maxX = Math.min((int) gc.getCanvas().getWidth() - 1,
//                (int) Math.max(Math.max(p1.getX(), p2.getX()), p3.getX()));
//        int minY = Math.max(0, (int) Math.min(Math.min(p1.getY(), p2.getY()), p3.getY()));
//        int maxY = Math.min((int) gc.getCanvas().getHeight() - 1,
//                (int) Math.max(Math.max(p1.getY(), p2.getY()), p3.getY()));
//
//        float area = (float) Vector2f.edgeFunction(p1, p2, p3);
//        if (Math.abs(area) < 1e-6) return;
//
//        for (int y = minY; y <= maxY; y++) {
//            for (int x = minX; x <= maxX; x++) {
//                Vector2f p = new Vector2f(x, y);
//
//                float w1 = (float) (Vector2f.edgeFunction(p2, p3, p) / area);
//                float w2 = (float) (Vector2f.edgeFunction(p3, p1, p) / area);
//                float w3 = (float) (Vector2f.edgeFunction(p1, p2, p) / area);
//
//                if (w1 >= -1e-6 && w2 >= -1e-6 && w3 >= -1e-6) {
//                    float z = (float) (w1 * v1.getZ() + w2 * v2.getZ() + w3 * v3.getZ());
//
//                    if (zBuffer.testAndSet(x, y, z)) {
//                        // Интерполируем освещение
//                        float light = w1 * light1 + w2 * light2 + w3 * light3;
//                        light = Math.max(0.2f, Math.min(1.0f, light)); // Ограничиваем
//
//                        Color shadedColor = Color.color(
//                                baseColor.getRed() * light,
//                                baseColor.getGreen() * light,
//                                baseColor.getBlue() * light
//                        );
//
//                        gc.getPixelWriter().setColor(x, y, shadedColor);
//                    }
//                }
//            }
//        }
//    }
//
//    public static void renderWireframe(
//            final GraphicsContext graphicsContext,
//            final Camera camera,
//            final Model mesh,
//            final int width,
//            final int height) {
//        // Существующий метод рендеринга каркаса
//        Matrix4f modelMatrix = rotateScaleTranslate();
//        Matrix4f viewMatrix = camera.getViewMatrix();
//        Matrix4f projectionMatrix = camera.getProjectionMatrix();
//
//        Matrix4f modelViewProjectionMatrix = new Matrix4f(modelMatrix);
//        modelViewProjectionMatrix.multiply(viewMatrix);
//        modelViewProjectionMatrix.multiply(projectionMatrix);
//
//        final int nPolygons = mesh.polygons.size();
//        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
//            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();
//
//            ArrayList<Point2f> resultPoints = new ArrayList<>();
//            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));
//
//                Vector3f vertexVecmath = new Vector3f(
//                        vertex.getX(),
//                        vertex.getY(),
//                        vertex.getZ());
//
//                Point2f resultPoint = Vector2f.vertexToPoint(
//                        Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath),
//                        width, height
//                );
//                resultPoints.add(resultPoint);
//            }
//
//            // Рисуем линии полигона
//            for (int vertexInPolygonInd = 1; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(vertexInPolygonInd - 1).x,
//                        resultPoints.get(vertexInPolygonInd - 1).y,
//                        resultPoints.get(vertexInPolygonInd).x,
//                        resultPoints.get(vertexInPolygonInd).y
//                );
//            }
//
//            if (nVerticesInPolygon > 0) {
//                graphicsContext.strokeLine(
//                        resultPoints.get(nVerticesInPolygon - 1).x,
//                        resultPoints.get(nVerticesInPolygon - 1).y,
//                        resultPoints.get(0).x,
//                        resultPoints.get(0).y
//                );
//            }
//        }
//    }
//}
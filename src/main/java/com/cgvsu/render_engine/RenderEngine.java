package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.rasterization.TriangleRasterizer;
import com.cgvsu.render_engine.rasterization.ZBuffer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import javax.vecmath.Point2f;
import java.util.ArrayList;
import java.util.List;

import static com.cgvsu.render_engine.GraphicConveyor.*;

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
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);

        final int nPolygons = mesh.polygons.size();
        for (int polygonInd = 0; polygonInd < nPolygons; ++polygonInd) {
            final int nVerticesInPolygon = mesh.polygons.get(polygonInd).getVertexIndices().size();

            ArrayList<Point2f> resultPoints = new ArrayList<>();
            for (int vertexInPolygonInd = 0; vertexInPolygonInd < nVerticesInPolygon; ++vertexInPolygonInd) {
                Vector3f vertex = mesh.vertices.get(mesh.polygons.get(polygonInd).getVertexIndices().get(vertexInPolygonInd));

                Vector3f vertexVecmath = new Vector3f(
                        vertex.getX(),
                        vertex.getY(),
                        vertex.getZ());

                Point2f resultPoint = Vector2f.vertexToPoint(
                        Matrix4f.multiplyMatrix4ByVector3(modelViewProjectionMatrix, vertexVecmath),
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
}
package com.cgvsu.render_engine.processing;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class NormalCalculator {
    public static void recalculateNormals(Model model) {
        List<Vector3f> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();

        ArrayList<Vector3f> newNormals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            newNormals.add(new Vector3f(0, 0, 0));
        }

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue;

            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));
            Vector3f normal = Vector3f.calculatePolygonNormal(v1, v2, v3);

            for (Integer vertexIndex : vertexIndices) {
                Vector3f currentNormal = newNormals.get(vertexIndex);
                currentNormal.setX(currentNormal.getX() + normal.getX());
                currentNormal.setY(currentNormal.getY() + normal.getY());
                currentNormal.setZ(currentNormal.getZ() + normal.getZ());
            }
        }

        for (Vector3f normal : newNormals) {
            Vector3f.normalizeVector(normal);
        }

        model.setNormals(newNormals);

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            polygon.getNormalIndices().clear();

            // Каждая вершина теперь ссылается на свою нормаль по тому же индексу
            for (Integer vertexIndex : vertexIndices) {
                polygon.getNormalIndices().add(vertexIndex);
            }
        }
    }
}

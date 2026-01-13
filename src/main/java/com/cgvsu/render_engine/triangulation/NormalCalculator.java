package com.cgvsu.render_engine.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class NormalCalculator {
    private static final float EPSILON = 1e-6f;

    public void recalculateNormals(Model model) {
        List<Vector3f> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();

        List<Vector3f> newNormals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            newNormals.add(new Vector3f(0, 0, 0));
        }

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue;

            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));

            Vector3f normal = calculatePolygonNormal(v1, v2, v3);

            for (Integer vertexIndex : vertexIndices) {
                Vector3f currentNormal = newNormals.get(vertexIndex);
                currentNormal.setX(currentNormal.getX() + normal.getX());
                currentNormal.setY(currentNormal.getY() + normal.getY());
                currentNormal.setZ(currentNormal.getZ() + normal.getZ());
            }
        }

        for (Vector3f normal : newNormals) {
//            normal = normal.normalize();
            normalizeVector(normal);
//            normal.normalizeVector(normal);
        }

        model.setNormals((ArrayList<Vector3f>) newNormals);

        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            polygon.getNormalIndices().clear();

            for (Integer vertexIndex : vertexIndices) {
                polygon.getNormalIndices().add(vertexIndex);
            }
        }
    }

    private Vector3f calculatePolygonNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = new Vector3f(
                v2.getX() - v1.getX(),
                v2.getY() - v1.getY(),
                v2.getZ() - v1.getZ()
        );

        Vector3f edge2 = new Vector3f(
                v3.getX() - v1.getX(),
                v3.getY() - v1.getY(),
                v3.getZ() - v1.getZ()
        );

        return edge1.multiplyVectorVector(edge2);
//        return crossProduct(edge1, edge2);
    }

    private Vector3f crossProduct(Vector3f a, Vector3f b) {
        return new Vector3f(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    private void normalizeVector(Vector3f vector) {
        float length = (float) Math.sqrt(
                vector.getX() * vector.getX() +
                        vector.getY() * vector.getY() +
                        vector.getZ() * vector.getZ()
        );

        if (length > EPSILON) {
            vector.setX(vector.getX() / length);
            vector.setY(vector.getY() / length);
            vector.setZ(vector.getZ() / length);
        }
    }
}

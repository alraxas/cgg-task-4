package com.cgvsu.render_engine.triangulation;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.render_engine.triangulation.model.BaseModel;
import com.cgvsu.render_engine.triangulation.model.TriangulatedModel;

import java.util.*;

public class Triangulator {

    public TriangulatedModel triangulate(BaseModel model) {
        TriangulatedModel result = new TriangulatedModel(model);

        for (Polygon polygon : model.polygons) {
            triangulatePolygonEarClipping(polygon, result, model.getVertices());
        }

        return result;
    }

    public Model processModel(Model model) {
        Model triangulatedModel = triangulateModel(model);

        recalculateNormals(triangulatedModel);

        return triangulatedModel;
    }

    // Триангуляция стандартной модели
    public Model triangulateModel(Model model) {
        Model result = new Model();
        result.setVertices(new ArrayList<>(model.getVertices()));
        result.setTextureVertices(new ArrayList<>(model.getTextureVertices()));
        result.setNormals(new ArrayList<>(model.getNormals()));

        for (Polygon polygon : model.getPolygons()) {
            triangulatePolygon(polygon, result);
        }

        return result;
    }

    private void triangulatePolygon(Polygon polygon, Model result) {
        ArrayList<Integer> vertices = polygon.getVertexIndices();
        ArrayList<Integer> textures = polygon.getTextureVertexIndices();
        ArrayList<Integer> normals = polygon.getNormalIndices();

        int vertexCount = vertices.size();

        if (vertexCount < 3) return;
        if (vertexCount == 3) {
            result.getPolygons().add(copyPolygon(polygon));
            return;
        }

        boolean hasUV = !textures.isEmpty();
        boolean hasNormals = !normals.isEmpty();

        // Простая веерная триангуляция для гарантии результата
        for (int i = 1; i < vertexCount - 1; i++) {
            Polygon triangle = new Polygon();

            triangle.getVertexIndices().add(vertices.get(0));
            triangle.getVertexIndices().add(vertices.get(i));
            triangle.getVertexIndices().add(vertices.get(i + 1));

            if (hasUV) {
                triangle.getTextureVertexIndices().add(textures.get(0));
                triangle.getTextureVertexIndices().add(textures.get(i));
                triangle.getTextureVertexIndices().add(textures.get(i + 1));
            }

            if (hasNormals) {
                triangle.getNormalIndices().add(normals.get(0));
                triangle.getNormalIndices().add(normals.get(i));
                triangle.getNormalIndices().add(normals.get(i + 1));
            }

            result.getPolygons().add(triangle);
        }
    }

    // Пересчет нормалей для модели
    public void recalculateNormals(Model model) {
        List<Vector3f> vertices = model.getVertices();
        List<Polygon> polygons = model.getPolygons();

        // Создаем список для новых нормалей
        List<Vector3f> newNormals = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            newNormals.add(new Vector3f(0, 0, 0));
        }

        // Вычисляем нормали для каждого полигона и добавляем к вершинам
        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            if (vertexIndices.size() < 3) continue;

            // Получаем вершины полигона
            Vector3f v1 = vertices.get(vertexIndices.get(0));
            Vector3f v2 = vertices.get(vertexIndices.get(1));
            Vector3f v3 = vertices.get(vertexIndices.get(2));

            // Вычисляем нормаль полигона
            Vector3f normal = calculatePolygonNormal(v1, v2, v3);

            // Добавляем нормаль к каждой вершине полигона
            for (Integer vertexIndex : vertexIndices) {
                Vector3f currentNormal = newNormals.get(vertexIndex);
                currentNormal.setX(currentNormal.getX() + normal.getX());
                currentNormal.setY(currentNormal.getY() + normal.getY());
                currentNormal.setZ(currentNormal.getZ() + normal.getZ());
            }
        }

        // Нормализуем все нормали
        for (Vector3f normal : newNormals) {
            normalizeVector(normal);
        }

        // Обновляем нормали в модели
        model.setNormals((ArrayList<Vector3f>) newNormals);

        // Обновляем индексы нормалей в полигонах
        for (Polygon polygon : polygons) {
            List<Integer> vertexIndices = polygon.getVertexIndices();
            polygon.getNormalIndices().clear();

            // Каждая вершина теперь ссылается на свою нормаль по тому же индексу
            for (int i = 0; i < vertexIndices.size(); i++) {
                polygon.getNormalIndices().add(vertexIndices.get(i));
            }
        }
    }

    // Вспомогательный метод для вычисления нормали полигона
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

        return crossProduct(edge1, edge2);
    }

    // Нормализация вектора
    private void normalizeVector(Vector3f vector) {
        float length = (float) Math.sqrt(
                vector.getX() * vector.getX() +
                        vector.getY() * vector.getY() +
                        vector.getZ() * vector.getZ()
        );

        if (length > 0) {
            vector.setX(vector.getX() / length);
            vector.setY(vector.getY() / length);
            vector.setZ(vector.getZ() / length);
        }
    }

    // Оригинальные методы остаются без изменений
    public void triangulatePolygonEarClipping(Polygon originalPolygon, TriangulatedModel result, List<Vector3f> allVertices) {
        ArrayList<Integer> vertices = originalPolygon.getVertexIndices();
        ArrayList<Integer> textures = originalPolygon.getTextureVertexIndices();
        ArrayList<Integer> normals = originalPolygon.getNormalIndices();

        int vertexCount = vertices.size();

        if (vertexCount < 3) return;
        if (vertexCount == 3) {
            result.polygons.add(copyPolygon(originalPolygon));
            return;
        }

        List<Integer> remainingVertices = new ArrayList<>(vertices);
        List<Integer> remainingTextures = new ArrayList<>(textures);
        List<Integer> remainingNormals = new ArrayList<>(normals);

        boolean hasUV = !textures.isEmpty();
        boolean hasNormals = !normals.isEmpty();

        while (remainingVertices.size() > 3) {
            boolean earFound = false;

            for (int i = 0; i < remainingVertices.size() && !earFound; i++) {
                int prev = (i - 1 + remainingVertices.size()) % remainingVertices.size();
                int curr = i;
                int next = (i + 1) % remainingVertices.size();

                if (isEar(prev, curr, next, remainingVertices, allVertices)) {
                    Polygon triangle = new Polygon();

                    triangle.getVertexIndices().add(remainingVertices.get(prev));
                    triangle.getVertexIndices().add(remainingVertices.get(curr));
                    triangle.getVertexIndices().add(remainingVertices.get(next));

                    if (hasUV) {
                        triangle.getTextureVertexIndices().add(remainingTextures.get(prev));
                        triangle.getTextureVertexIndices().add(remainingTextures.get(curr));
                        triangle.getTextureVertexIndices().add(remainingTextures.get(next));
                    }

                    if (hasNormals) {
                        triangle.getNormalIndices().add(remainingNormals.get(prev));
                        triangle.getNormalIndices().add(remainingNormals.get(curr));
                        triangle.getNormalIndices().add(remainingNormals.get(next));
                    }

                    result.polygons.add(triangle);

                    remainingVertices.remove(curr);
                    if (hasUV) remainingTextures.remove(curr);
                    if (hasNormals) remainingNormals.remove(curr);

                    earFound = true;
                }
            }

            if (!earFound) {
                triangulatePolygonFan(remainingVertices, remainingTextures, remainingNormals, result);
                break;
            }
        }

        if (remainingVertices.size() == 3) {
            Polygon lastTriangle = new Polygon();
            lastTriangle.getVertexIndices().addAll(remainingVertices);
            if (hasUV) lastTriangle.getTextureVertexIndices().addAll(remainingTextures);
            if (hasNormals) lastTriangle.getNormalIndices().addAll(remainingNormals);
            result.polygons.add(lastTriangle);
        }
    }

    private boolean isEar(int prev, int curr, int next,
                          List<Integer> vertexIndices, List<Vector3f> vertices) {
        Vector3f a = vertices.get(vertexIndices.get(prev));
        Vector3f b = vertices.get(vertexIndices.get(curr));
        Vector3f c = vertices.get(vertexIndices.get(next));

        if (!isConvex(a, b, c)) {
            return false;
        }

        for (int i = 0; i < vertexIndices.size(); i++) {
            if (i == prev || i == curr || i == next) continue;

            Vector3f p = vertices.get(vertexIndices.get(i));
            if (isPointInTriangle(p, a, b, c)) {
                return false;
            }
        }

        return true;
    }

    private boolean isConvex(Vector3f a, Vector3f b, Vector3f c) {
        Vector3f ab = new Vector3f(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
        Vector3f bc = new Vector3f(c.getX() - b.getX(), c.getY() - b.getY(), c.getZ() - b.getZ());

        Vector3f cross = crossProduct(ab, bc);
        return cross.getZ() >= 0;
    }

    private boolean isPointInTriangle(Vector3f p, Vector3f a, Vector3f b, Vector3f c) {
        Vector3f v0 = new Vector3f(c.getX() - a.getX(), c.getY() - a.getY(), c.getZ() - a.getZ());
        Vector3f v1 = new Vector3f(b.getX() - a.getX(), b.getY() - a.getY(), b.getZ() - a.getZ());
        Vector3f v2 = new Vector3f(p.getX() - a.getX(), p.getY() - a.getY(), p.getZ() - a.getZ());

        float dot00 = dotProduct(v0, v0);
        float dot01 = dotProduct(v0, v1);
        float dot02 = dotProduct(v0, v2);
        float dot11 = dotProduct(v1, v1);
        float dot12 = dotProduct(v1, v2);

        float invDenom = 1 / (dot00 * dot11 - dot01 * dot01);
        float u = (dot11 * dot02 - dot01 * dot12) * invDenom;
        float v = (dot00 * dot12 - dot01 * dot02) * invDenom;

        return (u >= 0) && (v >= 0) && (u + v < 1);
    }

    private Vector3f crossProduct(Vector3f a, Vector3f b) {
        return new Vector3f(
                a.getY() * b.getZ() - a.getZ() * b.getY(),
                a.getZ() * b.getX() - a.getX() * b.getZ(),
                a.getX() * b.getY() - a.getY() * b.getX()
        );
    }

    private float dotProduct(Vector3f a, Vector3f b) {
        return (float) (a.getX() * b.getX() + a.getY() * b.getY() + a.getZ() * b.getZ());
    }

    private void triangulatePolygonFan(List<Integer> vertices, List<Integer> textures,
                                       List<Integer> normals, TriangulatedModel result) {
        boolean hasUV = !textures.isEmpty();
        boolean hasNormals = !normals.isEmpty();

        for (int i = 1; i < vertices.size() - 1; i++) {
            Polygon triangle = new Polygon();

            triangle.getVertexIndices().add(vertices.get(0));
            triangle.getVertexIndices().add(vertices.get(i));
            triangle.getVertexIndices().add(vertices.get(i + 1));

            if (hasUV) {
                triangle.getTextureVertexIndices().add(textures.get(0));
                triangle.getTextureVertexIndices().add(textures.get(i));
                triangle.getTextureVertexIndices().add(textures.get(i + 1));
            }

            if (hasNormals) {
                triangle.getNormalIndices().add(normals.get(0));
                triangle.getNormalIndices().add(normals.get(i));
                triangle.getNormalIndices().add(normals.get(i + 1));
            }

            result.polygons.add(triangle);
        }
    }

    private Polygon copyPolygon(Polygon p) {
        Polygon copy = new Polygon();
        copy.getVertexIndices().addAll(p.getVertexIndices());

        if (!p.getTextureVertexIndices().isEmpty())
            copy.getTextureVertexIndices().addAll(p.getTextureVertexIndices());

        if (!p.getNormalIndices().isEmpty())
            copy.getNormalIndices().addAll(p.getNormalIndices());

        return copy;
    }
}
package com.cgvsu.render_engine.processing;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.util.*;

public class Triangulator {
    public Model processModel(Model model) {
        Model triangulatedModel = triangulateModel(model);

        NormalCalculator.recalculateNormals(triangulatedModel);

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
package com.cgvsu.render_engine.triangulation.model;


import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

public class ModelAdapter extends BaseModel {

    public static BaseModel fromModel(Model model) {
        BaseModel out = new BaseModel();

        out.getVertices().addAll(model.getVertices());
        out.getTextureVertices().addAll(model.getTextureVertices());
        out.getNormals().addAll(model.getNormals());

        for (Polygon p : model.getPolygons()) {
            out.getPolygons().add(copyPolygon(p));
        }

        return out;
    }

    private static Polygon copyPolygon(Polygon p) {
        Polygon copy = new Polygon();
        copy.getVertexIndices().addAll(p.getVertexIndices());

        if (!p.getTextureVertexIndices().isEmpty())
            copy.getTextureVertexIndices().addAll(p.getTextureVertexIndices());

        if (!p.getNormalIndices().isEmpty())
            copy.getNormalIndices().addAll(p.getNormalIndices());

        return copy;
    }
}

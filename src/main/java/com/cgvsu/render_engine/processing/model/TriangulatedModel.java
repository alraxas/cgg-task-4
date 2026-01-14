package com.cgvsu.render_engine.processing.model;

import java.util.ArrayList;

public class TriangulatedModel extends BaseModel {
    private final BaseModel originalModel;

    public TriangulatedModel(BaseModel original) {
        this.originalModel = original;
        this.vertices = new ArrayList<>(original.vertices);
        this.textureVertices = new ArrayList<>(original.textureVertices);
        this.normals = new ArrayList<>(original.normals);
        this.polygons = new ArrayList<>();
    }

    public BaseModel getOriginalModel() {
        return originalModel;
    }
}

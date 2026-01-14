package com.cgvsu.render_engine.processing;

import com.cgvsu.model.Model;

public class ModelProcessor {
    private final Triangulator triangulator;

    public ModelProcessor() {
        this.triangulator = new Triangulator();
    }

    public Model processModel(Model model) {
        // Триангуляция
        Model triangulatedModel = triangulator.triangulateModel(model);

        // Пересчет нормалей
        NormalCalculator.recalculateNormals(triangulatedModel);

        return triangulatedModel;
    }

    public Model triangulate(Model model) {
        return triangulator.triangulateModel(model);
    }

    public void recalculateNormals(Model model) {
        NormalCalculator.recalculateNormals(model);
    }
}
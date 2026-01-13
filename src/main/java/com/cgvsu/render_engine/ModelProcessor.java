package com.cgvsu.render_engine;

import com.cgvsu.model.Model;
//import com.cgvsu.render_engine.NormalCalculator;
import com.cgvsu.render_engine.triangulation.NormalCalculator;
import com.cgvsu.render_engine.triangulation.Triangulator;

public class ModelProcessor {
    private final Triangulator triangulator;
    private final NormalCalculator normalCalculator;

    public ModelProcessor() {
        this.triangulator = new Triangulator();
        this.normalCalculator = new NormalCalculator();
    }

    public Model processModel(Model model) {
        // Триангуляция
        Model triangulatedModel = triangulator.triangulateModel(model);

        // Пересчет нормалей
        normalCalculator.recalculateNormals(triangulatedModel);

        return triangulatedModel;
    }

    public Model triangulate(Model model) {
        return triangulator.triangulateModel(model);
    }

    public void recalculateNormals(Model model) {
        normalCalculator.recalculateNormals(model);
    }
}
package com.cgvsu.render_engine;

//import javax.vecmath.Matrix4f;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

public class TransformationPipeline {
    private Matrix4f modelMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    private Matrix4f mvpMatrix; // Model-View-Projection

    public TransformationPipeline() {
//        modelMatrix = new Matrix4f();
        modelMatrix = Matrix4f.identity();

//        viewMatrix = new Matrix4f();
        viewMatrix = Matrix4f.identity();

//        projectionMatrix = new Matrix4f();
        projectionMatrix = Matrix4f.identity();

        mvpMatrix = new Matrix4f();
    }

    public void setModelMatrix(Matrix4f model) {
        this.modelMatrix = model;
        updateMVP();
    }

    public void setViewMatrix(Matrix4f view) {
        this.viewMatrix = view;
        updateMVP();
    }

    public void setProjectionMatrix(Matrix4f projection) {
        this.projectionMatrix = projection;
        updateMVP();
    }

    private void updateMVP() {
        mvpMatrix.set(modelMatrix);
        mvpMatrix.multiply(viewMatrix);
        mvpMatrix.multiply(projectionMatrix);
    }

    public Vector3f transformVertex(
            Vector3f vertex) {
        return Matrix4f.multiplyMatrix4ByVector3(mvpMatrix, vertex);
    }

    public Matrix4f getModelMatrix() { return modelMatrix; }
    public Matrix4f getViewMatrix() { return viewMatrix; }
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    public Matrix4f getMVPMatrix() { return mvpMatrix; }

    // Нормальная матрица (для освещения)
    public Matrix4f getNormalMatrix() {
        Matrix4f normalMatrix = new Matrix4f(modelMatrix);
        normalMatrix.multiply(viewMatrix);
        normalMatrix.invert();
        normalMatrix.transpose();
        return normalMatrix;
    }
}
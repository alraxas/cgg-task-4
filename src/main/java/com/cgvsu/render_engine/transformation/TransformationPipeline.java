package com.cgvsu.render_engine.transformation;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.GraphicConveyor;

/**
 * Конвейер преобразований для 3D графики.
 * Управляет последовательностью преобразований: модель -> вид -> проекция.
 */
public class TransformationPipeline {
    private Matrix4f modelMatrix;
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    private Matrix4f modelViewProjectionMatrix;

    public TransformationPipeline() {
        modelMatrix = new Matrix4f();
        viewMatrix = new Matrix4f();
        projectionMatrix = new Matrix4f();
        modelViewProjectionMatrix = new Matrix4f();
        updateMatrices();
    }

    /**
     * Устанавливает матрицу модели.
     */
    public void setModelMatrix(Matrix4f modelMatrix) {
        this.modelMatrix = modelMatrix;
        updateMatrices();
    }

    /**
     * Устанавливает параметры модели.
     */
    public void setModelTransform(
            float scaleX, float scaleY, float scaleZ,
            float rotateX, float rotateY, float rotateZ,
            float translateX, float translateY, float translateZ) {

        this.modelMatrix = GraphicConveyor.rotateScaleTranslate(
                scaleX, scaleY, scaleZ,
                rotateX, rotateY, rotateZ,
                translateX, translateY, translateZ
        );
        updateMatrices();
    }

    /**
     * Устанавливает матрицу вида.
     */
    public void setViewMatrix(Matrix4f viewMatrix) {
        this.viewMatrix = viewMatrix;
        updateMatrices();
    }

    /**
     * Устанавливает вид камеры.
     */
    public void setLookAt(Vector3f eye, Vector3f target, Vector3f up) {
        this.viewMatrix = GraphicConveyor.lookAt(eye, target, up);
        updateMatrices();
    }

    /**
     * Устанавливает матрицу проекции.
     */
    public void setProjectionMatrix(Matrix4f projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
        updateMatrices();
    }

    /**
     * Устанавливает перспективную проекцию.
     */
    public void setPerspectiveProjection(
            float fov, float aspectRatio,
            float nearPlane, float farPlane) {

        this.projectionMatrix = GraphicConveyor.perspective(
                fov, aspectRatio, nearPlane, farPlane
        );
        updateMatrices();
    }

    /**
     * Преобразует точку из мировых координат в экранные.
     */
    public Vector3f transformPoint(Vector3f point) {
        Vector3f result = new Vector3f(point.getX(), point.getY(), point.getZ());

        // Применяем MVP матрицу
        result = com.cgvsu.math.Matrix4f.multiplyMatrix4ByVector3(
                modelViewProjectionMatrix, result
        );

        return result;
    }

    /**
     * Преобразует нормаль.
     */
    public Vector3f transformNormal(Vector3f normal) {
        // Для нормалей нужна обратная транспонированная матрица модели-вида
        Matrix4f modelViewMatrix = new Matrix4f(modelMatrix);
        modelViewMatrix.multiply(viewMatrix);
        modelViewMatrix.invert();
        modelViewMatrix.transpose();

        return Matrix4f.transformNormal(normal, modelViewMatrix);
    }

    /**
     * Обновляет комбинированные матрицы.
     */
    private void updateMatrices() {
        modelViewProjectionMatrix = new Matrix4f(modelMatrix);
        modelViewProjectionMatrix.multiply(viewMatrix);
        modelViewProjectionMatrix.multiply(projectionMatrix);
    }

    public Matrix4f getModelMatrix() { return modelMatrix; }
    public Matrix4f getViewMatrix() { return viewMatrix; }
    public Matrix4f getProjectionMatrix() { return projectionMatrix; }
    public Matrix4f getModelViewProjectionMatrix() { return modelViewProjectionMatrix; }

    /**
     * Получает матрицу модели-вида (без проекции).
     */
    public Matrix4f getModelViewMatrix() {
        Matrix4f result = new Matrix4f(modelMatrix);
        result.multiply(viewMatrix);
        return result;
    }

    /**
     * Получает обратную транспонированную матрицу для нормалей.
     */
    public Matrix4f getNormalMatrix() {
        Matrix4f modelView = getModelViewMatrix();
        modelView.invert();
        modelView.transpose();
        return modelView;
    }
}
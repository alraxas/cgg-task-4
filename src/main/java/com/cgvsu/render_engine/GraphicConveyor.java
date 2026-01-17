package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.math.Vector4f;
import javafx.scene.paint.Color;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate(
            float scaleX, float scaleY, float scaleZ,
            float rotateX, float rotateY, float rotateZ,
            float translateX, float translateY, float translateZ) {

        Matrix4f scaleMatrix = createScaleMatrix(scaleX, scaleY, scaleZ);

        // Матрицы вращения
        Matrix4f rotationMatrix = createRotationMatrix(rotateX, rotateY, rotateZ);

        // Матрица переноса
        Matrix4f translationMatrix = createTranslationMatrix(translateX, translateY, translateZ);

        // Комбинируем: сначала масштаб, потом перенос
        Matrix4f result = new Matrix4f(scaleMatrix);
        result.multiply(rotationMatrix);
        result.multiply(translationMatrix);

        return result;
    }

    public static Matrix4f rotateScaleTranslate() {
        return rotateScaleTranslate(1.0f, 1.0f, 1.0f, 0, 0, 0, 0, 0, 0);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX = new Vector3f();
        Vector3f resultY = new Vector3f();
        Vector3f resultZ = new Vector3f();

        resultZ.sub(target, eye);
        resultX.cross(up, resultZ);
        resultY.cross(resultZ, resultX);

        resultX.normalize1();
        resultY.normalize1();
        resultZ.normalize1();

        double[] matrix = new double[]{
                resultX.getX(), resultY.getX(), resultZ.getX(), 0,
                resultX.getY(), resultY.getY(), resultZ.getY(), 0,
                resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0,
                -resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        if (fov <= 0 || aspectRatio <= 0 || nearPlane <= 0 || farPlane <= nearPlane) {
            throw new IllegalArgumentException("Invalid perspective parameters");
        }
        Matrix4f result = new Matrix4f();
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        result.m00 = tangentMinusOnDegree / aspectRatio;
        result.m11 = tangentMinusOnDegree;
        result.m22 = (farPlane + nearPlane) / (farPlane - nearPlane);
        result.m23 = 1.0F;
        result.m32 = 2 * (nearPlane * farPlane) / (nearPlane - farPlane);
        return result;
    }

    public static Matrix4f createScaleMatrix(float scaleX, float scaleY, float scaleZ) {
        return new Matrix4f(new float[]{
                scaleX, 0, 0, 0,
                0, scaleY, 0, 0,
                0, 0, scaleZ, 0,
                0, 0, 0, 1
        });
    }

    public static Matrix4f createTranslationMatrix(float tx, float ty, float tz) {
        return new Matrix4f(new float[]{
                1, 0, 0, tx,
                0, 1, 0, ty,
                0, 0, 1, tz,
                0, 0, 0, 1
        });
    }

    public static Matrix4f createRotationMatrix(float angleX, float angleY, float angleZ) {
        Matrix4f rotationX = createRotationXMatrix(angleX);
        Matrix4f rotationY = createRotationYMatrix(angleY);
        Matrix4f rotationZ = createRotationZMatrix(angleZ);

        Matrix4f result = new Matrix4f(rotationZ);
        result.multiply(rotationY);
        result.multiply(rotationX);

        return result;
    }

    public static Matrix4f createRotationXMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(new float[]{
                1, 0, 0, 0,
                0, cos, -sin, 0,
                0, sin, cos, 0,
                0, 0, 0, 1
        });
    }

    public static Matrix4f createRotationYMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(new float[]{
                cos, 0, sin, 0,
                0, 1, 0, 0,
                -sin, 0, cos, 0,
                0, 0, 0, 1
        });
    }

    public static Matrix4f createRotationZMatrix(float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);

        return new Matrix4f(new float[]{
                cos, sin, 0, 0,
                -sin, cos, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        });
    }
}

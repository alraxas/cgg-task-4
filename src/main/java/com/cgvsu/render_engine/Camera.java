package com.cgvsu.render_engine;

import com.cgvsu.math.Matrix4f;
import com.cgvsu.math.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f target;
    private float fov;
    private float aspectRatio;
    private float nearPlane;
    private float farPlane;

    // Для вращения камеры
    private float pitch = 0.0f;  // Вращение вокруг X
    private float yaw = 0.0f;    // Вращение вокруг Y
    private float roll = 0.0f;   // Вращение вокруг Z

    public Camera(Vector3f position, Vector3f target,
                  float fov, float aspectRatio,
                  float nearPlane, float farPlane) {
        this.position = position;
        this.target = target;
        this.fov = fov;
        this.aspectRatio = aspectRatio;
        this.nearPlane = nearPlane;
        this.farPlane = farPlane;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setTarget(Vector3f target) {
        this.target = target;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getTarget() {
        return target;
    }

    public float getFov() {
        return fov;
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public float getNearPlane() {
        return nearPlane;
    }

    public float getFarPlane() {
        return farPlane;
    }

    public Matrix4f getViewMatrix() {
        return GraphicConveyor.lookAt(position, target);
    }

    public Matrix4f getProjectionMatrix() {
        return GraphicConveyor.perspective(fov, aspectRatio, nearPlane, farPlane);
    }

    // Движение камеры относительно ее ориентации
    public void movePosition(Vector3f translation) {
        position.add(translation);
        // Чтобы камера продолжала смотреть на цель, тоже двигаем цель
        target.add(translation);
    }

    // Вращение камеры (в градусах)
    public void rotate(float pitchDelta, float yawDelta, float rollDelta) {
        this.pitch += pitchDelta;
        this.yaw += yawDelta;
        this.roll += rollDelta;

        // Ограничиваем pitch чтобы не переворачивать камеру
        this.pitch = Math.max(-89.0f, Math.min(89.0f, this.pitch));

        // Обновляем target на основе углов вращения
        updateTargetFromRotation();
    }

    private void updateTargetFromRotation() {
        // Преобразуем углы в радианы
        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        // Вычисляем новое направление
        float x = (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        float y = (float) Math.sin(pitchRad);
        float z = (float) (Math.cos(pitchRad) * Math.cos(yawRad));

        Vector3f direction = new Vector3f(x, y, z);
        direction.normalize1();

        // Обновляем цель (на расстоянии 10 единиц от камеры)
        target.setX(position.getX() + direction.getX() * 10);
        target.setY(position.getY() + direction.getY() * 10);
        target.setZ(position.getZ() + direction.getZ() * 10);
    }

    // Зум камеры (изменение FOV)
    public void zoom(float factor) {
        fov *= factor;
        fov = Math.max(10.0f, Math.min(120.0f, fov)); // Ограничиваем FOV
    }

    // Орбитальное вращение вокруг цели
    public void orbit(float deltaYaw, float deltaPitch) {
        // Вращение камеры вокруг цели
        float radius = (float) position.distance(target);

        yaw += deltaYaw;
        pitch += deltaPitch;
        pitch = Math.max(-89.0f, Math.min(89.0f, pitch));

        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        float x = target.getX() + radius * (float) (Math.cos(pitchRad) * Math.sin(yawRad));
        float y = target.getY() + radius * (float) Math.sin(pitchRad);
        float z = target.getZ() + radius * (float) (Math.cos(pitchRad) * Math.cos(yawRad));

        position.setX(x);
        position.setY(y);
        position.setZ(z);
    }
}
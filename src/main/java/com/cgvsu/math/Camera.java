package com.cgvsu.math;

/**
 * Класс для представления камеры с улучшенным управлением
 * Поддерживает управление клавиатурой и мышью
 */
public class Camera {
    private Vector3f position;      // Позиция камеры
    private Vector3f target;         // Точка, на которую смотрит камера
    private Vector3f up;              // Вектор "вверх" для камеры
    
    // Углы для сферических координат (для управления мышью)
    private double yaw;              // Угол поворота вокруг оси Y (в радианах)
    private double pitch;            // Угол наклона вверх/вниз (в радианах)
    private double distance;         // Расстояние от камеры до цели
    
    // Параметры управления
    private double moveSpeed;        // Скорость движения камеры
    private double rotationSpeed;    // Скорость вращения камеры
    private double mouseSensitivity; // Чувствительность мыши
    
    // Ограничения углов
    private static final double MAX_PITCH = Math.PI / 2 - 0.1; // Почти 90 градусов
    private static final double MIN_PITCH = -Math.PI / 2 + 0.1; // Почти -90 градусов

    /**
     * Создает камеру с начальными параметрами
     */
    public Camera() {
        this.position = new Vector3f(0.0, 0.0, 5.0);
        this.target = new Vector3f(0.0, 0.0, 0.0);
        this.up = new Vector3f(0.0, 1.0, 0.0);
        
        this.yaw = 0.0;
        this.pitch = 0.0;
        this.distance = 5.0;
        
        this.moveSpeed = 0.1;
        this.rotationSpeed = 0.01;
        this.mouseSensitivity = 0.005;
        
        updatePositionFromAngles();
    }

    /**
     * Создает камеру с заданными параметрами
     */
    public Camera(Vector3f position, Vector3f target, Vector3f up) {
        this.position = position != null ? position : new Vector3f(0.0, 0.0, 5.0);
        this.target = target != null ? target : new Vector3f(0.0, 0.0, 0.0);
        this.up = up != null ? up.normalize() : new Vector3f(0.0, 1.0, 0.0);
        
        this.yaw = 0.0;
        this.pitch = 0.0;
        this.distance = position.subtract(target).length();
        
        this.moveSpeed = 0.1;
        this.rotationSpeed = 0.01;
        this.mouseSensitivity = 0.005;
        
        updateAnglesFromPosition();
    }

    /**
     * Возвращает матрицу вида (view matrix) для векторов-столбцов
     * @return матрица вида
     */
    public Matrix4f getViewMatrix() {
        // Вычисляем векторы камеры
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = forward.multiplyVectorVector(up).normalize();
        Vector3f cameraUp = right.multiplyVectorVector(forward).normalize();
        
        // Создаем матрицу вида (look-at matrix) для векторов-столбцов
        // Матрица вида: [right.x  right.y  right.z  -dot(right,pos)]
        //               [up.x    up.y    up.z    -dot(up,pos)]
        //               [-forward.x -forward.y -forward.z dot(forward,pos)]
        //               [0       0       0       1]
        Matrix4f view = new Matrix4f();
        
        view.set(0, 0, right.getX());
        view.set(0, 1, right.getY());
        view.set(0, 2, right.getZ());
        view.set(0, 3, -right.dot(position));
        
        view.set(1, 0, cameraUp.getX());
        view.set(1, 1, cameraUp.getY());
        view.set(1, 2, cameraUp.getZ());
        view.set(1, 3, -cameraUp.dot(position));
        
        view.set(2, 0, -forward.getX());
        view.set(2, 1, -forward.getY());
        view.set(2, 2, -forward.getZ());
        view.set(2, 3, forward.dot(position));
        
        view.set(3, 0, 0.0);
        view.set(3, 1, 0.0);
        view.set(3, 2, 0.0);
        view.set(3, 3, 1.0);
        
        return view;
    }

    /**
     * Обновляет позицию камеры на основе углов (для управления мышью)
     */
    private void updatePositionFromAngles() {
        double x = target.getX() + distance * Math.cos(pitch) * Math.sin(yaw);
        double y = target.getY() + distance * Math.sin(pitch);
        double z = target.getZ() + distance * Math.cos(pitch) * Math.cos(yaw);
        this.position = new Vector3f(x, y, z);
    }

    /**
     * Обновляет углы на основе позиции камеры
     */
    private void updateAnglesFromPosition() {
        Vector3f direction = position.subtract(target);
        this.distance = direction.length();
        
        if (distance < 1e-9) {
            return;
        }
        
        this.pitch = Math.asin(direction.getY() / distance);
        this.yaw = Math.atan2(direction.getX(), direction.getZ());
    }

    // Управление клавиатурой

    /**
     * Движение камеры вперед
     */
    public void moveForward() {
        Vector3f direction = target.subtract(position).normalize();
        position = position.add(direction.scale(moveSpeed));
        target = target.add(direction.scale(moveSpeed));
    }

    /**
     * Движение камеры назад
     */
    public void moveBackward() {
        Vector3f direction = position.subtract(target).normalize();
        position = position.add(direction.scale(moveSpeed));
        target = target.add(direction.scale(moveSpeed));
    }

    /**
     * Движение камеры влево
     */
    public void moveLeft() {
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = forward.multiplyVectorVector(up).normalize();
        Vector3f left = right.scale(-1.0);
        position = position.add(left.scale(moveSpeed));
        target = target.add(left.scale(moveSpeed));
    }

    /**
     * Движение камеры вправо
     */
    public void moveRight() {
        Vector3f forward = target.subtract(position).normalize();
        Vector3f right = forward.multiplyVectorVector(up).normalize();
        position = position.add(right.scale(moveSpeed));
        target = target.add(right.scale(moveSpeed));
    }

    /**
     * Движение камеры вверх
     */
    public void moveUp() {
        position = position.add(up.scale(moveSpeed));
        target = target.add(up.scale(moveSpeed));
    }

    /**
     * Движение камеры вниз
     */
    public void moveDown() {
        position = position.add(up.scale(-moveSpeed));
        target = target.add(up.scale(-moveSpeed));
    }

    /**
     * Вращение камеры влево
     */
    public void rotateLeft() {
        yaw -= rotationSpeed;
        updatePositionFromAngles();
    }

    /**
     * Вращение камеры вправо
     */
    public void rotateRight() {
        yaw += rotationSpeed;
        updatePositionFromAngles();
    }

    /**
     * Вращение камеры вверх
     */
    public void rotateUp() {
        pitch += rotationSpeed;
        if (pitch > MAX_PITCH) {
            pitch = MAX_PITCH;
        }
        updatePositionFromAngles();
    }

    /**
     * Вращение камеры вниз
     */
    public void rotateDown() {
        pitch -= rotationSpeed;
        if (pitch < MIN_PITCH) {
            pitch = MIN_PITCH;
        }
        updatePositionFromAngles();
    }

    // Управление мышью

    /**
     * Обработка движения мыши для вращения камеры
     * @param deltaX изменение по оси X (в пикселях)
     * @param deltaY изменение по оси Y (в пикселях)
     */
    public void processMouseMovement(double deltaX, double deltaY) {
        yaw += deltaX * mouseSensitivity;
        pitch -= deltaY * mouseSensitivity;
        
        // Ограничиваем угол наклона
        if (pitch > MAX_PITCH) {
            pitch = MAX_PITCH;
        }
        if (pitch < MIN_PITCH) {
            pitch = MIN_PITCH;
        }
        
        updatePositionFromAngles();
    }

    /**
     * Обработка прокрутки колесика мыши для приближения/отдаления
     * @param delta изменение прокрутки
     */
    public void processMouseScroll(double delta) {
        distance += delta * 0.1;
        if (distance < 0.1) {
            distance = 0.1;
        }
        updatePositionFromAngles();
    }

    // Геттеры и сеттеры

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        if (position == null) {
            throw new IllegalArgumentException("Позиция не может быть null");
        }
        this.position = position;
        updateAnglesFromPosition();
    }

    public Vector3f getTarget() {
        return target;
    }

    public void setTarget(Vector3f target) {
        if (target == null) {
            throw new IllegalArgumentException("Цель не может быть null");
        }
        this.target = target;
        updateAnglesFromPosition();
    }

    public Vector3f getUp() {
        return up;
    }

    public void setUp(Vector3f up) {
        if (up == null) {
            throw new IllegalArgumentException("Вектор 'вверх' не может быть null");
        }
        this.up = up.normalize();
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public void setMoveSpeed(double moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public double getRotationSpeed() {
        return rotationSpeed;
    }

    public void setRotationSpeed(double rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    public double getMouseSensitivity() {
        return mouseSensitivity;
    }

    public void setMouseSensitivity(double mouseSensitivity) {
        this.mouseSensitivity = mouseSensitivity;
    }

    public double getYaw() {
        return yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public double getDistance() {
        return distance;
    }
}


package com.cgvsu.math;

public class Vector3f {
    private double x;
    private double y;
    private double z;

    public Vector3f() {
        this(0.0, 0.0, 0.0);
    }

    public Vector3f(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public Vector3f add(Vector3f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector3f(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3f subtract(Vector3f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3f multiply(double scalar) {
        return new Vector3f(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3f divide(double scalar) {
        if (scalar == 0.0) {
            throw new ArithmeticException("Деление на ноль невозможно");
        }
        return new Vector3f(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3f normalize() {
        double len = length();
        if (len == 0.0) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector3f(x / len, y / len, z / len);
    }

    public static void normalizeVector(Vector3f vector) {
        float length = (float) Math.sqrt(
                vector.getX() * vector.getX() +
                        vector.getY() * vector.getY() +
                        vector.getZ() * vector.getZ()
        );

        if (length > 1e-6f) {
            vector.setX(vector.getX() / length);
            vector.setY(vector.getY() / length);
            vector.setZ(vector.getZ() / length);
        }
    }

    public double multiplyVectorScalar(Vector3f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }

    public Vector3f multiplyVectorVector(Vector3f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector3f(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }
}


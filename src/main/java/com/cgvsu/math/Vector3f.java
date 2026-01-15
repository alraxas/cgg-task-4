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

    public float getZ() {
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

    public void sub(Vector3f a, Vector3f b) {
        this.x = a.x - b.x;
        this.y = a.y - b.y;
        this.z = a.z - b.z;
    }

    public Vector3f subtract(Vector3f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector3f(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3f scale(double scalar) {
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

    public double dot(Vector3f other) {
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

    public void cross(Vector3f a, Vector3f b) {
        // Используем временные переменные на случай, если
        // a или b — это тот же самый объект, что и this.
        float resX = (float) (a.getY() * b.getZ() - a.getZ() * b.getY());
        float resY = (float) (a.getZ() * b.getX() - a.getX() * b.getZ());
        float resZ = (float) (a.getX() * b.getY() - a.getY() * b.getX());

        this.setX(resX);
        this.setY(resY);
        this.setZ(resZ);
    }

    public static Vector3f calculatePolygonNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        Vector3f edge1 = new Vector3f(
                v2.getX() - v1.getX(),
                v2.getY() - v1.getY(),
                v2.getZ() - v1.getZ()
        );
        Vector3f edge2 = new Vector3f(
                v3.getX() - v1.getX(),
                v3.getY() - v1.getY(),
                v3.getZ() - v1.getZ()
        );
        return edge1.multiplyVectorVector(edge2);
    }

}


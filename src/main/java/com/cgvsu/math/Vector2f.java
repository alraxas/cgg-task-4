package com.cgvsu.math;

//import javax.vecmath.Point2f;
//import javax.vecmath.Vector3f;

import javax.vecmath.Point2f;

public class Vector2f {
    private double x;
    private double y;

    public Vector2f() {
        this(0.0, 0.0);
    }

    public Vector2f(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2f add(Vector2f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector2f(this.x + other.x, this.y + other.y);
    }

    public Vector2f subtract(Vector2f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector2f(this.x - other.x, this.y - other.y);
    }

    public Vector2f multiply(double scalar) {
        return new Vector2f(this.x * scalar, this.y * scalar);
    }

    public Vector2f divide(double scalar) {
        if (scalar == 0.0) {
            throw new ArithmeticException("Деление на ноль невозможно");
        }
        return new Vector2f(this.x / scalar, this.y / scalar);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2f normalize() {
        double len = length();
        if (len == 0.0) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector2f(x / len, y / len);
    }

    public double multiplyVectorScalar(Vector2f other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return this.x * other.x + this.y * other.y;
    }

    public static double edgeFunction(Vector2f a, Vector2f b, Vector2f c) {
        return ((c.x - a.x) * (b.y - a.y) -
                (c.y - a.y) * (b.x - a.x));
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f((float) (vertex.getX() * width + width / 2.0F), (float) (-vertex.getY() * height + height / 2.0F));
    }
}


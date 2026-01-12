package Math;

public class Vector2 {
    private double x;
    private double y;

    public Vector2() {
        this(0.0, 0.0);
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vector2 add(Vector2 other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 subtract(Vector2 other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 multiply(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }

    public Vector2 divide(double scalar) {
        if (scalar == 0.0) {
            throw new ArithmeticException("Деление на ноль невозможно");
        }
        return new Vector2(this.x / scalar, this.y / scalar);
    }

    public double length() {
        return Math.sqrt(x * x + y * y);
    }

    public Vector2 normalize() {
        double len = length();
        if (len == 0.0) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector2(x / len, y / len);
    }
    public double multiplyVectorScalar(Vector2 other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return this.x * other.x + this.y * other.y;
    }
}


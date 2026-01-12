package Math;

public class Vector4 {
    private double x;
    private double y;
    private double z;
    private double w;

    public Vector4() {
        this(0.0, 0.0, 0.0, 0.0);
    }

    public Vector4(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
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

    public double getW() {
        return w;
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

    public void setW(double w) {
        this.w = w;
    }

    public Vector4 add(Vector4 other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector4(this.x + other.x, this.y + other.y, this.z + other.z, this.w + other.w);
    }

    public Vector4 subtract(Vector4 other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector4(this.x - other.x, this.y - other.y, this.z - other.z, this.w - other.w);
    }

    public Vector4 multiplyScalar(double scalar) {
        return new Vector4(this.x * scalar, this.y * scalar, this.z * scalar, this.w * scalar);
    }

    public Vector4 divideScalar(double scalar) {
        if (scalar == 0.0) {
            throw new ArithmeticException("Деление на ноль невозможно");
        }
        return new Vector4(this.x / scalar, this.y / scalar, this.z / scalar, this.w / scalar);
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public Vector4 normalize() {
        double len = length();
        if (len == 0.0) {
            throw new ArithmeticException("Невозможно нормализовать нулевой вектор");
        }
        return new Vector4(x / len, y / len, z / len, w / len);
    }

    public double multiplyVectorScalar(Vector4 other) {
        if (other == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return this.x * other.x + this.y * other.y + this.z * other.z + this.w * other.w;
    }
}


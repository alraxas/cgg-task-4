package com.cgvsu.math;

public class Matrix4f {

    public float m00, m01, m02, m03;
    public float m10, m11, m12, m13;
    public float m20, m21, m22, m23;
    public float m30, m31, m32, m33;

    public Matrix4f() {
        setIdentity();
    }

    public Matrix4f(float m00, float m01, float m02, float m03,
                    float m10, float m11, float m12, float m13,
                    float m20, float m21, float m22, float m23,
                    float m30, float m31, float m32, float m33) {
        this.m00 = m00; this.m01 = m01; this.m02 = m02; this.m03 = m03;
        this.m10 = m10; this.m11 = m11; this.m12 = m12; this.m13 = m13;
        this.m20 = m20; this.m21 = m21; this.m22 = m22; this.m23 = m23;
        this.m30 = m30; this.m31 = m31; this.m32 = m32; this.m33 = m33;
    }

    public Matrix4f(double m00, double m01, double m02, double m03,
                    double m10, double m11, double m12, double m13,
                    double m20, double m21, double m22, double m23,
                    double m30, double m31, double m32, double m33) {
        this.m00 = (float) m00; this.m01 = (float) m01; this.m02 = (float) m02; this.m03 = (float) m03;
        this.m10 = (float) m10; this.m11 = (float) m11; this.m12 = (float) m12; this.m13 = (float) m13;
        this.m20 = (float) m20; this.m21 = (float) m21; this.m22 = (float) m22; this.m23 = (float) m23;
        this.m30 = (float) m30; this.m31 = (float) m31; this.m32 = (float) m32; this.m33 = (float) m33;
    }

    public Matrix4f(float[] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Массив не может быть null");
        }
        if (matrix.length < 16) {
            throw new IllegalArgumentException("Массив должен содержать минимум 16 элементов");
        }
        // Массив интерпретируется как матрица в порядке row-major
        this.m00 = matrix[0];  this.m01 = matrix[1];  this.m02 = matrix[2];  this.m03 = matrix[3];
        this.m10 = matrix[4];  this.m11 = matrix[5];  this.m12 = matrix[6];  this.m13 = matrix[7];
        this.m20 = matrix[8];  this.m21 = matrix[9];  this.m22 = matrix[10]; this.m23 = matrix[11];
        this.m30 = matrix[12]; this.m31 = matrix[13]; this.m32 = matrix[14]; this.m33 = matrix[15];
    }

    public Matrix4f(double[] matrix) {
        if (matrix == null) {
            throw new IllegalArgumentException("Массив не может быть null");
        }
        if (matrix.length < 16) {
            throw new IllegalArgumentException("Массив должен содержать минимум 16 элементов");
        }
        // Массив интерпретируется как матрица в порядке row-major
        this.m00 = (float) matrix[0];  this.m01 = (float) matrix[1];  this.m02 = (float) matrix[2];  this.m03 = (float) matrix[3];
        this.m10 = (float) matrix[4];  this.m11 = (float) matrix[5];  this.m12 = (float) matrix[6];  this.m13 = (float) matrix[7];
        this.m20 = (float) matrix[8];  this.m21 = (float) matrix[9];  this.m22 = (float) matrix[10]; this.m23 = (float) matrix[11];
        this.m30 = (float) matrix[12]; this.m31 = (float) matrix[13]; this.m32 = (float) matrix[14]; this.m33 = (float) matrix[15];
    }

    public Matrix4f(Matrix4f matrix4f) {
        this.m00 = matrix4f.m00;
        this.m01 = matrix4f.m01;
        this.m02 = matrix4f.m02;
        this.m03 = matrix4f.m03;
        this.m10 = matrix4f.m10;
        this.m11 = matrix4f.m11;
        this.m12 = matrix4f.m12;
        this.m13 = matrix4f.m13;
        this.m20 = matrix4f.m20;
        this.m21 = matrix4f.m21;
        this.m22 = matrix4f.m22;
        this.m23 = matrix4f.m23;
        this.m30 = matrix4f.m30;
        this.m31 = matrix4f.m31;
        this.m32 = matrix4f.m32;
        this.m33 = matrix4f.m33;
    }

    public void setIdentity() {
        m00 = 1.0f; m01 = 0.0f; m02 = 0.0f; m03 = 0.0f;
        m10 = 0.0f; m11 = 1.0f; m12 = 0.0f; m13 = 0.0f;
        m20 = 0.0f; m21 = 0.0f; m22 = 1.0f; m23 = 0.0f;
        m30 = 0.0f; m31 = 0.0f; m32 = 0.0f; m33 = 1.0f;
    }

    public static Matrix4f identity() {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        return result;
    }

    public void setZero() {
        m00 = 0.0f; m01 = 0.0f; m02 = 0.0f; m03 = 0.0f;
        m10 = 0.0f; m11 = 0.0f; m12 = 0.0f; m13 = 0.0f;
        m20 = 0.0f; m21 = 0.0f; m22 = 0.0f; m23 = 0.0f;
        m30 = 0.0f; m31 = 0.0f; m32 = 0.0f; m33 = 0.0f;
    }

    public static Matrix4f zero() {
        Matrix4f result = new Matrix4f();
        result.setZero();
        return result;
    }

    public float get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Индекс выходит за границы матрицы 4x4");
        }
        switch (row) {
            case 0:
                switch (col) {
                    case 0: return m00;
                    case 1: return m01;
                    case 2: return m02;
                    case 3: return m03;
                }
            case 1:
                switch (col) {
                    case 0: return m10;
                    case 1: return m11;
                    case 2: return m12;
                    case 3: return m13;
                }
            case 2:
                switch (col) {
                    case 0: return m20;
                    case 1: return m21;
                    case 2: return m22;
                    case 3: return m23;
                }
            case 3:
                switch (col) {
                    case 0: return m30;
                    case 1: return m31;
                    case 2: return m32;
                    case 3: return m33;
                }
        }
        return 0.0f;
    }

    public void set(int row, int col, float value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Индекс выходит за границы матрицы 4x4");
        }
        switch (row) {
            case 0:
                switch (col) {
                    case 0: m00 = value; break;
                    case 1: m01 = value; break;
                    case 2: m02 = value; break;
                    case 3: m03 = value; break;
                }
                break;
            case 1:
                switch (col) {
                    case 0: m10 = value; break;
                    case 1: m11 = value; break;
                    case 2: m12 = value; break;
                    case 3: m13 = value; break;
                }
                break;
            case 2:
                switch (col) {
                    case 0: m20 = value; break;
                    case 1: m21 = value; break;
                    case 2: m22 = value; break;
                    case 3: m23 = value; break;
                }
                break;
            case 3:
                switch (col) {
                    case 0: m30 = value; break;
                    case 1: m31 = value; break;
                    case 2: m32 = value; break;
                    case 3: m33 = value; break;
                }
                break;
        }
    }

    public void set(int row, int col, double value) {
        set(row, col, (float) value);
    }

    public void add(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        this.m00 += other.m00; this.m01 += other.m01; this.m02 += other.m02; this.m03 += other.m03;
        this.m10 += other.m10; this.m11 += other.m11; this.m12 += other.m12; this.m13 += other.m13;
        this.m20 += other.m20; this.m21 += other.m21; this.m22 += other.m22; this.m23 += other.m23;
        this.m30 += other.m30; this.m31 += other.m31; this.m32 += other.m32; this.m33 += other.m33;
    }

    public Matrix4f add(Matrix4f other, Matrix4f result) {
        if (other == null || result == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        result.m00 = this.m00 + other.m00; result.m01 = this.m01 + other.m01; result.m02 = this.m02 + other.m02; result.m03 = this.m03 + other.m03;
        result.m10 = this.m10 + other.m10; result.m11 = this.m11 + other.m11; result.m12 = this.m12 + other.m12; result.m13 = this.m13 + other.m13;
        result.m20 = this.m20 + other.m20; result.m21 = this.m21 + other.m21; result.m22 = this.m22 + other.m22; result.m23 = this.m23 + other.m23;
        result.m30 = this.m30 + other.m30; result.m31 = this.m31 + other.m31; result.m32 = this.m32 + other.m32; result.m33 = this.m33 + other.m33;
        return result;
    }

    public void subtract(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        this.m00 -= other.m00; this.m01 -= other.m01; this.m02 -= other.m02; this.m03 -= other.m03;
        this.m10 -= other.m10; this.m11 -= other.m11; this.m12 -= other.m12; this.m13 -= other.m13;
        this.m20 -= other.m20; this.m21 -= other.m21; this.m22 -= other.m22; this.m23 -= other.m23;
        this.m30 -= other.m30; this.m31 -= other.m31; this.m32 -= other.m32; this.m33 -= other.m33;
    }

    public Matrix4f subtract(Matrix4f other, Matrix4f result) {
        if (other == null || result == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        result.m00 = this.m00 - other.m00; result.m01 = this.m01 - other.m01; result.m02 = this.m02 - other.m02; result.m03 = this.m03 - other.m03;
        result.m10 = this.m10 - other.m10; result.m11 = this.m11 - other.m11; result.m12 = this.m12 - other.m12; result.m13 = this.m13 - other.m13;
        result.m20 = this.m20 - other.m20; result.m21 = this.m21 - other.m21; result.m22 = this.m22 - other.m22; result.m23 = this.m23 - other.m23;
        result.m30 = this.m30 - other.m30; result.m31 = this.m31 - other.m31; result.m32 = this.m32 - other.m32; result.m33 = this.m33 - other.m33;
        return result;
    }

    public Vector4f multiplyVector(Vector4f vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        double x = m00 * vector.getX() + m01 * vector.getY() +
                m02 * vector.getZ() + m03 * vector.getW();
        double y = m10 * vector.getX() + m11 * vector.getY() +
                m12 * vector.getZ() + m13 * vector.getW();
        double z = m20 * vector.getX() + m21 * vector.getY() +
                m22 * vector.getZ() + m23 * vector.getW();
        double w = m30 * vector.getX() + m31 * vector.getY() +
                m32 * vector.getZ() + m33 * vector.getW();
        return new Vector4f(x, y, z, w);
    }

    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        final float x = (float) ((vertex.getX() * matrix.m00) + (vertex.getY() * matrix.m10) + (vertex.getZ() * matrix.m20) + matrix.m30);
        final float y = (float) ((vertex.getX() * matrix.m01) + (vertex.getY() * matrix.m11) + (vertex.getZ() * matrix.m21) + matrix.m31);
        final float z = (float) ((vertex.getX() * matrix.m02) + (vertex.getY() * matrix.m12) + (vertex.getZ() * matrix.m22) + matrix.m32);
        final float w = (float) ((vertex.getX() * matrix.m03) + (vertex.getY() * matrix.m13) + (vertex.getZ() * matrix.m23) + matrix.m33);
        return new Vector3f(x / w, y / w, z / w);
    }

    public static Vector3f transformNormal(Vector3f normal, Matrix4f matrix) {
        // Для нормалей используется транспонированная обратная матрица,
        // но для упрощения используем верхнюю левую 3x3 часть
        float x = (float) (normal.getX() * matrix.m00 + normal.getY() * matrix.m10 + normal.getZ() * matrix.m20);
        float y = (float) (normal.getX() * matrix.m01 + normal.getY() * matrix.m11 + normal.getZ() * matrix.m21);
        float z = (float) (normal.getX() * matrix.m02 + normal.getY() * matrix.m12 + normal.getZ() * matrix.m22);

        Vector3f result = new Vector3f(x, y, z);
        result.normalize1();
        return result;
    }

    public static javax.vecmath.Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new javax.vecmath.Point2f((float) (vertex.getX() * width + width / 2.0F), (float) (-vertex.getY() * height + height / 2.0F));
    }

    public Vector3f transformVector(Vector3f vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        Vector4f v4 = toVector4(vector);
        Vector4f result = multiplyVector(v4);
        return toVector3(result);
    }

    public void multiply(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        float t00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20 + m03 * other.m30;
        float t01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21 + m03 * other.m31;
        float t02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22 + m03 * other.m32;
        float t03 = m00 * other.m03 + m01 * other.m13 + m02 * other.m23 + m03 * other.m33;

        float t10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20 + m13 * other.m30;
        float t11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21 + m13 * other.m31;
        float t12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22 + m13 * other.m32;
        float t13 = m10 * other.m03 + m11 * other.m13 + m12 * other.m23 + m13 * other.m33;

        float t20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20 + m23 * other.m30;
        float t21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21 + m23 * other.m31;
        float t22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22 + m23 * other.m32;
        float t23 = m20 * other.m03 + m21 * other.m13 + m22 * other.m23 + m23 * other.m33;

        float t30 = m30 * other.m00 + m31 * other.m10 + m32 * other.m20 + m33 * other.m30;
        float t31 = m30 * other.m01 + m31 * other.m11 + m32 * other.m21 + m33 * other.m31;
        float t32 = m30 * other.m02 + m31 * other.m12 + m32 * other.m22 + m33 * other.m32;
        float t33 = m30 * other.m03 + m31 * other.m13 + m32 * other.m23 + m33 * other.m33;

        m00 = t00; m01 = t01; m02 = t02; m03 = t03;
        m10 = t10; m11 = t11; m12 = t12; m13 = t13;
        m20 = t20; m21 = t21; m22 = t22; m23 = t23;
        m30 = t30; m31 = t31; m32 = t32; m33 = t33;
    }

    public Matrix4f multiply(Matrix4f other, Matrix4f result) {
        if (other == null || result == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        result.m00 = m00 * other.m00 + m01 * other.m10 + m02 * other.m20 + m03 * other.m30;
        result.m01 = m00 * other.m01 + m01 * other.m11 + m02 * other.m21 + m03 * other.m31;
        result.m02 = m00 * other.m02 + m01 * other.m12 + m02 * other.m22 + m03 * other.m32;
        result.m03 = m00 * other.m03 + m01 * other.m13 + m02 * other.m23 + m03 * other.m33;

        result.m10 = m10 * other.m00 + m11 * other.m10 + m12 * other.m20 + m13 * other.m30;
        result.m11 = m10 * other.m01 + m11 * other.m11 + m12 * other.m21 + m13 * other.m31;
        result.m12 = m10 * other.m02 + m11 * other.m12 + m12 * other.m22 + m13 * other.m32;
        result.m13 = m10 * other.m03 + m11 * other.m13 + m12 * other.m23 + m13 * other.m33;

        result.m20 = m20 * other.m00 + m21 * other.m10 + m22 * other.m20 + m23 * other.m30;
        result.m21 = m20 * other.m01 + m21 * other.m11 + m22 * other.m21 + m23 * other.m31;
        result.m22 = m20 * other.m02 + m21 * other.m12 + m22 * other.m22 + m23 * other.m32;
        result.m23 = m20 * other.m03 + m21 * other.m13 + m22 * other.m23 + m23 * other.m33;

        result.m30 = m30 * other.m00 + m31 * other.m10 + m32 * other.m20 + m33 * other.m30;
        result.m31 = m30 * other.m01 + m31 * other.m11 + m32 * other.m21 + m33 * other.m31;
        result.m32 = m30 * other.m02 + m31 * other.m12 + m32 * other.m22 + m33 * other.m32;
        result.m33 = m30 * other.m03 + m31 * other.m13 + m32 * other.m23 + m33 * other.m33;
        return result;
    }

    public Matrix4f multiplyMatrix(Matrix4f other) {
        Matrix4f result = new Matrix4f();
        multiply(other, result);
        return result;
    }

    public void transpose() {
        float temp;
        temp = m01; m01 = m10; m10 = temp;
        temp = m02; m02 = m20; m20 = temp;
        temp = m03; m03 = m30; m30 = temp;
        temp = m12; m12 = m21; m21 = temp;
        temp = m13; m13 = m31; m31 = temp;
        temp = m23; m23 = m32; m32 = temp;
    }

    public Matrix4f transpose(Matrix4f result) {
        if (result == null) {
            throw new IllegalArgumentException("Матрица результата не может быть null");
        }
        result.m00 = m00; result.m01 = m10; result.m02 = m20; result.m03 = m30;
        result.m10 = m01; result.m11 = m11; result.m12 = m21; result.m13 = m31;
        result.m20 = m02; result.m21 = m12; result.m22 = m22; result.m23 = m32;
        result.m30 = m03; result.m31 = m13; result.m32 = m23; result.m33 = m33;
        return result;
    }

    public double determinant() {
        return (m00 * (m11 * (m22 * m33 - m23 * m32) - m12 * (m21 * m33 - m23 * m31) + m13 * (m21 * m32 - m22 * m31)) -
                m01 * (m10 * (m22 * m33 - m23 * m32) - m12 * (m20 * m33 - m23 * m30) + m13 * (m20 * m32 - m22 * m30)) +
                m02 * (m10 * (m21 * m33 - m23 * m31) - m11 * (m20 * m33 - m23 * m30) + m13 * (m20 * m31 - m21 * m30)) -
                m03 * (m10 * (m21 * m32 - m22 * m31) - m11 * (m20 * m32 - m22 * m30) + m12 * (m20 * m31 - m21 * m30)));
    }

    public void setScale(double sx, double sy, double sz) {
        setIdentity();
        m00 = (float) sx;
        m11 = (float) sy;
        m22 = (float) sz;
    }

    public static Matrix4f scale(double sx, double sy, double sz) {
        Matrix4f result = identity();
        result.m00 = (float) sx;
        result.m11 = (float) sy;
        result.m22 = (float) sz;
        return result;
    }

    public static Matrix4f scale(double scale) {
        return scale(scale, scale, scale);
    }

    public void setRotateX(double angle) {
        setIdentity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        m11 = cos;
        m12 = -sin;
        m21 = sin;
        m22 = cos;
    }

    public static Matrix4f rotateX(double angle) {
        Matrix4f result = identity();
        result.setRotateX(angle);
        return result;
    }

    public void setRotateY(double angle) {
        setIdentity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        m00 = cos;
        m02 = sin;
        m20 = -sin;
        m22 = cos;
    }

    public static Matrix4f rotateY(double angle) {
        Matrix4f result = identity();
        result.setRotateY(angle);
        return result;
    }

    public void setRotateZ(double angle) {
        setIdentity();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        m00 = cos;
        m01 = -sin;
        m10 = sin;
        m11 = cos;
    }

    public static Matrix4f rotateZ(double angle) {
        Matrix4f result = identity();
        result.setRotateZ(angle);
        return result;
    }

    public void setRotate(Vector3f axis, double angle) {
        if (axis == null) {
            throw new IllegalArgumentException("Ось не может быть null");
        }
        Vector3f normalizedAxis = axis.normalize();
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float oneMinusCos = 1.0f - cos;

        float x = (float) normalizedAxis.getX();
        float y = (float) normalizedAxis.getY();
        float z = (float) normalizedAxis.getZ();

        setIdentity();
        m00 = cos + x * x * oneMinusCos;
        m01 = x * y * oneMinusCos - z * sin;
        m02 = x * z * oneMinusCos + y * sin;

        m10 = y * x * oneMinusCos + z * sin;
        m11 = cos + y * y * oneMinusCos;
        m12 = y * z * oneMinusCos - x * sin;

        m20 = z * x * oneMinusCos - y * sin;
        m21 = z * y * oneMinusCos + x * sin;
        m22 = cos + z * z * oneMinusCos;
    }

    public static Matrix4f rotate(Vector3f axis, double angle) {
        Matrix4f result = identity();
        result.setRotate(axis, angle);
        return result;
    }

    public void setTranslate(double tx, double ty, double tz) {
        setIdentity();
        m03 = (float) tx;
        m13 = (float) ty;
        m23 = (float) tz;
    }

    public static Matrix4f translate(double tx, double ty, double tz) {
        Matrix4f result = identity();
        result.setTranslate(tx, ty, tz);
        return result;
    }

    public static Matrix4f translate(Vector3f translation) {
        if (translation == null) {
            throw new IllegalArgumentException("Вектор переноса не может быть null");
        }
        return translate(translation.getX(), translation.getY(), translation.getZ());
    }

    public boolean invert() {
        // Используем алгоритм с вычислением определителя и алгебраических дополнений
        double det = determinant();

        // Если определитель близок к нулю, матрица необратима
        if (Math.abs(det) < 1e-9) {
            return false;
        }

        double invDet = 1.0 / det;

        // Вычисляем алгебраические дополнения (cofactors)
        float t00 = (float) (invDet * (m11 * (m22 * m33 - m23 * m32) - m12 * (m21 * m33 - m23 * m31) + m13 * (m21 * m32 - m22 * m31)));
        float t01 = (float) (-invDet * (m01 * (m22 * m33 - m23 * m32) - m02 * (m21 * m33 - m23 * m31) + m03 * (m21 * m32 - m22 * m31)));
        float t02 = (float) (invDet * (m01 * (m12 * m33 - m13 * m32) - m02 * (m11 * m33 - m13 * m31) + m03 * (m11 * m32 - m12 * m31)));
        float t03 = (float) (-invDet * (m01 * (m12 * m23 - m13 * m22) - m02 * (m11 * m23 - m13 * m21) + m03 * (m11 * m22 - m12 * m21)));

        float t10 = (float) (-invDet * (m10 * (m22 * m33 - m23 * m32) - m12 * (m20 * m33 - m23 * m30) + m13 * (m20 * m32 - m22 * m30)));
        float t11 = (float) (invDet * (m00 * (m22 * m33 - m23 * m32) - m02 * (m20 * m33 - m23 * m30) + m03 * (m20 * m32 - m22 * m30)));
        float t12 = (float) (-invDet * (m00 * (m12 * m33 - m13 * m32) - m02 * (m10 * m33 - m13 * m30) + m03 * (m10 * m32 - m12 * m30)));
        float t13 = (float) (invDet * (m00 * (m12 * m23 - m13 * m22) - m02 * (m10 * m23 - m13 * m20) + m03 * (m10 * m22 - m12 * m20)));

        float t20 = (float) (invDet * (m10 * (m21 * m33 - m23 * m31) - m11 * (m20 * m33 - m23 * m30) + m13 * (m20 * m31 - m21 * m30)));
        float t21 = (float) (-invDet * (m00 * (m21 * m33 - m23 * m31) - m01 * (m20 * m33 - m23 * m30) + m03 * (m20 * m31 - m21 * m30)));
        float t22 = (float) (invDet * (m00 * (m11 * m33 - m13 * m31) - m01 * (m10 * m33 - m13 * m30) + m03 * (m10 * m31 - m11 * m30)));
        float t23 = (float) (-invDet * (m00 * (m11 * m23 - m13 * m21) - m01 * (m10 * m23 - m13 * m20) + m03 * (m10 * m21 - m11 * m20)));

        float t30 = (float) (-invDet * (m10 * (m21 * m32 - m22 * m31) - m11 * (m20 * m32 - m22 * m30) + m12 * (m20 * m31 - m21 * m30)));
        float t31 = (float) (invDet * (m00 * (m21 * m32 - m22 * m31) - m01 * (m20 * m32 - m22 * m30) + m02 * (m20 * m31 - m21 * m30)));
        float t32 = (float) (-invDet * (m00 * (m11 * m32 - m12 * m31) - m01 * (m10 * m32 - m12 * m30) + m02 * (m10 * m31 - m11 * m30)));
        float t33 = (float) (invDet * (m00 * (m11 * m22 - m12 * m21) - m01 * (m10 * m22 - m12 * m20) + m02 * (m10 * m21 - m11 * m20)));

        // Обновляем текущую матрицу обратной
        m00 = t00; m01 = t01; m02 = t02; m03 = t03;
        m10 = t10; m11 = t11; m12 = t12; m13 = t13;
        m20 = t20; m21 = t21; m22 = t22; m23 = t23;
        m30 = t30; m31 = t31; m32 = t32; m33 = t33;

        return true;
    }

    public static Vector4f toVector4(Vector3f v) {
        if (v == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector4f(v.getX(), v.getY(), v.getZ(), 1.0);
    }

    public static Vector3f toVector3(Vector4f v) {
        if (v == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        if (Math.abs(v.getW()) < 1e-9) {
            throw new ArithmeticException("W компонента слишком мала для деления");
        }
        return new Vector3f(v.getX() / v.getW(), v.getY() / v.getW(), v.getZ() / v.getW());
    }

    public void set(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        this.m00 = other.m00; this.m01 = other.m01; this.m02 = other.m02; this.m03 = other.m03;
        this.m10 = other.m10; this.m11 = other.m11; this.m12 = other.m12; this.m13 = other.m13;
        this.m20 = other.m20; this.m21 = other.m21; this.m22 = other.m22; this.m23 = other.m23;
        this.m30 = other.m30; this.m31 = other.m31; this.m32 = other.m32; this.m33 = other.m33;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix4f matrix4F = (Matrix4f) obj;
        return Math.abs(this.m00 - matrix4F.m00) < 1e-6f &&
                Math.abs(this.m01 - matrix4F.m01) < 1e-6f &&
                Math.abs(this.m02 - matrix4F.m02) < 1e-6f &&
                Math.abs(this.m03 - matrix4F.m03) < 1e-6f &&
                Math.abs(this.m10 - matrix4F.m10) < 1e-6f &&
                Math.abs(this.m11 - matrix4F.m11) < 1e-6f &&
                Math.abs(this.m12 - matrix4F.m12) < 1e-6f &&
                Math.abs(this.m13 - matrix4F.m13) < 1e-6f &&
                Math.abs(this.m20 - matrix4F.m20) < 1e-6f &&
                Math.abs(this.m21 - matrix4F.m21) < 1e-6f &&
                Math.abs(this.m22 - matrix4F.m22) < 1e-6f &&
                Math.abs(this.m23 - matrix4F.m23) < 1e-6f &&
                Math.abs(this.m30 - matrix4F.m30) < 1e-6f &&
                Math.abs(this.m31 - matrix4F.m31) < 1e-6f &&
                Math.abs(this.m32 - matrix4F.m32) < 1e-6f &&
                Math.abs(this.m33 - matrix4F.m33) < 1e-6f;
    }

}


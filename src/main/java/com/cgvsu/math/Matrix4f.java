package com.cgvsu.math;

public class Matrix4f {

    private double[][] data;

    public Matrix4f() {
        this.data = new double[4][4];
    }

    public Matrix4f(double m00, double m01, double m02, double m03,
                    double m10, double m11, double m12, double m13,
                    double m20, double m21, double m22, double m23,
                    double m30, double m31, double m32, double m33) {
        this.data = new double[4][4];
        data[0][0] = m00; data[0][1] = m01; data[0][2] = m02; data[0][3] = m03;
        data[1][0] = m10; data[1][1] = m11; data[1][2] = m12; data[1][3] = m13;
        data[2][0] = m20; data[2][1] = m21; data[2][2] = m22; data[2][3] = m23;
        data[3][0] = m30; data[3][1] = m31; data[3][2] = m32; data[3][3] = m33;
    }

    public Matrix4f(float[] matrix) {
        //TODO: конструктор создает матрицу из одномерного массива float
    }

    public Matrix4f(double[] matrix) {
        //TODO: конструктор создает матрицу из одномерного массива
    }

    public static Matrix4f identity() {
        Matrix4f result = new Matrix4f();
        result.data[0][0] = 1.0;
        result.data[1][1] = 1.0;
        result.data[2][2] = 1.0;
        result.data[3][3] = 1.0;
        return result;
    }

    public static Matrix4f zero() {
        return new Matrix4f();
    }

    public double get(int row, int col) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Индекс выходит за границы матрицы 4x4");
        }
        return data[row][col];
    }

    public void set(int row, int col, double value) {
        if (row < 0 || row >= 4 || col < 0 || col >= 4) {
            throw new IndexOutOfBoundsException("Индекс выходит за границы матрицы 4x4");
        }
        data[row][col] = value;
    }

    public Matrix4f add(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.data[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return result;
    }

    public Matrix4f subtract(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.data[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return result;
    }

    public Vector4f multiplyVector(Vector4f vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        double x = data[0][0] * vector.getX() + data[0][1] * vector.getY() + 
                   data[0][2] * vector.getZ() + data[0][3] * vector.getW();
        double y = data[1][0] * vector.getX() + data[1][1] * vector.getY() + 
                   data[1][2] * vector.getZ() + data[1][3] * vector.getW();
        double z = data[2][0] * vector.getX() + data[2][1] * vector.getY() + 
                   data[2][2] * vector.getZ() + data[2][3] * vector.getW();
        double w = data[3][0] * vector.getX() + data[3][1] * vector.getY() + 
                   data[3][2] * vector.getZ() + data[3][3] * vector.getW();
        return new Vector4f(x, y, z, w);
    }

    public static Vector3f multiplyMatrix4ByVector3(final javax.vecmath.Matrix4f matrix, final Vector3f vertex) {
        final double x = (vertex.getX() * matrix.m00) + (vertex.getY() * matrix.m10) + (vertex.getZ() * matrix.m20) + matrix.m30;
        final double y = (vertex.getX() * matrix.m01) + (vertex.getY() * matrix.m11) + (vertex.getZ() * matrix.m21) + matrix.m31;
        final double z = (vertex.getX() * matrix.m02) + (vertex.getY() * matrix.m12) + (vertex.getZ() * matrix.m22) + matrix.m32;
        final double w = (vertex.getX() * matrix.m03) + (vertex.getY() * matrix.m13) + (vertex.getZ() * matrix.m23) + matrix.m33;
        return new Vector3f(x / w, y / w, z / w);
//        return new javax.vecmath.Vector3f(x / w, y / w, z / w);
    }

    /**
     * Умножает матрицу на вектор-столбец Vector3 (для аффинных преобразований)
     * Вектор расширяется до Vector4 с w=1, затем преобразуется и проецируется обратно
     * @param vector вектор 3D
     * @return преобразованный вектор 3D
     */
    public Vector3f transformVector(Vector3f vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        Vector4f v4 = toVector4(vector);
        Vector4f result = multiplyVector(v4);
        return toVector3(result);
    }

    public Matrix4f multiplyMatrix(Matrix4f other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                double sum = 0.0;
                for (int k = 0; k < 4; k++) {
                    sum += this.data[i][k] * other.data[k][j];
                }
                result.data[i][j] = sum;
            }
        }
        return result;
    }

    public Matrix4f transpose() {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                result.data[i][j] = this.data[j][i];
            }
        }
        return result;
    }

    public double determinant() {
        return determinant(this.data);
    }

    private double determinant(double[][] matrix) {
        int n = matrix.length;

        if (n == 1) {
            return matrix[0][0];
        }

        if (n == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }

        double det = 0;

        for (int col = 0; col < n; col++) {
            double[][] minor = getMinor(matrix, 0, col);
            det += Math.pow(-1, col) * matrix[0][col] * determinant(minor);
        }
        return det;
    }


    private double[][] getMinor(double[][] matrix, int rowToRemove, int colToRemove) {
        int n = matrix.length;
        double[][] minor = new double[n - 1][n - 1];
        int r = 0;

        for (int i = 0; i < n; i++) {
            if (i == rowToRemove) continue;
            int c = 0;
            for (int j = 0; j < n; j++) {
                if (j == colToRemove) continue;
                minor[r][c] = matrix[i][j];
                c++;
            }
            r++;
        }
        return minor;
    }

    /**
     * Создает матрицу масштабирования для векторов-столбцов
     * @param sx масштаб по оси X
     * @param sy масштаб по оси Y
     * @param sz масштаб по оси Z
     * @return матрица масштабирования
     */
    public static Matrix4f scale(double sx, double sy, double sz) {
        Matrix4f result = identity();
        result.data[0][0] = sx;
        result.data[1][1] = sy;
        result.data[2][2] = sz;
        return result;
    }

    /**
     * Создает матрицу масштабирования по всем осям
     * @param scale единый масштаб для всех осей
     * @return матрица масштабирования
     */
    public static Matrix4f scale(double scale) {
        return scale(scale, scale, scale);
    }

    /**
     * Создает матрицу вращения вокруг оси X для векторов-столбцов
     * @param angle угол в радианах
     * @return матрица вращения
     */
    public static Matrix4f rotateX(double angle) {
        Matrix4f result = identity();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        result.data[1][1] = cos;
        result.data[1][2] = -sin;
        result.data[2][1] = sin;
        result.data[2][2] = cos;
        return result;
    }

    /**
     * Создает матрицу вращения вокруг оси Y для векторов-столбцов
     * @param angle угол в радианах
     * @return матрица вращения
     */
    public static Matrix4f rotateY(double angle) {
        Matrix4f result = identity();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        result.data[0][0] = cos;
        result.data[0][2] = sin;
        result.data[2][0] = -sin;
        result.data[2][2] = cos;
        return result;
    }

    /**
     * Создает матрицу вращения вокруг оси Z для векторов-столбцов
     * @param angle угол в радианах
     * @return матрица вращения
     */
    public static Matrix4f rotateZ(double angle) {
        Matrix4f result = identity();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        result.data[0][0] = cos;
        result.data[0][1] = -sin;
        result.data[1][0] = sin;
        result.data[1][1] = cos;
        return result;
    }

    /**
     * Создает матрицу вращения вокруг произвольной оси для векторов-столбцов
     * @param axis ось вращения (должна быть нормализована)
     * @param angle угол в радианах
     * @return матрица вращения
     */
    public static Matrix4f rotate(Vector3f axis, double angle) {
        if (axis == null) {
            throw new IllegalArgumentException("Ось не может быть null");
        }
        Vector3f normalizedAxis = axis.normalize();
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double oneMinusCos = 1.0 - cos;
        
        double x = normalizedAxis.getX();
        double y = normalizedAxis.getY();
        double z = normalizedAxis.getZ();
        
        Matrix4f result = identity();
        result.data[0][0] = cos + x * x * oneMinusCos;
        result.data[0][1] = x * y * oneMinusCos - z * sin;
        result.data[0][2] = x * z * oneMinusCos + y * sin;
        
        result.data[1][0] = y * x * oneMinusCos + z * sin;
        result.data[1][1] = cos + y * y * oneMinusCos;
        result.data[1][2] = y * z * oneMinusCos - x * sin;
        
        result.data[2][0] = z * x * oneMinusCos - y * sin;
        result.data[2][1] = z * y * oneMinusCos + x * sin;
        result.data[2][2] = cos + z * z * oneMinusCos;
        
        return result;
    }

    /**
     * Создает матрицу переноса для векторов-столбцов
     * @param tx перенос по оси X
     * @param ty перенос по оси Y
     * @param tz перенос по оси Z
     * @return матрица переноса
     */
    public static Matrix4f translate(double tx, double ty, double tz) {
        Matrix4f result = identity();
        result.data[0][3] = tx;
        result.data[1][3] = ty;
        result.data[2][3] = tz;
        return result;
    }

    /**
     * Создает матрицу переноса из вектора
     * @param translation вектор переноса
     * @return матрица переноса
     */
    public static Matrix4f translate(Vector3f translation) {
        if (translation == null) {
            throw new IllegalArgumentException("Вектор переноса не может быть null");
        }
        return translate(translation.getX(), translation.getY(), translation.getZ());
    }

    /**
     * Преобразует Vector3 в Vector4 (добавляет w=1 для аффинных преобразований)
     * @param v вектор 3D
     * @return вектор 4D
     */
    public static Vector4f toVector4(Vector3f v) {
        if (v == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        return new Vector4f(v.getX(), v.getY(), v.getZ(), 1.0);
    }

    /**
     * Преобразует Vector4 в Vector3 (проекция обратно в 3D, деление на w)
     * @param v вектор 4D
     * @return вектор 3D
     */
    public static Vector3f toVector3(Vector4f v) {
        if (v == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        if (Math.abs(v.getW()) < 1e-9) {
            throw new ArithmeticException("W компонента слишком мала для деления");
        }
        return new Vector3f(v.getX() / v.getW(), v.getY() / v.getW(), v.getZ() / v.getW());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix4f matrix4F = (Matrix4f) obj;
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (Math.abs(this.data[i][j] - matrix4F.data[i][j]) >= 1e-9) {
                    return false;
                }
            }
        }
        return true;
    }
}


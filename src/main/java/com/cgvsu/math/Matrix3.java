package com.cgvsu.math;

public class Matrix3 {
    private double[][] data;

    public Matrix3() {
        this.data = new double[3][3];
    }

    public Matrix3(double m00, double m01, double m02,
                   double m10, double m11, double m12,
                   double m20, double m21, double m22) {
        this.data = new double[3][3];
        data[0][0] = m00; data[0][1] = m01; data[0][2] = m02;
        data[1][0] = m10; data[1][1] = m11; data[1][2] = m12;
        data[2][0] = m20; data[2][1] = m21; data[2][2] = m22;
    }

    public static Matrix3 identity() {
        Matrix3 result = new Matrix3();
        result.data[0][0] = 1.0;
        result.data[1][1] = 1.0;
        result.data[2][2] = 1.0;
        return result;
    }

    public static Matrix3 zero() {
        return new Matrix3();
    }

    public double get(int row, int col) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IndexOutOfBoundsException("Индекс выходит за границы матрицы 3x3");
        }
        return data[row][col];
    }

    public void set(int row, int col, double value) {
        if (row < 0 || row >= 3 || col < 0 || col >= 3) {
            throw new IndexOutOfBoundsException("Индекс выходит за границы матрицы 3x3");
        }
        data[row][col] = value;
    }

    public Matrix3 add(Matrix3 other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        Matrix3 result = new Matrix3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.data[i][j] = this.data[i][j] + other.data[i][j];
            }
        }
        return result;
    }

    public Matrix3 subtract(Matrix3 other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        Matrix3 result = new Matrix3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result.data[i][j] = this.data[i][j] - other.data[i][j];
            }
        }
        return result;
    }

    public Vector3f multiplyVector(Vector3f vector) {
        if (vector == null) {
            throw new IllegalArgumentException("Вектор не может быть null");
        }
        double x = data[0][0] * vector.getX() + data[0][1] * vector.getY() + data[0][2] * vector.getZ();
        double y = data[1][0] * vector.getX() + data[1][1] * vector.getY() + data[1][2] * vector.getZ();
        double z = data[2][0] * vector.getX() + data[2][1] * vector.getY() + data[2][2] * vector.getZ();
        return new Vector3f(x, y, z);
    }

    public Matrix3 multiplyMatrix(Matrix3 other) {
        if (other == null) {
            throw new IllegalArgumentException("Матрица не может быть null");
        }
        Matrix3 result = new Matrix3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double sum = 0.0;
                for (int k = 0; k < 3; k++) {
                    sum += this.data[i][k] * other.data[k][j];
                }
                result.data[i][j] = sum;
            }
        }
        return result;
    }

    public Matrix3 transpose() {
        Matrix3 result = new Matrix3();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix3 matrix3 = (Matrix3) obj;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (Math.abs(this.data[i][j] - matrix3.data[i][j]) >= 1e-9) {
                    return false;
                }
            }
        }
        return true;
    }
}


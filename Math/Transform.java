package Math;

/**
 * Класс для представления аффинных преобразований модели
 * Поддерживает масштабирование, вращение и перенос
 */
public class Transform {
    private Vector3 position;      // Позиция (перенос)
    private Vector3 rotation;        // Углы вращения вокруг осей X, Y, Z (в радианах)
    private Vector3 scale;          // Масштабирование по осям X, Y, Z

    /**
     * Создает трансформацию с начальными значениями
     */
    public Transform() {
        this.position = new Vector3(0.0, 0.0, 0.0);
        this.rotation = new Vector3(0.0, 0.0, 0.0);
        this.scale = new Vector3(1.0, 1.0, 1.0);
    }

    /**
     * Создает трансформацию с заданными параметрами
     * @param position позиция
     * @param rotation углы вращения в радианах
     * @param scale масштабирование
     */
    public Transform(Vector3 position, Vector3 rotation, Vector3 scale) {
        this.position = position != null ? position : new Vector3(0.0, 0.0, 0.0);
        this.rotation = rotation != null ? rotation : new Vector3(0.0, 0.0, 0.0);
        this.scale = scale != null ? scale : new Vector3(1.0, 1.0, 1.0);
    }

    /**
     * Возвращает матрицу преобразования модели
     * Порядок преобразований: M = T * R * S (для векторов-столбцов)
     * @return матрица преобразования
     */
    public Matrix4 getMatrix() {
        // Создаем матрицы преобразований
        Matrix4 scaleMatrix = Matrix4.scale(scale.getX(), scale.getY(), scale.getZ());
        Matrix4 rotationMatrix = getRotationMatrix();
        Matrix4 translationMatrix = Matrix4.translate(position);

        // Для векторов-столбцов порядок: T * R * S
        // Последнее преобразование применяется первым
        Matrix4 result = translationMatrix.multiplyMatrix(rotationMatrix);
        result = result.multiplyMatrix(scaleMatrix);
        
        return result;
    }

    /**
     * Создает матрицу вращения из углов Эйлера
     * Порядок вращения: Z, Y, X (для векторов-столбцов)
     * @return матрица вращения
     */
    private Matrix4 getRotationMatrix() {
        Matrix4 rotX = Matrix4.rotateX(rotation.getX());
        Matrix4 rotY = Matrix4.rotateY(rotation.getY());
        Matrix4 rotZ = Matrix4.rotateZ(rotation.getZ());
        
        // Для векторов-столбцов: R = Rz * Ry * Rx
        Matrix4 result = rotZ.multiplyMatrix(rotY);
        result = result.multiplyMatrix(rotX);
        
        return result;
    }

    /**
     * Преобразует точку из локальных координат модели в мировые координаты
     * @param point точка в локальных координатах
     * @return точка в мировых координатах
     */
    public Vector3 transformPoint(Vector3 point) {
        if (point == null) {
            throw new IllegalArgumentException("Точка не может быть null");
        }
        Matrix4 transformMatrix = getMatrix();
        return transformMatrix.transformVector(point);
    }

    // Геттеры и сеттеры

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        if (position == null) {
            throw new IllegalArgumentException("Позиция не может быть null");
        }
        this.position = position;
    }

    public void setPosition(double x, double y, double z) {
        this.position = new Vector3(x, y, z);
    }

    public Vector3 getRotation() {
        return rotation;
    }

    public void setRotation(Vector3 rotation) {
        if (rotation == null) {
            throw new IllegalArgumentException("Вращение не может быть null");
        }
        this.rotation = rotation;
    }

    public void setRotation(double x, double y, double z) {
        this.rotation = new Vector3(x, y, z);
    }

    public Vector3 getScale() {
        return scale;
    }

    public void setScale(Vector3 scale) {
        if (scale == null) {
            throw new IllegalArgumentException("Масштаб не может быть null");
        }
        this.scale = scale;
    }

    public void setScale(double x, double y, double z) {
        this.scale = new Vector3(x, y, z);
    }

    public void setScale(double uniformScale) {
        this.scale = new Vector3(uniformScale, uniformScale, uniformScale);
    }

    /**
     * Сбрасывает трансформацию к начальным значениям
     */
    public void reset() {
        this.position = new Vector3(0.0, 0.0, 0.0);
        this.rotation = new Vector3(0.0, 0.0, 0.0);
        this.scale = new Vector3(1.0, 1.0, 1.0);
    }

    /**
     * Создает копию трансформации
     * @return новая трансформация с теми же параметрами
     */
    public Transform copy() {
        return new Transform(
            new Vector3(position.getX(), position.getY(), position.getZ()),
            new Vector3(rotation.getX(), rotation.getY(), rotation.getZ()),
            new Vector3(scale.getX(), scale.getY(), scale.getZ())
        );
    }
}


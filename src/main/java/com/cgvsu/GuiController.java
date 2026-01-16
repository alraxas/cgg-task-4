package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderManager;
import com.cgvsu.render_engine.lighting.Material;
import com.cgvsu.render_engine.rendering.RenderSettings;
import com.cgvsu.render_engine.texture.TextureManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GuiController {

    final private float TRANSLATION = 2.0F;
    final private float ROTATION = 2.0F;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    @FXML
    private CheckBox wireframeCheckBox;

    @FXML
    private CheckBox textureCheckBox;

    @FXML
    private CheckBox lightingCheckBox;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private Slider scaleSlider;

    @FXML
    private Slider rotationXSlider;

    @FXML
    private Slider rotationYSlider;

    @FXML
    private Slider rotationZSlider;

    @FXML
    private Label statusLabel;

    private Model mesh = null;
    private RenderManager renderManager;
    private Camera camera;
    private Timeline timeline;

    // Трансформации модели
    private float scale = 1.0f;
    private float rotateX = 0.0f;
    private float rotateY = 0.0f;
    private float rotateZ = 0.0f;
    private float translateX = 0.0f;
    private float translateY = 0.0f;
    private float translateZ = 0.0f;

    @FXML
    private void initialize() {
        // Инициализация компонентов интерфейса
        initUIComponents();

        // Инициализация камеры
        camera = new Camera(
                new Vector3f(0, 10, 50),    // Позиция камеры
                new Vector3f(0, 0, 0),      // Цель камеры
                (float) Math.toRadians(60), // Угол обзора 60 градусов
                1.0f,                       // Соотношение сторон (будет обновлено)
                0.1f,                       // Ближняя плоскость
                1000.0f                     // Дальняя плоскость
        );

        // Инициализация менеджера рендеринга
        renderManager = new RenderManager();

        // Настройка обработчиков изменения размеров canvas
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            updateCameraAspectRatio();
        });

        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            updateCameraAspectRatio();
        });

        // Инициализация таймлайна для анимации
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);

        KeyFrame frame = new KeyFrame(Duration.millis(16), event -> {
            renderFrame();
        });

        timeline.getKeyFrames().add(frame);
        timeline.play();

        updateStatus("Готов к работе. Загрузите модель.");
    }

    private void initUIComponents() {
        // Настройка слайдеров
        scaleSlider.setMin(0.1);
        scaleSlider.setMax(5.0);
        scaleSlider.setValue(1.0);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setMajorTickUnit(1.0);
        scaleSlider.setMinorTickCount(4);

        rotationXSlider.setMin(0);
        rotationXSlider.setMax(360);
        rotationXSlider.setValue(0);
        rotationXSlider.setShowTickLabels(true);
        rotationXSlider.setShowTickMarks(true);
        rotationXSlider.setMajorTickUnit(90);

        rotationYSlider.setMin(0);
        rotationYSlider.setMax(360);
        rotationYSlider.setValue(0);
        rotationYSlider.setShowTickLabels(true);
        rotationYSlider.setShowTickMarks(true);
        rotationYSlider.setMajorTickUnit(90);

        rotationZSlider.setMin(0);
        rotationZSlider.setMax(360);
        rotationZSlider.setValue(0);
        rotationZSlider.setShowTickLabels(true);
        rotationZSlider.setShowTickMarks(true);
        rotationZSlider.setMajorTickUnit(90);

        // Настройка ColorPicker
        colorPicker.setValue(Color.LIGHTBLUE);

        // Настройка CheckBox'ов
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(false);

        // Обработчики событий UI
        wireframeCheckBox.setOnAction(event -> updateRenderSettings());
        textureCheckBox.setOnAction(event -> updateRenderSettings());
        lightingCheckBox.setOnAction(event -> updateRenderSettings());
        colorPicker.setOnAction(event -> updateRenderSettings());

        scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            scale = newVal.floatValue();
            updateStatus(String.format("Масштаб: %.2f", scale));
        });

        rotationXSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotateX = (float) Math.toRadians(newVal.doubleValue());
            updateStatus(String.format("Вращение X: %.0f°", newVal.doubleValue()));
        });

        rotationYSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotateY = (float) Math.toRadians(newVal.doubleValue());
            updateStatus(String.format("Вращение Y: %.0f°", newVal.doubleValue()));
        });

        rotationZSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            rotateZ = (float) Math.toRadians(newVal.doubleValue());
            updateStatus(String.format("Вращение Z: %.0f°", newVal.doubleValue()));
        });
    }

    private void renderFrame() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (width <= 0 || height <= 0) return;

        // Очистка canvas
        canvas.getGraphicsContext2D().clearRect(0, 0, width, height);

        if (mesh != null) {
            try {
                // Применяем трансформации к модели
                applyModelTransformations();

                // Рендеринг
                renderManager.render(
                        canvas.getGraphicsContext2D(),
                        camera,
                        mesh,
                        (int) width,
                        (int) height
                );
            } catch (Exception e) {
                updateStatus("Ошибка рендеринга: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void applyModelTransformations() {
        if (mesh == null) return;

        // Здесь можно применить трансформации к модели перед рендерингом
        // В текущей архитекции трансформации применяются в GraphicConveyor
        // во время рендеринга, поэтому здесь просто обновляем настройки
    }

    private void updateRenderSettings() {
        RenderSettings settings = renderManager.getRenderSettings();

        settings.setDrawWireframe(wireframeCheckBox.isSelected());
        settings.setUseTexture(textureCheckBox.isSelected());
        settings.setUseLighting(lightingCheckBox.isSelected());
        settings.setSolidColor(colorPicker.getValue());

        updateStatus(getCurrentModeDescription());
    }

    private String getCurrentModeDescription() {
        RenderSettings settings = renderManager.getRenderSettings();
        StringBuilder mode = new StringBuilder("Режим: ");

        if (settings.isDrawWireframe()) mode.append("Каркас ");
        if (settings.isUseTexture()) mode.append("Текстура ");
        if (settings.isUseLighting()) mode.append("Освещение ");
        if (!settings.isDrawWireframe() && !settings.isUseTexture() && !settings.isUseLighting()) {
            mode.append("Сплошная заливка");
        }

        return mode.toString();
    }

    private void updateCameraAspectRatio() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        if (height > 0) {
            camera.setAspectRatio((float) (width / height));
        }
    }

    private void updateStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
        System.out.println(message);
    }

    @FXML
    private void onOpenModelMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("3D Models (*.obj)", "*.obj"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        fileChooser.setTitle("Загрузить 3D модель");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Path filePath = file.toPath();
            String fileContent = Files.readString(filePath);

            // Чтение модели
            mesh = ObjReader.read(fileContent);

            // Автоматическая триангуляция если нужно
            if (mesh != null && mesh.getPolygons() != null) {
                int triangleCount = 0;
                int otherCount = 0;

                for (var polygon : mesh.getPolygons()) {
                    if (polygon.getVertexIndices().size() == 3) {
                        triangleCount++;
                    } else {
                        otherCount++;
                    }
                }

                if (otherCount > 0) {
                    updateStatus(String.format(
                            "Модель загружена. Треугольники: %d, Другие полигоны: %d. " +
                                    "Рекомендуется триангулировать модель.",
                            triangleCount, otherCount));
                } else {
                    updateStatus(String.format(
                            "Модель загружена. Вершин: %d, Треугольников: %d",
                            mesh.getVertices().size(), triangleCount));
                }

                // Создаем материал для модели если его нет
                if (mesh.getMaterial() == null) {
                    Material material = new Material();
                    material.setBaseColor(colorPicker.getValue());
                    material.setAmbientCoefficient(0.2f);
                    material.setDiffuseCoefficient(0.7f);
                    material.setSpecularCoefficient(0.5f);
                    material.setShininess(32.0f);
                    mesh.setMaterial(material);
                }

                // Центрирование модели (опционально)
                centerModel();

            } else {
                updateStatus("Ошибка: не удалось загрузить модель");
                mesh = null;
            }

        } catch (IOException e) {
            updateStatus("Ошибка чтения файла: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            updateStatus("Ошибка загрузки модели: " + e.getMessage());
            e.printStackTrace();
            mesh = null;
        }
    }

    private void centerModel() {
        if (mesh == null || mesh.getVertices().isEmpty()) return;

        // Находим центр модели
        float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
        float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;

        for (Vector3f vertex : mesh.getVertices()) {
            float x = vertex.getX();
            float y = vertex.getY();
            float z = vertex.getZ();

            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        // Вычисляем центр
        float centerX = (minX + maxX) / 2.0f;
        float centerY = (minY + maxY) / 2.0f;
        float centerZ = (minZ + maxZ) / 2.0f;

        // Можем сохранить информацию о центре для использования
        updateStatus(String.format(
                "Центр модели: (%.2f, %.2f, %.2f). Размер: %.2f x %.2f x %.2f",
                centerX, centerY, centerZ,
                maxX - minX, maxY - minY, maxZ - minZ));
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp", "*.gif"));
        fileChooser.setTitle("Загрузить текстуру");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            // Загрузка текстуры через менеджер
            renderManager.loadTexture(file.getAbsolutePath());
            textureCheckBox.setSelected(true);
            updateRenderSettings();
            updateStatus("Текстура загружена: " + file.getName());

        } catch (Exception e) {
            updateStatus("Ошибка загрузки текстуры: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onResetViewMenuItemClick() {
        // Сброс камеры
        camera = new Camera(
                new Vector3f(0, 10, 50),
                new Vector3f(0, 0, 0),
                (float) Math.toRadians(60),
                camera.getAspectRatio(),
                0.1f,
                1000.0f
        );

        // Сброс трансформаций модели
        scale = 1.0f;
        rotateX = 0.0f;
        rotateY = 0.0f;
        rotateZ = 0.0f;

        scaleSlider.setValue(1.0);
        rotationXSlider.setValue(0);
        rotationYSlider.setValue(0);
        rotationZSlider.setValue(0);

        updateStatus("Вид сброшен");
    }

    @FXML
    private void onExitMenuItemClick() {
        timeline.stop();
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    // ============ ОБРАБОТЧИКИ ДВИЖЕНИЯ КАМЕРЫ ============

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, -TRANSLATION));
        updateStatus("Камера: вперед");
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, 0, TRANSLATION));
        updateStatus("Камера: назад");
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(TRANSLATION, 0, 0));
        updateStatus("Камера: влево");
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(-TRANSLATION, 0, 0));
        updateStatus("Камера: вправо");
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, TRANSLATION, 0));
        updateStatus("Камера: вверх");
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        camera.movePosition(new Vector3f(0, -TRANSLATION, 0));
        updateStatus("Камера: вниз");
    }

    @FXML
    public void handleCameraRotateLeft(ActionEvent actionEvent) {
        camera.rotate(0, -ROTATION, 0);
        updateStatus("Камера: поворот влево");
    }

    @FXML
    public void handleCameraRotateRight(ActionEvent actionEvent) {
        camera.rotate(0, ROTATION, 0);
        updateStatus("Камера: поворот вправо");
    }

    @FXML
    public void handleCameraRotateUp(ActionEvent actionEvent) {
        camera.rotate(-ROTATION, 0, 0);
        updateStatus("Камера: поворот вверх");
    }

    @FXML
    public void handleCameraRotateDown(ActionEvent actionEvent) {
        camera.rotate(ROTATION, 0, 0);
        updateStatus("Камера: поворот вниз");
    }

    @FXML
    public void handleZoomIn(ActionEvent actionEvent) {
        camera.zoom(1.1f);
        updateStatus("Увеличение");
    }

    @FXML
    public void handleZoomOut(ActionEvent actionEvent) {
        camera.zoom(0.9f);
        updateStatus("Уменьшение");
    }

    // ============ ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ============

    @FXML
    private void onWireframeOnlyAction() {
        wireframeCheckBox.setSelected(true);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(false);
        updateRenderSettings();
    }

    @FXML
    private void onSolidOnlyAction() {
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(false);
        updateRenderSettings();
    }

    @FXML
    private void onTexturedOnlyAction() {
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(true);
        lightingCheckBox.setSelected(false);
        updateRenderSettings();
    }

    @FXML
    private void onLitOnlyAction() {
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(true);
        updateRenderSettings();
    }

    @FXML
    private void onAllFeaturesAction() {
        wireframeCheckBox.setSelected(true);
        textureCheckBox.setSelected(true);
        lightingCheckBox.setSelected(true);
        updateRenderSettings();
    }

    @FXML
    private void onClearTextureAction() {
        renderManager.setTexture(null);
        textureCheckBox.setSelected(false);
        updateRenderSettings();
        updateStatus("Текстура очищена");
    }

    @FXML
    private void onToggleLightFollowCamera() {
        boolean current = renderManager.getSceneLighting().isLightFollowsCamera();
        renderManager.getSceneLighting().setLightFollowsCamera(!current);
        updateStatus("Освещение следует за камерой: " + (!current ? "ВКЛ" : "ВЫКЛ"));
    }

    @FXML
    private void onAboutAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("3D Viewer");
        alert.setContentText("Программа для просмотра 3D моделей в формате OBJ\n" +
                "Реализованы:\n" +
                "- Загрузка моделей .obj\n" +
                "- Режимы отображения: каркас, сплошная заливка\n" +
                "- Текстурирование\n" +
                "- Освещение\n" +
                "- Управление камерой\n" +
                "\nВерсия 1.0");
        alert.showAndWait();
    }
}
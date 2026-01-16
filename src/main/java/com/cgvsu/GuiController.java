package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.RenderEngine;
import com.cgvsu.render_engine.lighting.LightingModel;
import com.cgvsu.render_engine.rendering.RenderSettings;
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

    final private float TRANSLATION = 5.0F;
    final private float ROTATION = 5.0F;

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
    private Label statusLabel;

    private Model mesh = null;
    private RenderEngine renderEngine;
    private Camera camera;
    private Timeline timeline;

    // Настройки рендеринга
    private RenderSettings renderSettings;
    private LightingModel lightingModel;

    // Текущий цвет заливки
    private Color fillColor = Color.LIGHTBLUE;

    // Масштаб модели
    private float modelScale = 1.0f;

    @FXML
    private void initialize() {
        // Инициализация компонентов
        initUIComponents();

        // Инициализация камеры (старая версия, которая работала)
        camera = new Camera(
                new Vector3f(0, 10, 100),    // Позиция камеры
                new Vector3f(0, 0, 0),       // Цель камеры
                1.0F,                         // FOV
                1.0F,                         // Aspect ratio (будет обновлено)
                0.01F,                        // Near plane
                100.0F                        // Far plane
        );

        // Инициализация рендерера
        renderEngine = new RenderEngine();

        // Инициализация модели освещения
        lightingModel = new LightingModel();
        lightingModel.setAmbientIntensity(0.3f);
        lightingModel.setDiffuseIntensity(0.7f);
        lightingModel.setSpecularIntensity(0.5f);

        // Настройка рендерера с освещением
        renderEngine.setLightingModel(lightingModel);

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

        updateStatus("Ready to load model. Use File -> Load Model");
    }

    private void initUIComponents() {
        // Настройка слайдера масштаба
        scaleSlider.setMin(0.1);
        scaleSlider.setMax(5.0);
        scaleSlider.setValue(1.0);
        scaleSlider.setShowTickLabels(true);
        scaleSlider.setShowTickMarks(true);
        scaleSlider.setMajorTickUnit(1.0);
        scaleSlider.setMinorTickCount(4);

        // Настройка ColorPicker
        colorPicker.setValue(Color.LIGHTBLUE);

        // Настройка CheckBox'ов
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(false);

        // Обработчики событий UI
        wireframeCheckBox.setOnAction(event -> updateRenderSettings());
        textureCheckBox.setOnAction(event -> {
            updateRenderSettings();
            if (textureCheckBox.isSelected()) {
                updateStatus("Texture mode selected (not implemented in basic version)");
            }
        });

        lightingCheckBox.setOnAction(event -> {
            updateRenderSettings();
            if (lightingCheckBox.isSelected()) {
                updateStatus("Lighting mode selected");
            }
        });

        colorPicker.setOnAction(event -> {
            fillColor = colorPicker.getValue();
            updateStatus("Color changed to: " + fillColor.toString());
        });

        scaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            modelScale = newVal.floatValue();
            updateStatus(String.format("Model scale: %.2f", modelScale));
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
                // Определяем режим рендеринга
                boolean drawWireframe = wireframeCheckBox.isSelected();
                boolean useLighting = lightingCheckBox.isSelected();

                if (drawWireframe) {
                    // Рендерим только каркас
                    RenderEngine.renderWireframe(
                            canvas.getGraphicsContext2D(),
                            camera,
                            mesh,
                            (int) width,
                            (int) height
                    );
                } else if (useLighting) {
                    // Рендерим с освещением
                    // Направление света (от камеры к цели)
                    Vector3f lightDirection = new Vector3f(
                            camera.getTarget().getX() - camera.getPosition().getX(),
                            camera.getTarget().getY() - camera.getPosition().getY(),
                            camera.getTarget().getZ() - camera.getPosition().getZ()
                    );
                    lightDirection.normalize1();

                    // Обновляем освещение с текущей камерой
                    lightingModel.setCamera(camera);

                    // Рендеринг с освещением
                    renderEngine.renderWithLighting(
                            canvas.getGraphicsContext2D(),
                            camera,
                            mesh,
                            (int) width,
                            (int) height,
                            fillColor,
                            lightDirection
                    );
                } else {
                    // Рендерим сплошную заливку (базовый режим)
                    renderEngine.renderSolid(
                            canvas.getGraphicsContext2D(),
                            camera,
                            mesh,
                            (int) width,
                            (int) height,
                            fillColor
                    );
                }

            } catch (Exception e) {
                updateStatus("Rendering error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void updateRenderSettings() {
        // Обновляем статус в зависимости от выбранных опций
        StringBuilder mode = new StringBuilder("Mode: ");

        if (wireframeCheckBox.isSelected()) {
            mode.append("Wireframe ");
        } else if (lightingCheckBox.isSelected()) {
            mode.append("Solid with Lighting ");
        } else if (textureCheckBox.isSelected()) {
            mode.append("Textured ");
        } else {
            mode.append("Solid fill ");
        }

        updateStatus(mode.toString());
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
        fileChooser.setTitle("Load 3D Model");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Path filePath = file.toPath();
            String fileContent = Files.readString(filePath);

            // Чтение модели
            mesh = ObjReader.read(fileContent);

            if (mesh != null) {
                int vertexCount = mesh.getVertices().size();
                int polygonCount = mesh.getPolygons().size();

                updateStatus(String.format(
                        "Model loaded: %s\nVertices: %d, Polygons: %d",
                        file.getName(), vertexCount, polygonCount));

                // Центрирование камеры на модели
                centerCameraOnModel();

            } else {
                updateStatus("Error: Failed to load model");
                mesh = null;
            }

        } catch (IOException e) {
            updateStatus("File read error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            updateStatus("Model loading error: " + e.getMessage());
            e.printStackTrace();
            mesh = null;
        }
    }

    private void centerCameraOnModel() {
        if (mesh == null || mesh.getVertices().isEmpty()) return;

        // Находим границы модели
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

        // Вычисляем размер модели
        float sizeX = maxX - minX;
        float sizeY = maxY - minY;
        float sizeZ = maxZ - minZ;
        float maxSize = Math.max(sizeX, Math.max(sizeY, sizeZ));

        // Настраиваем камеру
        float cameraDistance = maxSize * 2.0f; // Расстояние камеры пропорционально размеру модели
        cameraDistance = Math.max(cameraDistance, 50.0f); // Минимальное расстояние

        camera.setPosition(new Vector3f(0, maxSize * 0.5f, cameraDistance));
        camera.setTarget(new Vector3f(centerX, centerY, centerZ));

        updateStatus(String.format(
                "Model centered. Size: %.1f x %.1f x %.1f",
                sizeX, sizeY, sizeZ));
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        // Базовая реализация загрузки текстуры
        // В полной версии нужно использовать TextureManager
        updateStatus("Texture loading not fully implemented in basic version");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            updateStatus("Texture selected: " + file.getName() + " (loading not implemented)");
            // Здесь должен быть код загрузки текстуры через TextureManager
        }
    }

    @FXML
    private void onResetViewMenuItemClick() {
        // Сброс камеры к виду по умолчанию
        if (mesh != null) {
            centerCameraOnModel();
        } else {
            camera = new Camera(
                    new Vector3f(0, 10, 100),
                    new Vector3f(0, 0, 0),
                    1.0F,
                    camera.getAspectRatio(),
                    0.01F,
                    100.0F
            );
        }

        // Сброс настроек
        modelScale = 1.0f;
        scaleSlider.setValue(1.0);
        fillColor = Color.LIGHTBLUE;
        colorPicker.setValue(fillColor);

        updateStatus("View reset");
    }

    @FXML
    private void onExitMenuItemClick() {
        timeline.stop();
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onAboutAction() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("3D Model Viewer");
        alert.setContentText("Simple 3D model viewer for OBJ files\n" +
                "Features:\n" +
                "- Load .obj models\n" +
                "- Wireframe rendering\n" +
                "- Solid color rendering\n" +
                "- Basic lighting\n" +
                "- Camera controls\n" +
                "\nBasic Version");
        alert.showAndWait();
    }

    // ============ ОБРАБОТЧИКИ ДВИЖЕНИЯ КАМЕРЫ ============

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        // Движение вперед - приближение к цели
        Vector3f direction = new Vector3f(
                camera.getTarget().getX() - camera.getPosition().getX(),
                camera.getTarget().getY() - camera.getPosition().getY(),
                camera.getTarget().getZ() - camera.getPosition().getZ()
        );
        direction.normalize1();
        direction.scale(TRANSLATION);

        camera.movePosition(direction);
        updateStatus("Camera: forward");
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        // Движение назад - отдаление от цели
        Vector3f direction = new Vector3f(
                camera.getPosition().getX() - camera.getTarget().getX(),
                camera.getPosition().getY() - camera.getTarget().getY(),
                camera.getPosition().getZ() - camera.getTarget().getZ()
        );
        direction.normalize1();
        direction.scale(TRANSLATION);

        camera.movePosition(direction);
        updateStatus("Camera: backward");
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        // Движение влево
        Vector3f direction = new Vector3f(-TRANSLATION, 0, 0);
        camera.movePosition(direction);
        updateStatus("Camera: left");
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        // Движение вправо
        Vector3f direction = new Vector3f(TRANSLATION, 0, 0);
        camera.movePosition(direction);
        updateStatus("Camera: right");
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        // Движение вверх
        Vector3f direction = new Vector3f(0, TRANSLATION, 0);
        camera.movePosition(direction);
        updateStatus("Camera: up");
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        // Движение вниз
        Vector3f direction = new Vector3f(0, -TRANSLATION, 0);
        camera.movePosition(direction);
        updateStatus("Camera: down");
    }

    @FXML
    public void handleCameraRotateLeft(ActionEvent actionEvent) {
        // Вращение камеры влево вокруг цели
        orbitCamera(0, -ROTATION);
        updateStatus("Camera: rotate left");
    }

    @FXML
    public void handleCameraRotateRight(ActionEvent actionEvent) {
        // Вращение камеры вправо вокруг цели
        orbitCamera(0, ROTATION);
        updateStatus("Camera: rotate right");
    }

    @FXML
    public void handleCameraRotateUp(ActionEvent actionEvent) {
        // Вращение камеры вверх
        orbitCamera(-ROTATION, 0);
        updateStatus("Camera: rotate up");
    }

    @FXML
    public void handleCameraRotateDown(ActionEvent actionEvent) {
        // Вращение камеры вниз
        orbitCamera(ROTATION, 0);
        updateStatus("Camera: rotate down");
    }

    @FXML
    public void handleZoomIn(ActionEvent actionEvent) {
        // Увеличение - уменьшаем FOV
        float currentFov = camera.getFov();
        camera.setFov(currentFov * 0.9f);
        updateStatus("Zoom in");
    }

    @FXML
    public void handleZoomOut(ActionEvent actionEvent) {
        // Уменьшение - увеличиваем FOV
        float currentFov = camera.getFov();
        camera.setFov(currentFov * 1.1f);
        updateStatus("Zoom out");
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============

    private void orbitCamera(float deltaPitch, float deltaYaw) {
        // Простая орбитальная камера
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();

        // Вычисляем расстояние до цели
        float dx = position.getX() - target.getX();
        float dy = position.getY() - target.getY();
        float dz = position.getZ() - target.getZ();

        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Преобразуем в сферические координаты
        float pitch = (float) Math.asin(dy / distance);
        float yaw = (float) Math.atan2(dx, dz);

        // Добавляем изменения
        pitch += Math.toRadians(deltaPitch);
        yaw += Math.toRadians(deltaYaw);

        // Ограничиваем pitch
        pitch = (float) Math.max(-Math.PI/2 + 0.1, Math.min(Math.PI/2 - 0.1, pitch));

        // Преобразуем обратно в декартовы координаты
        float newX = target.getX() + distance * (float) (Math.sin(yaw) * Math.cos(pitch));
        float newY = target.getY() + distance * (float) Math.sin(pitch);
        float newZ = target.getZ() + distance * (float) (Math.cos(yaw) * Math.cos(pitch));

        camera.setPosition(new Vector3f(newX, newY, newZ));
    }

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
        textureCheckBox.setSelected(false);
        updateRenderSettings();
        updateStatus("Texture cleared");
    }

    @FXML
    private void onToggleLightFollowCamera() {
        if (lightingModel != null) {
            boolean current = lightingModel.isLightFollowsCamera();
            lightingModel.setLightFollowsCamera(!current);
            updateStatus("Light follows camera: " + (!current ? "ON" : "OFF"));
        }
    }
}
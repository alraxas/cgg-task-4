package com.cgvsu;

import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.objreader.ObjReader;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.processing.ModelProcessor;
import com.cgvsu.render_engine.rendering.UnifiedRenderer;
import com.cgvsu.render_engine.texture.Texture;
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
    private UnifiedRenderer renderer;
    private Camera camera;
    private Timeline timeline;
    private ModelProcessor modelProcessor;

    // Текущий цвет заливки
    private Color fillColor = Color.LIGHTBLUE;
    // Масштаб модели
    private float modelScale = 1.0f;
    // Текущая текстура
    private Texture currentTexture = null;

    @FXML
    private void initialize() {
        // Инициализация компонентов
        initUIComponents();

        // Инициализация камеры
        camera = new Camera(
                new Vector3f(0, 10, 100),    // Позиция камеры
                new Vector3f(0, 0, 0),       // Цель камеры
                1.0F,                         // FOV
                1.0F,                         // Aspect ratio (будет обновлено)
                0.01F,                        // Near plane
                100.0F                        // Far plane
        );
        renderer = new UnifiedRenderer();
        modelProcessor = new ModelProcessor();

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
        fillColor = Color.LIGHTBLUE;

        // Настройка CheckBox'ов
        wireframeCheckBox.setSelected(false);
        textureCheckBox.setSelected(false);
        lightingCheckBox.setSelected(false);

        // Обработчики событий UI
        wireframeCheckBox.setOnAction(event -> updateRenderSettings());
        textureCheckBox.setOnAction(event -> {
            updateRenderSettings();
            if (textureCheckBox.isSelected() && currentTexture == null) {
                updateStatus("Texture mode selected - please load a texture first");
            }
        });

        lightingCheckBox.setOnAction(event -> {
            updateRenderSettings();
            updateStatus("Lighting mode selected");
        });

        colorPicker.setOnAction(event -> {
            fillColor = colorPicker.getValue();
            renderer.setSolidColor(fillColor);
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

        if (mesh != null) {
            try {
                updateRenderSettings();

                renderer.render(
                        canvas.getGraphicsContext2D(),
                        camera,
                        mesh,
                        (int) width,
                        (int) height
                );

            } catch (Exception e) {
                updateStatus("Rendering error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Если нет модели, просто очищаем экран
            canvas.getGraphicsContext2D().clearRect(0, 0, width, height);
        }
    }

    private void updateRenderSettings() {
        if (renderer == null) return;

        // Обновляем все настройки рендеринга
        renderer.getRenderSettings().setDrawWireframe(wireframeCheckBox.isSelected());
        renderer.getRenderSettings().setUseTexture(textureCheckBox.isSelected());
        renderer.getRenderSettings().setUseLighting(lightingCheckBox.isSelected());
        renderer.getRenderSettings().setSolidColor(fillColor);

        // Обновляем статус в зависимости от выбранных опций
        StringBuilder mode = new StringBuilder("Mode: ");

        if (wireframeCheckBox.isSelected()) mode.append("Wireframe ");
        if (textureCheckBox.isSelected()) mode.append("Textured ");
        if (lightingCheckBox.isSelected()) mode.append("Lit ");

        if (!wireframeCheckBox.isSelected() && !textureCheckBox.isSelected() && !lightingCheckBox.isSelected()) {
            mode.append("Solid fill");
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
//        System.out.println(message);
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

                mesh = modelProcessor.processModel(mesh);

                int triangulatedPolygonCount = mesh.getPolygons().size();

                updateStatus(String.format(
                        "Model loaded: %s\n" +
                                "Original: Vertices: %d, Polygons: %d\n" +
                                "Triangulated: Polygons: %d",
                        file.getName(),
                        vertexCount,
                        polygonCount,
                        triangulatedPolygonCount));
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
        float cameraDistance = Math.max(maxSize * 2.0f, 50.0f);

        camera.setPosition(new Vector3f(centerX, centerY + sizeY * 0.3f, centerZ + cameraDistance));
        camera.setTarget(new Vector3f(centerX, centerY, centerZ));

        updateStatus(String.format(
                "Model centered. Size: %.1f x %.1f x %.1f",
                sizeX, sizeY, sizeZ));
    }

    @FXML
    private void onLoadTextureMenuItemClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.bmp"));
        fileChooser.setTitle("Load Texture");

        File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                // Загружаем текстуру через UnifiedRenderer
                renderer.loadTexture(file.getAbsolutePath());
                textureCheckBox.setSelected(true);
                updateRenderSettings();
                updateStatus("Texture loaded: " + file.getName());

            } catch (Exception e) {
                updateStatus("Error loading texture: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onResetViewMenuItemClick() {
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
        renderer.setSolidColor(fillColor);

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
        alert.setHeaderText("3D Model Viewer with UnifiedRenderer");
        alert.setContentText("3D model viewer for OBJ files\n" +
                "Features:\n" +
                "- Load .obj models\n" +
                "- Wireframe rendering\n" +
                "- Solid color rendering\n" +
                "- Textures support\n" +
                "- Lighting effects\n" +
                "- Camera controls\n" +
                "\nUsing UnifiedRenderer architecture");
        alert.showAndWait();
    }

    // ============ ОБРАБОТЧИКИ ДВИЖЕНИЯ КАМЕРЫ ============

    @FXML
    public void handleCameraForward(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f(0, 0, -TRANSLATION);
        camera.movePosition(direction);
        updateStatus("Camera: forward");
    }

    @FXML
    public void handleCameraBackward(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f(0, 0, TRANSLATION);
        camera.movePosition(direction);
        updateStatus("Camera: backward");
    }

    @FXML
    public void handleCameraLeft(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f(-TRANSLATION, 0, 0);
        camera.movePosition(direction);
        updateStatus("Camera: left");
    }

    @FXML
    public void handleCameraRight(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f(TRANSLATION, 0, 0);
        camera.movePosition(direction);
        updateStatus("Camera: right");
    }

    @FXML
    public void handleCameraUp(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f(0, TRANSLATION, 0);
        camera.movePosition(direction);
        updateStatus("Camera: up");
    }

    @FXML
    public void handleCameraDown(ActionEvent actionEvent) {
        Vector3f direction = new Vector3f(0, -TRANSLATION, 0);
        camera.movePosition(direction);
        updateStatus("Camera: down");
    }

    @FXML
    public void handleCameraRotateLeft(ActionEvent actionEvent) {
        orbitCamera(0, -ROTATION);
        updateStatus("Camera: rotate left");
    }

    @FXML
    public void handleCameraRotateRight(ActionEvent actionEvent) {
        orbitCamera(0, ROTATION);
        updateStatus("Camera: rotate right");
    }

    @FXML
    public void handleCameraRotateUp(ActionEvent actionEvent) {
        orbitCamera(-ROTATION, 0);
        updateStatus("Camera: rotate up");
    }

    @FXML
    public void handleCameraRotateDown(ActionEvent actionEvent) {
        orbitCamera(ROTATION, 0);
        updateStatus("Camera: rotate down");
    }

    @FXML
    public void handleZoomIn(ActionEvent actionEvent) {
        float currentFov = camera.getFov();
        camera.setFov(currentFov * 0.9f);
        updateStatus("Zoom in");
    }

    @FXML
    public void handleZoomOut(ActionEvent actionEvent) {
        float currentFov = camera.getFov();
        camera.setFov(currentFov * 1.1f);
        updateStatus("Zoom out");
    }

    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ============

    private void orbitCamera(float deltaPitch, float deltaYaw) {
        Vector3f position = camera.getPosition();
        Vector3f target = camera.getTarget();

        float dx = position.getX() - target.getX();
        float dy = position.getY() - target.getY();
        float dz = position.getZ() - target.getZ();

        float distance = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float pitch = (float) Math.asin(dy / distance);
        float yaw = (float) Math.atan2(dx, dz);

        pitch += Math.toRadians(deltaPitch);
        yaw += Math.toRadians(deltaYaw);

        pitch = (float) Math.max(-Math.PI/2 + 0.1, Math.min(Math.PI/2 - 0.1, pitch));

        float newX = target.getX() + distance * (float) (Math.sin(yaw) * Math.cos(pitch));
        float newY = target.getY() + distance * (float) Math.sin(pitch);
        float newZ = target.getZ() + distance * (float) (Math.cos(yaw) * Math.cos(pitch));

        camera.setPosition(new Vector3f(newX, newY, newZ));
    }

    // ============ БЫСТРЫЕ КОМАНДЫ ============

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
        renderer.setTexture(null);
        textureCheckBox.setSelected(false);
        updateRenderSettings();
        updateStatus("Texture cleared");
    }

    @FXML
    private void onToggleLightFollowCamera() {
        if (renderer != null && renderer.getSceneLighting() != null) {
            boolean current = renderer.getSceneLighting().isLightFollowsCamera();
            renderer.getSceneLighting().setLightFollowsCamera(!current);
            updateStatus("Light follows camera: " + (!current ? "ON" : "OFF"));
        }
    }
}
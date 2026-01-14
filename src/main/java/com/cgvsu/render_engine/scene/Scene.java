package com.cgvsu.render_engine.scene;

import com.cgvsu.model.Model;
import com.cgvsu.render_engine.Camera;
import com.cgvsu.render_engine.texture.Texture;
import java.util.ArrayList;
import java.util.List;

public class Scene {
    private List<Model> models;
    private List<Camera> cameras;
    private List<Texture> textures;
    private int activeCameraIndex;

    public Scene() {
        this.models = new ArrayList<>();
        this.cameras = new ArrayList<>();
        this.textures = new ArrayList<>();
        this.activeCameraIndex = 0;
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public void removeModel(Model model) {
        models.remove(model);
    }

    public List<Model> getModels() {
        return models;
    }

    public Model getModel(int index) {
        return models.get(index);
    }

    public void addCamera(Camera camera) {
        cameras.add(camera);
    }

    public void removeCamera(Camera camera) {
        cameras.remove(camera);
    }

    public List<Camera> getCameras() {
        return cameras;
    }

    public Camera getCamera(int index) {
        return cameras.get(index);
    }

    public Camera getActiveCamera() {
        if (cameras.isEmpty()) {
            return null;
        }
        return cameras.get(activeCameraIndex);
    }

    public void setActiveCamera(int index) {
        if (index >= 0 && index < cameras.size()) {
            activeCameraIndex = index;
        }
    }

    public void setActiveCamera(Camera camera) {
        int index = cameras.indexOf(camera);
        if (index != -1) {
            activeCameraIndex = index;
        }
    }

    public void addTexture(Texture texture) {
        textures.add(texture);
    }

    public Texture getTexture(int index) {
        return textures.get(index);
    }

    public List<Texture> getTextures() {
        return textures;
    }

    public boolean hasActiveCamera() {
        return !cameras.isEmpty() && activeCameraIndex < cameras.size();
    }

    public int getActiveCameraIndex() {
        return activeCameraIndex;
    }
}
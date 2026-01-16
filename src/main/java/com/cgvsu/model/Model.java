package com.cgvsu.model;
import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.render_engine.lighting.Material;
import com.cgvsu.render_engine.texture.Texture;
import com.cgvsu.render_engine.texture.TextureManager;

import java.util.*;

public class Model {

    public ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    public ArrayList<Vector2f> textureVertices = new ArrayList<Vector2f>();
    public ArrayList<Vector3f> normals = new ArrayList<Vector3f>();
    public ArrayList<Polygon> polygons = new ArrayList<Polygon>();
    private String texturePath = null;
    private Material material;

    public Model(ArrayList<Vector3f> vertices, ArrayList<Vector2f> textureVertices,
                 ArrayList<Vector3f> normals, ArrayList<Polygon> polygons) {
        this.vertices = vertices;
        this.textureVertices = textureVertices;
        this.normals = normals;
        this.polygons = polygons;
        this.material = new Material();
    }

    public Model() {}

    public ArrayList<Vector3f> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Vector3f> vertices) {
        this.vertices = vertices;
    }

    public ArrayList<Vector2f> getTextureVertices() {
        return textureVertices;
    }

    public void setTextureVertices(ArrayList<Vector2f> textureVertices) {
        this.textureVertices = textureVertices;
    }

    public ArrayList<Vector3f> getNormals() {
        return normals;
    }

    public void setNormals(ArrayList<Vector3f> normals) {
        this.normals = normals;
    }

    public ArrayList<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(ArrayList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public String getTexturePath() {
        return texturePath;
    }

    public void loadTexture(String texturePath) {
        if (this.material == null) {
            this.material = new Material();
        }
        Texture texture = TextureManager.getInstance().loadTexture(texturePath);
        this.material.setDiffuseTexture(texture);
    }

    public void setTexture(Texture texture) {
        if (this.material == null) {
            this.material = new Material();
        }
        this.material.setDiffuseTexture(texture);
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}

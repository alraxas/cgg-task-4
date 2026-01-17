package com.cgvsu.render_engine.processing;

import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;
import com.cgvsu.math.Vector3f;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TriangulatorTest {

    private Triangulator triangulator;
    private Model model;

    @BeforeEach
    void setUp() {
        triangulator = new Triangulator();
        model = new Model();
    }

    @Test
    void testTriangulateModel_EmptyModel() {
        model.setVertices(new ArrayList<>());
        model.setTextureVertices(new ArrayList<>());
        model.setNormals(new ArrayList<>());
        model.setPolygons(new ArrayList<>());

        Model result = triangulator.triangulateModel(model);

        assertNotNull(result);
        assertTrue(result.getVertices().isEmpty());
        assertTrue(result.getPolygons().isEmpty());
    }

    @Test
    void testTriangulatePolygon_Triangle_Unchanged() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon triangle = new Polygon();
        triangle.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        // Act
        triangulator.triangulatePolygon(triangle, resultModel);

        // Assert
        assertEquals(1, resultModel.getPolygons().size());
        Polygon result = resultModel.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), result.getVertexIndices());
    }

    @Test
    void testTriangulatePolygon_Quad() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        // Act
        triangulator.triangulatePolygon(quad, resultModel);

        // Assert
        assertEquals(2, resultModel.getPolygons().size());

        // First triangle: 0-1-2
        Polygon firstTriangle = resultModel.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), firstTriangle.getVertexIndices());

        // Second triangle: 0-2-3
        Polygon secondTriangle = resultModel.getPolygons().get(1);
        assertEquals(Arrays.asList(0, 2, 3), secondTriangle.getVertexIndices());
    }

    @Test
    void testTriangulatePolygon_Pentagon() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon pentagon = new Polygon();
        pentagon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4)));

        // Act
        triangulator.triangulatePolygon(pentagon, resultModel);

        // Assert
        assertEquals(3, resultModel.getPolygons().size());

        List<Polygon> triangles = resultModel.getPolygons();
        assertEquals(Arrays.asList(0, 1, 2), triangles.get(0).getVertexIndices());
        assertEquals(Arrays.asList(0, 2, 3), triangles.get(1).getVertexIndices());
        assertEquals(Arrays.asList(0, 3, 4), triangles.get(2).getVertexIndices());
    }

    @Test
    void testTriangulatePolygon_WithTextureCoordinates() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon quadWithUV = new Polygon();
        quadWithUV.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        quadWithUV.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12, 13)));

        // Act
        triangulator.triangulatePolygon(quadWithUV, resultModel);

        // Assert
        assertEquals(2, resultModel.getPolygons().size());

        // Check first triangle has correct UV indices
        Polygon firstTriangle = resultModel.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), firstTriangle.getVertexIndices());
        assertEquals(Arrays.asList(10, 11, 12), firstTriangle.getTextureVertexIndices());

        // Check second triangle has correct UV indices
        Polygon secondTriangle = resultModel.getPolygons().get(1);
        assertEquals(Arrays.asList(0, 2, 3), secondTriangle.getVertexIndices());
        assertEquals(Arrays.asList(10, 12, 13), secondTriangle.getTextureVertexIndices());
    }

    @Test
    void testTriangulatePolygon_WithNormals() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon quadWithNormals = new Polygon();
        quadWithNormals.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        quadWithNormals.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22, 23)));

        // Act
        triangulator.triangulatePolygon(quadWithNormals, resultModel);

        // Assert
        assertEquals(2, resultModel.getPolygons().size());

        Polygon firstTriangle = resultModel.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), firstTriangle.getVertexIndices());
        assertEquals(Arrays.asList(20, 21, 22), firstTriangle.getNormalIndices());

        Polygon secondTriangle = resultModel.getPolygons().get(1);
        assertEquals(Arrays.asList(0, 2, 3), secondTriangle.getVertexIndices());
        assertEquals(Arrays.asList(20, 22, 23), secondTriangle.getNormalIndices());
    }

    @Test
    void testTriangulatePolygon_WithAllAttributes() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon polygon = new Polygon();
        polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));
        polygon.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12, 13)));
        polygon.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22, 23)));

        // Act
        triangulator.triangulatePolygon(polygon, resultModel);

        // Assert
        assertEquals(2, resultModel.getPolygons().size());

        Polygon triangle = resultModel.getPolygons().get(0);
        assertEquals(Arrays.asList(0, 1, 2), triangle.getVertexIndices());
        assertEquals(Arrays.asList(10, 11, 12), triangle.getTextureVertexIndices());
        assertEquals(Arrays.asList(20, 21, 22), triangle.getNormalIndices());
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 4, 5, 6, 10})
    void testTriangulatePolygon_VariousSizes(int vertexCount) {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon polygon = new Polygon();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < vertexCount; i++) {
            indices.add(i);
        }
        polygon.setVertexIndices((ArrayList<Integer>) indices);

        // Act
        triangulator.triangulatePolygon(polygon, resultModel);

        // Assert
        int expectedTriangleCount = vertexCount - 2;
        assertEquals(expectedTriangleCount, resultModel.getPolygons().size());
    }

    @Test
    void testTriangulateModel_MultiplePolygons() {
        // Arrange
        List<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(1, 1, 0));
        vertices.add(new Vector3f(0, 1, 0));

        model.setVertices((ArrayList<Vector3f>) vertices);

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        Polygon triangle = new Polygon();
        triangle.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        List<Polygon> polygons = new ArrayList<>();
        polygons.add(quad);
        polygons.add(triangle);
        model.setPolygons((ArrayList<Polygon>) polygons);

        // Act
        Model result = triangulator.triangulateModel(model);

        // Assert
        assertEquals(4, result.getVertices().size()); // Original vertices preserved
        assertEquals(3, result.getPolygons().size()); // Quad -> 2 triangles + 1 triangle
    }

    @Test
    void testProcessModel_FullPipeline() {
        // Arrange
        List<Vector3f> vertices = new ArrayList<>();
        vertices.add(new Vector3f(0, 0, 0));
        vertices.add(new Vector3f(1, 0, 0));
        vertices.add(new Vector3f(1, 1, 0));
        vertices.add(new Vector3f(0, 1, 0));

        model.setVertices((ArrayList<Vector3f>) vertices);

        Polygon quad = new Polygon();
        quad.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2, 3)));

        List<Polygon> polygons = new ArrayList<>();
        polygons.add(quad);
        model.setPolygons((ArrayList<Polygon>) polygons);

        // Act
        Model result = triangulator.processModel(model);

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getVertices().size());
        assertEquals(2, result.getPolygons().size()); // Quad should be triangulated

        // Check that both polygons are triangles
        for (Polygon polygon : result.getPolygons()) {
            assertEquals(3, polygon.getVertexIndices().size());
        }
    }

    @Test
    void testCopyPolygon() {
        // Arrange
        Polygon original = new Polygon();
        original.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));
        original.setTextureVertexIndices(new ArrayList<>(Arrays.asList(10, 11, 12)));
        original.setNormalIndices(new ArrayList<>(Arrays.asList(20, 21, 22)));

        // Act
        Triangulator triangulatorInstance = new Triangulator();
        Polygon copy = triangulatorInstance.copyPolygon(original);

        // Assert
        assertNotSame(original, copy);
        assertEquals(original.getVertexIndices(), copy.getVertexIndices());
        assertEquals(original.getTextureVertexIndices(), copy.getTextureVertexIndices());
        assertEquals(original.getNormalIndices(), copy.getNormalIndices());
    }

    @Test
    void testCopyPolygon_PartialAttributes() {
        // Test with only vertices
        Polygon verticesOnly = new Polygon();
        verticesOnly.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

        Triangulator triangulatorInstance = new Triangulator();
        Polygon copy = triangulatorInstance.copyPolygon(verticesOnly);

        assertEquals(Arrays.asList(0, 1, 2), copy.getVertexIndices());
        assertTrue(copy.getTextureVertexIndices().isEmpty());
        assertTrue(copy.getNormalIndices().isEmpty());
    }

    @Test
    void testTriangulatePolygon_EmptyLists() {
        // Arrange
        Model resultModel = new Model();
        resultModel.setPolygons(new ArrayList<>());

        Polygon polygon = new Polygon();
        // Не устанавливаем списки - они должны быть пустыми по умолчанию

        // Act
        triangulator.triangulatePolygon(polygon, resultModel);

        // Assert
        assertEquals(0, resultModel.getPolygons().size()); // Пустой полигон не должен обрабатываться
    }

    @Test
    void testTriangulatePolygon_NullSafety() {
        // Arrange
        Model resultModel = null;

        // Act & Assert
        assertDoesNotThrow(() -> {
            Model model = new Model();
            model.setPolygons(new ArrayList<>());

            Polygon polygon = new Polygon();
            polygon.setVertexIndices(new ArrayList<>(Arrays.asList(0, 1, 2)));

            triangulator.triangulatePolygon(polygon, model);
        });
    }
}
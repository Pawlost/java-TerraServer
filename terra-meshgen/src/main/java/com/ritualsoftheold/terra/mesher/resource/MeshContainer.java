package com.ritualsoftheold.terra.mesher.resource;

import com.jme3.math.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Helps with building meshes for voxel data.
 *
 */
public class MeshContainer {

    private ArrayList<Vector3f> vector3fs;

    private ArrayList<Integer> indices;

    private ArrayList<Vector3f> texCoords;

    private ArrayList<Vector3f> normals;

    /**
     * Creates a new mesh container.
     */
    public MeshContainer() {
        vector3fs = new ArrayList<>();
        indices = new ArrayList<>();
        texCoords = new ArrayList<>();
        normals = new ArrayList<>();
    }

    public void normals(Vector3f[] normal){normals.addAll(Arrays.asList(normal));}

    public void vertex(Vector3f[] vectors) {
        vector3fs.addAll(Arrays.asList(vectors));
    }

    public void triangle(int[] indexes) {
        for(int index : indexes){
            indices.add(index);
        }
    }

    public void texture(Vector3f[] textureCoords) {
        texCoords.addAll(Arrays.asList(textureCoords));
    }

    public ArrayList<Vector3f> getVertice() {
        return vector3fs;
    }

    public ArrayList<Integer> getIndices() {
        return indices;
    }

    public ArrayList<Vector3f> getTextureCoordinates() {
        return texCoords;
    }

    public ArrayList<Vector3f> getNormals(){return normals;}

    public void clear(){
        texCoords.clear();
        indices.clear();
        vector3fs.clear();
        normals.clear();
    }
}
package com.ritualsoftheold.terra.mesher;

import com.jme3.math.Vector3f;
import com.ritualsoftheold.terra.core.material.TerraMaterial;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class Face implements Comparable<Face> {
    private ArrayList<Vector3f> textureCoords;
    private Vector3f[] vector3f;
    private Vector3f[] normals;
    private TerraMaterial material;

    public Face() {
        textureCoords = new ArrayList<>();
        vector3f = new Vector3f[4];
        normals = new Vector3f[4];
    }

    public void setNormals(Vector3f... normals) {
        this.normals = normals;
    }

    public void setNormals(Vector3f normal, int position) {
        this.normals[position]=normal;
    }
    public void setNormals(int x, int y, int z, int position){
        this.normals[position] = new Vector3f(x,y,z);
    }

    public void setVector3f(int x, int y, int z, int position) {
        this.vector3f[position] = new Vector3f(x/4f, y/4f, z/4f);
    }

    public void setVector3f(Vector3f vector3f, int position) {
        this.vector3f[position] = vector3f;
    }

    public void setTextureCoords(float x, float y) {
        //TODO better material positioning (maybe it is alright like this)
        this.textureCoords.add(new Vector3f(x, y, material.getWorldId() - 2));
    }

    public Vector3f[] getTextureCoords() {
        Vector3f[] clone = new Vector3f[textureCoords.size()];
        textureCoords.toArray(clone);
        return clone;
    }

    public Vector3f[] getVector3fs() {
        return vector3f;
    }

    public void setMaterial(TerraMaterial material) {
        this.material = material;
    }

    public TerraMaterial getMaterial() {
        return material;
    }

    public Vector3f[] getNormals() {
        return normals;
    }

    @Override
    public int compareTo(@NotNull Face o) {
        if(o.vector3f[0].x < vector3f[0].x){
            return 1;
        }else if (o.vector3f[0].x > vector3f[0].x){
            return -1;
        }
        return 0;
    }
}
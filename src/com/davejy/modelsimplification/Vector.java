package com.davejy.modelsimplification;

/**
 * Created by Jiao Yue on 2017/4/20.
 */

class Vector {

    private static final String TAG = "Vector";

    public float x;
    public float y;
    public float z;

    public Vector(){}

    public Vector(float[] a) {
        x = a[0];
        y = a[1];
        z = a[2];
    }

    public Vector(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float magnitude() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector normalize() {
        float d = magnitude();
        if (d == 0) {
            System.err.println("normalize: try to normalize a zero vector");
            return this;
        }
        x /= d;
        y /= d;
        z /= d;
        return this;
    }

    public float dotProduct(final Vector v) {
        return x * v.x + y * v.y + z * v.z;
    }

    public Vector crossProduct(final Vector v) {
        return new Vector(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }

    public float[] toFloatArray() {
        return new float[]{x, y, z, 1};
    }

    public static Vector add(final Vector v1, final Vector v2) {
        return new Vector(v1.x + v2.x, v1.y + v2.y, v1.z + v2.z);
    }

    public static Vector minus(final Vector v1, final Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "," + z + ")";
    }
}

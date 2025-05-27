package net.t106.sinkerglwallpaper;

import android.opengl.Matrix;

/**
 * Matrix utility class for OpenGL ES 3.2 migration
 * Replaces deprecated GLU functions with modern matrix calculations
 */
public class MatrixUtils {
    
    /**
     * Creates a perspective projection matrix (replaces GLU.gluPerspective)
     * @param fovY Field of view angle in degrees
     * @param aspect Aspect ratio (width/height)
     * @param zNear Near clipping plane distance
     * @param zFar Far clipping plane distance
     * @return 4x4 perspective projection matrix
     */
    public static float[] perspective(float fovY, float aspect, float zNear, float zFar) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.perspectiveM(matrix, 0, fovY, aspect, zNear, zFar);
        return matrix;
    }
    
    /**
     * Creates a look-at view matrix (replaces GLU.gluLookAt)
     * @param eyeX Camera position X
     * @param eyeY Camera position Y
     * @param eyeZ Camera position Z
     * @param centerX Look-at target X
     * @param centerY Look-at target Y
     * @param centerZ Look-at target Z
     * @param upX Up vector X
     * @param upY Up vector Y
     * @param upZ Up vector Z
     * @return 4x4 view matrix
     */
    public static float[] lookAt(float eyeX, float eyeY, float eyeZ,
                                float centerX, float centerY, float centerZ,
                                float upX, float upY, float upZ) {
        float[] matrix = new float[16];
        Matrix.setLookAtM(matrix, 0, 
                         eyeX, eyeY, eyeZ,
                         centerX, centerY, centerZ,
                         upX, upY, upZ);
        return matrix;
    }
    
    /**
     * Creates a rotation matrix around Z axis
     * @param angle Rotation angle in degrees
     * @return 4x4 rotation matrix
     */
    public static float[] rotateZ(float angle) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.rotateM(matrix, 0, angle, 0, 0, 1);
        return matrix;
    }
    
    /**
     * Multiplies two 4x4 matrices (result = left * right)
     * @param left Left matrix
     * @param right Right matrix
     * @return Resulting matrix
     */
    public static float[] multiply(float[] left, float[] right) {
        float[] result = new float[16];
        Matrix.multiplyMM(result, 0, left, 0, right, 0);
        return result;
    }
    
    /**
     * Creates an identity matrix
     * @return 4x4 identity matrix
     */
    public static float[] identity() {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        return matrix;
    }
    
    /**
     * Creates a translation matrix
     * @param x Translation X
     * @param y Translation Y
     * @param z Translation Z
     * @return 4x4 translation matrix
     */
    public static float[] translate(float x, float y, float z) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.translateM(matrix, 0, x, y, z);
        return matrix;
    }
    
    /**
     * Creates a scale matrix
     * @param x Scale X
     * @param y Scale Y
     * @param z Scale Z
     * @return 4x4 scale matrix
     */
    public static float[] scale(float x, float y, float z) {
        float[] matrix = new float[16];
        Matrix.setIdentityM(matrix, 0);
        Matrix.scaleM(matrix, 0, x, y, z);
        return matrix;
    }
}
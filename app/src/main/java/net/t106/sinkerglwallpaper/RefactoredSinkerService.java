package net.t106.sinkerglwallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.GLWallpaperServiceES32;

/**
 * Refactored version of SinkerService using the new class architecture
 * Demonstrates cleaner, more maintainable code structure
 */
public class RefactoredSinkerService extends GLWallpaperServiceES32 {
    public static int[] textures = new int[2];
    public static int blend_type;
    public static int[] col = new int[4];
    private static Context context = null;
    
    // Static method to provide context to other classes
    public static Context getContext() {
        return context;
    }
    
    public class RefactoredSinkerEngine extends GLWallpaperServiceES32.GLEngine {
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            setRenderer(new RefactoredRenderer());
        }
    }
    
    public class RefactoredRenderer implements GLWallpaperServiceES32.Renderer {
        private RotatingGraveyard centerGraveyard;
        private RotatingGraveyard backgroundGraveyard;
        private StaticFilter rightFilter;
        private ConfigurableFilter leftFilter;
        
        // OpenGL ES 3.2 matrices
        private float[] projectionMatrix;
        private float[] viewMatrix;
        private long lastTime;
        
        public RefactoredRenderer() {
            // Create objects using factory methods - much cleaner!
            centerGraveyard = RotatingGraveyard.createCenter();
            backgroundGraveyard = RotatingGraveyard.createBackground();
            rightFilter = new StaticFilter();
            leftFilter = new ConfigurableFilter();
            
            projectionMatrix = new float[16];
            viewMatrix = new float[16];
            lastTime = System.currentTimeMillis();
        }
        
        @Override
        public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
            // Clear screen
            android.opengl.GLES32.glClear(android.opengl.GLES32.GL_COLOR_BUFFER_BIT);
            
            // Calculate delta time for animations
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastTime) / 1000.0f;
            lastTime = currentTime;
            
            // Update objects - clean and simple!
            backgroundGraveyard.Update(deltaTime);
            centerGraveyard.Update(deltaTime);
            leftFilter.Update(deltaTime);
            rightFilter.Update(deltaTime);
            
            // Draw objects - same order as original
            backgroundGraveyard.Draw(viewMatrix, projectionMatrix);
            centerGraveyard.Draw(viewMatrix, projectionMatrix);
            leftFilter.Draw(viewMatrix, projectionMatrix);
            rightFilter.Draw(viewMatrix, projectionMatrix);
        }
        
        @Override
        public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int wid, int hei) {
            android.opengl.GLES32.glViewport(0, 0, wid, hei);
            
            // Create projection matrix using modern approach
            projectionMatrix = MatrixUtils.perspective(45f, (float)wid/(float)hei, 0.1f, 100f);
            
            if (context != null) {
                loadUserSettings();
            }
        }
        
        /**
         * Loads user settings and applies them to filters
         * Much cleaner than the original scattered approach!
         */
        private void loadUserSettings() {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            
            // Load blend type
            String type = sp.getString("blend_type", "mul");
            if (type.equals("add")) blend_type = 0;
            else if (type.equals("mul")) blend_type = 1;
            else if (type.equals("alpha")) blend_type = 2;
            else if (type.equals("xor")) blend_type = 3;
            
            // Load color settings
            col[0] = sp.getInt("col_R", 50);
            col[1] = sp.getInt("col_G", 50);
            col[2] = sp.getInt("col_B", 100);
            col[3] = sp.getInt("col_Alpha", 50);
            
            // Load filter size
            String Isvert = sp.getString("filter_size", "smoll");
            boolean isSmall = Isvert.equals("smoll");
            leftFilter.sizechange(isSmall);
            rightFilter.sizechange(isSmall);
            
            // Load graveyard size and set up view matrix
            int gr_size = sp.getInt("size", 200);
            float cameraZ = (gr_size + 100) / 100.0f;
            viewMatrix = MatrixUtils.lookAt(0, 0, cameraZ, 0, 0, 0, 0, 1, 0);
        }
        
        @Override
        public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, 
                                    javax.microedition.khronos.egl.EGLConfig arg1) {
            // Delete old textures if they exist
            if (textures[0] != 0 || textures[1] != 0) {
                TextureUtils.deleteTextures(textures);
            }
            
            // Load textures using new utility
            int[] newTextures = TextureUtils.loadTextureWithFlipped(context, R.drawable.gr);
            if (newTextures != null) {
                textures[0] = newTextures[0]; // Original texture
                textures[1] = newTextures[1]; // Flipped texture
            }
            
            // Set background color
            android.opengl.GLES32.glClearColor(0, 0, 0, 0);
            
            // Initialize all rendering objects - clean and organized!
            backgroundGraveyard.initGL();
            centerGraveyard.initGL();
            leftFilter.initGL();
            rightFilter.initGL();
        }
    }
    
    @Override
    public Engine onCreateEngine() {
        context = this;
        return new RefactoredSinkerEngine();
    }
    
    // Legacy compatibility method
    public static java.nio.FloatBuffer makeFloatBuffer(float[] values) {
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.allocateDirect(values.length * 4);
        bb.order(java.nio.ByteOrder.nativeOrder());
        java.nio.FloatBuffer fb = bb.asFloatBuffer();
        fb.put(values);
        fb.position(0);
        return fb;
    }
}
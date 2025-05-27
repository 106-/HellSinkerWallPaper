package net.t106.sinkerglwallpaper.opengl.shaders;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import net.t106.sinkerglwallpaper.opengl.utils.ShaderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Shader loader utility for loading shader source code from assets
 */
public class ShaderLoader {
    private static final String TAG = "ShaderLoader";
    
    /**
     * Loads shader source code from assets
     * @param context Application context
     * @param filename Shader filename in assets/shaders/
     * @return Shader source code, or null if loading failed
     */
    public static String loadShaderFromAssets(Context context, String filename) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open("shaders/" + filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            
            StringBuilder source = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                source.append(line).append("\n");
            }
            
            reader.close();
            return source.toString();
            
        } catch (IOException e) {
            Log.e(TAG, "Failed to load shader: " + filename, e);
            return null;
        }
    }
    
    /**
     * Creates a shader program from asset files
     * @param context Application context
     * @param vertexShaderFile Vertex shader filename
     * @param fragmentShaderFile Fragment shader filename
     * @return Shader program handle, or 0 if creation failed
     */
    public static int createProgramFromAssets(Context context, String vertexShaderFile, String fragmentShaderFile) {
        String vertexSource = loadShaderFromAssets(context, vertexShaderFile);
        String fragmentSource = loadShaderFromAssets(context, fragmentShaderFile);
        
        if (vertexSource == null || fragmentSource == null) {
            Log.e(TAG, "Failed to load shader sources");
            return 0;
        }
        
        return ShaderUtils.createProgram(vertexSource, fragmentSource);
    }
    
    /**
     * Predefined shader programs for the application
     */
    public static class Programs {
        public static int createBasicProgram(Context context) {
            return createProgramFromAssets(context, "basic_vertex.glsl", "basic_fragment.glsl");
        }
        
        public static int createBlendProgram(Context context) {
            return createProgramFromAssets(context, "basic_vertex.glsl", "blend_fragment.glsl");
        }
    }
}
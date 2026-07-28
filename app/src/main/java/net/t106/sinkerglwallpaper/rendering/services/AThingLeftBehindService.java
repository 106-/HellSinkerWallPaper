package net.t106.sinkerglwallpaper.rendering.services;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES32;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import net.rbgrn.android.glwallpaperservice.GLWallpaperServiceES32;
import net.t106.sinkerglwallpaper.R;
import net.t106.sinkerglwallpaper.rendering.objects.CenterGarland;
import net.t106.sinkerglwallpaper.rendering.objects.BackgroundGarland;
import net.t106.sinkerglwallpaper.rendering.filters.LeftFilter;
import net.t106.sinkerglwallpaper.rendering.filters.OuterBrightnessFilter;
import net.t106.sinkerglwallpaper.rendering.filters.RightFilter;
import net.t106.sinkerglwallpaper.opengl.utils.MatrixUtils;
import net.t106.sinkerglwallpaper.opengl.utils.TextureUtils;

public class AThingLeftBehindService extends GLWallpaperServiceES32{
	public static int[] textures = new int[1];
	private static Context context = null;
	
	// Static method to provide context to other classes
	public static Context getContext() {
		return context;
	}
	
	public class AThingLeftBehindEngine extends GLWallpaperServiceES32.GLEngine{
		
		@Override
	    public void onCreate(SurfaceHolder surfaceHolder) {
	        super.onCreate(surfaceHolder);
	        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
	        setRenderer(new MyRenderer());
		}       
		
	}
	
	public class MyRenderer implements GLWallpaperServiceES32.Renderer {
		private CenterGarland cgy;
		private BackgroundGarland bgy;
		private RightFilter rf;
		private LeftFilter lf;
		private OuterBrightnessFilter outerBrightnessFilter;
		
		// OpenGL ES 3.2 matrices
		private float[] projectionMatrix;
		private float[] viewMatrix;
		private long lastTimeNanos;
		
		public MyRenderer()
		{   
			cgy = new CenterGarland();
			bgy = new BackgroundGarland();
			rf = new RightFilter();
			lf = new LeftFilter();
			outerBrightnessFilter = new OuterBrightnessFilter();
			
			projectionMatrix = MatrixUtils.identity();
			viewMatrix = MatrixUtils.identity();
			lastTimeNanos = 0L;
		}
		
		@Override
		public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {			
			// Reset OpenGL state to known values
			resetOpenGLState();
			
			// Clear screen
			GLES32.glClear(GLES32.GL_COLOR_BUFFER_BIT);
			
			// Calculate delta time for animations
			long currentTimeNanos = SystemClock.elapsedRealtimeNanos();
			float deltaTime = lastTimeNanos == 0L ? 0.0f
				: (currentTimeNanos - lastTimeNanos) / 1_000_000_000.0f;
			lastTimeNanos = currentTimeNanos;
			
			// Update objects
			bgy.Update(deltaTime);
			cgy.Update(deltaTime);
			lf.Update(deltaTime);
			rf.Update(deltaTime);
			outerBrightnessFilter.Update(deltaTime);
			
			// Original sort order: opaque center plate, dark layer, cyan layer,
			// right-half inversion, then gray over the full center band.
			lf.DrawBase(viewMatrix, projectionMatrix);
			bgy.Draw(viewMatrix, projectionMatrix);
			cgy.Draw(viewMatrix, projectionMatrix);
			rf.Draw(viewMatrix, projectionMatrix);
			lf.Draw(viewMatrix, projectionMatrix);
			// The menu/HUD queue brightens only the two areas outside the
			// existing center band after all background layers are composited.
			outerBrightnessFilter.Draw(viewMatrix, projectionMatrix);
		}
		
		private void resetOpenGLState() {
			// Reset only binding states, not capabilities that objects need to control
			GLES32.glUseProgram(0);
			GLES32.glBindTexture(GLES32.GL_TEXTURE_2D, 0);
			GLES32.glBindBuffer(GLES32.GL_ARRAY_BUFFER, 0);
			GLES32.glBindBuffer(GLES32.GL_ELEMENT_ARRAY_BUFFER, 0);
			
			// Don't reset blend, depth test, or cull face as objects may need them
			// Reset vertex attribute arrays
			for (int i = 0; i < 8; i++) {
				GLES32.glDisableVertexAttribArray(i);
			}
		}
		
		@Override
		public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int wid, int hei) {
			GLES32.glViewport(0, 0, wid, hei);
			
			// Preserve the original 480-pixel logical height. The visible width
			// follows the device aspect ratio, so the 512-square sprite is never
			// stretched on portrait phones.
			float halfHeight = 240.0f;
			float halfWidth = halfHeight * (float)wid / (float)hei;
			projectionMatrix = MatrixUtils.orthographic(
				-halfWidth, halfWidth, -halfHeight, halfHeight);
			viewMatrix = MatrixUtils.identity();
			lf.setViewport(halfWidth, halfHeight);
			rf.setViewport(halfWidth, halfHeight);
			outerBrightnessFilter.setViewport(halfWidth, halfHeight);
		}
		
		@Override
		public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig arg1) {
			// Delete old textures if they exist
			if (textures[0] != 0) {
				TextureUtils.deleteTexture(textures[0]);
				textures[0] = 0;
			}
			
			textures[0] = TextureUtils.loadTexture(context, R.drawable.gr);
			if (textures[0] == 0) {
				android.util.Log.e("AThingLeftBehindService", "Failed to load textures!");
			} else {
				android.util.Log.d("AThingLeftBehindService", "Texture loaded: " + textures[0]);
			}
			
			// The original render target is black outside the center band.
			GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GLES32.glDisable(GLES32.GL_DEPTH_TEST);
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			lastTimeNanos = 0L;
			
			// Initialize all rendering objects
			bgy.initGL();
			cgy.initGL();
			lf.initGL();
			rf.initGL();
			outerBrightnessFilter.initGL();
		}
	}
	
	@Override
	public Engine onCreateEngine()
	{
		context = this;
		return new AThingLeftBehindEngine();
	}
	
	//頂点の配列をバッファーに変換するメソッド
	 public static FloatBuffer makeFloatBuffer(float[] values) {
	  ByteBuffer bb = ByteBuffer.allocateDirect(values.length * 4);
	  bb.order(ByteOrder.nativeOrder());
	  FloatBuffer fb = bb.asFloatBuffer();
	  fb.put(values);
	  fb.position(0);
	  return fb;
	 }
}

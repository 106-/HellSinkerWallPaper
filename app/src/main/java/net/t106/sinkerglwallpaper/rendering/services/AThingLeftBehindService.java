package net.t106.sinkerglwallpaper.rendering.services;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES32;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import androidx.preference.PreferenceManager;
import net.rbgrn.android.glwallpaperservice.GLWallpaperServiceES32;
import net.t106.sinkerglwallpaper.rendering.backgrounds.MenuBackgroundProfile;
import net.t106.sinkerglwallpaper.rendering.backgrounds.MenuSpriteRenderer;
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
		private final MenuSpriteRenderer spriteRenderer;
		private final MenuSpriteRenderer.SpriteState[] spriteStates;
		private final int[][] menuTextures;
		private final SharedPreferences preferences;
		private RightFilter rf;
		private LeftFilter lf;
		private OuterBrightnessFilter outerBrightnessFilter;
		
		// OpenGL ES 3.2 matrices
		private float[] projectionMatrix;
		private float[] viewMatrix;
		private long lastTimeNanos;
		private long animationTick;
		private float tickRemainder;

		public MyRenderer()
		{
			spriteRenderer = new MenuSpriteRenderer();
			spriteStates = new MenuSpriteRenderer.SpriteState[] {
				new MenuSpriteRenderer.SpriteState(),
				new MenuSpriteRenderer.SpriteState(),
				new MenuSpriteRenderer.SpriteState(),
			};
			menuTextures = new int[MenuBackgroundProfile.values().length][2];
			preferences = PreferenceManager.getDefaultSharedPreferences(context);
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

			tickRemainder += Math.max(0.0f, deltaTime) * 60.0f;
			long elapsedTicks = (long)tickRemainder;
			tickRemainder -= elapsedTicks;
			animationTick += elapsedTicks;

			MenuBackgroundProfile profile = MenuBackgroundProfile.fromPreference(
				preferences.getString(
					MenuBackgroundProfile.PREFERENCE_KEY,
					MenuBackgroundProfile.DEFAULT_VALUE));
			profile.configureSprites(animationTick, spriteStates);

			// The sort-key order recovered from the game is: opaque base,
			// sprite layers, optional right-half inversion, optional gray pass.
			lf.DrawBase(viewMatrix, projectionMatrix, profile.baseColor);
			int[] profileTextures = menuTextures[profile.ordinal()];
			for (int i = 0; i < profile.spriteCount; i++) {
				spriteRenderer.draw(
					viewMatrix,
					projectionMatrix,
					profileTextures[0],
					profileTextures[1],
					spriteStates[i]);
			}
			if (profile.invertRightHalf) {
				rf.Draw(viewMatrix, projectionMatrix);
			}
			if (profile.overlayColor != 0) {
				lf.DrawOverlay(
					viewMatrix, projectionMatrix, profile.overlayColor);
			}
			// The menu/HUD queue brightens only the two areas outside the
			// existing center band, equally for all four background choices.
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
			for (int[] pair : menuTextures) {
				for (int i = 0; i < pair.length; i++) {
					if (pair[i] != 0) {
						TextureUtils.deleteTexture(pair[i]);
						pair[i] = 0;
					}
				}
			}

			for (MenuBackgroundProfile profile : MenuBackgroundProfile.values()) {
				int[] pair = menuTextures[profile.ordinal()];
				pair[0] = TextureUtils.loadTexture(context, profile.rgbResource);
				pair[1] = TextureUtils.loadTexture(context, profile.alphaResource);
				if (pair[0] == 0 || pair[1] == 0) {
					android.util.Log.e(
						"AThingLeftBehindService",
						"Failed to load textures for " + profile.name());
				}
			}

			// The original render target is black outside the center band.
			GLES32.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			GLES32.glDisable(GLES32.GL_DEPTH_TEST);
			GLES32.glDisable(GLES32.GL_CULL_FACE);
			lastTimeNanos = 0L;
			animationTick = 0L;
			tickRemainder = 0.0f;

			// Initialize all rendering objects
			spriteRenderer.initGL(context);
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

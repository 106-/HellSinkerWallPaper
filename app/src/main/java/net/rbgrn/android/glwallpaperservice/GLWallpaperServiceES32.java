/*
 * GLWallpaperService (OpenGL ES 3.2 version)
 *
 * Based on the original implementation by Robert Green.
 * Adapted to request an OpenGL ES 3.x core context while preserving the
 * original public class / method structure as much as possible.
 *
 * Key Modification Summary (see comments marked "MOD ES3.2" below):
 *   • DefaultContextFactory now passes an attrib‑list that requests
 *     EGL_CONTEXT_CLIENT_VERSION = 3 so that EGL creates a 3.x context.
 *   • Added a constant for the attribute key (0x3098) to avoid importing
 *     android.opengl.EGL14 in this legacy EGL10 code path.
 *   • Added import for android.opengl.GLES32 so renderers can call GLES32.*
 *     even when they are invoked from the same engine.
 *
 * No other functional changes have been introduced; the threading model
 * and the Engine / GLThread / Renderer contracts remain identical.
 */

package net.rbgrn.android.glwallpaperservice;

import android.opengl.GLES32;       // MOD ES3.2 : renderer side utilities
import android.opengl.GLSurfaceView;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.Writer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * GLWallpaperService adapted for OpenGL ES 3.2 core profile.
 */
public class GLWallpaperServiceES32 extends WallpaperService {
    private static final String TAG = "GLWallpaperService";

    @Override
    public Engine onCreateEngine() {
        return new GLEngine();
    }

    /* --------------------------------------------------------------------- */
    public class GLEngine extends Engine {
        public static final int RENDERMODE_WHEN_DIRTY      = 0;
        public static final int RENDERMODE_CONTINUOUSLY    = 1;

        private GLThread                        mGLThread;
        private GLSurfaceView.EGLConfigChooser  mEGLConfigChooser;
        private GLSurfaceView.EGLContextFactory mEGLContextFactory;
        private GLSurfaceView.EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
        private GLSurfaceView.GLWrapper         mGLWrapper;
        private int                             mDebugFlags;

        public GLEngine() {
            super();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                onResume();
            } else {
                onPause();
            }
            super.onVisibilityChanged(visible);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mGLThread.requestExitAndWait();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mGLThread.onWindowResize(width, height);
            super.onSurfaceChanged(holder, format, width, height);
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceCreated()");
            mGLThread.surfaceCreated(holder);
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "onSurfaceDestroyed()");
            mGLThread.surfaceDestroyed();
            super.onSurfaceDestroyed(holder);
        }

        /* --------------------------------------------------------------
         * Public configuration hooks – identical to original version
         * -------------------------------------------------------------- */
        public void setGLWrapper(GLSurfaceView.GLWrapper glWrapper) {
            mGLWrapper = glWrapper;
        }

        public void setDebugFlags(int debugFlags) {
            mDebugFlags = debugFlags;
        }

        public int getDebugFlags() {
            return mDebugFlags;
        }

        public void setRenderer(GLSurfaceView.Renderer renderer) {
            checkRenderThreadState();
            if (mEGLConfigChooser == null) {
                mEGLConfigChooser = new BaseConfigChooser.SimpleEGLConfigChooser(true);
            }
            if (mEGLContextFactory == null) {
                mEGLContextFactory = new DefaultContextFactory();
            }
            if (mEGLWindowSurfaceFactory == null) {
                mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
            }
            mGLThread = new GLThread(renderer,
                    mEGLConfigChooser,
                    mEGLContextFactory,
                    mEGLWindowSurfaceFactory,
                    mGLWrapper);
            mGLThread.start();
        }

        public void setEGLContextFactory(GLSurfaceView.EGLContextFactory factory) {
            checkRenderThreadState();
            mEGLContextFactory = factory;
        }

        public void setEGLWindowSurfaceFactory(GLSurfaceView.EGLWindowSurfaceFactory factory) {
            checkRenderThreadState();
            mEGLWindowSurfaceFactory = factory;
        }

        public void setEGLConfigChooser(GLSurfaceView.EGLConfigChooser configChooser) {
            checkRenderThreadState();
            mEGLConfigChooser = configChooser;
        }

        public void setEGLConfigChooser(boolean needDepth) {
            setEGLConfigChooser(new BaseConfigChooser.SimpleEGLConfigChooser(needDepth));
        }

        public void setEGLConfigChooser(int redSize, int greenSize, int blueSize,
                                        int alphaSize, int depthSize, int stencilSize) {
            setEGLConfigChooser(new BaseConfigChooser.ComponentSizeChooser(redSize, greenSize,
                    blueSize, alphaSize, depthSize, stencilSize));
        }

        public void setRenderMode(int renderMode) {
            mGLThread.setRenderMode(renderMode);
        }

        public int getRenderMode() {
            return mGLThread.getRenderMode();
        }

        public void requestRender() {
            mGLThread.requestRender();
        }

        public void onPause() {
            mGLThread.onPause();
        }

        public void onResume() {
            mGLThread.onResume();
        }

        public void queueEvent(Runnable r) {
            mGLThread.queueEvent(r);
        }

        private void checkRenderThreadState() {
            if (mGLThread != null) {
                throw new IllegalStateException("setRenderer has already been called for this instance.");
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* Deprecated interface stubs kept for source compatibility           */

    @Deprecated
    public interface Renderer extends GLSurfaceView.Renderer {
    }

    /* ------------------------------------------------------------------ */
    /* Log helper                                                          */

    static class LogWriter extends Writer {
        private final StringBuilder mBuilder = new StringBuilder();
        @Override public void close() { flushBuilder(); }
        @Override public void flush() { flushBuilder(); }
        @Override public void write(char[] buf, int offset, int count) {
            for (int i = 0; i < count; i++) {
                char c = buf[offset + i];
                if (c == '\n') {
                    flushBuilder();
                } else {
                    mBuilder.append(c);
                }
            }
        }
        private void flushBuilder() {
            if (mBuilder.length() > 0) {
                Log.v("GLSurfaceView", mBuilder.toString());
                mBuilder.setLength(0);
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* EGL helper factories – modified for ES 3.2                          */

    @Deprecated
    interface EGLContextFactory extends GLSurfaceView.EGLContextFactory {
    }

    /**
     * Default context factory now requests an ES 3.x context.
     */
    static class DefaultContextFactory implements GLSurfaceView.EGLContextFactory {
        // MOD ES3.2 : attribute key (EGL_CONTEXT_CLIENT_VERSION)
        private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            // MOD ES3.2 : Request an OpenGL ES 3.* context
            int[] attrib_list = {
                    EGL_CONTEXT_CLIENT_VERSION, 3,   // ES 3.x
                    EGL10.EGL_NONE
            };
            return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attrib_list);
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }

    @Deprecated
    interface EGLWindowSurfaceFactory extends GLSurfaceView.EGLWindowSurfaceFactory {
    }

    static class DefaultWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {
        @Override
        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                              EGLConfig config, Object nativeWindow) {
            EGLSurface eglSurface = null;
            while (eglSurface == null) {
                try {
                    eglSurface = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
                } catch (Throwable t) {
                    // retry until surface is ready
                } finally {
                    if (eglSurface == null) {
                        try { Thread.sleep(10); } catch (InterruptedException ignore) { }
                    }
                }
            }
            return eglSurface;
        }

        @Override
        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    @Deprecated
    interface GLWrapper extends GLSurfaceView.GLWrapper {
    }

    /* ------------------------------------------------------------------ */
    /* EGL helper class                                                    */

    static class EglHelper {
        private EGL10 mEgl;
        private EGLDisplay mEglDisplay;
        private EGLSurface mEglSurface;
        private EGLContext mEglContext;
        EGLConfig mEglConfig;

        private GLSurfaceView.EGLConfigChooser mEGLConfigChooser;
        private GLSurfaceView.EGLContextFactory mEGLContextFactory;
        private GLSurfaceView.EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
        private GLSurfaceView.GLWrapper mGLWrapper;

        public EglHelper(GLSurfaceView.EGLConfigChooser chooser, GLSurfaceView.EGLContextFactory contextFactory,
                         GLSurfaceView.EGLWindowSurfaceFactory surfaceFactory, GLSurfaceView.GLWrapper wrapper) {
            this.mEGLConfigChooser = chooser;
            this.mEGLContextFactory = contextFactory;
            this.mEGLWindowSurfaceFactory = surfaceFactory;
            this.mGLWrapper = wrapper;
        }

        public void start() {
            if (mEgl == null) {
                mEgl = (EGL10) EGLContext.getEGL();
            }

            if (mEglDisplay == null) {
                mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            }

            if (mEglConfig == null) {
                int[] version = new int[2];
                mEgl.eglInitialize(mEglDisplay, version);
                mEglConfig = mEGLConfigChooser.chooseConfig(mEgl, mEglDisplay);
            }

            if (mEglContext == null) {
                mEglContext = mEGLContextFactory.createContext(mEgl, mEglDisplay, mEglConfig);
                if (mEglContext == null || mEglContext == EGL10.EGL_NO_CONTEXT) {
                    throw new RuntimeException("createContext failed");
                }
            }

            mEglSurface = null;
        }

        public GL createSurface(SurfaceHolder holder) {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
            }

            mEglSurface = mEGLWindowSurfaceFactory.createWindowSurface(mEgl, mEglDisplay, mEglConfig, holder);

            if (mEglSurface == null || mEglSurface == EGL10.EGL_NO_SURFACE) {
                throw new RuntimeException("createWindowSurface failed");
            }

            if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {
                throw new RuntimeException("eglMakeCurrent failed.");
            }

            GL gl = mEglContext.getGL();
            if (mGLWrapper != null) {
                gl = mGLWrapper.wrap(gl);
            }

            return gl;
        }

        public boolean swap() {
            mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
            return mEgl.eglGetError() != EGL11.EGL_CONTEXT_LOST;
        }

        public void destroySurface() {
            if (mEglSurface != null && mEglSurface != EGL10.EGL_NO_SURFACE) {
                mEgl.eglMakeCurrent(mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                mEGLWindowSurfaceFactory.destroySurface(mEgl, mEglDisplay, mEglSurface);
                mEglSurface = null;
            }
        }

        public void finish() {
            if (mEglContext != null) {
                mEGLContextFactory.destroyContext(mEgl, mEglDisplay, mEglContext);
                mEglContext = null;
            }
            if (mEglDisplay != null) {
                mEgl.eglTerminate(mEglDisplay);
                mEglDisplay = null;
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* GL Thread implementation                                            */

    static class GLThread extends Thread {
        private final static boolean LOG_THREADS = false;
        public final static int DEBUG_CHECK_GL_ERROR = 1;
        public final static int DEBUG_LOG_GL_CALLS = 2;

        private final GLThreadManager sGLThreadManager = new GLThreadManager();
        private GLThread mEglOwner;

        private GLSurfaceView.EGLConfigChooser mEGLConfigChooser;
        private GLSurfaceView.EGLContextFactory mEGLContextFactory;
        private GLSurfaceView.EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
        private GLSurfaceView.GLWrapper mGLWrapper;

        public SurfaceHolder mHolder;
        private boolean mSizeChanged = true;

        public boolean mDone;
        private boolean mPaused;
        private boolean mHasSurface;
        private boolean mWaitingForSurface;
        private boolean mHaveEgl;
        private int mWidth;
        private int mHeight;
        private int mRenderMode;
        private boolean mRequestRender;
        private boolean mEventsWaiting;

        private GLSurfaceView.Renderer mRenderer;
        private ArrayList<Runnable> mEventQueue = new ArrayList<Runnable>();
        private EglHelper mEglHelper;

        GLThread(GLSurfaceView.Renderer renderer, GLSurfaceView.EGLConfigChooser chooser, GLSurfaceView.EGLContextFactory contextFactory,
                 GLSurfaceView.EGLWindowSurfaceFactory surfaceFactory, GLSurfaceView.GLWrapper wrapper) {
            super();
            mDone = false;
            mWidth = 0;
            mHeight = 0;
            mRequestRender = true;
            mRenderMode = GLWallpaperServiceES32.GLEngine.RENDERMODE_CONTINUOUSLY;
            mRenderer = renderer;
            this.mEGLConfigChooser = chooser;
            this.mEGLContextFactory = contextFactory;
            this.mEGLWindowSurfaceFactory = surfaceFactory;
            this.mGLWrapper = wrapper;
        }

        @Override
        public void run() {
            setName("GLThread " + getId());
            if (LOG_THREADS) {
                Log.i("GLThread", "starting tid=" + getId());
            }

            try {
                guardedRun();
            } catch (InterruptedException e) {
                // fall thru and exit normally
            } finally {
                sGLThreadManager.threadExiting(this);
            }
        }

        private void stopEglLocked() {
            if (mHaveEgl) {
                mHaveEgl = false;
                mEglHelper.destroySurface();
                sGLThreadManager.releaseEglSurface(this);
            }
        }

        private void guardedRun() throws InterruptedException {
            mEglHelper = new EglHelper(mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory, mGLWrapper);
            try {
                GL10 gl = null;
                boolean tellRendererSurfaceCreated = true;
                boolean tellRendererSurfaceChanged = true;

                while (!isDone()) {
                    int w = 0;
                    int h = 0;
                    boolean changed = false;
                    boolean needStart = false;
                    boolean eventsWaiting = false;

                    synchronized (sGLThreadManager) {
                        while (true) {
                            if (mPaused) {
                                stopEglLocked();
                            }
                            if (!mHasSurface) {
                                if (!mWaitingForSurface) {
                                    stopEglLocked();
                                    mWaitingForSurface = true;
                                    sGLThreadManager.notifyAll();
                                }
                            } else {
                                if (!mHaveEgl) {
                                    if (sGLThreadManager.tryAcquireEglSurface(this)) {
                                        mHaveEgl = true;
                                        mEglHelper.start();
                                        mRequestRender = true;
                                        needStart = true;
                                    }
                                }
                            }

                            if (mDone) {
                                return;
                            }

                            if (mEventsWaiting) {
                                eventsWaiting = true;
                                mEventsWaiting = false;
                                break;
                            }

                            if ((!mPaused) && mHasSurface && mHaveEgl && (mWidth > 0) && (mHeight > 0)
                                    && (mRequestRender || (mRenderMode == GLWallpaperServiceES32.GLEngine.RENDERMODE_CONTINUOUSLY))) {
                                changed = mSizeChanged;
                                w = mWidth;
                                h = mHeight;
                                mSizeChanged = false;
                                mRequestRender = false;
                                if (mHasSurface && mWaitingForSurface) {
                                    changed = true;
                                    mWaitingForSurface = false;
                                    sGLThreadManager.notifyAll();
                                }
                                break;
                            }

                            if (LOG_THREADS) {
                                Log.i("GLThread", "waiting tid=" + getId());
                            }
                            sGLThreadManager.wait();
                        }
                    }

                    if (eventsWaiting) {
                        Runnable r;
                        while ((r = getEvent()) != null) {
                            r.run();
                            if (isDone()) {
                                return;
                            }
                        }
                        continue;
                    }

                    if (needStart) {
                        tellRendererSurfaceCreated = true;
                        changed = true;
                    }
                    if (changed) {
                        gl = (GL10) mEglHelper.createSurface(mHolder);
                        tellRendererSurfaceChanged = true;
                    }
                    if (tellRendererSurfaceCreated) {
                        mRenderer.onSurfaceCreated(gl, mEglHelper.mEglConfig);
                        tellRendererSurfaceCreated = false;
                    }
                    if (tellRendererSurfaceChanged) {
                        mRenderer.onSurfaceChanged(gl, w, h);
                        tellRendererSurfaceChanged = false;
                    }
                    if ((w > 0) && (h > 0)) {
                        mRenderer.onDrawFrame(gl);
                        mEglHelper.swap();
                        Thread.sleep(10);
                    }
                }
            } finally {
                synchronized (sGLThreadManager) {
                    stopEglLocked();
                    mEglHelper.finish();
                }
            }
        }

        private boolean isDone() {
            synchronized (sGLThreadManager) {
                return mDone;
            }
        }

        public void setRenderMode(int renderMode) {
            if (!((GLWallpaperServiceES32.GLEngine.RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= GLWallpaperServiceES32.GLEngine.RENDERMODE_CONTINUOUSLY))) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (sGLThreadManager) {
                mRenderMode = renderMode;
                if (renderMode == GLWallpaperServiceES32.GLEngine.RENDERMODE_CONTINUOUSLY) {
                    sGLThreadManager.notifyAll();
                }
            }
        }

        public int getRenderMode() {
            synchronized (sGLThreadManager) {
                return mRenderMode;
            }
        }

        public void requestRender() {
            synchronized (sGLThreadManager) {
                mRequestRender = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            mHolder = holder;
            synchronized (sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceCreated tid=" + getId());
                }
                mHasSurface = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void surfaceDestroyed() {
            synchronized (sGLThreadManager) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "surfaceDestroyed tid=" + getId());
                }
                mHasSurface = false;
                sGLThreadManager.notifyAll();
                while (!mWaitingForSurface && isAlive() && !mDone) {
                    try {
                        sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (sGLThreadManager) {
                mPaused = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void onResume() {
            synchronized (sGLThreadManager) {
                mPaused = false;
                mRequestRender = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (sGLThreadManager) {
                mWidth = w;
                mHeight = h;
                mSizeChanged = true;
                sGLThreadManager.notifyAll();
            }
        }

        public void requestExitAndWait() {
            synchronized (sGLThreadManager) {
                mDone = true;
                sGLThreadManager.notifyAll();
            }
            try {
                join();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        public void queueEvent(Runnable r) {
            synchronized (this) {
                mEventQueue.add(r);
                synchronized (sGLThreadManager) {
                    mEventsWaiting = true;
                    sGLThreadManager.notifyAll();
                }
            }
        }

        private Runnable getEvent() {
            synchronized (this) {
                if (mEventQueue.size() > 0) {
                    return mEventQueue.remove(0);
                }
            }
            return null;
        }

        private class GLThreadManager {
            public synchronized void threadExiting(GLThread thread) {
                if (LOG_THREADS) {
                    Log.i("GLThread", "exiting tid=" + thread.getId());
                }
                thread.mDone = true;
                if (mEglOwner == thread) {
                    mEglOwner = null;
                }
                notifyAll();
            }

            public synchronized boolean tryAcquireEglSurface(GLThread thread) {
                if (mEglOwner == thread || mEglOwner == null) {
                    mEglOwner = thread;
                    notifyAll();
                    return true;
                }
                return false;
            }

            public synchronized void releaseEglSurface(GLThread thread) {
                if (mEglOwner == thread) {
                    mEglOwner = null;
                }
                notifyAll();
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* Config chooser classes                                             */

    @Deprecated
    interface EGLConfigChooser extends GLSurfaceView.EGLConfigChooser {
    }

    abstract static class BaseConfigChooser implements GLSurfaceView.EGLConfigChooser {
        public BaseConfigChooser(int[] configSpec) {
            mConfigSpec = configSpec;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, mConfigSpec, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs, num_config);
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs);

        protected int[] mConfigSpec;

        public static class ComponentSizeChooser extends BaseConfigChooser {
            public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize,
                                        int stencilSize) {
                super(new int[]{EGL10.EGL_RED_SIZE, redSize, EGL10.EGL_GREEN_SIZE, greenSize, EGL10.EGL_BLUE_SIZE,
                        blueSize, EGL10.EGL_ALPHA_SIZE, alphaSize, EGL10.EGL_DEPTH_SIZE, depthSize, EGL10.EGL_STENCIL_SIZE,
                        stencilSize, EGL10.EGL_NONE});
                mValue = new int[1];
                mRedSize = redSize;
                mGreenSize = greenSize;
                mBlueSize = blueSize;
                mAlphaSize = alphaSize;
                mDepthSize = depthSize;
                mStencilSize = stencilSize;
            }

            @Override
            public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
                EGLConfig closestConfig = null;
                int closestDistance = 1000;
                for (EGLConfig config : configs) {
                    int d = findConfigAttrib(egl, display, config, EGL10.EGL_DEPTH_SIZE, 0);
                    int s = findConfigAttrib(egl, display, config, EGL10.EGL_STENCIL_SIZE, 0);
                    if (d >= mDepthSize && s >= mStencilSize) {
                        int r = findConfigAttrib(egl, display, config, EGL10.EGL_RED_SIZE, 0);
                        int g = findConfigAttrib(egl, display, config, EGL10.EGL_GREEN_SIZE, 0);
                        int b = findConfigAttrib(egl, display, config, EGL10.EGL_BLUE_SIZE, 0);
                        int a = findConfigAttrib(egl, display, config, EGL10.EGL_ALPHA_SIZE, 0);
                        int distance = Math.abs(r - mRedSize) + Math.abs(g - mGreenSize) + Math.abs(b - mBlueSize)
                                + Math.abs(a - mAlphaSize);
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestConfig = config;
                        }
                    }
                }
                return closestConfig;
            }

            private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
                if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                    return mValue[0];
                }
                return defaultValue;
            }

            private int[] mValue;
            protected int mRedSize;
            protected int mGreenSize;
            protected int mBlueSize;
            protected int mAlphaSize;
            protected int mDepthSize;
            protected int mStencilSize;
        }

        public static class SimpleEGLConfigChooser extends ComponentSizeChooser {
            public SimpleEGLConfigChooser(boolean withDepthBuffer) {
                super(4, 4, 4, 0, withDepthBuffer ? 16 : 0, 0);
                mRedSize = 5;
                mGreenSize = 6;
                mBlueSize = 5;
            }
        }
    }
}

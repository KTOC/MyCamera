package com.example.test.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.opengl.GLSurfaceView.Renderer;
import android.util.AttributeSet;
import android.util.Log;

public class StreamGLSurfaceView extends GLSurfaceView implements Renderer,
        OnFrameAvailableListener {

    private static String TAG = "StreamGLSurfaceView";
    private static final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    private static final int FRAME_TIME = 20;
    private boolean surfaceChanged = false;
    private boolean updateSurface = false;
    private boolean isUpdated = false;

    private float[] mMVPMatrix = new float[16];
    private float[] mSTMatrix = new float[16];

    private int mProgram;
    private int mTextureID;
    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;
    private SurfaceTexture mSurfaceTexture;
    private int[] textures = new int[1];

    private static final int FLOAT_SIZE_BYTES = 4;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private final float[] mTriangleVerticesData = {
            // X, Y, Z, U, V
            -1.0f, -1.0f, 0, 0.f, 0.f,
            1.0f, -1.0f, 0, 1.f, 0.f,
            -1.0f, 1.0f,0, 0.f, 1.f,
            1.0f, 1.0f, 0, 1.f, 1.f, };

    private FloatBuffer mTriangleVertices;

    private final String mVertexShader = "uniform mat4 uMVPMatrix;\n"
            + "uniform mat4 uSTMatrix;\n" + "attribute vec4 aPosition;\n"
            + "attribute vec4 aTextureCoord;\n"
            + "varying vec2 vTextureCoord;\n" + "void main() {\n"
            + "  gl_Position = uMVPMatrix * aPosition;\n"
            + "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" + "}\n";

    private final String mFragmentShader = "#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "varying vec2 vTextureCoord;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "void main() {\n"
            + "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" + "}\n";

    public StreamGLSurfaceView(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public StreamGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        setBackground(null);

        setEGLContextClientVersion(2);
        setEGLConfigChooser(5, 6, 5, 0, 0, 0);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mProgram = createProgram(mVertexShader, mFragmentShader);
        if (mProgram == 0) {
            return;
        }
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for aTextureCoord");
        }

        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uMVPMatrix");
        }

        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uSTMatrix");
        checkGlError("glGetUniformLocation uSTMatrix");
        if (muSTMatrixHandle == -1) {
            throw new RuntimeException(
                    "Could not get attrib location for uSTMatrix");
        }

        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        checkGlError("glBindTexture mTextureID");

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        updateSurface = false;
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        if (mTriangleVertices == null) {
            mTriangleVertices = ByteBuffer
                    .allocateDirect(
                            mTriangleVerticesData.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTriangleVertices.put(mTriangleVerticesData).position(0);
        }
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public void onDrawFrame(GL10 gl) {
        long startTime;
        long endTime;

        startTime = System.currentTimeMillis();
        if (mSurfaceTexture != null) {
            initGlDisplay(gl);

            try {
                if (!isUpdated) {
                    mSurfaceTexture.updateTexImage();
                    isUpdated = true;
                }

                if (updateSurface) {
                    updateSurface = false;

                    mSurfaceTexture.updateTexImage();
                    mSurfaceTexture.getTransformMatrix(mSTMatrix);

                    GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f);
                    GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT
                            | GLES20.GL_COLOR_BUFFER_BIT);

                    GLES20.glUseProgram(mProgram);
                    checkGlError("glUseProgram");

                    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                    GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);

                    mTriangleVertices
                            .position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
                    GLES20.glVertexAttribPointer(maPositionHandle, 3,
                            GLES20.GL_FLOAT, false,
                            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                            mTriangleVertices);
                    checkGlError("glVertexAttribPointer maPosition");
                    GLES20.glEnableVertexAttribArray(maPositionHandle);
                    checkGlError("glEnableVertexAttribArray maPositionHandle");

                    mTriangleVertices
                            .position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
                    GLES20.glVertexAttribPointer(maTextureHandle, 3,
                            GLES20.GL_FLOAT, false,
                            TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
                            mTriangleVertices);
                    checkGlError("glVertexAttribPointer maTextureHandle");
                    GLES20.glEnableVertexAttribArray(maTextureHandle);
                    checkGlError("glEnableVertexAttribArray maTextureHandle");

                    Matrix.setIdentityM(mMVPMatrix, 0);
                    GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false,
                            mMVPMatrix, 0);
                    GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false,
                            mSTMatrix, 0);

                    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
                    checkGlError("glDrawArrays");
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }

        endTime = System.currentTimeMillis();

        if (endTime - startTime < FRAME_TIME) {
            try {
                Thread.sleep(FRAME_TIME - (endTime - startTime));
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void initGlDisplay(GL10 gl) {
        if (surfaceChanged) {
            if (mTextureID != 0) {
                GLES20.glDeleteTextures(1, textures, 0);
            }

            /*
             * Create our texture. This has to be done each time the surface is
             * created.
             */

            GLES20.glGenTextures(1, textures, 0);

            mTextureID = textures[0];

            mSurfaceTexture.detachFromGLContext();
            mSurfaceTexture.attachToGLContext(mTextureID);
            GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
            checkGlError("glBindTexture textureName");

            // Can't do mipmapping with camera source
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            // Clamp to edge is the only option
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES,
                    GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            checkGlError("glTexParameteri textureName");

            surfaceChanged = false;
        }
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        updateSurface = false;

        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
        }

        mSurfaceTexture = surfaceTexture;
        if (mSurfaceTexture != null) {
            mSurfaceTexture.setOnFrameAvailableListener(this);
            surfaceChanged = true;
            isUpdated = false;
            requestRender();
        }
    }

	long startTime = System.currentTimeMillis();
    
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO Auto-generated method stub
		long time = System.currentTimeMillis();
		if (time - startTime > 200) {
			Log.d(TAG, "onFrameAvailable time = " + (time - startTime));
		}
		startTime = time;
		updateSurface = true;
		requestRender();
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader != 0) {
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);
            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0) {
                Log.e(TAG, "Could not compile shader " + shaderType + ":");
                Log.e(TAG, GLES20.glGetShaderInfoLog(shader));
                GLES20.glDeleteShader(shader);
                shader = 0;
            }
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("glAttachShader");
            GLES20.glAttachShader(program, pixelShader);
            checkGlError("glAttachShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "Could not link program: ");
                Log.e(TAG, GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            }
        }
        return program;
    }
    
    

    private void checkGlError(String op) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + ": glError " + error);
            throw new RuntimeException(op + ": glError " + error);
        }
    }
}

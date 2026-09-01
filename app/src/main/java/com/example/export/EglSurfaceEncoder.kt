package com.example.export

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * High-performance EGL and GLES20 surface renderer that guarantees exact presentation timestamps (PTS)
 * per video frame on Android's MediaCodec input surface via EGLExt.eglPresentationTimeANDROID.
 */
class EglSurfaceEncoder(private val surface: Surface) {

  private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
  private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
  private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

  private var programId = 0
  private var textureId = 0
  private var positionHandle = 0
  private var texCoordHandle = 0
  private var textureUniformHandle = 0

  private val vertexBuffer: FloatBuffer
  private val texCoordBuffer: FloatBuffer

  // Standard full-screen quad coordinates
  private val vertexCoords = floatArrayOf(
    -1.0f, -1.0f,
     1.0f, -1.0f,
    -1.0f,  1.0f,
     1.0f,  1.0f
  )

  // Texture coordinates (Flipped vertically to match Android Canvas coordinate space)
  private val texCoords = floatArrayOf(
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
  )

  init {
    vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .apply {
        put(vertexCoords)
        position(0)
      }

    texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .apply {
        put(texCoords)
        position(0)
      }

    initEgl()
    initGl()
  }

  private fun initEgl() {
    eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
    if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
      throw RuntimeException("unable to get EGL14 display")
    }

    val version = IntArray(2)
    if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
      throw RuntimeException("unable to initialize EGL14")
    }

    val attribList = intArrayOf(
      EGL14.EGL_RED_SIZE, 8,
      EGL14.EGL_GREEN_SIZE, 8,
      EGL14.EGL_BLUE_SIZE, 8,
      EGL14.EGL_ALPHA_SIZE, 8,
      EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
      EGL_RECORDABLE_ANDROID, 1,
      EGL14.EGL_NONE
    )

    val configs = arrayOfNulls<EGLConfig>(1)
    val numConfigs = IntArray(1)
    if (!EGL14.eglChooseConfig(eglDisplay, attribList, 0, configs, 0, configs.size, numConfigs, 0) || numConfigs[0] <= 0) {
      // Fallback without EGL_RECORDABLE_ANDROID
      val fallbackAttribList = intArrayOf(
        EGL14.EGL_RED_SIZE, 8,
        EGL14.EGL_GREEN_SIZE, 8,
        EGL14.EGL_BLUE_SIZE, 8,
        EGL14.EGL_ALPHA_SIZE, 8,
        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
        EGL14.EGL_NONE
      )
      if (!EGL14.eglChooseConfig(eglDisplay, fallbackAttribList, 0, configs, 0, configs.size, numConfigs, 0) || numConfigs[0] <= 0) {
        throw RuntimeException("unable to find a suitable EGLConfig")
      }
    }

    val contextAttribs = intArrayOf(
      EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
      EGL14.EGL_NONE
    )

    eglContext = EGL14.eglCreateContext(eglDisplay, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
    if (eglContext == EGL14.EGL_NO_CONTEXT) {
      throw RuntimeException("failed to create EGL context")
    }

    val surfaceAttribs = intArrayOf(EGL14.EGL_NONE)
    eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, configs[0], surface, surfaceAttribs, 0)
    if (eglSurface == EGL14.EGL_NO_SURFACE) {
      throw RuntimeException("failed to create EGL window surface")
    }

    if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
      throw RuntimeException("eglMakeCurrent failed")
    }
  }

  private fun initGl() {
    val vertexShaderCode = """
      attribute vec4 aPosition;
      attribute vec2 aTexCoord;
      varying vec2 vTexCoord;
      void main() {
        gl_Position = aPosition;
        vTexCoord = aTexCoord;
      }
    """.trimIndent()

    val fragmentShaderCode = """
      precision mediump float;
      varying vec2 vTexCoord;
      uniform sampler2D uTexture;
      void main() {
        gl_FragColor = texture2D(uTexture, vTexCoord);
      }
    """.trimIndent()

    val vShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
    val fShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

    programId = GLES20.glCreateProgram().also { prog ->
      GLES20.glAttachShader(prog, vShader)
      GLES20.glAttachShader(prog, fShader)
      GLES20.glLinkProgram(prog)
    }

    positionHandle = GLES20.glGetAttribLocation(programId, "aPosition")
    texCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
    textureUniformHandle = GLES20.glGetUniformLocation(programId, "uTexture")

    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    textureId = textures[0]

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
  }

  private fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
      GLES20.glShaderSource(shader, shaderCode)
      GLES20.glCompileShader(shader)
    }
  }

  /**
   * Uploads bitmap, renders fullscreen quad, tags exact timestamp in nanoseconds, and swaps buffers.
   */
  fun renderBitmapFrame(bitmap: Bitmap, timestampNs: Long, width: Int, height: Int) {
    if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
      return
    }

    GLES20.glViewport(0, 0, width, height)
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    GLES20.glUseProgram(programId)

    // Bind texture and upload bitmap
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    GLES20.glUniform1i(textureUniformHandle, 0)

    // Position vertices
    GLES20.glEnableVertexAttribArray(positionHandle)
    GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

    // TexCoord vertices
    GLES20.glEnableVertexAttribArray(texCoordHandle)
    GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

    // Draw Quad
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    GLES20.glDisableVertexAttribArray(positionHandle)
    GLES20.glDisableVertexAttribArray(texCoordHandle)

    // Set precise deterministic presentation timestamp in MediaCodec input surface pipeline
    EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, timestampNs)
    EGL14.eglSwapBuffers(eglDisplay, eglSurface)
  }

  fun release() {
    if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
      GLES20.glDeleteTextures(1, intArrayOf(textureId), 0)
      GLES20.glDeleteProgram(programId)

      EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
      EGL14.eglDestroySurface(eglDisplay, eglSurface)
      EGL14.eglDestroyContext(eglDisplay, eglContext)
      EGL14.eglReleaseThread()
      EGL14.eglTerminate(eglDisplay)
    }
    eglDisplay = EGL14.EGL_NO_DISPLAY
    eglContext = EGL14.EGL_NO_CONTEXT
    eglSurface = EGL14.EGL_NO_SURFACE
  }

  companion object {
    private const val EGL_RECORDABLE_ANDROID = 0x3142
  }
}

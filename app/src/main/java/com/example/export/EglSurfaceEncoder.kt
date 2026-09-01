package com.example.export

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLExt
import android.opengl.EGLSurface
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.Surface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * High-performance EGL and GLES20 surface renderer that guarantees exact presentation timestamps (PTS)
 * per video frame on Android's MediaCodec input surface via EGLExt.eglPresentationTimeANDROID.
 *
 * Supports both direct OES hardware texture rendering (ultra-fast decoding pipeline)
 * and 2D Bitmap compositing.
 */
class EglSurfaceEncoder(private val surface: Surface) {

  private var eglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
  private var eglContext: EGLContext = EGL14.EGL_NO_CONTEXT
  private var eglSurface: EGLSurface = EGL14.EGL_NO_SURFACE

  // 2D Texture Shader (Bitmap compositing & Overlay)
  private var program2DId = 0
  private var texture2DId = 0
  private var pos2DHandle = 0
  private var texCoord2DHandle = 0
  private var texUniform2DHandle = 0

  // OES Texture Shader (Direct Hardware Decoded SurfaceTexture)
  private var programOesId = 0
  private var posOesHandle = 0
  private var texCoordOesHandle = 0
  private var texMatrixOesHandle = 0
  private var texUniformOesHandle = 0

  private val vertexBuffer: FloatBuffer
  private val texCoordBufferFlipped: FloatBuffer
  private val texCoordBufferNormal: FloatBuffer

  // Standard full-screen quad coordinates
  private val vertexCoords = floatArrayOf(
    -1.0f, -1.0f,
     1.0f, -1.0f,
    -1.0f,  1.0f,
     1.0f,  1.0f
  )

  // Texture coordinates (Flipped vertically to match Android Canvas coordinate space)
  private val texCoordsFlipped = floatArrayOf(
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
  )

  // Normal texture coordinates for OES
  private val texCoordsNormal = floatArrayOf(
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f
  )

  init {
    vertexBuffer = ByteBuffer.allocateDirect(vertexCoords.size * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .apply {
        put(vertexCoords)
        position(0)
      }

    texCoordBufferFlipped = ByteBuffer.allocateDirect(texCoordsFlipped.size * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .apply {
        put(texCoordsFlipped)
        position(0)
      }

    texCoordBufferNormal = ByteBuffer.allocateDirect(texCoordsNormal.size * 4)
      .order(ByteOrder.nativeOrder())
      .asFloatBuffer()
      .apply {
        put(texCoordsNormal)
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
    // 1. 2D Standard Program
    val vertexShaderCode2D = """
      attribute vec4 aPosition;
      attribute vec2 aTexCoord;
      varying vec2 vTexCoord;
      void main() {
        gl_Position = aPosition;
        vTexCoord = aTexCoord;
      }
    """.trimIndent()

    val fragmentShaderCode2D = """
      precision mediump float;
      varying vec2 vTexCoord;
      uniform sampler2D uTexture;
      void main() {
        gl_FragColor = texture2D(uTexture, vTexCoord);
      }
    """.trimIndent()

    val vShader2D = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode2D)
    val fShader2D = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode2D)

    program2DId = GLES20.glCreateProgram().also { prog ->
      GLES20.glAttachShader(prog, vShader2D)
      GLES20.glAttachShader(prog, fShader2D)
      GLES20.glLinkProgram(prog)
    }

    pos2DHandle = GLES20.glGetAttribLocation(program2DId, "aPosition")
    texCoord2DHandle = GLES20.glGetAttribLocation(program2DId, "aTexCoord")
    texUniform2DHandle = GLES20.glGetUniformLocation(program2DId, "uTexture")

    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    texture2DId = textures[0]

    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2DId)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

    // 2. OES External Texture Program
    val vertexShaderCodeOES = """
      attribute vec4 aPosition;
      attribute vec2 aTexCoord;
      uniform mat4 uTexMatrix;
      varying vec2 vTexCoord;
      void main() {
        gl_Position = aPosition;
        vTexCoord = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
      }
    """.trimIndent()

    val fragmentShaderCodeOES = """
      #extension GL_OES_EGL_image_external : require
      precision mediump float;
      varying vec2 vTexCoord;
      uniform samplerExternalOES uOesTexture;
      void main() {
        gl_FragColor = texture2D(uOesTexture, vTexCoord);
      }
    """.trimIndent()

    val vShaderOES = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCodeOES)
    val fShaderOES = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCodeOES)

    programOesId = GLES20.glCreateProgram().also { prog ->
      GLES20.glAttachShader(prog, vShaderOES)
      GLES20.glAttachShader(prog, fShaderOES)
      GLES20.glLinkProgram(prog)
    }

    posOesHandle = GLES20.glGetAttribLocation(programOesId, "aPosition")
    texCoordOesHandle = GLES20.glGetAttribLocation(programOesId, "aTexCoord")
    texMatrixOesHandle = GLES20.glGetUniformLocation(programOesId, "uTexMatrix")
    texUniformOesHandle = GLES20.glGetUniformLocation(programOesId, "uOesTexture")
  }

  private fun loadShader(type: Int, shaderCode: String): Int {
    return GLES20.glCreateShader(type).also { shader ->
      GLES20.glShaderSource(shader, shaderCode)
      GLES20.glCompileShader(shader)
    }
  }

  /**
   * Generates a new OpenGL OES Texture ID for binding with a SurfaceTexture.
   */
  fun createOesTexture(): Int {
    val textures = IntArray(1)
    GLES20.glGenTextures(1, textures, 0)
    val oesId = textures[0]
    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesId)
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
    GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
    return oesId
  }

  /**
   * Fast Hardware Rendering:
   * 1. Renders the decoded hardware video frame from OES texture
   * 2. Overlays the transparent character overlay bitmap with alpha blending
   * 3. Tags presentation timestamp and swaps buffers
   */
  fun renderOesAndOverlayFrame(
    oesTextureId: Int,
    texMatrix: FloatArray,
    overlayBitmap: Bitmap,
    timestampNs: Long,
    width: Int,
    height: Int
  ) {
    if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) return

    GLES20.glViewport(0, 0, width, height)
    GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

    // 1. Draw OES Hardware Video Texture
    GLES20.glDisable(GLES20.GL_BLEND)
    GLES20.glUseProgram(programOesId)

    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId)
    GLES20.glUniform1i(texUniformOesHandle, 0)
    GLES20.glUniformMatrix4fv(texMatrixOesHandle, 1, false, texMatrix, 0)

    GLES20.glEnableVertexAttribArray(posOesHandle)
    GLES20.glVertexAttribPointer(posOesHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

    GLES20.glEnableVertexAttribArray(texCoordOesHandle)
    GLES20.glVertexAttribPointer(texCoordOesHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBufferNormal)

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    GLES20.glDisableVertexAttribArray(posOesHandle)
    GLES20.glDisableVertexAttribArray(texCoordOesHandle)

    // 2. Draw 2D Character Overlay with Alpha Blending
    GLES20.glEnable(GLES20.GL_BLEND)
    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

    GLES20.glUseProgram(program2DId)
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2DId)
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, overlayBitmap, 0)
    GLES20.glUniform1i(texUniform2DHandle, 0)

    GLES20.glEnableVertexAttribArray(pos2DHandle)
    GLES20.glVertexAttribPointer(pos2DHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

    GLES20.glEnableVertexAttribArray(texCoord2DHandle)
    GLES20.glVertexAttribPointer(texCoord2DHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBufferFlipped)

    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    GLES20.glDisableVertexAttribArray(pos2DHandle)
    GLES20.glDisableVertexAttribArray(texCoord2DHandle)
    GLES20.glDisable(GLES20.GL_BLEND)

    // 3. Presentation Timestamp and Buffer Swap
    EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, timestampNs)
    EGL14.eglSwapBuffers(eglDisplay, eglSurface)
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

    GLES20.glDisable(GLES20.GL_BLEND)
    GLES20.glUseProgram(program2DId)

    // Bind texture and upload bitmap
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture2DId)
    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    GLES20.glUniform1i(texUniform2DHandle, 0)

    // Position vertices
    GLES20.glEnableVertexAttribArray(pos2DHandle)
    GLES20.glVertexAttribPointer(pos2DHandle, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

    // TexCoord vertices
    GLES20.glEnableVertexAttribArray(texCoord2DHandle)
    GLES20.glVertexAttribPointer(texCoord2DHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBufferFlipped)

    // Draw Quad
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

    GLES20.glDisableVertexAttribArray(pos2DHandle)
    GLES20.glDisableVertexAttribArray(texCoord2DHandle)

    // Set precise deterministic presentation timestamp in MediaCodec input surface pipeline
    EGLExt.eglPresentationTimeANDROID(eglDisplay, eglSurface, timestampNs)
    EGL14.eglSwapBuffers(eglDisplay, eglSurface)
  }

  fun release() {
    if (eglDisplay != EGL14.EGL_NO_DISPLAY) {
      GLES20.glDeleteTextures(1, intArrayOf(texture2DId), 0)
      GLES20.glDeleteProgram(program2DId)
      GLES20.glDeleteProgram(programOesId)

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

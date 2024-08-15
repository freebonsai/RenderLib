package com.github.bonsai.renderlib.shaders

import org.apache.commons.io.IOUtils
import org.lwjgl.opengl.*
import java.io.InputStream


abstract class Shader(fragmentShader: InputStream, vertexShader: InputStream) {
    private var programId: Int = 0
    private var uniformsMap = HashMap<String, Int>()

    init {
        var vertexShaderID = 0
        var fragmentShaderID = 0

        try {
            vertexShaderID = createShader(IOUtils.toString(vertexShader), ARBVertexShader.GL_VERTEX_SHADER_ARB, "/shaders/source/entity/vertex.vsh")
            IOUtils.closeQuietly(vertexShader)

            fragmentShaderID = createShader(IOUtils.toString(fragmentShader), ARBFragmentShader.GL_FRAGMENT_SHADER_ARB, "/shaders/$fragmentShader")
            IOUtils.closeQuietly(fragmentShader)
        } catch (e: Exception) {
            println("RenderLib: Error creating shader ${this::class.java.simpleName}")
            e.printStackTrace()
        }

        if (vertexShaderID != 0 && fragmentShaderID != 0) {
            programId = ARBShaderObjects.glCreateProgramObjectARB()
            if (programId != 0) {
                ARBShaderObjects.glAttachObjectARB(programId, vertexShaderID)
                ARBShaderObjects.glAttachObjectARB(programId, fragmentShaderID)

                ARBShaderObjects.glLinkProgramARB(programId)
                ARBShaderObjects.glValidateProgramARB(programId)
            }
        }

    }

    fun startShader() {
        GL11.glPushMatrix()
        GL20.glUseProgram(programId)

        if (uniformsMap.isEmpty()) {
            setupUniforms()
        }

        updateUniforms()
    }

    fun stopShader() {
        GL20.glUseProgram(0)
        GL11.glPopMatrix()
    }

    abstract fun setupUniforms()

    abstract fun updateUniforms()

    private fun createShader(shaderSource: String, shaderType: Int, shaderName: String): Int {
        var shader = 0

        try {
            shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType)

            if (shader == 0) return 0

            ARBShaderObjects.glShaderSourceARB(shader, shaderSource)
            ARBShaderObjects.glCompileShaderARB(shader)

            if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE)
                throw RuntimeException("RenderLib: Error creating shader: ${this::class.java.simpleName} " + getLogInfo(shader))
            else println("RenderLib: Successfully created shader $shaderName")

            return shader
        } catch (e: Exception) {
            ARBShaderObjects.glDeleteObjectARB(shader)
            throw e
        }
    }

    private fun getLogInfo(i: Int) =
        ARBShaderObjects.glGetInfoLogARB(i, ARBShaderObjects.glGetObjectParameteriARB(i, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB))


    private fun setUniform(uniformName: String, location: Int) {
        uniformsMap[uniformName] = location
    }

    fun setupUniform(uniformName: String) =
        setUniform(uniformName, GL20.glGetUniformLocation(programId, uniformName))


    fun getUniform(uniformName: String) =
        uniformsMap[uniformName] ?: throw NoSuchElementException("No uniform with name $uniformName")
}
package com.github.bonsai.renderlib.shaders

import com.github.bonsai.renderlib.RenderLib.mc
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.GL11.*
import java.io.InputStream


abstract class FramebufferShader(fragmentShader: InputStream, vertexShader: InputStream) : Shader(fragmentShader, vertexShader) {
    protected var radius: Float = 2f
    private var entityShadows = false

    fun startDraw() {
        GlStateManager.pushMatrix()

        framebuffer = setupFrameBuffer(framebuffer)
        framebuffer.bindFramebuffer(true)
        entityShadows = mc.gameSettings.entityShadows
        mc.gameSettings.entityShadows = false
    }

    fun stopDraw(radius: Float) {
        mc.gameSettings.entityShadows = entityShadows
        mc.framebuffer.bindFramebuffer(true)
        this.radius = radius

        startShader()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        framebuffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false)
        GlStateManager.disableBlend()
        stopShader()

        GlStateManager.popMatrix()
    }

    /**
     * @param frameBuffer
     * @return frameBuffer
     */
    private fun setupFrameBuffer(frameBuffer: Framebuffer): Framebuffer {
        return if (frameBuffer.framebufferWidth != mc.displayWidth || frameBuffer.framebufferHeight  != mc.displayHeight) {
            Framebuffer(mc.displayWidth, mc.displayHeight, true)
        } else {
            frameBuffer.framebufferClear()
            frameBuffer
        }
    }

    companion object {
        private var framebuffer = Framebuffer(mc.displayWidth, mc.displayHeight, true)
    }
}
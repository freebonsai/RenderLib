package com.github.bonsai.renderlib.utils

import com.github.bonsai.renderlib.RenderLib.renderManager
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import java.nio.FloatBuffer

object RenderUtils3D {
    private val BUF_FLOAT_4 = BufferUtils.createFloatBuffer(4)
    var isRenderingOutlinedEntities = false
        private set
    private val cyl = Cylinder()

    private fun blendFactor() = GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

    fun texEnv(par: Int, params: FloatBuffer) {
        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, par, params)
    }

    fun texEnvi(par: Int, param: Int) {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, par, param)
    }

    fun translate(x: Number, y: Number, z: Number = 1f) = GlStateManager.translate(x.toDouble(), y.toDouble(), z.toDouble())

    fun rotate(degrees: Number, axis: Int) {
        when (axis) {
            0 -> GlStateManager.rotate(degrees.toFloat(), 1f, 0f, 1f)
            1 -> GlStateManager.rotate(degrees.toFloat(), 0f, 1f, 0f)
            2 -> GlStateManager.rotate(degrees.toFloat(), 0f, 0f, 1f)
        }
    }

    private fun preDraw() {
        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        blendFactor()
        translate(-renderManager.viewerPosX, -renderManager.viewerPosY, -renderManager.viewerPosZ)
    }

    fun depth(depth: Boolean) {
        if (depth) GlStateManager.enableDepth() else GlStateManager.disableDepth()
        GlStateManager.depthMask(depth)
    }

    private fun resetDepth() {
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
    }

    private fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        (-1).bind()
        GlStateManager.popMatrix()
    }

    fun enableOutlineMode() {
        isRenderingOutlinedEntities = true
        texEnvi(GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        texEnvi(GL13.GL_COMBINE_RGB,      GL11.GL_REPLACE)
        texEnvi(GL13.GL_SOURCE0_RGB,      GL13.GL_CONSTANT)
        texEnvi(GL13.GL_OPERAND0_RGB,     GL11.GL_SRC_COLOR)
        texEnvi(GL13.GL_COMBINE_ALPHA,    GL11.GL_REPLACE)
        texEnvi(GL13.GL_SOURCE0_ALPHA,    GL11.GL_TEXTURE)
        texEnvi(GL13.GL_OPERAND0_ALPHA,   GL11.GL_SRC_ALPHA)
    }

    fun disableOutlineMode() {
        texEnvi(GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        texEnvi(GL13.GL_COMBINE_RGB,      GL11.GL_MODULATE)
        texEnvi(GL13.GL_SOURCE0_RGB,      GL11.GL_TEXTURE)
        texEnvi(GL13.GL_OPERAND0_RGB,     GL11.GL_SRC_COLOR)
        texEnvi(GL13.GL_COMBINE_ALPHA,    GL11.GL_MODULATE)
        texEnvi(GL13.GL_SOURCE0_ALPHA,    GL11.GL_TEXTURE)
        texEnvi(GL13.GL_OPERAND0_ALPHA,   GL11.GL_SRC_ALPHA)
        isRenderingOutlinedEntities = false
    }

    fun textureColor(color: Int) {
        BUF_FLOAT_4.put(color.floatValues)
        texEnv(GL11.GL_TEXTURE_ENV_COLOR, BUF_FLOAT_4)
    }



    fun drawCylinder(
        pos: Vec3, baseRadius: Number, topRadius: Number, height: Number,
        slices: Number, stacks: Number, rot1: Number, rot2: Number, rot3: Number,
        color: Int, depth: Boolean = false, lineMode: Boolean = false
    ) {
        preDraw()
        GL11.glLineWidth(2.0f)
        depth(depth)
        GlStateManager.depthMask(false)

        if (depth) GlStateManager.disableDepth()

        color.bind()
        translate(pos.xCoord, pos.yCoord, pos.zCoord)
        rotate(rot1, 0)
        rotate(rot2, 2)
        rotate(rot3, 1)

        cyl.drawStyle = if (lineMode) GLU.GLU_LINE else GLU.GLU_FILL
        cyl.draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())

        if (!depth) resetDepth()
        postDraw()
    }
}
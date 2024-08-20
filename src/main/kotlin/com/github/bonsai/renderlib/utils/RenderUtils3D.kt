package com.github.bonsai.renderlib.utils

import com.github.bonsai.renderlib.RenderLib.mc
import com.github.bonsai.renderlib.RenderLib.partialTicks
import com.github.bonsai.renderlib.RenderLib.renderManager
import com.github.bonsai.renderlib.RenderLib.tessellator
import com.github.bonsai.renderlib.RenderLib.worldRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

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

    fun scale(x: Number, y: Number, z: Number) = GlStateManager.scale(x.toDouble(), y.toDouble(), z.toDouble())

    fun WorldRenderer.addVertex(x: Number, y: Number, z: Number, nx: Number, ny: Number = 0, nz: Number = 0) {
        pos(x.toDouble(), y.toDouble(), z.toDouble()).normal(nx.toFloat(), ny.toFloat(), nz.toFloat()).endVertex()
    }

    fun preDraw(texture2D: Boolean = false) {
        GlStateManager.pushMatrix()
        if (!texture2D) GlStateManager.disableTexture2D() else GlStateManager.enableTexture2D()
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

    fun resetDepth() {
        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
    }

    fun postDraw() {
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.enableLighting()
        (-1).bind()
        resetDepth()
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

    /**
     * Draws text in the world at the specified position with the specified color and optional parameters.
     *
     * @param text            The text to be drawn.
     * @param vec3            The position to draw the text.
     * @param color           The color of the text.
     * @param depthTest       Indicates whether to draw with depth (default is true).
     * @param scale           The scale of the text (default is 0.03).
     * @param shadow          Indicates whether to render a shadow for the text (default is true).
     */
    fun drawStringInWorld(text: String, vec3: Vec3, color: Int = -1, depthTest: Boolean = true, scale: Float = 0.3f, shadow: Boolean = false) {
        val xMultiplier = if (mc.gameSettings.thirdPersonView == 2) -1 else 1

        preDraw()
        vec3.translate()
        rotate(-renderManager.playerViewY, 1)
        rotate(renderManager.playerViewX * xMultiplier, 0)
        scale(-scale, -scale, scale)
        depth(depthTest)

        val textWidth = mc.fontRendererObj.getStringWidth(text)
        mc.fontRendererObj.drawString("$text§r", -textWidth / 2f, 0f, color, shadow)

        postDraw()
    }

    fun drawCylinder(
        pos: Vec3, baseRadius: Number, topRadius: Number, height: Number,
        slices: Number, stacks: Number, rot1: Number, rot2: Number, rot3: Number,
        color: Int, depth: Boolean = false, lineMode: Boolean = false
    ) {
        preDraw()
        GL11.glLineWidth(2.0f)
        depth(depth)

        color.bind()
        translate(pos.xCoord, pos.yCoord, pos.zCoord)
        rotate(rot1, 0)
        rotate(rot2, 2)
        rotate(rot3, 1)

        cyl.drawStyle = if (lineMode) GLU.GLU_LINE else GLU.GLU_FILL
        cyl.draw(baseRadius.toFloat(), topRadius.toFloat(), height.toFloat(), slices.toInt(), stacks.toInt())

        postDraw()
    }

    fun drawLines(vararg points: Vec3, color: Int, lineWidth: Float, depth: Boolean) {
        if (points.size <= 1) return
        color.bind()
        preDraw()
        depth(depth)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)

        worldRenderer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
        for (point in points) {
            worldRenderer.pos(point.xCoord, point.yCoord, point.zCoord).endVertex()
        }
        tessellator.draw()

        postDraw()
    }



    private val beaconBeam = ResourceLocation("textures/entity/beacon_beam.png")

    fun drawBeaconBeam(vec3: Vec3, color: Int, depth: Boolean = false, height: Int = 300) {
        val bottomOffset = 0
        val topOffset = bottomOffset + height
        depth(depth)

        mc.textureManager.bindTexture(beaconBeam)

        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT.toFloat())
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT.toFloat())

        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO)
        GlStateManager.enableCull()
        preDraw(true)

        val time = mc.theWorld.worldTime.toDouble() + partialTicks
        val x = vec3.xCoord
        val y = vec3.yCoord
        val z = vec3.zCoord
        val d1  = MathHelper.func_181162_h(-time * 0.2 - floor(-time * 0.1))
        val d2  = time * 0.025 * -1.5
        val d4  = .5 + cos(d2 + 2.356194490192345)  * .2
        val d5  = .5 + sin(d2 + 2.356194490192345)  * .2
        val d6  = .5 + cos(d2 + (Math.PI / 4))      * .2
        val d7  = .5 + sin(d2 + (Math.PI / 4))      * .2
        val d8  = .5 + cos(d2 + 3.9269908169872414) * .2
        val d9  = .5 + sin(d2 + 3.9269908169872414) * .2
        val d10 = .5 + cos(d2 + 5.497787143782138)  * .2
        val d11 = .5 + sin(d2 + 5.497787143782138)  * .2
        val d14 = -1 + d1
        val d15 = height * 2.5 + d14

        preDraw(true)

        fun WorldRenderer.color(alpha: Float = color.alpha) {
            this.color(color.red, color.green, color.blue, alpha).endVertex()
        }

        worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)

            pos(x + d4,  y + topOffset,    z + d5 ).tex(1.0, d15).color()
            pos(x + d4,  y + bottomOffset, z + d5 ).tex(1.0, d14).color()
            pos(x + d6,  y + bottomOffset, z + d7 ).tex(0.0, d14).color()
            pos(x + d6,  y + topOffset,    z + d7 ).tex(0.0, d15).color()
            pos(x + d10, y + topOffset,    z + d11).tex(1.0, d15).color()
            pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color()
            pos(x + d8,  y + bottomOffset, z + d9 ).tex(0.0, d14).color()
            pos(x + d8,  y + topOffset,    z + d9 ).tex(0.0, d15).color()
            pos(x + d6,  y + topOffset,    z + d7 ).tex(1.0, d15).color()
            pos(x + d6,  y + bottomOffset, z + d7 ).tex(1.0, d14).color()
            pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color()
            pos(x + d10, y + topOffset,    z + d11).tex(0.0, d15).color()
            pos(x + d8,  y + topOffset,    z + d9 ).tex(1.0, d15).color()
            pos(x + d8,  y + bottomOffset, z + d9 ).tex(1.0, d14).color()
            pos(x + d4,  y + bottomOffset, z + d5 ).tex(0.0, d14).color()
            pos(x + d4,  y + topOffset,    z + d5 ).tex(0.0, d15).color()
        }
        tessellator.draw()
        postDraw()
        preDraw(true)
        blendFactor()

        val d12 = -1 + d1
        val d13 = height + d12
        val alpha = color.alpha
        worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)
            pos(x + .2, y + topOffset,    z + .2).tex(1.0, d13).color(.25f * alpha)
            pos(x + .2, y + bottomOffset, z + .2).tex(1.0, d12).color(.25f * alpha)
            pos(x + .8, y + bottomOffset, z + .2).tex(0.0, d12).color(.25f * alpha)
            pos(x + .8, y + topOffset,    z + .2).tex(0.0, d13).color(.25f * alpha)
            pos(x + .8, y + topOffset,    z + .8).tex(1.0, d13).color(.25f * alpha)
            pos(x + .8, y + bottomOffset, z + .8).tex(1.0, d12).color(.25f * alpha)
            pos(x + .2, y + bottomOffset, z + .8).tex(0.0, d12).color(.25f * alpha)
            pos(x + .2, y + topOffset,    z + .8).tex(0.0, d13).color(.25f * alpha)
            pos(x + .8, y + topOffset,    z + .2).tex(1.0, d13).color(.25f * alpha)
            pos(x + .8, y + bottomOffset, z + .2).tex(1.0, d12).color(.25f * alpha)
            pos(x + .8, y + bottomOffset, z + .8).tex(0.0, d12).color(.25f * alpha)
            pos(x + .8, y + topOffset,    z + .8).tex(0.0, d13).color(.25f * alpha)
            pos(x + .2, y + topOffset,    z + .8).tex(1.0, d13).color(.25f * alpha)
            pos(x + .2, y + bottomOffset, z + .8).tex(1.0, d12).color(.25f * alpha)
            pos(x + .2, y + bottomOffset, z + .2).tex(0.0, d12).color(.25f * alpha)
            pos(x + .2, y + topOffset,    z + .2).tex(0.0, d13).color(.25f * alpha)

            endVertex()
        }
        tessellator.draw()
        postDraw()
    }

    /**
     * Draws a filled Axis Aligned Bounding Box (AABB).
     *
     * @param aabb The bounding box to draw.
     * @param color The color to use for drawing.
     * @param depth Whether to enable depth testing.
     */
    fun drawFilledAABB(aabb: AxisAlignedBB, color: Int, depth: Boolean = false) {
        preDraw()
        GlStateManager.enableCull()
        depth(depth)
        color.bind()

        addVertexesForFilledBox(aabb)
        tessellator.draw()

        GlStateManager.disableCull()
        postDraw()
    }

    /**
     * Draws an outlined Axis Aligned Bounding Box (AABB).
     *
     * @param aabb The bounding box to draw.
     * @param color The color to use for drawing.
     * @param thickness The thickness of the outline.
     * @param depth Whether to enable depth testing.
     */
    fun drawOutlinedAABB(aabb: AxisAlignedBB, color: Int, thickness: Number = 3f, depth: Boolean = false, smoothLines: Boolean = true) {
        preDraw()

        if (smoothLines) {
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        }

        GL11.glLineWidth(thickness.toFloat())
        depth(depth)
        color.bind()
        addVertexesForOutlinedBox(aabb)
        tessellator.draw()

        if (smoothLines) GL11.glDisable(GL11.GL_LINE_SMOOTH)

        GL11.glLineWidth(2f)
        postDraw()
    }

    private fun addVertexesForFilledBox(aabb: AxisAlignedBB) {
        with(aabb) { worldRenderer {
            begin(7, DefaultVertexFormats.POSITION_NORMAL)

            // Front face
            addVertex(minX, maxY, minZ, 0, 0, -1)
            addVertex(maxX, maxY, minZ, 0, 0, -1)
            addVertex(maxX, minY, minZ, 0, 0, -1)
            addVertex(minX, minY, minZ, 0, 0, -1)

            // Back face
            addVertex(minX, minY, maxZ, 0, 0, 1)
            addVertex(maxX, minY, maxZ, 0, 0, 1)
            addVertex(maxX, maxY, maxZ, 0, 0, 1)
            addVertex(minX, maxY, maxZ, 0, 0, 1)

            // Bottom face
            addVertex(minX, minY, minZ, 0, -1)
            addVertex(maxX, minY, minZ, 0, -1)
            addVertex(maxX, minY, maxZ, 0, -1)
            addVertex(minX, minY, maxZ, 0, -1)

            // Top face
            addVertex(minX, maxY, maxZ, 0, 1)
            addVertex(maxX, maxY, maxZ, 0, 1)
            addVertex(maxX, maxY, minZ, 0, 1)
            addVertex(minX, maxY, minZ, 0, 1)

            // Left face
            addVertex(minX, minY, maxZ, -1)
            addVertex(minX, maxY, maxZ, -1)
            addVertex(minX, maxY, minZ, -1)
            addVertex(minX, minY, minZ, -1)

            // Right face
            addVertex(maxX, minY, minZ, 1)
            addVertex(maxX, maxY, minZ, 1)
            addVertex(maxX, maxY, maxZ, 1)
            addVertex(maxX, minY, maxZ, 1)
        }}
    }

    private fun addVertexesForOutlinedBox(aabb: AxisAlignedBB) {
        with(aabb) { worldRenderer {
            begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)
            pos(minX, minY, minZ).endVertex()
            pos(minX, minY, maxZ).endVertex()
            pos(maxX, minY, maxZ).endVertex()
            pos(maxX, minY, minZ).endVertex()
            pos(minX, minY, minZ).endVertex()

            pos(minX, maxY, minZ).endVertex()
            pos(minX, maxY, maxZ).endVertex()
            pos(maxX, maxY, maxZ).endVertex()
            pos(maxX, maxY, minZ).endVertex()
            pos(minX, maxY, minZ).endVertex()

            pos(minX, maxY, maxZ).endVertex()
            pos(minX, minY, maxZ).endVertex()
            pos(maxX, minY, maxZ).endVertex()
            pos(maxX, maxY, maxZ).endVertex()
            pos(maxX, maxY, minZ).endVertex()
            pos(maxX, minY, minZ).endVertex()
        }
    }}
}
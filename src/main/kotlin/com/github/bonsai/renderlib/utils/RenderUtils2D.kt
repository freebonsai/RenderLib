package com.github.bonsai.renderlib.utils

import com.github.bonsai.renderlib.RenderLib.mc
import com.github.bonsai.renderlib.RenderLib.tessellator
import com.github.bonsai.renderlib.RenderLib.worldRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Project

object RenderUtils2D {
    data class Box2D(val x: Double, val y: Double, val w: Double, val h: Double)

    private val modelViewMatrix = BufferUtils.createFloatBuffer(16)
    private val projectionMatrix = BufferUtils.createFloatBuffer(16)
    private val viewportDims = BufferUtils.createIntBuffer(16)


    fun onRenderWorld() {
        GlStateManager.pushMatrix()
        mc.thePlayer?.renderVec?.let {
            GlStateManager.translate(-it.xCoord, -it.yCoord, -it.zCoord)
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelViewMatrix)
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionMatrix)
            GL11.glGetInteger(GL11.GL_VIEWPORT, viewportDims)
        }
        GlStateManager.popMatrix()
    }

    /**
     * Projects a 3D point to 2D screen coordinates.
     *
     * @param vec3 The 3D point to be projected.
     * @return The 2D screen coordinates as a Vec3, or null if projection fails.
     */
    private fun worldToScreenPosition(vec3: Vec3): Vec3? {
        val coords = BufferUtils.createFloatBuffer(3)
        val success = Project.gluProject(
            vec3.xCoord.toFloat(), vec3.yCoord.toFloat(), vec3.zCoord.toFloat(),
            modelViewMatrix, projectionMatrix, viewportDims, coords
        )

        return success.takeIf { it && coords[2] in 0.0..1.0 }?.run {
            val sr = ScaledResolution(mc)
            Vec3(coords[0] / sr.scaleFactor.toDouble(), (sr.scaledHeight - (coords[1] / sr.scaleFactor)).toDouble(), coords[2].toDouble())
        }
    }

    private fun calculateBoundingBox(aabb: AxisAlignedBB): Box2D? {
        var x1 = Double.MAX_VALUE
        var x2 = Double.MIN_VALUE
        var y1 = Double.MAX_VALUE
        var y2 = Double.MIN_VALUE

        aabb.corners.forEach { corner ->
            worldToScreenPosition(corner)?.let { pos ->
                x1 = x1.coerceAtMost (pos.xCoord)
                x2 = x2.coerceAtLeast(pos.xCoord)
                y1 = y1.coerceAtMost (pos.yCoord)
                y2 = y2.coerceAtLeast(pos.yCoord)
            }
        }
        return if (x1 != Double.MAX_VALUE) Box2D(x1, y1, x2, y2) else null
    }


    fun drawNameTag(vec3: Vec3, name: String) =
        worldToScreenPosition(vec3)?.let { mc.fontRendererObj.drawString(name, it.xCoord.toFloat(), it.yCoord.toFloat(), -1, true) }

    fun draw2DESP(aabb: AxisAlignedBB, color: Int, thickness: Float) =
        calculateBoundingBox(aabb)?.let { drawBox(it, color, thickness) }

    fun drawBox(box: Box2D, color: Int, lineWidth: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glLineWidth(lineWidth)
        color.bind()

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

        val corners = box.corners
        for (i in 0..9 step 2) {
            worldRenderer.pos(corners[i], corners[i + 1], 0.0).endVertex()
        }

        tessellator.draw()

        (-1).bind()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.popMatrix()
    }
}
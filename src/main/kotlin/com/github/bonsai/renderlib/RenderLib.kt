package com.github.bonsai.renderlib

import com.github.bonsai.renderlib.utils.RenderUtils2D
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.entity.RenderManager

object RenderLib {
    val mc: Minecraft = Minecraft.getMinecraft()
    internal var partialTicks = 0f
    val tessellator: Tessellator = Tessellator.getInstance()
    val worldRenderer: WorldRenderer = tessellator.worldRenderer
    val renderManager: RenderManager = mc.renderManager

    fun renderWorldTrigger(pt: Float) {
        partialTicks = pt
        RenderUtils2D.onRenderWorld()
    }
}
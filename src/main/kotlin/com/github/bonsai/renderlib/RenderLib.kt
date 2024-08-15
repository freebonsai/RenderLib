package com.github.bonsai.renderlib

import com.github.bonsai.renderlib.utils.RenderUtils2D
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer

object RenderLib {
    val mc: Minecraft = Minecraft.getMinecraft()
    internal var partialTicks = 0f
    val tessellator: Tessellator = Tessellator.getInstance()
    val worldRenderer: WorldRenderer = tessellator.worldRenderer
    
    fun setPartialTicks(pt: Float) {
        partialTicks = pt
    }

    fun renderWorldTrigger() {
        RenderUtils2D.onRenderWorld()
    }
}
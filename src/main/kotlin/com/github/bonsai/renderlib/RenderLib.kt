package com.github.bonsai.renderlib

import com.github.bonsai.renderlib.utils.RenderUtils2D
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.entity.RenderManager
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object RenderLib {
    internal val mc: Minecraft = Minecraft.getMinecraft()
    internal var partialTicks = 0f
    internal val tessellator: Tessellator = Tessellator.getInstance()
    internal val worldRenderer: WorldRenderer = tessellator.worldRenderer
    internal val renderManager: RenderManager = mc.renderManager

    fun renderWorldTrigger(pt: Float) {
        partialTicks = pt
        RenderUtils2D.onRenderWorld()
    }

    fun onOverlay(event: RenderGameOverlayEvent.Pre) = Renderer.onOverlay(event)

    fun onTick(event: TickEvent.ClientTickEvent) = Renderer.onTick(event)

    fun worldLoad(event: WorldEvent.Load) = Renderer.worldLoad(event)
}
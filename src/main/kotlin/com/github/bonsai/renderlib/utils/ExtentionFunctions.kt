package com.github.bonsai.renderlib.utils

import com.github.bonsai.renderlib.RenderLib.partialTicks
import com.github.bonsai.renderlib.utils.RenderUtils2D.Box2D
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

fun Int.bind() {
    GlStateManager.resetColor()
    GlStateManager.color((this shr 24 and 0xFF) / 255.0f, (this shr 16 and 0xFF) / 255.0f, (this shr 8 and 0xFF) / 255.0f, (this and 0xFF) / 255.0f)
}

val Box2D.corners get() = arrayListOf(x,y, w,y, w,h, x,h, x,y)

val AxisAlignedBB.corners: List<Vec3>
    get() = listOf(
        Vec3(minX, minY, minZ), Vec3(minX, maxY, minZ), Vec3(maxX, maxY, minZ), Vec3(maxX, minY, minZ),
        Vec3(minX, minY, maxZ), Vec3(minX, maxY, maxZ), Vec3(maxX, maxY, maxZ), Vec3(maxX, minY, maxZ)
    )


val Entity.renderX: Double
    get() = lastTickPosX + (posX - lastTickPosX) * partialTicks

val Entity.renderY: Double
    get() = lastTickPosY + (posY - lastTickPosY) * partialTicks

val Entity.renderZ: Double
    get() = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks

val Entity.renderVec: Vec3
    get() = Vec3(renderX, renderY, renderZ)
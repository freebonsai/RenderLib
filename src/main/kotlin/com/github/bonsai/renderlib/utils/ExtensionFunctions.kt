package com.github.bonsai.renderlib.utils

import com.github.bonsai.renderlib.RenderLib.partialTicks
import com.github.bonsai.renderlib.utils.RenderUtils2D.Box2D
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3

val Int.red:    Float get() = (this shr 24 and 0xFF) / 255.0f
val Int.green:  Float get() = (this shr 16 and 0xFF) / 255.0f
val Int.blue:   Float get() = (this shr 8  and 0xFF) / 255.0f
val Int.alpha:  Float get() = (this        and 0xFF) / 255.0f

val Int.floatValues: FloatArray get() {
    return floatArrayOf(red, green, blue, alpha)
}

fun Int.bind() {
    GlStateManager.resetColor()
    floatValues.let { GlStateManager.color(it[0], it[1], it[2], it[3]) }
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

val Entity.renderBoundingBox: AxisAlignedBB
    get() = AxisAlignedBB(
        renderX - this.width / 2,
        renderY,
        renderZ - this.width / 2,
        renderX + this.width / 2,
        renderY + this.height,
        renderZ + this.width / 2
    )

fun AxisAlignedBB.outlineBounds(): AxisAlignedBB =
    expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)


fun Vec3.toAABB(add: Double = 1.0): AxisAlignedBB {
    return AxisAlignedBB(this.xCoord, this.yCoord, this.zCoord, this.xCoord + add, this.yCoord + add, this.zCoord + add).outlineBounds()
}
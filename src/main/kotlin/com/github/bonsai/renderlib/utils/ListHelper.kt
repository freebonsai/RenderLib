package com.github.bonsai.renderlib.utils

import org.lwjgl.opengl.GL11


object ListHelper {
    data class GLList(var id: Int = -1)

    inline operator fun GLList.invoke(block: GLList.() -> Unit) {
        if (id == -1) {
            id = GL11.glGenLists(1)
            GL11.glNewList(id, GL11.GL_COMPILE)
            block.invoke(this)
            GL11.glEndList()
        }
        else GL11.glCallList(id)
    }
}
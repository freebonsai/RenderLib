package com.github.bonsai.renderlib.utils

import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.jvm.isAccessible

fun callMappedMethod(instance: Any, unmappedName: String, mappedName: String, vararg args: Any?): Any {
    return instance::class.declaredFunctions.find { it.name == unmappedName || it.name == mappedName }?.let {
        it.isAccessible = true
        it.call(instance, *args)
    } ?: throw NoSuchMethodException("Method not found: $unmappedName or $mappedName")
}

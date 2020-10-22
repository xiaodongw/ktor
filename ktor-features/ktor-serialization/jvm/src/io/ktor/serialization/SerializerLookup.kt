/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.serialization

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

@OptIn(ExperimentalStdlibApi::class)
internal fun serializerByTypeInfo(type: KType): KSerializer<*> {
    val classifierClass = type.classifier as? KClass<*>
    if (classifierClass != null && classifierClass.java.isArray) {
        return arraySerializer(type)
    }

    return serializer(type)
}

// NOTE: this should be removed once kotlinx.serialization serializer get support of arrays that is blocked by KT-32839
private fun arraySerializer(type: KType): KSerializer<*> {
    val elementType = type.arguments[0].type ?: error("Array<*> is not supported")
    val elementSerializer = serializerByTypeInfo(elementType)

    @Suppress("UNCHECKED_CAST")
    return ArraySerializer(
        elementType.jvmErasure as KClass<Any>,
        elementSerializer as KSerializer<Any>
    )
}

@OptIn(InternalSerializationApi::class)
internal fun serializerForSending(
    context: PipelineContext<Any, ApplicationCall>,
    value: Any,
    module: SerializersModule
): KSerializer<*> {
    val responseType = context.call.attributes.getOrNull(ResponseTypeAttributeKey)
    if (responseType != null) return module.serializer(responseType)

    return when (value) {
        is JsonElement -> JsonElement.serializer()
        is List<*> -> ListSerializer(value.elementSerializer(context, module))
        is Set<*> -> SetSerializer(value.elementSerializer(context, module))
        is Map<*, *> -> MapSerializer(value.keys.elementSerializer(context, module), value.values.elementSerializer(context, module))
        is Map.Entry<*, *> -> MapEntrySerializer(
            serializerForSending(context, value.key ?: error("Map.Entry(null, ...) is not supported"), module),
            serializerForSending(context, value.value ?: error("Map.Entry(..., null) is not supported)"), module)
        )
        is Array<*> -> {
            val componentType = value.javaClass.componentType.kotlin.starProjectedType
            val componentClass =
                componentType.classifier as? KClass<*> ?: error("Unsupported component type $componentType")

            @Suppress("UNCHECKED_CAST")
            ArraySerializer(
                componentClass as KClass<Any>,
                serializerByTypeInfo(componentType) as KSerializer<Any>
            )
        }
        else -> module.getContextual(value::class) ?: value::class.serializer()
    }
}

@Suppress("EXPERIMENTAL_API_USAGE_ERROR")
private fun Collection<*>.elementSerializer(
    context: PipelineContext<Any, ApplicationCall>,
    module: SerializersModule
): KSerializer<*> {
    val serializers = mapNotNull { value ->
        value?.let { serializerForSending(context, it, module) }
    }.distinctBy { it.descriptor.serialName }

    if (serializers.size > 1) {
        val message = "Serializing collections of different element types is not yet supported. " +
            "Selected serializers: ${serializers.map { it.descriptor.serialName }}"
        error(message)
    }

    val selected: KSerializer<*> = serializers.singleOrNull() ?: String.serializer()
    if (selected.descriptor.isNullable) {
        return selected
    }

    @Suppress("UNCHECKED_CAST")
    selected as KSerializer<Any>

    if (any { it == null }) {
        return selected.nullable
    }

    return selected
}

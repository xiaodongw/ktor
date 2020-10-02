/*
 * Copyright 2014-2020 JetBrains s.r.o and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package io.ktor.application

import io.ktor.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

private val FeatureConfigPhase = PipelinePhase("FeatureConfigPhase")

public interface DynamicConfigFeature<in TPipeline : Pipeline<*, ApplicationCall>, TConfiguration : Any, TFeature : Any> :
    ApplicationFeature<TPipeline, TConfiguration, TFeature> {

    public val configKey: AttributeKey<TConfiguration.() -> Unit>
        get() = AttributeKey("${key.name}_configBuilder")

    @Deprecated(
        "This feature can change it's configurations by calling `config` function in routing. " +
            "To get latest config please use `getConfiguration` function inside call interceptor.",
        replaceWith = ReplaceWith("install")
    )
    public override fun install(pipeline: TPipeline, configure: TConfiguration.() -> Unit): TFeature {
        return install(pipeline)
    }

    public fun install(pipeline: TPipeline): TFeature

    public fun PipelineContext<*, ApplicationCall>.getConfiguration(): (TConfiguration.() -> Unit) {
        return call.attributes[configKey]
    }
}

public fun <Config, Feature : DynamicConfigFeature<*, Config, *>> Route.config(
    feature: Feature,
    configBuilder: Config.() -> Unit,
    routeBuilder: Route.() -> Unit
): Route {
    val interceptor: suspend PipelineContext<*, ApplicationCall>.(Unit) -> Unit = {
        val oldConfig = call.attributes.getOrNull(feature.configKey)
        val newConfig = when (oldConfig) {
            null -> configBuilder
            else -> fun(config: Config) {
                config.apply(oldConfig).apply(configBuilder)
            }
        }
        call.attributes.put(feature.configKey, newConfig)
    }

    val configuredRoute = createChild(object : RouteSelector(RouteSelectorEvaluation.qualityConstant) {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int) = RouteSelectorEvaluation.Constant
    })

    configuredRoute.insertPhaseBefore(ApplicationCallPipeline.Features, FeatureConfigPhase)
    configuredRoute.intercept(FeatureConfigPhase, interceptor)

    routeBuilder(configuredRoute)
    return configuredRoute
}

/**
 * Installs [feature] into this pipeline, if it is not yet installed
 */
public fun <P : Pipeline<*, ApplicationCall>, B : Any, F : Any> P.install(
    feature: DynamicConfigFeature<P, B, F>,
    configure: B.() -> Unit = {}
): F {
    val registry = attributes.computeIfAbsent(featureRegistryKey) { Attributes(true) }
    when (val installedFeature = registry.getOrNull(feature.key)) {
        null -> {
            try {
                @Suppress("DEPRECATION_ERROR")
                val installed = feature.install(this, configure)
                registry.put(feature.key, installed)
                insertPhaseBefore(ApplicationCallPipeline.Features, FeatureConfigPhase)
                intercept(FeatureConfigPhase) {
                    call.attributes.put(feature.configKey, configure)
                }
                //environment.log.trace("`${feature.name}` feature was installed successfully.")
                return installed
            } catch (t: Throwable) {
                //environment.log.error("`${feature.name}` feature failed to install.", t)
                throw t
            }
        }
        feature -> {
            //environment.log.warning("`${feature.name}` feature is already installed")
            return installedFeature
        }
        else -> {
            throw DuplicateApplicationFeatureException("Conflicting application feature is already installed with the same key as `${feature.key.name}`")
        }
    }
}

package com.feature.feed.component.root.ui

import com.feature.feed.root.FeedRootComponent
import kotlin.reflect.KClass

/**
 * Ключ перехода — пара старой и новой конфигураций.
 * Порядок важен (Feed → Article и Article → Feed — разные кейсы).
 */
data class TransitionKey(
    val from: KClass<out FeedRootComponent.Config>,
    val to: KClass<out FeedRootComponent.Config>,
)

/**
 * Реестр: мапа от TransitionKey к DelayedTransition.
 * Можно пополнять новыми записями в одном месте.
 */
object TransitionRegistry {
    private val map = mutableMapOf<TransitionKey, DelayedTransition>()

    var defaultTransition: DelayedTransition = SlideFadeDelayedTransition

    fun register(
        from: KClass<out FeedRootComponent.Config>,
        to: KClass<out FeedRootComponent.Config>,
        transition: DelayedTransition,
    ) {
        map[TransitionKey(from, to)] = transition
    }

    fun get(
        from: FeedRootComponent.Config,
        to: FeedRootComponent.Config,
    ): DelayedTransition {
        return map[TransitionKey(from::class, to::class)]
            ?: defaultTransition
    }
}

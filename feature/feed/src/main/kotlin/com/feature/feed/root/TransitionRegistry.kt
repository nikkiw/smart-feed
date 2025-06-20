package com.feature.feed.root

import kotlin.reflect.KClass

/**
 * Ключ перехода — пара старой и новой конфигураций.
 * Порядок важен (Feed → Article и Article → Feed — разные кейсы).
 */
data class TransitionKey(
    val from: KClass<out FeedRootComponent.Config>,
    val to:   KClass<out FeedRootComponent.Config>
)

/**
 * Реестр: мапа от TransitionKey к DelayedTransition.
 * Можно пополнять новыми записями в одном месте.
 */
object TransitionRegistry {
    // Явные маппинги
    private val map = mutableMapOf<TransitionKey, DelayedTransition>()

    // Дефолтный переход, можно менять при инициализации
    var defaultTransition: DelayedTransition = SlideFadeDelayedTransition

    fun register(
        from: KClass<out FeedRootComponent.Config>,
        to:   KClass<out FeedRootComponent.Config>,
        transition: DelayedTransition
    ) {
        map[TransitionKey(from, to)] = transition
    }

    fun get(
        from: FeedRootComponent.Config,
        to:   FeedRootComponent.Config
    ): DelayedTransition {
        return map[ TransitionKey(from::class, to::class) ]
            ?: defaultTransition
    }
}


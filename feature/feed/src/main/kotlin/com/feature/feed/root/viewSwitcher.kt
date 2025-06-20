package com.feature.feed.root

import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialContainerTransform

@OptIn(ExperimentalDecomposeApi::class)
fun <C : Any, T : Any> ViewContext.viewSwitcherWithRegistry(
    viewFactory: (T) -> View
): ViewContext.(parent: ViewGroup, newStack: ChildStack<C, T>, oldStack: ChildStack<C, T>?) -> Unit =
    { parent, newStack, oldStack ->

        val oldView: View? = parent.getChildAt(0)
        val newView = viewFactory(newStack.active.instance)

        if (oldView != null && oldStack != null) {
            // Берём конфигах старый и новый
            val oldCfg = oldStack.active.configuration as FeedRootComponent.Config
            val newCfg = newStack.active.configuration as FeedRootComponent.Config

            val transition = TransitionRegistry.get(oldCfg, newCfg)

            transition.begin(
                parent = parent,
                newView = newView,
                oldView = oldView,
                isForward = newStack.items.size >= oldStack.items.size
            )
            // После окончания анимации явно удаляем старый и добавляем новый
            parent.removeView(oldView)
            parent.addView(newView)
            parent.requestApplyInsets()
        } else {
            parent.removeAllViews()
            parent.addView(newView)
            parent.requestApplyInsets()
        }
    }

interface DelayedTransition {
    fun begin(
        parent: ViewGroup,
        newView: View,
        oldView: View,
        isForward: Boolean
    )
}


internal object SlideFadeDelayedTransition : DelayedTransition {
    override fun begin(
        parent: ViewGroup,
        newView: View,
        oldView: View,
        isForward: Boolean
    ) {
        val transition = TransitionSet()
            .addTransition(Slide(Gravity.END).addTarget(if (isForward) newView else oldView))
            .addTransition(Fade(Fade.MODE_IN).addTarget(newView))
            .addTransition(Fade(Fade.MODE_OUT).addTarget(oldView))
            .setInterpolator(FastOutSlowInInterpolator())

        // Запускаем переход
        TransitionManager.beginDelayedTransition(parent, transition)
    }
}

internal object FadeTransition : DelayedTransition {
    override fun begin(
        parent: ViewGroup,
        newView: View,
        oldView: View,
        isForward: Boolean
    ) {
        // Создаём Fade-переход
        val transition = TransitionSet()
            .addTransition(Fade(Fade.MODE_IN).addTarget(newView))
            .addTransition(Fade(Fade.MODE_OUT).addTarget(oldView))
            .setInterpolator(FastOutSlowInInterpolator())
            .apply {
                duration = 400
                ordering = TransitionSet.ORDERING_TOGETHER
            }


        // Запускаем переход
        TransitionManager.beginDelayedTransition(parent, transition)
    }
}

internal object SlideFadeDelayedTransitionTo : DelayedTransition {
    override fun begin(
        parent: ViewGroup,
        newView: View,
        oldView: View,
        isForward: Boolean
    ) {
        val transition = TransitionSet()
            .addTransition(Slide(Gravity.END).addTarget(newView))
            .addTransition(Fade(Fade.MODE_IN).addTarget(newView))
            .addTransition(Fade(Fade.MODE_OUT).addTarget(oldView))
            .setInterpolator(FastOutSlowInInterpolator())

        // Запускаем переход
        TransitionManager.beginDelayedTransition(parent, transition)
    }
}

internal object SlideFadeDelayedTransitionBack : DelayedTransition {
    override fun begin(
        parent: ViewGroup,
        newView: View,
        oldView: View,
        isForward: Boolean
    ) {
        val transition = TransitionSet()
            .addTransition(Slide(Gravity.END).addTarget( oldView))
            .addTransition(Fade(Fade.MODE_IN).addTarget(newView))
            .addTransition(Fade(Fade.MODE_OUT).addTarget(oldView))
            .setInterpolator(FastOutSlowInInterpolator())

        // Запускаем переход
        TransitionManager.beginDelayedTransition(parent, transition)
    }
}
//
internal object ComplexBoundsTransformTransition : DelayedTransition {
    override fun begin(
        parent: ViewGroup,
        newView: View,
        oldView: View,
        isForward: Boolean
    ) {


        val transform = MaterialContainerTransform().apply {
            duration = 500L
            scrimColor = Color.TRANSPARENT
            fadeMode = MaterialContainerTransform.FADE_MODE_CROSS
            drawingViewId = parent.id
//            setAllContainerColors(
//                MaterialColors.getColor(
//                    oldView.context,
//                    com.google.android.material.R.attr.colorSurface,
//                    Color.RED
//                )
//
//            )
            excludeTarget(com.feature.feed.R.id.appBarLayout, true)
            excludeTarget(com.feature.feed.R.id.shimmerView, true)
        }

        TransitionManager.beginDelayedTransition(parent, transform)

    }
}
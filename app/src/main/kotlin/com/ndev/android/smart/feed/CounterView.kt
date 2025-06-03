package com.ndev.android.smart.feed


import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.arkivanov.decompose.value.subscribe
import com.arkivanov.essenty.lifecycle.doOnDestroy
import android.view.LayoutInflater
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext

/**
 * Расширение на ViewContext:
 * Inflates R.layout.counter, привязывает TextView и кнопки к CounterComponent,
 * подписывается на model и возвращает корневое View.
 */
@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.CounterView(component: CounterComponent): View {
    // 1. Инфлейтим разметку (но НЕ добавляем сразу в parent)
    // parent берётся из ViewContext (это container, переданный при создании DefaultViewContext)
    val layout = LayoutInflater.from(parent.context)
        .inflate(R.layout.counter, parent, false)

    // 2. Находим необходимые view
    val counterText: TextView = layout.findViewById(R.id.text_count)
    val btnInc: Button = layout.findViewById(R.id.button_increment)
    val btnDec: Button = layout.findViewById(R.id.button_decrement)

    // 3. Подписываемся на model: каждый раз, когда приходит новое CounterModel, обновляем текст
    component.model.subscribe(component.lifecycle) { data ->
        counterText.text = data.text
    }

    // 4. Клики по кнопкам: вызываем методы компонента
    btnInc.setOnClickListener { component.increment() }
    btnDec.setOnClickListener { component.decrement() }

    // 5. Убираем подписки при уничтожении lifecycle (не обязательно, subscribe сам отписывается)
    component.lifecycle.doOnDestroy {
        // если нужны какие-то дополнительные действия при destroy
    }

    // 6. Возвращаем корень инфлейтнутой разметки
    return layout
}

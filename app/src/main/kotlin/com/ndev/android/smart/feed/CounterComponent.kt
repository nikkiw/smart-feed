package com.ndev.android.smart.feed

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value

// Простая «модель» для View: хранит текст (например, "Текущее значение: 5")
data class CounterModel(val text: String)

// CounterComponent: exposes a Value<CounterModel> and increment/decrement methods
class CounterComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext {

    // Внутреннее состояние: текущее число
    private var currentCount: Int = 0

    // MutableValue для модели; извне будем читать через .model
    private val _model = MutableValue(CounterModel(text = currentCount.toString()))
    val model: Value<CounterModel> get() = _model

    // Увеличить счёт
    fun increment() {
        currentCount += 1
        _model.value = CounterModel(text = currentCount.toString())
    }

    // Уменьшить счёт
    fun decrement() {
        currentCount -= 1
        _model.value = CounterModel(text = currentCount.toString())
    }
}

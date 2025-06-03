package com.ndev.android.smart.feed


import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import android.widget.FrameLayout
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.DefaultViewContext
import com.arkivanov.essenty.lifecycle.essentyLifecycle

class MainActivity : AppCompatActivity() {

    // 1. Создаём корневой ComponentContext c Essenty Lifecycle
    private val componentContext by lazy {
        DefaultComponentContext(lifecycle = essentyLifecycle())
    }

    // 2. Наш CounterComponent
    private val counterComponent by lazy {
        CounterComponent(componentContext)
    }

    @OptIn(ExperimentalDecomposeApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // 3. Находим контейнер из XML
        val container: FrameLayout = findViewById(R.id.content)

        // 4. Обёртка Decompose: создаём ViewContext, прокидываем parent и lifecycle
        val viewContext = DefaultViewContext(
            parent = container,
            lifecycle = componentContext.lifecycle
        )

        // 5. Вызываем extension-функцию, чтобы получить готовый View
        val counterView: View = viewContext.CounterView(counterComponent)

        // 6. Добавляем получившийся View в контейнер
        container.addView(counterView)
    }
}

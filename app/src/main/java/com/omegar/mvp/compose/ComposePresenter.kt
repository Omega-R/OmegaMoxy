package com.omegar.mvp.compose

import com.omegar.mvp.MvpPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

class ComposePresenter: MvpPresenter<ComposeView>(), CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main.immediate + job

    init {
        launch {
            while (true) {
                delay(1.seconds)
                viewState.loading = !viewState.loading
                println("[Ant]: loading = ${viewState.loading}")
            }
        }
    }

    override fun attachView(view: ComposeView) {
        super.attachView(view)
        println("[Ant]: attachView ComposePresenter")
    }

    fun requestChangeColor() {
        viewState.randomColor()
    }

    override fun detachView(view: ComposeView) {
        super.detachView(view)
        println("[Ant]: detachView ComposePresenter")
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        println("[Ant]: destroy ComposePresenter")
    }


}
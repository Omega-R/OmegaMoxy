package com.omegar.mvp.compose

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
import com.omegar.mvp.MvpDelegate
import com.omegar.mvp.MvpDelegateHolder
import com.omegar.mvp.MvpView
import com.omegar.mvp.toKeyStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KProperty

abstract class MvpPresenterState<View: MvpView> {

    abstract val mvpDelegate: MvpDelegate<View>

    protected open class BaseStateMvpView<V: MvpView>: MvpDelegateHolder<V> {
        private val coroutineScope = CoroutineScope(Dispatchers.Default)

        @Suppress("UNCHECKED_CAST")
        override val mvpDelegate = MvpDelegate(this as V)

        protected class FlowDelegate<T>(private val sharedFlow: MutableSharedFlow<T>) {
            operator fun getValue(thisRef: BaseStateMvpView<*>, property: KProperty<*>): T = runBlocking { sharedFlow.first() }

            operator fun setValue(thisRef: BaseStateMvpView<*>, property: KProperty<*>, value: T) = with(thisRef.coroutineScope) {
                if (!sharedFlow.tryEmit(value)) {
                    launch {
                        sharedFlow.emit(value)
                    }
                }
            }
        }

    }
}

private const val KEY_STORE = "moxyKeyStore"

@Composable
fun <T: MvpPresenterState<*>> rememberMvp(calculation: @DisallowComposableCalls () -> T): T {
    return remember(calculation).apply {
        val savedStateRegistryOwner = LocalSavedStateRegistryOwner.current

        if (!mvpDelegate.isCreated()) {
            val savedInstanceState = savedStateRegistryOwner.savedStateRegistry.consumeRestoredStateForKey(KEY_STORE)
            mvpDelegate.onCreate(savedInstanceState?.toKeyStore())
        }

        DisposableEffect(Unit) {
            var saved = false
            mvpDelegate.onAttach()

            savedStateRegistryOwner.savedStateRegistry.registerSavedStateProvider(KEY_STORE) {
                val outState = Bundle()
                mvpDelegate.onSaveInstanceState(outState.toKeyStore())
                saved = true
                outState
            }

            onDispose {
                mvpDelegate.onDetach()
                if (!saved) {
                    mvpDelegate.onDestroyView()
                    mvpDelegate.onDestroy()
                }

                savedStateRegistryOwner.savedStateRegistry.unregisterSavedStateProvider(KEY_STORE)
            }
        }
    }
}
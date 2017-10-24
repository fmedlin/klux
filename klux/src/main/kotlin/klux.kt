class DispatchToken(val token: String) {
}

class Action(val actionType: String, var value: Any? = null) {
}

class Dispatcher {
    private var lastId = 1
    private var isDispatching = false
    private var pendingAction: Action? = null

    private val callbacks = mutableMapOf<DispatchToken, (Action) -> Unit>()
    private val pending   = mutableMapOf<DispatchToken, Boolean>()
    private val handled   = mutableMapOf<DispatchToken, Boolean>()

    fun register(callback: (Action) -> Unit): DispatchToken {
        val token = DispatchToken("ID_" + lastId++)
        callbacks[token] = callback
        return token
    }

    fun unregister(token: DispatchToken) {
        callbacks.remove(token)
    }

    fun waitFor(ids: Array<DispatchToken?>) {
        require(isDispatching)

        ids.forEach { token ->
            require(token != null)
            if (pending[token] == false) {
                invokeCallback(token!!)
            }
        }
    }

    fun dispatch(action: Action) {
        require(!isDispatching)

        startDispatching(action)
        callbacks.keys.forEach { token ->
            if (pending[token] == false) {
                invokeCallback(token)
            }
        }
        stopDispatching()
    }

    private fun startDispatching(action: Action) {
        callbacks.keys.forEach { token ->
            pending[token] = false
            handled[token] = false
        }
        pendingAction = action
        isDispatching = true
    }

    private fun invokeCallback(token: DispatchToken) {
        pending[token] = true
        callbacks[token]!!(pendingAction!!)
        handled[token] = true
    }

    private fun stopDispatching() {
        pendingAction = null
        isDispatching = false
    }
}

abstract class Store(val dispatcher: Dispatcher) {
    var changed = false

    val dispatchToken: DispatchToken? = dispatcher.register({
        invokeOnDispatch(it)
    })

    open val invokeOnDispatch: (Action) -> Unit = {
        changed = false
        onDispatch(it)
        if (changed) {
            // emitter.emit(changeEvent)
        }
    }

    fun onDispatch(action: Action) {
        throw IllegalAccessException("override required")
    }
}


abstract class FluxReduceStore<TState>(dispatcher: Dispatcher) : Store(dispatcher) {
    var state: TState = getInitialState()

    abstract fun getInitialState(): TState
    abstract fun reduce(state: TState, action: Action): TState

    override val invokeOnDispatch: (Action) -> Unit = {
        changed = false

        val startingState = state
        val endingState = reduce(startingState, it)

        if (startingState != endingState) {
            state = endingState
            // emitChange()
        }

        if (changed) {
            // emitter.emit(changeEvent)
        }
    }

}

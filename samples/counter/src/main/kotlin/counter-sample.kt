class CounterStore(dispatcher: Dispatcher) : FluxReduceStore<Int>(dispatcher) {
    override fun getInitialState(): Int {
        return 1
    }

    override fun reduce(state: Int, action: Action): Int {
        return when (action.actionType) {
            "add" -> state + action.value as Int
            "double" -> state * 2
            else -> state
        }
    }
}

fun main(args: Array<String>) {
    val counterDispatcher = Dispatcher()
    val counterStore = CounterStore(counterDispatcher)

    counterDispatcher.dispatch(Action("add", 1))
    counterDispatcher.dispatch(Action("double"))
    counterDispatcher.dispatch(Action("double"))
    counterDispatcher.dispatch(Action("nop"))

    println("counter: ${counterStore.state}")
}

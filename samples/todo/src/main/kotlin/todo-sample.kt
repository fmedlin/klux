data class TodoItem(
        val title: String,
        val completed: Boolean = false
)

class TodoStore(dispatcher: Dispatcher) : FluxReduceStore<List<TodoItem>>(dispatcher) {
    override fun getInitialState(): List<TodoItem> {
        return emptyList()
    }

    override fun reduce(state: List<TodoItem>, action: Action): List<TodoItem> {
        return when (action.actionType) {
            "add-todo" -> state + TodoItem(action.value as String)
            "complete" -> {
                state.toMutableList().apply {
                    val index = action.value as Int
                    this[index] = this[index].copy(completed = true)
                }.toList()
            }
            else -> throw IllegalStateException("unrecognized action: ${action.actionType}")
        }
    }
}

fun main(args: Array<String>) {
    val store = TodoStore(Dispatcher())
    store.dispatcher.dispatch(Action("add-todo", "Make my day"))
    store.dispatcher.dispatch(Action("add-todo", "Don't buy anything"))
    store.dispatcher.dispatch(Action("add-todo", "Take the last train to Clarksville"))
    store.dispatcher.dispatch(Action("complete", 1))

    println("\ncompleted items:")
    store.state.filter { it.completed }.forEach { println(it.title) }

    println("\nremaining items:")
    store.state.filter { !it.completed }.forEach { println(it.title) }
}

package ph.mcmod.ct.api

object MultiThreadsInit {
	private val TASKS = mutableListOf<() -> Unit>()
	
	operator fun plusAssign(task: () -> Unit) {
		TASKS += task
	}
	
	fun execute() {
		TASKS.stream().parallel().forEach { it() }
		TASKS.clear()
	}
}
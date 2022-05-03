package ph.mcmod.ct.api

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object Asynchronization {
	private val TASKS = mutableListOf<() -> Unit>()
	
	init {
		ServerLifecycleEvents.SERVER_STARTING.register {
			ServerTickEvents.START_SERVER_TICK.register {
				TASKS.forEach { it() }
				TASKS.clear()
			}
			ServerTickEvents.END_SERVER_TICK.register {
				TASKS.forEach { it() }
				TASKS.clear()
			}
		}
	}
	@JvmStatic
	operator fun plusAssign(task: () -> Unit) {
		TASKS += task
	}
}
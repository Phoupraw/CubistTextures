package ph.mcmod.ct

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents

object ClientMain {
	@JvmStatic fun init() {
		if (DUMP) ClientLifecycleEvents.CLIENT_STARTED.register { throw RuntimeException("资源包已dump，令游戏崩溃。如果要正常游戏，把DUMP设为false。") }
	}
}
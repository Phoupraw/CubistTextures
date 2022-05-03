package ph.mcmod.ct.api

import com.mojang.serialization.Lifecycle
import net.minecraft.util.registry.MutableRegistry
import net.minecraft.util.registry.Registry
import java.util.*

object MixinHelper {
	interface TrySetCampfire {
		fun trySetCampfire():Boolean
	}
	@JvmStatic
	fun <T> setRegistry(registry: Registry<T>, entry: T, newEntry: T): T {
		val rawId = registry.getRawId(entry)
//		val id = registry.getId(entry)!!
		val registryKey = registry.getKey(entry).orElseThrow()
		return (registry as MutableRegistry).replace(OptionalInt.of(rawId), registryKey, newEntry, Lifecycle.stable()).value()
	}
}
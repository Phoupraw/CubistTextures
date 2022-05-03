@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct

import net.devtech.arrp.api.RuntimeResourcePack
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.minecraft.item.ItemGroup
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ph.mcmod.ct.api.*
import ph.mcmod.ct.game.BeaconPillar
import ph.mcmod.ct.game.GlazedTerracottaLike
import ph.mcmod.ct.game.StoneSuite
import ph.mcmod.ct.game.WoodenSuite

const val NAMESPACE = "cubist_texture"
const val ARRP = false
const val DUMP = false
@JvmField
val LOGGER: Logger = LogManager.getLogger(NAMESPACE)
@JvmField
val RESOURCE_PACK: RuntimeResourcePack = RuntimeResourcePack.create(Identifier(NAMESPACE, "runtime"))
@JvmField
val ARRP_HELPER = ArrpHelper(RESOURCE_PACK)
val ITEM_GROUP: ItemGroup = FabricItemGroupBuilder.build(Identifier(NAMESPACE, "item_group")) { StoneSuite.CHISELED_CUTTING_STONE.fullBlock.asItem().defaultStack }.apply {
	runAtClient {
		if (ARRP)
			ARRP_HELPER.lang_zh_cn.itemGroup(Identifier(name), "立体化纹理") // FIXME: 2022/5/3 翻译文本显示的还是本地化键，而不是正确翻译。
	}
}

object Main {
	@JvmStatic fun init() {
		
		WoodenSuite.loadClass()
		StoneSuite.loadClass()
		BeaconPillar.loadClass()
		GlazedTerracottaLike.loadClass()
		
		Asynchronization.loadClass()
		MultiThreadsInit.execute()
		
		fun giveRecipes(server: MinecraftServer, players: Collection<ServerPlayerEntity>) {
			val recipesIds = server.recipeManager.keys().toList().filter { it.namespace == NAMESPACE }.toTypedArray()
			players.forEach { it.unlockRecipes(recipesIds) }
		}
		ServerPlayConnectionEvents.JOIN.register { playNetworkHandler, packetSender, server ->
			giveRecipes(server, listOf(playNetworkHandler.player))
		}
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register { server, resourceManager, boolean ->
			giveRecipes(server, server.playerManager.playerList)
		}
		
	}
}




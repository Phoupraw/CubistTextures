package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Blocks
import net.minecraft.block.PillarBlock
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import ph.mcmod.ct.ARRP
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.*

object BeaconPillar {
	const val PATH = "beacon_pillar"
	val BLOCK = PillarBlock(FabricBlockSettings.copyOf(Blocks.STONE).luminance(15)).register(PATH)
	val ITEM = BlockItem(BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	
	init {
		if (ARRP) {
			ARRP_HELPER.pack.addLootTable_itself(BLOCK)
			ARRP_HELPER.pack.addRecipe_stoneCutting(Items.BEACON, ITEM, 64)
			runAtClient {
				ARRP_HELPER.lang_zh_cn.blockRespect(BLOCK, "信标柱")
				ARRP_HELPER.add_model_item_block(BLOCK.id)
			}
		}
	}
}
@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.game

import net.devtech.arrp.json.models.JModel
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.BlockItem
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.addModel
import ph.mcmod.ct.api.id
import ph.mcmod.ct.api.register
import ph.mcmod.ct.api.runAtClient

object LitRedstoneLamp {
	const val PATH = "lit_redstone_lamp"
	val BLOCK = Block(FabricBlockSettings.copyOf(Blocks.REDSTONE_LAMP).luminance(15)).register(PATH)
	val ITEM = BlockItem(BLOCK,FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	
	init {
		runAtClient {
			ARRP_HELPER.lang_zh_cn.itemRespect(ITEM, "点亮的红石灯")
			ARRP_HELPER.add_blockState_single(BLOCK.id)
			ARRP_HELPER.pack.addModel(BLOCK, JModel.model("block/redstone_lamp_on"))
			ARRP_HELPER.add_model_item_block(PATH)
		}
	}
}
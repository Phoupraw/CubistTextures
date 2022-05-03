@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.game

import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.RedstoneTorchBlock
import net.minecraft.item.*
import net.minecraft.util.Identifier
import net.minecraft.util.collection.DefaultedList
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.id
import ph.mcmod.ct.api.register
import ph.mcmod.ct.api.runAtClient

object ExtinctRedstoneTorch {
	const val PATH = "extinct_redstone_torch"
	val ITEM = TItem(FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	
	init {
		ITEM.appendBlocks(Item.BLOCK_ITEMS,Items.REDSTONE_TORCH)
		runAtClient {
			ARRP_HELPER.lang_zh_cn.itemRespect(ITEM, "熄灭的红石火把")
			ARRP_HELPER.add_model_item_generated(ITEM.id, Identifier("block/redstone_torch_off"))
//			ARRP_HELPER.pack.addModel(ITEM, JModel.model("block/redstone_torch_off")/*.displayAsBlock()*/)
		}
	}
	
	class TItem(settings: Settings) : WallStandingBlockItem(Blocks.REDSTONE_TORCH,Blocks.REDSTONE_WALL_TORCH, settings) {
		override fun getPlacementState(context: ItemPlacementContext): BlockState? {
			return super.getPlacementState(context)?.with(RedstoneTorchBlock.LIT, false)
		}
		
		override fun getTranslationKey(): String {
			return orCreateTranslationKey
		}
		override fun appendStacks(group: ItemGroup, stacks: DefaultedList<ItemStack>) {
			if (isIn(group)) stacks += defaultStack
		}
	}
}
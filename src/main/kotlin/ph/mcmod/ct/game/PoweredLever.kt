@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.game

import net.devtech.arrp.json.models.JModel
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Blocks
import net.minecraft.block.LeverBlock
import net.minecraft.item.*
import net.minecraft.util.ActionResult
import net.minecraft.util.collection.DefaultedList
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.addModel
import ph.mcmod.ct.api.displayAsBlock
import ph.mcmod.ct.api.register
import ph.mcmod.ct.api.runAtClient

object PoweredLever {
	const val PATH = "powered_level"
	val ITEM = TItem(FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	
	init {
		ITEM.appendBlocks(Item.BLOCK_ITEMS, Items.LEVER)
		runAtClient {
			ARRP_HELPER.lang_zh_cn.itemRespect(ITEM, "激活的拉杆")
			ARRP_HELPER.pack.addModel(ITEM, JModel.model("block/lever_on").displayAsBlock())
		}
	}
	
class TItem(settings: Settings) : BlockItem(Blocks.LEVER, settings) {
	override fun place(context: ItemPlacementContext): ActionResult {
		return super.place(context).apply {
			if (isAccepted) (Blocks.LEVER as LeverBlock).togglePower(getPlacementState(context), context.world, context.blockPos)
		}
	}
	
	override fun getTranslationKey(): String {
		return orCreateTranslationKey
	}
	
	override fun appendStacks(group: ItemGroup, stacks: DefaultedList<ItemStack>) {
		if (isIn(group)) stacks += defaultStack
	}
}
}
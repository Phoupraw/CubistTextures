@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.api

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.Block
import net.minecraft.block.MapColor
import net.minecraft.block.SlabBlock
import net.minecraft.item.BlockItem
import ph.mcmod.ct.ARRP
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP

open class SlabStairsSuite(val path: String, chinese: String, val mapColor: MapColor, val luminance: Int, settingsFrom: Array<Block>) {
	val fullBlock = Block(settingsOf(settingsFrom[0])).registerItem(path)
	val slab = SlabBlock(settingsOf(settingsFrom[1])).registerItem("${path}_slab")
	val stairs = PublicStairsBlock(fullBlock.defaultState, settingsOf(settingsFrom[2])).registerItem("${path}_stairs")
	val verticalSlab = VerticalSlabBlock(FabricBlockSettings.copyOf(slab)).registerItem("${path}_vertical_slab")
	val coverplate = CoverplateBlock(FabricBlockSettings.copyOf(slab)).registerItem("${path}_${CoverplateBlock.PATH}")
	
	fun settingsOf(block: Block) = FabricBlockSettings.copyOf(block).mapColor(mapColor).luminance()
	fun FabricBlockSettings.luminance(): FabricBlockSettings {
		if (luminance > 0) luminance(luminance)
		else if (luminance < 0) luminance(-luminance).emissiveLighting(ALWAYS_TRUE)
		return this
	}
	
	init {
		MultiThreadsInit += {
			val fullBlockId = fullBlock.id
			if (ARRP) {
				ARRP_HELPER.pack.addLootTable_itself(fullBlockId)
				ARRP_HELPER.pack.addLootTable_slab(slab.id)
				ARRP_HELPER.pack.addLootTable_itself(stairs)
				ARRP_HELPER.pack.addLootTable_verticalSlab(verticalSlab.id)
				ARRP_HELPER.pack.addLootTable_coverplate(coverplate.id)
				ARRP_HELPER.pack.addRecipe_craftingShaped(slab, 6)("###")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(slab, 1)("#")("#", verticalSlab)()
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, slab, 2)
				ARRP_HELPER.pack.addRecipe_craftingShaped(stairs, 4)("#  ", "## ", "###")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, stairs, 1)
				ARRP_HELPER.pack.addRecipe_craftingShaped(verticalSlab, 6)("#", "#", "#")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(verticalSlab, 1)("#")("#", slab)()
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, verticalSlab, 2)
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, coverplate, 16)
				runAtClient {
					ARRP_HELPER.lang_zh_cn.blockRespect(fullBlock, chinese)
					ARRP_HELPER.add_blockState_single(fullBlockId)
					ARRP_HELPER.pack.addModel(fullBlock, ArrpHelper.jModel_cubeAll(fullBlockId.preBlock()))
					ARRP_HELPER.add_model_item_block(fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(slab, "${chinese}台阶")
					ARRP_HELPER.pack.addBlockStateAndModels_slab(slab.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(stairs, "${chinese}楼梯")
					ARRP_HELPER.pack.addBlockStateAndModels_stairs(stairs.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(verticalSlab, "${chinese}竖台阶")
					ARRP_HELPER.pack.addBlockStateAndModels_verticalSlab(verticalSlab.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(coverplate, "${chinese}盖板")
					ARRP_HELPER.pack.addBlockStateAndModels_coverplate(coverplate.id, fullBlockId)
				}
			}
		}
	}
	
	companion object {
		@JvmStatic fun <T : Block> T.registerItem(path: String): T {
			register(path)
			BlockItem(this, FabricItemSettings().group(ITEM_GROUP)).register(path)
			return this
		}
		
		@JvmStatic fun cut(ingredient: Block, result: Block) {
			ARRP_HELPER.pack.addRecipe_craftingShaped(result, 2)("##", "##")("#", ingredient)()
		}
	}
}


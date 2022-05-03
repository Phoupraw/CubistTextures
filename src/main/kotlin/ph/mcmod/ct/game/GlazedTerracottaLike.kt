package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.Blocks.JIGSAW
import net.minecraft.block.GlazedTerracottaBlock
import net.minecraft.block.MapColor
import net.minecraft.item.Items
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.api.*
import ph.mcmod.ct.api.SlabStairsSuite.Companion.registerItem
import ph.mcmod.ct.game.StoneSuite.Companion.JIGSAW_CROSS_STONE

class GlazedTerracottaLike(val path: String, chinese: String, val mapColor: MapColor, val luminance: Int = 0) {
	val fullBlock = GlazedTerracottaBlock(settingsOf(Blocks.STONE)).registerItem(path)
	fun settingsOf(block: Block) = FabricBlockSettings.copyOf(block).mapColor(mapColor).luminance()
	fun FabricBlockSettings.luminance(): FabricBlockSettings {
		if (luminance > 0) luminance(luminance)
		else if (luminance < 0) luminance(-luminance).emissiveLighting(ALWAYS_TRUE)
		return this
	}
	
	init {
		MultiThreadsInit += {
			val fullBlockId = fullBlock.id
			ARRP_HELPER.pack.addLootTable_itself(fullBlockId)
			runAtClient {
				ARRP_HELPER.lang_zh_cn.blockRespect(fullBlock, chinese)
				ARRP_HELPER.pack.addBlockStateAndModels_glazedTerracotta(fullBlockId)
			}
		}
	}
	
	@Suppress("unused")
	companion object {
		val JIGSAW_ARROW_STONE = GlazedTerracottaLike("jigsaw_arrow_stone", "拼图箭头石", JIGSAW.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 8)("###", "#@#", "###")("#", Blocks.MAGENTA_GLAZED_TERRACOTTA)("@", Items.BLACK_DYE)()
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, JIGSAW_CROSS_STONE.fullBlock, 1)
		}
		val JIGSAW_PIECE_STONE = GlazedTerracottaLike("jigsaw_piece_stone", "拼图碎片石", JIGSAW.defaultMapColor).apply { ARRP_HELPER.pack.addRecipe_stoneCutting(JIGSAW_ARROW_STONE.fullBlock, fullBlock, 1) }
		val JIGSAW_HALF_LINE_STONE = GlazedTerracottaLike("jigsaw_half_line_stone", "拼图半线石", JIGSAW.defaultMapColor).apply { ARRP_HELPER.pack.addRecipe_stoneCutting(JIGSAW_ARROW_STONE.fullBlock, fullBlock, 1) }
		val JIGSAW_LINE_STONE = GlazedTerracottaLike("jigsaw_line_stone", "拼图直线石", JIGSAW.defaultMapColor).apply { ARRP_HELPER.pack.addRecipe_stoneCutting(JIGSAW_ARROW_STONE.fullBlock, fullBlock, 1) }
		@JvmField val BLUE_COMMAND_ARROW_STONE = GlazedTerracottaLike("blue_command_arrow_stone", "蓝色命令箭头石", Blocks.REPEATING_COMMAND_BLOCK.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 8)("###", "#@#", "###")("#", Blocks.BLUE_GLAZED_TERRACOTTA)("@", Items.WHITE_DYE)()
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, StoneSuite.BLUE_COMMAND_RHOMBUS_STONE.fullBlock, 1)
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, StoneSuite.BLUE_COMMAND_SQUARE_STONE.fullBlock, 1)
		}
		@JvmField val BLUE_COMMAND_VECTOR_STONE = GlazedTerracottaLike("blue_command_vector_stone", "蓝色命令箭矢石", Blocks.REPEATING_COMMAND_BLOCK.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_stoneCutting(BLUE_COMMAND_ARROW_STONE.fullBlock, fullBlock, 1)
		}
		@JvmField val GREEN_COMMAND_ARROW_STONE = GlazedTerracottaLike("green_command_arrow_stone", "绿色命令箭头石", Blocks.CHAIN_COMMAND_BLOCK.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 8)("###", "#@#", "###")("#", Blocks.GREEN_GLAZED_TERRACOTTA)("@", Items.WHITE_DYE)()
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, StoneSuite.GREEN_COMMAND_RHOMBUS_STONE.fullBlock, 1)
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, StoneSuite.GREEN_COMMAND_SQUARE_STONE.fullBlock, 1)
		}
		@JvmField val GREEN_COMMAND_VECTOR_STONE = GlazedTerracottaLike("green_command_vector_stone", "绿色命令箭矢石", Blocks.CHAIN_COMMAND_BLOCK.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_stoneCutting(GREEN_COMMAND_ARROW_STONE.fullBlock, fullBlock, 1)
		}
		@JvmField val PINK_COMMAND_ARROW_STONE = GlazedTerracottaLike("pink_command_arrow_stone", "粉色命令箭头石", Blocks.COMMAND_BLOCK.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 8)("###", "#@#", "###")("#", Blocks.PINK_GLAZED_TERRACOTTA)("@", Items.WHITE_DYE)()
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, StoneSuite.PINK_COMMAND_RHOMBUS_STONE.fullBlock, 1)
			ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, StoneSuite.PINK_COMMAND_SQUARE_STONE.fullBlock, 1)
		}
		@JvmField val PINK_COMMAND_VECTOR_STONE = GlazedTerracottaLike("pink_command_vector_stone", "粉色命令箭矢石", Blocks.COMMAND_BLOCK.defaultMapColor).apply {
			ARRP_HELPER.pack.addRecipe_stoneCutting(PINK_COMMAND_ARROW_STONE.fullBlock, fullBlock, 1)
		}
	}
}
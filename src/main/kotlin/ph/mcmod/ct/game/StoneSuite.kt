package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.block.*
import net.minecraft.block.Blocks.*
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.EntityType
import net.minecraft.item.Items
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.tag.BlockTags
import net.minecraft.tag.ItemTags
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import ph.mcmod.ct.ARRP
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.api.*

class StoneSuite(path: String, chinese: String, mapColor: MapColor, luminance: Int = 0) : SlabStairsSuite(path, chinese, mapColor, luminance, arrayOf(STONE, STONE_SLAB, STONE_STAIRS)) {
	val wall = WallBlock(settingsOf(BRICK_WALL)).registerItem("${path}_wall")
	val iShape = IShapeBlock(FabricBlockSettings.copyOf(stairs)).registerItem("${path}_${IShapeBlock.PATH}")
	val button = PublicWoodenButtonBlock(settingsOf(STONE_BUTTON)).registerItem("${path}_button")
	val pressurePlate = PublicPressurePlateBlock(PressurePlateBlock.ActivationRule.MOBS, settingsOf(STONE_PRESSURE_PLATE)).registerItem("${path}_pressure_plate")
	val trapdoor = ManualTrapdoorBlock(FabricBlockSettings.of(Material.STONE, mapColor).luminance().requiresTool().strength(2.0f).sounds(BlockSoundGroup.STONE).nonOpaque().allowsSpawning { _, _, _, _ -> false }, false).registerItem("${path}_trapdoor")
	val verticalTrapdoor = VerticalTrapdoorBlock(FabricBlockSettings.copyOf(trapdoor), false).registerItem("${path}_${VerticalTrapdoorBlock.PATH}")
	
	init {
		MultiThreadsInit += {
			val fullBlockId = fullBlock.id
			if (ARRP) {
				ARRP_HELPER.pack.addLootTable_itself(wall)
				ARRP_HELPER.pack.addLootTable_itself(button)
				ARRP_HELPER.pack.addLootTable_itself(pressurePlate)
				ARRP_HELPER.pack.addLootTable_itself(iShape)
				ARRP_HELPER.pack.addLootTable_itself(trapdoor)
				ARRP_HELPER.pack.addLootTable_itself(verticalTrapdoor)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(fullBlockId)
				ARRP_HELPER.getTag(BlockTags.SLABS).add(slab.id)
				ARRP_HELPER.getTag(ItemTags.SLABS).add(slab.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(slab.id)
				ARRP_HELPER.getTag(BlockTags.STAIRS).add(stairs.id)
				ARRP_HELPER.getTag(ItemTags.STAIRS).add(stairs.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(stairs.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(verticalSlab.id)
				ARRP_HELPER.getTag(BlockTags.WALLS).add(wall.id)
				ARRP_HELPER.getTag(ItemTags.WALLS).add(wall.id)
				ARRP_HELPER.getTag(BlockTags.BUTTONS).add(button.id)
				ARRP_HELPER.getTag(ItemTags.BUTTONS).add(button.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(button.id)
				ARRP_HELPER.getTag(BlockTags.STONE_PRESSURE_PLATES).add(pressurePlate.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(pressurePlate.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(coverplate.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(iShape.id)
				ARRP_HELPER.getTag(BlockTags.TRAPDOORS).add(trapdoor.id)
				ARRP_HELPER.getTag(ItemTags.TRAPDOORS).add(trapdoor.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(trapdoor.id)
				ARRP_HELPER.getTag(BlockTags.PICKAXE_MINEABLE).add(verticalTrapdoor.id)
				ARRP_HELPER.pack.addRecipe_craftingShaped(wall, 6)("###", "###")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, wall, 1)
				ARRP_HELPER.pack.addRecipe_craftingShaped(button)("#")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(pressurePlate)("##")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(iShape, 7)("###", " # ", "###")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, iShape, 1)
				ARRP_HELPER.pack.addRecipe_craftingShaped(trapdoor, 2)("###")("#", pressurePlate)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(verticalTrapdoor, 2)("#", "#", "#")("#", pressurePlate)()
			}
			runAtClient {
				BlockRenderLayerMap.INSTANCE.putBlocks( RenderLayer.getCutout(),trapdoor,verticalTrapdoor)
				if (ARRP) {
					ARRP_HELPER.lang_zh_cn.blockRespect(wall, "${chinese}???")
					ARRP_HELPER.pack.addBlockStateAndModels_wall(wall.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(button, "${chinese}??????")
					ARRP_HELPER.pack.addBlockStateAndModels_button(button.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(pressurePlate, "${chinese}?????????")
					ARRP_HELPER.pack.addBlockStateAndModels_pressurePlate(pressurePlate.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(iShape, "??????${chinese}")
					ARRP_HELPER.pack.addBlockStateAndModels_iShape(iShape.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(trapdoor, "${chinese}?????????")
					ARRP_HELPER.pack.addBlockStateAndModels_generatedTrapdoor(trapdoor.id)
					ARRP_HELPER.lang_zh_cn.blockRespect(verticalTrapdoor, "${chinese}????????????")
					ARRP_HELPER.pack.addBlockStateAndModels_verticalTrapdoor(fullBlockId)
				}
			}
		}
	}
	@Suppress("unused")
	companion object {
		@JvmField val BLAST_FURNACE_STONE = StoneSuite("blast_furnace_stone", "?????????", BLAST_FURNACE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLAST_FURNACE, fullBlock, 16)
			}
		}
		@JvmField val BORDERED_BLAST_FURNACE_STONE = StoneSuite("bordered_blast_furnace_stone", "???????????????", BLAST_FURNACE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLAST_FURNACE, fullBlock, 16)
			}
		}
		@JvmField val DARK_BLAST_FURNACE_STONE = StoneSuite("dark_blast_furnace_stone", "????????????", BLAST_FURNACE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLAST_FURNACE, fullBlock, 16)
			}
		}
		@JvmField val CHISELED_DARK_BLAST_FURNACE_STONE = StoneSuite("chiseled_dark_blast_furnace_stone", "??????????????????", BLAST_FURNACE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLAST_FURNACE, fullBlock, 16)
			}
		}
		@JvmField val LIGHT_BLAST_FURNACE_STONE = StoneSuite("light_blast_furnace_stone", "????????????", BLAST_FURNACE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLAST_FURNACE, fullBlock, 16)
			}
		}
		@JvmField val BORDERED_LIGHT_BLAST_FURNACE_STONE = StoneSuite("bordered_light_blast_furnace_stone", "??????????????????", BLAST_FURNACE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLAST_FURNACE, fullBlock, 16)
			}
		}
		@JvmField val PV_STONE = StoneSuite("pv_stone", "?????????", DAYLIGHT_DETECTOR.defaultMapColor, 4).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(DAYLIGHT_DETECTOR, fullBlock, 9)
			}
		}
		@JvmField val BORDERED_PV_STONE = StoneSuite("bordered_pv_stone", "???????????????", DAYLIGHT_DETECTOR.defaultMapColor, 4).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(DAYLIGHT_DETECTOR, fullBlock, 9)
			}
		}
		@JvmField val DARK_PV_STONE = StoneSuite("dark_pv_stone", "????????????", DAYLIGHT_DETECTOR.defaultMapColor, 3).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(DAYLIGHT_DETECTOR, fullBlock, 9)
			}
		}
		@JvmField val BORDERED_DARK_PV_STONE = StoneSuite("bordered_dark_pv_stone", "??????????????????", DAYLIGHT_DETECTOR.defaultMapColor, 3).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(DAYLIGHT_DETECTOR, fullBlock, 9)
			}
		}
		@JvmField val MECHANICAL_STONE = StoneSuite("mechanical_stone", "?????????", DISPENSER.defaultMapColor).apply {
			if (ARRP) {
				stoneCutMechanical(fullBlock)
			}
		}
		@JvmField val BORDERED_MECHANICAL_STONE = StoneSuite("bordered_mechanical_stone", "???????????????", DISPENSER.defaultMapColor).apply {
			if (ARRP) {
				stoneCutMechanical(fullBlock)
			}
		}
		@JvmField val DARK_MECHANICAL_STONE = StoneSuite("dark_mechanical_stone", "????????????", DISPENSER.defaultMapColor).apply {
			if (ARRP) {
				stoneCutMechanical(fullBlock)
			}
		}
		@JvmField val BORDERED_DARK_MECHANICAL_STONE = StoneSuite("bordered_dark_mechanical_stone", "??????????????????", DISPENSER.defaultMapColor).apply {
			if (ARRP) {
				stoneCutMechanical(fullBlock)
			}
		}
		@JvmField val LIGHT_MECHANICAL_STONE = StoneSuite("light_mechanical_stone", "????????????", DISPENSER.defaultMapColor).apply {
			if (ARRP) {
				stoneCutMechanical(fullBlock)
			}
		}
		@JvmField val BORDERED_LIGHT_MECHANICAL_STONE = StoneSuite("bordered_light_mechanical_stone", "??????????????????", DISPENSER.defaultMapColor).apply {
			if (ARRP) {
				stoneCutMechanical(fullBlock)
			}
		}
		@JvmField val BORDERED_CRYING_OBSIDIAN = StoneSuite("bordered_crying_obsidian", "????????????????????????", RESPAWN_ANCHOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(RESPAWN_ANCHOR, fullBlock, 9)
			}
		}
		@JvmField val CUT_CRYING_OBSIDIAN = StoneSuite("cut_crying_obsidian", "????????????????????????", RESPAWN_ANCHOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(RESPAWN_ANCHOR, fullBlock, 9)
			}
		}
		@JvmField val CRYING_OBSIDIAN_LAMP = StoneSuite("crying_obsidian_lamp", "?????????????????????", RESPAWN_ANCHOR.defaultMapColor, 15).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(RESPAWN_ANCHOR, fullBlock, 9)
			}
		}
		@JvmField val BORDERED_END_STONE = StoneSuite("bordered_end_stone", "???????????????", END_STONE_BRICKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(END_STONE_BRICKS, fullBlock, 1)
			}
		}
		@JvmField val CHISELED_END_STONE = StoneSuite("chiseled_end_stone", "???????????????", END_STONE_BRICKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(END_STONE_BRICKS, fullBlock, 1)
			}
		}
		@JvmField val SMOKER_STONE = StoneSuite("smoker_stone", "?????????", SMOKER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMOKER, fullBlock, 12)
			}
		}
		@JvmField val BORDERED_SMOKER_STONE = StoneSuite("bordered_smoker_stone", "???????????????", SMOKER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMOKER, fullBlock, 12)
			}
		}
		@JvmField val OBSERVER_STONE = StoneSuite("observer_stone", "?????????", OBSERVER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(OBSERVER, fullBlock, 9)
			}
		}
		@JvmField val BORDERED_OBSERVER_STONE = StoneSuite("bordered_observer_stone", "???????????????", OBSERVER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(OBSERVER, fullBlock, 9)
			}
		}
		@JvmField val LIGHT_BORDERED_OBSERVER_STONE = StoneSuite("light_bordered_observer_stone", "??????????????????", OBSERVER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(OBSERVER, fullBlock, 9)
			}
		}
		@JvmField val SIMITHING_STONE = StoneSuite("smithing_stone", "?????????", SMITHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMITHING_TABLE, fullBlock, 6)
			}
		}
		@JvmField val BORDERED_SIMITHING_STONE = StoneSuite("bordered_smithing_stone", "???????????????", SMITHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMITHING_TABLE, fullBlock, 6)
			}
		}
		@JvmField val CUT_SIMITHING_STONE = StoneSuite("cut_smithing_stone", "???????????????", SMITHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMITHING_TABLE, fullBlock, 6)
				cut(SIMITHING_STONE.fullBlock, fullBlock)
			}
		}
		@JvmField val ENCHANTING_TABLE_STONE = StoneSuite("enchanting_table_stone", "????????????", CAMPFIRE.defaultMapColor, 8).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ENCHANTING_TABLE, fullBlock, 32)
			}
		}
		@JvmField val FLOWER_POT_STONE = StoneSuite("flower_pot_stone", "?????????", FLOWER_POT.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(FLOWER_POT, fullBlock, 1)
			}
		}
		@JvmField val STRUCTRURE_SAVE_STONE = StoneSuite("structure_save_stone", "???????????????", STRUCTURE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PURPLE_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val STRUCTRURE_LOAD_STONE = StoneSuite("structure_load_stone", "???????????????", STRUCTURE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PURPLE_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val STRUCTRURE_CORNER_STONE = StoneSuite("structure_corner_stone", "???????????????", STRUCTURE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PURPLE_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val STRUCTRURE_DATA_STONE = StoneSuite("structure_data_stone", "???????????????", STRUCTURE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PURPLE_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val STRUCTRURE_ITEM_STONE = StoneSuite("structure_item_stone", "???????????????", STRUCTURE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PURPLE_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val GOLD_AXOLOTL_STONE = StoneSuite("gold_axolotl_stone", "???????????????", YELLOW_GLAZED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(YELLOW_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val BLUE_AXOLOTL_STONE = StoneSuite("blue_axolotl_stone", "???????????????", BLUE_GLAZED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BLUE_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val CYAN_AXOLOTL_STONE = StoneSuite("cyan_axolotl_stone", "???????????????", CYAN_GLAZED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CYAN_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val BROWN_AXOLOTL_STONE = StoneSuite("brown_axolotl_stone", "???????????????", BROWN_GLAZED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BROWN_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val PINK_AXOLOTL_STONE = StoneSuite("pink_axolotl_stone", "???????????????", PINK_GLAZED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PINK_GLAZED_TERRACOTTA, fullBlock, 1)
			}
		}
		@JvmField val HOPPER_STONE = StoneSuite("hopper_stone", "?????????", HOPPER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(HOPPER, fullBlock, 13)
			}
		}
		@JvmField val CUT_HOPPER_STONE = StoneSuite("cut_hopper_stone", "???????????????", HOPPER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(HOPPER, fullBlock, 13)
				cut(HOPPER_STONE.fullBlock, fullBlock)
			}
		}
		@JvmField val ENDER_CHEST_LAMP = StoneSuite("ender_chest_lamp", "????????????", ENDER_CHEST.defaultMapColor, 15).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ENDER_CHEST, fullBlock, 9)
			}
		}
		@JvmField val ENDER_CHEST_STONE = StoneSuite("ender_chest_stone", "????????????", ENDER_CHEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ENDER_CHEST, fullBlock, 9)
			}
		}
		@JvmField val CUT_ENDER_CHEST_STONE = StoneSuite("cut_ender_chest_stone", "??????????????????", ENDER_CHEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ENDER_CHEST, fullBlock, 9)
				cut(ENDER_CHEST_STONE.fullBlock, fullBlock)
			}
		}
		@JvmField val CHISELED_ENDER_CHEST_STONE = StoneSuite("chiseled_ender_chest_stone", "??????????????????", ENDER_CHEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ENDER_CHEST, fullBlock, 9)
			}
		}
		@JvmField val BEACON_STONE = StoneSuite("beacon_stone", "?????????", BEACON.defaultMapColor, 15).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEACON, fullBlock, 64)
				ARRP_HELPER.pack.addRecipe_craftingShapeless(fullBlock, 6, Items.WHITE_DYE, Items.LIGHT_BLUE_DYE, Items.CYAN_DYE, SMOOTH_STONE, SMOOTH_STONE, SMOOTH_STONE, SMOOTH_STONE, SMOOTH_STONE, SMOOTH_STONE)
			}
		}
		@JvmField val BORDERED_BEACON_STONE = StoneSuite("bordered_beacon_stone", "???????????????", BEACON.defaultMapColor, 15).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEACON, fullBlock, 64)
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEACON_STONE.fullBlock, fullBlock, 1)
			}
		}
		@JvmField val SMOOTH_BEACON_STONE = StoneSuite("smooth_beacon_stone", "???????????????", BEACON.defaultMapColor, 15).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEACON, fullBlock, 64)
				smelt(BEACON_STONE.fullBlock, fullBlock)
			}
		}
		@JvmField val CUT_BEACON_STONE = StoneSuite("cut_beacon_stone", "???????????????", BEACON.defaultMapColor, 15).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEACON, fullBlock, 64)
				cut(BEACON_STONE.fullBlock, fullBlock)
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEACON_STONE.fullBlock, fullBlock, 1)
			}
		}
		@JvmField val BELL_STONE = StoneSuite("bell_stone", "??????", BELL.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BELL, fullBlock, 32)
				ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock)(" # ", "#@#", " # ")("#", Items.GOLD_NUGGET)("@", SMOOTH_STONE)()
			}
		}
		@JvmField val SMOOTH_BELL_STONE = StoneSuite("smooth_bell_stone", "????????????", BELL.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BELL, fullBlock, 32)
				smelt(BELL_STONE.fullBlock, fullBlock)
			}
		}
		@JvmField val CUTTING_STONE = StoneSuite("cutting_stone", "?????????", STONECUTTER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(STONECUTTER, fullBlock, 4)
			}
		}
		@JvmField val CUT_CUTTING_STONE = StoneSuite("cut_cutting_stone", "???????????????", STONECUTTER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(STONECUTTER, fullBlock, 4)
				cut(CUTTING_STONE.fullBlock, fullBlock)
			}
		}
		@JvmField val DARK_SMOOTH_STONE = StoneSuite("dark_smooth_stone", "????????????", STONECUTTER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(STONECUTTER, fullBlock, 4)
			}
		}
		@JvmField val CHISELED_CUTTING_STONE = StoneSuite("chiseled_cutting_stone", "???????????????", STONECUTTER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(STONECUTTER, fullBlock, 4)
			}
		}
		@JvmField val CUT_LODESTONE = StoneSuite("cut_lodestone", "????????????", LODESTONE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(LODESTONE, fullBlock, 64)
				ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 2)("##", "##")("#", SMOOTH_STONE_SLAB)()
			}
		}
		@JvmField val CHISELED_LODESTONE = StoneSuite("chiseled_lodestone", "????????????", LODESTONE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(LODESTONE, fullBlock, 64)
				ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 8)("###", "#@#", "###")("#", CHISELED_STONE_BRICKS)("@", Items.BLACK_DYE)()
			}
		}
		@JvmField val JIGSAW_CROSS_STONE = StoneSuite("jigsaw_cross_stone", "???????????????", JIGSAW.defaultMapColor)
		@JvmField val BLUE_COMMAND_RHOMBUS_STONE = StoneSuite("blue_command_rhombus_stone", "?????????????????????", REPEATING_COMMAND_BLOCK.defaultMapColor)
		@JvmField val BLUE_COMMAND_SQUARE_STONE = StoneSuite("blue_command_square_stone", "?????????????????????", REPEATING_COMMAND_BLOCK.defaultMapColor)
		@JvmField val GREEN_COMMAND_RHOMBUS_STONE = StoneSuite("green_command_rhombus_stone", "?????????????????????", CHAIN_COMMAND_BLOCK.defaultMapColor)
		@JvmField val GREEN_COMMAND_SQUARE_STONE = StoneSuite("green_command_square_stone", "?????????????????????", CHAIN_COMMAND_BLOCK.defaultMapColor)
		@JvmField val PINK_COMMAND_RHOMBUS_STONE = StoneSuite("pink_command_rhombus_stone", "?????????????????????", COMMAND_BLOCK.defaultMapColor)
		@JvmField val PINK_COMMAND_SQUARE_STONE = StoneSuite("pink_command_square_stone", "?????????????????????", COMMAND_BLOCK.defaultMapColor)
		@JvmField val DRAGON_EGG_STONE = StoneSuite("dragon_egg_stone", "?????????", DRAGON_EGG.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_craftingShapeless(fullBlock, 6, Items.PURPLE_DYE, Items.BLACK_DYE, Items.BLACK_DYE, DEEPSLATE, DEEPSLATE, DEEPSLATE, DEEPSLATE, DEEPSLATE, DEEPSLATE)
			}
		}
		
		fun stoneCutMechanical(fullBlock: Block) {
			ARRP_HELPER.pack.addRecipe_stoneCutting(DISPENSER, fullBlock, 9, fullBlock.id.preStoneCutting() + "/dispenser")
			ARRP_HELPER.pack.addRecipe_stoneCutting(DROPPER, fullBlock, 8, fullBlock.id.preStoneCutting() + "/dropper")
			ARRP_HELPER.pack.addRecipe_stoneCutting(PISTON, fullBlock, 9, fullBlock.id.preStoneCutting() + "/piston")
			ARRP_HELPER.pack.addRecipe_stoneCutting(STICKY_PISTON, fullBlock, 10, fullBlock.id.preStoneCutting() + "/sticky_piston")
		}
		
		fun smelt(ingredient: Block, result: Block) {
			ARRP_HELPER.pack.addRecipe_smelting(ingredient, result, 0.1)
			ARRP_HELPER.pack.addRecipe_blasting(ingredient, result, 0.1)
		}
	}
}
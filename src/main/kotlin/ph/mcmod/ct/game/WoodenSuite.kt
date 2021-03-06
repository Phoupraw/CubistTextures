@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.minecraft.block.Blocks.*
import net.minecraft.block.FenceBlock
import net.minecraft.block.FenceGateBlock
import net.minecraft.block.MapColor
import net.minecraft.block.PressurePlateBlock
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.tag.BlockTags
import net.minecraft.tag.ItemTags
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import ph.mcmod.ct.ARRP
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.api.*

class WoodenSuite(path: String, chinese: String, mapColor: MapColor, luminance: Int = 0) : SlabStairsSuite(path, chinese, mapColor, luminance, arrayOf(OAK_PLANKS, OAK_SLAB, OAK_STAIRS)) {
	val fence = FenceBlock(settingsOf(OAK_FENCE)).registerItem("${path}_fence")
	val fenceGate = FenceGateBlock(settingsOf(OAK_FENCE_GATE)).registerItem("${path}_fence_gate")
	val button = PublicWoodenButtonBlock(settingsOf(OAK_BUTTON)).registerItem("${path}_button")
	val pressurePlate = PublicPressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, settingsOf(OAK_PRESSURE_PLATE)).registerItem("${path}_pressure_plate")
	val trapdoor = ManualTrapdoorBlock(settingsOf(OAK_TRAPDOOR), true).registerItem("${path}_trapdoor")
	val verticalTrapdoor = VerticalTrapdoorBlock(FabricBlockSettings.copyOf(trapdoor), true).registerItem("${path}_${VerticalTrapdoorBlock.PATH}")
	
	init {
		MultiThreadsInit += {
			val fullBlockId = fullBlock.id
			if (ARRP) {
				ARRP_HELPER.pack.addLootTable_itself(fence)
				ARRP_HELPER.pack.addLootTable_itself(fenceGate)
				ARRP_HELPER.pack.addLootTable_itself(button)
				ARRP_HELPER.pack.addLootTable_itself(pressurePlate)
				ARRP_HELPER.pack.addLootTable_itself(trapdoor)
				ARRP_HELPER.pack.addLootTable_itself(verticalTrapdoor)
				ARRP_HELPER.getTag(BlockTags.PLANKS).add(fullBlockId)
				ARRP_HELPER.getTag(ItemTags.PLANKS).add(fullBlockId)
				ARRP_HELPER.getTag(BlockTags.WOODEN_SLABS).add(slab.id)
				ARRP_HELPER.getTag(ItemTags.WOODEN_SLABS).add(slab.id)
				ARRP_HELPER.getTag(BlockTags.WOODEN_STAIRS).add(stairs.id)
				ARRP_HELPER.getTag(ItemTags.WOODEN_STAIRS).add(stairs.id)
				ARRP_HELPER.getTag(BlockTags.AXE_MINEABLE).add(verticalSlab.id)
				ARRP_HELPER.getTag(BlockTags.AXE_MINEABLE).add(coverplate.id)
				ARRP_HELPER.getTag(BlockTags.WOODEN_FENCES).add(fence.id)
				ARRP_HELPER.getTag(ItemTags.WOODEN_FENCES).add(fence.id)
				ARRP_HELPER.getTag(BlockTags.AXE_MINEABLE).add(fenceGate.id)
				ARRP_HELPER.getTag(BlockTags.WOODEN_BUTTONS).add(button.id)
				ARRP_HELPER.getTag(ItemTags.WOODEN_BUTTONS).add(button.id)
				ARRP_HELPER.getTag(BlockTags.WOODEN_PRESSURE_PLATES).add(pressurePlate.id)
				ARRP_HELPER.getTag(ItemTags.WOODEN_PRESSURE_PLATES).add(pressurePlate.id)
				ARRP_HELPER.getTag(BlockTags.WOODEN_TRAPDOORS).add(trapdoor.id)
				ARRP_HELPER.getTag(ItemTags.WOODEN_TRAPDOORS).add(trapdoor.id)
				ARRP_HELPER.getTag(BlockTags.AXE_MINEABLE).add(verticalTrapdoor.id)
				ARRP_HELPER.pack.addRecipe_craftingShaped(fence, 3)("#@#", "#@#")("#", fullBlock)("@", Items.STICK)()
				ARRP_HELPER.pack.addRecipe_stoneCutting(fullBlock, fence, 1)
				ARRP_HELPER.pack.addRecipe_craftingShaped(fenceGate)("@#@", "@#@")("#", fullBlock)("@", Items.STICK)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(button)("#")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(pressurePlate)("##")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(trapdoor, 2)("###", "###")("#", fullBlock)()
				ARRP_HELPER.pack.addRecipe_craftingShaped(verticalTrapdoor, 2)("#", "#", "#")("#", pressurePlate)()
			}
			runAtClient {
				BlockRenderLayerMap.INSTANCE.putBlocks( RenderLayer.getCutout(),trapdoor,verticalTrapdoor)
				if (ARRP) {
					ARRP_HELPER.lang_zh_cn.blockRespect(fence, "${chinese}??????")
					ARRP_HELPER.pack.addBlockStateAndModels_fence(fence.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(fenceGate, "${chinese}?????????")
					ARRP_HELPER.pack.addBlockStateAndModels_fenceGate(fenceGate.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(button, "${chinese}??????")
					ARRP_HELPER.pack.addBlockStateAndModels_button(button.id, fullBlockId)
					ARRP_HELPER.lang_zh_cn.blockRespect(pressurePlate, "${chinese}?????????")
					ARRP_HELPER.pack.addBlockStateAndModels_pressurePlate(pressurePlate.id, fullBlockId)
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
		@JvmField val ITEM_TAG_CAMPFIRE: TagKey<Item> = TagKey.of(Registry.ITEM.key, Identifier("campfires")).apply {
			if (ARRP) {
				ARRP_HELPER.getTag(this).add(CAMPFIRE.id).add(SOUL_CAMPFIRE.id)
			}
		}
		@JvmField val CAMPFIRE_WOOD = WoodenSuite("campfire_wood", "?????????", CAMPFIRE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ITEM_TAG_CAMPFIRE, fullBlock, 12)
			}
		}
		@JvmField val SMITHING_WOOD = WoodenSuite("smithing_wood", "?????????", SMITHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMITHING_TABLE, fullBlock, 4)
			}
		}
		@JvmField val BORDERED_SMITHING_WOOD = WoodenSuite("bordered_smithing_wood", "???????????????", SMITHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMITHING_TABLE, fullBlock, 4)
			}
		}
		@JvmField val CUT_SMITHING_WOOD = WoodenSuite("cut_smithing_wood", "???????????????", SMITHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SMITHING_TABLE, fullBlock, 4)
				cut(SMITHING_WOOD.fullBlock, fullBlock)
			}
		}
		@JvmField val BEE_NEST_WOOD = WoodenSuite("bee_nest_wood", "?????????", BEE_NEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEE_NEST, fullBlock, 6)
			}
		}
		@JvmField val WAXED_BEE_NEST_WOOD = WoodenSuite("waxed_bee_nest_wood", "???????????????", BEE_NEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_craftingShaped(fullBlock, 8)("###", "#@#", "###")("#", BEE_NEST_WOOD.fullBlock)("@", Items.HONEYCOMB)()
			}
		}
		@JvmField val CHISELED_BEE_NEST_WOOD = WoodenSuite("chiseled_bee_nest_wood", "???????????????", BEE_NEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEE_NEST, fullBlock, 6)
			}
		}
		@JvmField val BEEHIVE_WOOD = WoodenSuite("beehive_wood", "?????????", BEEHIVE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEEHIVE, fullBlock, 6)
			}
		}
		@JvmField val BORDERED_BEEHIVE_WOOD = WoodenSuite("bordered_beehive_wood", "???????????????", BEEHIVE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEEHIVE, fullBlock, 6)
			}
		}
		@JvmField val CHISELED_BEEHIVE_WOOD = WoodenSuite("chiseled_beehive_wood", "???????????????", BEEHIVE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEEHIVE, fullBlock, 6)
			}
		}
		@JvmField val CARVED_BEEHIVE_WOOD = WoodenSuite("carved_beehive_wood", "???????????????", BEEHIVE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BEEHIVE, fullBlock, 6)
			}
		}
		@JvmField val OAK_BOAT_WOOD = WoodenSuite("oak_boat_wood", "????????????", OAK_PLANKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.OAK_BOAT, fullBlock, 5)
			}
		}
		@JvmField val SPRUCE_BOAT_WOOD = WoodenSuite("spruce_boat_wood", "????????????", SPRUCE_PLANKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.SPRUCE_BOAT, fullBlock, 5)
			}
		}
		@JvmField val BIRCH_BOAT_WOOD = WoodenSuite("birch_boat_wood", "????????????", BIRCH_PLANKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.BIRCH_BOAT, fullBlock, 5)
			}
		}
		@JvmField val JUNGLE_BOAT_WOOD = WoodenSuite("jungle_boat_wood", "????????????", JUNGLE_PLANKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.JUNGLE_BOAT, fullBlock, 5)
			}
		}
		@JvmField val ACACIA_BOAT_WOOD = WoodenSuite("acacia_boat_wood", "???????????????", ACACIA_PLANKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.ACACIA_BOAT, fullBlock, 5)
			}
		}
		@JvmField val DAKR_OAK_BOAT_WOOD = WoodenSuite("dark_oak_boat_wood", "??????????????????", DARK_OAK_PLANKS.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.DARK_OAK_BOAT, fullBlock, 5)
			}
		}
		@JvmField val OAK_DOOR_WOOD = WoodenSuite("oak_door_wood", "????????????", OAK_DOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(OAK_DOOR, fullBlock, 2)
			}
		}
		@JvmField val SPRUCE_TRAPDOOR_WOOD = WoodenSuite("spruce_trapdoor_wood", "??????????????????", SPRUCE_DOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(SPRUCE_TRAPDOOR, fullBlock, 3)
			}
		}
		@JvmField val BIRCH_TRAPDOOR_WOOD = WoodenSuite("birch_trapdoor_wood", "??????????????????", BIRCH_TRAPDOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BIRCH_TRAPDOOR, fullBlock, 3)
			}
		}
		@JvmField val JUNGLE_DOOR_WOOD = WoodenSuite("jungle_door_wood", "????????????", JUNGLE_DOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(JUNGLE_DOOR, fullBlock, 2)
			}
		}
		@JvmField val DARK_OAK_TRAPDOOR_WOOD = WoodenSuite("dark_oak_trapdoor_wood", "????????????????????????", DARK_OAK_TRAPDOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(DARK_OAK_TRAPDOOR, fullBlock, 3)
			}
		}
		@JvmField val CRIMSON_DOOR_WOOD = WoodenSuite("crimson_door_wood", "????????????", CRIMSON_DOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CRIMSON_DOOR, fullBlock, 2)
			}
		}
		@JvmField val WARPED_DOOR_WOOD = WoodenSuite("warped_door_wood", "????????????", WARPED_DOOR.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(WARPED_DOOR, fullBlock, 2)
			}
		}
		@JvmField val CHEST_WOOD = WoodenSuite("chest_wood", "??????", CHEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val BORDERED_CHEST_WOOD = WoodenSuite("bordered_chest_wood", "????????????", CHEST.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val CHRISTMAS_CHEST_WOOD = WoodenSuite("christmas_chest_wood", "????????????", RED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val BORDERED_CHRISTMAS_CHEST_WOOD = WoodenSuite("bordered_christmas_chest_wood", "??????????????????", RED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val CHISELED_CHRISTMAS_CHEST_WOOD = WoodenSuite("chiseled_christmas_chest_wood", "??????????????????", RED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val CUT_CHRISTMAS_CHEST_WOOD = WoodenSuite("cut_christmas_chest_wood", "??????????????????", RED_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
				cut(CHRISTMAS_CHEST_WOOD.fullBlock, fullBlock)
			}
		}
		@JvmField val CHRISTMAS_EVE_CHEST_WOOD = WoodenSuite("christmas_eve_chest_wood", "????????????", GREEN_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val BORDERED_CHRISTMAS_EVE_CHEST_WOOD = WoodenSuite("bordered_christmas_eve_chest_wood", "??????????????????", GREEN_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val CHISELED_CHRISTMAS_EVE_CHEST_WOOD = WoodenSuite("chiseled_christmas_eve_chest_wood", "??????????????????", GREEN_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
			}
		}
		@JvmField val CUT_CHRISTMAS_EVE_CHEST_WOOD = WoodenSuite("cut_christmas_eve_chest_wood", "??????????????????", GREEN_TERRACOTTA.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CHEST, fullBlock, 8)
				cut(CHRISTMAS_EVE_CHEST_WOOD.fullBlock, fullBlock)
			}
		}
		@JvmField val LOOM_WOOD = WoodenSuite("loom_wood", "????????????", LOOM.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(LOOM, fullBlock, 4)
			}
		}
		@JvmField val BORDERED_LOOM_WOOD = WoodenSuite("bordered_loom_wood", "??????????????????", LOOM.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(LOOM, fullBlock, 4)
			}
		}
		@JvmField val LEATHER_WOOD = WoodenSuite("leather_wood", "?????????", LOOM.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(LOOM, fullBlock, 4)
			}
		}
		@JvmField val NOTE_WOOD = WoodenSuite("note_wood", "?????????", NOTE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(NOTE_BLOCK, fullBlock, 8)
			}
		}
		@JvmField val BORDERED_NOTE_WOOD = WoodenSuite("bordered_note_wood", "???????????????", NOTE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(NOTE_BLOCK, fullBlock, 8)
			}
		}
		@JvmField val CHISELED_NOTE_WOOD = WoodenSuite("chiseled_note_wood", "???????????????", NOTE_BLOCK.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(NOTE_BLOCK, fullBlock, 8)
				cut(NOTE_WOOD.fullBlock, fullBlock)
			}
		}
		@JvmField val BARREL_WOOD = WoodenSuite("barrel_wood", "??????", BARREL.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BARREL, fullBlock, 7)
			}
		}
		@JvmField val BORDERED_BARREL_WOOD = WoodenSuite("bordered_barrel_wood", "????????????", BARREL.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BARREL, fullBlock, 7)
			}
		}
		@JvmField val CHISELED_BARREL_WOOD = WoodenSuite("chiseled_barrel_wood", "????????????", BARREL.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BARREL, fullBlock, 7)
			}
		}
		@JvmField val CUT_BARREL_WOOD = WoodenSuite("cut_barrel_wood", "????????????", BARREL.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(BARREL, fullBlock, 7)
				cut(BARREL_WOOD.fullBlock, fullBlock)
			}
		}
		@JvmField val COMPOSTER_WOOD = WoodenSuite("composter_wood", "?????????", COMPOSTER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(COMPOSTER, fullBlock, 3)
			}
		}
		@JvmField val CUT_COMPOSTER_WOOD = WoodenSuite("cut_composter_wood", "???????????????", COMPOSTER.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(COMPOSTER, fullBlock, 3)
				cut(COMPOSTER_WOOD.fullBlock, fullBlock)
			}
		}
		@JvmField val ENCHANTING_TABLE_WOOD = WoodenSuite("enchanting_table_wood", "????????????", ENCHANTING_TABLE.defaultMapColor, 8).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(ENCHANTING_TABLE, fullBlock, 32)
			}
		}
		@JvmField val GLOW_ITEM_FRAME_WOOD = WoodenSuite("glow_item_frame_wood", "????????????????????????", GLOWSTONE.defaultMapColor, -1).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(Items.GLOW_ITEM_FRAME, fullBlock, 4)
			}
		}
		@JvmField val PISTON_WOOD = WoodenSuite("piston_wood", "?????????", PISTON.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(PISTON, fullBlock, 9)
			}
		}
		@JvmField val PV_WOOD = WoodenSuite("pv_wood", "?????????", DAYLIGHT_DETECTOR.defaultMapColor, 4).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(DAYLIGHT_DETECTOR, fullBlock, 9)
			}
		}
		@JvmField val CHISELED_BIRCH_WOOD = WoodenSuite("chiseled_birch_wood", "???????????????", FLETCHING_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(FLETCHING_TABLE, fullBlock, 4)
			}
		}
		@JvmField val CUT_DARK_OAK_WOOD = WoodenSuite("cut_dark_oak_wood", "??????????????????", CARTOGRAPHY_TABLE.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(CARTOGRAPHY_TABLE, fullBlock, 4)
			}
		}
		@JvmField val CHISELED_LECTERN_WOOD = WoodenSuite("chiseled_lectern_wood", "???????????????", LECTERN.defaultMapColor).apply {
			if (ARRP) {
				ARRP_HELPER.pack.addRecipe_stoneCutting(LECTERN, fullBlock, 11)
			}
		}
	}
}


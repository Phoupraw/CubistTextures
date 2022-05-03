package ph.mcmod.ct.game

import net.devtech.arrp.json.blockstate.JBlockModel
import net.devtech.arrp.json.blockstate.JState
import net.devtech.arrp.json.blockstate.JVariant
import net.devtech.arrp.json.models.JModel
import net.devtech.arrp.json.models.JTextures
import net.devtech.arrp.json.recipe.*
import net.devtech.arrp.json.tags.JTag
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.client.render.RenderLayer
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.item.WallStandingBlockItem
import net.minecraft.particle.ParticleTypes
import net.minecraft.resource.ResourceType
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.tag.BlockTags.PICKAXE_MINEABLE
import net.minecraft.util.Identifier
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.NAMESPACE
import ph.mcmod.ct.RESOURCE_PACK
import ph.mcmod.ct.api.*

object CoralLight {
	
	@JvmField
	val TORCH_BLOCK_SETTINGS: FabricBlockSettings = FabricBlockSettings.copyOf(Blocks.TORCH).luminance(14)
	@JvmField
	val WALL_TORCH_BLOCK_SETTINGS: FabricBlockSettings = FabricBlockSettings.copyOf(Blocks.WALL_TORCH).luminance(14)
	@JvmField
	val CAMPFIRE_BLOCK_SETTINGS: FabricBlockSettings = FabricBlockSettings.of(Material.STONE, MapColor.BLUE).strength(2F).sounds(BlockSoundGroup.CORAL).nonOpaque().luminance(WaterloggedS.CampfireBlock.createLightLevelFromLitBlockState(15))
	@JvmField
	val CAMPFIRE_BLOCKS = mutableListOf<WaterloggedS.CampfireBlock>()
	//	@JvmField
//	val
	init {
		registerS("tube", Items.TUBE_CORAL, Items.TUBE_CORAL_BLOCK, "管")
		registerS("brain", Items.BRAIN_CORAL, Items.BRAIN_CORAL_BLOCK, "脑纹")
		registerS("bubble", Items.BUBBLE_CORAL, Items.BUBBLE_CORAL_BLOCK, "气泡")
		registerS("fire", Items.FIRE_CORAL, Items.FIRE_CORAL_BLOCK, "火")
		registerS("horn", Items.HORN_CORAL, Items.HORN_CORAL_BLOCK, "鹿角")
		RESOURCE_PACK.addTag(PICKAXE_MINEABLE.id.pre("blocks/"), JTag.tag().apply {
			for (block in CAMPFIRE_BLOCKS) {
				this.add(block.id)
			}
		})
//		(BlockEntityType.CAMPFIRE as MixinHelper.TrySetCampfire).trySetCampfire()
	}
	
	fun registerS(prefix: String, coral: Item, coralBlock: Item, translationPrefix: String) {
//		fun printJson(any: Any) {
//			println(RuntimeResourcePackImpl.GSON.toJson(any))
//		}
		
		val torchId = Identifier(NAMESPACE, "${prefix}_torch")
		val wallTorchId = Identifier(NAMESPACE, "${prefix}_wall_torch")
		val campfireId = Identifier(NAMESPACE, "${prefix}_campfire")
		
		val torchBlock = WaterloggedS.FloorTorchBlock(TORCH_BLOCK_SETTINGS, ParticleTypes.FLAME).register(torchId.path)
		val wallTorchBlock = WaterloggedS.WallTorchBlock(WALL_TORCH_BLOCK_SETTINGS, ParticleTypes.FLAME).register(wallTorchId.path)
		val campfireBlock = WaterloggedS.CampfireBlock(true, 1, CAMPFIRE_BLOCK_SETTINGS).register(campfireId.path)
		CAMPFIRE_BLOCKS += campfireBlock
		
		val torchItem = WallStandingBlockItem(torchBlock, wallTorchBlock, WaterloggedS.TORCH_ITEM_SETTINGS).register(torchId.path)
		val campfireItem = BlockItem(campfireBlock, WaterloggedS.CAMPFIRE_ITEM_SETTINGS).register(campfireId.path)
		
		RESOURCE_PACK.addRecipe(
		  torchId.pre("crafting_shapeless/"),
		  JRecipe.shapeless(
			JIngredients.ingredients()
			  .add(JIngredient.ingredient().item(coral))
			  .apply {
				  for (i in 0..7) {
					  add(JIngredient.ingredient().item(Items.TORCH))
				  }
			  },
			JResult.itemStack(torchItem, 8)
		  )
		)
		RESOURCE_PACK.addRecipe(
		  campfireId.pre("crafting_shaped/"),
		  JRecipe.shaped(
			JPattern.pattern(
			  " T ",
			  "TCT",
			  "BBB"
			),
			JKeys.keys()
			  .key("T", JIngredient.ingredient().item(torchItem))
			  .key("C", JIngredient.ingredient().item(coral))
//			  .key("L", JIngredient.ingredient().tag(ItemTags.LOGS_THAT_BURN.id.toString()))
			  .key("B", JIngredient.ingredient().item(coralBlock)),
			JResult.item(campfireItem)
		  )
		)
		
		ARRP_HELPER.add_lootTable_block_dropItself(torchId.path)
		ARRP_HELPER.add_lootTable_block_dropSingle(wallTorchId.path, torchId)
		RESOURCE_PACK.addResource(ResourceType.SERVER_DATA, Identifier(NAMESPACE, "loot_tables/blocks/${campfireId.path}.json"), """{
	"type": "block",
	"pools": [
		{
			"rolls": 1,
			"entries": [
				{
					"type": "item",
					"name": "$campfireId",
					"conditions": [
						{
							"condition": "match_tool",
							"predicate": {
								"enchantments": [
									{
										"enchantment": "silk_touch",
										"level": {
											"min": 1
										}
									}
								]
							}
						}
					]
				}
			]
		},
		{
			"rolls": 1,
			"entries": [
				{
					"type": "item",
					"name": "${coral.id}",
					"conditions": [
						{
							"condition": "inverted",
							"term": {
								"condition": "match_tool",
								"predicate": {
									"enchantments": [
										{
											"enchantment": "silk_touch",
											"level": {
												"min": 1
											}
										}
									]
								}
							}
						}
					]
				}
			]
		},
		{
			"rolls": 1,
			"entries": [
				{
					"type": "item",
					"name": "${coralBlock.id}",
					"conditions": [
						{
							"condition": "inverted",
							"term": {
								"condition": "match_tool",
								"predicate": {
									"enchantments": [
										{
											"enchantment": "silk_touch",
											"level": {
												"min": 1
											}
										}
									]
								}
							}
						}
					]
				}
			]
		}
	]
}""".toByteArray())
		
		ARRP_HELPER.tag_block_pickaxe_mineable.add(campfireId)
		SynopsisTooltip.addSynopsis(torchItem, "亮度14；可放置在水下")
		SynopsisTooltip.addSynopsis(campfireItem, "亮度15；可在水下燃烧")
		
		runAtClient {
			BlockRenderLayerMapImpl.INSTANCE.putBlocks(RenderLayer.getCutout(), campfireBlock)
//			val blockTorchId = torchId.pre("block/")
//			val blockWallTorchId = wallTorchId.pre("block/")
			val blockCampfireId = campfireId.pre("block/")
			WaterloggedS.FloorTorchBlock.addBlockState(torchId)
			WaterloggedS.WallTorchBlock.addBlockState(wallTorchId)
			RESOURCE_PACK.addBlockState(JState.state().add(JVariant().apply {
				for (facing2y in arrayOf("south" to 0, "west" to 90, "north" to 180, "east" to 270)) {
					for (lit2suffix in arrayOf("true" to "", "false" to "_off")) {
						put("facing=${facing2y.first},lit=${lit2suffix.first}", JBlockModel(blockCampfireId + lit2suffix.second).y(facing2y.second))
					}
				}
			}), campfireId)
			WaterloggedS.FloorTorchBlock.addBlockModel(torchBlock)
			WaterloggedS.WallTorchBlock.addBlockModel(wallTorchBlock, torchId)
			RESOURCE_PACK.addModel(JModel().parent("cubist_texture:block/coral_campfire").textures(JTextures()
			  .`var`("coral", coral.id.pre("block/").toString())
			  .`var`("coral_block", coralBlock.id.pre("block/").toString())), blockCampfireId)
			RESOURCE_PACK.addModel(JModel().parent("cubist_texture:block/coral_campfire_off").textures(JTextures()
			  .`var`("coral", coral.id.pre("block/").toString())
			  .`var`("coral_block", coralBlock.id.pre("block/").toString())), blockCampfireId + "_off")

//			val torchIdItem = torchId.pre("item/")
			val campfireIdItem = campfireId.pre("item/")
			WaterloggedS.FloorTorchBlock.addItemModel(torchId)
			RESOURCE_PACK.addModel(JModel().parent("item/generated").textures(JTextures().layer0(campfireIdItem.toString())), campfireIdItem)
			
			ARRP_HELPER.lang_zh_cn
			  .blockRespect(torchBlock, "${translationPrefix}珊瑚火把")
			  .blockRespect(campfireBlock, "${translationPrefix}珊瑚营火")
			
		}
	}
}
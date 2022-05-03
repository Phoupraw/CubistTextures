@file:Suppress("DEPRECATION")

package ph.mcmod.ct.api

import com.google.gson.JsonObject
import net.devtech.arrp.api.RRPCallback
import net.devtech.arrp.api.RuntimeResourcePack
import net.devtech.arrp.impl.RuntimeResourcePackImpl
import net.devtech.arrp.json.blockstate.JBlockModel
import net.devtech.arrp.json.blockstate.JState
import net.devtech.arrp.json.blockstate.JVariant
import net.devtech.arrp.json.lang.JLang
import net.devtech.arrp.json.loot.*
import net.devtech.arrp.json.models.JDisplay
import net.devtech.arrp.json.models.JModel
import net.devtech.arrp.json.models.JPosition
import net.devtech.arrp.json.models.JTextures
import net.devtech.arrp.json.recipe.*
import net.devtech.arrp.json.tags.JTag
import net.devtech.arrp.util.UnsafeByteArrayOutputStream
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.resource.ResourceType
import net.minecraft.tag.BlockTags
import net.minecraft.tag.EntityTypeTags
import net.minecraft.tag.ItemTags
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import ph.mcmod.ct.ARRP
import ph.mcmod.ct.DUMP
import ph.mcmod.ct.NAMESPACE
import java.io.IOException
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timer

/**
 * 基于ARRP，提供了快速添加各种资源文件的方法。
 */
class ArrpHelper(val pack: RuntimeResourcePack, val namespace: String = pack.id.namespace) {
	/**简体中文 "zh_ch"*/
	val lang_zh_cn = JLangUTF8()
	/** 式英语 "en_us"（默认语言）*/
	val lang_en_us = JLang()
	
	val languages = mutableMapOf(
	  Identifier(NAMESPACE, "zh_cn") to lang_zh_cn,
	  Identifier(NAMESPACE, "en_us") to lang_en_us
	)
	
	val tags = mutableMapOf<Identifier, JTag>()
	
	/**可用镐挖掘的方块*/
	val tag_block_pickaxe_mineable: JTag = getTag(BlockTags.PICKAXE_MINEABLE)
	/**箭、光灵箭、药箭等物品*/
	val tag_item_arrows: JTag = getTag(ItemTags.ARROWS)
	/**箭（药箭）、光灵箭等实体*/
	val tag_entityType_arrows: JTag = getTag(EntityTypeTags.ARROWS)
	
	init {
		if (ARRP) RRPCallback.AFTER_VANILLA.register {
			it += pack
			for ((id, lang) in languages) {
				if (lang is JLangUTF8) pack.addAsset(id.pre("lang/") + ".json", lang.toBytes())
				else pack.addLang(id, lang)
			}
			for ((id, jTag) in tags) pack.addTag(id, jTag)
			if (DUMP) {
				pack.dump(Path.of("src\\main\\resources\\assets"))
				Timer().schedule(1000L) { throw RuntimeException("资源包已dump，令游戏崩溃。") }
			}
		}
	}
	
	constructor(namespace: String) : this(RuntimeResourcePack.create(Identifier(namespace, "runtime")), namespace)
	/**为物品设置物品模型：继承同名方块模型*/
	fun add_model_item_block(path: String) {
		add_model_item_block(Identifier(namespace, path))
	}
	
	fun add_model_item_block(id: Identifier) {
		pack.addModel_blockItem(id)
	}
	
	fun add_model_item_block(itemId: Identifier, blockId: Identifier) {
		pack.addModel(JModel.model(blockId.preBlock()), itemId.preItem())
	}
	/**为物品设置物品模型：继承"item/generated"，唯一图层为同名纹理*/
	fun add_model_item_generated(path: String) {
		val id = Identifier(namespace, path)
		add_model_item_generated(id, id.preItem())
	}
	
	fun add_model_item_generated(itemId: Identifier, textureId: Identifier) {
		pack.addModel(JModel.model(Identifier("item/generated")).textures(JTextures().layer0(textureId.toString())), itemId.preItem())
	}
	/**为方块设置方块状态json：只有一种状态，直接导向同名方块模型*/
	fun add_blockState_single(id: Identifier) {
		pack.addBlockState_single(id)
	}
	/**为方块设置方块状态json：只有一种状态，直接导向同名方块模型*/
	fun add_blockState_single(path: String) {
		add_blockState_single(Identifier(namespace, path))
	}
	
	/**为方块设置掉落物战利品表：只掉落它自身*/
	fun add_lootTable_block_dropItself(path: String) {
		add_lootTable_block_dropItself(Identifier(namespace, path))
	}
	
	fun add_lootTable_block_dropItself(id: Identifier) {
		pack.addLootTable_itself(id)
	}
	
	fun add_lootTable_block_silkTouch(path: String) {
		val id = Identifier(namespace, path)
		pack.addResource(ResourceType.SERVER_DATA, Identifier(NAMESPACE, "loot_tables/blocks/${path}.json"), """{
	"type": "block",
	"pools": [
		{
			"rolls": 1,
			"entries": [
				{
					"type": "item",
					"name": "$id",
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
		}
	]
}""".toByteArray())
	}
	/**为方块设置掉落物战利品表：只掉落一个物品，其ID为[itemId]*/
	fun add_lootTable_block_dropSingle(blockPath: String, itemId: Identifier) {
		pack.addLootTable(Identifier(namespace, "blocks/$blockPath"), JLootTable.loot("block").pool(JPool().rolls(1).entry(JEntry().type("item").name(itemId.toString()))))
	}
	/**为箭添加标签，包括物品标签[tag_item_arrows]和实体标签[tag_entityType_arrows]。*/
	fun add_tag_arrow(path: String) {
		val id = Identifier(namespace, path)
		tag_item_arrows.add(id)
		tag_entityType_arrows.add(id)
	}
	@JvmName("getTag_block")
	fun getTag(tag: TagKey<Block>) = tags.getOrPut(tag.id.pre("blocks/")) { JTag.tag() }
	@JvmName("getTag_item")
	fun getTag(tag: TagKey<Item>) = tags.getOrPut(tag.id.pre("items/")) { JTag.tag() }
	@JvmName("getTag_entityType")
	fun getTag(tag: TagKey<EntityType<*>>) = tags.getOrPut(tag.id.pre("entity_types/")) { JTag.tag() }
	@JvmName("getTag_fluid")
	fun getTag(tag: TagKey<Fluid>) = tags.getOrPut(tag.id.pre("fluids/")) { JTag.tag() }
	fun addPickaxeMineable(vararg blockIds: Identifier) {
		tag_block_pickaxe_mineable.apply { blockIds.forEach(this::add) }
	}
	
	fun addPickaxeMineable(vararg blocks: Block) {
		addPickaxeMineable(*blocks.map { it.id }.toTypedArray())
	}
	
	companion object {
		val DISPLAY_BLOCK: JDisplay = JDisplay()
		  .setGui(JPosition().rotation(30f, 225f, 0f).translation(0f, 0f, 0f).scale(0.625f, 0.625f, 0.625f))
		  .setGround(JPosition().rotation(0f, 0f, 0f).translation(0f, 3f, 0f).scale(0.25f, 0.25f, 0.25f))
		  .setFixed(JPosition().rotation(0f, 0f, 0f).translation(0f, 0f, 0f).scale(0.5f, 0.5f, 0.5f))
		  .setThirdperson_righthand(JPosition().rotation(75f, 45f, 0f).translation(0f, 2.5f, 0f).scale(0.375f, 0.375f, 0.375f))
		  .setFirstperson_righthand(JPosition().rotation(0f, 45f, 0f).translation(0f, 0f, 0f).scale(0.4f, 0.4f, 0.4f))
		  .setFirstperson_lefthand(JPosition().rotation(0f, 225f, 0f).translation(0f, 0f, 0f).scale(0.4f, 0.4f, 0.4f))
		
		fun jModel_cubeAll(textureId: Identifier): JModel = JModel.model("block/cube_all").textures(JTextures().`var`("all", textureId.toString()))
	}
	/**在非开发环境下强制使用[Charsets.UTF_8]而非[Charset.defaultCharset]，以免出现乱码。*/
	class JLangUTF8 : JLang() {
		fun toBytes(): ByteArray {
			val ubaos = UnsafeByteArrayOutputStream()
			val writer = OutputStreamWriter(ubaos, Charsets.UTF_8)
			@Suppress("DEPRECATION")
			RuntimeResourcePackImpl.GSON.toJson(lang, writer)
			try {
				writer.close()
			} catch (e: IOException) {
				throw RuntimeException(e)
			}
			return ubaos.bytes
		}
	}
	
	class JCraftingShapedRecipeBuilder(val pack: RuntimeResourcePack, val result: ItemConvertible, val count: Int = 1, val recipeId: Identifier = result.asItem().id.preCraftingShaped()) {
		val keys = mutableMapOf<String, JIngredient>()
		lateinit var pattern: Array<out String>
		
		init {
			synchronized(NOT_BUILT) { NOT_BUILT += this }
		}
		
		operator fun invoke(vararg pattern: String): JCraftingShapedRecipeBuilder {
			this.pattern = pattern
			return this
		}
		
		operator fun invoke(key: String, item0: ItemConvertible): JCraftingShapedRecipeBuilder {
			keys[key] = JIngredient.ingredient().item(item0.asItem())
			return this
		}
		
		operator fun invoke(key: String, tag: TagKey<out Item>): JCraftingShapedRecipeBuilder {
			keys[key] = JIngredient.ingredient().tag(tag)
			return this
		}
		
		operator fun invoke(): ByteArray {
			synchronized(NOT_BUILT) { NOT_BUILT -= this }
			return pack.addRecipe(recipeId, JRecipe.shaped(JPattern.pattern(*pattern), keys.entries.fold(JKeys.keys()) { jKeys, entry -> jKeys.key(entry.key, entry.value) }, JResult.itemStack(result.asItem(), count)))
		}
		
		companion object {
			@JvmField val NOT_BUILT = mutableListOf<JCraftingShapedRecipeBuilder>()
			
			init {
				RRPCallback.BEFORE_VANILLA.register {
					if (NOT_BUILT.isNotEmpty()) {
						error("有JCraftingShapedRecipeBuilder未构建：${NOT_BUILT.map { it.recipeId }}，请调用`JCraftingShapedRecipeBuilder.invoke()`来构建。")
					}
				}
			}
		}
	}
}

fun RuntimeResourcePack.addLootTable_itself(block: Block): ByteArray = addLootTable_itself(block.id)
fun RuntimeResourcePack.addLootTable_itself(id: Identifier): ByteArray = addLootTable_single(id, id)
fun RuntimeResourcePack.addLootTable_single(blockId: Identifier, itemId: Identifier): ByteArray = addLootTable(blockId.pre("blocks/"), JLootTable.loot("block").pool(JPool().rolls(1).entry(JEntry().type("item").name(itemId.toString()))))
fun RuntimeResourcePack.addLootTable_slab(id: Identifier): ByteArray = addData(id.pre("loot_tables/blocks/").json(), """{
	"type": "minecraft:block",
	"pools": [
		{
			"rolls": 1,
			"entries": [
				{
					"type": "minecraft:item",
					"name": "$id",
					"functions": [
						{
							"function": "minecraft:set_count",
							"count": 2,
							"conditions": [
								{
									"condition": "minecraft:block_state_property",
									"block": "$id",
									"properties": {"type": "double"}
								}
							]
						},
						{"function": "minecraft:explosion_decay"}
					]
				}
			]
		}
	]
}""".toByteArray())

fun RuntimeResourcePack.addLootTable_verticalSlab(id: Identifier): ByteArray = addData(id.pre("loot_tables/blocks/").json(), """{
	"type": "minecraft:block",
	"pools": [
		{
			"rolls": 1,
			"entries": [
				{
					"type": "minecraft:item",
					"name": "$id",
					"functions": [
						{
							"function": "minecraft:set_count",
							"conditions": [
								{
									"condition": "minecraft:block_state_property",
									"block": "$id",
									"properties": {"form": "full"}
								}
							],
							"count": 2
						},
						{"function": "minecraft:explosion_decay"}
					]
				}
			]
		}
	]
}""".toByteArray())

fun RuntimeResourcePack.addLootTable_coverplate(id: Identifier): ByteArray = addLootTable(id.pre("blocks/"), JLootTable.loot("block").pool(JPool().rolls(JRoll(1, 1)).entry(JEntry().type("item").name(id.toString()).apply {
	function(JFunction("set_count").parameter("count", 0))
	for (property in CoverplateBlock.PROPERTIES.values) {
		function(JFunction("set_count")
		  .parameter("count", 1)
		  .parameter("add", true)
		  .condition(JCondition("block_state_property")
			.parameter("block", id)
			.parameter("properties", JsonObject().apply { addProperty(property.name, true) })))
	}
})))

fun RuntimeResourcePack.addRecipe_craftingShaped(result: ItemConvertible, count: Int = 1, recipeId: Identifier = result.asItem().id.preCraftingShaped()) = ArrpHelper.JCraftingShapedRecipeBuilder(this, result, count, recipeId)
fun RuntimeResourcePack.addRecipe_craftingShapeless(result: ItemConvertible, count: Int, vararg ingredients: ItemConvertible, recipeId: Identifier = result.asItem().id.pre("crafting_shapeless/")): ByteArray = addRecipe_craftingShapeless(result, count, *ingredients.map { it.asItem().id.toString() }.toTypedArray(), recipeId = recipeId)
fun RuntimeResourcePack.addRecipe_craftingShapeless(result: ItemConvertible, count: Int, vararg ingredients: String, recipeId: Identifier = result.asItem().id.pre("crafting_shapeless/")): ByteArray = addRecipe(recipeId, JRecipe.shapeless(ingredients.fold(JIngredients.ingredients()) { jIngredients, itemOrTag -> jIngredients.add(JIngredient.ingredient().item(itemOrTag)) }, JResult.itemStack(result.asItem(), count)))
fun RuntimeResourcePack.addRecipe_smelting(ingredient: ItemConvertible, result: ItemConvertible, experience: Number, cookingTime: Int = 200, recipeId: Identifier = result.asItem().id.pre("smelting/")): ByteArray = addRecipe(recipeId, JRecipe.smelting(JIngredient.ingredient().item(ingredient.asItem()), JResult.item(result.asItem())).experience(experience.toFloat()).cookingTime(cookingTime))
fun RuntimeResourcePack.addRecipe_blasting(ingredient: ItemConvertible, result: ItemConvertible, experience: Number, cookingTime: Int = 100, recipeId: Identifier = result.asItem().id.pre("blasting/")): ByteArray = addRecipe(recipeId, JRecipe.blasting(JIngredient.ingredient().item(ingredient.asItem()), JResult.item(result.asItem())).experience(experience.toFloat()).cookingTime(cookingTime))
fun RuntimeResourcePack.addRecipe_stoneCutting(ingredient: ItemConvertible, result: ItemConvertible, count: Int, recipeId: Identifier = result.asItem().id.preStoneCutting()): ByteArray = addRecipe(recipeId, JRecipe.stonecutting(JIngredient.ingredient().item(ingredient.asItem()), JResult.itemStack(result.asItem(), count)))
fun RuntimeResourcePack.addRecipe_stoneCutting(ingredient: TagKey<Item>, result: ItemConvertible, count: Int, recipeId: Identifier = result.asItem().id.preStoneCutting()): ByteArray = addRecipe(recipeId, JRecipe.stonecutting(JIngredient.ingredient().tag(ingredient), JResult.itemStack(result.asItem(), count)))
//@JvmName("addTag_block")
//fun RuntimeResourcePack.addTag(tagIdentified: Tag.Identified<Block>, jTag: JTag): ByteArray = addTag(tagIdentified.id.pre("blocks/"), jTag)
//@JvmName("addTag_fluid")
//fun RuntimeResourcePack.addTag(tagIdentified: Tag.Identified<Fluid>, jTag: JTag): ByteArray = addTag(tagIdentified.id.pre("fluids/"), jTag)
//@JvmName("addTag_item")
//fun RuntimeResourcePack.addTag(tagIdentified: Tag.Identified<Item>, jTag: JTag): ByteArray = addTag(tagIdentified.id.pre("items/"), jTag)
//@JvmName("addTag_entityType")
//fun RuntimeResourcePack.addTag(tagIdentified: Tag.Identified<EntityType<*>>, jTag: JTag): ByteArray = addTag(tagIdentified.id.pre("entity_types/"), jTag)

fun JIngredient.tag(tag: TagKey<out Item>): JIngredient {
	tag(tag.id.toString())
	return this
}

operator fun JLang.set(item: Item, translation: String): JLang = this.itemRespect(item, translation)
operator fun JLang.set(block: Block, translation: String): JLang = this.blockRespect(block, translation)
operator fun JLang.set(entityType: EntityType<*>, translation: String): JLang = this.entityRespect(entityType, translation)
operator fun JLang.set(enchantment: Enchantment, translation: String): JLang = this.enchantment(enchantment.id, translation)

fun RuntimeResourcePack.addModel(item: Item, jModel: JModel): ByteArray = addModel(jModel, item.id.pre("item/"))
fun RuntimeResourcePack.addModel(block: Block, jModel: JModel): ByteArray = addModel(jModel, block.id.pre("block/"))
fun RuntimeResourcePack.addModel_cubeAllBlock(id: Identifier): ByteArray = addModel(ArrpHelper.jModel_cubeAll(id.preBlock()), id.preBlock())
fun RuntimeResourcePack.addModel_blockItem(id: Identifier) {
	addModel(JModel.model(id.preBlock()), id.preItem())
}

fun JModel.displayAsBlock(): JModel = this.display(ArrpHelper.DISPLAY_BLOCK)

fun RuntimeResourcePack.addBlockState_single(blockStateId: Identifier, modelId: Identifier = blockStateId.preBlock()) {
	addBlockState(JState.state().add(JVariant().put("", JBlockModel(modelId))), blockStateId)
}

fun RuntimeResourcePack.addBlockStateAndModels_slab(slabId: Identifier, fullBlockId: Identifier) {
	addAsset(slabId.preBlockStates().json(), """{
	"variants": {
		"type=bottom": {"model": "${slabId.preBlock()}"},
		"type=double": {"model": "${fullBlockId.preBlock()}"},
		"type=top": {"model": "${slabId.preBlock()}_top"}
	}
}""".toByteArray())
	addAsset(slabId.preBlock().preModels().json(), """{
	"parent": "minecraft:block/slab",
	"textures": {
		"bottom": "${fullBlockId.preBlock()}",
		"top": "${fullBlockId.preBlock()}",
		"side": "${fullBlockId.preBlock()}"
	}
}""".toByteArray())
	addAsset(slabId.preBlock().preModels().plus("_top").json(), """{
	"parent": "minecraft:block/slab_top",
	"textures": {
		"bottom": "${fullBlockId.preBlock()}",
		"top": "${fullBlockId.preBlock()}",
		"side": "${fullBlockId.preBlock()}"
	}
}""".toByteArray())
	addModel_blockItem(slabId)
}

fun RuntimeResourcePack.addBlockStateAndModels_verticalSlab(slabId: Identifier, fullBlockId: Identifier) {
	addBlockState(JState.state(JVariant()
	  .put(VerticalSlabBlock.FORM.name, VerticalSlabBlock.Form.WEST, JBlockModel(slabId.preBlock()).uvlock().y(180))
	  .put(VerticalSlabBlock.FORM.name, VerticalSlabBlock.Form.EAST, JBlockModel(slabId.preBlock()))
	  .put(VerticalSlabBlock.FORM.name, VerticalSlabBlock.Form.NORTH, JBlockModel(slabId.preBlock()).uvlock().y(270))
	  .put(VerticalSlabBlock.FORM.name, VerticalSlabBlock.Form.SOUTH, JBlockModel(slabId.preBlock()).uvlock().y(90))
	  .put(VerticalSlabBlock.FORM.name, VerticalSlabBlock.Form.FULL, JBlockModel(fullBlockId.preBlock()))), slabId)
	addModel(JModel.model(Identifier(NAMESPACE, "vertical_slab").preBlock()).textures(JTextures().particle(fullBlockId.preBlock().toString())), slabId.preBlock())
	addModel_blockItem(slabId)
}

fun RuntimeResourcePack.addBlockStateAndModels_stairs(stairsId: Identifier, fullBlockId: Identifier) {
	addAsset(stairsId.preBlockStates().json(), """{
	"variants": {
		"facing=east,half=bottom,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"y": 270,
			"uvlock": true
		},
		"facing=east,half=bottom,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner"
		},
		"facing=east,half=bottom,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"y": 270,
			"uvlock": true
		},
		"facing=east,half=bottom,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer"
		},
		"facing=east,half=bottom,shape=straight": {
			"model": "${stairsId.preBlock()}"
		},
		"facing=east,half=top,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"uvlock": true
		},
		"facing=east,half=top,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"y": 90,
			"uvlock": true
		},
		"facing=east,half=top,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"uvlock": true
		},
		"facing=east,half=top,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"y": 90,
			"uvlock": true
		},
		"facing=east,half=top,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"x": 180,
			"uvlock": true
		},
		"facing=north,half=bottom,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"y": 180,
			"uvlock": true
		},
		"facing=north,half=bottom,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"y": 270,
			"uvlock": true
		},
		"facing=north,half=bottom,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"y": 180,
			"uvlock": true
		},
		"facing=north,half=bottom,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"y": 270,
			"uvlock": true
		},
		"facing=north,half=bottom,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"y": 270,
			"uvlock": true
		},
		"facing=north,half=top,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"y": 270,
			"uvlock": true
		},
		"facing=north,half=top,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"uvlock": true
		},
		"facing=north,half=top,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"y": 270,
			"uvlock": true
		},
		"facing=north,half=top,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"uvlock": true
		},
		"facing=north,half=top,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"x": 180,
			"y": 270,
			"uvlock": true
		},
		"facing=south,half=bottom,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner"
		},
		"facing=south,half=bottom,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"y": 90,
			"uvlock": true
		},
		"facing=south,half=bottom,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer"
		},
		"facing=south,half=bottom,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"y": 90,
			"uvlock": true
		},
		"facing=south,half=bottom,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"y": 90,
			"uvlock": true
		},
		"facing=south,half=top,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"y": 90,
			"uvlock": true
		},
		"facing=south,half=top,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"y": 180,
			"uvlock": true
		},
		"facing=south,half=top,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"y": 90,
			"uvlock": true
		},
		"facing=south,half=top,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"y": 180,
			"uvlock": true
		},
		"facing=south,half=top,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"x": 180,
			"y": 90,
			"uvlock": true
		},
		"facing=west,half=bottom,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"y": 90,
			"uvlock": true
		},
		"facing=west,half=bottom,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"y": 180,
			"uvlock": true
		},
		"facing=west,half=bottom,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"y": 90,
			"uvlock": true
		},
		"facing=west,half=bottom,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"y": 180,
			"uvlock": true
		},
		"facing=west,half=bottom,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"y": 180,
			"uvlock": true
		},
		"facing=west,half=top,shape=inner_left": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"y": 180,
			"uvlock": true
		},
		"facing=west,half=top,shape=inner_right": {
			"model": "${stairsId.preBlock()}_inner",
			"x": 180,
			"y": 270,
			"uvlock": true
		},
		"facing=west,half=top,shape=outer_left": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"y": 180,
			"uvlock": true
		},
		"facing=west,half=top,shape=outer_right": {
			"model": "${stairsId.preBlock()}_outer",
			"x": 180,
			"y": 270,
			"uvlock": true
		},
		"facing=west,half=top,shape=straight": {
			"model": "${stairsId.preBlock()}",
			"x": 180,
			"y": 180,
			"uvlock": true
		}
	}
}""".toByteArray())
	addAsset(stairsId.preBlock().preModels().json(), """{
	"parent": "minecraft:block/stairs",
	"textures": {
		"bottom": "${fullBlockId.preBlock()}",
		"top": "${fullBlockId.preBlock()}",
		"side": "${fullBlockId.preBlock()}"
	}
}""".toByteArray())
	addAsset(stairsId.preBlock().preModels().plus("_inner").json(), """{
	"parent": "minecraft:block/inner_stairs",
	"textures": {
		"bottom": "${fullBlockId.preBlock()}",
		"top": "${fullBlockId.preBlock()}",
		"side": "${fullBlockId.preBlock()}"
	}
}""".toByteArray())
	addAsset(stairsId.preBlock().preModels().plus("_outer").json(), """{
	"parent": "minecraft:block/outer_stairs",
	"textures": {
		"bottom": "${fullBlockId.preBlock()}",
		"top": "${fullBlockId.preBlock()}",
		"side": "${fullBlockId.preBlock()}"
	}
}""".toByteArray())
	addModel_blockItem(stairsId)
}

fun RuntimeResourcePack.addBlockStateAndModels_coverplate(coverplateId: Identifier, fullBlockId: Identifier) {
	addAsset(coverplateId.preBlockStates().json(), """{
	"multipart": [
		{
			"when": {"west": true},
			"apply": {"model": "${coverplateId.preBlock()}_face", "y": 270, "uvlock": true}
		},
		{
			"when": {"east": true},
			"apply": {"model": "${coverplateId.preBlock()}_face", "y": 90, "uvlock": true}
		},
		{
			"when": {"down": true},
			"apply": {"model": "${coverplateId.preBlock()}_face", "x": 90, "uvlock": true}
		},
		{
			"when": {"up": true},
			"apply": {"model": "${coverplateId.preBlock()}_face", "x": 270, "uvlock": true}
		},
		{
			"when": {"north": true},
			"apply": {"model": "${coverplateId.preBlock()}_face", "y": 0, "uvlock": true}
		},
		{
			"when": {"south": true},
			"apply": {"model": "${coverplateId.preBlock()}_face", "y": 180, "uvlock": true}
		},
		{
			"when": {"OR": [{"west": true}, {"down": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "y": 0, "uvlock": true}
		},
		{
			"when": {"OR": [{"east": true}, {"down": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "y": 180, "uvlock": true}
		},
		{
			"when": {"OR": [{"north": true}, {"down": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "y": 90, "uvlock": true}
		},
		{
			"when": {"OR": [{"south": true}, {"down": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "y": 270, "uvlock": true}
		},
		{
			"when": {"OR": [{"west": true}, {"up": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 180, "y": 0, "uvlock": true}
		},
		{
			"when": {"OR": [{"east": true}, {"up": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 180, "y": 180, "uvlock": true}
		},
		{
			"when": {"OR": [{"north": true}, {"up": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 180, "y": 90, "uvlock": true}
		},
		{
			"when": {"OR": [{"south": true}, {"up": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 180, "y": 270, "uvlock": true}
		},
		{
			"when": {"OR": [{"west": true}, {"north": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 90, "y": 90, "uvlock": true}
		},
		{
			"when": {"OR": [{"north": true}, {"east": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 90, "y": 180, "uvlock": true}
		},
		{
			"when": {"OR": [{"east": true}, {"south": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 90, "y": 270, "uvlock": true}
		},
		{
			"when": {"OR": [{"south": true}, {"west": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_edge", "x": 90, "y": 0, "uvlock": true}
		},
		{
			"when": {"OR": [{"west": true}, {"down": true}, {"north": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 0, "y": 0, "uvlock": true}
		},
		{
			"when": {"OR": [{"north": true}, {"down": true}, {"east": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 0, "y": 90, "uvlock": true}
		},
		{
			"when": {"OR": [{"east": true}, {"down": true}, {"south": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 0, "y": 180, "uvlock": true}
		},
		{
			"when": {"OR": [{"south": true}, {"down": true}, {"west": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 0, "y": 270, "uvlock": true}
		},
		{
			"when": {"OR": [{"west": true}, {"up": true}, {"north": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 180, "y": 90, "uvlock": true}
		},
		{
			"when": {"OR": [{"north": true}, {"up": true}, {"east": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 180, "y": 180, "uvlock": true}
		},
		{
			"when": {"OR": [{"east": true}, {"up": true}, {"south": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 180, "y": 270, "uvlock": true}
		},
		{
			"when": {"OR": [{"south": true}, {"up": true}, {"west": true}]},
			"apply": {"model": "${coverplateId.preBlock()}_vertex", "x": 180, "y": 0, "uvlock": true}
		}
	]
}""".toByteArray())
	addModel(JModel().parent("$NAMESPACE:block/${CoverplateBlock.PATH}_face").textures(JTextures().particle(fullBlockId.preBlock().toString())), coverplateId.preBlock() + "_face")
	addModel(JModel().parent("$NAMESPACE:block/${CoverplateBlock.PATH}_edge").textures(JTextures().particle(fullBlockId.preBlock().toString())), coverplateId.preBlock() + "_edge")
	addModel(JModel().parent("$NAMESPACE:block/${CoverplateBlock.PATH}_vertex").textures(JTextures().particle(fullBlockId.preBlock().toString())), coverplateId.preBlock() + "_vertex")
	addModel(JModel().parent("$NAMESPACE:block/${CoverplateBlock.PATH}").textures(JTextures().particle(fullBlockId.preBlock().toString())), coverplateId.preBlock())
	addModel_blockItem(coverplateId)
}

fun RuntimeResourcePack.addBlockStateAndModels_fence(fenceId: Identifier, fullBlockId: Identifier) {
	addAsset(fenceId.preBlockStates().json(), """{
	"multipart": [
		{
			"apply": {
				"model": "${fenceId.preBlock()}_post"
			}
		},
		{
			"when": {"north": true},
			"apply": {
				"model": "${fenceId.preBlock()}_side",
				"uvlock": true
			}
		},
		{
			"when": {"east": true},
			"apply": {
				"model": "${fenceId.preBlock()}_side",
				"y": 90,
				"uvlock": true
			}
		},
		{
			"when": {"south": true},
			"apply": {
				"model": "${fenceId.preBlock()}_side",
				"y": 180,
				"uvlock": true
			}
		},
		{
			"when": {"west": true},
			"apply": {
				"model": "${fenceId.preBlock()}_side",
				"y": 270,
				"uvlock": true
			}
		}
	]
}""".toByteArray())
	addModel(JModel.model("block/fence_inventory").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceId.preBlock())
	addModel(JModel.model("block/fence_post").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceId.preBlock() + "_post")
	addModel(JModel.model("block/fence_side").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceId.preBlock() + "_side")
	addModel_blockItem(fenceId)
}

fun RuntimeResourcePack.addBlockStateAndModels_fenceGate(fenceGateId: Identifier, fullBlockId: Identifier) {
	addAsset(fenceGateId.preBlockStates().json(), """{
	"variants": {
		"facing=east,in_wall=false,open=false": {
			"uvlock": true,
			"y": 270,
			"model": "${fenceGateId.preBlock()}"
		},
		"facing=east,in_wall=false,open=true": {
			"uvlock": true,
			"y": 270,
			"model": "${fenceGateId.preBlock()}_open"
		},
		"facing=east,in_wall=true,open=false": {
			"uvlock": true,
			"y": 270,
			"model": "${fenceGateId.preBlock()}_wall"
		},
		"facing=east,in_wall=true,open=true": {
			"uvlock": true,
			"y": 270,
			"model": "${fenceGateId.preBlock()}_wall_open"
		},
		"facing=north,in_wall=false,open=false": {
			"uvlock": true,
			"y": 180,
			"model": "${fenceGateId.preBlock()}"
		},
		"facing=north,in_wall=false,open=true": {
			"uvlock": true,
			"y": 180,
			"model": "${fenceGateId.preBlock()}_open"
		},
		"facing=north,in_wall=true,open=false": {
			"uvlock": true,
			"y": 180,
			"model": "${fenceGateId.preBlock()}_wall"
		},
		"facing=north,in_wall=true,open=true": {
			"uvlock": true,
			"y": 180,
			"model": "${fenceGateId.preBlock()}_wall_open"
		},
		"facing=south,in_wall=false,open=false": {
			"uvlock": true,
			"model": "${fenceGateId.preBlock()}"
		},
		"facing=south,in_wall=false,open=true": {
			"uvlock": true,
			"model": "${fenceGateId.preBlock()}_open"
		},
		"facing=south,in_wall=true,open=false": {
			"uvlock": true,
			"model": "${fenceGateId.preBlock()}_wall"
		},
		"facing=south,in_wall=true,open=true": {
			"uvlock": true,
			"model": "${fenceGateId.preBlock()}_wall_open"
		},
		"facing=west,in_wall=false,open=false": {
			"uvlock": true,
			"y": 90,
			"model": "${fenceGateId.preBlock()}"
		},
		"facing=west,in_wall=false,open=true": {
			"uvlock": true,
			"y": 90,
			"model": "${fenceGateId.preBlock()}_open"
		},
		"facing=west,in_wall=true,open=false": {
			"uvlock": true,
			"y": 90,
			"model": "${fenceGateId.preBlock()}_wall"
		},
		"facing=west,in_wall=true,open=true": {
			"uvlock": true,
			"y": 90,
			"model": "${fenceGateId.preBlock()}_wall_open"
		}
	}
}""".toByteArray())
	addModel(JModel.model("block/template_fence_gate").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceGateId.preBlock())
	addModel(JModel.model("block/template_fence_gate_open").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceGateId.preBlock() + "_open")
	addModel(JModel.model("block/template_fence_gate_wall").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceGateId.preBlock() + "_wall")
	addModel(JModel.model("block/template_fence_gate_wall_open").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), fenceGateId.preBlock() + "_wall_open")
	addModel_blockItem(fenceGateId)
}

fun RuntimeResourcePack.addBlockStateAndModels_wall(wallId: Identifier, fullBlockId: Identifier) {
	addAsset(wallId.preBlockStates().json(), """{
	"multipart": [
		{
			"when": {
				"up": true
			},
			"apply": {
				"model": "${wallId.preBlock()}_post"
			}
		},
		{
			"when": {
				"north": "low"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side",
				"uvlock": true
			}
		},
		{
			"when": {
				"east": "low"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side",
				"y": 90,
				"uvlock": true
			}
		},
		{
			"when": {
				"south": "low"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side",
				"y": 180,
				"uvlock": true
			}
		},
		{
			"when": {
				"west": "low"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side",
				"y": 270,
				"uvlock": true
			}
		},
		{
			"when": {
				"north": "tall"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side_tall",
				"uvlock": true
			}
		},
		{
			"when": {
				"east": "tall"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side_tall",
				"y": 90,
				"uvlock": true
			}
		},
		{
			"when": {
				"south": "tall"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side_tall",
				"y": 180,
				"uvlock": true
			}
		},
		{
			"when": {
				"west": "tall"
			},
			"apply": {
				"model": "${wallId.preBlock()}_side_tall",
				"y": 270,
				"uvlock": true
			}
		}
	]
}""".toByteArray())
	addModel(JModel.model("block/wall_inventory").textures(JTextures().`var`("wall", fullBlockId.preBlock().toString())), wallId.preBlock())
	addModel(JModel.model("block/template_wall_post").textures(JTextures().`var`("wall", fullBlockId.preBlock().toString())), wallId.preBlock() + "_post")
	addModel(JModel.model("block/template_wall_side").textures(JTextures().`var`("wall", fullBlockId.preBlock().toString())), wallId.preBlock() + "_side")
	addModel(JModel.model("block/template_wall_side_tall").textures(JTextures().`var`("wall", fullBlockId.preBlock().toString())), wallId.preBlock() + "_side_tall")
	addModel_blockItem(wallId)
}

fun RuntimeResourcePack.addBlockStateAndModels_button(buttonId: Identifier, fullBlockId: Identifier) {
	addAsset(buttonId.preBlockStates().json(), """{
	"variants": {
		"face=ceiling,facing=east,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 270,
			"x": 180
		},
		"face=ceiling,facing=east,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 270,
			"x": 180
		},
		"face=ceiling,facing=north,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 180,
			"x": 180
		},
		"face=ceiling,facing=north,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 180,
			"x": 180
		},
		"face=ceiling,facing=south,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"x": 180
		},
		"face=ceiling,facing=south,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"x": 180
		},
		"face=ceiling,facing=west,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 90,
			"x": 180
		},
		"face=ceiling,facing=west,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 90,
			"x": 180
		},
		"face=floor,facing=east,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 90
		},
		"face=floor,facing=east,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 90
		},
		"face=floor,facing=north,powered=false": {
			"model": "${buttonId.preBlock()}_released"
		},
		"face=floor,facing=north,powered=true": {
			"model": "${buttonId.preBlock()}_pressed"
		},
		"face=floor,facing=south,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 180
		},
		"face=floor,facing=south,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 180
		},
		"face=floor,facing=west,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 270
		},
		"face=floor,facing=west,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 270
		},
		"face=wall,facing=east,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 90,
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=east,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 90,
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=north,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=north,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=south,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 180,
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=south,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 180,
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=west,powered=false": {
			"model": "${buttonId.preBlock()}_released",
			"y": 270,
			"x": 90,
			"uvlock": true
		},
		"face=wall,facing=west,powered=true": {
			"model": "${buttonId.preBlock()}_pressed",
			"y": 270,
			"x": 90,
			"uvlock": true
		}
	}
}""".toByteArray())
	addModel(JModel.model("block/button").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), buttonId.preBlock() + "_released")
	addModel(JModel.model("block/button_pressed").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), buttonId.preBlock() + "_pressed")
	addModel(JModel.model("block/button_inventory").textures(JTextures().`var`("texture", fullBlockId.preBlock().toString())), buttonId.preBlock())
	addModel_blockItem(buttonId)
}

fun RuntimeResourcePack.addBlockStateAndModels_pressurePlate(pressurePlateId: Identifier, fullBlockId: Identifier) {
	val pressurePlateIdPreBlock = pressurePlateId.preBlock()
	val fullBlockIdPreBlock = fullBlockId.preBlock()
	addAsset(pressurePlateId.preBlockStates().json(), """{
	"variants": {
		"powered=false": {"model": "${pressurePlateIdPreBlock}"},
		"powered=true": {"model": "${pressurePlateIdPreBlock}_down"}
	}
}""".toByteArray())
	addModel(JModel.model("block/pressure_plate_up").textures(JTextures().`var`("texture", fullBlockIdPreBlock.toString())), pressurePlateIdPreBlock)
	addModel(JModel.model("block/pressure_plate_down").textures(JTextures().`var`("texture", fullBlockIdPreBlock.toString())), pressurePlateIdPreBlock + "_down")
	addModel_blockItem(pressurePlateId)
}

fun RuntimeResourcePack.addBlockStateAndModels_iShape(iShapeId: Identifier, fullBlockId: Identifier) {
	val iShapeIdPreBlock = iShapeId.preBlock()
	val iShapeIdPreBlockString = iShapeIdPreBlock.toString()
	val fullBlockIdPreBlock = fullBlockId.preBlock()
	val fullBlockIdPreBlockString = fullBlockIdPreBlock.toString()
	addAsset(iShapeId.preBlockStates().json(), """{
	"variants": {
		"axis=x,sequenced=false": {"model": "${iShapeIdPreBlockString}_x", "x": 90, "y": 0, "uvlock": true},
		"axis=x,sequenced=true": {"model": "${iShapeIdPreBlockString}_x", "x": 0, "y": 0, "uvlock": true},
		"axis=y,sequenced=false": {"model": "${iShapeIdPreBlockString}", "x": 90, "y": 0, "uvlock": true},
		"axis=y,sequenced=true": {"model": "${iShapeIdPreBlockString}", "x": 90, "y": 90, "uvlock": true},
		"axis=z,sequenced=false": {"model": "${iShapeIdPreBlockString}_x", "x": 90, "y": 90, "uvlock": true},
		"axis=z,sequenced=true": {"model": "${iShapeIdPreBlockString}", "x": 0, "y": 0, "uvlock": true}
	}
}""".toByteArray())
	addModel(JModel().parent("$NAMESPACE:block/${IShapeBlock.PATH}").textures(JTextures().particle(fullBlockIdPreBlockString)), iShapeIdPreBlock)
	addModel(JModel().parent("$NAMESPACE:block/${IShapeBlock.PATH}_x").textures(JTextures().particle(fullBlockIdPreBlockString)), iShapeIdPreBlock + "_x")
	addModel_blockItem(iShapeId)
}

fun RuntimeResourcePack.addBlockStateAndModels_glazedTerracotta(blockId: Identifier, textureId: Identifier = blockId.preBlock()) {
	addAsset(blockId.preBlockStates().json(), """{
	"variants": {
		"facing=south": {"model": "${blockId.preBlock()}", "y": 0},
		"facing=west": {"model": "${blockId.preBlock()}", "y": 90},
		"facing=north": {"model": "${blockId.preBlock()}", "y": 180},
		"facing=east": {"model": "${blockId.preBlock()}", "y": 270}
	}
}""".toByteArray())
	addModel(JModel().parent("block/template_glazed_terracotta").textures(JTextures().`var`("pattern", textureId.toString())), blockId.preBlock())
	addModel_blockItem(blockId)
}

fun RuntimeResourcePack.addBlockStateAndModels_generatedTrapdoor(trapdoorId: Identifier) {
	val args = listOf(
	  listOf("east", "bottom", false, 0, 90),
	  listOf("east", "bottom", true, 0, 90),
	  listOf("east", "top", false, 0, 90),
	  listOf("east", "top", true, 180, 270),
	  listOf("north", "bottom", false, 0, 0),
	  listOf("north", "bottom", true, 0, 0),
	  listOf("north", "top", false, 0, 0),
	  listOf("north", "top", true, 180, 180),
	  listOf("south", "bottom", false, 0, 180),
	  listOf("south", "bottom", true, 0, 180),
	  listOf("south", "top", false, 0, 180),
	  listOf("south", "top", true, 180, 0),
	  listOf("west", "bottom", false, 0, 270),
	  listOf("west", "bottom", true, 0, 270),
	  listOf("west", "top", false, 0, 270),
	  listOf("west", "top", true, 180, 90))
	val patterns = listOf("oak", "jungle", "acacia", "crimson", "warped")
	val templates = listOf("bottom", "top", "open")
	addAsset(trapdoorId.preBlockStates().json(), args.joinToString(",\n", """{"variants":{""", "}}\n") { arg ->
		val template = if (arg[2] as Boolean) "open" else arg[1]
		""""facing=${arg[0]},half=${arg[1]},open=${arg[2]}":${
			patterns.joinToString(",", "[", "]") { pattern ->
				"""{"model":"${trapdoorId.preBlock()}/${pattern}_${template}","x":${arg[3]},"y":${arg[4]}}"""
			}
		}"""
	}.toByteArray())
	for (pattern in patterns) for (template in templates) {
		addModel(JModel().parent("block/template_orientable_trapdoor_$template")
		  .textures(JTextures().`var`("texture", "${trapdoorId.preBlock()}/${pattern}")),
		  trapdoorId.preBlock() + "/${pattern}_${template}")
	}
	addModel(JModel.model("${trapdoorId.preBlock()}/oak_bottom"), trapdoorId.preItem())
}
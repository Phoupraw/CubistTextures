package ph.mcmod.ct.game

import com.mojang.datafixers.util.Pair
import net.devtech.arrp.json.recipe.*
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelProviderContext
import net.fabricmc.fabric.api.client.model.ModelResourceProvider
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext
import net.minecraft.block.*
import net.minecraft.client.render.model.*
import net.minecraft.client.render.model.json.ModelOverrideList
import net.minecraft.client.render.model.json.ModelTransformation
import net.minecraft.client.texture.Sprite
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.SpriteIdentifier
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.tag.BlockTags
import net.minecraft.tag.ItemTags
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockRenderView
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.NAMESPACE
import ph.mcmod.ct.api.*
import ph.mcmod.ct.api.SlabStairsSuite.Companion.registerItem
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

object GlowItemFrameWood {
	const val PATH = "glow_item_frame_wood"
	val BLOCK = Block(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)).registerItem(PATH)
	
	init {
		
		runAtClient {
			ARRP_HELPER.add_blockState_single(PATH)
			ModelLoadingRegistry.INSTANCE.registerResourceProvider { ModelResourceProvider(::loadModelResource) }
		}
	}
	@JvmStatic
	fun loadModelResource(identifier: Identifier, modelProviderContext: ModelProviderContext?): UnbakedModel? {
		return if (identifier == GlowItemFrameWoodModel.MODEL_ID) {
			GlowItemFrameWoodModel()
		} else {
			null
		}
	}
	
	class Suite(val path: String, chinese: String) {
		val planks = Block(FabricBlockSettings.copyOf(Blocks.OAK_PLANKS)).registerItem(path)
		val slab = SlabBlock(FabricBlockSettings.copyOf(Blocks.OAK_SLAB)).registerItem("${path}_slab")
		val stairs = PublicStairsBlock(planks.defaultState, FabricBlockSettings.copyOf(Blocks.OAK_STAIRS)).registerItem("${path}_stairs")
		val fence = FenceBlock(FabricBlockSettings.copyOf(Blocks.OAK_FENCE)).registerItem("${path}_fence")
		val fenceGate = FenceGateBlock(FabricBlockSettings.copyOf(Blocks.OAK_FENCE_GATE)).registerItem("${path}_fence_gate")
		val button = PublicWoodenButtonBlock(FabricBlockSettings.copyOf(Blocks.OAK_BUTTON)).registerItem("${path}_button")
		val pressurePlate = PublicPressurePlateBlock(PressurePlateBlock.ActivationRule.EVERYTHING, FabricBlockSettings.copyOf(Blocks.OAK_PRESSURE_PLATE)).registerItem("${path}_pressure_plate")
		val verticalSlab = VerticalSlabBlock(FabricBlockSettings.copyOf(Blocks.OAK_SLAB)).registerItem("${path}_vertical_slab")
		
		init {
			val planksId = planks.id
			ARRP_HELPER.pack.addLootTable_itself(planks)
			ARRP_HELPER.pack.addLootTable_slab(slab.id)
			ARRP_HELPER.pack.addLootTable_itself(stairs)
			ARRP_HELPER.pack.addLootTable_itself(fence)
			ARRP_HELPER.pack.addLootTable_itself(fenceGate)
			ARRP_HELPER.pack.addLootTable_itself(button)
			ARRP_HELPER.pack.addLootTable_itself(pressurePlate)
			ARRP_HELPER.pack.addLootTable_verticalSlab(verticalSlab.id)
			ARRP_HELPER.getTag(BlockTags.PLANKS).add(planksId)
			ARRP_HELPER.getTag(ItemTags.PLANKS).add(planksId)
			ARRP_HELPER.getTag(BlockTags.WOODEN_SLABS).add(slab.id)
			ARRP_HELPER.getTag(ItemTags.WOODEN_SLABS).add(slab.id)
			ARRP_HELPER.getTag(BlockTags.WOODEN_STAIRS).add(stairs.id)
			ARRP_HELPER.getTag(ItemTags.WOODEN_STAIRS).add(stairs.id)
			ARRP_HELPER.getTag(BlockTags.WOODEN_FENCES).add(fence.id)
			ARRP_HELPER.getTag(ItemTags.WOODEN_FENCES).add(fence.id)
			ARRP_HELPER.getTag(BlockTags.WOODEN_BUTTONS).add(button.id)
			ARRP_HELPER.getTag(ItemTags.WOODEN_BUTTONS).add(button.id)
			ARRP_HELPER.getTag(BlockTags.WOODEN_PRESSURE_PLATES).add(pressurePlate.id)
			ARRP_HELPER.getTag(ItemTags.WOODEN_PRESSURE_PLATES).add(pressurePlate.id)
			ARRP_HELPER.pack.addRecipe(slab.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("###"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())), JResult.itemStack(slab.asItem(), 6)))
			ARRP_HELPER.pack.addRecipe(stairs.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("#  ", "## ", "###"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())), JResult.itemStack(stairs.asItem(), 4)))
			ARRP_HELPER.pack.addRecipe(fence.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("#@#", "#@#"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())).key("@", JIngredient.ingredient().item(Items.STICK)), JResult.itemStack(fence.asItem(), 3)))
			ARRP_HELPER.pack.addRecipe(fenceGate.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("@#@", "@#@"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())).key("@", JIngredient.ingredient().item(Items.STICK)), JResult.itemStack(fenceGate.asItem(), 1)))
			ARRP_HELPER.pack.addRecipe(button.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("#"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())), JResult.itemStack(button.asItem(), 1)))
			ARRP_HELPER.pack.addRecipe(pressurePlate.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("##"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())), JResult.itemStack(pressurePlate.asItem(), 1)))
			ARRP_HELPER.pack.addRecipe(verticalSlab.id.preCraftingShaped(), JRecipe.shaped(JPattern.pattern("#", "#", "#"), JKeys.keys().key("#", JIngredient.ingredient().item(planks.asItem())), JResult.itemStack(verticalSlab.asItem(), 6)))
			ARRP_HELPER.pack.addRecipe_stoneCutting(planks.asItem(), verticalSlab.asItem(), 2)
			runAtClient {
				ARRP_HELPER.lang_zh_cn.blockRespect(planks, chinese)
				ARRP_HELPER.add_blockState_single(planksId)
				ARRP_HELPER.pack.addModel(planks, ArrpHelper.jModel_cubeAll(planksId.preBlock()))
				ARRP_HELPER.add_model_item_block(planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(slab, "${chinese}台阶")
				ARRP_HELPER.pack.addBlockStateAndModels_slab(slab.id, planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(stairs, "${chinese}楼梯")
				ARRP_HELPER.pack.addBlockStateAndModels_stairs(stairs.id, planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(fence, "${chinese}栅栏")
				ARRP_HELPER.pack.addBlockStateAndModels_fence(fence.id, planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(fenceGate, "${chinese}栅栏门")
				ARRP_HELPER.pack.addBlockStateAndModels_fenceGate(fenceGate.id, planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(button, "${chinese}按钮")
				ARRP_HELPER.pack.addBlockStateAndModels_button(button.id, planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(pressurePlate, "${chinese}压力板")
				ARRP_HELPER.pack.addBlockStateAndModels_pressurePlate(pressurePlate.id, planksId)
				ARRP_HELPER.lang_zh_cn.blockRespect(verticalSlab, "${chinese}竖台阶")
				ARRP_HELPER.pack.addBlockStateAndModels_verticalSlab(verticalSlab.id, planksId)
			}
		}
	}
//	class GlowModel(val light:Int): ForwardingBakedModel() {
//
//		override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
//			context.emitter.li
//			super.emitBlockQuads(blockView, state, pos, randomSupplier, context)
//		}
//	}
	class GlowItemFrameWoodModel : UnbakedModel, BakedModel, FabricBakedModel {
		lateinit var mesh: Mesh
		override fun getModelDependencies(): Collection<Identifier> = emptyList()
		
		override fun getTextureDependencies(unbakedModelGetter: Function<Identifier, UnbakedModel>?, unresolvedTextureReferences: MutableSet<Pair<String, String>>?): Collection<SpriteIdentifier> = listOf(SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MODEL_ID))
		
		override fun bake(loader: ModelLoader, textureGetter: Function<SpriteIdentifier, Sprite>, rotationContainer: ModelBakeSettings, modelId: Identifier): BakedModel {
			// 获得sprites
//			SPRITE = textureGetter.apply( SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE,MODEL_ID))
			SPRITES =Array(2) { textureGetter.apply(SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, MODEL_ID)) }
//			  SPRITE_IDS.map(textureGetter::apply).toTypedArray()
			// 用Renderer API构建mesh
			val renderer = RendererAccess.INSTANCE.renderer
			val builder: MeshBuilder = renderer!!.meshBuilder()
			val emitter = builder.emitter
			
			for (direction in Direction.values()) {
				val spriteIdx = if (direction.axis.isVertical) 1 else 0
				// 将新的面（face）添加到mesh
				emitter.square(direction, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f)
				// 设置面的sprite，必须在.square()之后调用
				// 我们还没有指定任何uv坐标，所以我们使用整个纹理，BAKE_LOCK_UV恰好就这么做。
				emitter.spriteBake(0, SPRITES[spriteIdx], MutableQuadView.BAKE_LOCK_UV)
				// 启用纹理使用
				emitter.spriteColor(0, -1, -1, -1, -1)
//				loader.getOrLoadModel(WoodenSuite.BIRCH_TRAPDOOR_WOOD.planks.id.preBlock()).bake(loader, textureGetter, rotationContainer, modelId)?.apply { getQuads(WoodenSuite.BIRCH_TRAPDOOR_WOOD.planks.defaultState,Direction.UP,Random())
//					emitter.from
//				}
				val a = 5 * 255 / 15
				emitter.lightmap(a, a, a, a)
//				emitter.lightmap(0,a)
//				MaterialFinder
				// 将quad添加到mesh
				emitter.emit()
			}
			mesh = builder.build()
			
			return this
		}
		
		override fun getQuads(state: BlockState?, face: Direction?, random: Random?): List<BakedQuad> = emptyList()
		
		override fun useAmbientOcclusion(): Boolean = false
		
		override fun hasDepth(): Boolean = false
		
		override fun isSideLit(): Boolean = false
		
		override fun isBuiltin(): Boolean = false
		
		override fun getParticleSprite(): Sprite = SPRITES[1]
		
		override fun getTransformation(): ModelTransformation? = null
		
		override fun getOverrides(): ModelOverrideList? = null
		
		override fun isVanillaAdapter(): Boolean = false
		
		override fun emitBlockQuads(blockView: BlockRenderView, state: BlockState, pos: BlockPos, randomSupplier: Supplier<Random>, context: RenderContext) {
			// 渲染函数
//			context.emitter.lightmap(255,255,255,255)
//			context.emitter.emit()
			// 我们仅渲染 mesh
			context.meshConsumer().accept(mesh)
		}
		
		override fun emitItemQuads(stack: ItemStack?, randomSupplier: Supplier<Random>?, context: RenderContext?) {
		
		}
		
		companion object {
			val SPRITE_IDS = arrayOf(
			  SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier("minecraft:block/furnace_front_on")),
			  SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, Identifier("minecraft:block/furnace_top"))
			)
//			lateinit var SPRITE:Sprite
			lateinit var SPRITES: Array<Sprite>
			val MODEL_ID = Identifier(NAMESPACE, "block/glow_item_frame_wood")
		}
	}
}
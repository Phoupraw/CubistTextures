package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRenderer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.DyeColor
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Heightmap
import net.minecraft.world.World
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.*
import ph.mcmod.ct.game.FakeBeacon.TBlock.ActiveType.*

object FakeBeacon {
	const val PATH = "fake_beacon"
	val BLOCK = TBlock(FabricBlockSettings.copyOf(Blocks.BEACON)).register(PATH)
	val ITEM = BlockItem(BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	val BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(::TBlockEntity, BLOCK).build().register(PATH)
	
	init {
		ARRP_HELPER.pack.addLootTable_itself(BLOCK)
		runAtClient {
			ARRP_HELPER.pack.addBlockState_single(BLOCK.id, Blocks.BEACON.id.preBlock())
			ARRP_HELPER.lang_zh_cn.blockRespect(BLOCK, "伪信标")
			ARRP_HELPER.add_model_item_block(BLOCK.id, Blocks.BEACON.id)
			BlockEntityRendererRegistry.register(BLOCK_ENTITY_TYPE, ::TRenderer)
			BlockRenderLayerMap.INSTANCE.putBlock(BLOCK, RenderLayer.getCutout())
		}
	}
	
	fun calcColors(world: World, pos: BlockPos, state: BlockState, blockEntity: TBlockEntity): List<MutablePair<Int, FloatArray>> {
		val redstone = world.isReceivingRedstonePower(pos)
		when (state[TBlock.ACTIVE_TYPE]) {
			INACTIVE -> return listOf()
			SAME -> if (!redstone) return listOf()
			INVERT -> if (redstone) return listOf()
			else -> {}
		}
		var beamSegment = 1 tm DyeColor.WHITE.colorComponents
		val beamSegments = mutableListOf(beamSegment)
		val topY = world.getTopY(Heightmap.Type.WORLD_SURFACE, pos.x, pos.z)
		var pos1 = pos.up()
		while (pos1.y <= topY) {
			val blockState = world.getBlockState(pos1)
			val block = blockState.block
			if (block is Stainable) {
				val color3F = block.color.colorComponents
				if (beamSegments.size <= 1) {
					beamSegment = 1 tm color3F
					beamSegments += beamSegment
				} else {
					if (color3F contentEquals beamSegment.second) {
						beamSegment.first++
					} else {
						beamSegment = 1 tm (beamSegment.second zip color3F).map { (it.first + it.second) / 2 }.toFloatArray()
						beamSegments += beamSegment
					}
				}
			} else {
				if (blockState.getOpacity(world, pos1) >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
					return listOf()
				} else
					beamSegment.first++
			}
			pos1 = pos1.up()
		}
		beamSegment.first = 1024
		return beamSegments
	}
	
	class TBlock(settings: Settings) : BlockWithEntity(settings) {
		init {
			defaultState = defaultState.with(ACTIVE_TYPE, ACTIVE)
		}
		
		override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
			world.setBlockState(pos, state.cycle(ACTIVE_TYPE))
			return ActionResult.SUCCESS
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(ACTIVE_TYPE)
		}
		
		override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = TBlockEntity(pos, state)
		
		override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.MODEL
		
		companion object {
			val ACTIVE_TYPE: EnumProperty<ActiveType> = EnumProperty.of("active_type", ActiveType::class.java)
		}
		
		enum class ActiveType : EnumStringIdentifiable {
			ACTIVE, INACTIVE, SAME, INVERT
		}
	}
	
	class TBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BLOCK_ENTITY_TYPE, pos, state)
	
	class TRenderer(context: BlockEntityRendererFactory.Context) : BlockEntityRenderer<TBlockEntity> {
		override fun render(entity: TBlockEntity, tickDelta: Float, matrices: MatrixStack, vertexConsumers: VertexConsumerProvider, light: Int, overlay: Int) {
			val world = entity.world ?: return
			val pos = entity.pos
			val state = world.getBlockState(pos)
			if (state.block !is TBlock)
				return
			val beamSegments = calcColors(world, pos, state, entity)
			var yOffset = 0
			for (beamSegment in beamSegments) {
				BeaconBlockEntityRenderer.renderBeam(matrices, vertexConsumers, BeaconBlockEntityRenderer.BEAM_TEXTURE, tickDelta, 1.0f, world.time, yOffset, beamSegment.first, beamSegment.second, 0.2f, 0.25f)
				yOffset += beamSegment.first
			}
		}
	}
}
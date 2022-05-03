@file:Suppress("DEPRECATION")

package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.*
import net.minecraft.block.piston.PistonBehavior
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.item.BlockItem
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.LightType
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.*
import java.util.*
import kotlin.random.asKotlinRandom

object GlimmerOverflow {
	const val PATH = "glimmer_overflow"
	val BLOCK = TBlock(FabricBlockSettings.copyOf(Blocks.GLOWSTONE)).register(PATH)
	val ITEM = BlockItem(BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	const val PERIOD = 20
	val DIRECTIONSES = Direction.Type.HORIZONTAL.permutation().map { listOf(Direction.DOWN) + it + listOf(Direction.UP) }
	const val LUMINANCE = 5
	const val FLAGS = 0
	
	init {
		ARRP_HELPER.pack.addLootTable_itself(BLOCK)
		TLightBlock.loadClass()
		runAtClient {
			ARRP_HELPER.pack.addBlockState_single(BLOCK.id, Blocks.GLOWSTONE.id.preBlock())
			ARRP_HELPER.lang_zh_cn.blockRespect(BLOCK, "荧溢")
			ARRP_HELPER.add_model_item_block(BLOCK.id, Blocks.GLOWSTONE.id)
		}
	}
	
	fun World.scheduleIfAbsent(pos: BlockPos, block: Block, delay: Int) {
		if (!blockTickScheduler.isTicking(pos, block)) createAndScheduleBlockTick(pos, block, delay)
	}
	
	inline fun <reified T> cleanLightsOnUse(block: Block, state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult, range: Int): ActionResult {
		val stack = player.getStackInHand(hand)
		if (stack.isEmpty) {
			if (world.isClient)
				return ActionResult.CONSUME
			cleanLights<T>(world, pos, range)
			world.breakBlock(pos, !player.isCreative)
			return ActionResult.SUCCESS
		}
		return ActionResult.PASS
	}
	
	inline fun <reified T> cleanLights(world: World, pos: BlockPos, range: Int) {
		for (pos1 in BlockPos.iterate(pos.add(-range, -range, -range), pos.add(range, range, range))) {
			if (world.getBlockState(pos1).block is T)
				world.removeBlock(pos1, false)
		}
	}
	
	class TBlock(settings: Settings) : Block(settings) {
		override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
			super.onBlockAdded(state, world, pos, oldState, notify)
			world.scheduleIfAbsent(pos, this, PERIOD)
		}
		
		override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
			super.scheduledTick(state, world, pos, random)
			for (direction in Direction.values()) {
				val pos1 = pos.offset(direction)
				val state1 = world.getBlockState(pos1).run {
					when {
						isAir -> TLightBlock.LIGHT_15
						isOf(Blocks.WATER) && get(FluidBlock.LEVEL) == 0 -> TLightBlock.LIGHT_15.water()
						block is TLightBlock -> with(Properties.LEVEL_15, 15)
						else -> null
					}
				} ?: continue
				world.setBlockState(pos1, state1, FLAGS)
			}
			world.scheduleIfAbsent(pos, this, PERIOD)
		}
		
		override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
			return cleanLightsOnUse<TLightBlock>(this, state, world, pos, player, hand, hit, 32).let {
				if (it == ActionResult.PASS) super.onUse(state, world, pos, player, hand, hit)
				else it
			}
		}
	}
	
	open class TLightBlock(settings: Settings) : LightBlock(settings) {
		override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
			super.onBlockAdded(state, world, pos, oldState, notify)
			world.scheduleIfAbsent(pos, this, PERIOD)
		}
		
		override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
			super.onStateReplaced(state, world, pos, newState, moved)
		}
		
		override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
			super.scheduledTick(state, world, pos, random)
			val light0 = state[LEVEL_15]
//			if (random.nextDouble() * 15 >= light0 && random.nextDouble() < 0.01) {
//				world.removeBlock(pos, false)
//				return
//			}
			var light = light0
			var changed = false
			val blockLight = world.getLightLevel(LightType.BLOCK, pos)
			val distributing0 = light - if (blockLight > LUMINANCE) 0 else LUMINANCE
			if (distributing0 > 0) {
				var distributing = distributing0
				for (direction in DIRECTIONSES.random(random.asKotlinRandom())) {
					val pos1 = pos.offset(direction)
					val blockLight1 = world.getLightLevel(LightType.BLOCK, pos1)
//					if (randomToLong((blockLight1 / 16.0).pow(5), random.nextDouble()).toInt().toBoolean())
//						continue
					val state1 = world.getBlockState(pos1).run {
						if (isAir) LIGHT_0
						else if (isOf(Blocks.WATER) && get(FluidBlock.LEVEL) == 0) LIGHT_0.water()
						else if (block is TLightBlock) this
						else null
					} ?: continue
					val light1 = state1[LEVEL_15]
					val addition = ((distributing + 1) / 2).coerceAtMost(15 - light1)
					distributing -= addition
					if (addition <= 0)
						continue
					world.setBlockState(pos1, state1.with(LEVEL_15, (light1 + addition)), FLAGS)
				}
				if (distributing != distributing0) {
					light = (light - (distributing0 - distributing)).coerceAtMost(15)
				}
			} else run {
				val pos1 = pos.offset(Direction.DOWN)
				val state1 = world.getBlockState(pos1).run {
					if (isAir) LIGHT_0
					else if (isOf(Blocks.WATER) && get(FluidBlock.LEVEL) == 0) LIGHT_0.water()
					else if (block is TLightBlock) this
					else null
				} ?: return@run
				val light1 = state1[LEVEL_15]
				val addition = 1.coerceAtMost(15 - light1)
				light -= addition
				world.setBlockState(pos1, state1.with(LEVEL_15, (light1 + addition)), FLAGS)
			}
			if (light != light0) {
				changed =
				  if (light <= 0) world.removeBlock(pos, false)
				  else world.setBlockState(pos, state.with(LEVEL_15, light), FLAGS)
			}
			if (light > LUMINANCE && !changed)
				world.scheduleIfAbsent(pos, this, PERIOD)
		}
		
		override fun getRenderType(state: BlockState): BlockRenderType = BlockRenderType.INVISIBLE
		
		override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape = super.getOutlineShape(state, world, pos, context)
		
		override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {

//			if (world.isClient)
//				return
//			val light0 = state[LEVEL_15]
//			val light = light0 - 15
//			if (light <= 0)
//				world.removeBlock(pos, false)
//			else if (light != light0)
//				world.setBlockState(pos, state.with(LEVEL_15, light))
		}
		
		override fun canFillWithFluid(world: BlockView?, pos: BlockPos?, state: BlockState?, fluid: Fluid?): Boolean {
			return true
		}
		
		override fun tryFillWithFluid(world: WorldAccess, pos: BlockPos, state: BlockState, fluidState: FluidState): Boolean {
			if (!super.tryFillWithFluid(world, pos, state, fluidState)) {
				world.setBlockState(pos, fluidState.blockState, NOTIFY_ALL)
				world.createAndScheduleFluidTick(pos, fluidState.fluid, fluidState.fluid.getTickRate(world))
			}
			return true
		}
		
		override fun getPistonBehavior(state: BlockState?): PistonBehavior {
			return PistonBehavior.DESTROY
		}
		
		companion object : TLightBlock(FabricBlockSettings.copyOf(Blocks.LIGHT).noCollision().luminance(LUMINANCE).hardness(0f).resistance(0f)) {
			@JvmField
			val LIGHT_15: BlockState = defaultState.with(LEVEL_15, 15)
			@JvmField
			val LIGHT_0: BlockState = defaultState.with(LEVEL_15, 0)
			
			init {
				register("glimmer_overflow_light")
				ARRP_HELPER.pack.addBlockState_single(id, Blocks.GLASS.id.preBlock())
				ARRP_HELPER.lang_zh_cn.blockRespect(this, "荧溢光")
				BlockRenderLayerMap.INSTANCE.putBlock(this, RenderLayer.getCutout())
			}
		}
	}
}
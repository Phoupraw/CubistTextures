@file:Suppress("DEPRECATION")

package ph.mcmod.ct.api

import net.minecraft.block.*
import net.minecraft.block.DoorBlock.OPEN
import net.minecraft.block.TrapdoorBlock.POWERED
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.WATERLOGGED
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import net.minecraft.world.WorldEvents
import net.minecraft.world.event.GameEvent
import ph.mcmod.ct.api.IShapeBlock.Companion.SEQUENCED

class VerticalTrapdoorBlock(settings: Settings, val manual: Boolean) : HorizontalFacingBlock(settings), Waterloggable {
	init {
		defaultState = defaultState.with(SEQUENCED, false).with(OPEN, false).with(WATERLOGGED, false).with(POWERED, false).with(FACING, Direction.EAST)
	}
	
	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		super.appendProperties(builder)
		builder.add(SEQUENCED, OPEN, WATERLOGGED, POWERED, FACING)
	}
	
	override fun canPathfindThrough(state: BlockState, world: BlockView?, pos: BlockPos?, type: NavigationType?): Boolean {
		return when (type) {
			NavigationType.LAND -> state.get(OPEN)
			NavigationType.WATER -> state.get(WATERLOGGED)
			NavigationType.AIR -> state.get(OPEN)
			else -> false
		}
	}
	
	override fun getFluidState(state: BlockState): FluidState {
		return if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
	}
	
	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction?, neighborState: BlockState?, world: WorldAccess, pos: BlockPos?, neighborPos: BlockPos?): BlockState? {
		if (state.get(WATERLOGGED)) {
			world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
		}
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}
	
	override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
		val (x, _, z) = ctx.hitPos - ctx.blockPos.toVec3d()
		val rotation = Direction.fromRotation(ctx.player?.yaw?.toDouble() ?: 0.0)
		val sequenced: Boolean
		val facing = if (rotation.axis == Direction.Axis.X) {
			if (x < 0.5) {
				sequenced = false
				Direction.WEST
			} else {
				sequenced = true
				Direction.EAST
			}
		} else {
			if (z < 0.5) {
				sequenced = false
				Direction.NORTH
			} else {
				sequenced = true
				Direction.SOUTH
			}
		}
		val open = ctx.world.isReceivingRedstonePower(ctx.blockPos)
		return defaultState.with(FACING, facing).with(OPEN, open).with(SEQUENCED, sequenced).with(WATERLOGGED, ctx.world.getFluidState(ctx.blockPos).fluid == Fluids.WATER)
	}
	
	override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
		val facing = state[FACING]
		val sequenced = state[SEQUENCED]
		val open = state[OPEN]
		val direction = if (open) {
			if (sequenced) facing.rotateYClockwise() else facing.rotateYCounterclockwise()
		} else facing
		return SHAPES.getOrPut(direction) { SHAPE_WEST.rotate(Direction.WEST, direction) }
	}
	
	override fun neighborUpdate(state: BlockState, world: World, pos: BlockPos, block: Block, fromPos: BlockPos, notify: Boolean) {
		if (!world.isClient) {
			val powered = world.isReceivingRedstonePower(pos)
			if (powered != state.get(POWERED)) {
				if (state.get(OPEN) != powered) toggleOpen(world, pos, state.with(POWERED, powered), true)
				else world.setBlockState(pos, state.with(POWERED, powered), NOTIFY_LISTENERS)
				if (state.get(WATERLOGGED)) {
					world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
				}
			}
		}
	}
	
	override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
		return if (!manual) {
			ActionResult.PASS
		} else {
			toggleOpen(world, pos, state, true, player)
			ActionResult.success(world.isClient)
		}
	}
	
	fun playToggleSound(player: PlayerEntity?, world: World, pos: BlockPos, open: Boolean) {
		val i: Int = if (open) {
			if (!manual) WorldEvents.IRON_TRAPDOOR_OPENS else WorldEvents.WOODEN_TRAPDOOR_OPENS
		} else {
			if (!manual) WorldEvents.IRON_TRAPDOOR_CLOSES else WorldEvents.WOODEN_TRAPDOOR_CLOSES
		}
		world.syncWorldEvent(player, i, pos, 0)
		world.emitGameEvent(player, if (open) GameEvent.BLOCK_OPEN else GameEvent.BLOCK_CLOSE, pos)
	}
	
	fun toggleOpen(world: World, pos: BlockPos, state: BlockState, together: Boolean, player: PlayerEntity? = null) {
		val facing = state[FACING]
		val sequenced = state[SEQUENCED]
		val open = state[OPEN]
		playToggleSound(player, world, pos, open)
		if (state[WATERLOGGED]) world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
		world.setBlockState(pos, state.cycle(OPEN), NOTIFY_LISTENERS)
		if (together) {
			for (pos1 in listOf(pos.down(), pos.up())) {
				val state1 = world.getBlockState(pos1)
				if (state1.isOf(this) && facing == state1[FACING] && sequenced == state1[SEQUENCED] && open == state1[OPEN]) {
					toggleOpen(world, pos1, state1, false, player)
					break
				}
			}
		}
	}
	
	companion object {
		const val PATH = "vertical_trapdoor"
		val SHAPE_WEST: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 3.0, 16.0, 16.0)
		val SHAPES = mutableMapOf(Direction.WEST to SHAPE_WEST)
	}
}
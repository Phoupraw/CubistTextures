@file:Suppress("DEPRECATION")

package ph.mcmod.ct.api

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.Waterloggable
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties.AXIS
import net.minecraft.state.property.Properties.WATERLOGGED
import net.minecraft.state.property.Property
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.Axis.*
import net.minecraft.util.math.Direction.AxisDirection.POSITIVE
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess

class IShapeBlock(settings: Settings) : Block(settings), Waterloggable {
	init {
		defaultState = defaultState.with(AXIS, X).with(SEQUENCED, false).with(WATERLOGGED, false)
	}
	
	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		super.appendProperties(builder)
		builder.add(AXIS, SEQUENCED, WATERLOGGED)
	}
	
	override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
		val axis = state[AXIS]!!
		val sequenced = state[SEQUENCED]
		return SHAPES.getOrPut(state.water(false).entries) {
			when (axis) {
				X -> SHAPE_0.rotate(Direction.SOUTH, Direction.EAST, Direction.UP).let { if (sequenced) it else it.rotate(axis[POSITIVE]) }
				Y -> SHAPE_0.rotate(Direction.SOUTH, Direction.UP, Direction.EAST).let { if (!sequenced) it else it.rotate(axis[POSITIVE]) }
				Z -> SHAPE_0.let { if (sequenced) it else it.rotate(axis[POSITIVE]) }
			}
		}
	}
	
	override fun hasSidedTransparency(state: BlockState): Boolean {
		return true
	}
	
	override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
		val side = ctx.side
		val (x, y, z) = ctx.hitPos - ctx.blockPos.toVec3d()
		val axis: Direction.Axis = if (ctx.player?.isSneaking != true) ctx.playerLookDirection.axis else side.axis// ?: error("不可能")
		val sequenced = if (ctx.player?.isSneaking != true) {
			when (axis) {
				X -> !side.axis.isHorizontal
				Y -> ctx.player?.horizontalFacing?.axis == X
				Z -> !side.axis.isHorizontal
			}
		} else {
			when (axis) {
				X -> z + y <= 1 && z - y <= 0 || z + y >= 1 && z - y >= 0
				Y -> x + z <= 1 && x - z <= 0 || x + z >= 1 && x - z >= 0
				Z -> y + x <= 1 && y - x <= 0 || y + x >= 1 && y - x >= 0
			}
		}
		return defaultState.with(AXIS, axis).with(SEQUENCED, sequenced).with(WATERLOGGED, ctx.world.getFluidState(ctx.blockPos).fluid == Fluids.WATER)
	}
	
	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
		if (state.get(WATERLOGGED)) world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}
	
	override fun getFluidState(state: BlockState): FluidState {
		return if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
	}
	
	override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
		val axis = state[AXIS]
		val sequenced = state[SEQUENCED]
		return when (rotation) {
			BlockRotation.NONE, BlockRotation.CLOCKWISE_180 -> state
			BlockRotation.CLOCKWISE_90, BlockRotation.COUNTERCLOCKWISE_90 -> (if (axis == Y) state else state.with(AXIS, if (axis == X) Z else X)).with(SEQUENCED, !sequenced)
		}
	}
	
	companion object {
		const val PATH = "i_shape"
		@JvmField val SEQUENCED: BooleanProperty = BooleanProperty.of("sequenced")
		@JvmField val SHAPE_0: VoxelShape = VoxelShapes.union(
		  createCuboidShape(0.0, 0.0, 0.0, 16.0, 5.0, 16.0),
		  createCuboidShape(0.0, 11.0, 0.0, 16.0, 16.0, 16.0),
		  createCuboidShape(5.0, 5.0, 0.0, 11.0, 11.0, 16.0))
		@JvmField val SHAPES = mutableMapOf<Map<Property<*>, Any>, VoxelShape>()
	}
}
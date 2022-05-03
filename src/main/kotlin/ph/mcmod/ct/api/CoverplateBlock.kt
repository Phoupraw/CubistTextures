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
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Property
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import java.util.function.Predicate

class CoverplateBlock(settings: Settings) : Block(settings), Waterloggable {
	init {
		defaultState = PROPERTIES.values.fold(defaultState) { state, property -> state.with(property, false) }.with(Properties.WATERLOGGED, false)
	}
	
	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		super.appendProperties(builder)
		builder.add(*PROPERTIES.values.toTypedArray(), Properties.WATERLOGGED)
	}
	
	override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
		return SHAPES.getOrPut(state.water(false).entries) {
			var shape = VoxelShapes.empty()
			for ((direction, property) in PROPERTIES) {
				if (!state[property]) continue
				shape = VoxelShapes.union(shape, WEST_SHAPE.rotate(Direction.WEST, direction, if (direction.axis == Direction.Axis.Y) Direction.NORTH else Direction.UP))
			}
			shape
		}
	}
	
	override fun getRaycastShape(state: BlockState, world: BlockView, pos: BlockPos): VoxelShape {
		return VoxelShapes.empty()
	}
	
	override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
		if (!world.isClient && newState.isOf(this) && PROPERTIES.values.none { newState[it] }) Asynchronization += { world.removeBlock(pos, false) }
		super.onStateReplaced(state, world, pos, newState, moved)
	}
	
	override fun hasSidedTransparency(state: BlockState): Boolean {
		return !PROPERTIES.values.all { state[it] }
	}
	
	override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
		val world = ctx.world
		val pos = ctx.blockPos
		val state = world.getBlockState(pos)
		val placementState = if (state.isOf(this)) state else defaultState
		val direction = if (state.isOf(this)) {
			if (ctx.hitPos - pos.toVec3d() in Box(0.05, 0.05, 0.05, 0.95, 0.95, 0.95)) ctx.side
			else ctx.side.opposite
		} else ctx.side.opposite
		return placementState.with(PROPERTIES[direction], true).with(Properties.WATERLOGGED, ctx.world.getFluidState(ctx.blockPos).fluid == Fluids.WATER)
	}
	
	override fun canReplace(state: BlockState, ctx: ItemPlacementContext): Boolean {
		val side = ctx.side
		return state.isOf(this)
		  && (((ctx.hitPos - ctx.blockPos.toCenter())[side.axis]) * side.vector[side.axis] < 0
		  && !(state[PROPERTIES[side]] && state[PROPERTIES[side.opposite]]))
	}
	
	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
		if (state.get(Properties.WATERLOGGED)) world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}
	
	override fun getFluidState(state: BlockState): FluidState {
		return if (state.get(Properties.WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
	}
	
	override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
		fun BlockState.rotate(mapper: (Direction) -> Direction): BlockState = Direction.Type.VERTICAL.fold(this) { state1, direction -> state1.with(PROPERTIES[mapper(direction)], this[PROPERTIES[direction]]) }
		return when (rotation) {
			BlockRotation.NONE -> state
			BlockRotation.CLOCKWISE_90 -> state.rotate { it.rotateYClockwise() }
			BlockRotation.CLOCKWISE_180 -> state.rotate { it.opposite }
			BlockRotation.COUNTERCLOCKWISE_90 -> state.rotate { it.rotateYCounterclockwise() }
		}
	}
	
	override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
		fun BlockState.mirror(predicate: Predicate<Direction>) = Direction.values().filter { predicate.test(it) }.fold(this) { state1, direction -> state1.with(PROPERTIES[direction.opposite], this[PROPERTIES[direction]]) }
		return when (mirror) {
			BlockMirror.NONE -> state
			BlockMirror.FRONT_BACK -> state.mirror(Direction.Axis.X)
			BlockMirror.LEFT_RIGHT -> state.mirror(Direction.Axis.Z)
		}
	}
	
	companion object {
		const val PATH = "coverplate"
		@JvmField val PROPERTIES = mutableMapOf(
		  Direction.WEST to Properties.WEST,
		  Direction.EAST to Properties.EAST,
		  Direction.DOWN to Properties.DOWN,
		  Direction.UP to Properties.UP,
		  Direction.NORTH to Properties.NORTH,
		  Direction.SOUTH to Properties.SOUTH
		)
		@JvmField val WEST_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 1.0, 16.0, 16.0)
		@JvmField val SHAPES = mutableMapOf<Map<Property<*>, Any>, VoxelShape>()
		
//		init {
//			val test = CoverplateBlock(FabricBlockSettings.copyOf(Blocks.POLISHED_DIORITE_SLAB)).registerItem(PATH)
//			ARRP_HELPER.pack.addModel_blockItem(test.id)
//		}
	}
}
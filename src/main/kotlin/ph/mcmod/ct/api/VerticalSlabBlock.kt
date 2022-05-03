@file:Suppress("DEPRECATION")

package ph.mcmod.ct.api

import net.minecraft.block.*
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.fluid.Fluid
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.state.property.Properties.WATERLOGGED
import net.minecraft.tag.FluidTags
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.WorldAccess
import kotlin.random.Random

class VerticalSlabBlock(settings: Settings) : Block(settings), Waterloggable {
	init {
		defaultState = defaultState.with(FORM, Form.EAST)
	}
	
	override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
		super.appendProperties(builder)
		builder.add(FORM, WATERLOGGED)
	}
	
	override fun hasSidedTransparency(state: BlockState): Boolean {
		return state.get(FORM) != Form.FULL
	}
	
	override fun getFluidState(state: BlockState): FluidState? {
		return if (state.get(SlabBlock.WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
	}
	
	override fun tryFillWithFluid(world: WorldAccess?, pos: BlockPos?, state: BlockState, fluidState: FluidState?): Boolean {
		return if (state.get(FORM) != Form.FULL) super.tryFillWithFluid(world, pos, state, fluidState) else false
	}
	
	override fun canFillWithFluid(world: BlockView?, pos: BlockPos?, state: BlockState, fluid: Fluid?): Boolean {
		return if (state.get(FORM) != Form.FULL) super.canFillWithFluid(world, pos, state, fluid) else false
	}
	
	override fun getStateForNeighborUpdate(state: BlockState, direction: Direction?, neighborState: BlockState?, world: WorldAccess, pos: BlockPos?, neighborPos: BlockPos?): BlockState? {
		if (state.get(SlabBlock.WATERLOGGED)) world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
	}
	
	override fun canPathfindThrough(state: BlockState?, world: BlockView, pos: BlockPos?, type: NavigationType?): Boolean {
		return when (type) {
			NavigationType.LAND -> false
			NavigationType.WATER -> world.getFluidState(pos).isIn(FluidTags.WATER)
			NavigationType.AIR -> false
			else -> false
		}
	}
	
	override fun canReplace(state: BlockState, ctx: ItemPlacementContext): Boolean {
		val form = state.get(FORM)
		return if (form != Form.FULL && ctx.stack.isOf(asItem())) {
			val side = ctx.side
			val (x, _, z) = ctx.hitPos - ctx.blockPos.toVec3d()
			when (form) {
				Form.WEST -> side == Direction.EAST || x >= 0.5
				Form.EAST -> side == Direction.WEST || x <= 0.5
				Form.NORTH -> side == Direction.SOUTH || z >= 0.5
				Form.SOUTH -> side == Direction.NORTH || z <= 0.5
				else -> false
			}
		} else false
	}
	
	override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
		val world = ctx.world
		val blockPos = ctx.blockPos
		val blockState = world.getBlockState(blockPos)
		return if (blockState.isOf(this)) {
			blockState.with(FORM, Form.FULL).with(SlabBlock.WATERLOGGED, false)
		} else {
			val (x, _, z) = ctx.hitPos - ctx.blockPos.toVec3d()
			defaultState.with(FORM,
			  if (ctx.player?.isSneaking != true) {
				  val direction = ctx.player?.horizontalFacing ?: Direction.EAST
				  if (direction.axis == Direction.Axis.X) {
					  if (x < 0.5) Form.WEST else Form.EAST
				  } else {
					  if (z < 0.5) Form.NORTH else Form.SOUTH
				  }
			  } else {
				  val side = ctx.side
				  when (side.axis!!) {
					  Direction.Axis.Y -> {
						  if (x + z <= 1 && x - z >= 0) Form.NORTH
						  else if (x + z <= 1 && x - z <= 0) Form.WEST
						  else if (x + z >= 1 && x - z >= 0) Form.EAST
						  else Form.SOUTH
					  }
					  Direction.Axis.X -> {
						  if (z < 1 / 3.0) Form.NORTH
						  else if (z < 2 / 3.0) side.opposite.toForm()
						  else Form.SOUTH
					  }
					  Direction.Axis.Z -> {
						  if (x < 1 / 3.0) Form.WEST
						  else if (x < 2 / 3.0) side.opposite.toForm()
						  else Form.EAST
					  }
				  }
			  }
			).with(WATERLOGGED, world.getFluidState(blockPos).fluid == Fluids.WATER)
		}
	}
	
	override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
		return state[FORM].shape
	}
	
	override fun rotate(state: BlockState, rotation: BlockRotation): BlockState {
		val form = state[FORM]
		return if (form == Form.FULL) state
		else when (rotation) {
			BlockRotation.NONE -> state
			BlockRotation.CLOCKWISE_90 -> state.with(FORM, form.toDirection().rotateYClockwise().toForm())
			BlockRotation.CLOCKWISE_180 -> state.with(FORM, form.toDirection().opposite.toForm())
			BlockRotation.COUNTERCLOCKWISE_90 -> state.with(FORM, form.toDirection().rotateYCounterclockwise().toForm())
		}
	}
	
	override fun mirror(state: BlockState, mirror: BlockMirror): BlockState {
		val form = state[FORM]
		return if (form == Form.FULL) state
		else when (mirror) {
			BlockMirror.NONE -> state
			BlockMirror.FRONT_BACK -> if (form.toDirection().axis == Direction.Axis.Z) state.with(FORM, form.toDirection().opposite.toForm()) else state
			BlockMirror.LEFT_RIGHT -> if (form.toDirection().axis == Direction.Axis.X) state.with(FORM, form.toDirection().opposite.toForm()) else state
		}
	}
	
	companion object {
		val FORM: EnumProperty<Form> = EnumProperty.of("form", Form::class.java)
	}
	
	enum class Form(val shape: VoxelShape) : StringIdentifiable {
		WEST(VoxelShapes.cuboid(0.0, 0.0, 0.0, 0.5, 1.0, 1.0)),
		EAST(WEST.shape.rotate(Direction.WEST, Direction.EAST)),
		NORTH(WEST.shape.rotate(Direction.WEST, Direction.NORTH)),
		SOUTH(WEST.shape.rotate(Direction.WEST, Direction.SOUTH)),
		FULL(VoxelShapes.fullCube());
		
		override fun asString(): String = name.lowercase()
		fun toDirection() = when (this) {
			WEST -> Direction.WEST
			EAST -> Direction.EAST
			NORTH -> Direction.NORTH
			SOUTH -> Direction.SOUTH
			FULL -> if (Random.nextBoolean()) Direction.DOWN else Direction.UP
		}
	}
	
	fun Direction.toForm() = when (this) {
		Direction.WEST -> Form.WEST
		Direction.EAST -> Form.EAST
		Direction.NORTH -> Form.NORTH
		Direction.SOUTH -> Form.SOUTH
		else -> Form.FULL
	}
}
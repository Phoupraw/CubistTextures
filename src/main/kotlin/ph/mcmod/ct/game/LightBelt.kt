@file:Suppress("UNUSED_ANONYMOUS_PARAMETER", "DEPRECATION")

package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemPlacementContext
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.state.StateManager
import net.minecraft.state.property.EnumProperty
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.NAMESPACE
import ph.mcmod.ct.api.*

object LightBelt {
	const val PATH = "light_belt"
	val WALL_BLOCK = TWallBlock(FabricBlockSettings.of(Material.METAL, MapColor.ORANGE).strength(3.0F, 6.0F).sounds(BlockSoundGroup.COPPER).nonOpaque().luminance(15).breakInstantly()).register("${PATH}_wall")
	val GROUND_BLOCK = TGroundBlock(FabricBlockSettings.copyOf(WALL_BLOCK)).register("${PATH}_ground")
	val ITEM = BlockItem(GROUND_BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	val BLOCK_TAG: TagKey<Block> = TagKey.of(Registry.BLOCK.key,Identifier(NAMESPACE, PATH))
	
	init {
		ARRP_HELPER.pack.addLootTable_single(WALL_BLOCK.id, ITEM.id)
		ARRP_HELPER.pack.addLootTable_single(GROUND_BLOCK.id, ITEM.id)
		ARRP_HELPER.getTag(BLOCK_TAG).add(WALL_BLOCK.id).add(GROUND_BLOCK.id)
//		ARRP_HELPER.addPickaxeMineable(WALL_BLOCK, GROUND_BLOCK)
		runAtClient {
			ARRP_HELPER.lang_zh_cn.itemRespect(ITEM, "光带")
			ARRP_HELPER.pack.addModel_blockItem(ITEM.id)
//			BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), WALL_BLOCK, GROUND_BLOCK)
		}
	}
	
	
	
	
	
	class TWallBlock(settings: Settings?) : Block(settings) {
		init {
			defaultState = defaultState.with(EAST, StateType.UD).with(WEST, StateType.UD).with(SOUTH, StateType.UD).with(NORTH, StateType.UD)
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(EAST, WEST, SOUTH, NORTH)
		}
		
		companion object {
			val EAST: EnumProperty<StateType> = EnumProperty.of("east", StateType::class.java)
			val WEST: EnumProperty<StateType> = EnumProperty.of("west", StateType::class.java)
			val SOUTH: EnumProperty<StateType> = EnumProperty.of("south", StateType::class.java)
			val NORTH: EnumProperty<StateType> = EnumProperty.of("north", StateType::class.java)
		}
		
		enum class StateType : StringIdentifiable {
			UD, LR, UL, UR, DR, DL;
			
			private val asStringCache = name.lowercase()
			override fun asString(): String = asStringCache
		}
	}
	
	class TGroundBlock(settings: Settings?) : Block(settings) {
		init {
			defaultState = PROPERTIES.values.fold(defaultState) { state, property -> state.with(property, ConnectionState.NONE) }
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(*PROPERTIES.values.toTypedArray())
		}
		
		override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
			super.onBlockAdded(state, world, pos, oldState, notify)
			updateCorners(world, pos, state)
			world.setBlockState(pos, state.getStateForNeighborUpdate(Direction.DOWN, state, world, pos, pos))
		}
		
		override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape = SHAPES.getOrPut(state) { getShapeForState(state) }
		
		override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
			var state0 = super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
			for (direction0 in Direction.Type.HORIZONTAL) {
				val pos1 = pos.offset(direction0).up()
				val state1 = world.getBlockState(pos1)
				val property = PROPERTIES[direction0]
				state0 = if (state1.isIn(BLOCK_TAG)) state0.with(property, ConnectionState.UP)
				else {
					val pos2 = pos.offset(direction0)
					val state2 = world.getBlockState(pos2)
					val pos3 = pos.offset(direction0).down()
					val state3 = world.getBlockState(pos3)
					if (state2.isOf(this) && state2[PROPERTIES[direction0.opposite]] == ConnectionState.JOINT) state0.with(property, ConnectionState.JOINT)
					else if (state2.isIn(BLOCK_TAG) || state3.isIn(BLOCK_TAG)) {
						val pos4 = pos.down()
						val state4 = world.getBlockState(pos4)
						if (state4.isOf(this) && state4[property] == ConnectionState.UP) state0.with(property, ConnectionState.JOINT)
						else state0.with(property, ConnectionState.SIDE)
					} else state0.with(property, ConnectionState.NONE)
				}
			}
			return state0
		}
		
		fun updateCorners(world: World, pos: BlockPos, state: BlockState) {
			for (pos0 in arrayOf(pos.down(), pos.up())) {
				for (direction in Direction.Type.HORIZONTAL) {
					val pos1 = pos0.offset(direction)
					val state1 = world.getBlockState(pos1)
					if (state1.isIn(BLOCK_TAG)) {
						world.setBlockState(pos1, state1.getStateForNeighborUpdate(direction.opposite, state, world, pos1, pos))
					}
				}
			}
		}
		
		override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
			updateCorners(world, pos, state)
			super.onStateReplaced(state, world, pos, newState, moved)
		}
		
		companion object {
			val WEST: EnumProperty<ConnectionState> = EnumProperty.of("west", ConnectionState::class.java)
			val EAST: EnumProperty<ConnectionState> = EnumProperty.of("east", ConnectionState::class.java)
			val NORTH: EnumProperty<ConnectionState> = EnumProperty.of("north", ConnectionState::class.java)
			val SOUTH: EnumProperty<ConnectionState> = EnumProperty.of("south", ConnectionState::class.java)
			val PROPERTIES = mapOf(Direction.WEST to WEST, Direction.EAST to EAST, Direction.NORTH to NORTH, Direction.SOUTH to SOUTH)
			val DOT_SHAPE: VoxelShape = createCuboidShape(5.5, -0.05, 5.5, 10.5, 2.0, 10.5)
			val SHAPES: MutableMap<BlockState, VoxelShape> = mutableMapOf()
			
			fun getShapeForState(state: BlockState): VoxelShape {
				if (PROPERTIES.values.all { state[it].isNone() })
					return DOT_SHAPE
				val center = if (
				  !(state[EAST].isNone() || state[WEST].isNone()) && (state[SOUTH].isNone() && state[NORTH].isNone()) ||
				  (state[EAST].isNone() && state[WEST].isNone()) && !(state[SOUTH].isNone() || state[NORTH].isNone())
				) VoxelShapes.empty() else DOT_SHAPE
				return VoxelShapes.union(center, *PROPERTIES.map {
					when (state[it.value]) {
						ConnectionState.SIDE -> createCuboidShape(6.0, 0.0, 0.0, 10.0, 1.0, 8.0)
						ConnectionState.UP -> VoxelShapes.union(createCuboidShape(6.0, 0.0, 0.0, 10.0, 1.0, 8.0), createCuboidShape(6.0, 1.0, 0.0, 10.0, 16.0, 1.0))
						ConnectionState.JOINT -> VoxelShapes.union(createCuboidShape(6.0, 0.0, 2.0, 10.0, 1.0, 8.0), createCuboidShape(5.0, 0.0, 0.0, 11.0, 3.0, 2.0))
						else -> null
					}?.run { it.key to this }
				}.filterNotNull().map { it.second.rotate(Direction.NORTH, it.first) }.toTypedArray())
			}
			
		}
		
	}
	
	class TItem(block: Block, settings: Settings) : BlockItem(block, settings) {
		
		
		override fun getPlacementState(context: ItemPlacementContext): BlockState? {
			var state = super.getPlacementState(context) ?: return null
			val world = context.world
			val pos = context.blockPos
			for (direction in Direction.Type.HORIZONTAL) {
				val pos1 = pos.offset(direction)
				val state1 = world.getBlockState(pos1)
				if (state1.isIn(BLOCK_TAG)) {
					state = state.with(TGroundBlock.PROPERTIES[direction], ConnectionState.SIDE)
				}
			}
			return state
		}
	}
	
	enum class ConnectionState : EnumStringIdentifiable {
		NONE, SIDE, UP, JOINT;
		
		fun isNone() = this == NONE
	}
}
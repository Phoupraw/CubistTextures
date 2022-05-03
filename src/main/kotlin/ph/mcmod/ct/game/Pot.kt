@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.game

import net.devtech.arrp.json.models.JModel
import net.devtech.arrp.json.models.JTextures
import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.*
import net.minecraft.block.entity.BlockEntity
import net.minecraft.entity.ai.pathing.NavigationType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView
import net.minecraft.world.World
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.*
//TODO
object Pot {
	const val PATH = "white_glazed_terracotta_pot"
	val BLOCK = TBlock(FabricBlockSettings.copyOf(Blocks.FLOWER_POT)).register(PATH)
	val ITEM = BlockItem(BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	val BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(::TBlockEntity, BLOCK).build().register(PATH)
	
	init {
		runAtClient {
			ARRP_HELPER.lang_zh_cn.itemRespect(ITEM, "白色带釉陶瓦花盆")
			ARRP_HELPER.add_blockState_single(BLOCK.id)
			ARRP_HELPER.pack.addModel(BLOCK, JModel.model("block/flower_pot").textures(JTextures()
			  .particle(Blocks.WHITE_GLAZED_TERRACOTTA.id.preBlock().toString())
			  .`var`("flowerpot", Blocks.WHITE_GLAZED_TERRACOTTA.id.preBlock().toString())))
			ARRP_HELPER.add_model_item_block(PATH)
		}
	}
	
	class TBlock(settings: Settings) : BlockWithEntity(settings) {
		init {
			defaultState=defaultState.with(Properties.EXTENDED,false)
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(Properties.EXTENDED)
		}
		override fun getOutlineShape(state: BlockState?, world: BlockView?, pos: BlockPos?, context: ShapeContext?): VoxelShape? {
			return SHAPE
		}
		
		override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
			if (world !is ServerWorld) {
				return ActionResult.CONSUME
			} else {
				(world.getBlockEntity(pos) as? TBlockEntity)?.apply {
					val handStack = player.getStackInHand(hand)
					if (plant == null) {
					
					} else {
						if (handStack.isEmpty) {
						
						}
					}
				}
				
				
				
				return ActionResult.SUCCESS
			}
		}
		
		override fun getPickStack(world: BlockView, pos: BlockPos, state: BlockState): ItemStack {
			return super.getPickStack(world, pos, state).apply {
				(world.getBlockEntity(pos) as? TBlockEntity)?.apply {
					orCreateNbt.put("BlockEntityTag", writeNbt(NbtCompound()))
				}
			}
		}
		
		override fun createBlockEntity(pos: BlockPos, state: BlockState): BlockEntity {
			return TBlockEntity(pos, state)
		}
		
		override fun canPathfindThrough(state: BlockState?, world: BlockView?, pos: BlockPos?, type: NavigationType?): Boolean {
			return false
		}
		
		override fun getRenderType(state: BlockState): BlockRenderType {
			return if (state[Properties.EXTENDED]) BlockRenderType.ENTITYBLOCK_ANIMATED else BlockRenderType.MODEL
		}
		
		companion object {
			val SHAPE: VoxelShape = createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 11.0)
		}
	}
	
	class TBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(BLOCK_ENTITY_TYPE, pos, state) {
		var plant: Pair<BlockState, BlockEntity?>? = null
		override fun writeNbt(root: NbtCompound) {
			super.writeNbt(root)
			plant?.apply {
				root.put("plant", NbtCompound().apply {
					put("state", NbtHelper.fromBlockState(first))
					second?.apply { put("entity", createNbtWithIdentifyingData()) }
				})
			}
		}
		
		override fun readNbt(root: NbtCompound) {
			super.readNbt(root)
			plant = if (root.containsCompound("plant")) {
				val compound = root.getCompound("plant")
				val state = NbtHelper.toBlockState(compound.getCompound("state"))
				val blockEntity = if (state is BlockEntityProvider) {
					state.createBlockEntity(pos, state)?.apply {
						if (compound.containsCompound("entity")) {
							readNbt(compound.getCompound("entity"))
						}
					}
				} else null
				state to blockEntity
			} else null
		}
	}
}
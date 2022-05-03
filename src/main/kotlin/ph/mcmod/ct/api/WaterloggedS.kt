@file:Suppress("DEPRECATION")

package ph.mcmod.ct.api

import net.devtech.arrp.json.blockstate.JBlockModel
import net.devtech.arrp.json.blockstate.JState
import net.devtech.arrp.json.blockstate.JVariant
import net.devtech.arrp.json.models.JModel
import net.devtech.arrp.json.models.JTextures
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.TorchBlock
import net.minecraft.block.Waterloggable
import net.minecraft.client.render.RenderLayer
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemGroup
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties
import net.minecraft.state.property.Properties.WATERLOGGED
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import net.minecraft.world.WorldAccess
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.RESOURCE_PACK
import java.util.*
import java.util.function.ToIntFunction

object WaterloggedS {
	@JvmField
	val TORCH_ITEM_SETTINGS: FabricItemSettings = FabricItemSettings().group(ItemGroup.DECORATIONS)
	@JvmField
	val CAMPFIRE_ITEM_SETTINGS = TORCH_ITEM_SETTINGS
	
	open class FloorTorchBlock(settings: Settings, particle: ParticleEffect) : TorchBlock(settings, particle), Waterloggable {
		companion object {
			fun addBlockState(id: Identifier) {
				ARRP_HELPER.add_blockState_single(id)
			}
			
			fun addBlockState(path: String) {
				ARRP_HELPER.add_blockState_single(path)
			}
			
			fun addBlockModel(block: Block) {
				val prefixedId = block.id.pre("block/")
				RESOURCE_PACK.addModel(JModel().parent("block/template_torch").textures(JTextures().`var`("torch", prefixedId.toString())), prefixedId)
				BlockRenderLayerMapImpl.INSTANCE.putBlocks(RenderLayer.getCutout(), block)
			}
			
			fun addItemModel(id: Identifier) {
				RESOURCE_PACK.addModel(JModel().parent("item/generated").textures(JTextures().layer0(id.pre("block/").toString())), id.pre("item/"))
			}
		}
		
		init {
			defaultState = super.getDefaultState().with(WATERLOGGED, false)
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(WATERLOGGED)
		}
		
		override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
			val fluidState = ctx.world.getFluidState(ctx.blockPos)
			val bl = fluidState.fluid == Fluids.WATER
			return super.getPlacementState(ctx)!!.with(WATERLOGGED, bl)
		}
		
		override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
			if (state.get(WATERLOGGED)) world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
			return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
		}
		
		override fun getFluidState(state: BlockState): FluidState {
			return if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
		}
		
		override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
			val d = pos.x + 0.5
			val e = pos.y + 0.7
			val f = pos.z + 0.5
			if (!world.isWater(pos))
				world.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0)
			world.addParticle(particle, d, e, f, 0.0, 0.0, 0.0)
		}
	}
	
	open class WallTorchBlock(settings: Settings, particle: ParticleEffect) : net.minecraft.block.WallTorchBlock(settings, particle), Waterloggable {
		companion object {
			@JvmStatic
			fun addBlockState(wallTorchId: Identifier) {
				RESOURCE_PACK.addBlockState(JState.state().add(JVariant().apply {
					for (facing2y in arrayOf("east" to 0, "south" to 90, "west" to 180, "north" to 270)) {
						put("facing=${facing2y.first}", JBlockModel(wallTorchId.pre("block/")).y(facing2y.second))
					}
				}), wallTorchId)
			}
			
			@JvmStatic
			fun addBlockModel(wallTorchBlock: Block, torchId: Identifier) {
				RESOURCE_PACK.addModel(JModel().parent("block/template_torch_wall").textures(JTextures().`var`("torch", torchId.pre("block/").toString())), wallTorchBlock.id.pre("block/"))
				BlockRenderLayerMapImpl.INSTANCE.putBlocks(RenderLayer.getCutout(), wallTorchBlock)
			}
		}

//		private val translationKey0: String by lazy { Util.createTranslationKey("block", id) }
		
		init {
			defaultState = super.getDefaultState().with(WATERLOGGED, false)
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(WATERLOGGED)
		}

//		override fun getTranslationKey(): String {
//			return translationKey0
//		}
		
		override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
			val fluidState = ctx.world.getFluidState(ctx.blockPos)
			val bl = fluidState.fluid == Fluids.WATER
			return super.getPlacementState(ctx)?.with(WATERLOGGED, bl)
		}
		
		override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
			if (state.get(WATERLOGGED)) {
				world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
			}
			return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
		}
		
		override fun getFluidState(state: BlockState): FluidState {
			return if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
		}
		
		override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
			val direction = state.get(FACING)
			val d = pos.x + 0.5
			val e = pos.y + 0.7
			val f = pos.z + 0.5
			val g = 0.22
			val h = 0.27
			val direction2 = direction.opposite
			if (!world.isWater(pos))
				world.addParticle(ParticleTypes.SMOKE, d + 0.27 * direction2.offsetX, e + g, f + h * direction2.offsetZ, 0.0, 0.0, 0.0)
			world.addParticle(particle, d + 0.27 * direction2.offsetX, e + g, f + h * direction2.offsetZ, 0.0, 0.0, 0.0)
		}
	}
	
	open class CampfireBlock(val emitsParticles: Boolean, fireDamage: Int, settings: Settings) : net.minecraft.block.CampfireBlock(emitsParticles, fireDamage, settings) {
		companion object {
			@JvmStatic
			fun createLightLevelFromLitBlockState(litLevel: Int): ToIntFunction<BlockState> {
				return ToIntFunction { state: BlockState -> if (state.get(Properties.LIT)) litLevel else 0 }
			}
		}
		
		override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
			return super.getPlacementState(ctx)?.with(LIT, true)
		}
		
		override fun tryFillWithFluid(world: WorldAccess, pos: BlockPos, state: BlockState, fluidState: FluidState): Boolean {
			return if (!state.get(Properties.WATERLOGGED) && fluidState.fluid == Fluids.WATER) {
				if (!world.isClient) {
					world.setBlockState(pos, state.with(Properties.WATERLOGGED, true) as BlockState, NOTIFY_ALL)
					world.createAndScheduleFluidTick(pos, fluidState.fluid, fluidState.fluid.getTickRate(world))
				}
				true
			} else {
				false
			}
		}
		
		override fun onProjectileHit(world: World, state: BlockState, hit: BlockHitResult, projectile: ProjectileEntity) {
			val blockPos = hit.blockPos
			if (!world.isClient && projectile.isOnFire && projectile.canModifyAt(world, blockPos) && !state.get(LIT)) {
				world.setBlockState(blockPos, state.with(Properties.LIT, true) as BlockState, NOTIFY_ALL or REDRAW_ON_MAIN_THREAD)
			}
		}
		
		override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
			if (state.get(LIT)) {
				if (random.nextInt(10) == 0) {
					world.playSound(pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5, SoundEvents.BLOCK_CAMPFIRE_CRACKLE, SoundCategory.BLOCKS, 0.5f + random.nextFloat(), random.nextFloat() * 0.7f + 0.6f, false)
				}
				if (emitsParticles && random.nextInt(5) == 0 && !world.isWater(pos)) {
					for (i in 0..random.nextInt(1)) {
						world.addParticle(ParticleTypes.LAVA, pos.x.toDouble() + 0.5, pos.y.toDouble() + 0.5, pos.z.toDouble() + 0.5, (random.nextFloat() / 2.0f).toDouble(), 5.0E-5, (random.nextFloat() / 2.0f).toDouble())
					}
				}
			}
		}
	}
	
	//	open class CampfireBlockEntity(pos: BlockPos, state: BlockState, type: BlockEntityType<*>) : net.minecraft.block.entity.BlockEntity(type, pos, state) {
//		val delegating = CampfireBlockEntity(pos, state)
//		override fun readNbt(nbt: NbtCompound?) {
//			super.readNbt(nbt)
//			delegating.readNbt(nbt)
//		}
//
//		override fun writeNbt(nbt: NbtCompound?) {
//			super.writeNbt(nbt)
//			(delegating as BlockEntity).writeNbt(nbt)
//		}
//	}
	@Suppress("unused")
	class WaterLoggableBlockTemplate(settings: Settings) : Block(settings), Waterloggable {
		init {
			defaultState = super.getDefaultState().with(WATERLOGGED, false)
		}
		
		override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
			super.appendProperties(builder)
			builder.add(WATERLOGGED)
		}
		
		override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
			val fluidState = ctx.world.getFluidState(ctx.blockPos)
			val bl = fluidState.fluid == Fluids.WATER
			return super.getPlacementState(ctx)?.with(WATERLOGGED, bl)
		}
		
		override fun getStateForNeighborUpdate(state: BlockState, direction: Direction, neighborState: BlockState, world: WorldAccess, pos: BlockPos, neighborPos: BlockPos): BlockState {
			if (state.get(WATERLOGGED)) {
				world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
			}
			return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos)
		}
		
		override fun getFluidState(state: BlockState): FluidState {
			return if (state.get(WATERLOGGED)) Fluids.WATER.getStill(false) else super.getFluidState(state)
		}
	}
}
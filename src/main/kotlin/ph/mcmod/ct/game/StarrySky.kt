@file:Suppress("DEPRECATION")

package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.property.Properties
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.*
import java.util.*

object StarrySky {
	const val PATH = "starry_sky"
	val BLOCK = TBlock(FabricBlockSettings.copyOf(Blocks.GLOWSTONE).ticksRandomly()).register(PATH)
	val ITEM = BlockItem(BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	const val FLAGS =0
	const val RANGE = 64
	const val BATCH = 20
	
	init {
		ARRP_HELPER.pack.addLootTable_itself(BLOCK)
		runAtClient {
			ARRP_HELPER.pack.addBlockState_single(BLOCK.id, Blocks.GLOWSTONE.id.preBlock())
			ARRP_HELPER.lang_zh_cn.blockRespect(BLOCK, "满天星")
			ARRP_HELPER.add_model_item_block(BLOCK.id, Blocks.GLOWSTONE.id)
		}
	}
	
	class TBlock(settings: Settings) : Block(settings) {
		override fun randomTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
			super.randomTick(state, world, pos, random)
			var i = 0
			for (pos1 in BlockPos.iterateRandomly(random, Int.MAX_VALUE, pos, RANGE)) {
				val state1 = world.getBlockState(pos1).run {
					when {
						isAir -> TLightBlock.defaultState
						isOf(Blocks.WATER) && get(FluidBlock.LEVEL) == 0 -> TLightBlock.defaultState.water()
						block is LightBlock -> with(Properties.LEVEL_15, 15)
						else -> null
					}
				} ?: continue
				if (world.setBlockState(pos1, state1, FLAGS)) {
					i++
					if (i>= BATCH)
						break
				}
			}
		}
		
		override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
			return GlimmerOverflow.cleanLightsOnUse<TLightBlock>(this, state, world, pos, player, hand, hit, RANGE).let {
				if (it == ActionResult.PASS) super.onUse(state, world, pos, player, hand, hit)
				else it
			}
		}
	}
	
	object TLightBlock : LightBlock(FabricBlockSettings.copyOf(Blocks.LIGHT)) {
		init {
			register("${PATH}_light")
		}
	}
}
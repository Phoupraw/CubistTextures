package ph.mcmod.ct.api

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.TrapdoorBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.fluid.Fluids.WATER
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import net.minecraft.world.WorldEvents.*
import net.minecraft.world.event.GameEvent
import net.minecraft.world.event.GameEvent.BLOCK_CLOSE
import net.minecraft.world.event.GameEvent.BLOCK_OPEN

open class ManualTrapdoorBlock(settings: Settings, val manual: Boolean) : TrapdoorBlock(settings) {
	override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
		return if (!manual) {
			ActionResult.PASS
		} else {
			val state1 = state.cycle(OPEN)
			world.setBlockState(pos, state1, NOTIFY_LISTENERS)
			if (state1.get(WATERLOGGED)) world.createAndScheduleFluidTick(pos, WATER, WATER.getTickRate(world))
			playToggleSound(player, world, pos, state1.get(OPEN))
			ActionResult.success(world.isClient)
		}
	}
	
	override fun playToggleSound(player: PlayerEntity?, world: World, pos: BlockPos, open: Boolean) {
		val i: Int = if (open) {
			if (!manual) IRON_TRAPDOOR_OPENS else WOODEN_TRAPDOOR_OPENS
		} else {
			if (!manual) IRON_TRAPDOOR_CLOSES else WOODEN_TRAPDOOR_CLOSES
		}
		world.syncWorldEvent(player, i, pos, 0)
		world.emitGameEvent(player, if (open) BLOCK_OPEN else BLOCK_CLOSE, pos)
	}
}
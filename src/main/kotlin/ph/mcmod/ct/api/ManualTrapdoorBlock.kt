package ph.mcmod.ct.api

import net.minecraft.block.BlockState
import net.minecraft.block.Material
import net.minecraft.block.TrapdoorBlock
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.fluid.Fluids
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.WorldEvents
import net.minecraft.world.event.GameEvent

class ManualTrapdoorBlock(settings: Settings, val manual: Boolean) : TrapdoorBlock(settings) {
	override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
		return if (!manual) {
			ActionResult.PASS
		} else {
			val state1 = state.cycle(OPEN)
			world.setBlockState(pos, state1, NOTIFY_LISTENERS)
			if (state1.get(WATERLOGGED)) world.createAndScheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
			playToggleSound(player, world, pos, state1.get(OPEN))
			ActionResult.success(world.isClient)
		}
	}
	
	override fun playToggleSound(player: PlayerEntity?, world: World, pos: BlockPos, open: Boolean) {
		val i: Int = if (open) {
			if (!manual) WorldEvents.IRON_TRAPDOOR_OPENS else WorldEvents.WOODEN_TRAPDOOR_OPENS
		} else {
			if (!manual) WorldEvents.IRON_TRAPDOOR_CLOSES else WorldEvents.WOODEN_TRAPDOOR_CLOSES
		}
		world.syncWorldEvent(player, i, pos, 0)
		world.emitGameEvent(player, if (open) GameEvent.BLOCK_OPEN else GameEvent.BLOCK_CLOSE, pos)
	}
}
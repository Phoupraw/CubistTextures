@file:Suppress("UNUSED_ANONYMOUS_PARAMETER")

package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents
import net.fabricmc.fabric.api.item.v1.FabricItemSettings
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.FluidBlock
import net.minecraft.client.render.RenderLayer
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.fluid.FluidState
import net.minecraft.fluid.Fluids
import net.minecraft.item.BlockItem
import net.minecraft.util.Hand
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.ITEM_GROUP
import ph.mcmod.ct.api.playWaterEvaporation
import ph.mcmod.ct.api.register
import ph.mcmod.ct.api.runAtClient

object SealedGlassWaterVat {
	const val PATH = "sealed_glass_water_vat"
	val BLOCK = TBlock(FabricBlockSettings.copyOf(Blocks.GLASS)).register(PATH)
	val ITEM = BlockItem(BLOCK, FabricItemSettings().group(ITEM_GROUP)).register(PATH)
	
	init {
		PlayerBlockBreakEvents.AFTER.register { world, player, blockPos, blockState, blockEntity ->
			if (!blockState.isOf(BLOCK))
				return@register
			val hot = world.dimension.isUltrawarm
			val artificial = player.isCreative && Hand.values().map(player::getStackInHand).any { it.isOf(ITEM) } || EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, player.mainHandStack) >= 1
			if (hot || artificial) {
				if (world.isWater(blockPos)) {
					(Blocks.WATER as FluidBlock).tryDrainFluid(world, blockPos, world.getBlockState(blockPos))
					if (hot && !artificial) {
						playWaterEvaporation(world, blockPos)
					}
				}
			}
		}
		runAtClient {
			ARRP_HELPER.add_blockState_single(PATH)
//			ARRP_HELPER.addBlockItemModel(PATH)
			ARRP_HELPER.lang_zh_cn.blockRespect(BLOCK, "密封玻璃水缸")
			BlockRenderLayerMapImpl.INSTANCE.putBlocks(RenderLayer.getCutout(), BLOCK)
		}
	}
	
	class TBlock(settings: Settings) : net.minecraft.block.Block(settings) {
		override fun getFluidState(state: BlockState): FluidState = Fluids.WATER.getStill(false)
	}
}
package ph.mcmod.ct.game

import net.minecraft.item.WallStandingBlockItem
import net.minecraft.particle.ParticleTypes
import net.minecraft.state.property.Properties
import ph.mcmod.ct.ARRP_HELPER
import ph.mcmod.ct.api.*

object LimeTorch {
	const val PATH = "lime_torch"
	@JvmField
	val BLOCK = WaterloggedS.FloorTorchBlock(CoralLight.TORCH_BLOCK_SETTINGS.luminance { blockState -> if (blockState.get(Properties.WATERLOGGED)) 15 else 10 }, ParticleTypes.FLAME).register(PATH)
	@JvmField
	val WALL_BLOCK = WaterloggedS.WallTorchBlock(CoralLight.TORCH_BLOCK_SETTINGS.luminance { blockState -> if (blockState.get(Properties.WATERLOGGED)) 15 else 10 }, ParticleTypes.FLAME).register("lime_wall_torch")
	@JvmField
	val ITEM = WallStandingBlockItem(BLOCK, WALL_BLOCK, WaterloggedS.TORCH_ITEM_SETTINGS).register(PATH)
	
	init {
		ARRP_HELPER.add_lootTable_block_dropItself(PATH)
		SynopsisTooltip.addSynopsis(ITEM,"水下亮度15；其它亮度10")
		runAtClient {
			WaterloggedS.FloorTorchBlock.addBlockState(PATH)
			WaterloggedS.WallTorchBlock.addBlockState(WALL_BLOCK.id)
			WaterloggedS.FloorTorchBlock.addBlockModel(BLOCK)
			WaterloggedS.WallTorchBlock.addBlockModel(WALL_BLOCK, BLOCK.id)
			WaterloggedS.FloorTorchBlock.addItemModel(ITEM.id)
			ARRP_HELPER.lang_zh_cn.blockRespect(BLOCK, "黄绿火把")
		}
	}
}
package ph.mcmod.ct.game

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Blocks
import net.minecraft.block.MapColor
import net.minecraft.block.Material
import net.minecraft.item.Items
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.BlockSoundGroup
import ph.mcmod.ct.api.SynopsisTooltip
import ph.mcmod.ct.api.WaterloggedS

object VanillaSoulLight {
	const val ENABLE = true
	@JvmField
	val SOUL_TORCH = WaterloggedS.FloorTorchBlock(FabricBlockSettings.copyOf(Blocks.TORCH).luminance(10), ParticleTypes.SOUL_FIRE_FLAME)
	@JvmField
	val SOUL_WALL_TORCH = WaterloggedS.WallTorchBlock(FabricBlockSettings.copyOf(Blocks.WALL_TORCH).luminance(10), ParticleTypes.SOUL_FIRE_FLAME)
	@JvmField
	val SOUL_CAMPFIRE = WaterloggedS.CampfireBlock(false, 2, FabricBlockSettings.of(Material.WOOD, MapColor.SPRUCE_BROWN).strength(2.0F).sounds(BlockSoundGroup.WOOD).nonOpaque().luminance(WaterloggedS.CampfireBlock.createLightLevelFromLitBlockState(10)))
	
	object LateInit {
		init {
			SynopsisTooltip.addSynopsis(Items.SOUL_TORCH, "可放在水下")
			SynopsisTooltip.addSynopsis(Items.SOUL_CAMPFIRE, "可在水下点燃")
		}
	}
}
package ph.mcmod.ct.game

import net.minecraft.block.Block

object GlowOverflow {
	const val PATH = "glow_overflow"
	
	open class TBlock(settings: Settings) : Block(settings)
}
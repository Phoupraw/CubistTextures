package ph.mcmod.ct.api

import net.minecraft.util.math.BlockBox
import net.minecraft.util.math.BlockPos
import kotlin.random.Random

interface BlockPosIterator : Iterator<BlockPos> {
	val box: BlockBox
}

abstract class AbstractBlockPosIterator(final override val box: BlockBox) : BlockPosIterator {
	var count = 0u
	val size = (box.blockCountX * box.blockCountY * box.blockCountZ).toUInt()
	final override fun hasNext(): Boolean = count < size
}

class RandomBlockPosIterator(box: BlockBox, val random: Random = Random.Default) : AbstractBlockPosIterator(box) {
	override fun next(): BlockPos {
		count++
		return BlockPos(random.nextInt(box.minX, box.maxX + 1), random.nextInt(box.minY, box.maxY + 1), random.nextInt(box.minZ, box.maxZ + 1))
	}
}

class HashBlockPosIterator(box: BlockBox, seed: Int = 31) : AbstractBlockPosIterator(box) {
	var index = seed.toUInt()
	override fun next(): BlockPos {
		count++
		index = (index + PRIME) % size
		var a = index.toInt()
		val z = a % (box.blockCountZ + 1)
		a /= box.blockCountZ
		val y = a % (box.blockCountY + 1)
		a /= box.blockCountY
		val x = a % (box.blockCountX + 1)
		return BlockPos(box.minX + x, box.minY + y, box.minZ + z)
	}
	
	companion object {
		const val PRIME = 0x7fff_ffffu
	}
}
//TODO
class SequentialBlockPosIterator(box: BlockBox,val way:(SequentialBlockPosIterator)->Unit= Way.XYZ) : AbstractBlockPosIterator(box) {
	var x = 0
	var y = 0
	var z = 0
	override fun next(): BlockPos {
		count++
		return BlockPos(box.minX + x, box.minY + y, box.minZ + z).also { way(this) }
	}
	enum class Way:(SequentialBlockPosIterator)->Unit{
		XYZ {
			override fun invoke(iter: SequentialBlockPosIterator) {
				iter.apply {
					x++
					if (x > box.blockCountX) {
						x = 0
						y++
						if (y > box.blockCountY) {
							y = 0
							z++
						}
					}
				}
			}
		},
		XZY {
			override fun invoke(iter: SequentialBlockPosIterator) {
				iter.apply {
					x++
					if (x > box.blockCountX) {
						x = 0
						z++
						if (z> box.blockCountZ) {
							z = 0
							y++
						}
					}
				}
			}
		},
	}
}
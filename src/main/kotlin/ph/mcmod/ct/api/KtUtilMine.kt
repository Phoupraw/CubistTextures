package ph.mcmod.ct.api

import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.item.Item
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.registry.Registry
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import ph.mcmod.ct.NAMESPACE

fun <T : Block> T.register(path: String): T = register(this, Registry.BLOCK, NAMESPACE, path)
fun <T : Item> T.register(path: String): T = register(this, Registry.ITEM, NAMESPACE, path)
fun <T : BlockEntity> BlockEntityType<T>.register(path: String): BlockEntityType<T> = register(this, Registry.BLOCK_ENTITY_TYPE, NAMESPACE, path)
fun <E : Entity, T : EntityType<in E>> T.register(path: String): T = register(this, Registry.ENTITY_TYPE, NAMESPACE, path)
fun <E> register(element: E, registry: Registry<in E>, namespace: String, path: String): E = Registry.register(registry, Identifier(namespace, path), element)

fun Box.rotate(shaft: Direction, times: Int = 1): Box = rotate(shaft.vector.toVec3d(), times)

fun Box.rotate(shaft: Vec3d, times: Int): Box = (1..times).fold(this) { box, _ -> box.rotate(shaft) }

fun Box.rotate(shaft: Vec3d): Box =
  offset(-0.5, -0.5, -0.5).run {
	  Box(Vec3d(minX, minY, minZ).let { it.crossProduct(shaft) + (it * shaft) },
		Vec3d(maxX, maxY, maxZ).let { it.crossProduct(shaft) + (it * shaft) })
  }.offset(0.5, 0.5, 0.5)

fun VoxelShape.rotate(shaft: Direction, times: Int = 1): VoxelShape {
	return boundingBoxes
	  .map { it.rotate(shaft, times) }
	  .map(VoxelShapes::cuboid)
	  .let { VoxelShapes.union(VoxelShapes.empty(), *(it.toTypedArray())) }
}

fun VoxelShape.rotate(initial: Direction, target: Direction, shaft: Direction = Direction.UP) = rotate(shaft, calcTimes(initial, target, shaft))
fun calcTimes(initial: Direction, target: Direction, shaft: Direction): Int {
	var v = initial.vector.toVec3d()
	val t = target.vector.toVec3d()
	val s = shaft.vector.toVec3d()
	var c = 0
	while (!(v approx t)) {
		v = v.crossProduct(s)
		c++
		require(c < 4) { "initial = ${initial}, target = ${target}, shaft = ${shaft}" }
	}
	return c
}
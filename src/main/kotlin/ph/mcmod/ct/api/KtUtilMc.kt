/**
 * 和Minecraft相关的函数、扩展函数、扩展属性等。
 */
@file:Suppress("unused")

package ph.mcmod.ct.api

import net.fabricmc.api.EnvType
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
import net.fabricmc.fabric.api.transfer.v1.storage.Storage
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext
import net.fabricmc.fabric.mixin.loot.table.LootSupplierBuilderHooks
import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.fluid.Fluid
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.StackReference
import net.minecraft.item.BucketItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootPool
import net.minecraft.loot.LootTable
import net.minecraft.loot.function.LootFunction
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.stat.Stat
import net.minecraft.stat.Stats
import net.minecraft.state.property.Properties
import net.minecraft.text.LiteralText
import net.minecraft.util.Identifier
import net.minecraft.util.math.*
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.math.cos
import kotlin.math.sin
import kotlin.reflect.KProperty

// Identifier
val Block.id: Identifier
	get() = Registry.BLOCK.getId(this)
val Item.id: Identifier
	get() = Registry.ITEM.getId(this)
val BlockEntityType<*>.id: Identifier
	get() = Registry.BLOCK_ENTITY_TYPE.getId(this)!!
val EntityType<*>.id: Identifier
	get() = Registry.ENTITY_TYPE.getId(this)
val Enchantment.id: Identifier
	get() = Registry.ENCHANTMENT.getId(this)!!
val Fluid.id: Identifier
	get() = Registry.FLUID.getId(this)
/** 给[Identifier]加前缀*/
fun Identifier.pre(prefix: String) = Identifier(this.namespace, prefix + this.path)
fun Identifier.preBlock() = pre("block/")
fun Identifier.preItem() = pre("item/")
fun Identifier.preBlockStates() = pre("blockstates/")
fun Identifier.preModels() = pre("models/")
fun Identifier.preTextures() = pre("textures/")
fun Identifier.preRecipes() = pre("recipes/")
fun Identifier.preCraftingShaped() = pre("crafting_shaped/")
fun Identifier.preStoneCutting() = pre("stone_cutting/")
fun Identifier.json() = this + ".json"
fun Identifier.png() = this + ".png"
/**给[Identifier]加后缀*/
operator fun Identifier.plus(suffix: String) = Identifier(this.namespace, this.path + suffix)
/**相当于`Identifier(this)`*/
fun String.id(): Identifier = Identifier(this)

// Vec3d
/**玩家射箭、投掷时，弹射物飞出的起点*/
var Entity.shootingPos: Vec3d
	get() = this.eyePos.add(0.0, -0.1, 0.0)
	set(value) = this.setPos(value.x, value.y + 0.1 - (this.eyeY - this.y), value.z)
/**
 * 由于[Entity.setPosition]和[Entity.getPos]的名称不统一，所以kotlin没法将其识别为属性，因此调用复合赋值运算符非常麻烦。
 *
 * 这个扩展属性可以方便调用复合赋值运算符。
 */
var Entity.coordinate: Vec3d
	get() = this.pos
	set(value) = this.setPosition(value)

operator fun Vec3d.plus(vec3d: Vec3d): Vec3d = this.add(vec3d)
operator fun Vec3d.unaryMinus(): Vec3d = this * -1
operator fun Vec3d.minus(vec3d: Vec3d): Vec3d = this.subtract(vec3d)
operator fun Vec3d.times(a: Double): Vec3d = this.multiply(a)
operator fun Vec3d.times(a: Number): Vec3d = this * a.toDouble()
/**不是叉乘或点乘，而是相同位置的数相乘。*/
operator fun Vec3d.times(vec3d: Vec3d): Vec3d = this.multiply(vec3d)
operator fun Vec3d.div(a: Double): Vec3d = this * (1 / a)
operator fun Vec3d.div(a: Number): Vec3d = this / a.toDouble()
/**直接转为[Vec3d]，不会向中心偏移0.5。*/
fun Vec3i.toVec3d(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())
/**有中心修正（向中心偏移0.5）的[toVec3d]。*/
fun Vec3i.toCenter(): Vec3d = Vec3d(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)
/**转为Kotlin的官方库里的[Triple]。*/
fun Vec3d.toTriple(): Triple<Double, Double, Double> = Triple(x, y, z)
/**两向量是否约等于。
 * @see Double.approx*/
infix fun Vec3d.approx(v: Vec3d) = this.x approx v.x && this.y approx v.y && this.z approx v.z
/**以[shaft]为轴，把`this`旋转[degree]的角度。*/
fun Vec3d.rotateBy(shaft: Vec3d, degree: Number): Vec3d {
	val k = shaft.normalize()
	val j = k.crossProduct(this).normalize()
	val i = j.crossProduct(k).normalize()
	val p = this.dotProduct(i)
	val h = this.dotProduct(k)
	val radian = Math.toRadians(degree.toDouble())
	return (i * cos(radian) + j * sin(radian)) * p + k * h
}
/**等于`rotateBy(shaft, degree)`
 * @see Vec3d.rotateBy*/
fun Iterable<Vec3d>.rotateBy(shaft: Vec3d, degree: Number) = this.map { it.rotateBy(shaft, degree) }
/**计算出把[origin]旋转到[target]所需的轴(`shaft`)和角度(`degree`)，再以此对`this`调用[Vec3d.rotateBy]`(shaft,degree)`。
 * @see Vec3d.rotateBy*/
fun Vec3d.rotateTo(origin: Vec3d, target: Vec3d): Vec3d {
	val shaft = origin.crossProduct(target)
	val radian = origin.dotProduct(target) / (origin.length() * target.length())
	return rotateBy(shaft, Math.toDegrees(radian))
}
/**等于`rotateTo(shaft, degree)`
 * @see Vec3d.rotateTo*/
fun Iterable<Vec3d>.rotateTo(origin: Vec3d, target: Vec3d) = this.map { it.rotateTo(origin, target) }
/**用于解构声明*/
operator fun Position.component1() = x
/**用于解构声明*/
operator fun Position.component2() = y
/**用于解构声明*/
operator fun Position.component3() = z
/**用于解构声明*/
operator fun Vec3i.component1() = x
/**用于解构声明*/
operator fun Vec3i.component2() = y
/**用于解构声明*/
operator fun Vec3i.component3() = z
/**如果[axis]是[Direction.Axis.X]，则返回[Position.getX]，依此类推。
 * @see Vec3d.getComponentAlongAxis*/
operator fun Position.get(axis: Direction.Axis) = Vec3d(x, y, z).getComponentAlongAxis(axis)
/**如果[axis]是[Direction.Axis.X]，则返回[Vec3i.x]，依此类推。
 * @see Vec3i.getComponentAlongAxis*/
operator fun Vec3i.get(axis: Direction.Axis) = this.getComponentAlongAxis(axis)
/**把[Position]视为一个三个元素的[Double]数组，获取索引为[index]的元素。*/
operator fun Position.get(index: Int) = when (index) {
	0 -> x
	1 -> y
	2 -> z
	else -> IndexOutOfBoundsException(index)
}
/**把[Vec3i]视为一个三个元素的[Int]数组，获取索引为[index]的元素。*/
operator fun Vec3i.get(index: Int) = when (index) {
	0 -> x
	1 -> y
	2 -> z
	else -> IndexOutOfBoundsException(index)
}
/**获取在`this`轴上，轴向方向为[direction]的[Direction]。*/
operator fun Direction.Axis.get(direction: Direction.AxisDirection) = when (this) {
	Direction.Axis.X -> if (direction == Direction.AxisDirection.NEGATIVE) Direction.WEST else Direction.EAST
	Direction.Axis.Y -> if (direction == Direction.AxisDirection.NEGATIVE) Direction.DOWN else Direction.UP
	Direction.Axis.Z -> if (direction == Direction.AxisDirection.NEGATIVE) Direction.NORTH else Direction.SOUTH
}

// Fabric Transfer API
val Item.variant: ItemVariant
	get() = ItemVariant.of(this)
val ItemStack.variant: ItemVariant
	get() = ItemVariant.of(this)
/**如果[StorageView.getAmount]小于等于0或[StorageView.isResourceBlank]，则为`true`。*/
val <T : TransferVariant<*>>StorageView<T>.empty
	get() = amount <= 0 || isResourceBlank
/**用于解构声明*/
operator fun <O> TransferVariant<O>.component1(): O = `object`
/**用于解构声明*/
operator fun TransferVariant<*>.component2(): NbtCompound? = nbt
/**用于解构声明*/
operator fun <R> ResourceAmount<R>.component1(): R = resource
/**用于解构声明*/
operator fun ResourceAmount<*>.component2(): Long = amount
/**如果[resource]和[amount]所代表的资源非空，则调用[Storage.insert]。*/
fun <T : TransferVariant<*>> Storage<T>.insertIfNotEmpty(resource: T, amount: Long, transactionContext: TransactionContext): Long {
	return if (!resource.isBlank && amount > 0)
		insert(resource, amount, transactionContext)
	else
		0
}
/**如果[resource]和[amount]所代表的资源非空，则调用[Storage.extract]。*/
fun <T : TransferVariant<*>> Storage<T>.extractIfNotEmpty(resource: T, amount: Long, transactionContext: TransactionContext): Long {
	return if (!resource.isBlank && amount > 0)
		extract(resource, amount, transactionContext)
	else
		0
}
/**如果[resource]和[amount]所代表的资源非空，则调用[StorageView.extract]。*/
fun <T : TransferVariant<*>> StorageView<T>.extractIfNotEmpty(resource: T, amount: Long, transactionContext: TransactionContext): Long {
	return if (!resource.isBlank && amount > 0)
		extract(resource, amount, transactionContext)
	else
		0
}
/**
 * 相当于`this.extract(this.resource, this.amount, [transactionContext]!)`。使用了[applyTransaction]。
 * @see StorageView.extract
 * @see StorageView.getResource
 * @see StorageView.getAmount
 */
fun <T : TransferVariant<*>> StorageView<T>.extractAll(transactionContext: TransactionContext? = null): Long {
	return applyTransaction(transactionContext) {
		if (!resource.isBlank && amount > 0)
			extract(resource, amount, it)
		else
			0
	}
}
/**
 * 相当于[this.extract(this.resource, [maxAmount], transactionContext)]。
 * @see StorageView.extract
 * @see StorageView.getResource
 */
fun <T> StorageView<T>.extract(maxAmount: Long, transactionContext: TransactionContext): Long = extract(resource, maxAmount, transactionContext)
/**立刻放入一个物品，当场调用[Transaction.commit]。*/
fun <T : TransferVariant<*>> Storage<T>.actuallyInsert(resource: T, amount: Long, transactionContext: TransactionContext? = null): Long {
	Transaction.openNested(transactionContext).use {
		return insertIfNotEmpty(resource, amount, it).apply { it.commit() }
	}
}
/**立刻抽取一个物品，当场调用[Transaction.commit]。*/
fun <T : TransferVariant<*>> Storage<T>.actuallyExtract(resource: T, amount: Long, transactionContext: TransactionContext? = null): Long {
	Transaction.openNested(transactionContext).use {
		return extractIfNotEmpty(resource, amount, it).apply { it.commit() }
	}
}

inline fun applyTransaction(transactionContext: TransactionContext? = null, transfer: (TransactionContext) -> Long): Long {
	val transaction = transactionContext ?: Transaction.openOuter()
	val amount = transfer(transaction)
	transactionContext ?: (transaction as Transaction).commit()
	return amount
}
/**
 * 相当于`(this as BucketItemAccessor).fabric_getFluid()`
 * @see BucketItemAccessor.fabric_getFluid
 */
val BucketItem.fluid: Fluid
	get() = (this as BucketItemAccessor).fabric_getFluid()

// Inventory ItemStack
/**限制数量，默认最大为[ItemStack.getMaxCount].(效果类似于[ItemStack.split]，只不过这个方法是不会返回新的[ItemStack]。）*/
fun ItemStack.limitCount(max: Int = this.maxCount): ItemStack {
	if (this.count > max)
		this.count = max
	return this
}
/**判断两摞物品能否堆叠，如果它们都是空的，则可堆叠，否则调用[ItemStack.canCombine]。*/
fun ItemStack.canCombine(another: ItemStack) = isEmpty || another.isEmpty || ItemStack.canCombine(this, another)
/**把[Inventory]视为[MutableCollection]<[ItemStack]>，返回[MutableCollection.iterator]。*/
operator fun Inventory.iterator(): MutableIterator<ItemStack> = listIterator()
/**把[Inventory]视为[MutableList]<[ItemStack]>，返回[MutableList.listIterator]。*/
fun Inventory.listIterator(): MutableListIterator<ItemStack> {
	return object : MutableListIterator<ItemStack> {
		var slotIndex = -1
		override fun hasNext(): Boolean = slotIndex < this@listIterator.size() - 1
		override fun next(): ItemStack = this@listIterator.getStack(++slotIndex)
		override fun nextIndex(): Int = slotIndex + 1
		override fun hasPrevious(): Boolean = slotIndex > 0
		override fun previous(): ItemStack = this@listIterator.getStack(--slotIndex)
		override fun previousIndex(): Int = slotIndex - 1
		override fun add(element: ItemStack) = throw UnsupportedOperationException()
		override fun remove() = throw UnsupportedOperationException()
		override fun set(element: ItemStack) = this@listIterator.setStack(slotIndex, element)
	}
}
/**把[Inventory]视为[MutableList]<[ItemStack]>，相当于此包中的[MutableList.forEach][ph.mcmod.ct.api.forEach]（Kotlin文档注释无法区分重载函数，所以这里无法导向此包中的[MutableList.forEach]）。*/
inline fun Inventory.forEach(consume: (index: Int, stack: ItemStack, remove: () -> Unit, set: (ItemStack) -> Unit) -> Unit) {
	val iterator = listIterator()
	while (iterator.hasNext())
		consume(iterator.nextIndex(), iterator.next(), iterator::remove, iterator::set)
}
/**把[Inventory]转换成[MutableList]<[ItemStack]>。注意不是包装而是转换！*/
fun Inventory.toMutableList(): MutableList<ItemStack> = MutableList(size(), this::getStack)

// NBT
/**把[NbtList]视为[MutableList]<[NbtCompound]>，相当于此包中的[MutableList.forEach][ph.mcmod.ct.api.forEach]（Kotlin文档注释无法区分重载函数，所以这里无法导向此包中的[MutableList.forEach]）。*/
inline fun NbtList.forCompound(consume: (index: Int, compound: NbtCompound, remove: () -> Unit, set: (NbtCompound) -> Unit) -> Unit) {
	val iterator = listIterator()
	while (iterator.hasNext())
		consume(iterator.nextIndex(), iterator.next() as NbtCompound, iterator::remove, iterator::set)
}
/**
 * 相当于`this.contains(key, NbtElement.COMPOUND_TYPE.toInt())`
 * @see NbtCompound.contains
 * @see NbtElement.COMPOUND_TYPE
 */
fun NbtCompound.containsCompound(key: String) = contains(key, NbtElement.COMPOUND_TYPE.toInt())
/**
 * 相当于`this.contains(key, NbtElement.INT_TYPE.toInt())`
 * @see NbtCompound.contains
 * @see NbtElement.INT_TYPE
 */
fun NbtCompound.containsInt(key: String) = contains(key, NbtElement.INT_TYPE.toInt())
/**
 * 相当于`this.contains(key, NbtElement.STRING_TYPE.toInt())`
 * @see NbtCompound.contains
 * @see NbtElement.STRING_TYPE
 */
fun NbtCompound.containsString(key: String) = contains(key, NbtElement.STRING_TYPE.toInt())
/**
 * 相当于`this.getList(key, NbtElement.COMPOUND_TYPE.toInt())`
 * @see NbtCompound.getList
 * @see NbtElement.COMPOUND_TYPE
 */
fun NbtCompound.getCompoundList(key: String): NbtList = getList(key, NbtElement.COMPOUND_TYPE.toInt())

// 杂项
/**免空指针检查的[EntityType.create]*/
fun <E : Entity> EntityType<E>.spawn(world: World): E = this.create(world) ?: throw IllegalStateException("$this.create($world) == null")

/**
 * 检查当前环境是物理客户端还是物理服务端，如果是物理客户端则执行[codeBlock]，否则什么也不做。
 *
 * 可以用于初始化仅客户端的资源。
 * @see EnvType.CLIENT
 */
inline fun runAtClient(codeBlock: () -> Unit) {
	if (FabricLoader.getInstance().environmentType == EnvType.CLIENT)
		codeBlock()
}
/**在客户端发送一句话，用于调试。*/
fun tellMe(any: Any?) {
	MinecraftClient.getInstance().player?.sendMessage(LiteralText(any.toString()), false)
}
/**类似[apply]的[tellMe]，可以无缝调用。*/
fun <T : Any?> T.tellMe(): T {
	tellMe(this)
	return this
}
/**由于无法直接获得函数类型的类对象（`((Int)->Unit)::class`会报语法错误），所以只能使用精炼类型来间接获得其类型。*/
inline fun <reified T> event(noinline invoker: (Array<T>) -> T): Event<T> = EventFactory.createArrayBacked(T::class.java, invoker)
/**创建或获取一个打开容器或工作方块的自定义统计。*/
fun createOpenStats(blockId: Identifier): Stat<Identifier> = Stats.CUSTOM.getOrCreateStat(blockId.pre("open_"))
/**播放水在下界被蒸发的音效和粒子效果。*/
fun playWaterEvaporation(world: World, pos: BlockPos) {
	val random = world.random
	world.playSound(
	  null,
	  pos,
	  SoundEvents.BLOCK_FIRE_EXTINGUISH,
	  SoundCategory.BLOCKS,
	  0.5f,
	  2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f
	)
	(world as? ServerWorld)?.spreadParticles(ParticleTypes.LARGE_SMOKE, false, pos.toCenter(), 0.3, 0.0, 8)
}

fun ServerWorld.spreadParticles(particle: ParticleEffect, alwaysSpawn: Boolean, pos: Position, offset: Position, speed: Double, count: Int) {
	server.playerManager.sendToAround(null,
	  pos.x,
	  pos.y,
	  pos.z,
	  if (alwaysSpawn) 16.0 else 128.0,
	  registryKey,
	  ParticleS2CPacket(particle,
		alwaysSpawn,
		pos.x,
		pos.y,
		pos.z,
		offset.x.toFloat(),
		offset.y.toFloat(),
		offset.z.toFloat(),
		speed.toFloat(),
		count
	  )
	)
}

fun ServerWorld.spreadParticles(particle: ParticleEffect, alwaysSpawn: Boolean, pos: Position, offset: Double, speed: Double, count: Int) {
	spreadParticles(particle, alwaysSpawn, pos, Vec3d(offset, offset, offset), speed, count)
}
/**用于属性委托*/
operator fun StackReference.getValue(thisRef: Any?, property: KProperty<*>): ItemStack = get()
/**用于属性委托*/
operator fun StackReference.setValue(thisRef: Any?, property: KProperty<*>, value: ItemStack) = set(value)
val LootTable.Builder.pools: List<LootPool>
	get() = (this as LootSupplierBuilderHooks).pools
val LootTable.Builder.functions: List<LootFunction>
	get() = (this as LootSupplierBuilderHooks).functions

fun BlockState.water(value: Boolean = true): BlockState = with(Properties.WATERLOGGED, value)

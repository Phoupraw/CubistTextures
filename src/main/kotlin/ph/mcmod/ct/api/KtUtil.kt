@file:Suppress("unused")

package ph.mcmod.ct.api

import net.minecraft.block.AbstractBlock
import java.lang.ref.Reference
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.nextTowards
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.random.Random
import kotlin.reflect.KProperty

// 容器
/**
 *[MutableMap.MutableEntry]非常离谱地没有重写[Map.Entry.value]为`var`，而是加了一个方法[MutableMap.MutableEntry.setValue]，这导致在使用自增、自减、复合赋值运算符的时候特别不方便，所以我写了这个扩展属性。
 */
var <K, V>  MutableMap.MutableEntry<K, V>.v: V
	get() = this.value
	set(value) {
		this.setValue(value)
	}
/**
 * [consumer]的参数名称和解释：
 * 1. `element` 元素
 * 2. `remove` 移除函数，调用它即调用[MutableIterator.remove]
 */
inline fun <T> MutableIterable<T>.forEach(consumer: (element: T, remove: () -> Unit) -> Unit) {
	val iterator = this.iterator()
	while (iterator.hasNext())
		consumer(iterator.next(), iterator::remove)
}
/**
 * [consumer]的参数名称和解释：
 * 1. `index` 索引
 * 2. `element` 元素
 * 3. `remove` 移除函数，调用它即调用[MutableListIterator.remove]
 * 4. `set` 设置函数，调用它即调用[MutableListIterator.set]
 */
inline fun <T> MutableList<T>.forEach(consumer: (index: Int, element: T, remove: () -> Unit, set: (T) -> Unit) -> Unit) {
	val iterator = this.listIterator()
	while (iterator.hasNext())
		consumer(iterator.nextIndex(), iterator.next(), iterator::remove, iterator::set)
}

inline fun <K, V> MutableMap<K, V>.forEach(consumer: (key: K, value: V, remove: () -> Unit, set: (V) -> V) -> Unit) {
	val iterator = iterator()
	while (iterator.hasNext()) {
		val entry = iterator.next()
		consumer(entry.key, entry.value, iterator::remove, entry::setValue)
	}
}

fun <T> Iterable<T>.permutation(): Iterable<List<T>> {
	val list = toList()
	return when (list.size) {
		0 -> emptyList()
		1 -> listOf(list)
		2 -> listOf(listOf(list[0], list[1]), listOf(list[1], list[0]))
		else -> list.asSequence().map { (list - it).permutation().map { it1 -> it1 + listOf(it) } }.flatMap { it }.asIterable()
	}
}
/**把一个迭代器包装成另一个迭代器。对迭代器内元素的处理不会立刻开始，而是每调用一次[Iterator.next]就处理一次。如果有些元素始终没有被迭代到，那么对于它们就永远不会调用[mapper]*/
inline fun <T, R> Iterator<T>.map(crossinline mapper: (T) -> R): Iterator<R> = object : Iterator<R> {
	override fun hasNext(): Boolean = this@map.hasNext()
	override fun next(): R = mapper(this@map.next())
}
/** 可变对 */
data class MutablePair<A, B>(var first: A, var second: B)
/** 创建可变对 */
infix fun <A, B> A.tm(second: B) = MutablePair(this, second)

// 杂项
/** 用于属性委托 */
operator fun <V> ThreadLocal<V>.getValue(thisRef: Any?, property: KProperty<*>): V = this.get()
/** 用于属性委托 */
operator fun <V> ThreadLocal<V>.setValue(thisRef: Any?, property: KProperty<*>, value: V) = this.set(value)
operator fun <V> Reference<V>.getValue(thisRef: Any?, property: KProperty<*>): V? = this.get()

fun <V> List<V>.provideDelegate(index: Int): ReadOnlyProperty<Any?, V> {
	return ReadOnlyProperty { _, _ -> this@provideDelegate[index] }
}

fun <V> MutableList<V>.provideDelegate(index: Int): ReadWriteProperty<Any?, V> {
	return object : ReadWriteProperty<Any?, V> {
		override fun getValue(thisRef: Any?, property: KProperty<*>): V = this@provideDelegate[index]
		
		override fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
			this@provideDelegate[index] = value
		}
	}
}
/** 同时使用两个[AutoCloseable]而只需一层函数 */
inline fun <A : AutoCloseable, B : AutoCloseable> use(a: A, b: B, run: (A, B) -> Unit) {
	a.use { b.use { run(a, b) } }
}
/**加载一个类而无需访问任何具体的字段或方法*/
fun Any.loadClass() {
	hashCode()
}
/**接收一个扩展函数的[use]*/
inline fun <T : AutoCloseable> T.useIt(consumer: T.() -> Unit) {
	use(consumer)
}
/**把一个浮点数转换成一个整数，规则如下：设a为[double]的小数部分，b为其整数部分，则有a的概率返回b+1，有1-a的概率返回b。*/
fun randomToLong(double: Double, randomDouble: Double = Random.nextDouble()): Long {
	val floor = floor(double)
	val decimal = double - floor
	return if (decimal < randomDouble)
		floor.toLong()
	else
		floor.toLong() + 1
}
/**永远返回`true`。当需要永远返回`true`的[Predicate]或`(Any?)->Boolean`函数类型时，可以直接引用该方法，避免创建新的lambda表达式。*/
@Suppress("UNUSED_PARAMETER")
fun <T> right(t: T) = true
fun Boolean.toInt() = if (this) 1 else 0
fun Int.toBoolean() = this != 0
operator fun Int.not() = if (toBoolean()) 0 else 1
inline fun <T, R> (() -> T).map(crossinline mapper: (T) -> R): () -> R = { mapper(this()) }
inline fun <T, R> ((R) -> Unit).map(crossinline mapper: (T) -> R): (T) -> Unit = { this(mapper(it)) }
infix fun Double.approx(d: Double) = abs(this - d) <= (this.nextTowards(d) - this) * 10
val ALWAYS_TRUE = AbstractBlock.ContextPredicate { _, _, _ -> true }
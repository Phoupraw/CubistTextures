package ph.mcmod.ct.api

import java.io.Serializable
import kotlin.math.exp

class FractionMatrix(private val values: Array<FractionArray>) : Serializable, Cloneable, Comparable<FractionMatrix>, Iterable<Fraction>, RandomAccess {
	val r: Int = values.size
	val c: Int = values.firstOrNull()?.size ?: 0
	val size = r * c
	val isSquare = r == c
	val isZero by lazy { this == zeros(r, c) }
	val isIdentity by lazy { isDiag && this == identity(r) }
	val isDiag by lazy {
		if (!isSquare) return@lazy false
		for (i in 0 until r) for (j in 0 until c) if (i != j && !this[i, j].isZero) return@lazy false
		true
	}
	val isSymmetrical by lazy {
		if (!isSquare) return@lazy false
		if (isDiag) return@lazy true
		for (i in 0 until r) for (j in i + 1 until c) if (this[i, j] != this[j, i]) return@lazy false
		true
	}
	val t by lazy { if (isSymmetrical) this else of(c, r) { i, j -> this[j, i] } }
	val hashCode0 by lazy { values.contentDeepHashCode() }
	val string0 by lazy {
		val lengths = (0 until c).map { j -> this[null, j].map { it.toString().length }.maxOrNull() ?: error("j=$j r=$r c=$c") }
		values.joinToString("\n ", "[", "]") { l ->
			l.withIndex().joinToString(" ", "[", "]") { "%${lengths[it.index]}s".format(it.value) }
		}
	}
	val simplest by lazy {
		if (isZero || isIdentity) return@lazy this
		if (isDiag) return@lazy identity(r)
		var m = this
		var i0 = 0
		var j = 0
		var detK = 1 f 1
		while (j < c) {
			while ((0 until r).all { m[it, j].isZero }) j++
			if (m[i0, j].isZero)
				for (i in i0 + 1 until r)
					if (!m[i, j].isZero) {
						m = m.swapLines(i0, i)
						detK *= -1
						break
					}
			m = m.timesLine(i0, (1 / m[i0, j]).also { detK *= it })
			for (i in 0 until r) if (i != i0 && !m[i, j].isZero) m = m.plusLine(i0, -m[i, j] / m[i0, j], i)
			if (++i0 >= r) break
			do j++ while (j < c && (i0 until r).all { m[it, j].isZero })
		}
		rank0 = i0
		det0 = detK
		m
	}
	private var rank0: Int = -1
	val rank by lazy {
		if (isZero) return@lazy 0
		if (isIdentity) return@lazy r
		simplest
		rank0
	}
	val canInv get() = isSquare && rank == r
	val inv by lazy {
		require(canInv) { "canInv=false rank=$rank this=\n$this" }
		if (isIdentity) return@lazy this
		if (isDiag) return@lazy 1 / this
		(this right identity(r)).simplest[null, c, 2 * c]
	}
	val isVector = r == 1 || c == 1
	private var det0 = 0 f 1
	val det by lazy {
		require(isSquare) { "isSquare=false r=$r c=$c this=\n$this" }
		if (!canInv) return@lazy 0
		if (isIdentity) return@lazy 1
		det0
	}
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		other as FractionMatrix
		return values contentDeepEquals other.values
	}
	
	override fun hashCode(): Int = hashCode0
	override fun toString(): String = string0
	public override fun clone(): FractionMatrix = this// DoubleMatrix(Array(r) { values[it].copyOf() })
	override operator fun compareTo(other: FractionMatrix): Int {
		for ((a, b) in (asSequence() zip other.asSequence()))
			if (a != b) return a.compareTo(b)
		return size.compareTo(other.size)
	}
	
	override fun iterator() = values.asSequence().flatMap(FractionArray::asSequence).iterator()
	
	operator fun get(i: Int) = this[i / c, i % c]
	operator fun get(i: Int, j: Int): Fraction {
		require(i in 0 until r) { "i !in 0 until r  i=$i r=$r" }
		require(j in 0 until c) { "j !in 0 until c  j=$j c=$c" }
		return values[i][j]
	}
	
	operator fun get(i: Int, @Suppress("UNUSED_PARAMETER") j: Nothing?) = this[i, i + 1, null]
	operator fun get(@Suppress("UNUSED_PARAMETER") i: Nothing?, j: Int) = this[null, j, j + 1]
	operator fun get(i1: Int, i2: Int, @Suppress("UNUSED_PARAMETER") j: Nothing?) = sub(i1 = i1, i2 = i2)
	operator fun get(@Suppress("UNUSED_PARAMETER") i: Nothing?, j1: Int, j2: Int) = sub(j1 = j1, j2 = j2)
	fun sub(i1: Int = 0, i2: Int = r, j1: Int = 0, j2: Int = c) = of(i2 - i1, j2 - j1) { i, j ->
		try {
			this[i + i1, j + j1]
		} catch (e: Throwable) {
			throw RuntimeException("i1=$i1 i2=$i2 j1=$j1 j2=$j2 i=$i j=$j", e)
		}
	}
	
	infix fun right(matrix: FractionMatrix) = of(r, c + matrix.c) { i, j -> if (j < c) this[i, j] else matrix[i, j - c] }
	infix fun down(matrix: FractionMatrix) = of(r + matrix.r, c) { i, j -> if (i < c) this[i, j] else matrix[i - r, j] }
	
	operator fun plus(matrix: FractionMatrix) = apply(matrix, Fraction::plus)
	operator fun plus(number: Number) = apply { it + number.toFraction() }
	operator fun minus(matrix: FractionMatrix) = apply(matrix, Fraction::minus)
	operator fun minus(number: Number) = apply { it - number.toFraction() }
	operator fun times(matrix: FractionMatrix) = of(r, matrix.c) { i, j -> (0 until c).fold(0 f 1) { a, k -> a + this[i, k] * matrix[k, j] } }
	operator fun times(number: Number) = apply { it * number.toFraction() }
	operator fun div(matrix: FractionMatrix) = (this right matrix).let { it[null, it.c - 1] }
	operator fun div(number: Number) = apply { it / number.toFraction() }
	infix fun pow(int: Int): FractionMatrix {
		require(isSquare) { "isSquare=false r=$r c=$c this=\n$this" }
		return (1..int).fold(identity(r)) { m, _ -> m * this }
	}
	
	infix fun timesValues(matrix: FractionMatrix) = apply(matrix, Fraction::times)
	infix fun divValues(matrix: FractionMatrix) = apply(matrix, Fraction::div)
	infix fun powValues(number: Number) = apply { it pow number.toLong() }
	operator fun unaryMinus() = this * -1
	
	fun swapLines(i1: Int, i2: Int): FractionMatrix {
		return of(r, c) { i, j ->
			when (i) {
				i1 -> this[i2, j]
				i2 -> this[i1, j]
				else -> this[i, j]
			}
		}
	}
	
	fun swapColumns(j1: Int, j2: Int): FractionMatrix {
		return of(r, c) { i, j ->
			when (j) {
				j1 -> this[i, j2]
				j2 -> this[i, j1]
				else -> this[i, j]
			}
		}
	}
	
	fun timesLine(i1: Int, number: Number) = plusLine(i1, number.toFraction() - 1, i1)
	fun timesColumn(j1: Int, number: Number) = plusColumn(j1, number.toFraction() - 1, j1)
	fun plusLine(i1: Int, number: Number, i2: Int): FractionMatrix {
		return of(r, c) { i, j ->
			if (i == i2) this[i1, j] * number.toFraction() + this[i, j]
			else this[i, j]
		}
	}
	
	fun plusColumn(j1: Int, number: Number, j2: Int): FractionMatrix {
		return of(r, c) { i, j ->
			if (j == j2) this[i, j1] * number.toFraction() + this[i, j]
			else this[i, j]
		}
	}
	
	infix fun cross(vector: FractionMatrix): FractionMatrix {
		require(this.isVector) { "this.isVector=false r=$r c=$c this=\n$this" }
		require(vector.isVector) { "vector.isVector=false r=$vector.r c=$vector.c vector=\n$vector" }
		return FractionMatrix()[
		  this[1] * vector[2] - this[2] * vector[1],
		  this[2] * vector[0] - this[0] * vector[2],
		  this[0] * vector[1] - this[1] * vector[0]]().t
	}
	
	fun derivate(): FractionMatrix {
		require(isVector) { "isVector=false r=$r c=$c this=\n$this" }
		return of(size - 1, 1) { i, _ -> this[i + 1] * (i + 1) }.t
	}
	
	fun reshape(r: Int, c: Int) = of(r, c) { i, j -> this[i * this.c + j] }
	
	inline fun apply(mapper: (Fraction) -> Fraction) = of(r, c) { i, j -> mapper(this[i, j]) }
	inline fun apply(matrix: FractionMatrix, mapper: (Fraction, Fraction) -> Fraction) = of(r, c) { i, j -> mapper(this[i, j], matrix[i, j]) }
	
	companion object {
		
		operator fun invoke() = Builder()
		fun zeros(r: Int, c: Int = r) = of(r, c) { _, _ -> 0 }
		fun ones(r: Int, c: Int = r) = zeros(r, c) + 1
		fun identity(o: Int) = of(o, o) { i, j -> if (i == j) 1.0 else 0.0 }
		inline fun of(r: Int, c: Int = r, mapper: (Int, Int) -> Number): FractionMatrix {
			return FractionMatrix(Array(r) { i ->
				Array(c) { j ->
					try {
						mapper(i, j).toFraction()
					} catch (e: Throwable) {
						throw RuntimeException("r=$r c=$c i=$i j=$j", e)
					}
				}
			})
		}
		
		inline operator fun invoke(r: Int, c: Int = r, mapper: (Int, Int) -> Number) = of(r, c, mapper)
		
		@JvmStatic
		fun main(args: Array<String>) {
//			val a = 34572 f 123456789
//			println(a.toDouble())
//			println(a.round((100000).toBigInteger()).toDouble())
			val n = 100.0
			val r = Fraction.exp(n f 1)
			println(r)
			println(r.toDouble())
			println(exp(n))
		}
	}
	
	class Builder {
		val rows = mutableListOf<FractionArray>()
		val c get() = rows.firstOrNull()?.size ?: -1
		fun addLine(row: FractionArray): Builder {
			require(rows.isEmpty() || row.size == c) {
				"row!=c  row=${row.contentToString()} c=$c rows=${rows.map(FractionArray::contentToString)}"
			}
			rows += row
			return this
		}
		
		fun addLine(row: Array<out Number>): Builder = addLine(row.map { it.toFraction() }.toTypedArray())
		operator fun get(vararg row: Number): Builder = addLine(row)
		operator fun invoke() = FractionMatrix(rows.toTypedArray())
	}
}
typealias FractionArray = Array<Fraction>

operator fun Number.plus(matrix: FractionMatrix) = matrix + this
operator fun Number.minus(matrix: FractionMatrix) = this + -matrix
operator fun Number.times(matrix: FractionMatrix) = matrix * this
operator fun Number.div(matrix: FractionMatrix) = this * matrix.apply { 1 / it }

object M {
	operator fun invoke() = FractionMatrix()
	operator fun get(vararg row: Number) = FractionMatrix().addLine(row)
}

interface Calculable<T> {
	val isZero: Boolean
	val isOne: Boolean
	operator fun plus(a: T): T
	operator fun minus(a: T): T
	operator fun times(a: T): T
	operator fun div(a: T): T
	operator fun unaryMinus(): T
}
@JvmInline
value class CDouble(val v: Double) : Calculable<CDouble> {
	override val isZero: Boolean get() = v == 1.0
	override val isOne: Boolean get() = v == 0.0
	override fun plus(a: CDouble): CDouble = CDouble(v + a.v)
	override fun minus(a: CDouble): CDouble = CDouble(v - a.v)
	override fun times(a: CDouble): CDouble = CDouble(v * a.v)
	override fun div(a: CDouble): CDouble = CDouble(v / a.v)
	override fun unaryMinus(): CDouble = CDouble(-v)
}
package ph.mcmod.ct.api

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import kotlin.math.abs
import java.math.BigInteger.ONE as I_ONE
import java.math.BigInteger.ZERO as I_ZERO

class Fraction private constructor(val nu: BigInteger, val de: BigInteger) : Number(), Comparable<Number> {
	val sign get() = nu.signum()
	val isZero = nu.isZero && !de.isZero
	val isNaN = nu.isZero && de.isZero
	val isInf = nu.isZero && !de.isZero
	val isSquare by lazy { sign >= 0 && (isZero || nu.sqrtAndRemainder()[1] == I_ZERO && de.sqrtAndRemainder()[1] == I_ZERO) }
	val string0 by lazy {
		if (this.isZero) return@lazy "0"
		if (this == ONE) return@lazy "1"
		if (this.isNaN) return@lazy "NaN"
		if (this.isInf) return@lazy if (sign > 0) "INF" else "-INF"
		if (de.isOne) return@lazy nu.toString()
		"$nu/$de"
	}
	val hashCode0 by lazy { nu.hashCode() xor de.hashCode() }
	val isInt = de.isOne
	val isOne = nu == I_ONE && de == I_ONE
	
	operator fun plus(addend: Fraction): Fraction {
		return Fraction(
		  nu * addend.de + addend.nu * de,
		  de * addend.de
		).reduce()
	}
	
	operator fun plus(addend: Number): Fraction = this + addend.toFraction()
	
	operator fun inc(): Fraction = this + 1
	
	operator fun unaryMinus(): Fraction = Fraction(-nu, de).reduce()
	
	operator fun minus(subtrahend: Fraction): Fraction = this + -subtrahend
	
	operator fun minus(subtrahend: Number): Fraction = this - subtrahend.toFraction()
	
	operator fun dec(): Fraction = this - 1
	
	operator fun times(factor: Fraction): Fraction = Fraction(nu * factor.nu, de * factor.de).reduce()
	
	operator fun times(factor: Number): Fraction = this * factor.toFraction()
	
	operator fun div(divisor: Fraction): Fraction = Fraction(nu * divisor.de, de * divisor.nu).reduce()
	
	operator fun div(divisor: Number): Fraction = this / divisor.toFraction()
	
	operator fun compareTo(other: Fraction): Int = (this - other).sign
	
	override operator fun compareTo(other: Number): Int = this.compareTo(other.toFraction())
	
	fun reduce(): Fraction {
		return when {
			de.isZero -> when {
				nu.isZero -> NAN
				nu.signum() > 0 -> INF
				else -> N_INF
			}
			nu.isZero -> ZERO
			nu == de -> ONE
			else -> {
				val cd = commonDivisor(nu.abs(), de.abs())
				Fraction((if (nu.signum() * de.signum() > 0) nu.abs() else -nu.abs()) / cd, de.abs() / cd)
			}
		}
	}
	
	override fun toString(): String = string0
	
	override fun hashCode(): Int = hashCode0
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		return if (other !is Fraction) {
			if (other is Number) this == other.toFraction()
			else false
		} else {
			val t = this.reduce()
			val o = other.reduce()
			t.nu == o.nu && t.de == o.de
		}
	}
	
	override fun toByte(): Byte = toLong().toByte()
	
	override fun toShort(): Short = toLong().toShort()
	
	override fun toChar(): Char = toInt().toChar()
	
	override fun toInt(): Int = toLong().toInt()
	
	override fun toLong(): Long = (nu / de).toLong()
	
	override fun toFloat(): Float = toDouble().toFloat()
	
	override fun toDouble(): Double = nu.toBigDecimal().divide(de.toBigDecimal(), MathContext.DECIMAL32).toDouble()
	infix fun pow(exp: BigInteger): Fraction {
		if (exp.isZero) return ONE
		val exp1 = exp.abs()
		return (I_ONE..exp1).asIterable().fold(ONE) { f, _ -> f * this }.let { if (exp.signum() > 0) it else 1 / it }
	}
	
	infix fun pow(long: Long) = this pow long.toBigInteger()
	infix fun pow(int: Int) = this pow int.toBigInteger()
	infix fun pow(x: Fraction): Fraction {
		if (x.isZero) return ONE
		return exp(x * ln(this))
	}
	
	fun limitDe(denominator: BigInteger = I_ONE) = if (de <= denominator) this else Fraction(((nu * denominator).toBigDecimal() / this.de.toBigDecimal()).round(MathContext.UNLIMITED).toBigIntegerExact(), denominator).reduce()
	fun toBigInteger() = limitDe().nu
	fun abs() = if (sign >= 0) this else this * -1
	fun limitLength(length: Int): Fraction {
		return if (toString().length <= length) this
		else {
			var minDe = I_ONE
			var maxDe = de
			var midDe: BigInteger
//			var prev = 0
			while (true) {
				midDe = minDe + (maxDe - minDe) / 100
//				println(">$minDe $maxDe $midDe")
//				Thread.sleep(100)
				if (midDe == minDe) break
				val q = limitDe(midDe)
				val l = q.toString().length
				if (l > length) maxDe = midDe
				else minDe = midDe
			}
			limitDe(minDe)
		}
	}
	
	companion object {
		val ZERO = Fraction(I_ZERO, I_ONE)
		val ONE = Fraction(I_ONE, I_ONE)
		val NAN = Fraction(I_ZERO, I_ZERO)
		val INF = Fraction(I_ONE, I_ZERO)
		val N_INF = -INF
		
		@JvmStatic
		fun commonDivisor(a: BigInteger, b: BigInteger): BigInteger {
			var x = a
			var y = b
			while (!y.isZero) {
				val t = x
				x = y
				y = t % y
			}
			return x
		}
		//		fun commonDivisor(a: BigDecimal, b: BigDecimal): BigInteger {
//			var x = a
//			var y = b
//			while (y != BigInteger.ZERO) {
//				val t = x
//				x = y
//				y = t % y
//			}
//			return TODO()
//		}
		operator fun invoke(numerator: Number, denominator: Number) = of(numerator, denominator)
		operator fun invoke(numerator: BigInteger, denominator: BigInteger) = of(numerator, denominator)
		@JvmStatic
		fun of(numerator: Number, denominator: Number): Fraction = numerator.toFraction() / denominator
		@JvmStatic
		fun of(numerator: BigInteger, denominator: BigInteger): Fraction = Fraction(numerator, denominator).reduce()
		
		/**
		 * 被of(Number)调用
		 */
		@JvmStatic
		fun of(value: BigDecimal): Fraction {
			val scale = value.scale()
			return of(value.scaleByPowerOfTen(scale).toBigIntegerExact(), BigInteger.TEN.pow(scale))
		}
		
		/**
		 * 被Number.toFraction()调用
		 */
		@JvmStatic
		fun of(value: Number): Fraction = value.toFraction()
		
		@JvmStatic
		fun min(a: Fraction, b: Fraction): Fraction = if (a > b) a else b
		@JvmStatic
		fun min(a: Number, b: Fraction): Fraction = min(a.toFraction(), b)
		@JvmStatic
		fun min(a: Fraction, b: Number): Fraction = min(a, b.toFraction())
		@JvmStatic
		fun max(a: Fraction, b: Fraction): Fraction = if (a > b) a else b
		@JvmStatic
		fun max(a: Number, b: Fraction): Fraction = max(a.toFraction(), b)
		@JvmStatic
		fun max(a: Fraction, b: Number): Fraction = max(a, b.toFraction())
		val DEFAULT_DE: BigInteger = BigInteger.TEN.pow(23)
		const val DEFAULT_LENGTH = 23
		inline fun powerSum(x: Fraction, begin: Int = 0, length: Int = DEFAULT_LENGTH, const: (Int) -> Fraction): Fraction = sum(begin, length) { const(it) * x.pow(it) }
		
		inline fun sum(begin: Int = 0, length: Int = DEFAULT_LENGTH, term: (Int) -> Fraction): Fraction {
			var sum = ZERO
			var prev = NAN
			var i = begin
			var warned = false
			while (prev.isNaN || !(sum - prev).isZero) {
				prev = sum
				sum = (sum + term(i)).limitLength(length)
				println(sum)
				i++
				if (i > 1000_0000) {
					if (!warned) {
						System.err.println("[WARN]i>1000_0000 begin=$begin rounding=$length sum=$sum prev=$prev")
						warned = true
					}
					require(i <= 10_0000_0000) { "i>10_0000_0000 begin=$begin rounding=$length sum=$sum prev=$prev" }
				}
			}
			return sum
		}
		
		fun exp(x: Fraction) = powerSum(x) { 1 / it.factorial().toFraction() }
		fun ln(x: Fraction): Fraction = 2 * sum { ((x - 1) / (x + 1)).pow(it * 2 + 1) / (it * 2 + 1) }
	}
}

val Number.isInLong: Boolean get() = this is Byte || this is Short || this is Int || this is Long
fun Number.toFraction(): Fraction = when {
	this is Fraction -> this
	this is BigInteger -> Fraction(this, I_ONE)
	this is BigDecimal -> Fraction.of(this)
	this.isInLong -> Fraction.of(this.toLong().toBigInteger(), I_ONE)
	else -> Fraction.of(this.toDouble().toBigDecimal())
}

operator fun Number.plus(other: Fraction): Fraction = other + toFraction()
operator fun Number.minus(other: Fraction): Fraction = this + -other
operator fun Number.times(other: Fraction): Fraction = other * this.toFraction()
operator fun Number.div(other: Fraction): Fraction = this.toFraction() / other
operator fun Number.compareTo(other: Fraction): Int = -other.compareTo(this)
val BigInteger.isOne get() = this == I_ONE
val Number.isOne: Boolean
	get() = when {
		this is Fraction -> isOne
		this is BigInteger -> isOne
		this is BigDecimal -> this == BigDecimal.ONE
		this.isInLong -> this.toLong() == 1L
		else -> this.toDouble() == 1.0
	}
val BigInteger.isZero get() = this.signum() == 0
val Number.isZero: Boolean
	get() = when {
		this is Fraction -> isZero
		this is BigInteger -> isZero
		this is BigDecimal -> signum() == 0
		this.isInLong -> this.toLong() == 0L
		else -> abs(this.toDouble()) <= 0
	}

infix fun Int.f(int: Int) = Fraction(this, int)
infix fun Number.f(number: Number) = Fraction(this, number)
fun ClosedRange<BigInteger>.asIterable() = Iterable {
	object : Iterator<BigInteger> {
		var n = start
		override fun hasNext(): Boolean = n < endInclusive
		override fun next(): BigInteger = n++
	}
}

fun Int.factorial(): BigInteger = (2..this).fold(I_ONE) { acc, i -> acc * i.toBigInteger() }
operator fun BigInteger.div(n: Int) = this / n.toBigInteger()
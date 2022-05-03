package ph.mcmod.ct

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO
import kotlin.io.path.*

private object DevScripts {
	@JvmStatic
	fun main(args: Array<String>) {
		Files.newDirectoryStream(Path.of("src\\main\\resources\\assets\\cubist_texture\\textures\\block")).forEach { generateTrapdoor0(it) }
//
//		generateTrapdoor1(Path.of("D:\\CCC\\Documents\\01_Programming\\Fabric Mod\\Cubist Textures\\src\\main\\resources\\assets\\cubist_texture\\test\\acacia_boat_wood.png"))
	}
	
	fun montage() {
		val dir = Path.of("run\\screenshots")
		val width = 486
		val height0 = 270
		val images = Files.newDirectoryStream(dir).map { ImageIO.read(it.inputStream()).getSubimage(690, 354, width, height0) }
		val rgbArray = images.flatMap { it.getRGB(0, 0, width, height0, null, 0, width).asIterable() }.toIntArray()
		val image = BufferedImage(width, height0 * images.size, BufferedImage.TYPE_INT_RGB)
		image.setRGB(0, 0, image.width, image.height, rgbArray, 0, width)
		ImageIO.write(image, "PNG", dir.resolve("longlong.png").outputStream())
	}
	
	val PATTERNS = listOf("oak", "jungle", "acacia", "crimson", "warped")
	fun generateTrapdoor0(path: Path) {
		if (path.extension != "png") return
		val rawTexture = ImageIO.read(path.inputStream())
		val width = rawTexture.width
		val height = rawTexture.height
		val framesCount = height / 16
		val textureses = (0 until framesCount).map { rawTexture.getSubimage(0, it * 16, width, 16) }.map { generateTrapdoor1(it) }
		val dir = path.parent.resolve(path.nameWithoutExtension + "_trapdoor").createDirectories()
		for ((j, pattern) in PATTERNS.withIndex()) {
			val texture = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
			val argbArray = (0 until framesCount).map { textureses[it][j] }.flatMap { it.getRGB(0, 0, width, 16, null, 0, width).asIterable() }.toIntArray()
			texture.setRGB(0, 0, width, height, argbArray, 0, width)
			ImageIO.write(texture, "PNG", dir.resolve("${pattern}.png").outputStream())
			if (framesCount > 1) {
				Files.copy(path.resolveSibling(path.name + ".mcmeta"), dir.resolve("${pattern}.png.mcmeta"), StandardCopyOption.REPLACE_EXISTING)
			}
		}
		
	}
	
	fun generateTrapdoor1(rawTexture: BufferedImage): List<BufferedImage> {
		val width = rawTexture.width
		val height = rawTexture.height
		
		val oak = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		oak.setRGB(0, 0, width, height, rawTexture.getRGB(0, 0, width, height, null, 0, width), 0, width)
		for ((x0, y0) in arrayOf(3 to 3, 10 to 3, 3 to 10, 10 to 10)) {
			for (i in 0..2) for (j in 0..2) oak.setRGB(x0 + i, y0 + j, 0)
			for (i in 0..4) oak.setRGB(x0 - 1, y0 - 1 + i, rawTexture.getRGB(width - 1, height - 1 - i))
			for (i in 0..3) oak.setRGB(x0 + i, y0 - 1, rawTexture.getRGB(width - 2 - i, height - 1))
		}
		
		val jungle = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		jungle.setRGB(0, 0, width, height, rawTexture.getRGB(0, 0, width, height, null, 0, width), 0, width)
		listOf(5, 3, 4, 4, 5, 4, 3, 5, 4, 5, 5, 5, 3, 6, 4, 6, 5, 6,
		  7, 3, 7, 4, 7, 5, 7, 6,
		  4, 9, 5, 9, 4, 10, 5, 10, 5, 11,
		  7, 9, 7, 10, 7, 11)
		  .chunked(2)
		  .flatMap { listOf(it, listOf(15 - it[0], it[1])) }
		  .forEach { (x, y) -> jungle.setRGB(x, y, 0) }
		listOf(5, 2, 6, 2, 6, 3, 6, 6, 8, 2, 9, 2, 9, 3, 9, 6, 11, 2, 12, 2,
		  3, 8, 3, 9, 4, 8, 4, 11, 5, 8, 6, 8, 6, 9, 6, 12, 8, 8, 9, 8, 9, 9, 9, 12, 11, 8, 11, 11)
		  .chunked(2)
		  .forEach { (x, y) -> jungle.setRGB(x, y, rawTexture.getRGB(width - 1 - (x + y) % 5, height - 1)) }
		
		val acacia = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		acacia.setRGB(0, 0, width, height, rawTexture.getRGB(0, 0, width, height, null, 0, width), 0, width)
		for (x0 in intArrayOf(2, 7, 12)) {
			val y0 = 3
			for (i in 0..1) for (j in 0..9) acacia.setRGB(x0 + i, y0 + j, 0)
			if (x0 != 2) for (i in 0..10) acacia.setRGB(x0 - 1, y0 - 1 + i, rawTexture.getRGB(width - 1, height - 1 - i))
			for (i in 0..1) acacia.setRGB(x0 + i, y0 - 1, rawTexture.getRGB(width - 2 - i, height - 1))
		}
		
		val crimson = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		crimson.setRGB(0, 0, width, height, rawTexture.getRGB(0, 0, width, height, null, 0, width), 0, width)
		for (y0 in intArrayOf(3, 7, 8, 12)) for (i in 0..9) crimson.setRGB(3 + i, y0, 0)
		for ((y0, h) in arrayOf(3 to 1, 7 to 2, 12 to 1)) {
			for (i in 0..9) for (j in 0 until h) crimson.setRGB(3 + i, y0 + j, 0)
			for (i in -1..10) for (j in -1 until h + 1) if (i == -1 || i == 10 || j == -1 || j == h)
				crimson.setRGB(3 + i, y0 + j, rawTexture.getRGB(width - (y0 + h + i + j) % 6 - 1, height - 1))
		}
		
		val warped = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
		warped.setRGB(0, 0, width, height, rawTexture.getRGB(0, 0, width, height, null, 0, width), 0, width)
		listOf(3, 3, 3, 4, 3, 5, 4, 5, 4, 6, 5, 6, 4, 7, 5, 7, 3, 8, 4, 8, 3, 9, 3, 10, 3, 11, 3, 12,
		  6, 3, 7, 3, 7, 4, 8, 4, 7, 5, 8, 5, 7, 6, 8, 6, 7, 7, 8, 7, 9, 7, 7, 8, 8, 8, 9, 8, 7, 9, 8, 9, 9, 9, 6, 10, 7, 10, 8, 10, 6, 11, 7, 11, 8, 11, 7, 12, 8, 12,
		  11, 3, 12, 3, 10, 4, 11, 4, 12, 4, 11, 5, 11, 6, 11, 7, 11, 8, 11, 9, 11, 10, 12, 10, 11, 11, 12, 11, 10, 12, 12, 12)
		  .chunked(2)
		  .forEach { (x, y) -> warped.setRGB(x, y, 0) }
		listOf(2, 2, 3, 2, 2, 3, 4, 4, 2, 5, 5, 5, 2, 6, 3, 6, 3, 7, 2, 9, 2, 10, 2, 11, 2, 12, 4, 12, 2, 13, 3, 13, 4, 13,
		  5, 2, 6, 2, 7, 2, 8, 2, 9, 6, 6, 9, 5, 10, 6, 12, 7, 13, 8, 13,
		  11, 2, 12, 2, 10, 3, 13, 3, 12, 5, 12, 7, 12, 8, 13, 8, 12, 9, 10, 11, 13, 11, 13, 12, 11, 13, 12, 13, 13, 13)
		  .chunked(2)
		  .forEach { (x, y) -> warped.setRGB(x, y, rawTexture.getRGB(width - 1 - (x + y) % 6, height - 1)) }
		
		return listOf(oak, jungle, acacia, crimson, warped)
	}
}
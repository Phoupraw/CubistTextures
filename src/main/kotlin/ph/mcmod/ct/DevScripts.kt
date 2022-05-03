package ph.mcmod.ct

import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

private object DevScripts {
	@JvmStatic
	fun main(args: Array<String>) {
		val dir = Path.of("D:\\CCC\\Documents\\01_Programming\\Fabric Mod\\Cubist Textures\\run\\screenshots")
		val width = 486
		val height0 = 270
		val images = Files.newDirectoryStream(dir).map { ImageIO.read(it.inputStream()).getSubimage(690, 354, width, height0) }
		val rgbArray = images.flatMap { it.getRGB(0, 0, width, height0, null, 0, width).asIterable() }.toIntArray()
		val image = BufferedImage(width, height0 * images.size, BufferedImage.TYPE_INT_RGB)
		image.setRGB(0, 0, image.width, image.height, rgbArray, 0, width)
		ImageIO.write(image, "PNG", dir.resolve("longlong.png").outputStream())
	}
}
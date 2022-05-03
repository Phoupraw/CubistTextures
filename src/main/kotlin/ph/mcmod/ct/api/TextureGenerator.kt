package ph.mcmod.ct.api

import java.awt.AlphaComposite
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

object TextureGenerator {
	val VANILLA_DIR = Path("D:\\Entertainments\\Minecraft 最多版本\\.minecraft\\versions\\1.18.1\\1.18.1.jar解压\\assets\\minecraft\\textures\\block")
	val MY_DIR = Path("./src/main/resources/assets/cubist_texture/textures/block")
	val PLANKS_TYPES = listOf("oak", "birch", "spruce", "jungle", "acacia", "dark_oak", "crimson", "warped")
	val DECORATIONS = listOf(
	  "" to (subImage("barrel_side", 0, 3, 16, 2) to subImage("barrel_top", 3, 8, 2, 2)),
	  "double_layers_" to (subImage("polished_granite", 0, 14, 16, 2) to subImage("polished_granite", 9, 13, 2, 2)),
	  "three_layers_" to (subImage("polished_deepslate", 0, 12, 16, 2) to subImage("polished_deepslate", 9, 13, 2, 2)),
	  "four_layers_" to (subImage("nether_bricks", 0, 14, 16, 2) to subImage("nether_bricks", 9, 13, 2, 2)),
	  "five_layers_" to (subImage("end_stone_bricks", 0, 14, 16, 2) to subImage("end_stone_bricks", 9, 13, 2, 2)),
	  "double_" to (subImage("polished_diorite", 0, 14, 16, 2) to subImage("polished_diorite", 9, 13, 2, 2)),
	  "triple_" to (subImage("tuff", 0, 14, 16, 2) to subImage("tuff", 9, 13, 2, 2)),
	  "quadruple_" to (subImage("red_nether_bricks", 0, 14, 16, 2) to subImage("red_nether_bricks", 9, 13, 2, 2)),
	  "quintuple_" to (subImage("purpur_block", 0, 14, 16, 2) to subImage("purpur_block", 9, 13, 2, 2)),
	)
	@JvmStatic
	fun main(args: Array<String>) {
		
		for (planksType in PLANKS_TYPES) {
			for (decoration in DECORATIONS) {
				create("${decoration.first}capacity_${planksType}_barrel", planksType, decoration.second)
			}
		}
	}
	
	fun create(path: String, planksType: String, decoration: Pair<BufferedImage, BufferedImage>) {
		create(path, planksType, decoration.first, decoration.second)
	}
	
	fun create(path: String, planksType: String, strap: BufferedImage, vent: BufferedImage) {
		val planksImg = ImageIO.read(VANILLA_DIR.resolve("${planksType}_planks.png").inputStream())
		val hole = ImageIO.read(MY_DIR.resolve("barrel_hole.png").inputStream()).getSubimage(1, 4, 14, 7)
		val darkStripe = planksImg.getSubimage(0, 15, 16, 1)
		val lightStripe = planksImg.getSubimage(0, 0, 16, 1)
		
		val bottom = BufferedImage(16, 16, planksImg.type)
		bottom.setRGB(0, 0, 16, 16, planksImg.getRGB(0, 0, 16, 16, null, 0, 16), 0, 16)
		bottom.setRGB(0, 0, 16, 1, planksImg.getRGB(0, 15, 16, 1, null, 0, 16), 0, 16)
		bottom.setRGB(0, 0, 1, 16, planksImg.getRGB(0, 15, 16, 1, null, 0, 16), 0, 1)
		bottom.setRGB(15, 0, 1, 16, planksImg.getRGB(0, 15, 16, 1, null, 0, 16), 0, 1)
		ImageIO.write(bottom, "PNG", MY_DIR.resolve("${path}_bottom.png").outputStream())
		
		val side = BufferedImage(16, 16, planksImg.type)
		val graphicsS = side.createGraphics()
		graphicsS.rotate(Math.PI / 2, 8.0, 8.0)
		graphicsS.drawImage(planksImg.getSubimage(0, 1, 16, 15), 0, 0, null)
		graphicsS.drawImage(planksImg.getSubimage(0, 0, 16, 1), 0, 15, null)
		graphicsS.rotate(-Math.PI / 2, 8.0, 8.0)
		graphicsS.drawImage(lightStripe, 0, 0, null)
		graphicsS.drawImage(lightStripe, 0, 15, null)
		graphicsS.drawImage(strap, 0, 3, null)
		graphicsS.drawImage(strap, 0, 11, null)
		graphicsS.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
		graphicsS.drawImage(lightStripe, 0, 1, null)
		graphicsS.drawImage(lightStripe, 0, 14, null)
		ImageIO.write(side, "PNG", MY_DIR.resolve("${path}_side.png").outputStream())
		
		val top = BufferedImage(16, 16, planksImg.type)
		val graphicsT = top.createGraphics()
		graphicsT.drawImage(bottom, 0, 0, null)
		graphicsT.drawImage(vent, 3, 8, null)
		ImageIO.write(top, "PNG", MY_DIR.resolve("${path}_top.png").outputStream())
		
		val shortStripe = darkStripe.getSubimage(0, 0, 7, 1)
		val open = BufferedImage(16, 16, planksImg.type)
		val graphicsO = open.createGraphics()
		graphicsO.drawImage(bottom, 0, 0, null)
		graphicsO.drawImage(hole, 1, 4, null)
		graphicsO.rotate(Math.PI / 2, 8.0, 8.0)
		val alphaRule = AlphaComposite.SRC_ATOP
		graphicsO.composite = AlphaComposite.getInstance(alphaRule, 0.8f)
		graphicsO.drawImage(shortStripe, 4, 1, null)
		graphicsO.drawImage(shortStripe, 4, 14, null)
		graphicsO.composite = AlphaComposite.getInstance(alphaRule, 0.6f)
		graphicsO.drawImage(shortStripe, 4, 2, null)
		graphicsO.drawImage(shortStripe, 4, 13, null)
		graphicsO.composite = AlphaComposite.getInstance(alphaRule, 0.4f)
		graphicsO.drawImage(shortStripe, 4, 3, null)
		graphicsO.drawImage(shortStripe, 4, 12, null)
		graphicsO.composite = AlphaComposite.getInstance(alphaRule, 0.2f)
		graphicsO.drawImage(shortStripe, 4, 4, null)
		graphicsO.drawImage(shortStripe, 4, 11, null)
		graphicsO.composite = AlphaComposite.getInstance(alphaRule, 0.1f)
		graphicsO.drawImage(planksImg.getSubimage(0, 0, 7, 3), 4, 5, null)
		graphicsO.drawImage(planksImg.getSubimage(0, 0, 7, 3), 4, 8, null)
		graphicsO.rotate(-Math.PI / 2, 8.0, 8.0)
		ImageIO.write(open, "PNG", MY_DIR.resolve("${path}_top_open.png").outputStream())
	}
	
	fun subImage(fileName: String, x: Int, y: Int, w: Int, h: Int): BufferedImage {
		return ImageIO.read(VANILLA_DIR.resolve("$fileName.png").inputStream()).getSubimage(x, y, w, h)
	}
}
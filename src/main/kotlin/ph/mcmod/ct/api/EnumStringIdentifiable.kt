package ph.mcmod.ct.api

import net.minecraft.util.StringIdentifiable

interface EnumStringIdentifiable : StringIdentifiable {
	val name: String
	override fun asString(): String = name.lowercase()
}
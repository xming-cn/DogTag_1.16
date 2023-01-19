package me.kqn.dogtag.file

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.library.xseries.getItemStack
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object ConfigObject {
    @Config(autoReload = false)
    lateinit var conf: Configuration

}
package me.kqn.dogtag.file

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object ConfigObject {
    @Config(autoReload = false)
    lateinit var conf: Configuration

}
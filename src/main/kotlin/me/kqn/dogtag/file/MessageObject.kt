package me.kqn.dogtag.file

import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration

object MessageObject {
    @Config(value="message.yml", autoReload = true)
    lateinit var message: Configuration
}
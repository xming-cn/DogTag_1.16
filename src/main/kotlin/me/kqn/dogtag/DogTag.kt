package me.kqn.dogtag

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info

object  DogTag : Plugin() {

    override fun onEnable() {
        info("Successfully running ExamplePlugin!")
    }
}
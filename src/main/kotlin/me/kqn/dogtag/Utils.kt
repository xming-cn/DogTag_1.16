package me.kqn.dogtag

import me.kqn.dogtag.file.ConfigObject
import org.bukkit.Bukkit



fun debug(msg:String){
   if(ConfigObject.conf.getBoolean("debug")) Bukkit.getLogger().info(msg)

}



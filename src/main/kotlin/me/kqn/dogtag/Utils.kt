package me.kqn.dogtag

import me.kqn.dogtag.file.ConfigObject
import org.bukkit.Bukkit
import taboolib.platform.util.onlinePlayers


fun debug(msg:String){
   if(ConfigObject.conf.getBoolean("debug")) {
      Bukkit.getLogger().info(msg)
      for (onlinePlayer in onlinePlayers) {
         if(onlinePlayer.isOp){
            onlinePlayer.sendMessage(msg)
         }
      }
   }

}



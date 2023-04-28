package me.kqn.dogtag

import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.util.Location
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.RegionContainer
import me.kqn.dogtag.file.ConfigObject.conf
import me.kqn.dogtag.file.MessageObject.message
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.io.newFile
import taboolib.common.platform.Plugin
import taboolib.common.platform.command.command
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submitAsync
import taboolib.common5.Baffle
import taboolib.common5.FileWatcher
import taboolib.expansion.*
import taboolib.library.xseries.getItemStack
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import taboolib.platform.util.giveItem
import taboolib.platform.util.isRightClick
import taboolib.platform.util.killer
import taboolib.platform.util.onlinePlayers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.max


object  DogTag : Plugin() {


    private lateinit var dogtag:ItemStack
    private lateinit var baffle: Baffle
    private lateinit var baffle2: Baffle
    private var confPath="plugins/DogTag/config.yml"
    override fun onEnable() {
        baffle2=Baffle.of(500.toLong(),TimeUnit.MILLISECONDS)
        baffle=Baffle.of(conf.getInt("CoolDown",10).toLong(),TimeUnit.SECONDS)
        if (conf.getBoolean("database.enable")) {
            setupPlayerDatabase(conf.getConfigurationSection("database")!!)
        } else {
            setupPlayerDatabase(newFile(getDataFolder(), "data.db"))
        }
         dogtag= conf.getItemStack("Item")?: ItemStack(Material.NAME_TAG)
        FileWatcher.INSTANCE.addSimpleListener(File(confPath)){
            reload()
            onlinePlayers.forEach { if (it.isOp){
            it.sendMessage("&a?????????".colored())
            }
            }
        }
        regCmd()
    }

    override fun onDisable() {
        baffle.resetAll()
        baffle2.resetAll()
    }
    private var worldGuardPlugin: WorldGuardPlugin? = null
    private var worldGuard: WorldGuard? = null
    private var regionContainer: RegionContainer? = null
    override fun onLoad() {

        this.worldGuardPlugin = WorldGuardPlugin.inst()
        this.worldGuard = WorldGuard.getInstance()
        val booleanFlag: StateFlag = Flag.SAFE_AREA
        this.regionContainer = worldGuard!!.platform.regionContainer;
        try {
            if (worldGuard!!.flagRegistry.get("DogTag-Safe-Area") != null) return
            worldGuard!!.flagRegistry.register(booleanFlag)
        } catch (e: Exception) {
            e.printStackTrace()
            Bukkit.getLogger().warning("[DogTag] WorldGuard not found, some features will not work")

        }
    }
    private fun  reload(){
        conf= Configuration.loadFromFile(File(confPath))
        dogtag= conf.getItemStack("Item")?: ItemStack(Material.NAME_TAG)
        baffle.resetAll()

        baffle=Baffle.of(conf.getInt("CoolDown",10).toLong(),TimeUnit.SECONDS)
    }

    private fun regCmd(){
        command("dogtag"){
            createHelper()
            literal("reload"){
                execute<CommandSender>{
                        sender, _, _ ->
                    submitAsync { reload()
                    sender.sendMessage("&a???")
                    }
                }
            }
            literal("get"){
                dynamic ("????"){
                    execute<Player>{
                        sender, context, argument ->
                        if(argument.toIntOrNull()!=null){
                            sender.giveItem(dogtag.clone().apply { amount=argument.toInt() })
                        }
                        else {
                            sender.sendMessage("&a?????????????????".colored())
                        }

                    }
                }
            }
            literal("remove"){
                dynamic ("????"){
                    dynamic("?????") {
                        execute<Player> {
                                sender, context, _ ->
                            val amt=context.argument(-1).toIntOrNull()?:return@execute
                            val player=Bukkit.getPlayer(context.argument(0))?:return@execute
                            debug(amt.toString()+"  "+player.name)
                            val points= player.getDataContainer()[pointKey]?.toInt() ?:return@execute
                            //var level=player.getDataContainer()[levelKey]?.let{it.toInt()}?:return@execute
                            val newPoints= max(points-amt,0)
                            var newLevel=0;
                            val levels= conf.getConfigurationSection("Levels")!!.getKeys(false).sortedWith(){x,y->
                                return@sortedWith (x.toIntOrNull()?:0)-(y.toIntOrNull()?:0)
                            }
                            for (key in levels) {
                                if(conf.getInt("Levels.${key}.exp",0)<=newPoints){
                                    newLevel=key.toInt()

                                }
                                else {
                                    break
                                }
                            }
                            player.getDataContainer()[pointKey]=newPoints
                            player.getDataContainer()[levelKey]=newLevel
                            sender.sendMessage("&a?????${player.name}??${amt}??????????".colored())
                        }
                    }
                }
            }
            literal("points"){
                dynamic ("?????"){
                    execute<CommandSender>{
                            sender, _, argument ->
                        val p=Bukkit.getPlayer(argument)
                        if(p==null){
                            sender.sendMessage("??????????")
                        }
                        else {
                            sender.sendMessage("???${argument}???????????${p.getDataContainer()[pointKey]?:0}")
                            sender.sendMessage("???${argument}??????????${p.getDataContainer()[levelKey]?:0}")
                        }
                    }

                }
            }


        }
    }

    @SubscribeEvent
    fun playerDeath(e:PlayerDeathEvent){
        debug("name: ${e.entity.name}  uuid: ${e.entity.uniqueId} killer: ${e.killer?.name} killer: ${e.killer?.uniqueId}")
        debug((e.entity.killer!=null).toString())
        debug((e.entity.killer!!.name!=e.entity.name).toString())
        debug((e.entity.uniqueId.toString()!=e.killer?.uniqueId.toString()).toString())
        if(e.entity.killer!=null&&e.entity.killer!!.name!=e.entity.name&&e.entity.uniqueId.toString()!=e.killer?.uniqueId.toString()){
            debug("${worldGuardPlugin!=null}")
            if (worldGuardPlugin != null) {
                val player=e.entity
                val localPlayer = WorldGuardPlugin.inst().wrapPlayer(player)

                debug(player.location.toString())
                val regions = regionContainer!!.createQuery()
                val loc: Location = Location(BukkitWorld(player.world), 10.0, 64.0, 100.0)

                val keepInventory = regions.testState(loc, localPlayer, Flag.SAFE_AREA)
                debug(keepInventory.toString())
                if (keepInventory) return
            }
          //  debug(baffle.hasNext(e.entity.uniqueId.toString()).toString())
            if(baffle.hasNext(e.entity.uniqueId.toString())){
                baffle.next(e.entity.uniqueId.toString())
                debug(e.entity.world.name+"  "+e.entity.location.toString())

                e.entity.world.dropItemNaturally(e.entity.location, dogtag.also { it.amount=1 })
            }
        }
    }
    val pointKey="dogtag_points"
    val levelKey="dogtag_levels"
    //?????????????
    @SubscribeEvent
    fun onClick(e:PlayerInteractEvent){
        if(e.isRightClick()){
            val mainhand=e.player.inventory.itemInMainHand

            if(dogtag.apply { amount=mainhand.amount } == mainhand){
                if(!baffle2.hasNext(e.player.name)){
                    return
                }
                baffle2.next(e.player.name)
                mainhand.amount=mainhand.amount-1
                val points= conf.getInt("Points")
                val p= e.player.getDataContainer()[pointKey]
                if(p==null){
                    e.player.getDataContainer()[pointKey] = points//????????
                }
                else {
                    e.player.getDataContainer()[pointKey] = p.toInt()+points//???????
                }
                e.player.sendMessage(message.getString("GET_POINTS","")!!.replace("%amount%",points.toString()).colored())//??????????I?¦Ë??
                //?????§Ø?????
                val total= e.player.getDataContainer()[pointKey]!!.toInt()
                val levels= conf.getConfigurationSection("Levels")!!.getKeys(false).sortedWith(){x,y->
                    return@sortedWith (x.toIntOrNull()?:0)-(y.toIntOrNull()?:0)
                }
                val lvlNow=e.player.getDataContainer()[levelKey]?.toInt()?:0//????????
                debug("level: ${levels}")
                for (it in levels) {
                    val need= conf["Levels.${it}.exp"] as Int

                    if(lvlNow<it.toInt()) {
                        if (need <= total) {
                            e.player.sendMessage(
                                message.getString("UPGRADE")!!.replace("%level%", it).colored()
                            )//???????????
                            e.player.getDataContainer()[levelKey] = it//?????????????
                            val cmds= conf.getStringList("Levels.${it}.command")
                            for (cmd in cmds) {
                                val tmp=e.player.isOp
                                e.player.isOp=true
                                e.player.performCommand(cmd.replace("[player]",e.player.name))//????????????
                                e.player.isOp=tmp
                            }
                        }
                        else {
                            break
                        }
                    }
                }


            }
        }
    }
    @SubscribeEvent
    fun setupData(e: PlayerJoinEvent) {
        // ????????????

        e.player.setupDataContainer()

    }
    @SubscribeEvent
    fun releaseDAta(e: PlayerQuitEvent) {
        // ??????????????
        e.player.releaseDataContainer()
    }
}
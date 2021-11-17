package dev.arbjerg.ukulele.jda

import dev.arbjerg.ukulele.audio.Player
import dev.arbjerg.ukulele.audio.PlayerRegistry
import dev.arbjerg.ukulele.config.BotProps
import dev.arbjerg.ukulele.data.GuildProperties
import dev.arbjerg.ukulele.features.HelpContext
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import org.springframework.stereotype.Component

class CommandContext(
        val beans: Beans,
        val guildProperties: GuildProperties,
        val guild: Guild,
        val channel: TextChannel,
        val invoker: Member,
        val message: Message,
        val command: Command,
        val prefix: String,
        /** Prefix + command name */
        val trigger: String
) {
    @Component
    class Beans(
            val players: PlayerRegistry,
            val botProps: BotProps
    ) {
        lateinit var commandManager: CommandManager
    }

    val player: Player by lazy { beans.players.get(guild, guildProperties) }

    /** The command argument text after the trigger */
    val argumentText: String by lazy {
        message.contentRaw.drop(trigger.length).trim()
    }
    val selfMember: Member get() = guild.selfMember

    fun reply(msg: String) {
        channel.sendMessage(msg).queue()
    }

    fun replyEmbed(embed: MessageEmbed) {
        channel.sendMessage(embed).queue()
    }

    fun replyHelp(forCommand: Command = command) {
        val help = HelpContext(this, forCommand)
        forCommand.provideHelp0(help)
        channel.sendMessage(help.buildMessage()).queue()
    }

    fun handleException(t: Throwable) {
        command.log.error("Handled exception occurred", t)
        reply("An exception occurred!\n`${t.message}`")
    }

    fun isPermissible(): Boolean {
        val ourVc = guild.selfMember.voiceState?.channel
        val theirVc = invoker.voiceState?.channel

        if (theirVc == null) {
            reply("You need to be in a voice channel")
            return false
        }

        if (ourVc != theirVc && ourVc != null) { // check to make sure people can't dick around w/ someone else's music
            reply("The bot is already in a Voice Channel. You need to be in the same voice channel as the bot to use commands")
            return false
        }

        if (theirVc.members.size == 2 && theirVc.members.contains(guild.selfMember) && theirVc.members.contains(invoker)) {
            //they're in channel alone with bot. Has Carte Blanche
            return true
        }

        if (invoker.hasPermission(Permission.MANAGE_CHANNEL)) {
            //Manage Channel perm?
            return true
        }

        if(invoker.idLong == beans.botProps.maintainerID){
            //maintainer?
            return true
        }

        for (role in invoker.roles) {
            //DJ role?
            if (role.name.equals(beans.botProps.DJRoleName)) {
                return true
            }
        }

        reply("This command requires you to either have a role named `${beans.botProps.DJRoleName}` or the `Manage Channels` permission to use it (being alone with the bot also works)")
        return false
    }
}
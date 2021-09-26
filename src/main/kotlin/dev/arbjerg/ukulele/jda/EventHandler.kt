package dev.arbjerg.ukulele.jda

import dev.arbjerg.ukulele.audio.PlayerRegistry
import dev.arbjerg.ukulele.config.BotProps
import dev.arbjerg.ukulele.data.GuildPropertiesService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.StatusChangeEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.*
import kotlin.concurrent.schedule

@Service
class EventHandler(val beans: Beans, private val commandManager: CommandManager) : ListenerAdapter() {

    @Component
    class Beans(
            val players: PlayerRegistry,
            val botProps: BotProps,
            val guildProperties: GuildPropertiesService,
    )

    private val log: Logger = LoggerFactory.getLogger(EventHandler::class.java)

    override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
        if (event.isWebhookMessage || event.author.isBot) return
        commandManager.onMessage(event.guild, event.channel, event.member!!, event.message)
    }

    override fun onStatusChange(event: StatusChangeEvent) {
        log.info("{}: {} -> {}", event.entity.shardInfo, event.oldStatus, event.newStatus)
    }

    private var timer: Timer? = null

    override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
        if (isAlone(event.guild)) {
            log.info("{}: Voice Channel {} in guild {} Empty, Scheduling cleanout for {} seconds from now", event.jda.shardInfo, event.channelLeft, event.guild.id, beans.botProps.timeout / 1000)
            timer = Timer("Cleanout", false)
            timer?.schedule(beans.botProps.timeout) {
                if (isAlone(event.guild)) {
                    log.info("{}: Voice Channel {} in guild {} Still Empty; Stopping player.", event.jda.shardInfo, event.channelLeft, event.guild.id)
                    GlobalScope.launch {
                        val guildProperties = beans.guildProperties.getAwait(event.guild.idLong)
                        beans.players.get(event.guild, guildProperties).stop()
                        event.guild.audioManager.closeAudioConnection()
                    }
                }
            }
        }
    }

    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (timer != null && timer is Timer && !isAlone(event.guild)) {
            log.info("{}: Voice Channel {} in guild {} No Longer empty, Canceling cleanout", event.jda.shardInfo, event.channelJoined, event.guild.id)
            timer?.cancel()
            timer = null
        }
    }

    private fun isAlone(guild: Guild): Boolean {
        var self = guild.selfMember
        var channel = self.voiceState?.channel

        if (channel == null) {
            return false
        }
        if (channel.members.size == 1 && channel.members.contains(self)) {
            return true
        }
        return false
    }

}
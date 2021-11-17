package dev.arbjerg.ukulele.command

import dev.arbjerg.ukulele.features.HelpContext
import dev.arbjerg.ukulele.jda.Command
import dev.arbjerg.ukulele.jda.CommandContext
import org.springframework.stereotype.Component

@Component
class ShuffleCommand : Command("shuffle") {
    override suspend fun CommandContext.invoke() {
        if (!isPermissible()) return
        player.shuffle()
        reply("Queue Shuffled!")
    }

    override fun HelpContext.provideHelp() {
        addUsage("")
        addDescription("Shuffles the remaining tracks in the queue.")
    }
}

package dev.arbjerg.ukulele.command

import dev.arbjerg.ukulele.features.HelpContext
import dev.arbjerg.ukulele.jda.Command
import dev.arbjerg.ukulele.jda.CommandContext
import org.springframework.stereotype.Component

@Component
class ShuffleCommand : Command("shuffle") {
    override suspend fun CommandContext.invoke() {
<<<<<<< HEAD
        if (!isPermissible()) return
        player.shuffle()
        reply("Queue Shuffled!")
=======
        player.shuffle()
        reply("This list has been shuffled.")
>>>>>>> freyacodes-master/master
    }

    override fun HelpContext.provideHelp() {
        addUsage("")
<<<<<<< HEAD
        addDescription("Shuffles the queue")
    }
}
=======
        addDescription("Shuffles the remaining tracks in the list.")
    }
}
>>>>>>> freyacodes-master/master

package dev.arbjerg.ukulele.features

import dev.arbjerg.ukulele.jda.Command
import dev.arbjerg.ukulele.jda.CommandContext
import net.dv8tion.jda.api.MessageBuilder

class HelpContext(val commandContext: CommandContext, var command: Command?) {
    private val lines = mutableListOf<String>()

    fun addUsage(usage: String) = addUsages(usage)

    fun addUsages(vararg usages: String) {
        if (usages.isEmpty()) throw IllegalArgumentException("Expected at least one usage!")
        lines.add(usages.joinToString(" OR ") {
            commandContext.prefix + command?.name + " " + it.trim()
        })
    }

    fun addLine(text: String) {
        addLines(text)
    }

    fun addLines(vararg lines: String) {
        if (lines.isEmpty()) throw IllegalArgumentException("Expected at least one line!")
        this.lines.addAll(lines)
    }

    fun addDescription(text: String) {
        lines.add("# " + text.trim())
    }

    fun buildMessage() = MessageBuilder()
            .appendCodeBlock(lines.joinToString(separator = "\n"), "md")
            .build()
}
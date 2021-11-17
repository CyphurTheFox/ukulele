package dev.arbjerg.ukulele.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("config")
class BotProps(
        var token: String = "",
        var shards: Int = 1,
        var prefix: String = "::",
        var database: String = "./database",
        var game: String = "",
        var timeout: Long = 300000,
        var DJRoleName: String = "DJ",
        var maintainerID: Long = 0
)
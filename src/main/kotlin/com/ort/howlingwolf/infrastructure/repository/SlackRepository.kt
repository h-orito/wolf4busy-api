package com.ort.howlingwolf.infrastructure.repository

import com.slack.api.Slack
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service


@Service
class SlackRepository {

    @Value("\${slack.token:}")
    private val slackToken: String? = null

    fun postToSlack(villageId: Int, msg: String) {
        slackToken ?: return
        val slack = Slack.getInstance()
        slack.methods(slackToken).chatPostMessage { req ->
            req.channel("howling-wolf")
                .text("vid: ${villageId}\n${msg}")
        }
    }
}
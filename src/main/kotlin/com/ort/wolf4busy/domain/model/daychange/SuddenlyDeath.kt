package com.ort.wolf4busy.domain.model.daychange

import com.ort.wolf4busy.domain.model.charachip.Chara
import com.ort.wolf4busy.domain.model.charachip.Charas
import com.ort.wolf4busy.domain.model.message.Message
import com.ort.wolf4busy.domain.model.message.Messages

object SuddenlyDeath {

    fun process(dayChange: DayChange, todayMessages: Messages, charas: Charas): DayChange {
        var village = dayChange.village.copy()
        var players = dayChange.players.copy()
        var messages = dayChange.messages.copy()

        // 突然死あり設定でなければ何もしない
        if (!village.setting.rules.availableSuddenlyDeath) return dayChange

        // 前日に発言していない人が対象
        dayChange.village.notDummyParticipant().filterAlive().memberList.filter { member ->
            todayMessages.messageList.none { message ->
                message.fromVillageParticipantId == member.id
            }
        }.forEach { member ->
            // 突然死
            village = village.suddenlyDeathParticipant(member.id, village.day.latestDay())
            // 入村制限
            players = players.restrictParticipation(member.playerId)
            // 突然死メッセージ
            messages = messages.add(createSuddenlyDeathMessage(charas.chara(member.charaId), village.day.latestDay().id))
        }

        return dayChange.copy(
            village = village,
            messages = messages,
            players = players
        ).setIsChange(dayChange)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    /**
     * 突然死メッセージ
     * @param chara chara
     * @param villageDayId 村日付ID
     */
    private fun createSuddenlyDeathMessage(
        chara: Chara,
        villageDayId: Int
    ): Message {
        return Message.createPublicSystemMessage(
            createSuddenlyDeathMessageString(chara), villageDayId
        )
    }

    private fun createSuddenlyDeathMessageString(chara: Chara): String =
        "${chara.charaName.name}は突然死した。"
}
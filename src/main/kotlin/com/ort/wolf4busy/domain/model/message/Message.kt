package com.ort.wolf4busy.domain.model.message

import com.ort.dbflute.allcommon.CDef
import com.ort.wolf4busy.domain.model.village.participant.VillageParticipant
import com.ort.wolf4busy.fw.Wolf4busyDateUtil

data class Message(
    val fromVillageParticipantId: Int?,
    val toVillageParticipantId: Int?,
    val time: MessageTime,
    val content: MessageContent
) {

    companion object {

        fun createSayMessage(
            from: VillageParticipant,
            villageDayId: Int,
            messageContent: MessageContent,
            to: VillageParticipant? = null
        ): Message = Message(
            fromVillageParticipantId = from.id,
            toVillageParticipantId = to?.id,
            time = MessageTime(
                villageDayId = villageDayId,
                datetime = Wolf4busyDateUtil.currentLocalDateTime(), // dummy
                unixTimeMilli = 0L // dummy
            ),
            content = messageContent
        )

        // 別途情報取得して表示させるので、参加者一覧であることだけわかればok
        fun createParticipantsMessage(villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.参加者一覧), "読み込み中...", villageDayId)

        fun createPublicSystemMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.公開システムメッセージ), text, villageDayId)

        fun createPrivateSystemMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.非公開システムメッセージ), text, villageDayId)

        fun createSeerPrivateMessage(text: String, villageDayId: Int, participant: VillageParticipant): Message =
            createSystemMessage(MessageType(CDef.MessageType.白黒占い結果), text, villageDayId, participant)

        fun createPsychicPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.白黒霊視結果), text, villageDayId)

        fun createAttackPrivateMessage(text: String, villageDayId: Int): Message =
            createSystemMessage(MessageType(CDef.MessageType.襲撃結果), text, villageDayId)

        // ===================================================================================
        //                                                                        Assist Logic
        //                                                                        ============
        private fun createSystemMessage(
            messageType: MessageType,
            text: String,
            villageDayId: Int,
            from: VillageParticipant? = null
        ): Message = Message(
            fromVillageParticipantId = from?.id,
            toVillageParticipantId = null,
            time = MessageTime(
                villageDayId = villageDayId,
                datetime = Wolf4busyDateUtil.currentLocalDateTime(), // dummy
                unixTimeMilli = 0L // dummy
            ),
            content = MessageContent(
                type = messageType,
                num = 0, // dummy
                text = text,
                faceCode = null
            )
        )
    }
}
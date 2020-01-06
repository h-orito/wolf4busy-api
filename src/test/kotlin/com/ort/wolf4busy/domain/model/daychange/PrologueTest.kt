package com.ort.wolf4busy.domain.model.daychange

import com.ort.dbflute.allcommon.CDef
import com.ort.wolf4busy.Wolf4busyTest
import com.ort.wolf4busy.domain.model.charachip.Charas
import com.ort.wolf4busy.domain.model.message.MessageType
import com.ort.wolf4busy.domain.model.message.Messages
import com.ort.wolf4busy.domain.model.village.VillageDays
import com.ort.wolf4busy.domain.model.village.participant.VillageParticipants
import com.ort.wolf4busy.domain.model.village.setting.PersonCapacity
import com.ort.wolf4busy.dummy.DummyDomainModelCreator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
class PrologueTest : Wolf4busyTest() {

    // ===================================================================================
    //                                                                                Test
    //                                                                           =========
    @Test
    fun test_leaveParticipantIfNeeded_退村なし() {
        // ## Arrange ##
        val participant1 = DummyDomainModelCreator.createDummyVillageParticipant()
        val participant2 = DummyDomainModelCreator.createDummyVillageParticipant()
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 2,
                    memberList = listOf(
                        participant1, participant2
                    )
                )
            )
        )
        val todayMessages = Messages(
            listOf(
                DummyDomainModelCreator.createDummyMessage().copy(
                    from = participant1,
                    content = DummyDomainModelCreator.createDummyMessageContent().copy(
                        type = MessageType(CDef.MessageType.通常発言)
                    ),
                    time = DummyDomainModelCreator.createDummyMessageTime().copy(
                        datetime = LocalDateTime.now().minusHours(1L)
                    )
                ),
                DummyDomainModelCreator.createDummyMessage().copy(
                    from = participant2,
                    content = DummyDomainModelCreator.createDummyMessageContent().copy(
                        type = MessageType(CDef.MessageType.通常発言)
                    ),
                    time = DummyDomainModelCreator.createDummyMessageTime().copy(
                        datetime = LocalDateTime.now().minusHours(1L)
                    )
                )
            )
        )
        val charas = Charas(
            listOf(
                DummyDomainModelCreator.createDummyChara().copy(id = participant1.charaId),
                DummyDomainModelCreator.createDummyChara().copy(id = participant2.charaId)
            )
        )

        // ## Act ##
        val afterDayChange = Prologue.leaveParticipantIfNeeded(dayChange, todayMessages, charas)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isFalse()
    }

    @Test
    fun test_leaveParticipantIfNeeded_退村あり() {
        // ## Arrange ##
        val participant1 = DummyDomainModelCreator.createDummyVillageParticipant()
        val participant2 = DummyDomainModelCreator.createDummyVillageParticipant()
        val latestDay = DummyDomainModelCreator.createDummyVillageDay()
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                participant = VillageParticipants(
                    count = 2,
                    memberList = listOf(
                        participant1, participant2
                    )
                ),
                day = VillageDays(listOf(latestDay))
            )
        )
        val todayMessages = Messages(
            listOf(
                DummyDomainModelCreator.createDummyMessage().copy(
                    from = participant1,
                    content = DummyDomainModelCreator.createDummyMessageContent().copy(
                        type = MessageType(CDef.MessageType.通常発言)
                    ),
                    time = DummyDomainModelCreator.createDummyMessageTime().copy(
                        villageDayId = latestDay.id,
                        datetime = LocalDateTime.now().minusHours(1L)
                    )
                ),
                DummyDomainModelCreator.createDummyMessage().copy(
                    from = participant2,
                    content = DummyDomainModelCreator.createDummyMessageContent().copy(
                        type = MessageType(CDef.MessageType.通常発言)
                    ),
                    time = DummyDomainModelCreator.createDummyMessageTime().copy(
                        villageDayId = latestDay.id,
                        datetime = LocalDateTime.now().minusHours(25L)
                    )
                )
            )
        )
        val charas = Charas(
            listOf(
                DummyDomainModelCreator.createDummyChara().copy(id = participant1.charaId),
                DummyDomainModelCreator.createDummyChara().copy(id = participant2.charaId)
            )
        )

        // ## Act ##
        val afterDayChange = Prologue.leaveParticipantIfNeeded(dayChange, todayMessages, charas)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isTrue()
        assertThat(afterDayChange.village.participant.member(participant1.id).isGone).isFalse()
        assertThat(afterDayChange.village.participant.member(participant2.id).isGone).isTrue()
    }

    @Test
    fun test_addDayIfNeeded_開始時刻になっていない() {
        // ## Arrange ##
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay().copy(
                            dayChangeDatetime = LocalDateTime.now().plusSeconds(1L)
                        )
                    )
                )
            )
        )

        // ## Act ##
        val afterDayChange = Prologue.addDayIfNeeded(dayChange)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isFalse()
    }

    @Test
    fun test_addDayIfNeeded_人数不足() {
        // ## Arrange ##
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay().copy(
                            dayChangeDatetime = LocalDateTime.now().minusSeconds(1L)
                        )
                    )
                )
            )
        )

        // ## Act ##
        val afterDayChange = Prologue.addDayIfNeeded(dayChange)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isTrue()
        assertThat(afterDayChange.village.status.toCdef()).isEqualTo(CDef.VillageStatus.廃村)
    }

    @Test
    fun test_addDayIfNeeded_人数が足りている() {
        // ## Arrange ##
        val dayChange = DummyDomainModelCreator.createDummyDayChange().copy(
            village = DummyDomainModelCreator.createDummyVillage().copy(
                day = VillageDays(
                    listOf(
                        DummyDomainModelCreator.createDummyVillageDay().copy(
                            dayChangeDatetime = LocalDateTime.now().minusSeconds(1L)
                        )
                    )
                ),
                setting = DummyDomainModelCreator.createDummyVillageSettings().copy(
                    capacity = PersonCapacity(2, 10)
                ),
                participant = VillageParticipants(
                    count = 2,
                    memberList = listOf(
                        DummyDomainModelCreator.createDummyVillageParticipant(),
                        DummyDomainModelCreator.createDummyVillageParticipant()
                    )
                )
            )
        )

        // ## Act ##
        val afterDayChange = Prologue.addDayIfNeeded(dayChange)

        // ## Assert ##
        assertThat(afterDayChange.isChange).isTrue()
        assertThat(afterDayChange.village.day.dayList.size).isEqualTo(2)
    }

    @Test
    fun test_dayChange() {
        // ## Arrange ##

        // ## Act ##

        // ## Assert ##
    }

}
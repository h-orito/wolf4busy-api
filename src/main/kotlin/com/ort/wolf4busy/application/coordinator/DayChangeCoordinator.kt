package com.ort.wolf4busy.application.coordinator

import com.ort.dbflute.allcommon.CDef
import com.ort.wolf4busy.application.service.*
import com.ort.wolf4busy.domain.model.charachip.Charas
import com.ort.wolf4busy.domain.model.commit.Commits
import com.ort.wolf4busy.domain.model.daychange.DayChange
import com.ort.wolf4busy.domain.model.player.Players
import com.ort.wolf4busy.domain.model.village.Village
import com.ort.wolf4busy.domain.model.village.ability.VillageAbilities
import com.ort.wolf4busy.domain.model.village.vote.VillageVotes
import com.ort.wolf4busy.fw.exception.Wolf4busyBusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DayChangeCoordinator(
    val villageService: VillageService,
    val voteService: VoteService,
    val abilityService: AbilityService,
    val commitService: CommitService,
    val messageService: MessageService,
    val charachipService: CharachipService,
    val playerService: PlayerService
) {

    /**
     * 必要あれば日付更新
     *
     * @param village village
     */
    @Transactional(rollbackFor = [Exception::class, Wolf4busyBusinessException::class])
    fun dayChangeIfNeeded(village: Village) {
        // 最新日の通常発言
        val votes: VillageVotes = voteService.findVillageVotes(village.id)
        val abilities: VillageAbilities = abilityService.findVillageAbilities(village.id)
        val commits: Commits = commitService.findCommits(village.id)
        val todayMessages = messageService.findMessageList(village.id, village.day.latestDay().id, listOf(CDef.MessageType.通常発言))
        val charas: Charas = charachipService.findCharaList(village.setting.charachip.charachipId)
        val players: Players = playerService.findPlayers(village.id)

        val beforeDayChange = DayChange(village, votes, abilities, players)

        // プロローグで長時間発言していない人を退村させる
        var dayChange: DayChange = beforeDayChange.leaveParticipantIfNeeded(todayMessages, charas).also {
            if (it.isChange) update(beforeDayChange, it)
        }

        // 必要あれば日付追加
        dayChange = dayChange.addDayIfNeeded(commits).also {
            if (!it.isChange) return
            update(beforeDayChange, it)
        }

        // 登録後の村日付idが必要になるので取得し直す
        dayChange = dayChange.copy(village = villageService.findVillage(village.id))

        // 日付更新
        dayChange.process(todayMessages, charas).let { if (it.isChange) update(dayChange, it) }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun update(before: DayChange, after: DayChange) {
        // village
        if (before.village.existsDifference(after.village)) {
            villageService.updateVillageDifference(before.village, after.village)
        }
        // message
        if (before.messages.existsDifference(after.messages)) {
            messageService.updateDifference(before.village.id, before.messages, after.messages)
        }
        // votes
        if (before.votes.existsDifference(after.votes)) {
            voteService.updateDifference(before.votes, after.votes)
        }
        // abilities
        if (before.abilities.existsDifference(after.abilities)) {
            abilityService.updateDifference(before.abilities, after.abilities)
        }
    }
}
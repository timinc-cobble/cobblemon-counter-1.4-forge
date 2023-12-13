package us.timinc.mc.cobblemon.counter.api

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.world.entity.player.Player
import us.timinc.mc.cobblemon.counter.store.KoCount
import us.timinc.mc.cobblemon.counter.store.KoStreak

object KoApi {
    fun getTotal(player: Player): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).total()
    }

    fun getCount(player: Player, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).get(species)
    }

    fun getStreak(player: Player): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val koStreakData = (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak)
        return Pair(koStreakData.species, koStreakData.count)
    }
}
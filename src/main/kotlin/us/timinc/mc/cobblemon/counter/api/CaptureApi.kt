package us.timinc.mc.cobblemon.counter.api

import com.cobblemon.mod.common.Cobblemon
import net.minecraft.world.entity.player.Player
import us.timinc.mc.cobblemon.counter.store.CaptureCount
import us.timinc.mc.cobblemon.counter.store.CaptureStreak

object CaptureApi {
    fun getTotal(player: Player): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).total()
    }

    fun getCount(player: Player, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).get(species)
    }

    fun getStreak(player: Player): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val koStreakData = (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak)
        return Pair(koStreakData.species, koStreakData.count)
    }
}
package us.timinc.mc.cobblemon.counter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.util.getPlayer
import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import us.timinc.mc.cobblemon.counter.store.CaptureCount
import us.timinc.mc.cobblemon.counter.store.CaptureStreak
import us.timinc.mc.cobblemon.counter.store.KoCount
import us.timinc.mc.cobblemon.counter.store.KoStreak
import java.util.*

@Mod(Counter.MOD_ID)
object Counter {
    const val MOD_ID = "cobbled_counter"

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    object Registration {
        @SubscribeEvent
        fun onInit(e: ServerStartedEvent) {
            PlayerDataExtensionRegistry.register(KoCount.NAME, KoCount::class.java)
            PlayerDataExtensionRegistry.register(KoStreak.NAME, KoStreak::class.java)
            PlayerDataExtensionRegistry.register(CaptureCount.NAME, CaptureCount::class.java)
            PlayerDataExtensionRegistry.register(CaptureStreak.NAME, CaptureStreak::class.java)

            CobblemonEvents.POKEMON_CAPTURED.subscribe { handlePokemonCapture(it) }
            CobblemonEvents.BATTLE_FAINTED.subscribe { handleWildDefeat(it) }
        }

        @SubscribeEvent
        fun onRegisterCommands(e: RegisterCommandsEvent) {
            e.dispatcher.register(
                literal("counter")
                    .then(
                        literal("ko")
                            .then(
                                literal("count")
                                    .then(
                                        argument("species", StringArgumentType.greedyString())
                                            .executes{ checkKoCount(it) }
                                    )
                            )
                            .then(
                                literal("streak")
                                    .executes { checkKoStreak(it) }
                            )
                    )
                    .then(
                        literal("capture")
                            .then(
                                literal("count")
                                    .then(
                                        argument("species", StringArgumentType.greedyString())
                                            .executes { checkCaptureCount(it) }
                                    )
                            )
                            .then(
                                literal("streak")
                                    .executes { checkCaptureStreak(it) }
                            )
                    )
            )
        }
    }

    private fun checkKoCount(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.player ?: return 0
        val species = StringArgumentType.getString(ctx, "species")
        val score = getPlayerKoCount(player, species)
        ctx.source.sendSystemMessage(Component.translatable("counter.ko.count", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun checkKoStreak(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.player ?: return 0
        val streakData = getPlayerKoStreak(player)
        val species = streakData.first
        val score = streakData.second
        ctx.source.sendSystemMessage(Component.translatable("counter.ko.streak", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun checkCaptureCount(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.player ?: return 0
        val species = StringArgumentType.getString(ctx, "species")
        val score = getPlayerCaptureCount(player, species)
        ctx.source.sendSystemMessage(Component.translatable("counter.capture.count", score, species))
        return Command.SINGLE_SUCCESS
    }

    private fun checkCaptureStreak(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.player ?: return 0
        val streakData = getPlayerCaptureStreak(player)
        val species = streakData.first
        val score = streakData.second
        ctx.source.sendSystemMessage(Component.translatable("counter.capture.streak", score, species))
        return Command.SINGLE_SUCCESS
    }

    @Suppress("unused")
    fun getPlayerKoStreak(player: Player, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak).get(species)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getPlayerKoStreak(player: Player): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val koStreakData = (playerData.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak)
        return Pair(koStreakData.species, koStreakData.count)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getPlayerKoCount(player: Player, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount).get(species)
    }

    @Suppress("unused")
    fun getPlayerCaptureStreak(player: Player, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak).get(species)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getPlayerCaptureStreak(player: Player): Pair<String, Int> {
        val playerData = Cobblemon.playerData.get(player)
        val captureStreakData = (playerData.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak)
        return Pair(captureStreakData.species, captureStreakData.count)
    }

    @Suppress("unused", "MemberVisibilityCanBePrivate")
    fun getPlayerCaptureCount(player: Player, species: String): Int {
        val playerData = Cobblemon.playerData.get(player)
        return (playerData.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount).get(species)
    }

    private fun handlePokemonCapture(event: PokemonCapturedEvent) {
        val species = event.pokemon.species.name.lowercase()

        val data = Cobblemon.playerData.get(event.player)

        val captureCount: CaptureCount = data.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount
        captureCount.add(species)

        val captureStreak: CaptureStreak =
            data.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak
        captureStreak.add(species)

        Cobblemon.playerData.saveSingle(data)
    }

    private fun handleWildDefeat(battleVictoryEvent: BattleFaintedEvent) {
        val targetEntity = battleVictoryEvent.killed.entity ?: return
        val targetPokemon = targetEntity.pokemon
        if (!targetPokemon.isWild()) {
            return
        }
        val species = targetPokemon.species.name.lowercase()

        battleVictoryEvent.battle.playerUUIDs.mapNotNull(UUID::getPlayer).forEach { player ->
            val data = Cobblemon.playerData.get(player)
            val koCount: KoCount = data.extraData.getOrPut(KoCount.NAME) { KoCount() } as KoCount
            val koStreak: KoStreak = data.extraData.getOrPut(KoStreak.NAME) { KoStreak() } as KoStreak

            koCount.add(species)
            koStreak.add(species)

            Cobblemon.playerData.saveSingle(data)
        }
    }
}
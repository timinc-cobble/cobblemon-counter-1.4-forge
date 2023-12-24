package us.timinc.mc.cobblemon.counter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleFaintedEvent
import com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.api.pokeball.PokeBalls
import com.cobblemon.mod.common.api.storage.player.PlayerDataExtensionRegistry
import com.cobblemon.mod.common.command.argument.PokemonArgumentType
import com.cobblemon.mod.common.util.getPlayer
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.world.entity.player.Player
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.event.server.ServerStartedEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import us.timinc.mc.cobblemon.counter.api.CaptureApi
import us.timinc.mc.cobblemon.counter.command.*
import us.timinc.mc.cobblemon.counter.config.CounterConfig
import us.timinc.mc.cobblemon.counter.store.CaptureCount
import us.timinc.mc.cobblemon.counter.store.CaptureStreak
import us.timinc.mc.cobblemon.counter.store.KoCount
import us.timinc.mc.cobblemon.counter.store.KoStreak
import java.util.*

@Mod(Counter.MOD_ID)
object Counter {
    const val MOD_ID = "cobbled_counter"

    private var logger: Logger = LogManager.getLogger(MOD_ID)
    private var config: CounterConfig = CounterConfig.Builder.load()
    private var eventsSetup: Boolean = false

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    object Registration {
        @SubscribeEvent
        fun onInit(e: ServerStartedEvent) {
            PlayerDataExtensionRegistry.register(KoCount.NAME, KoCount::class.java)
            PlayerDataExtensionRegistry.register(KoStreak.NAME, KoStreak::class.java)
            PlayerDataExtensionRegistry.register(CaptureCount.NAME, CaptureCount::class.java)
            PlayerDataExtensionRegistry.register(CaptureStreak.NAME, CaptureStreak::class.java)

            if (!eventsSetup) {
                CobblemonEvents.POKEMON_CAPTURED.subscribe { handlePokemonCapture(it) }
                CobblemonEvents.BATTLE_FAINTED.subscribe { handleWildDefeat(it) }
                CobblemonEvents.POKEMON_CATCH_RATE.subscribe { repeatBallBooster(it) }
                eventsSetup = true
            }
        }

        @SubscribeEvent
        fun onRegisterCommands(e: RegisterCommandsEvent) {
            e.dispatcher.register(
                literal("counter").then(
                    literal("ko").then(
                        literal("count")
                            .then(
                                argument("species", PokemonArgumentType.pokemon())
                                    .then(
                                        argument("player", EntityArgument.player())
                                            .executes { KoCountCommand.withPlayer(it) }
                                    )
                                    .executes { KoCountCommand.withoutPlayer(it) }
                            )
                    ).then(
                        literal("streak")
                            .then(
                                argument("player", EntityArgument.player())
                                    .executes { KoStreakCommand.withPlayer(it) }
                            )
                            .executes { KoStreakCommand.withoutPlayer(it) }

                    ).then(
                        literal("total")
                            .then(
                                argument("player", EntityArgument.player())
                                    .executes { KoTotalCommand.withPlayer(it) }
                            )
                            .executes { KoTotalCommand.withoutPlayer(it) }
                    ).then(
                        literal("reset")
                            .requires { source -> source.hasPermission(2) }
                            .then(
                                literal("count")
                                    .then(
                                        argument("player", EntityArgument.player())
                                            .executes { KoResetCommand.resetCount(it) }
                                    )
                            )
                            .then(
                                literal("streak")
                                    .then(
                                        argument("player", EntityArgument.player())
                                            .executes { KoResetCommand.resetStreak(it) }
                                    )
                            )
                            .then(
                                literal("all").then(
                                    argument("player", EntityArgument.player())
                                        .executes { KoResetCommand.reset(it) }
                                )
                            )
                    )
                ).then(
                    literal("capture").then(
                        literal("count")
                            .then(
                                argument("species", PokemonArgumentType.pokemon())
                                    .then(
                                        argument("player", EntityArgument.player())
                                            .executes { CaptureCountCommand.withPlayer(it) }
                                    )
                                    .executes { CaptureCountCommand.withoutPlayer(it) }
                            )
                    ).then(
                        literal("streak")
                            .then(
                                argument("player", EntityArgument.player())
                                    .executes { CaptureStreakCommand.withPlayer(it) }
                            )
                            .executes { CaptureStreakCommand.withoutPlayer(it) }
                    ).then(
                        literal("total")
                            .then(
                                argument("player", EntityArgument.player())
                                    .executes { CaptureTotalCommand.withPlayer(it) }
                            )
                            .executes { CaptureTotalCommand.withoutPlayer(it) }
                    ).then(
                        literal("reset")
                            .requires { source -> source.hasPermission(2) }
                            .then(
                                literal("count")
                                    .then(
                                        argument("player", EntityArgument.player())
                                            .executes { CaptureResetCommand.resetCount(it) }
                                    )
                            )
                            .then(
                                literal("streak")
                                    .then(
                                        argument("player", EntityArgument.player())
                                            .executes { CaptureResetCommand.resetStreak(it) }
                                    )
                            )
                            .then(
                                literal("all").then(
                                    argument("player", EntityArgument.player())
                                        .executes { CaptureResetCommand.reset(it) }
                                )
                            )
                    )
                )
            )
        }
    }

    private fun repeatBallBooster(event: PokemonCatchRateEvent) {
        val thrower = event.thrower

        if (thrower !is Player) return

        if (event.pokeBallEntity.pokeBall == PokeBalls.REPEAT_BALL && CaptureApi.getCount(
                thrower, event.pokemonEntity.pokemon.species.name.lowercase()
            ) > 0
        ) {
            event.catchRate *= 2.5f
        }
    }

    private fun handlePokemonCapture(event: PokemonCapturedEvent) {
        val species = event.pokemon.species.name.lowercase()

        val data = Cobblemon.playerData.get(event.player)

        val captureCount: CaptureCount = data.extraData.getOrPut(CaptureCount.NAME) { CaptureCount() } as CaptureCount
        captureCount.add(species)

        val captureStreak: CaptureStreak =
            data.extraData.getOrPut(CaptureStreak.NAME) { CaptureStreak() } as CaptureStreak
        captureStreak.add(species)

        info(
            "Player ${event.player.displayName.string} captured a $species streak(${captureStreak.count}) count(${
                captureCount.get(
                    species
                )
            })"
        )

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

            info(
                "Player ${player.displayName.string} KO'd a $species streak(${koStreak.count}) count(${
                    koCount.get(
                        species
                    )
                })"
            )

            Cobblemon.playerData.saveSingle(data)
        }
    }

    private fun info(msg: String) {
        if (!config.debug) return
        logger.info(msg)
    }
}
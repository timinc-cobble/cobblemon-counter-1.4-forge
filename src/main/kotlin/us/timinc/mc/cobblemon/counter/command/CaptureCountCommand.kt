package us.timinc.mc.cobblemon.counter.command

import com.cobblemon.mod.common.command.argument.PokemonArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import us.timinc.mc.cobblemon.counter.api.CaptureApi

object CaptureCountCommand {
    fun withPlayer(ctx: CommandContext<CommandSourceStack>): Int {
        return check(
            ctx,
            EntityArgument.getPlayer(ctx, "player"),
            PokemonArgumentType.getPokemon(ctx, "species").name.lowercase()
        )
    }

    fun withoutPlayer(ctx: CommandContext<CommandSourceStack>): Int {
        return ctx.source.player?.let { player ->
            check(
                ctx,
                player,
                PokemonArgumentType.getPokemon(ctx, "species").name.lowercase()
            )
        } ?: 0
    }

    private fun check(ctx: CommandContext<CommandSourceStack>, player: Player, species: String): Int {
        val score = CaptureApi.getCount(player, species)
        ctx.source.sendSystemMessage(Component.translatable("counter.capture.count", player.displayName, score, species))
        return score
    }
}
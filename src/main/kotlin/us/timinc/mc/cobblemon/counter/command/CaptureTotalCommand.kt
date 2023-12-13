package us.timinc.mc.cobblemon.counter.command

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import us.timinc.mc.cobblemon.counter.api.CaptureApi

object CaptureTotalCommand {
    fun withPlayer(ctx: CommandContext<CommandSourceStack>): Int {
        return check(
            ctx,
            EntityArgument.getPlayer(ctx, "player")
        )
    }

    fun withoutPlayer(ctx: CommandContext<CommandSourceStack>): Int {
        return ctx.source.player?.let { player ->
            check(
                ctx,
                player
            )
        } ?: 0
    }

    private fun check(ctx: CommandContext<CommandSourceStack>, player: Player): Int {
        val score = CaptureApi.getTotal(player)
        ctx.source.sendSystemMessage(Component.translatable("counter.capture.total", player.displayName, score))
        return score
    }
}
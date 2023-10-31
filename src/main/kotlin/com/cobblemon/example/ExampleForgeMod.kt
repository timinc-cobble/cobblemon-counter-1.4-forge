package com.cobblemon.example

import com.cobblemon.mod.common.api.pokemon.PokemonSpecies.getByIdentifier
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.Mod.EventBusSubscriber

@Mod("cobblemon_forge_mdk")
class ExampleForgeMod {
    @EventBusSubscriber(bus = EventBusSubscriber.Bus.FORGE)
    object Registration {
        @SubscribeEvent
        fun onCommandRegistration(event: RegisterCommandsEvent) {
            event.dispatcher.register(
                Commands.literal("test")
                    .executes { context: CommandContext<CommandSourceStack> ->
                        val species =
                            getByIdentifier(ResourceLocation.of("cobblemon:bidoof", ':'))
                        context.source.sendSystemMessage(
                            Component.literal("Got species: ")
                                .withStyle(Style.EMPTY.withColor(0x03e3fc))
                                .append(species!!.translatedName)
                        )
                        0
                    }
            )
        }
    }
}

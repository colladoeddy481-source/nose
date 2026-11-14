package com.example.rickregen;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class RegenCommand {
	private RegenCommand() {}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(Commands.literal("regen")
				// Para limitarlo a operadores: .requires(src -> src.hasPermission(2))
				.executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					if (!RegenManager.start(player)) {
						ctx.getSource().sendFailure(Component.literal("Ya estas regenerando."));
						return 0;
					}
					return 1;
				})
				.then(Commands.literal("reset").executes(ctx -> {
					ServerPlayer player = ctx.getSource().getPlayerOrException();
					RegenManager.resetSkin(player);
					ctx.getSource().sendSuccess(() -> Component.literal("Skin original restaurada."), false);
					return 1;
				}))));
	}
}

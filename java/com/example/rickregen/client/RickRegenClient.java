package com.example.rickregen.client;

import com.example.rickregen.network.RegenSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class RickRegenClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(RegenSyncPayload.TYPE, (payload, context) ->
			ClientRegenData.update(payload.playerId(), payload.regenerating(), payload.rickPrime()));

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientRegenData.clear());
	}
}

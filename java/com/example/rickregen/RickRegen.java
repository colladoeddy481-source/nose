package com.example.rickregen;

import com.example.rickregen.network.RegenSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.resources.ResourceLocation;

public class RickRegen implements ModInitializer {
	public static final String MOD_ID = "rickregen";

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(RegenSyncPayload.TYPE, RegenSyncPayload.CODEC);
		RegenManager.init();
		RegenCommand.register();
	}
}

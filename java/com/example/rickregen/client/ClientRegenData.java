package com.example.rickregen.client;

import com.example.rickregen.RickRegen;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/** Estado de regeneracion que el cliente conoce de cada jugador. */
public final class ClientRegenData {
	public static final ResourceLocation RICK_PRIME_TEXTURE = RickRegen.id("textures/entity/rick_prime.png");

	private record Info(boolean regenerating, boolean rickPrime) {}

	private static final Map<UUID, Info> DATA = new HashMap<>();

	private ClientRegenData() {}

	public static void update(UUID id, boolean regenerating, boolean rickPrime) {
		DATA.put(id, new Info(regenerating, rickPrime));
	}

	public static boolean isRegenerating(UUID id) {
		Info i = DATA.get(id);
		return i != null && i.regenerating();
	}

	public static boolean isRickPrime(UUID id) {
		Info i = DATA.get(id);
		return i != null && i.rickPrime();
	}

	public static void clear() {
		DATA.clear();
	}
}

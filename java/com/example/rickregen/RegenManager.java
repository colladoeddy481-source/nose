package com.example.rickregen;

import com.example.rickregen.network.RegenSyncPayload;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Logica de servidor de la regeneracion. El estado vive en memoria (no se guarda al cerrar el mundo). */
public final class RegenManager {
	/** Duracion de la animacion en ticks (160 = 8 segundos). */
	public static final int DURATION = 160;
	/** Cuantas veces una muerte se convierte en regeneracion. */
	public static final int MAX_DEATH_REGENS = 12;

	private static final class State {
		int ticksLeft;
		boolean rickPrime;
		int deathRegens;
	}

	private static final Map<UUID, State> STATES = new HashMap<>();

	private RegenManager() {}

	private static State state(ServerPlayer player) {
		return STATES.computeIfAbsent(player.getUUID(), id -> new State());
	}

	public static boolean isRegenerating(ServerPlayer player) {
		State s = STATES.get(player.getUUID());
		return s != null && s.ticksLeft > 0;
	}

	public static void init() {
		// Muerte -> regeneracion
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player) {
				State s = state(player);
				if (s.ticksLeft > 0) {
					player.setHealth(1.0F);
					return false;
				}
				if (s.deathRegens < MAX_DEATH_REGENS) {
					s.deathRegens++;
					player.setHealth(1.0F);
					start(player);
					return false;
				}
			}
			return true;
		});

		// Invulnerable mientras regenera
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player) {
				return !isRegenerating(player);
			}
			return true;
		});

		ServerTickEvents.END_SERVER_TICK.register(RegenManager::tick);

		// Que quien entra vea las skins/animaciones de los demas
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			for (Map.Entry<UUID, State> e : STATES.entrySet()) {
				State s = e.getValue();
				ServerPlayNetworking.send(handler.player, new RegenSyncPayload(e.getKey(), s.ticksLeft > 0, s.rickPrime));
			}
		});
	}

	public static boolean start(ServerPlayer player) {
		State s = state(player);
		if (s.ticksLeft > 0) return false;
		s.ticksLeft = DURATION;
		player.setGlowingTag(true);
		player.clearFire();
		player.level().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
		sync(player.server, player.getUUID(), s);
		return true;
	}

	public static void resetSkin(ServerPlayer player) {
		State s = state(player);
		s.rickPrime = false;
		sync(player.server, player.getUUID(), s);
	}

	private static void tick(MinecraftServer server) {
		for (UUID id : List.copyOf(STATES.keySet())) {
			State s = STATES.get(id);
			if (s == null || s.ticksLeft <= 0) continue;
			ServerPlayer player = server.getPlayerList().getPlayer(id);
			if (player == null) continue;

			s.ticksLeft--;
			int elapsed = DURATION - s.ticksLeft;

			// Inmovil
			player.setDeltaMovement(0, 0, 0);
			player.hurtMarked = true;
			player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 10, false, false, false));

			// Particulas doradas: manos + cabeza, mas intensas con el tiempo
			ServerLevel level = player.serverLevel();
			double x = player.getX(), y = player.getY(), z = player.getZ();
			double yaw = Math.toRadians(player.getYRot());
			double rightX = -Math.cos(yaw), rightZ = -Math.sin(yaw);
			int count = 2 + (int) (elapsed / (float) DURATION * 10);

			for (int side = -1; side <= 1; side += 2) {
				double hx = x + rightX * 0.9 * side;
				double hz = z + rightZ * 0.9 * side;
				level.sendParticles(ParticleTypes.END_ROD, hx, y + 1.4, hz, count, 0.08, 0.15, 0.08, 0.06);
				level.sendParticles(ParticleTypes.FLAME, hx, y + 1.4, hz, count / 2, 0.05, 0.1, 0.05, 0.03);
			}
			level.sendParticles(ParticleTypes.END_ROD, x, y + 2.0, z, count, 0.12, 0.25, 0.12, 0.08);
			level.sendParticles(ParticleTypes.FLAME, x, y + 2.0, z, count / 2, 0.1, 0.15, 0.1, 0.04);

			if (s.ticksLeft % 40 == 0 && s.ticksLeft > 0) {
				level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 1.0F, 1.0F);
			}

			if (s.ticksLeft == 0) {
				finish(player, s);
			}
		}
	}

	private static void finish(ServerPlayer player, State s) {
		s.rickPrime = true;
		player.setHealth(player.getMaxHealth());
		player.getFoodData().setFoodLevel(20);
		player.setGlowingTag(false);

		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.4, 0.8, 0.4, 0.15);
		level.playSound(null, player.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);

		sync(player.server, player.getUUID(), s);
	}

	private static void sync(MinecraftServer server, UUID id, State s) {
		RegenSyncPayload payload = new RegenSyncPayload(id, s.ticksLeft > 0, s.rickPrime);
		for (ServerPlayer p : server.getPlayerList().getPlayers()) {
			ServerPlayNetworking.send(p, payload);
		}
	}
}

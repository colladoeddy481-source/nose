package com.example.rickregen.network;

import com.example.rickregen.RickRegen;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor -> cliente: estado de regeneracion de un jugador. */
public record RegenSyncPayload(UUID playerId, boolean regenerating, boolean rickPrime) implements CustomPacketPayload {
	public static final Type<RegenSyncPayload> TYPE = new Type<>(RickRegen.id("regen_sync"));

	public static final StreamCodec<FriendlyByteBuf, RegenSyncPayload> CODEC = StreamCodec.of(
		(buf, p) -> {
			buf.writeUUID(p.playerId());
			buf.writeBoolean(p.regenerating());
			buf.writeBoolean(p.rickPrime());
		},
		buf -> new RegenSyncPayload(buf.readUUID(), buf.readBoolean(), buf.readBoolean())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

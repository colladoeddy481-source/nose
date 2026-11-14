package com.example.rickregen.mixin;

import com.example.rickregen.client.ClientRegenData;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Cambia la skin del jugador a Rick Prime (conserva capa y elytra). */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
	@Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
	private void rickregen$getSkin(CallbackInfoReturnable<PlayerSkin> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		if (ClientRegenData.isRickPrime(self.getUUID())) {
			PlayerSkin orig = cir.getReturnValue();
			cir.setReturnValue(new PlayerSkin(
				ClientRegenData.RICK_PRIME_TEXTURE,
				null,
				orig.capeTexture(),
				orig.elytraTexture(),
				PlayerSkin.Model.WIDE,
				orig.secure()
			));
		}
	}
}

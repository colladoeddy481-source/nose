package com.example.rickregen.mixin;

import com.example.rickregen.client.ClientRegenData;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Pose de regeneracion: brazos abiertos, cabeza hacia atras y temblor. */
@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin {
	@Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
	private void rickregen$pose(LivingEntity entity, float limbSwing, float limbSwingAmount,
								float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
		if (!ClientRegenData.isRegenerating(entity.getUUID())) return;

		PlayerModel<?> m = (PlayerModel<?>) (Object) this;
		float shake = Mth.sin(ageInTicks * 1.5F) * 0.04F;
		float open = (float) (Math.PI / 2.0);

		m.rightArm.xRot = 0.0F;
		m.rightArm.yRot = 0.0F;
		m.rightArm.zRot = open + shake;
		m.leftArm.xRot = 0.0F;
		m.leftArm.yRot = 0.0F;
		m.leftArm.zRot = -open - shake;
		m.head.xRot = -0.6F;

		m.hat.copyFrom(m.head);
		m.leftSleeve.copyFrom(m.leftArm);
		m.rightSleeve.copyFrom(m.rightArm);
	}
}

package dev.nebula.mixin;

import dev.nebula.module.ModuleManager;
import dev.nebula.module.impl.FarmFpsOptimizer;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {
	@Inject(
		method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void nebula$skipExplosions(ParticleEffect effect, double x, double y, double z, double vx, double vy, double vz,
	                                   CallbackInfoReturnable<Particle> cir) {
		FarmFpsOptimizer farm = ModuleManager.FARM;
		if (farm == null || !farm.enabled || !farm.explosions.value) return;
		if (effect.getType() == ParticleTypes.EXPLOSION || effect.getType() == ParticleTypes.EXPLOSION_EMITTER) {
			cir.setReturnValue(null);
		}
	}
}

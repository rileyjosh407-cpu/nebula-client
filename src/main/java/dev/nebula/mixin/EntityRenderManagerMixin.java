package dev.nebula.mixin;

import dev.nebula.module.ModuleManager;
import dev.nebula.module.impl.FarmFpsOptimizer;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderManager.class)
public class EntityRenderManagerMixin {
	@Inject(
		method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
		at = @At("HEAD"),
		cancellable = true
	)
	private void nebula$hideFarmEntities(Entity entity, Frustum frustum, double x, double y, double z,
	                                     CallbackInfoReturnable<Boolean> cir) {
		FarmFpsOptimizer farm = ModuleManager.FARM;
		if (farm == null || !farm.enabled) return;
		EntityType<?> type = entity.getType();
		if ((farm.xpOrbs.value && type == EntityType.EXPERIENCE_ORB)
			|| (farm.fallingBlocks.value && type == EntityType.FALLING_BLOCK)
			|| (farm.tnt.value && type == EntityType.TNT)) {
			cir.setReturnValue(false);
		}
	}
}

package dev.nebula.mixin;

import dev.nebula.module.ModuleManager;
import dev.nebula.module.impl.FarmFpsOptimizer;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Mixin(SoundSystem.class)
public class SoundSystemMixin {
	private static final Map<Identifier, Deque<Long>> NEBULA_RECENT = new HashMap<>();

	@Inject(
		method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;",
		at = @At("HEAD"),
		cancellable = true
	)
	private void nebula$muteSpam(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
		FarmFpsOptimizer farm = ModuleManager.FARM;
		if (farm == null || !farm.enabled || !farm.sounds.value) return;

		long now = System.currentTimeMillis();
		Deque<Long> times = NEBULA_RECENT.computeIfAbsent(sound.getId(), k -> new ArrayDeque<>());
		while (!times.isEmpty() && now - times.peekFirst() > 1000) times.pollFirst();

		if (times.size() >= farm.soundSpam.value) {
			cir.setReturnValue(SoundSystem.PlayResult.NOT_STARTED);
			return;
		}
		times.addLast(now);
	}
}

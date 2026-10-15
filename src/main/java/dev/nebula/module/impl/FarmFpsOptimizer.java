package dev.nebula.module.impl;

import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.module.Setting;

/**
 * Reduces client-side lag around farms by skipping rendering of noisy entities/particles and
 * muting sounds that repeat too often. Nothing here changes what the server sees.
 */
public class FarmFpsOptimizer extends Module {
	public final Setting.Bool xpOrbs = add(new Setting.Bool("XP Orbs", "Hides experience orbs.", true));
	public final Setting.Bool fallingBlocks = add(new Setting.Bool("Falling Blocks", "Hides falling sand, gravel and concrete powder.", true));
	public final Setting.Bool tnt = add(new Setting.Bool("TNT", "Hides activated TNT.", true));
	public final Setting.Bool explosions = add(new Setting.Bool("Explosions", "Hides explosion particles.", true));
	public final Setting.Bool sounds = add(new Setting.Bool("Sounds", "Mutes sounds that repeat too often.", true));
	public final Setting.Slider soundSpam = add(new Setting.Slider("Sound Spam", "How many repeats within one second trigger muting. Lower mutes sooner.", 1, 20, 5));

	public FarmFpsOptimizer() {
		super("Farm FPS Optimizer", "Reduces lag around farms", ModuleManager.CAT_OPTIMIZATION);
	}
}

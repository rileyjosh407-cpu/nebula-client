package dev.nebula.module;

import dev.nebula.module.impl.CoordsHud;
import dev.nebula.module.impl.FarmFpsOptimizer;
import dev.nebula.module.impl.RegionMapHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
	public static final String CAT_OPTIMIZATION = "Optimization";
	public static final String CAT_VISUAL = "Visual";
	public static final String[] CATEGORIES = {CAT_OPTIMIZATION, CAT_VISUAL};

	public static final List<Module> MODULES = new ArrayList<>();

	public static FarmFpsOptimizer FARM;
	public static RegionMapHud REGION_MAP;
	public static CoordsHud COORDS;

	/** Client-wide option, persisted in configs. */
	public static boolean notifyOnToggle = true;

	private static final boolean[] wasDown = new boolean[512];

	private ModuleManager() {}

	public static void init() {
		FARM = register(new FarmFpsOptimizer());
		REGION_MAP = register(new RegionMapHud());
		COORDS = register(new CoordsHud());
	}

	private static <T extends Module> T register(T m) {
		MODULES.add(m);
		return m;
	}

	public static List<Module> inCategory(String category) {
		List<Module> out = new ArrayList<>();
		for (Module m : MODULES) if (m.category.equals(category)) out.add(m);
		return out;
	}

	public static int enabledCount() {
		int n = 0;
		for (Module m : MODULES) if (m.enabled) n++;
		return n;
	}

	/** Polls per-module keybinds (rising edge toggles). Skipped while any screen is open. */
	public static void tick(MinecraftClient mc) {
		if (mc.currentScreen != null || mc.getWindow() == null) return;
		for (Module m : MODULES) {
			if (m.key < 0 || m.key >= wasDown.length) continue;
			boolean down = InputUtil.isKeyPressed(mc.getWindow(), m.key);
			if (down && !wasDown[m.key]) m.toggle();
			wasDown[m.key] = down;
		}
	}
}

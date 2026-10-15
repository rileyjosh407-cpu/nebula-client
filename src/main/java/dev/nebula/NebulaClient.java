package dev.nebula;

import dev.nebula.config.ConfigManager;
import dev.nebula.gui.Hud;
import dev.nebula.gui.ModuleScreen;
import dev.nebula.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class NebulaClient implements ClientModInitializer {
	public static final String ID = "nebula";
	private static final String DEFAULT_CONFIG = "default";

	private static KeyBinding openMenu;

	@Override
	public void onInitializeClient() {
		ConfigManager.init();
		ModuleManager.init();
		ConfigManager.load(DEFAULT_CONFIG);

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of(ID, "main"));
		openMenu = KeyBindingHelper.registerKeyBinding(
			new KeyBinding("key.nebula.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F7, category));

		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			while (openMenu.wasPressed()) {
				if (mc.currentScreen == null) mc.setScreen(new ModuleScreen());
			}
			ModuleManager.tick(mc);
		});

		HudElementRegistry.addLast(Identifier.of(ID, "hud"), (ctx, tickCounter) -> Hud.render(ctx));

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigManager.save(DEFAULT_CONFIG));
	}
}

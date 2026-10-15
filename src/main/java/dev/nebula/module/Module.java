package dev.nebula.module;

import dev.nebula.gui.Toasts;

import java.util.ArrayList;
import java.util.List;

public class Module {
	public final String name;
	public final String description;
	public final String category;
	public final List<Setting> settings = new ArrayList<>();

	public boolean enabled;
	/** GLFW key code, or -1 when unbound. */
	public int key = -1;
	/** UI state only. */
	public boolean expanded;

	public Module(String name, String description, String category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <T extends Setting> T add(T setting) {
		settings.add(setting);
		return setting;
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean on) {
		if (on == enabled) return;
		enabled = on;
		if (on) onEnable(); else onDisable();
		if (ModuleManager.notifyOnToggle) Toasts.push(name, on ? "Enabled" : "Disabled");
	}

	public void onEnable() {}

	public void onDisable() {}
}

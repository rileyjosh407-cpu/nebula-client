package dev.nebula.module.impl;

import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.module.Setting;

public class CoordsHud extends Module {
	public final Setting.Bool showFacing = add(new Setting.Bool("Facing", "Shows the direction you are facing.", true));
	public final Setting.Choice corner = add(new Setting.Choice("Corner", "Where the panel sits on screen.", 0, "Top Left", "Bottom Left", "Bottom Right"));

	public CoordsHud() {
		super("Coordinates", "Shows your position on screen", ModuleManager.CAT_VISUAL);
		this.enabled = true;
	}
}

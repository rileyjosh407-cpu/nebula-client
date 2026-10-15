package dev.nebula.module.impl;

import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.module.Setting;

/** A coordinate-grid overlay: shows which large region cell you are standing in and its neighbours. */
public class RegionMapHud extends Module {
	/** Cell size is stored in thousands of blocks so the slider stays small. */
	public final Setting.Slider cellSizeK = add(new Setting.Slider("Cell Size (k blocks)", "Width of each region cell, in thousands of blocks.", 1, 250, 50));
	public final Setting.Slider gridSize = add(new Setting.Slider("Grid Size", "Cells shown per side (odd numbers centre on you).", 3, 9, 7));
	public final Setting.Bool showCoords = add(new Setting.Bool("Show Coordinates", "Prints X/Z under the grid.", true));

	public RegionMapHud() {
		super("Region Map", "Shows your region on a coordinate grid", ModuleManager.CAT_VISUAL);
	}

	public int cellSize() {
		return cellSizeK.value * 1000;
	}
}

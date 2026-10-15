package dev.nebula.gui;

public final class Theme {
	public static final String[] NAMES = {"Violet", "Azure", "Emerald", "Amber", "Rose"};
	public static final int[] ACCENTS = {0xFF8B7CFF, 0xFF4DA3FF, 0xFF3DDC97, 0xFFFFB347, 0xFFFF6B8B};

	public static int index = 0;

	public static final int BG = 0xF2101018;
	public static final int SIDEBAR = 0xFF0B0B13;
	public static final int ROW = 0xFF171722;
	public static final int ROW_HOVER = 0xFF1E1E2C;
	public static final int TEXT = 0xFFE8E8F0;
	public static final int MUTED = 0xFF8A8AA0;
	public static final int OFF = 0xFF34344A;

	private Theme() {}

	public static int accent() {
		return ACCENTS[Math.floorMod(index, ACCENTS.length)];
	}

	public static int accentDim() {
		int a = accent();
		return 0xFF000000 | (((a >> 16 & 0xFF) / 3) << 16) | (((a >> 8 & 0xFF) / 3) << 8) | ((a & 0xFF) / 3);
	}
}

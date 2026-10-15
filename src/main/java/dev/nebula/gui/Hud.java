package dev.nebula.gui;

import dev.nebula.module.ModuleManager;
import dev.nebula.module.impl.CoordsHud;
import dev.nebula.module.impl.RegionMapHud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/** Everything drawn on the in-game HUD. */
public final class Hud {
	private Hud() {}

	public static void render(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.options.hudHidden) return;

		if (ModuleManager.REGION_MAP.enabled) renderRegionMap(ctx, mc, ModuleManager.REGION_MAP);
		if (ModuleManager.COORDS.enabled) renderCoords(ctx, mc, ModuleManager.COORDS);
		Toasts.render(ctx);
	}

	private static void renderRegionMap(DrawContext ctx, MinecraftClient mc, RegionMapHud m) {
		int n = m.gridSize.value;
		int cell = 14;
		int pad = 6;
		int w = n * cell + pad * 2;
		int h = n * cell + pad * 2 + (m.showCoords.value ? 22 : 0) + 12;
		int x0 = ctx.getScaledWindowWidth() - w - 6;
		int y0 = 6;

		ctx.fill(x0, y0, x0 + w, y0 + h, 0xE0101018);
		ctx.drawText(mc.textRenderer, "REGION MAP", x0 + pad, y0 + 4, Theme.MUTED, false);

		int size = m.cellSize();
		int px = (int) Math.floor(mc.player.getX());
		int pz = (int) Math.floor(mc.player.getZ());
		int pcx = Math.floorDiv(px, size);
		int pcz = Math.floorDiv(pz, size);
		int half = n / 2;
		int gy = y0 + 14;

		for (int r = 0; r < n; r++) {
			for (int c = 0; c < n; c++) {
				int cx = pcx - half + c;
				int cz = pcz - half + r;
				// Stable per-cell colour so neighbouring regions are easy to tell apart.
				int hash = (cx * 73856093) ^ (cz * 19349663);
				int hue = Math.floorMod(hash, 360);
				int color = hsv(hue, 0.45f, 0.55f);
				int x = x0 + pad + c * cell;
				int y = gy + r * cell;
				ctx.fill(x, y, x + cell - 1, y + cell - 1, color);
				if (cx == pcx && cz == pcz) {
					int a = Theme.accent();
					ctx.fill(x - 1, y - 1, x + cell, y, a);
					ctx.fill(x - 1, y + cell - 1, x + cell, y + cell, a);
					ctx.fill(x - 1, y, x, y + cell - 1, a);
					ctx.fill(x + cell - 1, y, x + cell, y + cell - 1, a);
				}
			}
		}

		if (m.showCoords.value) {
			int ty = gy + n * cell + 4;
			ctx.drawText(mc.textRenderer, "X: " + px + "  Z: " + pz, x0 + pad, ty, Theme.TEXT, false);
			ctx.drawText(mc.textRenderer, "Cell " + pcx + ", " + pcz, x0 + pad, ty + 10, Theme.MUTED, false);
		}
	}

	private static void renderCoords(DrawContext ctx, MinecraftClient mc, CoordsHud m) {
		String xyz = String.format("XYZ  %d  %d  %d",
			(int) Math.floor(mc.player.getX()), (int) Math.floor(mc.player.getY()), (int) Math.floor(mc.player.getZ()));
		String facing = "Facing  " + mc.player.getHorizontalFacing().name().toLowerCase(java.util.Locale.ROOT);
		int lines = m.showFacing.value ? 2 : 1;
		int w = Math.max(mc.textRenderer.getWidth(xyz), mc.textRenderer.getWidth(facing)) + 12;
		int h = lines * 10 + 8;
		int x;
		int y;
		switch (m.corner.index) {
			case 1 -> { x = 6; y = ctx.getScaledWindowHeight() - h - 40; }
			case 2 -> { x = ctx.getScaledWindowWidth() - w - 6; y = ctx.getScaledWindowHeight() - h - 40; }
			default -> { x = 6; y = 6; }
		}
		ctx.fill(x, y, x + w, y + h, 0xE0101018);
		ctx.fill(x, y, x + 2, y + h, Theme.accent());
		ctx.drawText(mc.textRenderer, xyz, x + 8, y + 4, Theme.TEXT, false);
		if (m.showFacing.value) ctx.drawText(mc.textRenderer, facing, x + 8, y + 14, Theme.MUTED, false);
	}

	private static int hsv(int hue, float s, float v) {
		return 0xFF000000 | java.awt.Color.HSBtoRGB(hue / 360f, s, v) & 0x00FFFFFF;
	}
}

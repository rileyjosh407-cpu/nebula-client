package dev.nebula.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Small self-contained toast notifications drawn on the HUD. */
public final class Toasts {
	private record Toast(String title, String message, long expiresAt) {}

	private static final List<Toast> ACTIVE = new ArrayList<>();
	private static final long LIFETIME_MS = 2500;

	private Toasts() {}

	public static synchronized void push(String title, String message) {
		ACTIVE.add(new Toast(title, message, System.currentTimeMillis() + LIFETIME_MS));
		while (ACTIVE.size() > 5) ACTIVE.remove(0);
	}

	public static synchronized void render(DrawContext ctx) {
		MinecraftClient mc = MinecraftClient.getInstance();
		long now = System.currentTimeMillis();
		ACTIVE.removeIf(t -> t.expiresAt < now);
		int y = 8;
		int w = 130;
		int x = ctx.getScaledWindowWidth() / 2 - w / 2;
		for (Toast t : ACTIVE) {
			ctx.fill(x, y, x + w, y + 26, 0xE0101018);
			ctx.fill(x, y, x + 2, y + 26, Theme.accent());
			ctx.drawText(mc.textRenderer, t.title(), x + 8, y + 4, Theme.TEXT, false);
			ctx.drawText(mc.textRenderer, t.message(), x + 8, y + 14, Theme.MUTED, false);
			y += 30;
		}
	}
}

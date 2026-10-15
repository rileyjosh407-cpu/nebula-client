package dev.nebula.gui;

import dev.nebula.config.ConfigManager;
import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.module.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.IntConsumer;

/**
 * The main menu. Drawn immediate-mode: every frame the layout is rebuilt and clickable regions
 * ("hits") are recorded, so there are no widget objects to keep in sync with module state.
 */
public class ModuleScreen extends Screen {
	private record Hit(int x1, int y1, int x2, int y2, Runnable click, IntConsumer drag) {
		boolean contains(int x, int y) {
			return x >= x1 && x < x2 && y >= y1 && y < y2;
		}
	}

	private static final String PAGE_SETTINGS = "Settings";
	private static final String PAGE_THEME = "Theme";
	private static final String PAGE_CONFIGS = "Configs";
	private static final String PAGE_KEYBINDS = "Keybinds";
	private static final String[] GENERAL_PAGES = {PAGE_SETTINGS, PAGE_THEME, PAGE_CONFIGS, PAGE_KEYBINDS};

	private final List<Hit> sideHits = new ArrayList<>();
	private final List<Hit> listHits = new ArrayList<>();
	private Hit searchHit;
	private Hit dragging;

	private String page = ModuleManager.CAT_OPTIMIZATION;
	private String search = "";
	private boolean searchFocused;
	private Module binding;
	private int scroll;
	private int maxScroll;
	private int mx, my;

	// Layout, recomputed every frame.
	private int px, py, pw, ph, sidebarW, cx, cw, listTop, listBottom;

	public ModuleScreen() {
		super(Text.literal("Nebula"));
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		ConfigManager.save("default");
		super.close();
	}

	// ------------------------------------------------------------------ rendering

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		mx = mouseX;
		my = mouseY;
		sideHits.clear();
		listHits.clear();
		searchHit = null;

		if (dragging != null && dragging.drag() != null) dragging.drag().accept(mouseX);

		pw = Math.min(560, width - 20);
		ph = Math.min(340, height - 20);
		px = (width - pw) / 2;
		py = (height - ph) / 2;
		sidebarW = 128;
		cx = px + sidebarW + 14;
		cw = pw - sidebarW - 28;
		listTop = py + 58;
		listBottom = py + ph - 10;

		ctx.fill(0, 0, width, height, 0x90000000);
		ctx.fill(px, py, px + pw, py + ph, Theme.BG);
		ctx.fill(px, py, px + sidebarW, py + ph, Theme.SIDEBAR);
		ctx.fill(px, py, px + pw, py + 1, Theme.accent());

		drawSidebar(ctx);
		drawHeader(ctx);

		ctx.enableScissor(cx - 2, listTop, cx + cw + 2, listBottom);
		int end = drawPage(ctx, listTop - scroll);
		ctx.disableScissor();
		maxScroll = Math.max(0, end + scroll - listBottom);
		scroll = Math.max(0, Math.min(maxScroll, scroll));
	}

	private void drawSidebar(DrawContext ctx) {
		int x = px + 12;
		int y = py + 14;
		ctx.drawText(textRenderer, "Nebula", x, y, Theme.accent(), false);
		ctx.drawText(textRenderer, "Client 1.0.0", x, y + 11, Theme.MUTED, false);

		y += 34;
		ctx.drawText(textRenderer, "MODULES", x, y, Theme.MUTED, false);
		y += 14;
		for (String cat : ModuleManager.CATEGORIES) {
			y = sidebarRow(ctx, y, cat, String.valueOf(ModuleManager.inCategory(cat).size()));
		}
		y += 10;
		ctx.drawText(textRenderer, "GENERAL", x, y, Theme.MUTED, false);
		y += 14;
		for (String p : GENERAL_PAGES) {
			y = sidebarRow(ctx, y, p, "");
		}
	}

	private int sidebarRow(DrawContext ctx, int y, String label, String badge) {
		int x1 = px + 8;
		int x2 = px + sidebarW - 8;
		boolean selected = label.equals(page) && search.isEmpty();
		boolean hover = mx >= x1 && mx < x2 && my >= y && my < y + 20;
		if (selected) {
			ctx.fill(x1, y, x2, y + 20, Theme.accentDim());
			ctx.fill(x1, y, x1 + 2, y + 20, Theme.accent());
		} else if (hover) {
			ctx.fill(x1, y, x2, y + 20, Theme.ROW);
		}
		ctx.drawText(textRenderer, label, x1 + 10, y + 6, selected ? Theme.TEXT : Theme.MUTED, false);
		if (!badge.isEmpty()) {
			ctx.drawText(textRenderer, badge, x2 - 6 - textRenderer.getWidth(badge), y + 6, Theme.MUTED, false);
		}
		final String target = label;
		sideHits.add(new Hit(x1, y, x2, y + 20, () -> {
			page = target;
			search = "";
			scroll = 0;
			binding = null;
		}, null));
		return y + 22;
	}

	private void drawHeader(DrawContext ctx) {
		String title = search.isEmpty() ? page : "Search";
		ctx.drawText(textRenderer, title, cx, py + 14, Theme.TEXT, false);
		String sub;
		if (isCategoryPage() || !search.isEmpty()) {
			sub = ModuleManager.MODULES.size() + " modules · " + ModuleManager.enabledCount() + " enabled";
		} else {
			sub = "Nebula Client";
		}
		ctx.drawText(textRenderer, sub, cx, py + 28, Theme.MUTED, false);

		int sw = 150;
		int sx = px + pw - sw - 14;
		int sy = py + 12;
		ctx.fill(sx, sy, sx + sw, sy + 20, searchFocused ? Theme.ROW_HOVER : Theme.ROW);
		if (searchFocused) {
			ctx.fill(sx, sy + 19, sx + sw, sy + 20, Theme.accent());
		}
		String shown = search.isEmpty() && !searchFocused ? "Search modules" : search + (searchFocused && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : "");
		ctx.drawText(textRenderer, shown, sx + 8, sy + 6, search.isEmpty() ? Theme.MUTED : Theme.TEXT, false);
		searchHit = new Hit(sx, sy, sx + sw, sy + 20, () -> searchFocused = true, null);
	}

	private boolean isCategoryPage() {
		for (String c : ModuleManager.CATEGORIES) if (c.equals(page)) return true;
		return false;
	}

	/** Draws the active page starting at y and returns the y just below the last row. */
	private int drawPage(DrawContext ctx, int y) {
		if (!search.isEmpty()) {
			String q = search.toLowerCase(Locale.ROOT);
			for (Module m : ModuleManager.MODULES) {
				if (m.name.toLowerCase(Locale.ROOT).contains(q) || m.description.toLowerCase(Locale.ROOT).contains(q)) {
					y = moduleCard(ctx, y, m);
				}
			}
			return y;
		}
		if (isCategoryPage()) {
			for (Module m : ModuleManager.inCategory(page)) y = moduleCard(ctx, y, m);
			return y;
		}
		return switch (page) {
			case PAGE_SETTINGS -> drawSettingsPage(ctx, y);
			case PAGE_THEME -> drawThemePage(ctx, y);
			case PAGE_CONFIGS -> drawConfigsPage(ctx, y);
			case PAGE_KEYBINDS -> drawKeybindsPage(ctx, y);
			default -> y;
		};
	}

	// ------------------------------------------------------------------ module cards

	private int moduleCard(DrawContext ctx, int y, Module m) {
		int h = 28;
		boolean hover = inList(mx, my) && mx >= cx && mx < cx + cw && my >= y && my < y + h;
		ctx.fill(cx, y, cx + cw, y + h, hover ? Theme.ROW_HOVER : Theme.ROW);
		if (m.enabled) ctx.fill(cx, y, cx + 2, y + h, Theme.accent());

		ctx.drawText(textRenderer, m.name, cx + 10, y + 6, Theme.TEXT, false);
		ctx.drawText(textRenderer, m.description, cx + 10, y + 17, Theme.MUTED, false);
		ctx.drawText(textRenderer, m.expanded ? "v" : ">", cx + cw - 56, y + 10, Theme.MUTED, false);
		drawSwitch(ctx, cx + cw - 34, y + 9, m.enabled);

		addListHit(cx, y, cx + cw - 40, y + h, () -> m.expanded = !m.expanded, null);
		addListHit(cx + cw - 40, y, cx + cw, y + h, m::toggle, null);
		y += h + 3;

		if (m.expanded) {
			for (Setting s : m.settings) y = settingRow(ctx, y, s);
			y = bindRow(ctx, y, m);
			y += 4;
		}
		return y;
	}

	private int settingRow(DrawContext ctx, int y, Setting s) {
		int h = 24;
		int x = cx + 8;
		int w = cw - 8;
		ctx.fill(x, y, x + w, y + h, 0xFF12121B);
		ctx.drawText(textRenderer, s.name, x + 10, y + 4, Theme.TEXT, false);
		ctx.drawText(textRenderer, trim(s.description, w - 190), x + 10, y + 14, Theme.MUTED, false);

		if (s instanceof Setting.Bool b) {
			drawSwitch(ctx, x + w - 34, y + 7, b.value);
			addListHit(x, y, x + w, y + h, () -> b.value = !b.value, null);
		} else if (s instanceof Setting.Slider sl) {
			int trackW = 100;
			int tx = x + w - trackW - 44;
			int ty = y + 11;
			ctx.fill(tx, ty, tx + trackW, ty + 3, Theme.OFF);
			int fillW = (int) (trackW * sl.fraction());
			ctx.fill(tx, ty, tx + fillW, ty + 3, Theme.accent());
			ctx.fill(tx + fillW - 2, ty - 3, tx + fillW + 2, ty + 6, Theme.TEXT);
			String v = String.valueOf(sl.value);
			ctx.drawText(textRenderer, v, x + w - 8 - textRenderer.getWidth(v), y + 8, Theme.TEXT, false);
			final int trackX = tx;
			final int trackWidth = trackW;
			addListHit(tx - 4, y, tx + trackW + 4, y + h, () -> sl.setFromFraction((mx - trackX) / (double) trackWidth),
				mouseX -> sl.setFromFraction((mouseX - trackX) / (double) trackWidth));
		} else if (s instanceof Setting.Choice c) {
			int bw = 96;
			int bx = x + w - bw - 8;
			ctx.fill(bx, y + 4, bx + bw, y + h - 4, Theme.ROW);
			String v = trim(c.get(), bw - 12);
			ctx.drawText(textRenderer, v, bx + 6, y + 8, Theme.TEXT, false);
			addListHit(bx, y, bx + bw, y + h, c::next, null);
		}
		return y + h + 2;
	}

	private int bindRow(DrawContext ctx, int y, Module m) {
		int h = 24;
		int x = cx + 8;
		int w = cw - 8;
		ctx.fill(x, y, x + w, y + h, 0xFF12121B);
		ctx.drawText(textRenderer, "Bind", x + 10, y + 4, Theme.TEXT, false);
		ctx.drawText(textRenderer, "Press a key to toggle this module", x + 10, y + 14, Theme.MUTED, false);
		keyButton(ctx, x + w - 96 - 8, y + 4, 96, h - 8, m);
		return y + h + 2;
	}

	private void keyButton(DrawContext ctx, int bx, int by, int bw, int bh, Module m) {
		boolean waiting = binding == m;
		ctx.fill(bx, by, bx + bw, by + bh, waiting ? Theme.accentDim() : Theme.ROW);
		String label = waiting ? "Press a key..." : keyName(m.key);
		ctx.drawText(textRenderer, label, bx + 6, by + 4, waiting ? Theme.TEXT : Theme.MUTED, false);
		addListHit(bx, by, bx + bw, by + bh, () -> binding = waiting ? null : m, null);
	}

	// ------------------------------------------------------------------ general pages

	private int drawSettingsPage(DrawContext ctx, int y) {
		int h = 28;
		ctx.fill(cx, y, cx + cw, y + h, Theme.ROW);
		ctx.drawText(textRenderer, "Toggle notifications", cx + 10, y + 6, Theme.TEXT, false);
		ctx.drawText(textRenderer, "Shows a toast when a module is switched on or off", cx + 10, y + 17, Theme.MUTED, false);
		drawSwitch(ctx, cx + cw - 34, y + 9, ModuleManager.notifyOnToggle);
		addListHit(cx, y, cx + cw, y + h, () -> ModuleManager.notifyOnToggle = !ModuleManager.notifyOnToggle, null);
		y += h + 3;

		ctx.fill(cx, y, cx + cw, y + h, Theme.ROW);
		ctx.drawText(textRenderer, "Menu key", cx + 10, y + 6, Theme.TEXT, false);
		ctx.drawText(textRenderer, "Right Shift by default. Change it in Options > Controls > Key Binds.", cx + 10, y + 17, Theme.MUTED, false);
		return y + h + 3;
	}

	private int drawThemePage(DrawContext ctx, int y) {
		for (int i = 0; i < Theme.NAMES.length; i++) {
			final int idx = i;
			int h = 28;
			boolean selected = Theme.index == i;
			ctx.fill(cx, y, cx + cw, y + h, selected ? Theme.ROW_HOVER : Theme.ROW);
			ctx.fill(cx + 10, y + 8, cx + 22, y + 20, Theme.ACCENTS[i]);
			ctx.drawText(textRenderer, Theme.NAMES[i], cx + 30, y + 10, Theme.TEXT, false);
			if (selected) ctx.drawText(textRenderer, "Selected", cx + cw - 12 - textRenderer.getWidth("Selected"), y + 10, Theme.MUTED, false);
			addListHit(cx, y, cx + cw, y + h, () -> Theme.index = idx, null);
			y += h + 3;
		}
		return y;
	}

	private int drawConfigsPage(DrawContext ctx, int y) {
		int h = 28;
		ctx.fill(cx, y, cx + cw, y + h, Theme.accentDim());
		ctx.drawText(textRenderer, "Save current settings as a new config", cx + 10, y + 10, Theme.TEXT, false);
		addListHit(cx, y, cx + cw, y + h, () -> {
			String name = "config-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
			ConfigManager.save(name);
			Toasts.push("Config saved", name);
		}, null);
		y += h + 6;

		for (String name : ConfigManager.list()) {
			ctx.fill(cx, y, cx + cw, y + h, Theme.ROW);
			ctx.drawText(textRenderer, name, cx + 10, y + 6, Theme.TEXT, false);
			ctx.drawText(textRenderer, "Click to load", cx + 10, y + 17, Theme.MUTED, false);
			addListHit(cx, y, cx + cw, y + h, () -> {
				boolean ok = ConfigManager.load(name);
				Toasts.push(ok ? "Config loaded" : "Load failed", name);
			}, null);
			y += h + 3;
		}
		return y;
	}

	private int drawKeybindsPage(DrawContext ctx, int y) {
		for (Module m : ModuleManager.MODULES) {
			int h = 26;
			ctx.fill(cx, y, cx + cw, y + h, Theme.ROW);
			ctx.drawText(textRenderer, m.name, cx + 10, y + 9, Theme.TEXT, false);
			keyButton(ctx, cx + cw - 104, y + 4, 96, h - 8, m);
			y += h + 3;
		}
		return y;
	}

	// ------------------------------------------------------------------ helpers

	private void drawSwitch(DrawContext ctx, int x, int y, boolean on) {
		ctx.fill(x, y, x + 24, y + 12, on ? Theme.accent() : Theme.OFF);
		int kx = on ? x + 13 : x + 1;
		ctx.fill(kx, y + 1, kx + 10, y + 11, 0xFFFFFFFF);
	}

	private String trim(String s, int maxWidth) {
		if (textRenderer.getWidth(s) <= maxWidth) return s;
		while (s.length() > 1 && textRenderer.getWidth(s + "...") > maxWidth) s = s.substring(0, s.length() - 1);
		return s + "...";
	}

	private boolean inList(int x, int y) {
		return y >= listTop && y < listBottom;
	}

	private void addListHit(int x1, int y1, int x2, int y2, Runnable click, IntConsumer drag) {
		if (y2 < listTop || y1 > listBottom) return;
		listHits.add(new Hit(x1, y1, x2, y2, click, drag));
	}

	private static String keyName(int key) {
		if (key < 0) return "None";
		String n = GLFW.glfwGetKeyName(key, 0);
		if (n != null) return n.toUpperCase(Locale.ROOT);
		return switch (key) {
			case GLFW.GLFW_KEY_SPACE -> "SPACE";
			case GLFW.GLFW_KEY_LEFT_SHIFT -> "L-SHIFT";
			case GLFW.GLFW_KEY_RIGHT_SHIFT -> "R-SHIFT";
			case GLFW.GLFW_KEY_LEFT_CONTROL -> "L-CTRL";
			case GLFW.GLFW_KEY_RIGHT_CONTROL -> "R-CTRL";
			case GLFW.GLFW_KEY_LEFT_ALT -> "L-ALT";
			case GLFW.GLFW_KEY_RIGHT_ALT -> "R-ALT";
			case GLFW.GLFW_KEY_TAB -> "TAB";
			case GLFW.GLFW_KEY_UP -> "UP";
			case GLFW.GLFW_KEY_DOWN -> "DOWN";
			case GLFW.GLFW_KEY_LEFT -> "LEFT";
			case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
			default -> (key >= GLFW.GLFW_KEY_F1 && key <= GLFW.GLFW_KEY_F12) ? "F" + (key - GLFW.GLFW_KEY_F1 + 1) : "KEY " + key;
		};
	}

	// ------------------------------------------------------------------ input

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		if (click.button() != 0) return super.mouseClicked(click, doubled);

		searchFocused = false;
		for (Hit h : sideHits) {
			if (h.contains(mx, my)) {
				h.click().run();
				return true;
			}
		}
		if (searchHit != null && searchHit.contains(mx, my)) {
			searchHit.click().run();
			return true;
		}
		if (inList(mx, my)) {
			for (int i = listHits.size() - 1; i >= 0; i--) {
				Hit h = listHits.get(i);
				if (h.contains(mx, my)) {
					h.click().run();
					if (h.drag() != null) dragging = h;
					return true;
				}
			}
		}
		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseReleased(Click click) {
		dragging = null;
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		scroll = (int) Math.max(0, Math.min(maxScroll, scroll - vertical * 22));
		return true;
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		int code = input.getKeycode();

		if (binding != null) {
			binding.key = input.isEscape() ? -1 : code;
			binding = null;
			return true;
		}
		if (searchFocused) {
			if (input.isEscape()) {
				searchFocused = false;
				return true;
			}
			if (code == GLFW.GLFW_KEY_BACKSPACE) {
				if (!search.isEmpty()) search = search.substring(0, search.length() - 1);
				scroll = 0;
				return true;
			}
			if (code == GLFW.GLFW_KEY_ENTER) {
				searchFocused = false;
				return true;
			}
		}
		return super.keyPressed(input);
	}

	@Override
	public boolean charTyped(CharInput input) {
		if (searchFocused && input.isValidChar() && search.length() < 24) {
			search += input.asString();
			scroll = 0;
			return true;
		}
		return super.charTyped(input);
	}
}

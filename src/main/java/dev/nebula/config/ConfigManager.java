package dev.nebula.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.nebula.gui.Theme;
import dev.nebula.module.Module;
import dev.nebula.module.ModuleManager;
import dev.nebula.module.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/** Saves/loads every module's state as JSON files in .minecraft/config/nebula/. */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Path dir;

	private ConfigManager() {}

	public static void init() {
		dir = FabricLoader.getInstance().getConfigDir().resolve("nebula");
		try {
			Files.createDirectories(dir);
		} catch (IOException ignored) {}
	}

	public static List<String> list() {
		List<String> names = new ArrayList<>();
		try (Stream<Path> s = Files.list(dir)) {
			s.map(p -> p.getFileName().toString())
				.filter(n -> n.endsWith(".json"))
				.forEach(n -> names.add(n.substring(0, n.length() - 5)));
		} catch (IOException ignored) {}
		Collections.sort(names);
		return names;
	}

	public static void save(String name) {
		JsonObject root = new JsonObject();
		root.addProperty("theme", Theme.index);
		root.addProperty("notifyOnToggle", ModuleManager.notifyOnToggle);
		JsonObject mods = new JsonObject();
		for (Module m : ModuleManager.MODULES) {
			JsonObject jm = new JsonObject();
			jm.addProperty("enabled", m.enabled);
			jm.addProperty("key", m.key);
			JsonObject js = new JsonObject();
			for (Setting s : m.settings) {
				Object raw = s.getRaw();
				if (raw instanceof Boolean b) js.addProperty(s.name, b);
				else if (raw instanceof Number n) js.addProperty(s.name, n);
			}
			jm.add("settings", js);
			mods.add(m.name, jm);
		}
		root.add("modules", mods);
		try (Writer w = Files.newBufferedWriter(dir.resolve(name + ".json"))) {
			GSON.toJson(root, w);
		} catch (IOException ignored) {}
	}

	public static boolean load(String name) {
		Path file = dir.resolve(name + ".json");
		if (!Files.exists(file)) return false;
		try (Reader r = Files.newBufferedReader(file)) {
			JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
			if (root.has("theme")) Theme.index = root.get("theme").getAsInt();
			if (root.has("notifyOnToggle")) ModuleManager.notifyOnToggle = root.get("notifyOnToggle").getAsBoolean();
			JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
			for (Module m : ModuleManager.MODULES) {
				if (!mods.has(m.name)) continue;
				JsonObject jm = mods.getAsJsonObject(m.name);
				boolean prevNotify = ModuleManager.notifyOnToggle;
				ModuleManager.notifyOnToggle = false; // no toast spam while loading
				if (jm.has("enabled")) m.setEnabled(jm.get("enabled").getAsBoolean());
				ModuleManager.notifyOnToggle = prevNotify;
				if (jm.has("key")) m.key = jm.get("key").getAsInt();
				if (jm.has("settings")) {
					JsonObject js = jm.getAsJsonObject("settings");
					for (Setting s : m.settings) {
						if (!js.has(s.name)) continue;
						JsonElement e = js.get(s.name);
						if (e.getAsJsonPrimitive().isBoolean()) s.setRaw(e.getAsBoolean());
						else s.setRaw(e.getAsInt());
					}
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}

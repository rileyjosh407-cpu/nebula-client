package dev.nebula.module;

/** A single configurable value on a module. Values are stored as Object so configs can round-trip through JSON. */
public abstract class Setting {
	public final String name;
	public final String description;

	protected Setting(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public abstract Object getRaw();

	public abstract void setRaw(Object value);

	public static class Bool extends Setting {
		public boolean value;

		public Bool(String name, String description, boolean def) {
			super(name, description);
			this.value = def;
		}

		@Override public Object getRaw() { return value; }

		@Override public void setRaw(Object v) {
			if (v instanceof Boolean b) value = b;
		}
	}

	public static class Slider extends Setting {
		public final int min, max;
		public int value;

		public Slider(String name, String description, int min, int max, int def) {
			super(name, description);
			this.min = min;
			this.max = max;
			this.value = def;
		}

		@Override public Object getRaw() { return value; }

		@Override public void setRaw(Object v) {
			if (v instanceof Number n) value = Math.max(min, Math.min(max, n.intValue()));
		}

		public void setFromFraction(double f) {
			value = (int) Math.round(min + Math.max(0, Math.min(1, f)) * (max - min));
		}

		public double fraction() {
			return max == min ? 0 : (value - min) / (double) (max - min);
		}
	}

	public static class Choice extends Setting {
		public final String[] options;
		public int index;

		public Choice(String name, String description, int def, String... options) {
			super(name, description);
			this.options = options;
			this.index = def;
		}

		public String get() { return options[index]; }

		public void next() { index = (index + 1) % options.length; }

		@Override public Object getRaw() { return index; }

		@Override public void setRaw(Object v) {
			if (v instanceof Number n && n.intValue() >= 0 && n.intValue() < options.length) index = n.intValue();
		}
	}
}

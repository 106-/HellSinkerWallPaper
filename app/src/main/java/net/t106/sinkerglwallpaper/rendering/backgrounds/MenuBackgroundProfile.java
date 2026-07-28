package net.t106.sinkerglwallpaper.rendering.backgrounds;

import net.t106.sinkerglwallpaper.R;

/**
 * Values recovered from the original menu-background routine.
 *
 * <p>Coordinates and animation periods remain in the original 640 x 480
 * coordinate system. MenuSpriteRenderer applies the Android wallpaper's
 * existing display scale when converting them to the logical viewport.</p>
 */
public enum MenuBackgroundProfile {
	GRAVEYARD(
		"graveyard",
		R.drawable.menu_graveyard_rgb,
		R.drawable.menu_graveyard_alpha,
		0xFF464F5B,
		true,
		0,
		2
	) {
		@Override
		public void configureSprites(long tick, MenuSpriteRenderer.SpriteState[] sprites) {
			sprites[0].set(320.0f, 240.0f, 1.5f, -1.5f,
				reverseTurns(tick, 1920, 1919), 0xFF2D343F);
			sprites[1].set(320.0f, 240.0f, 1.0f, 1.0f,
				forwardTurns(tick, 2880), 0xFF736355);
		}
	},

	DRIVING_FACTOR(
		"driving_factor",
		R.drawable.menu_driving_factor_rgb,
		R.drawable.menu_driving_factor_alpha,
		0xFF55442E,
		false,
		0,
		3
	) {
		@Override
		public void configureSprites(long tick, MenuSpriteRenderer.SpriteState[] sprites) {
			sprites[0].set(396.0f, 256.0f, 1.0f, 1.0f,
				forwardTurns(tick, 3840), 0xFF0D0D0D);
			sprites[1].set(140.0f, 0.0f, 1.0f, 1.0f,
				forwardTurns(tick, 2880), 0xFF5C91B4);
			sprites[2].set(500.0f, 480.0f, 1.0f, -1.0f,
				reverseTurns(tick, 2880, 2879), 0xFF5C91B4);
		}
	},

	LEFT_BEHIND(
		"left_behind",
		R.drawable.menu_left_behind_rgb,
		R.drawable.menu_left_behind_alpha,
		0xFF401060,
		true,
		0x40606060,
		2
	) {
		@Override
		public void configureSprites(long tick, MenuSpriteRenderer.SpriteState[] sprites) {
			sprites[0].set(320.0f, 240.0f, 1.5f, -1.5f,
				reverseTurns(tick, 1920, 1919), 0xFF400810);
			sprites[1].set(320.0f, 240.0f, 1.0f, 1.0f,
				forwardTurns(tick, 2880), 0xFF40C0FF);
		}
	},

	MEMENTO(
		"memento",
		R.drawable.menu_memento_rgb,
		R.drawable.menu_memento_alpha,
		0xFFFFFFFF,
		false,
		0x40808080,
		3
	) {
		@Override
		public void configureSprites(long tick, MenuSpriteRenderer.SpriteState[] sprites) {
			int phase = (int)(((tick % 2400L) * 4096L) / 2400L) & 4095;
			double angle = phase * (Math.PI * 2.0 / 4096.0);

			// The original reads float lookup tables and truncates toward zero.
			int cx = (int)((float)Math.cos(angle) * 20000.0f);
			int cy = (int)((float)Math.sin(angle) * 20000.0f);
			float orbitAX = 320.0f + cx / 100.0f;
			float orbitAY = 240.0f + cy / 100.0f;
			float orbitBX = 320.0f - cx / 80.0f;
			float orbitBY = 240.0f - cy / 80.0f;

			sprites[0].set(orbitAX, orbitAY, 0.5f, 0.5f,
				reverseTurns(tick, 3840, 1919), 0xFFC0C0C0);
			sprites[1].set(orbitAX, orbitAY, 1.0f, 1.0f,
				forwardTurns(tick, 4800), 0xFFC0C0C0);
			sprites[2].set(orbitBX, orbitBY, 0.75f, 0.75f,
				forwardTurns(tick, 1440), 0xFFC0C0C0);
		}
	};

	public static final String PREFERENCE_KEY = "background_type";
	public static final String DEFAULT_VALUE = "left_behind";

	public final String preferenceValue;
	public final int rgbResource;
	public final int alphaResource;
	public final int baseColor;
	public final boolean invertRightHalf;
	public final int overlayColor;
	public final int spriteCount;

	MenuBackgroundProfile(
		String preferenceValue,
		int rgbResource,
		int alphaResource,
		int baseColor,
		boolean invertRightHalf,
		int overlayColor,
		int spriteCount
	) {
		this.preferenceValue = preferenceValue;
		this.rgbResource = rgbResource;
		this.alphaResource = alphaResource;
		this.baseColor = baseColor;
		this.invertRightHalf = invertRightHalf;
		this.overlayColor = overlayColor;
		this.spriteCount = spriteCount;
	}

	public abstract void configureSprites(
		long tick, MenuSpriteRenderer.SpriteState[] sprites);

	public static MenuBackgroundProfile fromPreference(String value) {
		for (MenuBackgroundProfile profile : values()) {
			if (profile.preferenceValue.equals(value)) {
				return profile;
			}
		}
		return LEFT_BEHIND;
	}

	private static float forwardTurns(long tick, int period) {
		return (tick % period) / (float)period;
	}

	private static float reverseTurns(long tick, int period, int numeratorBase) {
		return (numeratorBase - (tick % period)) / (float)period;
	}
}

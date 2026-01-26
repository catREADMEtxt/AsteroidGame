
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;

public final class FontLoader {
	public static Font loadAquirePlain(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/aquire-font/Aquire-BW0ox.otf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
	public static Font loadAquireBold(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/aquire-font/AquireBold-8Ma60.otf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
	public static Font loadAquireLight(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/aquire-font/AquireLight-YzE0o.otf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
	public static Font loadCartesian(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/cartesian-font/Cartesian-0W5Oo.otf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
	public static Font loadEightgonPlain(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/eightgon-font/Eightgon-OGn6p.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
	public static Font loadEightgonItalic(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/eightgon-font/EightgonItalic-Zpw6z.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
	public static Font loadTrenchThin(float size) {
		try {
			Font baseFont = Font.createFont(Font.TRUETYPE_FONT,
					new File("assets/fonts/trench-font/TrenchThin-16R0.otf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(baseFont);

			return baseFont.deriveFont(size);
		} catch (Exception e) {
			e.printStackTrace();
			return new Font("Arial", Font.PLAIN, (int) size); // fallback
		}
	}
}
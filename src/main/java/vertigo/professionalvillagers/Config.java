package vertigo.professionalvillagers;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;

public class Config {

	private static final String SEPARATOR = " = ";
	private static final String QUICK_REROLL = "quickReroll";
	private static final String DISMISS_TRADER = "dismissTrader";
	private static final String RESET_PROFESSION = "resetProfession";
	private static final String LEARN_ENCHANTMENT = "learnEnchantment";
	private static final String LEVEL_ENCHANTMENTS = "levelEnchantments";
	private static final String MODIFIED_TRADES = "modifiedTrades";

	public boolean quickReroll = true;
	public boolean dismissTrader = true;
	public boolean resetProfession = false;
	public boolean learnEnchantment = false;
	public boolean levelEnchantments = true;
	public boolean modifiedTrades = false;

	public Config() {
		if(!read()){
			write();
		}
	}

	public void write() {
		File file = getFile();
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(QUICK_REROLL + SEPARATOR + quickReroll + System.lineSeparator());
			writer.write(DISMISS_TRADER + SEPARATOR + dismissTrader + System.lineSeparator());
			writer.write(RESET_PROFESSION + SEPARATOR + resetProfession + System.lineSeparator());
			writer.write(LEARN_ENCHANTMENT + SEPARATOR + learnEnchantment + System.lineSeparator());
			writer.write(LEVEL_ENCHANTMENTS + SEPARATOR + levelEnchantments + System.lineSeparator());
			writer.write(MODIFIED_TRADES + SEPARATOR + modifiedTrades);
		} catch (IOException e) {
			ProfessionalVillagers.LOGGER.error("Failed to write config ({})", file.getPath());
		}
	}

	public boolean read() {
		File file = getFile();
		if (!file.exists()) {
			return false;
		}
		try (BufferedReader reader = new BufferedReader((new FileReader(file)))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] segments = line.split(SEPARATOR);
				if (segments.length != 2 || segments[0].isEmpty() || segments[1].isEmpty()) {
					continue;
				}
				switch (segments[0]) {
					case QUICK_REROLL -> quickReroll = segments[1].equals("true");
					case DISMISS_TRADER -> dismissTrader = segments[1].equals("true");
					case RESET_PROFESSION -> resetProfession = segments[1].equals("true");
					case LEARN_ENCHANTMENT -> learnEnchantment = segments[1].equals("true");
					case LEVEL_ENCHANTMENTS -> levelEnchantments = segments[1].equals("true");
					case MODIFIED_TRADES -> modifiedTrades = segments[1].equals("true");
				}
			}
		} catch (IOException e) {
			ProfessionalVillagers.LOGGER.error("Failed to read config ({})", file.getPath());
		}
		return true;
	}

	private File getFile() {
		return FabricLoader.getInstance().getGameDir().resolve("config").resolve(ProfessionalVillagers.MOD_ID + ".ini").toFile();
	}

}
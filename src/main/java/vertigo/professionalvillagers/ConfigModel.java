package vertigo.professionalvillagers;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RestartRequired;

@Modmenu(modId = "professional-villagers")
@Config(name = "professional-villagers", wrapperName = "ModConfig")
public class ConfigModel {

	public boolean quickReroll = true;
	public boolean dismissTrader = true;
	public boolean resetProfession = true;
	public boolean learnEnchantment = false;
	@RestartRequired
	public boolean modifiedTrades = false;

}
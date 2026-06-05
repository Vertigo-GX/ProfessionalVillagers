package vertigo.professionalvillagers;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.trading.TradeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProfessionalVillagers implements ModInitializer {

	public static final String MOD_ID = "professional-villagers";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final Config CONFIG = new Config();

	public static final ResourceKey<TradeSet> PUMPKIN = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("professional-villagers", "pumpkin"));

	public static final ResourceKey<TradeSet> COD = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("professional-villagers", "cod"));

	public static final ResourceKey<TradeSet> DIAMOND_HOE = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("professional-villagers", "diamond_hoe"));

	public static final ResourceKey<TradeSet> DIAMOND_SHOVEL = ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath("professional-villagers", "diamond_shovel"));

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing");
	}

}
package vertigo.professionalvillagers;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.village.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ProfessionalVillagers implements ModInitializer {

	public static final String MOD_ID = "professional-villagers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Config CONFIG = new Config();

	private static final int NOVICE = 1;
	private static final int APPRENTICE = 2;
	private static final int JOURNEYMAN = 3;
	private static final int EXPERT = 4;
	private static final int MASTER = 5;

	/**
	 * Initializes the mod on the server side.
	 */
	@Override
	public void onInitialize() {
		LOGGER.info("Initializing");
		if (CONFIG.modifiedTrades) {
			modifyFarmerTrades();
			modifyFishermanTrades();
			modifyToolsmithTrades();
		}
	}

	/**
	 * Modifies the trades of the farmer profession.
	 * <ul>
	 *     <li>Buy pumpkin (apprentice) swapped with sell cookie (journeyman)</li>
	 * </ul>
	 */
	private void modifyFarmerTrades() {
		swapTrades(TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.FARMER).get(APPRENTICE), 0, TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.FARMER).get(JOURNEYMAN), 0);
	}

	/**
	 * Modifies the trades of the fisherman profession.
	 * <ul>
	 *     <li>Buy cod (apprentice) swapped with sell fishing rod (journeyman)</li>
	 * </ul>
	 */
	private void modifyFishermanTrades() {
		swapTrades(TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.FISHERMAN).get(APPRENTICE), 0, TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.FISHERMAN).get(JOURNEYMAN), 1);
	}

	/**
	 * Modifies the trades of the toolsmith profession.
	 * <ul>
	 *     <li>Sell stone axe removed from novice</li>
	 *     <li>Sell enchanted iron axe removed from journeyman</li>
	 *     <li>Sell diamond hoe removed from journeyman</li>
	 *     <li>Sell enchanted iron hoe added to journeyman</li>
	 *     <li>Sell enchanted diamond axe removed from expert</li>
	 *     <li>Sell enchanted diamond hoe added to master</li>
	 * </ul>
	 */
	private void modifyToolsmithTrades() {
		// Level      | Index 0            | Index 1        | Index 2           | Index 3          | Index 4
		// -----------|--------------------|----------------|-------------------|------------------|---------------
		// Novice     | b. coal            | s. stone axe   | s. stone shovel   | s. stone pickaxe | s. stone hoe
		// Apprentice | b. iron ingot      | s. bell        |                   |                  |
		// Journeyman | b. flint           | e. iron axe    | e. iron shovel    | e. iron pickaxe  | s. diamond hoe
		// Expert     | b. diamond         | e. diamond axe | e. diamond shovel |                  |
		// Master     | e. diamond pickaxe |
		Int2ObjectMap<TradeOffers.Factory[]> trades = TradeOffers.PROFESSION_TO_LEVELED_TRADE.get(VillagerProfession.TOOLSMITH);
		TradeOffers.Factory[] factories = trades.get(NOVICE);
		trades.put(NOVICE, new TradeOffers.Factory[]{factories[0], factories[2], factories[3], factories[4]});
		factories = trades.get(JOURNEYMAN);
		trades.put(JOURNEYMAN, new TradeOffers.Factory[]{factories[0], factories[2], factories[3], new TradeOffers.SellEnchantedToolFactory(Items.IRON_HOE, 2, 3, 10, 0.2F)});
		factories = trades.get(EXPERT);
		trades.put(EXPERT, new TradeOffers.Factory[]{factories[0], factories[2]});
		factories = trades.get(MASTER);
		trades.put(MASTER, new TradeOffers.Factory[]{factories[0], new TradeOffers.SellEnchantedToolFactory(Items.DIAMOND_HOE, 9, 3, 20, 0.2F)});
	}

	/**
	 * Swaps the two trades specified by the provided indexes.
	 *
	 * @param factories1 Factory array 1
	 * @param index1     Index for factory array 1
	 * @param factories2 Factory array 2
	 * @param index2     Index for factory array 2
	 */
	private void swapTrades(TradeOffers.Factory[] factories1, int index1, TradeOffers.Factory[] factories2, int index2) {
		TradeOffers.Factory factory = factories1[index1];
		factories1[index1] = factories2[index2];
		factories2[index2] = factory;
	}

}
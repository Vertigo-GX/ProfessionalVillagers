package vertigo.professionalvillagers;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ConfigScreen extends Screen {

	private static final int BUTTON_WIDTH = 310;
	private static final int BUTTON_HEIGHT = 20;

	private final Screen parent;

	private boolean modified = false;

	protected ConfigScreen(Screen parent) {
		super(Text.literal("professional-villagers.options"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
		layout.addHeader(new TextWidget(Text.translatable("professional-villagers.text.optionsTitle"), this.textRenderer));
		GridWidget grid = new GridWidget();
		grid.setRowSpacing(5);
		GridWidget.Adder adder = grid.createAdder(1);
		adder.add(createToggleButton("quickReroll", ProfessionalVillagers.CONFIG.quickReroll, b -> {
			setToggleButtonMessage(b, "quickReroll", ProfessionalVillagers.CONFIG.quickReroll ^= true);
		}));
		adder.add(createToggleButton("dismissTrader", ProfessionalVillagers.CONFIG.dismissTrader, b -> {
			setToggleButtonMessage(b, "dismissTrader", ProfessionalVillagers.CONFIG.dismissTrader ^= true);
		}));
		adder.add(createToggleButton("resetProfession", ProfessionalVillagers.CONFIG.resetProfession, b -> {
			setToggleButtonMessage(b, "resetProfession", ProfessionalVillagers.CONFIG.resetProfession ^= true);
		}));
		adder.add(createToggleButton("learnEnchantment", ProfessionalVillagers.CONFIG.learnEnchantment, b -> {
			setToggleButtonMessage(b, "learnEnchantment", ProfessionalVillagers.CONFIG.learnEnchantment ^= true);
		}));
		adder.add(createToggleButton("levelEnchantments", ProfessionalVillagers.CONFIG.levelEnchantments, b -> {
			setToggleButtonMessage(b, "levelEnchantments", ProfessionalVillagers.CONFIG.levelEnchantments ^= true);
		}));
		adder.add(createToggleButtonWithNote("modifiedTrades", ProfessionalVillagers.CONFIG.modifiedTrades, "requiresRestart", b -> {
			setToggleButtonMessage(b, "modifiedTrades", ProfessionalVillagers.CONFIG.modifiedTrades ^= true);
		}));
		layout.addBody(grid);
		layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, b -> {
			close();
		}).build());
		layout.forEachChild(this::addDrawableChild);
		layout.refreshPositions();
	}

	@Override
	public void close() {
		if (modified) {
			ProfessionalVillagers.CONFIG.write();
		}
		this.client.setScreen(this.parent);
	}

	private ButtonWidget createToggleButton(String key, boolean value, ButtonWidget.PressAction action) {
		return ButtonWidget.builder(ScreenTexts.composeToggleText(Text.translatable("professional-villagers.option." + key), value), action).tooltip(Tooltip.of(Text.translatable("professional-villagers.tooltip." + key))).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
	}

	private ButtonWidget createToggleButtonWithNote(String optionKey, boolean value, String noteKey, ButtonWidget.PressAction action) {
		return ButtonWidget.builder(ScreenTexts.composeToggleText(Text.translatable("professional-villagers.option." + optionKey), value), action).tooltip(Tooltip.of(Text.translatable("professional-villagers.tooltip." + optionKey).append("\n\n").append(Text.translatable("professional-villagers.text." + noteKey).withColor(Formatting.GOLD.getColorValue())))).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
	}

	private void setToggleButtonMessage(ButtonWidget button, String key, boolean value) {
		button.setMessage(ScreenTexts.composeToggleText(Text.translatable("professional-villagers.option." + key), value));
		modified = true;
	}

}
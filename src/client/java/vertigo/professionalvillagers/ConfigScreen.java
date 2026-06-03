package vertigo.professionalvillagers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

	private static final int BUTTON_WIDTH = 310;

	private static final int BUTTON_HEIGHT = 20;

	private final Screen parent;

	private boolean modified = false;

	protected ConfigScreen(Screen parent) {
		super(Component.literal("professional-villagers.options"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
		layout.addToHeader(new StringWidget(Component.translatable("professional-villagers.text.optionsTitle"), this.font));
		GridLayout grid = new GridLayout();
		grid.rowSpacing(5);
		GridLayout.RowHelper adder = grid.createRowHelper(1);
		adder.addChild(createToggleButton("quickReroll", ProfessionalVillagers.CONFIG.quickReroll, b -> setToggleButtonMessage(b, "quickReroll", ProfessionalVillagers.CONFIG.quickReroll ^= true)));
		adder.addChild(createToggleButton("dismissTrader", ProfessionalVillagers.CONFIG.dismissTrader, b -> setToggleButtonMessage(b, "dismissTrader", ProfessionalVillagers.CONFIG.dismissTrader ^= true)));
		adder.addChild(createToggleButton("resetProfession", ProfessionalVillagers.CONFIG.resetProfession, b -> setToggleButtonMessage(b, "resetProfession", ProfessionalVillagers.CONFIG.resetProfession ^= true)));
		adder.addChild(createToggleButton("learnEnchantment", ProfessionalVillagers.CONFIG.learnEnchantment, b -> setToggleButtonMessage(b, "learnEnchantment", ProfessionalVillagers.CONFIG.learnEnchantment ^= true)));
		adder.addChild(createToggleButton("levelEnchantments", ProfessionalVillagers.CONFIG.levelEnchantments, b -> setToggleButtonMessage(b, "levelEnchantments", ProfessionalVillagers.CONFIG.levelEnchantments ^= true)));
		adder.addChild(createToggleButtonWithNote("modifiedTrades", ProfessionalVillagers.CONFIG.modifiedTrades, "requiresRestart", b -> setToggleButtonMessage(b, "modifiedTrades", ProfessionalVillagers.CONFIG.modifiedTrades ^= true)));
		layout.addToContents(grid);
		layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build());
		layout.visitWidgets(this::addRenderableWidget);
		layout.arrangeElements();
	}

	@Override
	public void onClose() {
		if(modified) {
			ProfessionalVillagers.CONFIG.write();
		}
		this.minecraft.setScreen(this.parent);
	}

	private Button createToggleButton(String key, boolean value, Button.OnPress action) {
		return Button.builder(CommonComponents.optionStatus(Component.translatable("professional-villagers.option." + key), value), action).tooltip(Tooltip.create(Component.translatable("professional-villagers.tooltip." + key))).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
	}

	private Button createToggleButtonWithNote(String optionKey, boolean value, String noteKey, Button.OnPress action) {
		return Button.builder(CommonComponents.optionStatus(Component.translatable("professional-villagers.option." + optionKey), value), action).tooltip(Tooltip.create(Component.translatable("professional-villagers.tooltip." + optionKey).append("\n\n").append(Component.translatable("professional-villagers.text." + noteKey).withColor(ChatFormatting.GOLD.getColor())))).size(BUTTON_WIDTH, BUTTON_HEIGHT).build();
	}

	private void setToggleButtonMessage(Button button, String key, boolean value) {
		button.setMessage(CommonComponents.optionStatus(Component.translatable("professional-villagers.option." + key), value));
		modified = true;
	}

}
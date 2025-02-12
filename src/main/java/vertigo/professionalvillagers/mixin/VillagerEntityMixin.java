package vertigo.professionalvillagers.mixin;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.village.*;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vertigo.professionalvillagers.ProfessionalVillagers;

import java.util.Iterator;
import java.util.Optional;

@Mixin(VillagerEntity.class)
public abstract class VillagerEntityMixin extends MerchantEntity {

	public VillagerEntityMixin(EntityType<? extends MerchantEntity> type, World world) {
		super(type, world);
	}

	@Shadow
	public abstract VillagerData getVillagerData();

	@Shadow
	public abstract void setVillagerData(VillagerData villagerData);

	@Shadow
	public abstract int getExperience();

	@Shadow
	public abstract void setExperience(int experience);

	@Shadow
	public abstract void setOffers(TradeOfferList offers);

	/**
	 * When adding trading recipes to a novice-level librarian, set the level of all enchantments to 1.
	 */
	@Inject(method = "fillRecipes", at = @At("TAIL"))
	private void fillRecipesInject(CallbackInfo info) {
		VillagerData data = this.getVillagerData();
		if (data.getProfession() != VillagerProfession.LIBRARIAN || data.getLevel() > VillagerData.MIN_LEVEL) {
			return;
		}
		setEnchantmentLevels(1);
	}

	/**
	 * When a librarian levels up, set the level of all enchantments to the level of the librarian.
	 */
	@Inject(method = "levelUp", at = @At("TAIL"))
	private void levelUpInject(CallbackInfo info) {
		VillagerData data = this.getVillagerData();
		if (data.getProfession() != VillagerProfession.LIBRARIAN) {
			return;
		}
		setEnchantmentLevels(data.getLevel());
	}

	/**
	 * Sets the level of all enchantments to the specified level.
	 *
	 * @param level The enchantment level
	 */
	@Unique
	private void setEnchantmentLevels(int level) {
		for (TradeOffer o : this.getOffers()) {
			ItemStack stack = o.getSellItem();
			if (!stack.isOf(Items.ENCHANTED_BOOK)) {
				continue;
			}
			ItemEnchantmentsComponent component = EnchantmentHelper.getEnchantments(stack);
			ItemEnchantmentsComponent.Builder builder = null;
			// This supports enchanted books with multiple enchantments
			for (RegistryEntry<Enchantment> e : component.getEnchantments()) {
				int enchantmentLevel = component.getLevel(e);
				if (enchantmentLevel == level) {
					continue;
				}
				int maxLevel = e.value().getMaxLevel();
				if (enchantmentLevel < level && enchantmentLevel >= maxLevel) {
					continue;
				}
				if (builder == null) {
					builder = new ItemEnchantmentsComponent.Builder(component);
				}
				builder.set(e, Math.min(level, maxLevel));
			}
			if (builder != null) {
				EnchantmentHelper.set(stack, builder.build());
			}
		}
	}

	/**
	 * When interacting with a villager, enable additional functionality depending on the held item.
	 * <ul>
	 *     <li>Emerald block: Reset the trades of a villager with no experience</li>
	 *     <li>Poisonous potato: Reset the trades, level and experience of a villager with the weakness effect</li>
	 *     <li>Enchanted book: Add the enchantment as a trade to a master-level librarian</li>
	 * </ul>
	 */
	@Inject(method = "interactMob", at = @At("HEAD"))
	private void interactMobInject(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		if (getWorld().isClient) {
			return;
		}
		ItemStack stack = player.getStackInHand(hand);
		if (stack.isEmpty()) {
			return;
		}
		if (stack.isOf(Items.EMERALD_BLOCK)) {
			resetOffers();
		} else if (stack.isOf(Items.POISONOUS_POTATO)) {
			resetProfession(player, stack);
		} else if (stack.isOf(Items.ENCHANTED_BOOK)) {
			learnEnchantment(player, stack);
		}
	}

	/**
	 * Resets the trade offers of a villager with no experience.
	 */
	@Unique
	private void resetOffers() {
		if (!ProfessionalVillagers.CONFIG.quickReroll() || this.getExperience() > 0) {
			return;
		}
		this.setOffers(null);
	}

	/**
	 * Adds a random enchantment from the specified enchanted book as a trade to a master-level librarian.
	 *
	 * @param player The interacting player
	 * @param book   The enchanted book
	 */
	@Unique
	private void learnEnchantment(PlayerEntity player, ItemStack book) {
		if (!ProfessionalVillagers.CONFIG.learnEnchantment()) {
			return;
		}
		VillagerData data = this.getVillagerData();
		if (data.getProfession() != VillagerProfession.LIBRARIAN || data.getLevel() < VillagerData.MAX_LEVEL) {
			return;
		}
		TradeOfferList offers = this.getOffers();
		// A master-level librarian will have 9 trades by default
		if (offers.size() > 9) {
			return;
		}
		ItemEnchantmentsComponent component = EnchantmentHelper.getEnchantments(book);
		int size = component.getSize();
		if (size == 0) {
			return;
		}
		ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
		Iterator<RegistryEntry<Enchantment>> iterator = component.getEnchantments().iterator();
		if (size > 1) {
			int offset = this.getWorld().random.nextInt(size);
			while (offset-- > 0) {
				iterator.next();
			}
		}
		RegistryEntry<Enchantment> enchantment = iterator.next();
		newBook.addEnchantment(enchantment, component.getLevel(enchantment));
		offers.add(new TradeOffer(new TradedItem(Items.EMERALD, 64), Optional.of(new TradedItem(Items.BOOK)), newBook, 12, 15, 0.2F));
		book.decrementUnlessCreative(1, player);
	}

	/**
	 * Resets the level, experience and offers of a villager with the weakness effect.
	 *
	 * @param player The player
	 * @param stack  The relevant item stack
	 */
	@Unique
	private void resetProfession(PlayerEntity player, ItemStack stack) {
		if (!ProfessionalVillagers.CONFIG.resetProfession() || this.getExperience() == 0 || !this.hasStatusEffect(StatusEffects.WEAKNESS)) {
			return;
		}
		this.setVillagerData(this.getVillagerData().withLevel(VillagerData.MIN_LEVEL));
		this.setExperience(0);
		this.setOffers(null);
		stack.decrementUnlessCreative(1, player);
	}

}
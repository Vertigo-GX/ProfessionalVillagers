package vertigo.professionalvillagers.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
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

@Mixin(Villager.class)
public abstract class VillagerEntityMixin extends AbstractVillager {

	public VillagerEntityMixin(EntityType<? extends AbstractVillager> type, Level world) {
		super(type, world);
	}

	@Shadow
	public abstract VillagerData getVillagerData();

	@Shadow
	public abstract void setVillagerData(VillagerData villagerData);

	@Shadow
	public abstract int getVillagerXp();

	@Shadow
	public abstract void setVillagerXp(int experience);

	@Shadow
	public abstract void setOffers(MerchantOffers offers);

	/**
	 * When adding trading recipes to a novice-level librarian, set the level of all enchantments to 1.
	 */
	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void fillRecipesInject(CallbackInfo info) {
		if(!ProfessionalVillagers.CONFIG.levelEnchantments) {
			return;
		}
		VillagerData data = this.getVillagerData();
		if(!data.profession().is(VillagerProfession.LIBRARIAN) || data.level() > VillagerData.MIN_VILLAGER_LEVEL) {
			return;
		}
		setEnchantmentLevels(1);
	}

	/**
	 * When a librarian levels up, set the level of all enchantments to the level of the librarian.
	 */
	@Inject(method = "increaseMerchantCareer", at = @At("TAIL"))
	private void levelUpInject(CallbackInfo info) {
		if(!ProfessionalVillagers.CONFIG.levelEnchantments) {
			return;
		}
		VillagerData data = this.getVillagerData();
		if(!data.profession().is(VillagerProfession.LIBRARIAN)) {
			return;
		}
		setEnchantmentLevels(data.level());
	}

	/**
	 * Sets the level of all enchantments to the specified level.
	 *
	 * @param level The enchantment level
	 */
	@Unique
	private void setEnchantmentLevels(int level) {
		for(MerchantOffer o : this.getOffers()) {
			ItemStack stack = o.getResult();
			if(!stack.is(Items.ENCHANTED_BOOK)) {
				continue;
			}
			ItemEnchantments component = EnchantmentHelper.getEnchantmentsForCrafting(stack);
			ItemEnchantments.Mutable builder = null;
			// This supports enchanted books with multiple enchantments
			for(Holder<Enchantment> e : component.keySet()) {
				int enchantmentLevel = component.getLevel(e);
				if(enchantmentLevel == level) {
					continue;
				}
				int maxLevel = e.value().getMaxLevel();
				if(enchantmentLevel < level && enchantmentLevel >= maxLevel) {
					continue;
				}
				if(builder == null) {
					builder = new ItemEnchantments.Mutable(component);
				}
				builder.set(e, Math.min(level, maxLevel));
			}
			if(builder != null) {
				EnchantmentHelper.setEnchantments(stack, builder.toImmutable());
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
	@Inject(method = "mobInteract", at = @At("HEAD"))
	private void interactMobInject(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
		if(this.level().isClientSide()) {
			return;
		}
		ItemStack stack = player.getItemInHand(hand);
		if(stack.isEmpty()) {
			return;
		}
		if(stack.is(Items.EMERALD_BLOCK)) {
			resetOffers();
		} else if(stack.is(Items.POISONOUS_POTATO)) {
			resetProfession(player, stack);
		} else if(stack.is(Items.ENCHANTED_BOOK)) {
			learnEnchantment(player, stack);
		}
	}

	/**
	 * Resets the trade offers of a villager with no experience.
	 */
	@Unique
	private void resetOffers() {
		if(!ProfessionalVillagers.CONFIG.quickReroll || this.getVillagerXp() > 0) {
			return;
		}
		this.setOffers(null);
	}

	/**
	 * Adds a random enchantment from the specified enchanted book as a trade to a master-level librarian.
	 *
	 * @param player The interacting player
	 * @param book The enchanted book
	 */
	@Unique
	private void learnEnchantment(Player player, ItemStack book) {
		if(!ProfessionalVillagers.CONFIG.learnEnchantment) {
			return;
		}
		VillagerData data = this.getVillagerData();
		if(!data.profession().is(VillagerProfession.LIBRARIAN) || data.level() < VillagerData.MAX_VILLAGER_LEVEL) {
			return;
		}
		MerchantOffers offers = this.getOffers();
		// A master-level librarian will have 9 trades by default
		if(offers.size() > 9) {
			return;
		}
		ItemEnchantments component = EnchantmentHelper.getEnchantmentsForCrafting(book);
		int size = component.size();
		if(size == 0) {
			return;
		}
		ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
		Iterator<Holder<Enchantment>> iterator = component.keySet().iterator();
		if(size > 1) {
			int offset = this.level().random.nextInt(size);
			while(offset-- > 0) {
				iterator.next();
			}
		}
		Holder<Enchantment> enchantment = iterator.next();
		newBook.enchant(enchantment, component.getLevel(enchantment));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 64), Optional.of(new ItemCost(Items.BOOK)), newBook, 12, 15, 0.2F));
		book.consume(1, player);
	}

	/**
	 * Resets the level, experience and offers of a villager with the weakness effect.
	 *
	 * @param player The player
	 * @param stack The relevant item stack
	 */
	@Unique
	private void resetProfession(Player player, ItemStack stack) {
		if(!ProfessionalVillagers.CONFIG.resetProfession || this.getVillagerXp() == 0 || !this.hasEffect(MobEffects.WEAKNESS)) {
			return;
		}
		this.setVillagerData(this.getVillagerData().withLevel(VillagerData.MIN_VILLAGER_LEVEL));
		this.setVillagerXp(0);
		this.setOffers(null);
		stack.consume(1, player);
	}

}
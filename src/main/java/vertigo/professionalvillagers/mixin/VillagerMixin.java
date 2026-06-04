package vertigo.professionalvillagers.mixin;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.trading.*;
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
public abstract class VillagerMixin extends AbstractVillager {

	public VillagerMixin(EntityType<? extends AbstractVillager> type, Level level) {
		super(type, level);
	}

	@Shadow
	public abstract VillagerData getVillagerData();

	@Shadow
	public abstract void setVillagerData(VillagerData villagerData);

	@Shadow
	public abstract void setVillagerXp(int experience);

	@Shadow
	public abstract void setOffers(MerchantOffers offers);

	@Inject(method = "updateTrades", at = @At("TAIL"))
	private void updateTradesInject(ServerLevel level, CallbackInfo info) {
		if(ProfessionalVillagers.CONFIG.modifyTrades) {
			VillagerData data = this.getVillagerData();
			int villagerLevel = data.level();
			Holder<VillagerProfession> profession = data.profession();
			if(villagerLevel == 2 && profession.is(VillagerProfession.FARMER)) {
				adjustFarmerTrades();
			} else if(villagerLevel == 2 && profession.is(VillagerProfession.FISHERMAN)) {
				adjustFishermanTrades();
			} else if(villagerLevel == 3 && profession.is(VillagerProfession.TOOLSMITH)) {
				adjustToolsmithJourneymanTrades();
			} else if(villagerLevel == 4 && profession.is(VillagerProfession.TOOLSMITH)) {
				adjustToolsmithExpertTrades();
			}
		}
		if(ProfessionalVillagers.CONFIG.levelEnchantments) {
			VillagerData data = this.getVillagerData();
			if(!data.profession().is(VillagerProfession.LIBRARIAN)) {
				return;
			}
			setEnchantmentLevels(data.level());
		}
	}

	@Unique
	private void adjustFarmerTrades() {
		MerchantOffers offers = this.getOffers();
		if(offers.size() < 4) {
			return;
		}
		if(offers.get(2).getCostA().is(Items.PUMPKIN) || offers.get(3).getCostA().is(Items.PUMPKIN)) {
			return;
		}
		offers.set(3, new MerchantOffer(new ItemCost(Items.PUMPKIN, 6), new ItemStack(Items.EMERALD), 12, 10, 0.05f));
	}

	@Unique
	private void adjustFishermanTrades() {
		MerchantOffers offers = this.getOffers();
		if(offers.size() < 4) {
			return;
		}
		if(offers.get(2).getCostA().is(Items.COD) || offers.get(3).getCostA().is(Items.COD)) {
			return;
		}
		offers.set(3, new MerchantOffer(new ItemCost(Items.COD, 15), new ItemStack(Items.EMERALD), 16, 10, 0.05f));
	}

	@Unique
	private void adjustToolsmithJourneymanTrades() {
		MerchantOffers offers = this.getOffers();
		if(offers.size() < 6) {
			return;
		}
		if(offers.get(4).getResult().is(Items.DIAMOND_HOE) || offers.get(5).getResult().is(Items.DIAMOND_HOE)) {
			return;
		}
		offers.set(5, new MerchantOffer(new ItemCost(Items.EMERALD, 4), new ItemStack(Items.DIAMOND_HOE), 3, 10, 0.2f));
	}

	@Unique
	private void adjustToolsmithExpertTrades() {
		MerchantOffers offers = this.getOffers();
		if(offers.size() < 8) {
			return;
		}
		ItemStack first = offers.get(6).getResult();
		ItemStack second = offers.get(7).getResult();
		if(first.is(Items.DIAMOND_SHOVEL) || second.is(Items.DIAMOND_SHOVEL)) {
			return;
		}
		ItemStack stack = new ItemStack(Items.DIAMOND_SHOVEL);
		if(first.is(Items.DIAMOND_AXE)) {
			EnchantmentHelper.setEnchantments(stack, first.getEnchantments());
		} else if(second.is(Items.DIAMOND_AXE)) {
			EnchantmentHelper.setEnchantments(stack, second.getEnchantments());
		}
		// Cost should be 5 + base enchantment level; 18 is an average
		offers.set(7, new MerchantOffer(new ItemCost(Items.EMERALD, 18), stack, 3, 15, 0.2f));
	}

	@Unique
	private void setEnchantmentLevels(int level) {
		for(MerchantOffer offer : this.getOffers()) {
			ItemStack stack = offer.getResult();
			if(!stack.is(Items.ENCHANTED_BOOK)) {
				continue;
			}
			ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
			ItemEnchantments.Mutable mutable = null;
			for(Holder<Enchantment> enchantment : enchantments.keySet()) {
				int enchantmentLevel = enchantments.getLevel(enchantment);
				if(enchantmentLevel >= level) {
					continue;
				}
				int maxLevel = enchantment.value().getMaxLevel();
				if(enchantmentLevel >= maxLevel) {
					continue;
				}
				if(mutable == null) {
					mutable = new ItemEnchantments.Mutable(enchantments);
				}
				mutable.set(enchantment, Math.min(level, maxLevel));
			}
			if(mutable != null) {
				EnchantmentHelper.setEnchantments(stack, mutable.toImmutable());
			}
		}
	}

	@Inject(method = "mobInteract", at = @At("HEAD"))
	private void mobInteractInject(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
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

	@Unique
	private void resetOffers() {
		if(!ProfessionalVillagers.CONFIG.quickReroll || this.getVillagerXp() > 0) {
			return;
		}
		this.setOffers(null);
	}

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

	@Unique
	private void learnEnchantment(Player player, ItemStack book) {
		if(!ProfessionalVillagers.CONFIG.learnEnchantment) {
			return;
		}
		VillagerData data = this.getVillagerData();
		if(data.level() < VillagerData.MAX_VILLAGER_LEVEL || !data.profession().is(VillagerProfession.LIBRARIAN)) {
			return;
		}
		MerchantOffers offers = this.getOffers();
		// A master-level librarian has 10 trade offers
		if(offers.size() > 10) {
			return;
		}
		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(book);
		int size = enchantments.size();
		if(size == 0) {
			return;
		}
		ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
		Iterator<Holder<Enchantment>> iterator = enchantments.keySet().iterator();
		if(size > 1) {
			int offset = this.level().getRandom().nextInt(size);
			while(offset-- > 0) {
				iterator.next();
			}
		}
		Holder<Enchantment> enchantment = iterator.next();
		stack.enchant(enchantment, enchantments.getLevel(enchantment));
		offers.add(new MerchantOffer(new ItemCost(Items.EMERALD, 64), Optional.of(new ItemCost(Items.BOOK)), stack, 12, 15, 0.2F));
		book.consume(1, player);
	}

}
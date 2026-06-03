package vertigo.professionalvillagers.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vertigo.professionalvillagers.ProfessionalVillagers;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderEntityMixin extends AbstractVillager {

	@Unique
	private static final int DESPAWN_DELAY = 100; // 100 ticks = 5 seconds

	@Shadow
	public abstract int getDespawnDelay();

	@Shadow
	public abstract void setDespawnDelay(int despawnDelay);

	public WanderingTraderEntityMixin(EntityType<? extends AbstractVillager> type, Level world) {
		super(type, world);
	}

	/**
	 * When interacting with a wandering trader while holding an emerald block, lower its despawn delay to {@value #DESPAWN_DELAY} ticks. Wandering
	 * traders from spawn eggs have a despawn delay of 0, and are therefore unaffected.
	 */
	@Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
	private void interactMobInject(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
		if(this.level().isClientSide() || !ProfessionalVillagers.CONFIG.dismissTrader || this.getDespawnDelay() < DESPAWN_DELAY) {
			return;
		}
		if(!player.getItemInHand(hand).is(Items.EMERALD_BLOCK)) {
			return;
		}
		this.setDespawnDelay(DESPAWN_DELAY);
		info.setReturnValue(InteractionResult.SUCCESS);
	}

}
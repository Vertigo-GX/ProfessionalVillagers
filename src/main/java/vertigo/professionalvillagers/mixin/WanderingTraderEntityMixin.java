package vertigo.professionalvillagers.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vertigo.professionalvillagers.ProfessionalVillagers;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

	@Unique
	private static final int DESPAWN_DELAY = 100; // 100 ticks = 5 seconds

	@Shadow
	public abstract int getDespawnDelay();

	@Shadow
	public abstract void setDespawnDelay(int despawnDelay);

	public WanderingTraderEntityMixin(EntityType<? extends MerchantEntity> type, World world) {
		super(type, world);
	}

	/**
	 * When interacting with a wandering trader while holding an emerald block, lower its despawn delay to {@value #DESPAWN_DELAY} ticks. Wandering
	 * traders from spawn eggs have a despawn delay of 0, and are therefore unaffected.
	 */
	@Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
	private void interactMobInject(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		if(this.getEntityWorld().isClient() || !ProfessionalVillagers.CONFIG.dismissTrader || this.getDespawnDelay() < DESPAWN_DELAY) {
			return;
		}
		if(!player.getStackInHand(hand).isOf(Items.EMERALD_BLOCK)) {
			return;
		}
		this.setDespawnDelay(DESPAWN_DELAY);
		info.setReturnValue(ActionResult.SUCCESS);
	}

}
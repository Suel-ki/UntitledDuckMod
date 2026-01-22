package net.untitledduckmod.common.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.untitledduckmod.common.entity.WaterfowlEggEntity;
import net.untitledduckmod.common.entity.WaterfowlEntity;

import java.util.function.Supplier;

public class WaterfowlEggItem extends Item implements ProjectileItem {
    private final Supplier<EntityType<? extends ThrowableItemProjectile>> thrownEntityType;
    private final Supplier<EntityType<? extends WaterfowlEntity>> mobEntityType;

    public WaterfowlEggItem(Item.Properties properties, Supplier<EntityType<? extends ThrowableItemProjectile>> entityType, Supplier<EntityType<? extends WaterfowlEntity>> mobEntityType) {
        super(properties);
        this.thrownEntityType = entityType;
        this.mobEntityType = mobEntityType;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
        if (world instanceof ServerLevel serverWorld) {
            ThrowableItemProjectile.ProjectileFactory<WaterfowlEggEntity> projectileCreator = (world1, shooter, stack) -> new WaterfowlEggEntity(thrownEntityType.get(), world1, shooter, stack, mobEntityType.get());
            WaterfowlEggEntity.spawnProjectileFromRotation(projectileCreator, serverWorld, itemStack, user, 0.0F, 1.5F, 1.0F);
        }

        user.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, user);

        return InteractionResult.SUCCESS;
    }

    @Override
    public Projectile asProjectile(Level world, Position pos, ItemStack stack, Direction direction) {
        WaterfowlEggEntity eggEntity = new WaterfowlEggEntity(thrownEntityType.get(), world, stack, pos.x(), pos.y(), pos.z(), mobEntityType.get());
        eggEntity.setItem(stack);
        return eggEntity;
    }
}
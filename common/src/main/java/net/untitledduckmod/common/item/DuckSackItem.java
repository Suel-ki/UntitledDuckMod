package net.untitledduckmod.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.helper.NbtHelper;
import net.untitledduckmod.common.init.ModEntityTypes;
import net.untitledduckmod.common.init.ModItems;
import net.untitledduckmod.common.init.ModSoundEvents;

import java.util.UUID;

public class DuckSackItem extends Item {
    public DuckSackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        if (!world.isClientSide()) {
            BlockPos pos = context.getClickedPos();
            BlockState blockState = world.getBlockState(pos);
            Player user = context.getPlayer();
            Direction blockSide = context.getClickedFace();
            InteractionHand hand = context.getHand();
            ItemStack stack = context.getItemInHand();

            if (user != null) {
                user.swing(hand);

                BlockPos placePos;
                if (blockState.getCollisionShape(world, pos).isEmpty()) {
                    placePos = pos;
                } else {
                    placePos = pos.relative(blockSide);
                }

                if (placeCreature((ServerLevel) world, placePos, stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY))) {
                    world.gameEvent(user, GameEvent.ENTITY_PLACE, pos);

                    ItemStack emptySack = new ItemStack(ModItems.EMPTY_DUCK_SACK.get());
                    user.awardStat(Stats.ITEM_USED.get(this));
                    stack.consume(1, user);
                    if (stack.isEmpty()) {
                        user.setItemInHand(hand, emptySack);
                    } else if (!user.addItem(emptySack)) {
                        user.drop(emptySack, false);
                    }

                    world.playSound(user, pos, ModSoundEvents.DUCK_SACK_USE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    return InteractionResult.CONSUME;
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        BlockHitResult blockHitResult = getPlayerPOVHitResult(world, user, ClipContext.Fluid.SOURCE_ONLY);
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        } else if (!(world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            BlockPos pos = blockHitResult.getBlockPos();
            if (!(world.getBlockState(pos).getBlock() instanceof LiquidBlock)) {
                return InteractionResult.PASS;
            } else if (world.mayInteract(user, pos) &&
                    user.mayUseItemAt(pos, blockHitResult.getDirection(), stack)) {
                if (placeCreature((ServerLevel) world, pos, stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY))) {
                    user.awardStat(Stats.ITEM_USED.get(this));
                    world.gameEvent(user, GameEvent.ENTITY_PLACE, pos);

                    ItemStack emptySack = new ItemStack(ModItems.EMPTY_DUCK_SACK.get());
                    stack.consume(1, user);

                    world.playSound(user, pos, ModSoundEvents.DUCK_SACK_USE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                    if (stack.isEmpty()) {
                       return InteractionResult.CONSUME.heldItemTransformedTo(emptySack);
                    } else if (!user.addItem(emptySack)) {
                        user.drop(emptySack, false);
                    }

                    return InteractionResult.CONSUME;
                } else {
                    return InteractionResult.PASS;
                }
            } else {
                return InteractionResult.FAIL;
            }
        }
    }

    private boolean placeCreature(ServerLevel world, BlockPos pos, CustomData itemData) {
        CompoundTag entityData = itemData.copyTag();
        // Remove uuid when there already is a creature with same uuid.
        // This makes it possible to use the duck sack in creative, cloning every tag except the uuid.
        if (NbtHelper.containsUuid(entityData, Entity.TAG_UUID)) {
            UUID uuid = NbtHelper.getUuid(entityData, Entity.TAG_UUID);
            if (world.getEntity(uuid) != null) {
                entityData.remove(Entity.TAG_UUID);
            }
        }

        var entityType = EntityType.getKey(ModEntityTypes.getDuck()).toString();

        // This makes it possible to use duck sack with an empty nbt
        if (!entityData.contains(Entity.TAG_ID)) {
            entityData.putString(Entity.TAG_ID, entityType);
        }

        var errorReporter = new ProblemReporter.ScopedCollector(() -> entityType, DuckEntity.LOGGER);

        var nbtReadView = TagValueInput.create(errorReporter, world.registryAccess(), entityData);
        var optional = EntityType.create(nbtReadView, world, new EntitySpawnRequest(EntitySpawnReason.BUCKET, false));

        if (optional.isPresent()) {
            var newDuck = optional.get();
            if (newDuck instanceof DuckEntity duck) {
                duck.readAdditionalSaveData(nbtReadView);
                duck.setFromSack(true);
                duck.snapTo((double) pos.getX() + 0.5D, (double) pos.getY() + 0.4D, (double) pos.getZ() + 0.5D, Mth.wrapDegrees(world.getRandom().nextFloat() * 360.0F), 0.0F);
                duck.playAmbientSound();
                world.addFreshEntityWithPassengers(duck);
                return true;
            }
        }

       return false;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (NbtHelper.contains(stack, DataComponents.BUCKET_ENTITY_DATA)) {
            CompoundTag itemData = NbtHelper.get(stack, DataComponents.BUCKET_ENTITY_DATA);
            if (itemData != null) {
                if (itemData.contains("CustomName")) {
                    Component duckName = Component.literal(itemData.getString("CustomName").orElse("duck"));
                    return Component.translatable("item.untitledduckmod.duck_sack.named", duckName);
                }
            }
        }
        return super.getName(stack);
    }
}


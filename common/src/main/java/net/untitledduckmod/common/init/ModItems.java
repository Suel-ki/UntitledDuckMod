package net.untitledduckmod.common.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.item.DuckSackItem;
import net.untitledduckmod.common.item.WaterfowlEggItem;

import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems {
    // Duck
    public static Supplier<Item> DUCK_SPAWN_EGG;
    public static Supplier<Item> DUCK_EGG;
    public static Supplier<Item> RAW_DUCK;
    public static Supplier<Item> COOKED_DUCK;
    public static Supplier<Item> DUCK_FEATHER;
    public static Supplier<Item> DUCK_SACK;
    public static Supplier<Item> EMPTY_DUCK_SACK;
    // Goose
    public static Supplier<Item> GOOSE_SPAWN_EGG;
    public static Supplier<Item> GOOSE_EGG;
    public static Supplier<Item> RAW_GOOSE;
    public static Supplier<Item> COOKED_GOOSE;
    public static Supplier<Item> GOOSE_FOOT;

    public ModItems() {
        // Food
        RAW_DUCK = registerItem("raw_duck", Item::new, () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.4F).build()));
        COOKED_DUCK = registerItem("cooked_duck", Item::new, () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8F).build()));
        RAW_GOOSE = registerItem("raw_goose", Item::new, () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(0.6F).build()));
        COOKED_GOOSE = registerItem("cooked_goose", Item::new, () -> new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.0F).build()));
        // Item
        DUCK_EGG = registerItem("duck_egg", (Properties) -> new WaterfowlEggItem(Properties, ModEntityTypes::getDuckEgg, ModEntityTypes::getDuck), () -> new Item.Properties().stacksTo(16));
        DUCK_FEATHER = registerItem("duck_feather", Item::new, Item.Properties::new);
        DUCK_SACK = registerItem("duck_sack", DuckSackItem::new, Item.Properties::new);
        EMPTY_DUCK_SACK = registerItem("empty_duck_sack", Item::new, Item.Properties::new);
        GOOSE_EGG = registerItem("goose_egg", (Properties) -> new WaterfowlEggItem(Properties, ModEntityTypes::getGooseEgg, ModEntityTypes::getGoose), () -> new Item.Properties().stacksTo(16));
        GOOSE_FOOT = registerItem("goose_foot", Item::new, Item.Properties::new);
        // Spawn Egg
        DUCK_SPAWN_EGG = registerSpawnEggItem("duck_spawn_egg", ModEntityTypes.DUCK);
        GOOSE_SPAWN_EGG = registerSpawnEggItem("goose_spawn_egg", ModEntityTypes.GOOSE);
    }

    public Supplier<Item> registerItem(String name, Function<Item.Properties, Item> factory, Supplier<Item.Properties> settings)
    {
        var item = Items.registerItem(ResourceKey.create(Registries.ITEM, DuckMod.id(name)), factory, settings.get());
        return () -> item;
    }

    public Supplier<Item> registerSpawnEggItem(
            String name, Supplier<? extends EntityType<? extends Mob>> type
    ) {
        return registerItem(name, SpawnEggItem::new, () -> new Item.Properties().spawnEgg(type.get()));
    }

}

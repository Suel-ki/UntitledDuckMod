package net.untitledduckmod.common.init.neoforge;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.ModItems;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoItemsImpl extends ModItems {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(DuckMod.MOD_ID);

    public NeoItemsImpl(IEventBus eventBus)
    {
        super();
        ITEMS.register(eventBus);
    }

    @Override
    public Supplier<Item> registerItem(String name, Function<Item.Properties, Item> factory, Supplier<Item.Properties> settings)
    {
        return ITEMS.registerItem(name, factory, settings);
    }
}

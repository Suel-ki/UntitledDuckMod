package net.untitledduckmod.compat.neoforge.incubation;

import com.teamabnormals.blueprint.core.util.DataUtil;
import com.teamabnormals.incubation.common.block.BirdNestBlock;
import com.teamabnormals.incubation.common.block.EmptyNestBlock;
import com.teamabnormals.incubation.core.registry.IncubationBlocks;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.init.ModItems;

public class IncubationCompat {
    public static final String INCUBATION = "incubation";

    public IncubationCompat(IEventBus bus) {
        Holder.BLOCKS.register(bus);
        bus.register(IncubationCompat.Holder.class);
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(INCUBATION);
    }

    static class Holder {
        private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, DuckMod.MOD_ID);

        private static final DeferredHolder<Block, BirdNestBlock> TWIG_DUCK_NEST = BLOCKS.register("twig_duck_nest",
                () -> new BirdNestBlock(ModItems.DUCK_EGG, (EmptyNestBlock) IncubationBlocks.TWIG_NEST.get(), IncubationBlocks.IncubationProperties.HAY_NEST));
        private static final DeferredHolder<Block, BirdNestBlock> HAY_DUCK_NEST = BLOCKS.register("hay_duck_nest",
                () -> new BirdNestBlock(ModItems.DUCK_EGG, (EmptyNestBlock) IncubationBlocks.HAY_NEST.get(), IncubationBlocks.IncubationProperties.HAY_NEST));
        private static final DeferredHolder<Block, BirdNestBlock> TWIG_GOOSE_NEST = BLOCKS.register("twig_goose_nest",
                () -> new BirdNestBlock(ModItems.GOOSE_EGG, (EmptyNestBlock) IncubationBlocks.TWIG_NEST.get(), IncubationBlocks.IncubationProperties.HAY_NEST));
        private static final DeferredHolder<Block, BirdNestBlock> HAY_GOOSE_NEST = BLOCKS.register("hay_goose_nest",
                () -> new BirdNestBlock(ModItems.GOOSE_EGG, (EmptyNestBlock) IncubationBlocks.HAY_NEST.get(), IncubationBlocks.IncubationProperties.HAY_NEST));

        private static void registerFlammables() {
            DataUtil.registerFlammable(TWIG_DUCK_NEST.get(), 60, 20);
            DataUtil.registerFlammable(TWIG_GOOSE_NEST.get(), 60, 20);
            DataUtil.registerFlammable(HAY_DUCK_NEST.get(), 60, 20);
            DataUtil.registerFlammable(HAY_GOOSE_NEST.get(), 60, 20);
        }

        @SubscribeEvent
        public static void commonSetup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                EmptyNestBlock twigNest = (EmptyNestBlock) IncubationBlocks.TWIG_NEST.get();
                EmptyNestBlock hayNest = (EmptyNestBlock) IncubationBlocks.HAY_NEST.get();
                twigNest.addNest(ModItems.DUCK_EGG, TWIG_DUCK_NEST.get());
                twigNest.addNest(ModItems.GOOSE_EGG, TWIG_GOOSE_NEST.get());
                hayNest.addNest(ModItems.DUCK_EGG, HAY_DUCK_NEST.get());
                hayNest.addNest(ModItems.GOOSE_EGG, HAY_GOOSE_NEST.get());
                registerFlammables();
            });
        }
    }
}

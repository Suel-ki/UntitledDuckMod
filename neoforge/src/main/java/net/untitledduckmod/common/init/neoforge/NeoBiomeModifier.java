package net.untitledduckmod.common.init.neoforge;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.platform.ForgeRegistryHelper;

import java.util.function.Supplier;

public class NeoBiomeModifier implements BiomeModifier {
    private static final String BIOME_MODIFIER_NAME = "untitled_spawns";
    private static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<NeoBiomeModifier>> SERIALIZER;
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DuckMod.MOD_ID);

    public NeoBiomeModifier() {}
    public NeoBiomeModifier(IEventBus bus) {
        registerBiomeModifier(BIOME_MODIFIER_NAME, NeoBiomeModifier::makeCodec);
        BIOME_MODIFIERS.register(bus);
    }

    static {
        SERIALIZER = DeferredHolder.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DuckMod.id(BIOME_MODIFIER_NAME));
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            ForgeRegistryHelper.addBiomeSpawns(biome, builder);
        }
    }

    public MapCodec<? extends BiomeModifier> codec()
    {
        return SERIALIZER.get();
    }

    public static MapCodec<NeoBiomeModifier> makeCodec() {
        return MapCodec.unit(NeoBiomeModifier::new);
    }

    public static <T extends MapCodec<? extends BiomeModifier>> void registerBiomeModifier(String name, Supplier<T> biomeModifier) {
        BIOME_MODIFIERS.register(name, biomeModifier);
    }
}

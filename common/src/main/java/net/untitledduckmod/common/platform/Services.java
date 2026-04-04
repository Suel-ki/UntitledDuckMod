package net.untitledduckmod.common.platform;

import net.untitledduckmod.common.platform.service.IRegistryHelper;
import net.untitledduckmod.common.platform.service.IUntitledConfig;

import java.util.ServiceLoader;

public class Services {

    public static final IRegistryHelper PLATFORM = load(IRegistryHelper.class);

    public static final IUntitledConfig CONFIG = load(IUntitledConfig.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        return loadedService;
    }
}
package net.untitledduckmod.common.platform.service;

import java.util.List;

public interface IUntitledConfig {
    
    int duckWeight();
    
    int duckMinGroupSize();
    
    int duckMaxGroupSize();

    double duckFishingChange();
    
    boolean duckTamedNotFollow();

    boolean duckBabyRandomSize();


    int gooseWeight();

    int gooseMinGroupSize();

    int gooseMaxGroupSize();

    boolean gooseTamedNotFollow();

    boolean gooseBabyRandomSize();

    float foodHealingValue();

    boolean enableForceEat();

    int forceEatRandomMinTick();

    int forceEatRandomMaxTick();

    List<? extends String> intimidationBlacklist();

    void setup();
}

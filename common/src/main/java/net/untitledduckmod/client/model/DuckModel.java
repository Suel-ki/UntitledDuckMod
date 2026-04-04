package net.untitledduckmod.client.model;

import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.entity.DuckEntity;
import net.untitledduckmod.common.entity.WaterfowlEntity;
public class DuckModel extends WaterfowlModel<DuckEntity> {

    public DuckModel() {
        super(DuckMod.id("duck"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState instanceof LivingEntityRenderState livingEntityRenderState) {
            if (livingEntityRenderState.isBaby) {
                return ModelIdentifiers.DUCKLING_TEXTURE;
            }
        }
        var variant = renderState.hasGeckolibData(WaterfowlEntity.VARIANT_TICKET) ? renderState.getGeckolibData(WaterfowlEntity.VARIANT_TICKET) : 0;

        return switch (variant) {
            case 1 -> ModelIdentifiers.FEMALE_TEXTURE;
            case 2 -> ModelIdentifiers.CAMPBELL_TEXTURE;
            case 3 -> ModelIdentifiers.PEKIN_TEXTURE;
            default -> ModelIdentifiers.NORMAL_TEXTURE;
        };
    }
}

package net.untitledduckmod.client.model;

import net.minecraft.resources.Identifier;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public abstract class WaterfowlModel<T extends WaterfowlEntity>  extends DefaultedEntityGeoModel<T> {
    public WaterfowlModel(Identifier assetSubpath) { super(assetSubpath); }

    @Override
    public void addAdditionalStateData(WaterfowlEntity animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(WaterfowlEntity.LOOKING_AROUND_TICKET, animatable.lookingAround());
    }
}

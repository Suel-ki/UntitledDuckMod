package net.untitledduckmod.client.model;

import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import org.jspecify.annotations.Nullable;

public abstract class WaterfowlModel<T extends WaterfowlEntity>  extends DefaultedEntityGeoModel<T> {
    public WaterfowlModel(Identifier assetSubpath) { super(assetSubpath); }

    @Override
    public void addAdditionalStateData(WaterfowlEntity animatable, @Nullable Object relatedObject, GeoRenderState renderState) {
        renderState.addGeckolibData(WaterfowlEntity.BABY_SCALE_TICKET, animatable.getBabyScale());
        renderState.addGeckolibData(WaterfowlEntity.VARIANT_TICKET, animatable.getVariant());
        renderState.addGeckolibData(WaterfowlEntity.LOOKING_AROUND_TICKET, animatable.lookingAround());
    }
}

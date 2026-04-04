package net.untitledduckmod.client.renderer.entity;

import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.untitledduckmod.client.model.DuckModel;
import net.untitledduckmod.common.entity.DuckEntity;

public class DuckRenderer<R extends LivingEntityRenderState & GeoRenderState> extends WaterfowlRenderer<DuckEntity, R> {
    public DuckRenderer(EntityRendererProvider.Context context) { super(new DuckModel(), context); }
}

package net.untitledduckmod.client.renderer.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.untitledduckmod.client.model.GooseModel;
import net.untitledduckmod.common.entity.GooseEntity;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GooseRenderer<R extends LivingEntityRenderState & GeoRenderState> extends WaterfowlRenderer<GooseEntity, R> {
    public GooseRenderer(EntityRendererProvider.Context context) {
        super(new GooseModel(), context);
    }
}

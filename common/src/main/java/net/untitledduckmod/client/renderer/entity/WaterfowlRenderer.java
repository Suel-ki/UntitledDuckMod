package net.untitledduckmod.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.untitledduckmod.client.model.WaterfowlModel;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.init.ModEntityTypes;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.BoneSnapshots;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.RenderPassInfo;
import software.bernie.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;

public class WaterfowlRenderer<T extends WaterfowlEntity, R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<T, R> {
    private static final float ADULT_SHADOW_RADIUS = 0.3f;

    public WaterfowlRenderer(WaterfowlModel<T> model, EntityRendererProvider.Context context) {
        super(context, model);
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        withRenderLayer(new ItemInHandGeoLayer<>(this, "beak", "beak") {
            @Override
            protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStack stack, ItemDisplayContext displayContext, R renderState, SubmitNodeCollector renderTasks,
                                                 CameraRenderState cameraState, int packedLight, int packedOverlay, int renderColor) {
                if (displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND) {
                    poseStack.pushPose();
                    if (renderState.entityType == ModEntityTypes.getDuck()) {
                        poseStack.mulPose(Axis.XN.rotationDegrees(90f));
                        poseStack.translate(-0.1f, 0.0f, 0.0f);
                        poseStack.mulPose(Axis.ZN.rotationDegrees(45f));
                    } else if (renderState.entityType == ModEntityTypes.getGoose()) {
                        poseStack.mulPose(Axis.XN.rotationDegrees(90f));
                        poseStack.translate(0.16f, 0.03f, 0.00f);
                        poseStack.mulPose(Axis.ZN.rotationDegrees(45f));
                    }
                    poseStack.scale(0.7f, 0.7f, 0.7f);
                    final ItemStackRenderState stackRenderState = new ItemStackRenderState();
                    final Minecraft mc = Minecraft.getInstance();

                    mc.getItemModelResolver().updateForTopItem(stackRenderState, stack, ItemDisplayContext.GROUND, mc.level, null, (int)(long)renderState.getOrDefaultGeckolibData(DataTickets.ANIMATABLE_INSTANCE_ID, 0L) + displayContext.ordinal());
                    stackRenderState.submit(poseStack, renderTasks, packedLight, OverlayTexture.NO_OVERLAY, renderState.outlineColor);
                    poseStack.popPose();
                }
            }
        });
    }

    @Override
    protected float getShadowRadius(R state) {
        return super.getShadowRadius(state) * state.ageScale;
    }

    @Override
    public void addRenderData(T animatable, Void relatedObject, R renderState, float partialTick) {
        // set the variant in the render state
        renderState.addGeckolibData(WaterfowlEntity.VARIANT_TICKET, animatable.getVariant());
        renderState.addGeckolibData(WaterfowlEntity.BABY_SCALE_TICKET, animatable.getBabyScale());
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderState, float widthScale, float heightScale) {
        // set the entity scale for rendering (this replaces the need to change the scale in preRender)
        float modelScale = renderState.renderState().ageScale;

        this.withScale(modelScale);

        super.scaleModelForRender(renderState, widthScale, heightScale);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> renderPassInfo, BoneSnapshots snapshots) {
       snapshots.get("head").ifPresent(head -> {
                    R animationState = renderPassInfo.renderState();
                    boolean lookingAround = animationState.getGeckolibData(WaterfowlEntity.LOOKING_AROUND_TICKET);

                    if (lookingAround) {
                        head.setRotX(animationState.xRot * Mth.DEG_TO_RAD);
                        head.setRotY(animationState.yRot * Mth.DEG_TO_RAD);
                    }
                }
        );
    }

}

package net.untitledduckmod.client.renderer.entity;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.ItemInHandGeoLayer;
import com.geckolib.util.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.untitledduckmod.client.model.WaterfowlModel;
import net.untitledduckmod.common.entity.WaterfowlEntity;
import net.untitledduckmod.common.init.ModEntityTypes;

import static com.geckolib.constant.DefaultAnimations.hardcodedHeadRotation;

public class WaterfowlRenderer<T extends WaterfowlEntity, R extends LivingEntityRenderState & GeoRenderState> extends GeoEntityRenderer<T, R> {
    private static final float ADULT_SHADOW_RADIUS = 0.3f;

    public WaterfowlRenderer(WaterfowlModel<T> model, EntityRendererProvider.Context context) {
        super(context, model);
        this.shadowRadius = ADULT_SHADOW_RADIUS;
        withRenderLayer(new ItemInHandGeoLayer<>(context, this, "beak", "beak") {
            protected RenderData renderDataForHand(String boneName, HumanoidArm arm, T animatable, R renderState) {
                final HumanoidArm mainHandArm = animatable.getMainArm() == HumanoidArm.LEFT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
                final EquipmentSlot slot = arm == mainHandArm ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                final ItemDisplayContext context = switch (slot) {
                    case MAINHAND -> ItemDisplayContext.GROUND;
                    default -> ItemDisplayContext.NONE;
                };
                final ItemStack stack = animatable.getItemBySlot(slot);

                if (stack.getItem() instanceof ShieldItem)
                    renderState.addGeckolibData((slot == EquipmentSlot.MAINHAND ? MAINHAND_SHIELD : OFFHAND_SHIELD), true);

                return RenderData.item(boneName, context, RenderUtil.createRenderStateForItem(stack, this.itemModelResolver, context, animatable));
            }
            @Override
            protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStackRenderState stackRenderState, ItemDisplayContext displayContext, R renderState, SubmitNodeCollector renderTasks, int packedLight) {
                if (displayContext == ItemDisplayContext.GROUND) {
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
        renderState.addGeckolibData(WaterfowlEntity.LOOKING_AROUND_TICKET, animatable.lookingAround());
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
        super.adjustModelBonesForRender(renderPassInfo, snapshots);
        R animationState = renderPassInfo.renderState();
        boolean lookingAround = animationState.getOrDefaultGeckolibData(WaterfowlEntity.LOOKING_AROUND_TICKET, false);
        if (lookingAround) {
            hardcodedHeadRotation(renderPassInfo, snapshots, "head");
        }
    }

}

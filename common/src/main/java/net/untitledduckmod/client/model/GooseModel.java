package net.untitledduckmod.client.model;

import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.untitledduckmod.DuckMod;
import net.untitledduckmod.common.entity.GooseEntity;
import net.untitledduckmod.common.entity.WaterfowlEntity;

import java.util.Objects;

public class GooseModel extends WaterfowlModel<GooseEntity> {

    public GooseModel() {
        super(DuckMod.id("goose"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState instanceof LivingEntityRenderState livingEntityRenderState) {

            if (livingEntityRenderState.isBaby) {
                return ModelIdentifiers.GOSLING_TEXTURE;
            } else if (livingEntityRenderState.nameTag != null) {
                String name = Objects.requireNonNull(livingEntityRenderState.nameTag).getString().toLowerCase();
                switch (name) {
                    case "ping" -> {
                        return ModelIdentifiers.PING_GOOSE_TEXTURE;
                    }
                    case "sus" -> {
                        return ModelIdentifiers.SUS_GOOSE_TEXTURE;
                    }
                    case "untitled" -> {
                        return ModelIdentifiers.UNTITLED_GOOSE_TEXTURE;
                    }
                }
            }
        }
        var variant = renderState.hasGeckolibData(WaterfowlEntity.VARIANT_TICKET) ? renderState.getGeckolibData(WaterfowlEntity.VARIANT_TICKET) : 0;

        return switch (variant) {
            case 1 -> ModelIdentifiers.CANADIAN_GOOSE_TEXTURE;
            case 2 -> ModelIdentifiers.GREYLAG_GOOSE_TEXTURE;
            default -> ModelIdentifiers.GOOSE_TEXTURE;
        };
    }
}

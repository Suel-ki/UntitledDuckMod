package net.untitledduckmod.common.helper;

import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.UUID;

public class NbtHelper {

    public static boolean contains(ItemStack stack, DataComponentType<?> type) {
       return stack.has(type);
    }

    public static boolean containsUuid(CompoundTag tag, String key) {
        Tag nbtElement = tag.get(key);
        return nbtElement != null && nbtElement.getType() == IntArrayTag.TYPE && ((IntArrayTag)nbtElement).getAsIntArray().length == 4;
    }

    public static CompoundTag get(ItemStack stack, DataComponentType<CustomData> type) {
        CustomData data = stack.getComponents().get(type);
        if (data == null) {
            throw new NullPointerException("CompoundTag is null");
        } else {
            return data.copyTag();
        }
    }

    private static UUID toUuid(Tag element) {
        if (element == null) {
            throw new NullPointerException("Tag is null");
        }

        if (element.getType() != IntArrayTag.TYPE) {
            String var10002 = IntArrayTag.TYPE.getName();
            throw new IllegalArgumentException("Expected UUID-Tag to be of type " + var10002 + ", but found " + element.getType().getName() + ".");
        } else {
            int[] is = ((IntArrayTag)element).getAsIntArray();
            if (is.length != 4) {
                throw new IllegalArgumentException("Expected UUID-Array to be of length 4, but found " + is.length + ".");
            } else {
                return UUIDUtil.uuidFromIntArray(is);
            }
        }
    }

    public static UUID getUuid(CompoundTag tag, String key) {
        return toUuid(tag.get(key));
    }
}

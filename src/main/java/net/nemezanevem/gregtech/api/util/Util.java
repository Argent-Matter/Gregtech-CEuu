package net.nemezanevem.gregtech.api.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialFlagRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconSetRegistry;
import net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;

import java.util.NavigableMap;
import java.util.TreeMap;

import static net.nemezanevem.gregtech.api.util.GTValues.V;

public class Util {

    private static final NavigableMap<Long, Byte> tierByVoltage = new TreeMap<>();

    static {
        for (int i = 0; i < V.length; i++) {
            tierByVoltage.put(V[i], (byte) i);
        }
    }

    public static ResourceLocation gtResource(String path) {
        return new ResourceLocation(GregTech.MODID, path);
    }

    /**
     * Does almost the same thing as .to(LOWER_UNDERSCORE, string), but it also inserts underscores between words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maragingSteel300" -> "maraging_steel_300"
     */
    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i)) || (
                    Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maraging_steel_300" -> "maragingSteel300"
     */
    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_')
                continue;
            if (i == 0 || string.charAt(i - 1) == '_') {
                result.append(Character.toUpperCase(string.charAt(i)));
            } else {
                result.append(string.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * @return lowest tier that can handle passed voltage
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > V[GTValues.MAX]) return GTValues.MAX;
        return tierByVoltage.ceilingEntry(voltage).getValue();
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest tier below or equal to the voltage value given
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < V[GTValues.ULV]) return GTValues.ULV;
        return tierByVoltage.floorEntry(voltage).getValue();
    }


    public static ResourceLocation getId(MaterialFlag flag) {
        return MaterialFlagRegistry.MATERIAL_FLAGS_BUILTIN.get().getKey(flag);
    }
    public static ResourceLocation getId(MaterialIconSet set) {
        return MaterialIconSetRegistry.MATERIAL_ICON_SETS_BUILTIN.get().getKey(set);
    }
    public static ResourceLocation getId(PropertyKey<?> key) {
        return MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN.get().getKey(key);
    }
    public static ResourceLocation getId(Item key) {
        return ForgeRegistries.ITEMS.getKey(key);
    }
}

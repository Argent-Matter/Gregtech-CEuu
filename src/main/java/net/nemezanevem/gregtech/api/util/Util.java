package net.nemezanevem.gregtech.api.util;

import net.minecraft.resources.ResourceLocation;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialFlagRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconSetRegistry;
import net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;

public class Util {

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

    public static ResourceLocation getId(MaterialFlag flag) {
        return MaterialFlagRegistry.MATERIAL_FLAGS_BUILTIN.get().getKey(flag);
        return MaterialFlagRegistry.MATERIAL_FLAGS_BUILTIN.get().getKeys().toArray(ResourceLocation[]::new)[0].getPath()
    }
    public static ResourceLocation getId(MaterialIconSet set) {
        return MaterialIconSetRegistry.MATERIAL_ICONS_BUILTIN.get().getKey(set);
    }
    public static ResourceLocation getId(IMaterialProperty<?> set) {
        return MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN.get().getKey(set);
    }
}

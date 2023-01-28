package net.nemezanevem.gregtech.api.unification.material;

import com.google.common.collect.HashBiMap;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.GTValues;

import java.util.Arrays;
import java.util.List;

import static net.nemezanevem.gregtech.api.registry.material.MaterialRegistry.MATERIALS;

public class MarkerMaterials {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        Color.Colorless.toString();
        Tier.ULV.toString();
        Empty.toString();
    }

    /**
     * Marker materials without category
     */
    public static final RegistryObject<MarkerMaterial> Empty = MATERIALS.register("empty", () -> new MarkerMaterial("empty"));

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on TagPrefix
         * Often a default value for color prefixes
         */
        public static final RegistryObject<Material> Colorless = MATERIALS.register("colorless", () -> new MarkerMaterial("colorless"));

        public static final RegistryObject<Material> White = MATERIALS.register("white", () -> new MarkerMaterial("white"));
        public static final RegistryObject<Material> Orange = MATERIALS.register("orange", () -> new MarkerMaterial("orange"));
        public static final RegistryObject<Material> Magenta = MATERIALS.register("magenta", () -> new MarkerMaterial("magenta"));
        public static final RegistryObject<Material> LightBlue = MATERIALS.register("light_blue", () -> new MarkerMaterial("light_blue"));
        public static final RegistryObject<Material> Yellow = MATERIALS.register("yellow", () -> new MarkerMaterial("yellow"));
        public static final RegistryObject<Material> Lime = MATERIALS.register("lime", () -> new MarkerMaterial("lime"));
        public static final RegistryObject<Material> Pink = MATERIALS.register("pink", () -> new MarkerMaterial("pink"));
        public static final RegistryObject<Material> Gray = MATERIALS.register("gray", () -> new MarkerMaterial("gray"));
        public static final RegistryObject<Material> LightGray = MATERIALS.register("light_gray", () -> new MarkerMaterial("light_gray"));
        public static final RegistryObject<Material> Cyan = MATERIALS.register("cyan", () -> new MarkerMaterial("cyan"));
        public static final RegistryObject<Material> Purple = MATERIALS.register("purple", () -> new MarkerMaterial("purple"));
        public static final RegistryObject<Material> Blue = MATERIALS.register("blue", () -> new MarkerMaterial("blue"));
        public static final RegistryObject<Material> Brown = MATERIALS.register("brown", () -> new MarkerMaterial("brown"));
        public static final RegistryObject<Material> Green = MATERIALS.register("green", () -> new MarkerMaterial("green"));
        public static final RegistryObject<Material> Red = MATERIALS.register("red", () -> new MarkerMaterial("red"));
        public static final RegistryObject<Material> Black = MATERIALS.register("black", () -> new MarkerMaterial("black"));

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final List<RegistryObject<Material>> VALUES = Arrays.asList(White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown, Green, Red, Black);
        /**
         * Gets color by it's name
         * Name format is equal to EnumDyeColor
         */
        public static MarkerMaterial valueOf(String string) {
            for (RegistryObject<Material> color : VALUES) {
                if (color.get().toString().equals(string)) {
                    return (MarkerMaterial) color.get();
                }
            }
            return null;
        }

        /**
         * Contains associations between MC EnumDyeColor and Color MarkerMaterial
         */
        public static final HashBiMap<DyeColor, MarkerMaterial> COLORS = HashBiMap.create();

        static {
            for (DyeColor color : DyeColor.values()) {
                COLORS.put(color, Color.valueOf(color.getName()));
            }
        }

    }

    /**
     * Circuitry, batteries and other technical things
     */
    public static class Tier {
        public static final RegistryObject<Material> ULV = MATERIALS.register(GTValues.VN[GTValues.ULV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.ULV].toLowerCase()));
        public static final RegistryObject<Material> LV = MATERIALS.register(GTValues.VN[GTValues.LV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.LV].toLowerCase()));
        public static final RegistryObject<Material> MV = MATERIALS.register(GTValues.VN[GTValues.MV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.MV].toLowerCase()));
        public static final RegistryObject<Material> HV = MATERIALS.register(GTValues.VN[GTValues.HV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.HV].toLowerCase()));
        public static final RegistryObject<Material> EV = MATERIALS.register(GTValues.VN[GTValues.EV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.EV].toLowerCase()));
        public static final RegistryObject<Material> IV = MATERIALS.register(GTValues.VN[GTValues.IV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.IV].toLowerCase()));
        public static final RegistryObject<Material> LuV = MATERIALS.register(GTValues.VN[GTValues.LuV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.LuV].toLowerCase()));
        public static final RegistryObject<Material> ZPM = MATERIALS.register(GTValues.VN[GTValues.ZPM].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.ZPM].toLowerCase()));
        public static final RegistryObject<Material> UV = MATERIALS.register(GTValues.VN[GTValues.UV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.UV].toLowerCase()));
        public static final RegistryObject<Material> UHV = MATERIALS.register(GTValues.VN[GTValues.UHV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.UHV].toLowerCase()));

        public static final RegistryObject<Material> UEV = MATERIALS.register(GTValues.VN[GTValues.UEV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.UEV].toLowerCase()));
        public static final RegistryObject<Material> UIV = MATERIALS.register(GTValues.VN[GTValues.UIV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.UIV].toLowerCase()));
        public static final RegistryObject<Material> UXV = MATERIALS.register(GTValues.VN[GTValues.UXV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.UXV].toLowerCase()));
        public static final RegistryObject<Material> OpV = MATERIALS.register(GTValues.VN[GTValues.OpV].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.OpV].toLowerCase()));
        public static final RegistryObject<Material> MAX = MATERIALS.register(GTValues.VN[GTValues.MAX].toLowerCase(), () -> new MarkerMaterial(GTValues.VN[GTValues.MAX].toLowerCase()));
    }

    public static class Component {
        public static final RegistryObject<Material> Resistor = MATERIALS.register("resistor", () -> new MarkerMaterial("resistor"));
        public static final RegistryObject<Material> Transistor = MATERIALS.register("transistor", () -> new MarkerMaterial("transistor"));
        public static final RegistryObject<Material> Capacitor = MATERIALS.register("capacitor", () -> new MarkerMaterial("capacitor"));
        public static final RegistryObject<Material> Diode = MATERIALS.register("diode", () -> new MarkerMaterial("diode"));
        public static final RegistryObject<Material> Inductor = MATERIALS.register("inductor", () -> new MarkerMaterial("inductor"));
    }

}

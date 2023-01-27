package net.nemezanevem.gregtech.api.recipe.property;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.api.tileentity.multiblock.CleanroomType;

import java.util.List;

import static net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry.RECIPE_PROPERTIES;

public class GtRecipeProperties {

    public static final RegistryObject<RecipeProperty<CleanroomType>> CLEANROOM = RECIPE_PROPERTIES.register("cleanroom", CleanroomProperty::getInstance);
    public static final RegistryObject<RecipeProperty<Long>> FUSION_EU_TO_START = RECIPE_PROPERTIES.register("eu_to_start", FusionEUToStartProperty::getInstance);
    public static final RegistryObject<RecipeProperty<DimensionType[]>> GAS_COLLECTOR_DIMENSION = RECIPE_PROPERTIES.register("dimension", GasCollectorDimensionProperty::getInstance);
    public static final RegistryObject<RecipeProperty<ItemStack>> IMPLOSION_EXPLOSIVE = RECIPE_PROPERTIES.register("explosives", ImplosionExplosiveProperty::getInstance);
    public static final RegistryObject<RecipeProperty<Boolean>> PRIMITIVE = RECIPE_PROPERTIES.register("primitive", PrimitiveProperty::getInstance);

    public static void init() {}
}

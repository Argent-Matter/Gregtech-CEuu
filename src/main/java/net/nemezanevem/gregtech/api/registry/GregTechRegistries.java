package net.nemezanevem.gregtech.api.registry;

import net.minecraftforge.registries.IForgeRegistry;
import net.nemezanevem.gregtech.api.cover.CoverDefinition;
import net.nemezanevem.gregtech.api.gui.UIFactory;
import net.nemezanevem.gregtech.api.recipe.property.RecipeProperty;
import net.nemezanevem.gregtech.api.registry.cover.CoverRegistry;
import net.nemezanevem.gregtech.api.registry.gui.UIFactoryRegistry;
import net.nemezanevem.gregtech.api.registry.material.*;
import net.nemezanevem.gregtech.api.registry.material.info.*;
import net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry;
import net.nemezanevem.gregtech.api.registry.recipe.property.RecipePropertyRegistry;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.registry.tileentity.multiblock.MultiBlockAbilityRegistry;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.MultiblockAbility;
import net.nemezanevem.gregtech.api.unification.material.Element;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconType;

import java.util.function.Supplier;

public class GregTechRegistries {

    public static Supplier<IForgeRegistry<UIFactory<?>>> UI_FACTORY = UIFactoryRegistry.UI_FACTORIES_BUILTIN;

    public static Supplier<IForgeRegistry<Element>> ELEMENT = ElementRegistry.ELEMENTS_BUILTIN;
    public static Supplier<IForgeRegistry<Material>> MATERIAL = MaterialRegistry.MATERIALS_BUILTIN;
    public static Supplier<IForgeRegistry<PropertyKey<?>>> MATERIAL_PROPERTY = MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN;
    public static Supplier<IForgeRegistry<MaterialFlag>> MATERIAL_FLAG = MaterialFlagRegistry.MATERIAL_FLAGS_BUILTIN;
    public static Supplier<IForgeRegistry<MaterialIconSet>> MATERIAL_ICON_SET = MaterialIconSetRegistry.MATERIAL_ICON_SETS_BUILTIN;
    public static Supplier<IForgeRegistry<MaterialIconType>> MATERIAL_ICON_TYPE = MaterialIconTypeRegistry.MATERIAL_ICON_TYPES_BUILTIN;

    public static Supplier<IForgeRegistry<RecipeProperty<?>>> RECIPE_PROPERTY = RecipePropertyRegistry.RECIPE_PROPERTIES_BUILTIN;

    public static Supplier<IForgeRegistry<MetaTileEntity>> META_TILE_ENTITY = MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN;
    public static Supplier<IForgeRegistry<MultiblockAbility<?>>> MULTIBLOCK_ABILITY = MultiBlockAbilityRegistry.MULTIBLOCK_ABILITIES_BUILTIN;
    public static Supplier<IForgeRegistry<CoverDefinition>> COVER = CoverRegistry.COVERS_BUILTIN;
}

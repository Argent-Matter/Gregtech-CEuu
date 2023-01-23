package net.nemezanevem.gregtech.api.unification.material;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.RegistryObject;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.fluids.GtFluidTypes;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconSets;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.*;
import net.nemezanevem.gregtech.api.unification.stack.MaterialStack;
import net.nemezanevem.gregtech.api.util.SmallDigits;
import net.nemezanevem.gregtech.api.util.Util;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public class Material implements Comparable<Material> {

    /**
     * Basic Info of this Material.
     *
     * @see MaterialInfo
     */
    @Nonnull
    private final MaterialInfo materialInfo;

    /**
     * Properties of this Material.
     *
     * @see MaterialProperties
     */
    @Nonnull
    private final MaterialProperties properties;

    /**
     * Generation flags of this material
     *
     * @see MaterialFlags
     */
    @Nonnull
    private final MaterialFlags flags;

    /**
     * Chemical formula of this material
     */
    private String chemicalFormula;

    // TODO Fix isotope tooltips being set toSmallDownNumbers
    private String calculateChemicalFormula() {
        if (chemicalFormula != null) return this.chemicalFormula;
        if (materialInfo.element != null) {
            return materialInfo.element.getSymbol();
        }
        if (!materialInfo.componentList.isEmpty()) {
            StringBuilder components = new StringBuilder();
            for (MaterialStack component : materialInfo.componentList)
                components.append(component.toString());
            return components.toString();
        }
        return "";
    }

    public String getChemicalFormula() {
        return chemicalFormula;
    }

    public Material setFormula(String formula) {
        return setFormula(formula, false);
    }

    public Material setFormula(String formula, boolean withFormatting) {
        this.chemicalFormula = withFormatting ? SmallDigits.toSmallDownNumbers(formula) : formula;
        return this;
    }

    public ImmutableList<MaterialStack> getMaterialComponents() {
        return materialInfo.componentList;
    }

    public MaterialStack[] getMaterialComponentsCt() {
        return getMaterialComponents().toArray(new MaterialStack[0]);
    }

    private Material(@Nonnull MaterialInfo materialInfo, @Nonnull MaterialProperties properties, @Nonnull MaterialFlags flags) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.flags = flags;
        this.properties.setMaterial(this);
    }

    // thou shall not call
    protected Material(String name) {
        materialInfo = new MaterialInfo(name);
        materialInfo.iconSet = GtMaterialIconSets.DULL.get();
        properties = new MaterialProperties();
        flags = new MaterialFlags();
    }

    public void addFlags(MaterialFlag... flags) {
        /*if (RegistryAccess.BUILTIN.get() instanceof )
            throw new IllegalStateException("Cannot add flag to material when registry is frozen!");*/
        this.flags.addFlags(flags).verify(this);
    }

    public void addFlags(String... names) {
        addFlags(Arrays.stream(names)
                .map(MaterialFlag::getByName)
                .filter(Objects::nonNull)
                .toArray(MaterialFlag[]::new));
    }

    public boolean hasFlag(MaterialFlag flag) {
        return flags.hasFlag(flag);
    }

    public boolean hasFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).allMatch(this::hasFlag);
    }

    public boolean hasAnyOfFlags(MaterialFlag... flags) {
        return Arrays.stream(flags).anyMatch(this::hasFlag);
    }

    protected void calculateDecompositionType() {
        if (!materialInfo.componentList.isEmpty() &&
                !hasFlag(GtMaterialFlags.DECOMPOSITION_BY_CENTRIFUGING.get()) &&
                !hasFlag(GtMaterialFlags.DECOMPOSITION_BY_ELECTROLYZING.get()) &&
                !hasFlag(GtMaterialFlags.DISABLE_DECOMPOSITION.get())) {
            boolean onlyMetalMaterials = true;
            for (MaterialStack materialStack : materialInfo.componentList) {
                Material material = materialStack.material;
                onlyMetalMaterials &= material.hasProperty(GtMaterialProperties.INGOT.get());
            }
            //allow centrifuging of alloy materials only
            if (onlyMetalMaterials) {
                flags.addFlags(GtMaterialFlags.DECOMPOSITION_BY_CENTRIFUGING.get());
            } else {
                flags.addFlags(GtMaterialFlags.DECOMPOSITION_BY_ELECTROLYZING.get());
            }
        }
    }

    public Fluid getFluid() {
        FluidProperty prop = this.getProperty(GtMaterialProperties.FLUID.get());
        if (prop == null)
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a Fluid!");

        Fluid fluid = prop.getFluid();
        if (fluid == null)
            GregTech.LOGGER.warn("Material {} Fluid was null!", this);

        return fluid;
    }

    public FluidStack getFluid(int amount) {
        return new FluidStack(getFluid(), amount);
    }

    public int getBlockHarvestLevel() {
        if (!hasProperty(GtMaterialProperties.DUST.get()))
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a harvest level! Is probably a Fluid");
        int harvestLevel = this.<DustProperty>getProperty(GtMaterialProperties.DUST.get()).getHarvestLevel();
        return harvestLevel > 0 ? harvestLevel - 1 : harvestLevel;
    }

    public int getToolHarvestLevel() {
        if (!hasProperty(GtMaterialProperties.TOOL.get()))
            throw new IllegalArgumentException("Material " + materialInfo.name + " does not have a tool harvest level! Is probably not a Tool Material");
        return this.<ToolProperty>getProperty(GtMaterialProperties.TOOL.get()).getToolHarvestLevel();
    }

    public void setMaterialRGB(int materialRGB) {
        materialInfo.color = materialRGB;
    }

    public int getMaterialRGB() {
        return materialInfo.color;
    }

    public boolean hasFluidColor() {
        return materialInfo.hasFluidColor;
    }

    public void setMaterialIconSet(MaterialIconSet materialIconSet) {
        materialInfo.iconSet = materialIconSet;
    }

    public MaterialIconSet getMaterialIconSet() {
        return materialInfo.iconSet;
    }

    public boolean isRadioactive() {
        if (materialInfo.element != null)
            return materialInfo.element.halfLifeSeconds >= 0;
        for (MaterialStack material : materialInfo.componentList)
            if (material.material.isRadioactive()) return true;
        return false;
    }

    public long getProtons() {
        if (materialInfo.element != null)
            return materialInfo.element.getProtons();
        if (materialInfo.componentList.isEmpty())
            return Math.max(1, GtElements.Tc.get().getProtons());
        long totalProtons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalProtons += material.amount * material.material.getProtons();
        }
        return totalProtons / totalAmount;
    }

    public long getNeutrons() {
        if (materialInfo.element != null)
            return materialInfo.element.getNeutrons();
        if (materialInfo.componentList.isEmpty())
            return GtElements.Tc.get().getNeutrons();
        long totalNeutrons = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalNeutrons += material.amount * material.material.getNeutrons();
        }
        return totalNeutrons / totalAmount;
    }


    public long getMass() {
        if (materialInfo.element != null)
            return materialInfo.element.getMass();
        if (materialInfo.componentList.size() <= 0)
            return GtElements.Tc.get().getMass();
        long totalMass = 0, totalAmount = 0;
        for (MaterialStack material : materialInfo.componentList) {
            totalAmount += material.amount;
            totalMass += material.amount * material.material.getMass();
        }
        return totalMass / totalAmount;
    }

    public int getBlastTemperature() {
        BlastProperty prop = properties.getProperty(GtMaterialProperties.BLAST.get());
        return prop == null ? 0 : prop.getBlastTemperature();
    }

    public FluidStack getPlasma(int amount) {
        PlasmaProperty prop = properties.getProperty(GtMaterialProperties.PLASMA.get());
        return prop == null ? null : prop.getPlasma(amount);
    }

    public String toLowerUnderscoreString() {
        return Util.toLowerCaseUnderscore(toString());
    }

    public String getUnlocalizedName() {
        return "material." + materialInfo.name;
    }

    public MutableComponent getLocalizedName() {
        return Component.translatable(getUnlocalizedName());
    }

    @Override
    public int compareTo(Material material) {
        return toString().compareTo(material.toString());
    }

    @Override
    public String toString() {
        return materialInfo.name;
    }

    @Nonnull
    public MaterialProperties getProperties() {
        return properties;
    }

    public <T extends IMaterialProperty<T>> boolean hasProperty(PropertyKey<T> key) {
        return getProperty(key) != null;
    }

    public <T extends IMaterialProperty<T>> T getProperty(PropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends IMaterialProperty<T>> void setProperty(PropertyKey<T> key, IMaterialProperty<T> property) {
        /*if (GregTechAPI.MATERIAL_REGISTRY.isFrozen()) {
            throw new IllegalStateException("Cannot add properties to a Material when registry is frozen!");
        }*/
        properties.setProperty(key, property);
        properties.verify();
    }

    public boolean isSolid() {
        return hasProperty(GtMaterialProperties.INGOT.get()) || hasProperty(GtMaterialProperties.GEM.get());
    }

    public boolean hasFluid() {
        return hasProperty(GtMaterialProperties.FLUID.get());
    }

    public void verifyMaterial() {
        properties.verify();
        flags.verify(this);
        this.chemicalFormula = calculateChemicalFormula();
        calculateDecompositionType();
    }

    /**
     * @since GTCEu 2.0.0
     */
    public static class Builder {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private final MaterialFlags flags;

        /*
         * The temporary list of components for this Material.
         */
        private List<MaterialStack> composition = new ArrayList<>();

        /*
         * Temporary value to use to determine how to calculate default RGB
         */
        private boolean averageRGB = false;

        /**
         * Constructs a {@link Material}. This Builder replaces the old constructors, and
         * no longer uses a class hierarchy, instead using a {@link MaterialProperties} system.
         *
         * @param name The Name of this Material. Will be formatted as
         *             "material.<name>" for the Translation Key.
         * @since GTCEu 2.0.0
         */
        public Builder(String name) {
            if (name.charAt(name.length() - 1) == '_')
                throw new IllegalArgumentException("Material name cannot end with a '_'!");
            materialInfo = new MaterialInfo(name);
            properties = new MaterialProperties();
            flags = new MaterialFlags();
        }

        /*
         * Material Types
         */

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Will be created as a {@link GtFluidTypes#LIQUID}, without a Fluid Block.
         *
         * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
         */
        public Builder fluid() {
            properties.ensureSet(GtMaterialProperties.FLUID.get());
            return this;
        }

        /**
         * Add a {@link FluidProperty} to this Material.<br>
         * Will be created without a Fluid Block.
         *
         * @param type The {@link FluidType} of this Material, either Fluid or Gas.
         * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
         */
        public Builder fluid(FluidType type) {
            return fluid(type, false);
        }

        /**
         * Add a {@link FluidProperty} to this Material.
         *
         * @param type     The {@link FluidType} of this Material.
         * @param hasBlock If true, create a Fluid Block for this Material.
         * @throws IllegalArgumentException If a {@link FluidProperty} has already been added to this Material.
         */
        public Builder fluid(FluidType type, boolean hasBlock) {
            properties.setProperty(GtMaterialProperties.FLUID.get(), new FluidProperty(type, hasBlock));
            return this;
        }

        /**
         * Add a {@link PlasmaProperty} to this Material.<br>
         * Is not required to have a {@link FluidProperty}, and will not automatically apply one.
         *
         * @throws IllegalArgumentException If a {@link PlasmaProperty} has already been added to this Material.
         */
        public Builder plasma() {
            properties.ensureSet(GtMaterialProperties.PLASMA.get());
            return this;
        }

        /**
         * Add a {@link DustProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).
         *
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust() {
            properties.ensureSet(GtMaterialProperties.DUST.get());
            return this;
        }

        /**
         * Add a {@link DustProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining Level.
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust(int harvestLevel) {
            return dust(harvestLevel, 0);
        }

        /**
         * Add a {@link DustProperty} to this Material.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining Level.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.
         * @throws IllegalArgumentException If a {@link DustProperty} has already been added to this Material.
         */
        public Builder dust(int harvestLevel, int burnTime) {
            properties.setProperty(GtMaterialProperties.DUST.get(), new DustProperty(harvestLevel, burnTime));
            return this;
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot() {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            return this;
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot(int harvestLevel) {
            return ingot(harvestLevel, 0);
        }

        /**
         * Add an {@link IngotProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining. 2 will make it require a iron tool.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level (-1). So 2 will make the tool harvest diamonds.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
         *                     If this Material already had a Burn Time defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link IngotProperty} has already been added to this Material.
         */
        public Builder ingot(int harvestLevel, int burnTime) {
            DustProperty prop = properties.getProperty(GtMaterialProperties.DUST.get());
            if (prop == null) dust(harvestLevel, burnTime);
            else {
                if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
                if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
            }
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            return this;
        }

        /**
         * Add a {@link GemProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
         */
        public Builder gem() {
            properties.ensureSet(GtMaterialProperties.GEM.get());
            return this;
        }

        /**
         * Add a {@link GemProperty} to this Material.<br>
         * Will be created with no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If a {@link GemProperty} has already been added to this Material.
         */
        public Builder gem(int harvestLevel) {
            return gem(harvestLevel, 0);
        }

        /**
         * Add a {@link GemProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @param burnTime     The Burn Time (in ticks) of this Material as a Furnace Fuel.<br>
         *                     If this Material already had a Burn Time defined, it will be overridden.
         */
        public Builder gem(int harvestLevel, int burnTime) {
            DustProperty prop = properties.getProperty(GtMaterialProperties.DUST.get());
            if (prop == null) dust(harvestLevel, burnTime);
            else {
                if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
                if (prop.getBurnTime() == 0) prop.setBurnTime(burnTime);
            }
            properties.ensureSet(GtMaterialProperties.GEM.get());
            return this;
        }

        /**
         * Add a {@link PolymerProperty} to this Material.<br>
         * Will be created with a Harvest Level of 2 and no Burn Time (Furnace Fuel).<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         *
         * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
         */
        public Builder polymer() {
            properties.ensureSet(GtMaterialProperties.POLYMER.get());
            return this;
        }

        /**
         * Add a {@link PolymerProperty} to this Material.<br>
         * Will automatically add a {@link DustProperty} to this Material if it does not already have one.
         * Will have a burn time of 0
         *
         * @param harvestLevel The Harvest Level of this block for Mining.<br>
         *                     If this Material also has a {@link ToolProperty}, this value will
         *                     also be used to determine the tool's Mining level.<br>
         *                     If this Material already had a Harvest Level defined, it will be overridden.
         * @throws IllegalArgumentException If an {@link PolymerProperty} has already been added to this Material.
         */
        public Builder polymer(int harvestLevel) {
            DustProperty prop = properties.getProperty(GtMaterialProperties.DUST.get());
            if (prop == null) dust(harvestLevel, 0);
            else if (prop.getHarvestLevel() == 2) prop.setHarvestLevel(harvestLevel);
            properties.ensureSet(GtMaterialProperties.POLYMER.get());
            properties.ensureSet(GtMaterialProperties.FLUID.get());
            return this;
        }

        public Builder burnTime(int burnTime) {
            DustProperty prop = properties.getProperty(GtMaterialProperties.DUST.get());
            if (prop == null) {
                dust();
                prop = properties.getProperty(GtMaterialProperties.DUST.get());
            }
            prop.setBurnTime(burnTime);
            return this;
        }

        /**
         * Set the Color of this Material.<br>
         * Defaults to 0xFFFFFF unless {@link Builder#colorAverage()} was called, where
         * it will be a weighted average of the components of the Material.
         *
         * @param color The RGB-formatted Color.
         */
        public Builder color(int color) {
            color(color, true);
            return this;
        }

        /**
         * Set the Color of this Material.<br>
         * Defaults to 0xFFFFFF unless {@link Builder#colorAverage()} was called, where
         * it will be a weighted average of the components of the Material.
         *
         * @param color         The RGB-formatted Color.
         * @param hasFluidColor Whether the fluid should be colored or not.
         */
        public Builder color(int color, boolean hasFluidColor) {
            this.materialInfo.color = color;
            this.materialInfo.hasFluidColor = hasFluidColor;
            return this;
        }

        public Builder colorAverage() {
            this.averageRGB = true;
            return this;
        }

        /**
         * Set the {@link MaterialIconSet} of this Material.<br>
         * Defaults vary depending on if the Material has a:<br>
         * <ul>
         * <li> {@link GemProperty}, it will default to {@link MaterialIconSet#GEM_VERTICAL}
         * <li> {@link IngotProperty} or {@link DustProperty}, it will default to {@link MaterialIconSet#DULL}
         * <li> {@link FluidProperty}, it will default to either {@link MaterialIconSet#FLUID}
         *      or {@link MaterialIconSet#GAS}, depending on the {@link FluidType}
         * <li> {@link PlasmaProperty}, it will default to {@link MaterialIconSet#FLUID}
         * </ul>
         * Default will be determined by first-found Property in this order, unless specified.
         *
         * @param iconSet The {@link MaterialIconSet} of this Material.
         */
        public Builder iconSet(MaterialIconSet iconSet) {
            materialInfo.iconSet = iconSet;
            return this;
        }

        public Builder components(Object... components) {
            Preconditions.checkArgument(
                    components.length % 2 == 0,
                    "Material Components list malformed!"
            );

            for (int i = 0; i < components.length; i += 2) {
                if (components[i] == null) {
                    throw new IllegalArgumentException("Material in Components List is null for Material "
                            + this.materialInfo.name);
                }
                composition.add(new MaterialStack(
                        (Material) components[i],
                        (Integer) components[i + 1]
                ));
            }
            return this;
        }

        public Builder components(MaterialStack... components) {
            composition = Arrays.asList(components);
            return this;
        }

        public Builder components(ImmutableList<MaterialStack> components) {
            composition = components;
            return this;
        }

        /**
         * Add {@link MaterialFlags} to this Material.<br>
         * Dependent Flags (for example, {@link GtMaterialFlags#GENERATE_LONG_ROD} requiring
         * {@link GtMaterialFlags#GENERATE_ROD}) will be automatically applied.
         */
        public Builder flags(MaterialFlag... flags) {
            this.flags.addFlags(flags);
            return this;
        }

        /**
         * Add {@link MaterialFlags} to this Material.<br>
         * Dependent Flags (for example, {@link GtMaterialFlags#GENERATE_LONG_ROD} requiring
         * {@link GtMaterialFlags#GENERATE_ROD}) will be automatically applied.
         *
         * @param f1 A {@link Collection} of {@link MaterialFlag}. Provided this way for easy Flag presets to be applied.
         * @param f2 An Array of {@link MaterialFlag}. If no {@link Collection} is required, use {@link Builder#flags(MaterialFlag...)}.
         */
        public Builder flags(Collection<MaterialFlag> f1, MaterialFlag... f2) {
            this.flags.addFlags(f1.toArray(MaterialFlag[]::new));
            this.flags.addFlags(f2);
            return this;
        }

        public Builder element(RegistryObject<Element> element) {
            this.materialInfo.element = element.get();
            return this;
        }

        /**
         * Replaced the old toolStats methods which took many parameters.
         * Use {@link ToolProperty.Builder} instead to create a Tool Property.
         */
        public Builder toolStats(ToolProperty toolProperty) {
            properties.setProperty(GtMaterialProperties.TOOL.get(), toolProperty);
            return this;
        }

        public Builder rotorStats(float speed, float damage, int durability) {
            properties.setProperty(GtMaterialProperties.ROTOR.get(), new RotorProperty(speed, damage, durability));
            return this;
        }

        public Builder blastTemp(int temp) {
            properties.setProperty(GtMaterialProperties.BLAST.get(), new BlastProperty(temp));
            return this;
        }

        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier) {
            properties.setProperty(GtMaterialProperties.BLAST.get(), new BlastProperty(temp, gasTier, -1, -1));
            return this;
        }

        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride) {
            properties.setProperty(GtMaterialProperties.BLAST.get(), new BlastProperty(temp, gasTier, eutOverride, -1));
            return this;
        }

        public Builder blastTemp(int temp, BlastProperty.GasTier gasTier, int eutOverride, int durationOverride) {
            properties.setProperty(GtMaterialProperties.BLAST.get(), new BlastProperty(temp, gasTier, eutOverride, durationOverride));
            return this;
        }

        public Builder ore() {
            properties.ensureSet(GtMaterialProperties.ORE.get());
            return this;
        }

        public Builder ore(boolean emissive) {
            properties.setProperty(GtMaterialProperties.ORE.get(), new OreProperty(1, 1, emissive));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier) {
            properties.setProperty(GtMaterialProperties.ORE.get(), new OreProperty(oreMultiplier, byproductMultiplier));
            return this;
        }

        public Builder ore(int oreMultiplier, int byproductMultiplier, boolean emissive) {
            properties.setProperty(GtMaterialProperties.ORE.get(), new OreProperty(oreMultiplier, byproductMultiplier, emissive));
            return this;
        }

        public Builder fluidTemp(int temp) {
            properties.ensureSet(GtMaterialProperties.FLUID.get());
            properties.<FluidProperty>getProperty(GtMaterialProperties.FLUID.get()).setFluidTemperature(temp);
            return this;
        }

        public Builder washedIn(Material m) {
            properties.ensureSet(GtMaterialProperties.ORE.get());
            properties.<OreProperty>getProperty(GtMaterialProperties.ORE.get()).setWashedIn(m);
            return this;
        }

        public Builder washedIn(Material m, int washedAmount) {
            properties.ensureSet(GtMaterialProperties.ORE.get());
            properties.<OreProperty>getProperty(GtMaterialProperties.ORE.get()).setWashedIn(m, washedAmount);
            return this;
        }

        public Builder separatedInto(Material... m) {
            properties.ensureSet(GtMaterialProperties.ORE.get());
            properties.<OreProperty>getProperty(GtMaterialProperties.ORE.get()).setSeparatedInto(m);
            return this;
        }

        public Builder oreSmeltInto(Material m) {
            properties.ensureSet(GtMaterialProperties.ORE.get());
            properties.<OreProperty>getProperty(GtMaterialProperties.ORE.get()).setDirectSmeltResult(m);
            return this;
        }

        public Builder polarizesInto(Material m) {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            properties.<IngotProperty>getProperty(GtMaterialProperties.INGOT.get()).setMagneticMaterial(m);
            return this;
        }

        public Builder arcSmeltInto(Material m) {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            properties.<IngotProperty>getProperty(GtMaterialProperties.INGOT.get()).setArcSmeltingInto(m);
            return this;
        }

        public Builder macerateInto(Material m) {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            properties.<IngotProperty>getProperty(GtMaterialProperties.INGOT.get()).setMacerateInto(m);
            return this;
        }

        public Builder ingotSmeltInto(Material m) {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            properties.<IngotProperty>getProperty(GtMaterialProperties.INGOT.get()).setSmeltingInto(m);
            return this;
        }

        public Builder addOreByproducts(Material... byproducts) {
            properties.ensureSet(GtMaterialProperties.ORE.get());
            properties.<OreProperty>getProperty(GtMaterialProperties.ORE.get()).setOreByProducts(byproducts);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss) {
            cableProperties((int) voltage, amperage, loss, false);
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon) {
            properties.ensureSet(GtMaterialProperties.DUST.get());
            properties.setProperty(GtMaterialProperties.WIRE.get(), new WireProperty((int) voltage, amperage, loss, isSuperCon));
            return this;
        }

        public Builder cableProperties(long voltage, int amperage, int loss, boolean isSuperCon, int criticalTemperature) {
            properties.ensureSet(GtMaterialProperties.DUST.get());
            properties.setProperty(GtMaterialProperties.WIRE.get(), new WireProperty((int) voltage, amperage, loss, isSuperCon, criticalTemperature));
            return this;
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof) {
            return fluidPipeProperties(maxTemp, throughput, gasProof, false, false, false);
        }

        public Builder fluidPipeProperties(int maxTemp, int throughput, boolean gasProof, boolean acidProof, boolean cryoProof, boolean plasmaProof) {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            properties.setProperty(GtMaterialProperties.FLUID_PIPE.get(), new FluidPipeProperty(maxTemp, throughput, gasProof, acidProof, cryoProof, plasmaProof));
            return this;
        }

        public Builder itemPipeProperties(int priority, float stacksPerSec) {
            properties.ensureSet(GtMaterialProperties.INGOT.get());
            properties.setProperty(GtMaterialProperties.ITEM_PIPE.get(), new ItemPipeProperty(priority, stacksPerSec));
            return this;
        }

        @Deprecated
        public Builder addDefaultEnchant(Enchantment enchant, int level) {
            if (!properties.hasProperty(GtMaterialProperties.TOOL.get())) // cannot assign default here
                throw new IllegalArgumentException("Material cannot have an Enchant without Tools!");
            properties.<ToolProperty>getProperty(GtMaterialProperties.TOOL.get()).addEnchantmentForTools(enchant, level);
            return this;
        }

        public Material build() {
            materialInfo.componentList = ImmutableList.copyOf(composition);
            materialInfo.verifyInfo(properties, averageRGB);
            return new Material(materialInfo, properties, flags);
        }
    }

    /**
     * Holds the basic info for a Material, like the name, color, id, etc..
     */
    private static class MaterialInfo {
        /**
         * The unlocalized name of this Material.
         * <p>
         * Required.
         */
        private final String name;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        private int color = -1;

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        private boolean hasFluidColor = true;

        /**
         * The IconSet of this Material.
         * <p>
         * Default: - GEM_VERTICAL if it has GemProperty.
         * - DULL if has DustProperty or IngotProperty.
         * - FLUID or GAS if only has FluidProperty or PlasmaProperty, depending on {@link FluidType}.
         */
        private MaterialIconSet iconSet;

        /**
         * The components of this Material.
         * <p>
         * Default: none.
         */
        private ImmutableList<MaterialStack> componentList;

        /**
         * The Element of this Material, if it is a direct Element.
         * <p>
         * Default: none.
         */
        private Element element;

        private MaterialInfo(String name) {
            if (!Util.toLowerCaseUnderscore(Util.lowerUnderscoreToUpperCamel(name)).equals(name))
                throw new IllegalStateException("Cannot add materials with names like 'materialnumber'! Use 'material_number' instead.");
            this.name = name;
        }

        private void verifyInfo(MaterialProperties props, boolean averageRGB) {

            // Verify IconSet
            if (iconSet == null) {
                if (props.hasProperty(GtMaterialProperties.GEM.get())) {
                    iconSet = GtMaterialIconSets.GEM_VERTICAL.get();
                } else if (props.hasProperty(GtMaterialProperties.DUST.get()) || props.hasProperty(GtMaterialProperties.INGOT.get()) || props.hasProperty(GtMaterialProperties.POLYMER.get())) {
                    iconSet = GtMaterialIconSets.DULL.get();
                } else if (props.hasProperty(GtMaterialProperties.FLUID.get())) {
                    if (props.<FluidProperty>getProperty(GtMaterialProperties.FLUID.get()).isGas()) {
                        iconSet = GtMaterialIconSets.GAS.get();
                    } else iconSet = GtMaterialIconSets.FLUID.get();
                } else if (props.hasProperty(GtMaterialProperties.PLASMA.get()))
                    iconSet = GtMaterialIconSets.FLUID.get();
                else iconSet = GtMaterialIconSets.DULL.get();
            }

            // Verify MaterialRGB
            if (color == -1) {
                if (!averageRGB || componentList.isEmpty())
                    color = 0xFFFFFF;
                else {
                    long colorTemp = 0;
                    int divisor = 0;
                    for (MaterialStack stack : componentList) {
                        colorTemp += stack.material.getMaterialRGB() * stack.amount;
                        divisor += stack.amount;
                    }
                    color = (int) (colorTemp / divisor);
                }
            }
        }
    }
}
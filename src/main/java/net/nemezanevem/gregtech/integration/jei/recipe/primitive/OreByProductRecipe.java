package net.nemezanevem.gregtech.integration.jei.recipe.primitive;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidStack;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.recipe.GTRecipe.ChanceEntry;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialFlags;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.OreProperty;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.Util;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class OreByProductRecipe {

    private static final List<TagPrefix> ORES = new ArrayList<>();

    public static void addOreByProductPrefix(TagPrefix TagPrefix) {
        if (!ORES.contains(TagPrefix)) {
            ORES.add(TagPrefix);
        }
    }

    private static final ImmutableList<TagPrefix> IN_PROCESSING_STEPS = ImmutableList.of(
            TagPrefix.crushed,
            TagPrefix.crushedPurified,
            TagPrefix.dustImpure,
            TagPrefix.dustPure,
            TagPrefix.crushedCentrifuged
    );

    private static ImmutableList<ItemStack> ALWAYS_MACHINES;

    private final Int2ObjectMap<ChanceEntry> chances = new Int2ObjectOpenHashMap<>();
    private final List<List<ItemStack>> inputs = new ArrayList<>();
    private final List<List<ItemStack>> outputs = new ArrayList<>();
    private final List<List<FluidStack>> fluidInputs = new ArrayList<>();
    private boolean hasDirectSmelt = false;
    private boolean hasChemBath = false;
    private boolean hasSeparator = false;
    private boolean hasSifter = false;
    private int currentSlot;

    public OreByProductRecipe(Material material) {
        if (ALWAYS_MACHINES == null) {
            ALWAYS_MACHINES = ImmutableList.of(
                    MetaTileEntities.MACERATOR[GTValues.LV].getStackForm(),
                    MetaTileEntities.MACERATOR[GTValues.LV].getStackForm(),
                    MetaTileEntities.CENTRIFUGE[GTValues.LV].getStackForm(),
                    MetaTileEntities.ORE_WASHER[GTValues.LV].getStackForm(),
                    MetaTileEntities.THERMAL_CENTRIFUGE[GTValues.LV].getStackForm(),
                    MetaTileEntities.MACERATOR[GTValues.LV].getStackForm(),
                    MetaTileEntities.MACERATOR[GTValues.LV].getStackForm(),
                    MetaTileEntities.CENTRIFUGE[GTValues.LV].getStackForm()
            );
        }
        OreProperty property = material.getProperty(GtMaterialProperties.ORE.get());
        int oreMultiplier = property.getOreMultiplier();
        int byproductMultiplier = property.getByProductMultiplier();
        currentSlot = 0;
        Material[] byproducts = new Material[]{
                Util.selectItemInList(0, material, property.getOreByProducts(), Material.class),
                Util.selectItemInList(1, material, property.getOreByProducts(), Material.class),
                Util.selectItemInList(2, material, property.getOreByProducts(), Material.class),
                Util.selectItemInList(3, material, property.getOreByProducts(), Material.class)
        };

        // "INPUTS"

        Pair<Material, Integer> washedIn = property.getWashedIn();
        List<Material> separatedInto = property.getSeparatedInto();

        List<ItemStack> oreStacks = new ArrayList<>();
        for (TagPrefix prefix : ORES) {
            // get all ores with the relevant oredicts instead of just the first unified ore
            oreStacks.addAll(TagUnifier.getTagValue(prefix, material).stream().map(ItemStack::new).toList());
        }
        inputs.add(oreStacks);

        // set up machines as inputs
        List<ItemStack> simpleWashers = new ArrayList<>();
        simpleWashers.add(new ItemStack(Items.CAULDRON));
        simpleWashers.add(MetaTileEntities.ORE_WASHER[GTValues.LV].getStackForm());

        if (!material.hasProperty(GtMaterialProperties.BLAST.get())) {
            addToInputs(new ItemStack(Blocks.FURNACE));
            hasDirectSmelt = true;
        } else {
            addToInputs(ItemStack.EMPTY);
        }

        for (ItemStack stack : ALWAYS_MACHINES) {
            addToInputs(stack);
        }
        // same amount of lines as a for loop :trol:
        inputs.add(simpleWashers);
        inputs.add(simpleWashers);
        inputs.add(simpleWashers);

        if (washedIn != null && washedIn.getKey() != null) {
            hasChemBath = true;
            addToInputs(MetaTileEntities.CHEMICAL_BATH[GTValues.LV].getStackForm());
        } else {
            addToInputs(ItemStack.EMPTY);
        }
        if (separatedInto != null && !separatedInto.isEmpty()) {
            hasSeparator = true;
            addToInputs(MetaTileEntities.ELECTROMAGNETIC_SEPARATOR[GTValues.LV].getStackForm());
        } else {
            addToInputs(ItemStack.EMPTY);
        }
        if (material.hasProperty(GtMaterialProperties.GEM.get())) {
            hasSifter = true;
            addToInputs(MetaTileEntities.SIFTER[GTValues.LV].getStackForm());
        } else {
            addToInputs(ItemStack.EMPTY);
        }

        // add prefixes that should count as inputs to input lists (they will not be displayed in actual page)
        for (TagPrefix prefix : IN_PROCESSING_STEPS) {
            List<ItemStack> tempList = new ArrayList<>();
            tempList.add(new ItemStack(TagUnifier.get(prefix, material)));
            inputs.add(tempList);
        }

        // total number of inputs added
        currentSlot += 21;

        // BASIC PROCESSING

        // begin lots of logic duplication from OreRecipeHandler
        // direct smelt
        if (hasDirectSmelt) {
            ItemStack smeltingResult;
            Material smeltingMaterial = property.getDirectSmeltResult() == null ? material : property.getDirectSmeltResult();
            if (smeltingMaterial.hasProperty(GtMaterialProperties.INGOT.get())) {
                smeltingResult = new ItemStack(TagUnifier.get(TagPrefix.ingot, smeltingMaterial), 1);
            } else if (smeltingMaterial.hasProperty(GtMaterialProperties.GEM.get())) {
                smeltingResult = new ItemStack(TagUnifier.get(TagPrefix.gem, smeltingMaterial), 1);
            } else {
                smeltingResult = new ItemStack(TagUnifier.get(TagPrefix.dust, smeltingMaterial), 1);
            }
            smeltingResult.setCount(smeltingResult.getCount() * oreMultiplier);
            addToOutputs(smeltingResult);
        } else {
            addEmptyOutputs(1);
        }

        // macerate ore -> crushed
        addToOutputs(material, TagPrefix.crushed, 2 * oreMultiplier);
        if (TagUnifier.get(TagPrefix.gem, byproducts[0]) != null) {
            addToOutputs(byproducts[0], TagPrefix.gem, 1);
        } else {
            addToOutputs(byproducts[0], TagPrefix.dust, 1);
        }
        addChance(1400, 850);

        // macerate crushed -> impure
        addToOutputs(material, TagPrefix.dustImpure, 1);
        addToOutputs(byproducts[0], TagPrefix.dust, byproductMultiplier);
        addChance(1400, 850);

        // centrifuge impure -> dust
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(byproducts[0], TagPrefix.dustTiny, 1);

        // ore wash crushed -> crushed purified
        addToOutputs(material, TagPrefix.crushedPurified, 1);
        addToOutputs(byproducts[0], TagPrefix.dustTiny, 3);
        List<FluidStack> fluidStacks = new ArrayList<>();
        fluidStacks.add(GtMaterials.Water.get().getFluid(1000));
        fluidStacks.add(GtMaterials.DistilledWater.get().getFluid(100));
        fluidInputs.add(fluidStacks);

        // TC crushed/crushed purified -> centrifuged
        addToOutputs(material, TagPrefix.crushedCentrifuged, 1);
        addToOutputs(byproducts[1], TagPrefix.dustTiny, byproductMultiplier * 3);

        // macerate centrifuged -> dust
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(byproducts[2], TagPrefix.dust, 1);
        addChance(1400, 850);

        // macerate crushed purified -> purified
        addToOutputs(material, TagPrefix.dustPure, 1);
        addToOutputs(byproducts[1], TagPrefix.dust, 1);
        addChance(1400, 850);

        // centrifuge purified -> dust
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(byproducts[1], TagPrefix.dustTiny, 1);

        // cauldron/simple washer
        addToOutputs(material, TagPrefix.crushed, 1);
        addToOutputs(material, TagPrefix.crushedPurified, 1);
        addToOutputs(material, TagPrefix.dustImpure, 1);
        addToOutputs(material, TagPrefix.dust, 1);
        addToOutputs(material, TagPrefix.dustPure, 1);
        addToOutputs(material, TagPrefix.dust, 1);

        // ADVANCED PROCESSING

        // chem bath
        if (hasChemBath) {
            addToOutputs(material, TagPrefix.crushedPurified, 1);
            addToOutputs(byproducts[3], TagPrefix.dust, byproductMultiplier);
            addChance(7000, 580);
            List<FluidStack> washedFluid = new ArrayList<>();
            washedFluid.add(washedIn.getKey().getFluid(washedIn.getValue()));
            fluidInputs.add(washedFluid);
        } else {
            addEmptyOutputs(2);
            List<FluidStack> washedFluid = new ArrayList<>();
            fluidInputs.add(washedFluid);
        }

        // electromagnetic separator
        if (hasSeparator) {
            ItemStack separatedStack1 = new ItemStack(TagUnifier.get(TagPrefix.dustSmall, separatedInto.get(0)), 1);
            TagPrefix prefix = (separatedInto.get(separatedInto.size() - 1).getBlastTemperature() == 0 && separatedInto.get(separatedInto.size() - 1).hasProperty(GtMaterialProperties.INGOT.get())
                    ? TagPrefix.nugget : TagPrefix.dustSmall);
            ItemStack separatedStack2 = TagUnifier.get(prefix, separatedInto.get(separatedInto.size() - 1), prefix == TagPrefix.nugget ? 2 : 1);

            addToOutputs(material, TagPrefix.dust, 1);
            addToOutputs(separatedStack1);
            addChance(4000, 850);
            addToOutputs(separatedStack2);
            addChance(2000, 600);
        } else {
            addEmptyOutputs(3);
        }

        // sifter
        if (hasSifter) {
            boolean highOutput = material.hasFlag(GtMaterialFlags.HIGH_SIFTER_OUTPUT.get());
            ItemStack flawedStack = new ItemStack(TagUnifier.get(TagPrefix.gemFlawed, material), 1);
            ItemStack chippedStack = new ItemStack(TagUnifier.get(TagPrefix.gemChipped, material), 1);

            addToOutputs(material, TagPrefix.gemExquisite, 1);
            addGemChance(300, 100, 500, 150, highOutput);
            addToOutputs(material, TagPrefix.gemFlawless, 1);
            addGemChance(1000, 150, 1500, 200, highOutput);
            addToOutputs(material, TagPrefix.gem, 1);
            addGemChance(3500, 500, 5000, 1000, highOutput);
            addToOutputs(material, TagPrefix.dustPure, 1);
            addGemChance(5000, 750, 2500, 500, highOutput);

            if (!flawedStack.isEmpty()) {
                addToOutputs(flawedStack);
                addGemChance(2500, 300, 2000, 500, highOutput);
            } else {
                addEmptyOutputs(1);
            }
            if (!chippedStack.isEmpty()) {
                addToOutputs(chippedStack);
                addGemChance(3500, 400, 3000, 350, highOutput);
            } else {
                addEmptyOutputs(1);
            }
        } else {
            addEmptyOutputs(6);
        }
    }

    public List<List<ItemStack>> getInputs() {
        return inputs;
    }

    public List<List<FluidStack>> getFluidInputs() {
        return fluidInputs;
    }

    public List<List<ItemStack>> getOutputs() {
        return outputs;
    }

    public void addTooltip(int slotIndex, boolean input, Object ingredient, List<Component> tooltip) {
        if (chances.containsKey(slotIndex)) {
            ChanceEntry entry = chances.get(slotIndex);
            double chance = entry.chance() / 100.0;
            double boost = entry.boostPerTier() / 100.0;
            tooltip.add(Component.translatable("gregtech.recipe.chance", chance, boost));
        }
    }

    public ChanceEntry getChance(int slot) {
        return chances.get(slot);
    }

    public boolean hasSifter() {
        return hasSifter;
    }

    public boolean hasSeparator() {
        return hasSeparator;
    }

    public boolean hasChemBath() {
        return hasChemBath;
    }

    public boolean hasDirectSmelt() {
        return hasDirectSmelt;
    }

    private void addToOutputs(Material material, TagPrefix prefix, int size) {
        addToOutputs(TagUnifier.get(prefix, material, size));
    }

    private void addToOutputs(ItemStack stack) {
        List<ItemStack> tempList = new ArrayList<>();
        tempList.add(stack);
        outputs.add(tempList);
        currentSlot++;
    }

    private void addEmptyOutputs(int amount) {
        for (int i = 0; i < amount; i++) {
            addToOutputs(ItemStack.EMPTY);
        }
    }

    private void addToInputs(ItemStack stack) {
        List<ItemStack> tempList = new ArrayList<>();
        tempList.add(stack);
        inputs.add(tempList);
    }

    private void addChance(int base, int tier) {
        // this is solely for the chance overlay and tooltip, neither of which care about the ItemStack
        chances.put(currentSlot - 1, new ChanceEntry(ItemStack.EMPTY, base, tier));
    }

    // make the code less :weary:
    private void addGemChance(int baseLow, int tierLow, int baseHigh, int tierHigh, boolean high) {
        if (high) {
            addChance(baseHigh, tierHigh);
        } else {
            addChance(baseLow, tierLow);
        }
    }
}

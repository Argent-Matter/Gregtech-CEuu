package net.nemezanevem.gregtech.api.recipe;

import codechicken.lib.util.ServerUtils;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.resources.TextureArea;
import net.nemezanevem.gregtech.api.gui.widgets.ProgressWidget;
import net.nemezanevem.gregtech.api.gui.widgets.RecipeProgressWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.gui.widgets.TankWidget;
import net.nemezanevem.gregtech.api.recipe.ingredient.ExtendedIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.FluidIngredient;
import net.nemezanevem.gregtech.api.recipe.ingredient.IntCircuitIngredient;
import net.nemezanevem.gregtech.api.recipe.type.*;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.api.util.ValidationResult;
import net.nemezanevem.gregtech.api.util.ValidationResult.EnumValidationResult;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.datagen.recipe.builder.GTRecipeBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GTRecipeTypeImpl<R extends GTRecipe> implements GTRecipeType<R> {

    public static final Map<GTRecipeType<?>, GTRecipeBuilder<?>> TYPES_TO_BUILDERS = new HashMap<>();

    private static final Comparator<GTRecipe> RECIPE_DURATION_THEN_EU = Comparator.comparingInt(GTRecipe::getDuration)
            .thenComparingInt(GTRecipe::getEUt)
            .thenComparing(GTRecipe::hashCode);

    public static final IChanceFunction DEFAULT_CHANCE_FUNCTION = (baseChance, boostPerTier, baseTier, machineTier) -> {
        int tierDiff = machineTier - baseTier;
        if (tierDiff <= 0) return baseChance; // equal or invalid tiers do not boost at all
        if (baseTier == GTValues.ULV) tierDiff--; // LV does not boost over ULV
        return baseChance + (boostPerTier * tierDiff);
    };

    public IChanceFunction chanceFunction = DEFAULT_CHANCE_FUNCTION;

    public final ResourceLocation id;

    private final R recipeBuilderSample;
    private final int minInputs, maxInputs;
    private final int minOutputs, maxOutputs;
    private final int minFluidInputs, maxFluidInputs;
    private final int minFluidOutputs, maxFluidOutputs;
    protected final Byte2ObjectMap<TextureArea> slotOverlays;
    protected TextureArea specialTexture;
    protected int[] specialTexturePosition;
    protected TextureArea progressBarTexture;
    protected ProgressWidget.MoveType moveType;
    public final boolean isHidden;

    private final Branch lookup = new Branch();
    private boolean hastaggedInputs = false;
    private boolean hasNBTMatcherInputs = false;
    private static final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> ingredientRoot = new WeakHashMap<>();
    private final WeakHashMap<AbstractMapIngredient, WeakReference<AbstractMapIngredient>> fluidIngredientRoot = new WeakHashMap<>();

    protected SoundEvent sound;
    private GTRecipeType<?> smallRecipeMap;

    public GTRecipeTypeImpl(ResourceLocation id, int minInputs, int maxInputs, int minOutputs, int maxOutputs, int minFluidInputs, int maxFluidInputs, int minFluidOutputs, int maxFluidOutputs, R defaultRecipe, boolean isHidden) {
        this.id = id;
        this.slotOverlays = new Byte2ObjectOpenHashMap<>();
        this.progressBarTexture = GuiTextures.PROGRESS_BAR_ARROW;
        this.moveType = ProgressWidget.MoveType.HORIZONTAL;

        this.minInputs = minInputs;
        this.minFluidInputs = minFluidInputs;
        this.minOutputs = minOutputs;
        this.minFluidOutputs = minFluidOutputs;

        this.maxInputs = maxInputs;
        this.maxFluidInputs = maxFluidInputs;
        this.maxOutputs = maxOutputs;
        this.maxFluidOutputs = maxFluidOutputs;

        this.isHidden = isHidden;
        defaultRecipe.setType(this);
        this.recipeBuilderSample = defaultRecipe;
        GtRecipeTypes.toRegister.put(this.id, this);

        TYPES_TO_BUILDERS.put(this, new GTRecipeBuilder<>(this));
    }

    public static List<GTRecipeType<?>> getRecipeMaps() {
        return ImmutableList.copyOf(GtRecipeTypes.toRegister.values());
    }

    public static GTRecipeType<?> getById(ResourceLocation id) {
        return GtRecipeTypes.toRegister.get(id);
    }

    public IChanceFunction getChanceFunction() {
        return chanceFunction;
    }

    public static boolean isFoundInvalidRecipe() {
        return foundInvalidRecipe;
    }

    public static void setFoundInvalidRecipe(boolean foundInvalidRecipe) {
        GTRecipeTypeImpl.foundInvalidRecipe |= foundInvalidRecipe;
        TagPrefix currentTagPrefix = TagPrefix.getCurrentProcessingPrefix();
        if (currentTagPrefix != null) {
            Material currentMaterial = TagPrefix.getCurrentMaterial();
            GregTech.LOGGER.error("Error happened during processing ore registration of prefix {} and material {}. " + "Seems like cross-mod compatibility issue. Report to GTCEu github.", currentTagPrefix, currentMaterial);
        }
    }

    public GTRecipeType<R> setProgressBar(TextureArea progressBar, ProgressWidget.MoveType moveType) {
        this.progressBarTexture = progressBar;
        this.moveType = moveType;
        return this;
    }

    public GTRecipeType<R> setSlotOverlay(boolean isOutput, boolean isFluid, TextureArea slotOverlay) {
        return this.setSlotOverlay(isOutput, isFluid, false, slotOverlay).setSlotOverlay(isOutput, isFluid, true, slotOverlay);
    }

    public GTRecipeTypeImpl<R> setSlotOverlay(boolean isOutput, boolean isFluid, boolean isLast, TextureArea slotOverlay) {
        this.slotOverlays.put((byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0)), slotOverlay);
        return this;
    }

    public GTRecipeType<R> setSound(SoundEvent sound) {
        this.sound = sound;
        return this;
    }

    public GTRecipeType<R> setChanceFunction(IChanceFunction function) {
        chanceFunction = function;
        return this;
    }

    public GTRecipeType<R> setSmallRecipeMap(GTRecipeType<?> recipeMap) {
        this.smallRecipeMap = recipeMap;
        return this;
    }

    public GTRecipeType<?> getSmallRecipeMap() {
        return smallRecipeMap;
    }

    private static boolean foundInvalidRecipe = false;

    //internal usage only, use buildAndRegister()
    public void addRecipe(ValidationResult<GTRecipe> validationResult) {
        validationResult = postValidateRecipe(validationResult);
        switch (validationResult.getType()) {
            case SKIP:
                return;
            case INVALID:
                setFoundInvalidRecipe(true);
                return;
        }
        GTRecipe recipe = validationResult.getResult();

        compileRecipe(recipe);

    }

    public void compileRecipe(GTRecipe recipe) {
        if (recipe == null) {
            return;
        }
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        recurseIngredientTreeAdd(recipe, items, lookup, 0, 0);
    }

    public boolean removeRecipe(GTRecipe recipe) {
        List<List<AbstractMapIngredient>> items = fromRecipe(recipe);
        if (recurseIngredientTreeRemove(recipe, items, lookup, 0) != null) {
            return true;
        }
        return false;
    }

    protected ValidationResult<GTRecipe> postValidateRecipe(ValidationResult<GTRecipe> validationResult) {
        EnumValidationResult recipeStatus = validationResult.getType();
        GTRecipe recipe = validationResult.getResult();
        if (!Util.isBetweenInclusive(getMinInputs(), getMaxInputs(), recipe.getInputs().size())) {
            GregTech.LOGGER.error("Invalid amount of recipe inputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getInputs().size(), getMinInputs(), getMaxInputs());
            GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException("Invalid number of Inputs"));
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!Util.isBetweenInclusive(getMinOutputs(), getMaxOutputs(), recipe.getOutputs().size() + recipe.getChancedOutputs().size())) {
            GregTech.LOGGER.error("Invalid amount of recipe outputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getOutputs().size() + recipe.getChancedOutputs().size(), getMinOutputs(), getMaxOutputs());
            GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException("Invalid number of Outputs"));
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!Util.isBetweenInclusive(getMinFluidInputs(), getMaxFluidInputs(), recipe.getFluidInputs().size())) {
            GregTech.LOGGER.error("Invalid amount of recipe fluid inputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getFluidInputs().size(), getMinFluidInputs(), getMaxFluidInputs());
            GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Inputs"));
            recipeStatus = EnumValidationResult.INVALID;
        }
        if (!Util.isBetweenInclusive(getMinFluidOutputs(), getMaxFluidOutputs(), recipe.getFluidOutputs().size())) {
            GregTech.LOGGER.error("Invalid amount of recipe fluid outputs. Actual: {}. Should be between {} and {} inclusive.", recipe.getFluidOutputs().size(), getMinFluidOutputs(), getMaxFluidOutputs());
            GregTech.LOGGER.error("Stacktrace:", new IllegalArgumentException("Invalid number of Fluid Outputs"));
            recipeStatus = EnumValidationResult.INVALID;
        }
        return ValidationResult.newResult(recipeStatus, recipe);
    }

    @Nullable
    public R findRecipe(long voltage, IItemHandlerModifiable inputs, IMultipleTankHandler fluidInputs, int outputFluidTankCapacity) {
        return this.findRecipe(voltage, Util.itemHandlerToList(inputs), Util.fluidHandlerToList(fluidInputs), outputFluidTankCapacity);
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage                 Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs                  the Item Inputs
     * @param fluidInputs             the Fluid Inputs
     * @param outputFluidTankCapacity minimal capacity of output fluid tank, used for fluid canner recipes for example
     * @return the Recipe it has found or null for no matching Recipe
     */
    @Nullable
    public R findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity) {
        return findRecipe(voltage, inputs, fluidInputs, outputFluidTankCapacity, false);
    }

    /**
     * Finds a Recipe matching the Fluid and/or ItemStack Inputs.
     *
     * @param voltage                 Voltage of the Machine or Long.MAX_VALUE if it has no Voltage
     * @param inputs                  the Item Inputs
     * @param fluidInputs             the Fluid Inputs
     * @param outputFluidTankCapacity minimal capacity of output fluid tank, used for fluid canner recipes for example
     * @param exactVoltage            should require exact voltage matching on recipe. used by craftweaker
     * @return the Recipe it has found or null for no matching Recipe
     */

    @Nullable
    public R findRecipe(long voltage, List<ItemStack> inputs, List<FluidStack> fluidInputs, int outputFluidTankCapacity, boolean exactVoltage) {
        return find(inputs.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList()), fluidInputs.stream().filter(f -> !f.isEmpty()).collect(Collectors.toList()), recipe -> {
            if (exactVoltage && recipe.getEUt() != voltage) {
                return false;
            }
            return recipe.getEUt() <= voltage && recipe.matches(false, inputs, fluidInputs);
        });
    }

    @Nullable
    public R find(@Nonnull List<ItemStack> items, @Nonnull List<FluidStack> fluids, @Nonnull Predicate<GTRecipe> canHandle) {
        // First, check if items and fluids are valid.
        if (items.size() == Integer.MAX_VALUE || fluids.size() == Integer.MAX_VALUE) {
            return null;
        }
        if (items.size() == 0 && fluids.size() == 0) {
            return null;
        }

        Optional<R> recipeOptional = ServerUtils.getServer().getRecipeManager().getAllRecipesFor(this).stream().filter(recipe -> recipe.matches(false, items, fluids) && canHandle.test(recipe)).distinct().findFirst();
        return recipeOptional.orElse(null);
        /*List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.size() + fluids.size());
        if (items.size() > 0) {
            buildFromItemStacks(list, uniqueItems(items));
        }
        if (fluids.size() > 0) {
            List<FluidStack> stack = new ObjectArrayList<>(fluids.size());
            for (FluidStack f : fluids) {
                if (f == null || f.getAmount() == 0) {
                    continue;
                }
                stack.add(f);
            }
            if (stack.size() > 0) {
                buildFromFluidStacks(list, stack);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        return recurseIngredientTreeFindRecipe(list, lookup, canHandle);
        */
    }

    /**
     * Builds a list of unique ItemStacks from the given Collection of ItemStacks.
     * Used to reduce the number inputs, if for example there is more than one of the same input,
     * pack them into one.
     * This uses a strict comparison, so it will not pack the same item with different NBT tags,
     * to allow the presence of, for example, more than one configured circuit in the input.
     *
     * @param inputs The Collection of GTRecipeInputs.
     * @return an array of unique itemstacks.
     */

    public static ItemStack[] uniqueItems(Collection<ItemStack> inputs) {
        int index = 0;
        ItemStack[] uniqueItems = new ItemStack[inputs.size()];
        main:
        for (ItemStack input : inputs) {
            if (input.isEmpty()) {
                continue;
            }
            if (index > 0) {
                for (int i = 0; i < uniqueItems.length; i++) {
                    ItemStack unique = uniqueItems[i];
                    if (unique == null) break;
                    else if (ItemStack.matches(input, unique) && ItemStack.tagMatches(input, unique)) {
                        continue main;
                    }
                }
            }
            uniqueItems[index++] = input;
        }
        if (index == uniqueItems.length) {
            return uniqueItems;
        }
        ItemStack[] retUniqueItems = new ItemStack[index];
        System.arraycopy(uniqueItems, 0, retUniqueItems, 0, index);
        return retUniqueItems;
    }

    /**
     * Builds a list of unique inputs from the given list GTRecipeInputs.
     * Used to reduce the number inputs, if for example there is more than one of the same input,
     * pack them into one.
     *
     * @param input The list of GTRecipeInputs.
     * @return The list of unique inputs.
     */

    public static List<ExtendedIngredient> uniqueIngredientsList(List<ExtendedIngredient> input) {
        List<ExtendedIngredient> list = new ObjectArrayList<>(input.size());
        for (ExtendedIngredient item : input) {
            boolean isEqual = false;
            for (ExtendedIngredient obj : list) {
                if (item.equals(obj)) {
                    isEqual = true;
                    break;
                }
            }
            if (isEqual) continue;
            if (item instanceof IntCircuitIngredient) {
                list.add(0, item);
            } else {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * Recursively finds a recipe, top level. call this to find a recipe
     *
     * @param ingredients the ingredients part
     * @param branchRoot  the root branch to search from.
     * @return a recipe
     */
    private GTRecipe recurseIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchRoot, @Nonnull Predicate<GTRecipe> canHandle) {
        // Try each ingredient as a starting point, adding it to the skiplist.
        for (int i = 0; i < ingredients.size(); i++) {
            GTRecipe r = recurseIngredientTreeFindRecipe(ingredients, branchRoot, canHandle, i, 0, (1L << i));
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    /**
     * Recursively finds a recipe
     *
     * @param ingredients the ingredients part
     * @param branchMap   the current branch of the tree
     * @param canHandle   predicate to test found recipe.
     * @param index       the index of the wrapper to get
     * @param count       how deep we are in recursion, < ingredients.length
     * @param skip        bitmap of ingredients to skip, i.e. which ingredients are used in the
     *                    recursion.
     * @return a recipe
     */
    private GTRecipe recurseIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, @Nonnull Predicate<GTRecipe> canHandle, int index, int count, long skip) {
        if (count == ingredients.size()) {
            return null;
        }
        List<AbstractMapIngredient> wr = ingredients.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient t : wr) {
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap;
            if (t.isSpecialIngredient()) {
                targetMap = branchMap.getSpecialNodes();
            } else {
                targetMap = branchMap.getNodes();
            }

            Either<GTRecipe, Branch> result = targetMap.get(t);
            if (result != null) {
                // Either return recipe or continue branch.
                GTRecipe r = result.map(recipe -> canHandle.test(recipe) ? recipe : null, right -> diveIngredientTreeFindRecipe(ingredients, right, canHandle, index, count, skip));
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    private GTRecipe diveIngredientTreeFindRecipe(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch map, Predicate<GTRecipe> canHandle, int index, int count, long skip) {
        // We loop around ingredients.size() if we reach the end.
        int counter = (index + 1) % ingredients.size();
        while (counter != index) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << counter)) == 0)) {
                // Recursive call.
                GTRecipe found = recurseIngredientTreeFindRecipe(ingredients, map, canHandle, counter, count + 1, skip | (1L << counter));
                if (found != null) {
                    return found;
                }
            }
            counter = (counter + 1) % ingredients.size();
        }
        return null;
    }

    /**
     * Exhaustively gathers all recipes that can be crafted with the given ingredients, into a Set.
     *
     * @param items  the ingredients, in the form of a List of ItemStack. Usually the inputs of a Recipe
     * @param fluids the ingredients, in the form of a List of FluidStack. Usually the inputs of a Recipe
     * @return a Set of recipes that can be crafted with the given ingredients
     */

    @Nullable
    public Set<GTRecipe> findRecipeCollisions(List<ItemStack> items, List<FluidStack> fluids) {
        // First, check if items and fluids are valid.
        if (items.size() == Integer.MAX_VALUE || fluids.size() == Integer.MAX_VALUE) {
            return null;
        }
        if (items.size() == 0 && fluids.size() == 0) {
            return null;
        }
        // Filter out empty fluids.

        // Build input.
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>(items.size() + fluids.size());
        if (items.size() > 0) {
            buildFromItemStacks(list, uniqueItems(items));
        }
        if (fluids.size() > 0) {
            List<FluidStack> stack = new ObjectArrayList<>(fluids.size());
            for (FluidStack f : fluids) {
                if (f == null || f.getAmount() == 0) {
                    continue;
                }
                stack.add(f);
            }
            if (stack.size() > 0) {
                buildFromFluidStacks(list, stack);
            }
        }
        if (list.size() == 0) {
            return null;
        }
        Set<GTRecipe> collidingRecipes = new HashSet<>();
        return recurseIngredientTreeFindRecipeCollisions(list, lookup, collidingRecipes);
    }

    private Set<GTRecipe> recurseIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchRoot, Set<GTRecipe> collidingRecipes) {
        // Try each ingredient as a starting point, adding it to the skiplist.
        for (int i = 0; i < ingredients.size(); i++) {
            recurseIngredientTreeFindRecipeCollisions(ingredients, branchRoot, i, 0, (1L << i), collidingRecipes);
        }
        return collidingRecipes;
    }

    private GTRecipe recurseIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int index, int count, long skip, Set<GTRecipe> collidingRecipes) {
        if (count == ingredients.size()) {
            return null;
        }
        List<AbstractMapIngredient> wr = ingredients.get(index);
        // Iterate over current level of nodes.
        for (AbstractMapIngredient t : wr) {
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap;
            if (t.isSpecialIngredient()) {
                targetMap = branchMap.getSpecialNodes();
            } else {
                targetMap = branchMap.getNodes();
            }

            Either<GTRecipe, Branch> result = targetMap.get(t);
            if (result != null) {
                // Either return recipe or continue branch.
                GTRecipe r = result.map(recipe -> recipe, right -> diveIngredientTreeFindRecipeCollisions(ingredients, right, index, count, skip, collidingRecipes));
                if (r != null) {
                    collidingRecipes.add(r);
                }
            }
        }
        return null;
    }

    private GTRecipe diveIngredientTreeFindRecipeCollisions(@Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch map, int index, int count, long skip, Set<GTRecipe> collidingRecipes) {
        // We loop around ingredients.size() if we reach the end.
        int counter = (index + 1) % ingredients.size();
        while (counter != index) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip & (1L << counter)) == 0)) {
                // Recursive call.
                GTRecipe r = recurseIngredientTreeFindRecipeCollisions(ingredients, map, counter, count + 1, skip | (1L << counter), collidingRecipes);
                if (r != null) {
                    return r;
                }
            }
            counter = (counter + 1) % ingredients.size();
        }
        return null;
    }

    public ModularUI.Builder createJeiUITemplate(IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(200, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplate(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        addInventorySlotGroup(builder, exportItems, exportFluids, true, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    //this DOES NOT include machine control widgets or binds player inventory
    public ModularUI.Builder createUITemplateNoOutputs(DoubleSupplier progressSupplier, IItemHandlerModifiable importItems, IItemHandlerModifiable exportItems, FluidTankList importFluids, FluidTankList exportFluids, int yOffset) {
        ModularUI.Builder builder = ModularUI.defaultBuilder(yOffset);
        builder.widget(new RecipeProgressWidget(progressSupplier, 78, 23 + yOffset, 20, 20, progressBarTexture, moveType, this));
        addInventorySlotGroup(builder, importItems, importFluids, false, yOffset);
        if (this.specialTexture != null && this.specialTexturePosition != null) addSpecialTexture(builder);
        return builder;
    }

    protected void addInventorySlotGroup(ModularUI.Builder builder, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isOutputs, int yOffset) {
        int itemInputsCount = itemHandler.getSlots();
        int fluidInputsCount = fluidHandler.getTanks();
        boolean invertFluids = false;
        if (itemInputsCount == 0) {
            int tmp = itemInputsCount;
            itemInputsCount = fluidInputsCount;
            fluidInputsCount = tmp;
            invertFluids = true;
        }
        int[] inputSlotGrid = determineSlotsGrid(itemInputsCount);
        int itemSlotsToLeft = inputSlotGrid[0];
        int itemSlotsToDown = inputSlotGrid[1];
        int startInputsX = isOutputs ? 106 : 70 - itemSlotsToLeft * 18;
        int startInputsY = 33 - (int) (itemSlotsToDown / 2.0 * 18) + yOffset;
        boolean wasGroup = itemHandler.getSlots() + fluidHandler.getTanks() == 12;
        if (wasGroup) startInputsY -= 9;
        else if (itemHandler.getSlots() >= 6 && fluidHandler.getTanks() >= 2 && !isOutputs) startInputsY -= 9;
        for (int i = 0; i < itemSlotsToDown; i++) {
            for (int j = 0; j < itemSlotsToLeft; j++) {
                int slotIndex = i * itemSlotsToLeft + j;
                if (slotIndex >= itemInputsCount) break;
                int x = startInputsX + 18 * j;
                int y = startInputsY + 18 * i;
                addSlot(builder, x, y, slotIndex, itemHandler, fluidHandler, invertFluids, isOutputs);
            }
        }
        if (wasGroup) startInputsY += 2;
        if (fluidInputsCount > 0 || invertFluids) {
            if (itemSlotsToDown >= fluidInputsCount && itemSlotsToLeft < 3) {
                int startSpecX = isOutputs ? startInputsX + itemSlotsToLeft * 18 : startInputsX - 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int y = startInputsY + 18 * i;
                    addSlot(builder, startSpecX, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            } else {
                int startSpecY = startInputsY + itemSlotsToDown * 18;
                for (int i = 0; i < fluidInputsCount; i++) {
                    int x = isOutputs ? startInputsX + 18 * (i % 3) : startInputsX + itemSlotsToLeft * 18 - 18 - 18 * (i % 3);
                    int y = startSpecY + (i / 3) * 18;
                    addSlot(builder, x, y, i, itemHandler, fluidHandler, !invertFluids, isOutputs);
                }
            }
        }
    }

    protected void addSlot(ModularUI.Builder builder, int x, int y, int slotIndex, IItemHandlerModifiable itemHandler, FluidTankList fluidHandler, boolean isFluid, boolean isOutputs) {
        if (!isFluid) {
            builder.widget(new SlotWidget(itemHandler, slotIndex, x, y, true, !isOutputs).setBackgroundTexture(getOverlaysForSlot(isOutputs, false, slotIndex == itemHandler.getSlots() - 1)));
        } else {
            builder.widget(new TankWidget(fluidHandler.getTankAt(slotIndex), x, y, 18, 18).setAlwaysShowFull(true).setBackgroundTexture(getOverlaysForSlot(isOutputs, true, slotIndex == fluidHandler.getTanks() - 1)).setContainerClicking(true, !isOutputs));
        }
    }

    protected TextureArea[] getOverlaysForSlot(boolean isOutput, boolean isFluid, boolean isLast) {
        TextureArea base = isFluid ? GuiTextures.FLUID_SLOT : GuiTextures.SLOT;
        byte overlayKey = (byte) ((isOutput ? 2 : 0) + (isFluid ? 1 : 0) + (isLast ? 4 : 0));
        if (slotOverlays.containsKey(overlayKey)) {
            return new TextureArea[]{base, slotOverlays.get(overlayKey)};
        }
        return new TextureArea[]{base};
    }

    protected static int[] determineSlotsGrid(int itemInputsCount) {
        int itemSlotsToLeft;
        int itemSlotsToDown;
        double sqrt = Math.sqrt(itemInputsCount);
        //if the number of input has an integer root
        //return it.
        if (sqrt % 1 == 0) {
            itemSlotsToLeft = itemSlotsToDown = (int) sqrt;
        } else if (itemInputsCount == 3) {
            itemSlotsToLeft = 3;
            itemSlotsToDown = 1;
        } else {
            //if we couldn't fit all into a perfect square,
            //increase the amount of slots to the left
            itemSlotsToLeft = (int) Math.ceil(sqrt);
            itemSlotsToDown = itemSlotsToLeft - 1;
            //if we still can't fit all the slots in a grid,
            //increase the amount of slots on the bottom
            if (itemInputsCount > itemSlotsToLeft * itemSlotsToDown) {
                itemSlotsToDown = itemSlotsToLeft;
            }
        }
        return new int[]{itemSlotsToLeft, itemSlotsToDown};
    }

    /**
     * Adds a recipe to the map. (recursive part)
     *
     * @param recipe      the recipe to add.
     * @param ingredients list of input ingredients.
     * @param branchMap   the current branch in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many added already.
     */
    boolean recurseIngredientTreeAdd(@Nonnull GTRecipe recipe, @Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int index, int count) {
        if (count >= ingredients.size()) return true;
        if (index >= ingredients.size()) {
            throw new RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen");
        }
        // Loop through NUMBER_OF_INGREDIENTS times.
        List<AbstractMapIngredient> current = ingredients.get(index);
        Either<GTRecipe, Branch> r;
        final Branch branchRight = new Branch();
        for (AbstractMapIngredient obj : current) {
            Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap;
            if (obj.isSpecialIngredient()) {
                targetMap = branchMap.getSpecialNodes();
            } else {
                targetMap = branchMap.getNodes();
            }

            // Either add the recipe or create a branch.
            r = targetMap.compute(obj, (k, v) -> {
                if (count == ingredients.size() - 1) {
                    if (v != null) {
                        if (v.left().isPresent() && v.left().get() == recipe) {
                            return v;
                        } else {
                            if (ConfigHolder.misc.debug || GTValues.isDeobfEnvironment()) {
                                GregTech.LOGGER.warn("Recipe duplicate or conflict found in RecipeMap {} and was not added. See next lines for details", this.id);

                                GregTech.LOGGER.warn("Attempted to add Recipe: {}", recipe.toString());

                                if (v.left().isPresent()) {
                                    GregTech.LOGGER.warn("Which conflicts with: {}", v.left().get().toString());
                                } else {
                                    GregTech.LOGGER.warn("Could not find exact duplicate/conflict.");
                                }
                            }
                        }
                    } else {
                        v = Either.left(recipe);
                    }
                    return v;
                } else if (v == null) {
                    v = Either.right(branchRight);
                }
                return v;
            });

            if (r.right().map(m -> !recurseIngredientTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size(), count + 1)).orElse(false)) {
                current.forEach(i -> {
                    if (count == ingredients.size() - 1) {
                        targetMap.remove(obj);
                    } else {
                        if (targetMap.get(obj).right().isPresent()) {
                            Branch branch = targetMap.get(obj).right().get();
                            if (branch.isEmptyBranch()) {
                                targetMap.remove(obj);
                            }
                        }
                    }
                });
                return false;
            }
        }
        return true;
    }

    protected void buildFromRecipeFluids(List<List<AbstractMapIngredient>> builder, List<FluidIngredient> fluidInputs) {
        for (FluidIngredient fluidInput : fluidInputs) {
            AbstractMapIngredient ingredient;
            ingredient = new MapFluidIngredient(fluidInput);
            WeakReference<AbstractMapIngredient> cached = fluidIngredientRoot.get(ingredient);
            if (cached != null && cached.get() != null) {
                builder.add(Collections.singletonList(cached.get()));
            } else {
                fluidIngredientRoot.put(ingredient, new WeakReference<>(ingredient));
                builder.add(Collections.singletonList(ingredient));
            }
        }
    }

    protected void buildFromFluidStacks(List<List<AbstractMapIngredient>> builder, List<FluidStack> ingredients) {
        for (FluidStack t : ingredients) {
            builder.add(Collections.singletonList(new MapFluidIngredient(t)));
        }
    }

    protected List<List<AbstractMapIngredient>> fromRecipe(GTRecipe r) {
        List<List<AbstractMapIngredient>> list = new ObjectArrayList<>((r.getIngredients().size()) + r.getFluidInputs().size());
        if (r.getInputs().size() > 0) {
            buildFromRecipeItems(list, uniqueIngredientsList(r.getInputs()));
        }
        if (r.getFluidInputs().size() > 0) {
            buildFromRecipeFluids(list, r.getFluidInputs());
        }
        return list;
    }

    protected void buildFromRecipeItems(List<List<AbstractMapIngredient>> list, List<ExtendedIngredient> ingredients) {
        for (ExtendedIngredient r : ingredients) {
            AbstractMapIngredient ingredient;
            if (r.isTag()) {
                hastaggedInputs = true;
                ingredient = new MapTagIngredient(r.getTag());

                WeakReference<AbstractMapIngredient> cached = ingredientRoot.get(ingredient);
                if (cached != null && cached.get() != null) {
                    list.add(Collections.singletonList(cached.get()));
                } else {
                    ingredientRoot.put(ingredient, new WeakReference<>(ingredient));
                    list.add(Collections.singletonList(ingredient));
                }

            } else {
                List<AbstractMapIngredient> inner = new ObjectArrayList<>(1);
                inner.addAll(MapItemStackIngredient.from(r));

                for (int i = 0; i < inner.size(); i++) {
                    AbstractMapIngredient mappedIngredient = inner.get(i);
                    WeakReference<AbstractMapIngredient> cached = ingredientRoot.get(mappedIngredient);
                    if (cached != null && cached.get() != null) {
                        inner.set(i, cached.get());
                    } else {
                        ingredientRoot.put(mappedIngredient, new WeakReference<>(mappedIngredient));
                    }
                }
                list.add(inner);
            }
        }
    }

    protected void buildFromItemStacks(List<List<AbstractMapIngredient>> list, ItemStack[] ingredients) {
        for (ItemStack stack : ingredients) {
            CompoundTag nbt = stack.getTag();

            List<AbstractMapIngredient> ls = new ObjectArrayList<>(1);
            ls.add(new MapItemStackIngredient(stack, nbt));
            if (hastaggedInputs) {
                ForgeRegistries.ITEMS.tags().getReverseTag(stack.getItem()).ifPresent(reverseTag -> {
                    reverseTag.getTagKeys().forEach(tagKey -> {
                        AbstractMapIngredient ingredient = new MapTagIngredient(tagKey);
                        ls.add(ingredient);
                    });
                });
            }
            if (ls.size() > 0) {
                list.add(ls);
            }
        }
    }

    protected GTRecipeType<R> setSpecialTexture(int x, int y, int width, int height, TextureArea area) {
        this.specialTexturePosition = new int[]{x, y, width, height};
        this.specialTexture = area;
        return this;
    }

    protected ModularUI.Builder addSpecialTexture(ModularUI.Builder builder) {
        builder.image(specialTexturePosition[0], specialTexturePosition[1], specialTexturePosition[2], specialTexturePosition[3], specialTexture);
        return builder;
    }

    public Collection<GTRecipe> getRecipeList() {
        ObjectOpenHashSet<GTRecipe> recipes = new ObjectOpenHashSet<>();
        return lookup.getRecipes(true).filter(recipes::add).sorted(RECIPE_DURATION_THEN_EU).collect(Collectors.toList());
    }

    public SoundEvent getSound() {
        return sound;
    }

    public Component getLocalizedName() {
        return Component.translatable("recipemap." + id.getPath() + ".name");
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public GTRecipeBuilder<R> recipeBuilder() {
        return (GTRecipeBuilder<R>) TYPES_TO_BUILDERS.get(this);
    }

    /**
     * Removes a recipe from the map. (recursive part)
     *
     * @param recipeToRemove the recipe to add.
     * @param ingredients    list of input ingredients.
     * @param branchMap      the current branch in the recursion.
     */
    private GTRecipe recurseIngredientTreeRemove(@Nonnull GTRecipe recipeToRemove, @Nonnull List<List<AbstractMapIngredient>> ingredients, @Nonnull Branch branchMap, int depth) {
        for (List<AbstractMapIngredient> current : ingredients) {
            for (AbstractMapIngredient obj : current) {
                Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap;
                if (obj.isSpecialIngredient()) {
                    targetMap = branchMap.getSpecialNodes();
                } else {
                    targetMap = branchMap.getNodes();
                }
                if (ingredients.size() == 0) return null;
                GTRecipe r = removeDive(recipeToRemove, ingredients.subList(1, ingredients.size()), targetMap, obj, depth);
                if (r != null) {
                    if (ingredients.size() == 1) {
                        targetMap.remove(obj);
                    } else {
                        if (targetMap.get(obj).right().isPresent()) {
                            Branch branch = targetMap.get(obj).right().get();
                            if (branch.isEmptyBranch()) {
                                targetMap.remove(obj);
                            }
                        }
                    }
                    return r;
                }
            }
        }
        return null;
    }

    private GTRecipe removeDive(GTRecipe recipeToRemove, @Nonnull List<List<AbstractMapIngredient>> ingredients, Map<AbstractMapIngredient, Either<GTRecipe, Branch>> targetMap, AbstractMapIngredient obj, int depth) {
        Either<GTRecipe, Branch> result = targetMap.get(obj);
        if (result != null) {
            // Either return recipe or continue branch.
            GTRecipe r = result.map(recipe -> recipe, right -> recurseIngredientTreeRemove(recipeToRemove, ingredients, right, depth + 1));
            if (r == recipeToRemove) {
                return r;
            }
        }
        return null;
    }

    public int getMinInputs() {
        return minInputs;
    }

    public int getMaxInputs() {
        return maxInputs;
    }

    public int getMinOutputs() {
        return minOutputs;
    }

    public int getMaxOutputs() {
        return maxOutputs;
    }

    public int getMinFluidInputs() {
        return minFluidInputs;
    }

    public int getMaxFluidInputs() {
        return maxFluidInputs;
    }

    public int getMinFluidOutputs() {
        return minFluidOutputs;
    }

    public int getMaxFluidOutputs() {
        return maxFluidOutputs;
    }

    @Override
    public String toString() {
        return "RecipeMap{" + "unlocalizedName='" + id + '\'' + '}';
    }

}

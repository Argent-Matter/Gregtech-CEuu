package net.nemezanevem.gregtech.api.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.stack.MaterialStack;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ModHandler {

    public static final boolean ERROR_ON_INVALID_RECIPE = GTValues.isDeobfEnvironment() || !ConfigHolder.misc.ignoreErrorOrInvalidRecipes;
    public static boolean hasInvalidRecipe = false;
    private static FluidStack WATER;
    private static FluidStack DISTILLED_WATER;
    private static FluidStack LAVA;
    private static FluidStack STEAM;

    private ModHandler() {/**/}

    public static void init() {
        WATER = new FluidStack(Fluids.WATER, 1);
        DISTILLED_WATER = GtMaterials.DistilledWater.get().getFluid(1);
        LAVA = new FluidStack(Fluids.LAVA, 0);
        STEAM = GtMaterials.Steam.get().getFluid(1);
    }

    public static void postInit() {
        if (ERROR_ON_INVALID_RECIPE && hasInvalidRecipe) {
            throw new IllegalStateException("Invalid Recipes Found. See earlier log entries for details.");
        }
    }

    // Fluids

    /**
     * @param stack the fluid to check
     * @return if the fluid is a valid water fluid
     */
    public static boolean isWater(@Nullable FluidStack stack) {
        if (stack == null) return false;
        if (WATER.isFluidEqual(stack)) return true;
        if (DISTILLED_WATER.isFluidEqual(stack)) return true;

        for (ResourceLocation fluidName : ConfigHolder.machines.boilerFluids) {
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
            if (fluid == null) continue;
            if (stack.isFluidEqual(new FluidStack(fluid, 1))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param stack the fluid to check
     * @return if the fluid is a valid lava fluid
     */
    @SuppressWarnings("unused")
    public static boolean isLava(FluidStack stack) {
        return LAVA.isFluidEqual(stack);
    }

    /**
     * @param amount the amount of fluid
     * @return a FluidStack of lava
     */
    @SuppressWarnings("unused")
    @Nonnull
    public static FluidStack getLava(int amount) {
        return new FluidStack(Fluids.LAVA, amount);
    }

    /**
     * @param stack the fluid to check
     * @return if the fluid is a valid steam fluid
     */
    public static boolean isSteam(FluidStack stack) {
        return STEAM.isFluidEqual(stack);
    }

    /**
     * Returns a Liquid Stack with given amount of Steam.
     */
    public static FluidStack getSteam(int amount) {
        return GtMaterials.Steam.get().getFluid(amount);
    }

    /**
     * @param material the material to check
     * @return if the material is a wood
     */
    public static boolean isMaterialWood(@Nullable Material material) {
        return material == GtMaterials.Wood.get() || material == GtMaterials.TreatedWood.get();
    }

    // Furnace Smelting

    /**
     * @param stack the stack to check
     * @return the furnace fuel value for the stack
     */
    public static int getFuelValue(@Nonnull ItemStack stack) {
        return ForgeHooks.getBurnTime(stack, null);
    }

    /**
     * @param fuelStack the stack to check
     * @return the remainder item for a burnt fuel in a boiler
     */
    public static ItemStack getBurningFuelRemainder(ItemStack fuelStack) {
        float remainderChance;
        Item remainder;
        if (TagUnifier.getTagNames(fuelStack.getItem()).contains("fuel/coke")) {
            remainder = TagUnifier.get(TagPrefix.dust, GtMaterials.Ash.get());
            remainderChance = 0.5f;
        } else {
            MaterialStack materialStack = TagUnifier.getMaterial(fuelStack.getItem());
            if (materialStack == null)
                return ItemStack.EMPTY;
            else if (materialStack.material == GtMaterials.Charcoal.get()) {
                remainder = TagUnifier.get(TagPrefix.dust, GtMaterials.Ash.get());
                remainderChance = 0.3f;
            } else if (materialStack.material == GtMaterials.Coal.get()) {
                remainder = TagUnifier.get(TagPrefix.dust, GtMaterials.DarkAsh.get());
                remainderChance = 0.35f;
            } else if (materialStack.material == GtMaterials.Coke.get()) {
                remainder = TagUnifier.get(TagPrefix.dust, GtMaterials.Ash.get());
                remainderChance = 0.5f;
            } else return ItemStack.EMPTY;
        }
        return GTValues.RNG.nextFloat() <= remainderChance ? new ItemStack(remainder) : ItemStack.EMPTY;
    }

    /*

    /**
     * Add a smelting recipe for all valid items in a unification entry
     *
     * @param input  the unification entry to input
     * @param output the output of the recipe
     *
    public static void addSmeltingRecipe(@Nonnull UnificationEntry input, @Nonnull ItemStack output) {
        addSmeltingRecipe(input, output, 0.0F);
    }

    /**
     * @param input  the input of the recipe
     * @param output the output of the recipe
     *
    public static void addSmeltingRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output) {
        addSmeltingRecipe(input, output, 0.0F);
    }

    /**
     * Add a smelting recipe for all valid items in a unification entry
     *
     * @param input      the unification entry to input
     * @param output     the output of the recipe
     * @param experience the experience of the recipe
     *
    public static void addSmeltingRecipe(@Nonnull UnificationEntry input, @Nonnull ItemStack output, float experience) {
        for (Item inputStack : TagUnifier.getAll(input)) {
            addSmeltingRecipe(new ItemStack(inputStack), output, experience);
        }
    }

    private static final LoadingCache<ItemStack, SmeltingRecipe> recipeCache = CacheBuilder.newBuilder().initialCapacity(100).build(new CacheLoader<>() {
        @Override
        public SmeltingRecipe load(ItemStack ingredient) {
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream().filter(smeltingRecipe -> smeltingRecipe.getIngredients().get(0).test(ingredient)).findFirst().get();
        }
    });

    /**
     * Add a furnace smelting recipe
     *
     * @param input      the input of the recipe
     * @param output     the output of the recipe
     * @param experience the experience of the recipe
     *
    public static void addSmeltingRecipe(@Nonnull ItemStack input, @Nonnull ItemStack output, float experience, FurnaceRecipeBuilder builder) {
        if (input.isEmpty() && setErroredInvalidRecipe("Furnace Recipe Input cannot be an empty ItemStack")) {
            return;
        }
        if (output.isEmpty() && setErroredInvalidRecipe("Furnace Recipe Output cannot be an empty ItemStack")) {
            return;
        }


        RecipeManager manager =  ServerLifecycleHooks.getCurrentServer().getRecipeManager();

        try {
            SmeltingRecipe recipe = recipeCache.get(input);
            if (recipe == null) {
                //register only if there is no recipe with duplicate input
                builder.ingredient(Ingredient.of(input)).experience(experience).build();
                recipes.addSmeltingRecipe(input, output, experience);
            } else {
                logInvalidRecipe(String.format("Tried to register duplicate Furnace Recipe: %sx %s:%s -> %sx %s:%s, %sexp",
                        input.getCount(), Objects.requireNonNull(Util.getId(input.getItem())).getNamespace(), input.getDisplayName(),
                        output.getCount(), Objects.requireNonNull(Util.getId(input.getItem())).getNamespace(), output.getDisplayName(), experience));
            }
        } catch (Exception ignored) {

        }
    }

    /**
     * @param input the input for the recipe
     * @return the output of the recipe
     *
    @Nonnull
    public static ItemStack getSmeltingOutput(@Nonnull ItemStack input) {
        if (input.isEmpty()) return ItemStack.EMPTY;
        return TagUnifier.getUnificated(FurnaceRecipes.instance().getSmeltingResult(input));
    }

    // Crafting Recipes

    /**
     * Adds Shaped Crafting Recipes.
     * <p/>
     * {@link MetaItem}'s are converted to ItemStack via {@link MetaItem.MetaValueItem#getStackForm()} method.
     * <p/>
     * For Enums - {@link Enum#name()} is called.
     * <p/>
     * For {@link UnificationEntry} - {@link UnificationEntry#toString()} is called.
     * <p/>
     * For Lowercase Characters - gets IGTool from {@link ToolHelper#getToolFromSymbol(Character)}, and calls {@link IGTTool#getOreDictName()}
     * <p/>
     * Base tool names are as follows:
     * <ul>
     * <li>{@code 'c'} -  {@code craftingToolCrowbar}</li>
     * <li>{@code 'd'} -  {@code craftingToolScrewdriver}</li>
     * <li>{@code 'f'} -  {@code craftingToolFile}</li>
     * <li>{@code 'h'} -  {@code craftingToolHardHammer}</li>
     * <li>{@code 'k'} -  {@code craftingToolKnife}</li>
     * <li>{@code 'm'} -  {@code craftingToolMortar}</li>
     * <li>{@code 'r'} -  {@code craftingToolSoftHammer}</li>
     * <li>{@code 's'} -  {@code craftingToolSaw}</li>
     * <li>{@code 'w'} -  {@code craftingToolWrench}</li>
     * <li>{@code 'x'} -  {@code craftingToolWireCutter}</li>
     * </ul>
     *
     * @param regName the registry name for the recipe
     * @param result  the output for the recipe
     * @param recipe  the contents of the recipe
     *
    public static void addShapedRecipe(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
        addShapedRecipe(false, regName, result, false, false, recipe);
    }

    /**
     * Adds a shaped recipe with a single fluid container, which gets consumed as input and is output with different contents
     *
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     *
    public static void addFluidReplaceRecipe(String regName, ItemStack result, Object... recipe) {
        addFluidReplaceRecipe(regName, result, false, recipe);
    }

    /**
     * Adds a shaped recipe which clears the nbt of the outputs
     *
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     *
    public static void addShapedNBTClearingRecipe(String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(false, regName, result, true, false, recipe);
    }

    /**
     * Adds a shaped recipe which sets the {@link UnificationEntry} for the output according to the crafting inputs
     *
     * @param withUnificationData whether to use unification data
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     *
    public static void addShapedRecipe(boolean withUnificationData, String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(withUnificationData, regName, result, false, false, recipe);
    }

    /**
     * Adds a mirrored shaped recipe
     *
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     *
    public static void addMirroredShapedRecipe(String regName, ItemStack result, Object... recipe) {
        addShapedRecipe(false, regName, result, false, true, recipe);
    }

    /**
     * @param withUnificationData whether to use unification data
     * @param isNBTClearing       whether to clear output nbt
     * @param isMirrored          whether the recipe should be mirrored
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     *
    public static void addShapedRecipe(boolean withUnificationData, @Nonnull String regName, @Nonnull ItemStack result, boolean isNBTClearing, boolean isMirrored, @Nonnull Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        addRecipe(regName, result, isNBTClearing, isMirrored, recipe);

        if (withUnificationData) {
            OreDictUnifier.registerOre(result, getRecyclingIngredients(result.getCount(), recipe));
        }
    }

    /**
     * @see ModHandler#addFluidReplaceRecipe(String, ItemStack, Object...)
     *
    public static void addFluidReplaceRecipe(String regName, ItemStack result, boolean isNBTClearing, Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        IRecipe shapedOreRecipe = new FluidReplaceRecipe(isNBTClearing, null, result.copy(),
                finalizeShapedRecipeInput(recipe))
                .setMirrored(false) //make all recipes not mirrored by default
                .setRegistryName(regName);

        registerRecipe(shapedOreRecipe);
    }

    /**
     * @param chargePredicate   the predicate for charging the output
     * @param overrideCharge    whether to override the energy amount
     * @param transferMaxCharge whether to transfer all the potential charge
     * @see ModHandler#addShapedRecipe(String, ItemStack, Object...)
     *
    public static void addShapedEnergyTransferRecipe(String regName, ItemStack result, Predicate<ItemStack> chargePredicate, boolean overrideCharge, boolean transferMaxCharge, Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        IRecipe shapedOreRecipe = new ShapedOreEnergyTransferRecipe(null, result.copy(), chargePredicate, overrideCharge, transferMaxCharge, finalizeShapedRecipeInput(recipe))
                .setMirrored(false)
                .setRegistryName(regName);

        registerRecipe(shapedOreRecipe);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean validateRecipeWithOutput(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
        if (result.isEmpty()) {
            if (setErroredInvalidRecipe("Recipe output cannot be an empty ItemStack. Recipe: " + regName)) return false;
        }
        return validateRecipe(regName, recipe);
    }

    private static void addRecipe(@Nonnull String regName, @Nonnull ItemStack result, boolean isNBTClearing, boolean isMirrored, @Nonnull Object... recipe) {
        IRecipe shapedOreRecipe = new GTShapedOreRecipe(isNBTClearing, null, result.copy(), finalizeShapedRecipeInput(recipe))
                .setMirrored(isMirrored)
                .setRegistryName(regName);

        registerRecipe(shapedOreRecipe);
    }

    private static void registerRecipe(@Nonnull IRecipe recipe) {
        ForgeRegistries.RECIPES.register(recipe);
    }

    /**
     * @param regName the name of the recipe
     * @param recipe  the recipe to validate
     * @return if the recipe should be skipped
     *
    private static boolean validateRecipe(String regName, Object... recipe) {
        if (recipe == null) {
            return !setErroredInvalidRecipe("Recipe cannot be null");
        } else if (recipe.length == 0) {
            return !setErroredInvalidRecipe("Recipe cannot be empty");
        } else if (Arrays.asList(recipe).contains(null) || Arrays.asList(recipe).contains(ItemStack.EMPTY)) {
            String recipeMessage = Arrays.stream(recipe)
                    .map(o -> o == null ? "NULL" : o)
                    .map(o -> o == ItemStack.EMPTY ? "EMPTY STACK" : o)
                    .map(Object::toString)
                    .map(s -> "\"" + s + "\"")
                    .collect(Collectors.joining(", "));
            return !setErroredInvalidRecipe("Recipe cannot contain null elements or Empty ItemStacks. Recipe: " + recipeMessage);
        } else {
            ModContainer container = ModList.get().getModContainerById(GregTech.MODID).get();
            if (ForgeRegistries.RECIPES.containsKey(new ResourceLocation(container == null ? GregTech.MODID : container.getModId().toLowerCase(), regName))) {
                String recipeMessage = Arrays.stream(recipe)
                        .map(Object::toString)
                        .map(s -> "\"" + s + "\"")
                        .collect(Collectors.joining(", "));
                logInvalidRecipe("Tried to register recipe, " + regName + ", with duplicate key. Recipe: " + recipeMessage);
                return false;
            }
        }
        return true;
    }

    /**
     * @param recipe the recipe to finalize
     * @return the finalized recipe
     *
    @Nonnull
    public static Object[] finalizeShapedRecipeInput(Object... recipe) {
        for (byte i = 0; i < recipe.length; i++) {
            recipe[i] = finalizeIngredient(recipe[i]);
        }
        int idx = 0;
        Collection<Object> recipeList = new ArrayList<>(Arrays.asList(recipe));

        while (recipe[idx] instanceof String) {
            StringBuilder s = new StringBuilder((String) recipe[idx++]);
            while (s.length() < 3) s.append(" ");
            if (s.length() > 3) throw new IllegalArgumentException("Recipe row cannot be larger than 3. Index: " + idx);
            for (char c : s.toString().toCharArray()) {
                IGTTool tool = ToolHelper.getToolFromSymbol(c);
                if (tool != null && tool.getOreDictName() != null) {
                    recipeList.add(c);
                    recipeList.add(tool.getOreDictName());
                }
            }
        }
        return recipeList.toArray();
    }

    /**
     * @param ingredient the ingredient to finalize
     * @return the finalized ingredient
     *
    @Nonnull
    public static Object finalizeIngredient(@Nonnull Object ingredient) {
        if (ingredient instanceof MetaItem) {
            ingredient = new ItemStack((MetaItem) ingredient);
        } else if (ingredient instanceof Enum) {
            ingredient = ((Enum<?>) ingredient).name();
        } else if (ingredient instanceof TagPrefix) {
            ingredient = ((TagPrefix) ingredient).name();
        } else if (ingredient instanceof UnificationEntry) {
            UnificationEntry entry = (UnificationEntry) ingredient;
            if (ConfigHolder.misc.debug && entry.material != null && !entry.tagPrefix.isIgnored(entry.material) &&
                    !entry.tagPrefix.doGenerateItem(entry.material)) {
                logInvalidRecipe("Attempted to create recipe for invalid/missing Unification Entry " + ingredient);
            }
            ingredient = ingredient.toString();
        } else if (!(ingredient instanceof ItemStack
                || ingredient instanceof Item
                || ingredient instanceof Block
                || ingredient instanceof String
                || ingredient instanceof Character
                || ingredient instanceof Boolean
                || ingredient instanceof Ingredient)) {
            throw new IllegalArgumentException(ingredient.getClass().getSimpleName() + " type is not suitable for crafting input.");
        }
        return ingredient;
    }

    /**
     * @param outputCount the amount of outputs the recipe has
     * @param recipe      the recipe to retrieve from
     * @return the recycling ingredients for a recipe
     *
    @Nullable
    public static ItemMaterialInfo getRecyclingIngredients(int outputCount, @Nonnull Object... recipe) {
        Char2IntOpenHashMap inputCountMap = new Char2IntOpenHashMap();
        Object2LongMap<Material> materialStacksExploded = new Object2LongOpenHashMap<>();

        int itr = 0;
        while (recipe[itr] instanceof String) {
            String s = (String) recipe[itr];
            for (char c : s.toCharArray()) {
                if (ToolHelper.getToolFromSymbol(c) != null) continue; // skip tools
                int count = inputCountMap.getOrDefault(c, 0);
                inputCountMap.put(c, count + 1);
            }
            itr++;
        }

        char lastChar = ' ';
        for (int i = itr; i < recipe.length; i++) {
            Object ingredient = recipe[i];

            // Track the current working ingredient symbol
            if (ingredient instanceof Character) {
                lastChar = (char) ingredient;
                continue;
            }

            // Should never happen if recipe is formatted correctly
            // In the case that it isn't, this error should be handled
            // by an earlier method call parsing the recipe.
            if (lastChar == ' ') return null;

            Item item;
            if (ingredient instanceof MetaItem) {
                item = (MetaItem) ingredient;
            } else if (ingredient instanceof UnificationEntry) {
                item = TagUnifier.get((UnificationEntry) ingredient);
            } else if (ingredient instanceof ItemStack) {
                item = ((ItemStack) ingredient).getItem();
            } else if (ingredient instanceof Item) {
                item = (Item) ingredient;
            } else if (ingredient instanceof Block) {
                item = ((Block) ingredient).asItem();
            } else if (ingredient instanceof String) {
                item = TagUnifier.get((String) ingredient);
            } else continue; // throw out bad entries

            // First try to get ItemMaterialInfo
            ItemMaterialInfo info = TagUnifier.getMaterialInfo(item);
            if (info != null) {
                for (MaterialStack ms : info.getMaterials()) {
                    if (!(ms.material instanceof MarkerMaterial)) {
                        addMaterialStack(materialStacksExploded, inputCountMap, ms, lastChar);
                    }
                }
                continue;
            }

            // Then try to get a single Material (UnificationEntry needs this, for example)
            MaterialStack materialStack = TagUnifier.getMaterial(item);
            if (materialStack != null && !(materialStack.material instanceof MarkerMaterial)) {
                addMaterialStack(materialStacksExploded, inputCountMap, materialStack, lastChar);
            }

            // Gather any secondary materials if this item has an TagPrefix
            TagPrefix prefix = TagUnifier.getPrefix(item);
            if (prefix != null && !prefix.secondaryMaterials.isEmpty()) {
                for (MaterialStack ms : prefix.secondaryMaterials) {
                    addMaterialStack(materialStacksExploded, inputCountMap, ms, lastChar);
                }
            }
        }

        return new ItemMaterialInfo(materialStacksExploded.entrySet().stream()
                .map(e -> new MaterialStack(e.getKey(), e.getValue() / outputCount))
                .sorted(Comparator.comparingLong(m -> -m.amount))
                .collect(Collectors.toList())
        );
    }

    /**
     * Adds a MaterialStack to a map of {@code <Material, Quantity>}
     *
     * @param materialStacksExploded the map to add to
     * @param inputCountMap          the map supplying quantities by char
     * @param ms                     the stack to add
     * @param c                      the char for quantities
     *
    private static void addMaterialStack(@Nonnull Object2LongMap<Material> materialStacksExploded,
                                         @Nonnull Char2IntFunction inputCountMap, @Nonnull MaterialStack ms, char c) {
        long amount = materialStacksExploded.getOrDefault(ms.material, 0L);
        materialStacksExploded.put(ms.material, (ms.amount * inputCountMap.get(c)) + amount);
    }

    /**
     * Add a shapeless recipe
     *
     * @param regName the registry name of the recipe
     * @param result  the output of the recipe
     * @param recipe  the recipe to add
     *
    public static void addShapelessRecipe(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
        addShapelessRecipe(regName, result, false, recipe);
    }

    /**
     * Adds a shapeless recipe which clears the nbt of the outputs
     *
     * @see ModHandler#addShapelessRecipe(String, ItemStack, boolean, Object...)
     *
    public static void addShapelessNBTClearingRecipe(@Nonnull String regName, @Nonnull ItemStack result, @Nonnull Object... recipe) {
        addShapelessRecipe(regName, result, true, recipe);
    }

    /**
     * Add a shapeless recipe
     *
     * @param isNBTClearing if the recipe should clear the nbt of the outputs
     * @see ModHandler#addShapelessRecipe(String, ItemStack, Object...)
     *
    public static void addShapelessRecipe(String regName, ItemStack result, boolean isNBTClearing, Object... recipe) {
        if (!validateRecipeWithOutput(regName, result, recipe)) return;

        for (byte i = 0; i < recipe.length; i++) {
            if (recipe[i] instanceof MetaItem) {
                recipe[i] = ((MetaItem) recipe[i]);
            } else if (recipe[i] instanceof Enum) {
                recipe[i] = ((Enum<?>) recipe[i]).name();
            } else if (recipe[i] instanceof TagPrefix) {
                recipe[i] = ((TagPrefix) recipe[i]).name();
            } else if (recipe[i] instanceof UnificationEntry) {
                recipe[i] = recipe[i].toString();
            } else if (recipe[i] instanceof Character) {
                IGTTool tool = ToolHelper.getToolFromSymbol((char) recipe[i]);
                if (tool == null || tool.getOreDictName() == null) {
                    throw new IllegalArgumentException("Tool name is not found for char " + recipe[i]);
                }
                recipe[i] = tool.getOreDictName();
            } else if (!(recipe[i] instanceof ItemStack
                    || recipe[i] instanceof Item
                    || recipe[i] instanceof Block
                    || recipe[i] instanceof String)) {
                throw new IllegalArgumentException(recipe.getClass().getSimpleName() + " type is not suitable for crafting input.");
            }
        }
        IRecipe shapelessRecipe = new GTShapelessOreRecipe(isNBTClearing, null, result.copy(), recipe)
                .setRegistryName(regName);

        try {
            // workaround for MC bug that makes all shaped recipe inputs that have enchanted items
            // or renamed ones on input fail, even if all ingredients match it
            Field field = ShapelessOreRecipe.class.getDeclaredField("isSimple");
            field.setAccessible(true);
            field.setBoolean(shapelessRecipe, false);
        } catch (ReflectiveOperationException exception) {
            GregTech.LOGGER.error("Failed to mark shapeless recipe as complex", exception);
        }

        registerRecipe(shapelessRecipe);
    }

    // recipe removal

    /**
     * Remove all recipes matching a unification entry as input
     *
     * @param input the input to match
     * @return if a recipe was removed
     *
    @SuppressWarnings("unused")
    public static boolean removeFurnaceSmelting(@Nonnull UnificationEntry input) {
        boolean result = false;
        for (Item inputStack : TagUnifier.getAll(input)) {
            result = result || removeFurnaceSmelting(new ItemStack(inputStack));
        }
        return result;
    }

    /**
     * Remove a Smelting Recipe by input
     *
     * @param input the input to remove by
     * @return if the recipe was removed
     *
    public static boolean removeFurnaceSmelting(@Nonnull ItemStack input) {
        if (input.isEmpty()) {
            if (setErroredInvalidRecipe("Cannot remove furnace recipe with empty input.")) return false;
        }

        boolean wasRemoved = FurnaceRecipes.instance().getSmeltingList().keySet().removeIf(currentStack -> currentStack.getItem() == input.getItem() && (currentStack.getMetadata() == GTValues.W || currentStack.getMetadata() == input.getMetadata()));

        if (ConfigHolder.misc.debug) {
            if (wasRemoved) {
                GregTech.LOGGER.info("Removed Smelting Recipe for Input: {}", input.getDisplayName());
            } else {
                GregTech.LOGGER.error("Failed to Remove Smelting Recipe for Input: {}", input.getDisplayName());
            }
        }

        return wasRemoved;
    }

    /**
     * Remove all recipes matching an output
     *
     * @param output the output to match
     * @return the amount of recipes removed
     *
    @SuppressWarnings("UnusedReturnValue")
    public static int removeRecipeByOutput(@Nonnull ItemStack output) {
        int recipesRemoved = removeRecipeByOutput(recipe -> ItemStack.areItemStacksEqual(recipe.getRecipeOutput(), output));

        if (ConfigHolder.misc.debug) {
            if (recipesRemoved != 0) {
                GregTech.LOGGER.info("Removed {} Recipe(s) with Output: {}", recipesRemoved, output.getDisplayName());
            } else {
                GregTech.LOGGER.error("Failed to Remove Recipe with Output: {}", output.getDisplayName());
            }
        }
        return recipesRemoved;
    }

    /**
     * Remove all recipes matching a predicate
     *
     * @param predicate the matcher
     * @return the amount of recipes removed
     *
    public static int removeRecipeByOutput(Predicate<IRecipe> predicate) {
        int recipesRemoved = 0;

        final IForgeRegistry<IRecipe> registry = ForgeRegistries.RECIPES;

        Collection<IRecipe> toRemove = new ArrayList<>();

        for (IRecipe recipe : registry) {
            if (predicate.test(recipe)) {
                toRemove.add(recipe);
                recipesRemoved++;
            }
        }

        toRemove.forEach(recipe -> {
            if (recipe.getRegistryName() != null) {
                registry.register(new DummyRecipe().setRegistryName(recipe.getRegistryName()));
            }
        });

        return recipesRemoved;
    }

    /**
     * Removes a Crafting Table Recipe with the given name.
     *
     * @param location the ResourceLocation of the Recipe.
     *
    public static void removeRecipeByName(@Nonnull ResourceLocation location) {
        if (ConfigHolder.misc.debug) {
            String recipeName = location.toString();
            if (ForgeRegistries.RECIPES.containsKey(location)) {
                GregTech.LOGGER.info("Removed Recipe with Name: {}", recipeName);
            } else {
                GregTech.LOGGER.error("Failed to Remove Recipe with Name: {}", recipeName);
            }
        }
        ForgeRegistries.RECIPES.register(new DummyRecipe().setRegistryName(location));
    }

    /**
     * Removes a Crafting Table Recipe with the given name.
     *
     * @param recipeName the name of the Recipe.
     *
    public static void removeRecipeByName(String recipeName) {
        removeRecipeByName(new ResourceLocation(recipeName));
    }

    /**
     * Removes Crafting Table Recipes with a range of names, being {@link GTValues} voltage names.
     *
     * <p>
     * An example of how to use it: {@code removeTieredRecipeByName("gregtech:transformer_", EV, UV);}
     * <p>
     * This will remove recipes with names:
     *
     * <ul>
     * <li>gregtech:transformer_ev</li>
     * <li>gregtech:transformer_iv</li>
     * <li>gregtech:transformer_luv</li>
     * <li>gregtech:transformer_zpm</li>
     * <li>gregtech:transformer_uv</li>
     * </ul>
     *
     * @param recipeName The base name of the Recipes to remove.
     * @param startTier  The starting tier index, inclusive.
     * @param endTier    The ending tier index, inclusive.
     *
    @SuppressWarnings("unused")
    public static void removeTieredRecipeByName(@Nonnull String recipeName, int startTier, int endTier) {
        for (int i = startTier; i <= endTier; i++) {
            removeRecipeByName(String.format("%s%s", recipeName, GTValues.VN[i].toLowerCase()));
        }
    }

    ///////////////////////////////////////////////////
    //            Get Recipe Output Helpers          //
    ///////////////////////////////////////////////////

    /**
     * @param world  the world to check the output from
     * @param recipe the recipe to retrieve from. Must not contain null values.
     * @return a Pair of the recipe, and the output
     *
    @Nonnull
    public static Pair<IRecipe, ItemStack> getRecipeOutput(@Nullable World world, @Nullable ItemStack... recipe) {
        if (recipe == null || recipe.length == 0) return ImmutablePair.of(null, ItemStack.EMPTY);
        if (world == null) world = DummyWorld.INSTANCE;

        InventoryCrafting craftingGrid = new InventoryCrafting(new DummyContainer(), 3, 3);

        for (int i = 0; i < 9 && i < recipe.length; i++) {
            ItemStack recipeStack = recipe[i];
            if (!recipeStack.isEmpty()) {
                craftingGrid.setInventorySlotContents(i, recipeStack);
            }
        }

        for (IRecipe tmpRecipe : CraftingManager.REGISTRY) {
            if (tmpRecipe.matches(craftingGrid, world)) {
                ItemStack itemStack = tmpRecipe.getCraftingResult(craftingGrid);
                return ImmutablePair.of(tmpRecipe, itemStack);
            }
        }

        return ImmutablePair.of(null, ItemStack.EMPTY);
    }

    /**
     * Note: If a Furnace recipe is added through CT that is the exact same as one of the recipes that will be removed
     * then this recipe will not be added. Forge will prevent the duplicate smelting recipe from being added
     * before we remove the recipe added by another mod, therefore the CT recipe will fail. At this point,
     * disable the config and remove the recipes manually
     *
    public static void removeSmeltingEBFMetals() {
        boolean isCTLoaded = ModList.get().isLoaded(GTValues.MODID_CT);

        Field actionAddFurnaceRecipe$output = null;

        Map<ItemStack, ItemStack> furnaceList = FurnaceRecipes.instance().getSmeltingList();

        Iterator<Map.Entry<ItemStack, ItemStack>> recipeIterator = furnaceList.entrySet().iterator();

        outer:
        while (recipeIterator.hasNext()) {
            Map.Entry<ItemStack, ItemStack> recipe = recipeIterator.next();

            ItemStack output = recipe.getValue();
            ItemStack input = recipe.getKey();
            MaterialStack ms = OreDictUnifier.getMaterial(output);

            if (ms != null) {
                Material material = ms.material;
                if (material.hasProperty(GtMaterialProperties.BLAST.get())) {
                    Item dust = TagUnifier.get(TagPrefix.dust, material);
                    Item ingot = TagUnifier.get(TagPrefix.ingot, material);
                    //Check if the inputs are actually dust -> ingot
                    if (output.is(ingot) && input.is(dust)) {
                        if (isCTLoaded) {
                            if (actionAddFurnaceRecipe$output == null) {
                                try {
                                    actionAddFurnaceRecipe$output = ActionAddFurnaceRecipe.class.getDeclaredField("output");
                                    actionAddFurnaceRecipe$output.setAccessible(true);
                                } catch (NoSuchFieldException e) {
                                    GregTech.LOGGER.error("Could not reflect Furnace output field", e);
                                    return;
                                }
                            }
                            for (ActionAddFurnaceRecipe aafr : MCFurnaceManager.recipesToAdd) {
                                try {
                                    // Check for equality, if the stack added into FurnaceManager..
                                    // ..was a cached stack in an existing ActionAddFurnaceRecipe as well
                                    if (actionAddFurnaceRecipe$output.get(aafr) == output) {
                                        if (ConfigHolder.misc.debug) {
                                            GregTech.LOGGER.info("Not removing Smelting Recipe for EBF material {} as it is added via CT", LocalizationUtils.format(material.getUnlocalizedName()));
                                        }
                                        continue outer;
                                    }
                                } catch (IllegalAccessException e) {
                                    GregTech.LOGGER.error("Could not get Furnace recipe output from field", e);
                                }
                            }
                        }
                        recipeIterator.remove();
                        if (ConfigHolder.misc.debug) {
                            GregTech.LOGGER.info("Removing Smelting Recipe for EBF material {}", LocalizationUtils.format(material.getUnlocalizedName()));
                        }
                    }
                }
            }
        }
    }

    */

    /**
     * @param message the message for the exception
     * @return if recipe registration should continue
     * @throws IllegalArgumentException if a recipe was invalid and invalid recipes are not ignored
     */
    public static boolean setErroredInvalidRecipe(@Nonnull String message) throws IllegalArgumentException {
        hasInvalidRecipe = true;
        logInvalidRecipe(message);
        return ERROR_ON_INVALID_RECIPE;
    }

    public static void logInvalidRecipe(@Nonnull String message) {
        GregTech.LOGGER.warn("Invalid Recipe Found", new IllegalArgumentException(message));
    }
}

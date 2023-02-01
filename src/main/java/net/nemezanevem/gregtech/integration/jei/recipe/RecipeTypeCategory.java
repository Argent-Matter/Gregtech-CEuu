package net.nemezanevem.gregtech.integration.jei.recipe;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.capability.impl.FluidTankList;
import net.nemezanevem.gregtech.api.gui.BlankUIHolder;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.widgets.ProgressWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.gui.widgets.TankWidget;
import net.nemezanevem.gregtech.api.recipe.GTRecipe;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RecipeTypeCategory implements IRecipeCategory<GTRecipeWrapper> {

    public static final RecipeType<GTRecipeWrapper> GREGTECH = RecipeType.create(GregTech.MODID, "gregtech", GTRecipeWrapper.class);

    private final GTRecipeType<?> recipeType;
    private final ModularUI modularUI;
    private final ItemStackHandler importItems, exportItems;
    private final FluidTankList importFluids, exportFluids;
    private final IDrawable backgroundDrawable;
    private Object iconIngredient;
    private IDrawable icon;

    private static final int FONT_HEIGHT = 9;
    private static final HashMap<GTRecipeType<?>, RecipeTypeCategory> categoryMap = new HashMap<>();

    public RecipeTypeCategory(GTRecipeType<?> recipeMap, IGuiHelper guiHelper) {
        this.recipeType = recipeMap;
        FluidTank[] importFluidTanks = new FluidTank[recipeMap.getMaxFluidInputs()];
        for (int i = 0; i < importFluidTanks.length; i++)
            importFluidTanks[i] = new FluidTank(16000);
        FluidTank[] exportFluidTanks = new FluidTank[recipeMap.getMaxFluidOutputs()];
        for (int i = 0; i < exportFluidTanks.length; i++)
            exportFluidTanks[i] = new FluidTank(16000);
        this.modularUI = recipeMap.createJeiUITemplate(
                (importItems = new ItemStackHandler(recipeMap.getMaxInputs())),
                (exportItems = new ItemStackHandler(recipeMap.getMaxOutputs())),
                (importFluids = new FluidTankList(false, importFluidTanks)),
                (exportFluids = new FluidTankList(false, exportFluidTanks)), 0
        ).build(new BlankUIHolder(), Minecraft.getInstance().player);
        this.modularUI.initWidgets();
        this.backgroundDrawable = guiHelper.createBlankDrawable(modularUI.getWidth(), modularUI.getHeight() * 2 / 3 + getPropertyShiftAmount(recipeMap));
        categoryMap.put(recipeMap, this);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryName(GTRecipeWrapper recipe) {
        return recipe.getRecipe().getId();
    }

    @Override
    public RecipeType<GTRecipeWrapper> getRecipeType() {
        return GREGTECH;
    }

    @Override
    @Nonnull
    public Component getTitle() {
        return recipeType.getLocalizedName();
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        if (icon != null) {
            return icon;
        } else if (iconIngredient != null) {
            // cache the icon drawable for less gc pressure
            return icon = GTJeiPlugin.guiHelper.createDrawableIngredient(iconIngredient);
        }
        // JEI will automatically populate the icon as the first registered catalyst if null
        return null;
    }

    public void setIcon(Object icon) {
        if (iconIngredient == null) {
            iconIngredient = icon;
        }
    }

    @Override
    @Nonnull
    public IDrawable getBackground() {
        return backgroundDrawable;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GTRecipeWrapper recipeWrapper, IFocusGroup focuses) {
        List<List<FluidStack>> inputsList = recipeWrapper.getRecipe().getFluidInputs().stream().map(input -> Arrays.asList(input.getFluids())).toList();
        List<FluidStack> outputsList = recipeWrapper.getRecipe().getFluidOutputs().stream().toList();

        for (Widget uiWidget : modularUI.guiWidgets.values()) {

            if (uiWidget instanceof SlotWidget slotWidget) {
                if (!(slotWidget.getHandle() instanceof SlotItemHandler handle)) {
                    continue;
                }
                if (handle.getItemHandler() == importItems) {
                    //this is input item stack slot widget, so add it to item group
                    if(recipeWrapper.getRecipe().getInputs().get(handle.getSlotIndex()).isConsumable()) {
                        builder.addSlot(RecipeIngredientRole.INPUT,
                                        slotWidget.getPosition().x + 1,
                                        slotWidget.getPosition().y + 1)
                                .addItemStacks(Arrays.asList(recipeWrapper.getRecipe().getIngredients().get(handle.getSlotIndex()).getItems()))
                                .addTooltipCallback(recipeWrapper::addItemTooltip)
                                .setSlotName(slotWidget.getPosition().x + " " + slotWidget.getPosition().y);
                    } else {
                        builder.addSlot(RecipeIngredientRole.CATALYST,
                                        slotWidget.getPosition().x + 1,
                                        slotWidget.getPosition().y + 1)
                                .addItemStacks(Arrays.asList(recipeWrapper.getRecipe().getIngredients().get(handle.getSlotIndex()).getItems()))
                                .addTooltipCallback(recipeWrapper::addItemTooltip)
                                .setSlotName(slotWidget.getPosition().x + " " + slotWidget.getPosition().y);
                    }

                } else if (handle.getItemHandler() == exportItems) {
                    //this is output item stack slot widget, so add it to item group
                    builder.addSlot(RecipeIngredientRole.OUTPUT,
                                    slotWidget.getPosition().x + 1,
                                    slotWidget.getPosition().y + 1)
                            .addItemStack(recipeWrapper.getRecipe().getOutputs().get(handle.getSlotIndex()))
                            .addTooltipCallback(recipeWrapper::addItemTooltip)
                            .setSlotName(slotWidget.getPosition().x + " " + slotWidget.getPosition().y);
                }

            } else if (uiWidget instanceof TankWidget tankWidget) {
                if (importFluids.getFluidTanks().contains(tankWidget.fluidTank)) {
                    int importIndex = importFluids.getFluidTanks().indexOf(tankWidget.fluidTank);
                    int fluidAmount = 0;
                    if (inputsList.size() > importIndex && !inputsList.get(importIndex).isEmpty())
                        fluidAmount = inputsList.get(importIndex).get(0).getAmount();
                    //this is input tank widget, so add it to fluid group
                    if(recipeWrapper.getRecipe().getFluidInputs().get(importIndex).isConsumable()) {
                        builder.addSlot(RecipeIngredientRole.INPUT,
                                        tankWidget.getPosition().x,
                                        tankWidget.getPosition().y)
                                .setSlotName(tankWidget.getPosition().x + " " + tankWidget.getPosition().y)
                                .addTooltipCallback(recipeWrapper::addFluidTooltip)
                                .addIngredients(ForgeTypes.FLUID_STACK, inputsList.get(importIndex));
                    } else {
                        builder.addSlot(RecipeIngredientRole.CATALYST,
                                        tankWidget.getPosition().x,
                                        tankWidget.getPosition().y)
                                .setSlotName(tankWidget.getPosition().x + " " + tankWidget.getPosition().y)
                                .addTooltipCallback(recipeWrapper::addFluidTooltip)
                                .addIngredients(ForgeTypes.FLUID_STACK, inputsList.get(importIndex));
                    }

                } else if (exportFluids.getFluidTanks().contains(tankWidget.fluidTank)) {
                    int exportIndex = exportFluids.getFluidTanks().indexOf(tankWidget.fluidTank);
                    int fluidAmount = 0;
                    if (outputsList.size() > exportIndex && !outputsList.get(exportIndex).isEmpty())
                        fluidAmount = outputsList.get(exportIndex).getAmount();
                    //this is output tank widget, so add it to fluid group
                    builder.addSlot(RecipeIngredientRole.INPUT,
                                    tankWidget.getPosition().x,
                                    tankWidget.getPosition().y)
                            .setSlotName(tankWidget.getPosition().x + " " + tankWidget.getPosition().y)
                            .addIngredient(ForgeTypes.FLUID_STACK, outputsList.get(exportIndex));

                }
            }
        }
        builder.setShapeless();
    }

    @Override
    public void draw(GTRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        var slots = recipeSlotsView.getSlotViews();
        for (int i = 0; i < slots.size(); ++i) {
            if(slots.get(i).getRole() == RecipeIngredientRole.OUTPUT) {
                GTRecipe.ChanceEntry entry = recipe.getOutputChance(i);
                if(entry != null) {
                    String[] xy = slots.get(i).getSlotName().get().split(" ");
                    int x = Integer.decode(xy[0]);
                    int y = Integer.decode(xy[1]);

                    stack.pushPose();
                    stack.scale(0.5f, 0.5f, 0.5f);
                    Minecraft.getInstance().font.draw(stack, Float.toString(entry.chance() / 1000f), x - 5, y - 5, 0xFFFFFF00);
                    stack.popPose();
                }
            }
        }

        recipe.drawInfo(stack, Minecraft.getInstance(), 128, 128, mouseX, mouseY);

        for (Widget widget : modularUI.guiWidgets.values()) {
            if (widget instanceof ProgressWidget) widget.detectAndSendChanges();
            widget.drawInBackground(stack, mouseY, mouseX, Minecraft.getInstance().getPartialTick(), new IRenderContext() {});
            widget.drawInForeground(stack, (int) mouseX, (int) mouseY);
        }
    }

    public static HashMap<GTRecipeType<?>, RecipeTypeCategory> getCategoryMap() {
        return categoryMap;
    }

    private static boolean shouldShiftWidgets(@Nonnull GTRecipeType<?> recipeMap) {
        return recipeMap.getMaxInputs() + recipeMap.getMaxOutputs() >= 6 ||
                recipeMap.getMaxFluidInputs() + recipeMap.getMaxFluidOutputs() >= 6;
    }

    private static int getPropertyShiftAmount(@Nonnull GTRecipeType<?> recipeMap) {
        int maxPropertyCount = 0;
        if (shouldShiftWidgets(recipeMap)) {
            for (GTRecipe recipe : recipeMap.getRecipeList()) {
                if (recipe.getPropertyCount() > maxPropertyCount)
                    maxPropertyCount = recipe.getPropertyCount();
            }
        }
        return maxPropertyCount * FONT_HEIGHT;
    }

    @Override
    public boolean isHandled(GTRecipeWrapper recipe) {
        return recipe.getRecipe().getType() == this.recipeType;
    }
}

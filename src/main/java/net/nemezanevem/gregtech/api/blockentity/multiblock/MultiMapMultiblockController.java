package net.nemezanevem.gregtech.api.blockentity.multiblock;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.capability.GregtechDataCodes;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.capability.IMultipleRecipeTypes;
import net.nemezanevem.gregtech.api.pattern.TraceabilityPredicate;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MultiMapMultiblockController extends RecipeTypeMultiblockController implements IMultipleRecipeTypes {

    // array of possible recipes, specific to each multi - used when the multi has multiple RecipeTypes
    private final GTRecipeType<?>[] recipeMaps;

    // index of the current selected recipe - used when the multi has multiple RecipeTypes
    private int recipeMapIndex = 0;

    public MultiMapMultiblockController(ResourceLocation metaTileEntityId, GTRecipeType<?>[] recipeMaps) {
        super(metaTileEntityId, recipeMaps[0]);
        this.recipeMaps = recipeMaps;
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (recipeMaps.length == 1) return true;
        if (!getWorld().isClientSide) {
            if (!this.recipeMapWorkable.isActive()) {
                int index;
                GTRecipeType<?>[] recipeMaps = getAvailableRecipeTypes();
                if (playerIn.isCrouching()) // cycle recipemaps backwards
                    index = (recipeMapIndex - 1 < 0 ? recipeMaps.length - 1 : recipeMapIndex - 1) % recipeMaps.length;
                else // cycle recipemaps forwards
                    index = (recipeMapIndex + 1) % recipeMaps.length;

                setRecipeTypeIndex(index);
                this.recipeMapWorkable.forceRecipeRecheck();
            } else {
                playerIn.sendSystemMessage(Component.translatable("gregtech.multiblock.multiple_recipemaps.switch_message"));
            }
        }

        return true; // return true here on the client to keep the GUI closed
    }

    @Override
    public GTRecipeType<?>[] getAvailableRecipeTypes() {
        return recipeMaps;
    }

    protected void setRecipeTypeIndex(int index) {
        this.recipeMapIndex = index;
        if (!getWorld().isClientSide) {
            writeCustomData(GregtechDataCodes.RECIPE_MAP_INDEX, buf -> buf.writeByte(index));
            markDirty();
        }
    }

    @Override
    public GTRecipeType<?> getCurrentRecipeType() {
        return getAvailableRecipeTypes()[recipeMapIndex];
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        boolean checkedItemIn = false, checkedItemOut = false, checkedFluidIn = false, checkedFluidOut = false;

        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(GtMultiblockAbilities.INPUT_ENERGY.get()).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        for (GTRecipeType<?> recipeMap : getAvailableRecipeTypes()) {
            if (!checkedItemIn && checkItemIn) {
                if (recipeMap.getMinInputs() > 0) {
                    checkedItemIn = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_ITEMS.get()).setMinGlobalLimited(1).setPreviewCount(1));
                } else if (recipeMap.getMaxInputs() > 0) {
                    checkedItemIn = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_ITEMS.get()).setPreviewCount(1));
                }
            }
            if (!checkedItemOut && checkItemOut) {
                if (recipeMap.getMinOutputs() > 0) {
                    checkedItemOut = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_ITEMS.get()).setMinGlobalLimited(1).setPreviewCount(1));
                } else if (recipeMap.getMaxOutputs() > 0) {
                    checkedItemOut = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_ITEMS.get()).setPreviewCount(1));
                }
            }
            if (!checkedFluidIn && checkFluidIn) {
                if (recipeMap.getMinFluidInputs() > 0) {
                    checkedFluidIn = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidInputs()));
                } else if (recipeMap.getMaxFluidInputs() > 0) {
                    checkedFluidIn = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()).setPreviewCount(1));
                }
            }
            if (!checkedFluidOut && checkFluidOut) {
                if (recipeMap.getMinFluidOutputs() > 0) {
                    checkedFluidOut = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_FLUIDS.get()).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidOutputs()));
                } else if (recipeMap.getMaxFluidOutputs() > 0) {
                    checkedFluidOut = true;
                    predicate = predicate.or(abilities(GtMultiblockAbilities.EXPORT_FLUIDS.get()).setPreviewCount(1));
                }
            }
        }
        return predicate;
    }

    @Override
    protected void addExtraDisplayInfo(List<Component> textList) {
        super.addExtraDisplayInfo(textList);
        if (recipeMaps.length == 1) return;
        textList.add(Component.translatable("gregtech.multiblock.multiple_recipemaps.header")
                .withStyle(style ->  style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("gregtech.multiblock.multiple_recipemaps.tooltip")))));

        textList.add(Component.translatable("recipemap." + getAvailableRecipeTypes()[this.recipeMapIndex].getUnlocalizedName() + ".name")
                .withStyle(style ->  style.withColor(ChatFormatting.AQUA)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("gregtech.multiblock.multiple_recipemaps.tooltip")))));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (recipeMaps.length == 1) return;
        tooltip.add(Component.translatable("gregtech.multiblock.multiple_recipemaps_recipes.tooltip", this.recipeMapsToString()));
    }

    public String recipeMapsToString() {
        StringBuilder recipeMapsString = new StringBuilder();
        GTRecipeType<?>[] recipeMaps = getAvailableRecipeTypes();
        for(int i = 0; i < recipeMaps.length; i++) {
            recipeMapsString.append(recipeMaps[i].getUnlocalizedName());
            if(recipeMaps.length - 1 != i)
                recipeMapsString.append(", "); // For delimiting
        }
        return recipeMapsString.toString();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.putInt("RecipeTypeIndex", recipeMapIndex);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        recipeMapIndex = data.getInt("RecipeTypeIndex");
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeByte(recipeMapIndex);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        recipeMapIndex = buf.readByte();
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.RECIPE_MAP_INDEX) {
            recipeMapIndex = buf.readByte();
            scheduleRenderUpdate();
        }
    }

    private LazyOptional<IMultipleRecipeTypes> lazy = LazyOptional.of(() -> this);

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        LazyOptional<T> capabilityResult = super.getCapability(capability, side);
        if (capabilityResult == null && capability == GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS) {
            return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS.orEmpty(capability, lazy);
        }
        return capabilityResult;
    }
}

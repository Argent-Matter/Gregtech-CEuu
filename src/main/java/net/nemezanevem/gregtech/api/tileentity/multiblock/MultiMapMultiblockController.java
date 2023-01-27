package net.nemezanevem.gregtech.api.tileentity.multiblock;

import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IMultipleRecipeTypes;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeType;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Direction;
import net.minecraft.util.InteractionHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("unused")
public abstract class MultiMapMultiblockController extends RecipeTypeMultiblockController implements IMultipleRecipeTypes {

    // array of possible recipes, specific to each multi - used when the multi has multiple RecipeTypes
    private final RecipeType<?>[] recipeMaps;

    // index of the current selected recipe - used when the multi has multiple RecipeTypes
    private int recipeMapIndex = 0;

    public MultiMapMultiblockController(ResourceLocation metaTileEntityId, RecipeType<?>[] recipeMaps) {
        super(metaTileEntityId, recipeMaps[0]);
        this.recipeMaps = recipeMaps;
    }

    @Override
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (recipeMaps.length == 1) return true;
        if (!getWorld().isClientSide) {
            if (!this.recipeMapWorkable.isActive()) {
                int index;
                RecipeType<?>[] recipeMaps = getAvailableRecipeTypes();
                if (playerIn.isSneaking()) // cycle recipemaps backwards
                    index = (recipeMapIndex - 1 < 0 ? recipeMaps.length - 1 : recipeMapIndex - 1) % recipeMaps.length;
                else // cycle recipemaps forwards
                    index = (recipeMapIndex + 1) % recipeMaps.length;

                setRecipeTypeIndex(index);
                this.recipeMapWorkable.forceRecipeRecheck();
            } else {
                playerIn.sendMessage(Component.translatable("gregtech.multiblock.multiple_recipemaps.switch_message"));
            }
        }

        return true; // return true here on the client to keep the GUI closed
    }

    @Override
    public RecipeType<?>[] getAvailableRecipeTypes() {
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
    public RecipeType<?> getCurrentRecipeType() {
        return getAvailableRecipeTypes()[recipeMapIndex];
    }

    @Override
    public TraceabilityPredicate autoAbilities(boolean checkEnergyIn, boolean checkMaintenance, boolean checkItemIn, boolean checkItemOut, boolean checkFluidIn, boolean checkFluidOut, boolean checkMuffler) {
        boolean checkedItemIn = false, checkedItemOut = false, checkedFluidIn = false, checkedFluidOut = false;

        TraceabilityPredicate predicate = super.autoAbilities(checkMaintenance, checkMuffler)
                .or(checkEnergyIn ? abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(3).setPreviewCount(1) : new TraceabilityPredicate());

        for (RecipeType<?> recipeMap : getAvailableRecipeTypes()) {
            if (!checkedItemIn && checkItemIn) {
                if (recipeMap.getMinInputs() > 0) {
                    checkedItemIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
                } else if (recipeMap.getMaxInputs() > 0) {
                    checkedItemIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1));
                }
            }
            if (!checkedItemOut && checkItemOut) {
                if (recipeMap.getMinOutputs() > 0) {
                    checkedItemOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setPreviewCount(1));
                } else if (recipeMap.getMaxOutputs() > 0) {
                    checkedItemOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1));
                }
            }
            if (!checkedFluidIn && checkFluidIn) {
                if (recipeMap.getMinFluidInputs() > 0) {
                    checkedFluidIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidInputs()));
                } else if (recipeMap.getMaxFluidInputs() > 0) {
                    checkedFluidIn = true;
                    predicate = predicate.or(abilities(MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(1));
                }
            }
            if (!checkedFluidOut && checkFluidOut) {
                if (recipeMap.getMinFluidOutputs() > 0) {
                    checkedFluidOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setMinGlobalLimited(1).setPreviewCount(recipeMap.getMinFluidOutputs()));
                } else if (recipeMap.getMaxFluidOutputs() > 0) {
                    checkedFluidOut = true;
                    predicate = predicate.or(abilities(MultiblockAbility.EXPORT_FLUIDS).setPreviewCount(1));
                }
            }
        }
        return predicate;
    }

    @Override
    protected void addExtraDisplayInfo(List<ITextComponent> textList) {
        super.addExtraDisplayInfo(textList);
        if (recipeMaps.length == 1) return;
        textList.add(Component.translatable("gregtech.multiblock.multiple_recipemaps.header")
                .setStyle(new Style().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("gregtech.multiblock.multiple_recipemaps.tooltip")))));

        textList.add(Component.translatable("recipemap." + getAvailableRecipeTypes()[this.recipeMapIndex].getUnlocalizedName() + ".name")
                .setStyle(new Style().setColor(TextFormatting.AQUA)
                        .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("gregtech.multiblock.multiple_recipemaps.tooltip")))));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        if (recipeMaps.length == 1) return;
        tooltip.add(Component.translatable("gregtech.multiblock.multiple_recipemaps_recipes.tooltip", this.recipeMapsToString()));
    }

    @SideOnly(Side.CLIENT)
    public String recipeMapsToString() {
        StringBuilder recipeMapsString = new StringBuilder();
        RecipeType<?>[] recipeMaps = getAvailableRecipeTypes();
        for(int i = 0; i < recipeMaps.length; i++) {
            recipeMapsString.append(recipeMaps[i].getLocalizedName());
            if(recipeMaps.length - 1 != i)
                recipeMapsString.append(", "); // For delimiting
        }
        return recipeMapsString.toString();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag data) {
        super.writeToNBT(data);
        data.setInteger("RecipeTypeIndex", recipeMapIndex);
        return data;
    }

    @Override
    public void readFromNBT(CompoundTag data) {
        super.readFromNBT(data);
        recipeMapIndex = data.getInteger("RecipeTypeIndex");
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

    @Override
    public <T> T getCapability(Capability<T> capability, Direction side) {
        T capabilityResult = super.getCapability(capability, side);
        if (capabilityResult == null && capability == GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS) {
            return GregtechTileCapabilities.CAPABILITY_MULTIPLE_RECIPEMAPS.cast(this);
        }
        return capabilityResult;
    }
}

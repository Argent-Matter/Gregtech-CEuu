package net.nemezanevem.gregtech.common.metatileentities.electric;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.SimpleMachineMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.impl.NotifiableItemStackHandler;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;

public class MetaTileEntityMacerator extends SimpleMachineMetaTileEntity {

    private final int outputAmount;

    public MetaTileEntityMacerator(ResourceLocation metaTileEntityId, GTRecipeType<?> recipeMap, int outputAmount, ICubeRenderer renderer, int tier) {
        super(metaTileEntityId, recipeMap, renderer, tier, true);
        this.outputAmount = outputAmount;
        initializeInventory();
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new NotifiableItemStackHandler(outputAmount, this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMacerator(metaTileEntityId, workable.getRecipeType(), outputAmount, renderer, getTier());
    }

    @Override
    public int getItemOutputLimit() {
        return outputAmount;
    }
}

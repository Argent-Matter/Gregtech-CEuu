package net.nemezanevem.gregtech.api.pipenet.block.material;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.nemezanevem.gregtech.api.pipenet.block.BlockPipe;
import net.nemezanevem.gregtech.api.pipenet.block.IPipeType;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.GtMaterials;
import net.nemezanevem.gregtech.api.unification.material.Material;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static codechicken.lib.util.ClientUtils.getWorld;
import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.UPDATE_PIPE_MATERIAL;

public abstract class TileEntityMaterialPipeBase<PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType> extends TileEntityPipeBase<PipeType, NodeDataType> implements IMaterialPipeTile<PipeType, NodeDataType> {

    private Material pipeMaterial = GtMaterials.Aluminium.get();

    public TileEntityMaterialPipeBase(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    @Override
    public Material getPipeMaterial() {
        return pipeMaterial;
    }

    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType, Material pipeMaterial) {
        super.setPipeData(pipeBlock, pipeType);
        this.pipeMaterial = pipeMaterial;
        if (!getWorld().isClientSide) {
            writeCustomData(UPDATE_PIPE_MATERIAL, this::writePipeMaterial);
        }
    }

    @Override
    public void setPipeData(BlockPipe<PipeType, NodeDataType, ?> pipeBlock, PipeType pipeType) {
        throw new UnsupportedOperationException("Unsupported for TileEntityMaterialMaterialPipeBase");
    }

    @Override
    public int getDefaultPaintingColor() {
        return pipeMaterial == null ? super.getDefaultPaintingColor() : pipeMaterial.getMaterialRGB();
    }

    @Override
    public void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity) {
        super.transferDataFrom(tileEntity);
        this.pipeMaterial = ((IMaterialPipeTile<PipeType, NodeDataType>) tileEntity).getPipeMaterial();
    }

    @Nonnull
    @Override
    public CompoundTag writeToNBT(@Nonnull CompoundTag compound) {
        super.writeToNBT(compound);
        compound.putString("PipeMaterial", pipeMaterial.toString());
        return compound;
    }

    @Override
    public void readFromNBT(@Nonnull CompoundTag compound) {
        super.readFromNBT(compound);
        this.pipeMaterial = MaterialRegistry.MATERIALS_BUILTIN.get().getValue(new ResourceLocation(compound.getString("PipeMaterial")));
        if (this.pipeMaterial == null) {
            this.pipeMaterial = GtMaterials.Aluminium.get(); // fallback
        }
    }

    private void writePipeMaterial(FriendlyByteBuf buf) {
        buf.writeRegistryId(MaterialRegistry.MATERIALS_BUILTIN.get(), pipeMaterial);
    }

    private void readPipeMaterial(FriendlyByteBuf buf) {
        this.pipeMaterial = buf.readRegistryId();
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeRegistryId(MaterialRegistry.MATERIALS_BUILTIN.get(), pipeMaterial);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.pipeMaterial = buf.readRegistryId();
    }

    @Override
    public void receiveCustomData(int discriminator, FriendlyByteBuf buf) {
        super.receiveCustomData(discriminator, buf);
        if (discriminator == UPDATE_PIPE_MATERIAL) {
            readPipeMaterial(buf);
            scheduleChunkForRenderUpdate();
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        return super.getCapability(cap);
    }
}

package net.nemezanevem.gregtech.common.pipelike.fluidpipe;

import com.google.common.base.Preconditions;
import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidPipeProperty;
import gregtech.api.util.EntityDamageUtil;
import gregtech.client.renderer.pipe.FluidPipeRenderer;
import gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipeTickable;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.nemezanevem.gregtech.api.pipenet.block.material.BlockMaterialPipe;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.FluidPipeProperty;
import net.nemezanevem.gregtech.common.pipelike.fluidpipe.net.WorldFluidPipeNet;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

public class BlockFluidPipe extends BlockMaterialPipe<FluidPipeType, FluidPipeProperty, WorldFluidPipeNet> {

    private final SortedMap<Material, FluidPipeProperty> enabledMaterials = new TreeMap<>();

    public BlockFluidPipe(FluidPipeType pipeType) {
        super(pipeType);
    }

    public void addPipeMaterial(Material material, FluidPipeProperty fluidPipeProperties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(fluidPipeProperties, "material %s fluidPipeProperties was null", material);
        Preconditions.checkArgument(MaterialRegistry.MATERIALS_BUILTIN.get().getKey(material) != null, "material %s is not registered", material);
        this.enabledMaterials.put(material, fluidPipeProperties);
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public Class<FluidPipeType> getPipeTypeClass() {
        return FluidPipeType.class;
    }

    @Override
    public WorldFluidPipeNet getWorldPipeNet(Level world) {
        return WorldFluidPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected FluidPipeProperty createProperties(FluidPipeType fluidPipeType, Material material) {
        return fluidPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @Override
    protected FluidPipeProperty getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        for (Material material : enabledMaterials.keySet()) {
            pItems.add(getItem(material));
        }
    }

    @Override
    public void breakBlock(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        super.breakBlock(worldIn, pos, state);
        //TODO insert to neighbours
    }

    @Override
    public boolean canPipesConnect(IPipeTile<FluidPipeType, FluidPipeProperty> selfTile, Direction side, IPipeTile<FluidPipeType, FluidPipeProperty> sideTile) {
        return selfTile instanceof TileEntityFluidPipe && sideTile instanceof TileEntityFluidPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<FluidPipeType, FluidPipeProperty> selfTile, Direction side, BlockEntity tile) {
        return tile != null && tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getHeldItemMainhand();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockFluidPipe;
    }

    @Override
    public void onEntityCollision(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Entity entityIn) {
        if (worldIn.isClientSide) return;
        TileEntityFluidPipe pipe = (TileEntityFluidPipe) getPipeTileEntity(worldIn, pos);
        if (pipe instanceof TileEntityFluidPipeTickable && pipe.getFrameMaterial() == null && ((TileEntityFluidPipeTickable) pipe).getOffsetTimer() % 10 == 0) {
            if (entityIn instanceof LivingEntity) {
                if (((TileEntityFluidPipeTickable) pipe).getFluidTanks().length > 1) {
                    // apply temperature damage for the hottest and coldest pipe (multi fluid pipes)
                    int maxTemperature = Integer.MIN_VALUE;
                    int minTemperature = Integer.MAX_VALUE;
                    for (FluidTank tank : ((TileEntityFluidPipeTickable) pipe).getFluidTanks()) {
                        if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                            maxTemperature = Math.max(maxTemperature, tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                            minTemperature = Math.min(minTemperature, tank.getFluid().getFluid().getTemperature(tank.getFluid()));
                        }
                    }
                    if (maxTemperature != Integer.MIN_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage((LivingEntity) entityIn, maxTemperature, 1.0F, 5);
                    }
                    if (minTemperature != Integer.MAX_VALUE) {
                        EntityDamageUtil.applyTemperatureDamage((LivingEntity) entityIn, minTemperature, 1.0F, 5);
                    }
                } else {
                    FluidTank tank = ((TileEntityFluidPipeTickable) pipe).getFluidTanks()[0];
                    if (tank.getFluid() != null && tank.getFluid().amount > 0) {
                        // Apply temperature damage for the pipe (single fluid pipes)
                        EntityDamageUtil.applyTemperatureDamage((LivingEntity) entityIn, tank.getFluid().getFluid().getTemperature(), 1.0F, 5);
                    }
                }
            }
        }
    }

    @Override
    public TileEntityPipeBase<FluidPipeType, FluidPipeProperty> createNewTileEntity(boolean supportsTicking, BlockPos pPos, BlockState pState) {
        return new TileEntityFluidPipeTickable(); // fluid pipes are always ticking
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public EnumBlockRenderType getRenderType(@Nonnull BlockState state) {
        return FluidPipeRenderer.INSTANCE.getBlockRenderType();
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(Level world, BlockPos blockPos) {
        return FluidPipeRenderer.INSTANCE.getParticleTexture((IPipeTile<?, ?>) world.getBlockEntity(blockPos));
    }
}

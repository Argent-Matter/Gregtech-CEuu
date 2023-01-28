package net.nemezanevem.gregtech.common.pipelike.cable;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.api.capability.GregtechCapabilities;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.api.pipenet.block.material.BlockMaterialPipe;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.WireProperty;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.pipelike.cable.net.WorldENet;
import net.nemezanevem.gregtech.common.pipelike.cable.tile.TileEntityCable;
import net.nemezanevem.gregtech.common.pipelike.cable.tile.TileEntityCableTickable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BlockCable extends BlockMaterialPipe<Insulation, WireProperty, WorldENet> implements EntityBlock {

    private final Map<Material, WireProperty> enabledMaterials = new TreeMap<>();

    public BlockCable(Insulation cableType) {
        super(cableType);
    }

    public void addCableMaterial(Material material, WireProperty wireProperties) {
        Preconditions.checkNotNull(material, "material was null");
        Preconditions.checkNotNull(wireProperties, "material %s wireProperties was null", material);
        Preconditions.checkArgument(MaterialRegistry.MATERIALS_BUILTIN.get().getKey(material) != null, "material %s is not registered", material);
        if (!pipeType.orePrefix.isIgnored(material)) {
            this.enabledMaterials.put(material, wireProperties);
        }
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public Class<Insulation> getPipeTypeClass() {
        return Insulation.class;
    }

    @Override
    protected WireProperty createProperties(Insulation insulation, Material material) {
        return insulation.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    @Override
    protected WireProperty getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public WorldENet getWorldPipeNet(Level world) {
        return WorldENet.getWorldENet(world);
    }

    @Override
    protected boolean isPipeTool(@Nonnull ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClass.WIRE_CUTTER);
    }

    @Override
    public int getLightValue(BlockState state, BlockGetter world, BlockPos pos) {
        BlockEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEntityCable cable) {
            int temp = cable.getTemperature();
            // max light at 5000 K
            // min light at 500 K
            if(temp >= 5000) {
                return 15;
            }
            if (temp > 500) {
                return (temp - 500) * 15 / (4500);
            }
        }
        return 0;
    }



    @Override
    public void breakBlock(@Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state) {
        if (worldIn.isClientSide) {
            TileEntityCable cable = (TileEntityCable) getPipeTileEntity(worldIn, pos);
            cable.killParticle();
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean canPipesConnect(IPipeTile<Insulation, WireProperty> selfTile, Direction side, IPipeTile<Insulation, WireProperty> sideTile) {
        return selfTile instanceof TileEntityCable && sideTile instanceof TileEntityCable;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<Insulation, WireProperty> selfTile, Direction side, BlockEntity tile) {
        return tile != null && tile.getCapability(GregtechCapabilities.CAPABILITY_ENERGY_CONTAINER, side.getOpposite()) != null;
    }

    @Override
    public boolean isHoldingPipe(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockCable;
    }

    @Override
    public boolean hasPipeCollisionChangingItem(BlockGetter world, BlockPos pos, ItemStack stack) {
        return ToolHelper.isTool(stack, ToolClass.WIRE_CUTTER) ||
                Util.isCoverBehaviorItem(stack, () -> hasCover(getPipeTileEntity(world, pos)),
                        coverDef -> ICoverable.canPlaceCover(coverDef, getPipeTileEntity(world, pos).getCoverableImplementation()));
    }

    @Override
    public void onEntityCollision(Level worldIn, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull Entity entityIn) {
        if (worldIn.isClientSide) return;
        Insulation insulation = getPipeTileEntity(worldIn, pos).getPipeType();
        if (insulation.insulationLevel == -1 && entityIn instanceof LivingEntity living) {
            TileEntityCable cable = (TileEntityCable) getPipeTileEntity(worldIn, pos);
            if (cable != null && cable.getFrameMaterial() == null && cable.getNodeData().getLossPerBlock() > 0) {
                long voltage = cable.getCurrentMaxVoltage();
                double amperage = cable.getAverageAmperage();
                if (voltage > 0L && amperage > 0L) {
                    float damageAmount = (float) ((Util.getTierByVoltage(voltage) + 1) * amperage * 4);
                    living.hurt(DamageSources.getElectricDamage(), damageAmount);
                    if (living instanceof ServerPlayer serverPlayer) {
                        AdvancementTriggers.ELECTROCUTION_DEATH.trigger(serverPlayer);
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("deprecation")
    public RenderType getRenderType(@Nonnull BlockState state) {
        return CableRenderer.INSTANCE.getBlockRenderType();
    }

    @Override
    public TileEntityPipeBase<Insulation, WireProperty> createNewTileEntity(boolean supportsTicking) {
        return supportsTicking ? new TileEntityCableTickable() : new TileEntityCable();
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(Level world, BlockPos blockPos) {
        return CableRenderer.INSTANCE.getParticleTexture((TileEntityCable) world.getBlockEntity(blockPos));
    }
}

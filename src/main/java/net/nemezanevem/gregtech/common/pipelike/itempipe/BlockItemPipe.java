package net.nemezanevem.gregtech.common.pipelike.itempipe;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Pair;
import gregtech.api.GregTechAPI;
import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.ItemPipeProperty;
import gregtech.client.renderer.pipe.ItemPipeRenderer;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.Player;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.nemezanevem.gregtech.api.pipenet.block.material.BlockMaterialPipe;
import net.nemezanevem.gregtech.api.pipenet.tile.IPipeTile;
import net.nemezanevem.gregtech.api.pipenet.tile.TileEntityPipeBase;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.ItemPipeProperty;
import net.nemezanevem.gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import net.nemezanevem.gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.nemezanevem.gregtech.common.pipelike.itempipe.tile.TileEntityItemPipeTickable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockItemPipe extends BlockMaterialPipe<ItemPipeType, ItemPipeProperty, WorldItemPipeNet> {

    private final Map<Material, ItemPipeProperty> enabledMaterials = new HashMap<>();

    public BlockItemPipe(ItemPipeType itemPipeType) {
        super(itemPipeType);
        setHarvestLevel(ToolClasses.WRENCH, 1);
    }

    public void addPipeMaterial(Material material, ItemPipeProperty properties) {
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(properties, "material %s itemPipeProperties was null", material);
        Preconditions.checkArgument(MaterialRegistry.MATERIALS_BUILTIN.get().getKey(material) != null, "material %s is not registered", material);
        this.enabledMaterials.put(material, properties);
    }

    @Override
    public TileEntityPipeBase<ItemPipeType, ItemPipeProperty> createNewTileEntity(boolean supportsTicking, BlockPos pPos, BlockState pState) {
        return supportsTicking ? new TileEntityItemPipeTickable() : new TileEntityItemPipe();
    }

    @Override
    public Class<ItemPipeType> getPipeTypeClass() {
        return ItemPipeType.class;
    }

    @Override
    protected ItemPipeProperty getFallbackType() {
        return enabledMaterials.values().iterator().next();
    }

    @Override
    public WorldItemPipeNet getWorldPipeNet(Level world) {
        return WorldItemPipeNet.getWorldPipeNet(world);
    }

    @Override
    protected Pair<TextureAtlasSprite, Integer> getParticleTexture(Level world, BlockPos blockPos) {
        return ItemPipeRenderer.INSTANCE.getParticleTexture((TileEntityItemPipe) world.getBlockEntity(blockPos));
    }

    @Override
    protected ItemPipeProperty createProperties(ItemPipeType itemPipeType, Material material) {
        return itemPipeType.modifyProperties(enabledMaterials.getOrDefault(material, getFallbackType()));
    }

    public Collection<Material> getEnabledMaterials() {
        return Collections.unmodifiableSet(enabledMaterials.keySet());
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        for (Material material : enabledMaterials.keySet()) {
            pItems.add(getItem(material));
        }
    }

    @Override
    public ItemPipeType getItemPipeType(ItemStack itemStack) {
        return super.getItemPipeType(itemStack);
    }

    @Override
    public boolean canPipesConnect(IPipeTile<ItemPipeType, ItemPipeProperty> selfTile, Direction side, IPipeTile<ItemPipeType, ItemPipeProperty> sideTile) {
        return selfTile instanceof TileEntityItemPipe && sideTile instanceof TileEntityItemPipe;
    }

    @Override
    public boolean canPipeConnectToBlock(IPipeTile<ItemPipeType, ItemPipeProperty> selfTile, Direction side, BlockEntity tile) {
        return tile != null && tile.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).isPresent();
    }

    @Override
    public boolean isHoldingPipe(Player player) {
        if (player == null) {
            return false;
        }
        ItemStack stack = player.getMainHandItem();
        return stack != ItemStack.EMPTY && stack.getItem() instanceof ItemBlockItemPipe;
    }

    @Override
    @Nonnull
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("deprecation")
    public RenderType getRenderType(@Nonnull BlockState state) {
        return ItemPipeRenderer.INSTANCE.getBlockRenderType();
    }


}

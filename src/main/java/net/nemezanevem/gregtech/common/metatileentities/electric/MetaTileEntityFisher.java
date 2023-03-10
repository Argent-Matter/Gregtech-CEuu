package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.SlotWidget;
import net.nemezanevem.gregtech.api.util.GTTransferUtils;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class MetaTileEntityFisher extends TieredMetaTileEntity {

    private static final int WATER_CHECK_SIZE = 25;

    private final int inventorySize;
    private final long fishingTicks;
    private final long energyAmountPerFish;

    public MetaTileEntityFisher(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        this.inventorySize = (tier + 1) * (tier + 1);
        this.fishingTicks = 1000 - tier * 200L;
        this.energyAmountPerFish = GTValues.V[tier];
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFisher(metaTileEntityId, getTier());
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        int rowSize = (int) Math.sqrt(inventorySize);

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176,
                18 + 18 * rowSize + 94)
                .label(10, 5, getMetaFullName())
                .widget(new SlotWidget(importItems, 0, 18, 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT, GuiTextures.STRING_SLOT_OVERLAY));

        for (int y = 0; y < rowSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                int index = y * rowSize + x;
                builder.widget(new SlotWidget(exportItems, index, 89 - rowSize * 9 + x * 18, 18 + y * 18, true, false)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        builder.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 18 + 18 * rowSize + 12);
        return builder.build(getHolder(), entityPlayer);
    }

    @Override
    public void tick() {
        super.tick();
        ItemStack baitStack = importItems.getStackInSlot(0);
        if (!getWorld().isClientSide && energyContainer.getEnergyStored() >= energyAmountPerFish && getOffsetTimer() % fishingTicks == 0L && !baitStack.isEmpty()) {
            ServerLevel world = (ServerLevel) this.getWorld();
            int waterCount = 0;
            int edgeSize = (int) Math.sqrt(WATER_CHECK_SIZE);
            for (int x = 0; x < edgeSize; x++) {
                for (int z = 0; z < edgeSize; z++) {
                    BlockPos waterCheckPos = getPos().below().offset(x - edgeSize / 2, 0, z - edgeSize / 2);
                    if (world.getBlockState(waterCheckPos).getBlock() instanceof LiquidBlock &&
                            world.getBlockState(waterCheckPos).getMaterial() == Material.WATER) {
                        waterCount++;
                    }
                }
            }
            if (waterCount == WATER_CHECK_SIZE) {
                LootTable table = world.getServer().getLootTables().get(BuiltInLootTables.FISHING);
                NonNullList<ItemStack> itemStacks = NonNullList.create();
                itemStacks.addAll(table.getRandomItems(new LootContext.Builder(world).create(LootContextParamSet.builder().build())));
                if (GTTransferUtils.addItemsToItemHandler(exportItems, true, itemStacks)) {
                    GTTransferUtils.addItemsToItemHandler(exportItems, false, itemStacks);
                    energyContainer.removeEnergy(energyAmountPerFish);
                    baitStack.shrink(1);
                }
            }
        }
        if (!getWorld().isClientSide && getOffsetTimer() % 5 == 0) {
            pushItemsIntoNearbyHandlers(getFrontFacing());
        }
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(1) {
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                if (OreDictUnifier.getOreDictionaryNames(stack).contains("string")) {
                    return super.insertItem(slot, stack, simulate);
                }
                return stack;
            }
        };
    }

    @Override
    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(inventorySize);
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        Textures.SCREEN.renderSided(Direction.UP, renderState, translation, pipeline);
        Textures.PIPE_OUT_OVERLAY.renderSided(getFrontFacing(), renderState, translation, pipeline);
        Textures.PIPE_IN_OVERLAY.renderSided(Direction.DOWN, renderState, translation, pipeline);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable Level player, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.machine.fisher.tooltip"));
        tooltip.add(Component.translatable("gregtech.machine.fisher.speed", fishingTicks));
        tooltip.add(Component.translatable("gregtech.machine.fisher.requirement", (int) Math.sqrt(WATER_CHECK_SIZE), (int) Math.sqrt(WATER_CHECK_SIZE)));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.voltage_in", energyContainer.getInputVoltage(), GTValues.VNF[getTier()]));
        tooltip.add(Component.translatable("gregtech.universal.tooltip.energy_storage_capacity", energyContainer.getEnergyCapacity()));
        super.addInformation(stack, player, tooltip, advanced);
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}

package net.nemezanevem.gregtech.common.pipelike.cable;

import gregtech.api.GTValues;
import gregtech.api.pipenet.block.material.BlockMaterialPipe;
import gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import gregtech.api.unification.material.properties.WireProperty;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.World;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.pipenet.block.material.BlockMaterialPipe;
import net.nemezanevem.gregtech.api.pipenet.block.material.ItemBlockMaterialPipe;
import net.nemezanevem.gregtech.api.unification.material.properties.properties.WireProperty;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.common.ConfigHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockCable extends ItemBlockMaterialPipe<Insulation, WireProperty> {

    public ItemBlockCable(BlockCable block) {
        super(block);
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn) {
        WireProperty wireProperties = blockPipe.createItemProperties(stack);
        int tier = Util.getTierByVoltage(wireProperties.getVoltage());
        if (wireProperties.isSuperconductor()) tooltip.add(Component.translatable("gregtech.cable.superconductor", GTValues.VN[tier]));
        tooltip.add(Component.translatable("gregtech.cable.voltage", wireProperties.getVoltage(), GTValues.VNF[tier]));
        tooltip.add(Component.translatable("gregtech.cable.amperage", wireProperties.getAmperage()));
        tooltip.add(Component.translatable("gregtech.cable.loss_per_block", wireProperties.getLossPerBlock()));

        if (ConfigHolder.misc.debug) {
            tooltip.add("MetaItem Id: " + ((BlockMaterialPipe<?, ?, ?>)blockPipe).getPrefix() + ((BlockMaterialPipe<?, ?, ?>)blockPipe).getItemMaterial(stack).toCamelCaseString());
        }
    }
}

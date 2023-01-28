package net.nemezanevem.gregtech.common.block;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.block.VariantBlock;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockMachineCasing extends VariantBlock<BlockMachineCasing.MachineCasingType> {

    public BlockMachineCasing() {
        super(BlockBehaviour.Properties.of(Material.METAL).sound(SoundType.METAL).strength(4.0f, 8.0f).isValidSpawn(((pState, pLevel, pPos, pValue) -> false)));
        registerDefaultState(getState(MachineCasingType.ULV));
    }

    public enum MachineCasingType implements StringRepresentable {

        //Voltage-tiered casings
        ULV(makeName(GTValues.VOLTAGE_NAMES[0])),
        LV(makeName(GTValues.VOLTAGE_NAMES[1])),
        MV(makeName(GTValues.VOLTAGE_NAMES[2])),
        HV(makeName(GTValues.VOLTAGE_NAMES[3])),
        EV(makeName(GTValues.VOLTAGE_NAMES[4])),
        IV(makeName(GTValues.VOLTAGE_NAMES[5])),
        LuV(makeName(GTValues.VOLTAGE_NAMES[6])),
        ZPM(makeName(GTValues.VOLTAGE_NAMES[7])),
        UV(makeName(GTValues.VOLTAGE_NAMES[8])),
        UHV(makeName(GTValues.VOLTAGE_NAMES[9])),
        UEV(makeName(GTValues.VOLTAGE_NAMES[10])),
        UIV(makeName(GTValues.VOLTAGE_NAMES[11])),
        UXV(makeName(GTValues.VOLTAGE_NAMES[12])),
        OpV(makeName(GTValues.VOLTAGE_NAMES[13])),
        MAX(makeName(GTValues.VOLTAGE_NAMES[14]));

        private final String name;

        MachineCasingType(String name) {
            this.name = name;
        }

        @Override
        @Nonnull
        public String getSerializedName() {
            return this.name;
        }

        private static String makeName(String voltageName) {
            return String.join("_", voltageName.toLowerCase().split(" "));
        }
    }
}

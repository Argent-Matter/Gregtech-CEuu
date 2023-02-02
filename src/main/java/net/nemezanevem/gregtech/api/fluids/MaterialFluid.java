package net.nemezanevem.gregtech.api.fluids;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.util.FluidTooltipUtil;
import net.nemezanevem.gregtech.api.util.Util;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class MaterialFluid extends FluidType {

    private final Material material;
    private final FluidType fluidType;

    private final ResourceLocation textureLoc;

    public MaterialFluid(String fluidName, @Nonnull Material material, @Nonnull FluidType fluidType, ResourceLocation texture) {
        super(FluidType.Properties.create().descriptionId(fluidName));
        this.material = material;
        this.fluidType = fluidType;
        this.textureLoc = texture;
    }

    public void registerFluidTooltip() {
        FluidTooltipUtil.registerTooltip(this, FluidTooltipUtil.getMaterialTooltip(material, getFluidType().getTemperature(), fluidType.equals(GTFluidTypes.PLASMA)));
    }

    @Nonnull
    public Material getMaterial() {
        return this.material;
    }

    @Nonnull
    public FluidType getFluidType() {
        return this.fluidType;
    }

    @Override
    public String getDescriptionId() {
        return material.getUnlocalizedName();
    }

    @Override
    public Component getDescription(FluidStack stack) {
        Component localizedName = Component.translatable(getDescriptionId());
        if (fluidType != null) {
            return Component.translatable(fluidType.getDescriptionId(), localizedName);
        }
        return localizedName;
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
    {
        consumer.accept(new IClientFluidTypeExtensions()
        {
            @Override
            public ResourceLocation getStillTexture() {
                return MaterialFluid.this.textureLoc;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return MaterialFluid.this.textureLoc;
            }

            @Override
            public int getTintColor() {
                return Util.convertRGBtoOpaqueRGBA_MC(MaterialFluid.this.material.getMaterialRGB());
            }
        });
    }
}

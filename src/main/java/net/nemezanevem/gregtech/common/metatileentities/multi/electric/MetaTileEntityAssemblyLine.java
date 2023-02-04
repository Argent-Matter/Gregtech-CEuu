package net.nemezanevem.gregtech.common.metatileentities.multi.electric;

import codechicken.lib.vec.Vector3;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.blockentity.multiblock.GtMultiblockAbilities;
import net.nemezanevem.gregtech.api.blockentity.multiblock.IMultiblockPart;
import net.nemezanevem.gregtech.api.blockentity.multiblock.RecipeTypeMultiblockController;
import net.nemezanevem.gregtech.api.capability.GregtechDataCodes;
import net.nemezanevem.gregtech.api.pattern.BlockPattern;
import net.nemezanevem.gregtech.api.pattern.FactoryBlockPattern;
import net.nemezanevem.gregtech.api.recipe.GtRecipeTypes;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.particle.GTParticleManager;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;
import net.nemezanevem.gregtech.common.block.BlockGlassCasing;
import net.nemezanevem.gregtech.common.block.BlockMetalCasing;
import net.nemezanevem.gregtech.common.block.BlockMultiblockCasing;
import net.nemezanevem.gregtech.common.block.MetaBlocks;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntities;

import static net.nemezanevem.gregtech.api.util.RelativeDirection.*;

;

public class MetaTileEntityAssemblyLine extends RecipeTypeMultiblockController {

    private static final ResourceLocation LASER_LOCATION = new ResourceLocation(GregTech.MODID,"textures/fx/laser/laser.png");
    private static final ResourceLocation LASER_HEAD_LOCATION = new ResourceLocation(GregTech.MODID,"textures/fx/laser/laser_start.png");

    public MetaTileEntityAssemblyLine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GtRecipeTypes.ASSEMBLY_LINE_RECIPES.get());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAssemblyLine(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(FRONT, UP, RIGHT)
                .aisle("FIF", "RTR", "SAG", "#Y#")
                .aisle("FIF", "RTR", "GAG", "#Y#").setRepeatable(3, 15)
                .aisle("FOF", "RTR", "GAG", "#Y#")
                .where('S', selfPredicate())
                .where('F', states(getCasingState())
                        .or(autoAbilities(false, true, false, false, false, false, false))
                        .or(abilities(GtMultiblockAbilities.IMPORT_FLUIDS.get()).setMaxGlobalLimited(4)))
                .where('O', abilities(GtMultiblockAbilities.EXPORT_ITEMS.get()).addTooltips(Component.translatable("gregtech.multiblock.pattern.location_end")))
                .where('Y', states(getCasingState()).or(abilities(GtMultiblockAbilities.INPUT_ENERGY.get()).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                .where('I', metaTileEntities(MetaTileEntities.ITEM_IMPORT_BUS[0]))
                .where('G', states(MetaBlocks.MULTIBLOCK_CASING.get().getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)))
                .where('A', states(MetaBlocks.MULTIBLOCK_CASING.get().getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_CONTROL)))
                .where('R', states(MetaBlocks.TRANSPARENT_CASING.get().getState(BlockGlassCasing.CasingType.LAMINATED_GLASS)))
                .where('T', states(MetaBlocks.MULTIBLOCK_CASING.get().getState(BlockMultiblockCasing.MultiblockCasingType.ASSEMBLY_LINE_CASING)))
                .where('#', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected BlockState getCasingState() {
        return MetaBlocks.METAL_CASING.get().getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private int beamCount;
    private GTLaserBeamParticle[][] beamParticles;

    @Override
    public void tick() {
        super.tick();
        if(ConfigHolder.ClientConfig.ShaderOptions.assemblyLineParticles) {
            if (getRecipeTypeWorkable().isWorking()) {
                int maxBeams = getAbilities(GtMultiblockAbilities.IMPORT_ITEMS.get()).size() + 1;
                int maxProgress = getRecipeTypeWorkable().getMaxProgress();

                // Each beam should be visible for an equal amount of time, which is derived from the maximum number of
                // beams and the maximum progress in the recipe.
                int beamTime = Math.max(1, maxProgress / maxBeams);

                int currentBeamCount = Math.min(maxBeams, getRecipeTypeWorkable().getProgress() / beamTime);

                if (currentBeamCount != beamCount) {
                    beamCount = currentBeamCount;
                    writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
                }
            }
            else if (beamCount != 0) {
                beamCount = 0;
                writeCustomData(GregtechDataCodes.UPDATE_PARTICLE, this::writeParticles);
            }
        }
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        writeParticles(buf);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        readParticles(buf);
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        if (dataId == GregtechDataCodes.UPDATE_PARTICLE) {
            readParticles(buf);
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (getWorld().isClientSide && beamParticles != null) {
            for (GTLaserBeamParticle[] particle : beamParticles) {
                if (particle[0] != null) {
                    particle[0].setExpired();
                    particle[1].setExpired();
                }
            }
        }
        beamParticles = null;
    }

    private void writeParticles(FriendlyByteBuf buf) {
        buf.writeVarInt(beamCount);
    }

    private void readParticles(FriendlyByteBuf buf) {
        beamCount = buf.readVarInt();
        if (beamParticles == null) {
            beamParticles = new GTLaserBeamParticle[17][2];
        }
        BlockPos.MutableBlockPos pos = getPos().mutable();
        for (int i = 0; i < beamParticles.length; i++) {
            GTLaserBeamParticle particle = beamParticles[i][0];
            if (i < beamCount && particle == null) {
                pos.set(getPos());
                Vector3 startPos = new Vector3().add(
                                pos.move(getFrontFacing().getClockWise().getOpposite(), i))
                        .add(0.5, 0, 0.5);
                Vector3 endPos = startPos.copy().subtract(0, 1, 0);

                beamParticles[i][0] = createALParticles(getWorld(), startPos, endPos);

                pos.set(getPos());
                startPos = new Vector3().add(
                                pos.move(getFrontFacing().getClockWise().getOpposite(), i).move(getFrontFacing().getOpposite(), 2))
                        .add(0.5, 0, 0.5);
                endPos = startPos.copy().subtract(0, 1, 0);
                beamParticles[i][1] = createALParticles(getWorld(), startPos, endPos);

                // Don't forget to add particles
                GTParticleManager.INSTANCE.addEffect(beamParticles[i][0], beamParticles[i][1]);

            } else if (i >= beamCount && particle != null) {
                particle.setExpired();
                beamParticles[i][0] = null;
                beamParticles[i][1].setExpired();
                beamParticles[i][1] = null;
            }
        }
    }

    private GTLaserBeamParticle createALParticles(Level world, Vector3 startPos, Vector3 endPos) {
        GTLaserBeamParticle particle =  new GTLaserBeamParticle(world, startPos, endPos)
                .setBody(LASER_LOCATION)
                .setBeamHeight(0.125f)
                // Try commenting or adjusting on the next four lines to see what happens
                .setDoubleVertical(true)
                .setHead(LASER_HEAD_LOCATION)
                .setHeadWidth(0.1f)
                .setEmit(0.2f);

        particle.setOnUpdate(p -> {
            if (!isValid() || !Util.isPosChunkLoaded(getWorld(), getPos()) || getWorld().getBlockEntity(getPos()) != this.getHolder()) {
                p.setExpired();
            }
        });

        return particle;
    }
}

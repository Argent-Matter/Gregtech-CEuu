package net.nemezanevem.gregtech.common.metatileentities.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonChargePlayerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.phys.Vec3;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.TieredMetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.IS_WORKING;

public class MetaTileEntityMagicEnergyAbsorber extends TieredMetaTileEntity {

    private final IntList connectedCrystalsIds = new IntArrayList();
    private boolean hasDragonEggAmplifier = false;
    private boolean isActive = false;

    public MetaTileEntityMagicEnergyAbsorber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, GTValues.EV);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMagicEnergyAbsorber(metaTileEntityId);
    }

    private ICubeRenderer getRenderer() {
        return isActive ? Textures.MAGIC_ENERGY_ABSORBER_ACTIVE : Textures.MAGIC_ENERGY_ABSORBER;
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getRenderer().getParticleSprite(), getPaintingColorForRendering());
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        IVertexOperation[] colouredPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        getRenderer().render(renderState, translation, colouredPipeline);
    }

    @Override
    public void tick() {
        super.tick();
        if (getWorld().isClientSide)
            return;
        if (!(getWorld().dimension() == Level.END)) {
            return; //don't try to do anything outside end dimension
        }
        if (getOffsetTimer() % 20 == 0 || isFirstTick()) {
            updateDragonEggStatus();
        }
        if (getOffsetTimer() % 200 == 0 || isFirstTick()) {
            updateConnectedCrystals();
        }
        int totalEnergyGeneration = 0;
        for (int connectedCrystalId : connectedCrystalsIds) {
            //since we don't check quite often, check twice before outputting energy
            if (getWorld().getEntity(connectedCrystalId) instanceof EndCrystal) {
                totalEnergyGeneration += hasDragonEggAmplifier ? 128 : 32;
            }
        }
        if (totalEnergyGeneration > 0) {
            energyContainer.changeEnergy(totalEnergyGeneration);
        }
        setActive(totalEnergyGeneration > 0);
    }

    private void setActive(boolean isActive) {
        if (this.isActive != isActive) {
            this.isActive = isActive;
            if (!getWorld().isClientSide) {
                writeCustomData(IS_WORKING, w -> w.writeBoolean(isActive));
            }
        }
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == IS_WORKING) {
            this.isActive = buf.readBoolean();
        }
    }

    @Override
    public void writeInitialSyncData(FriendlyByteBuf buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(isActive);
    }

    @Override
    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        super.receiveInitialSyncData(buf);
        this.isActive = buf.readBoolean();
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isClientSide) {
            resetConnectedEnderCrystals();
        }
    }

    @Override
    protected boolean isEnergyEmitter() {
        return true;
    }

    private void updateConnectedCrystals() {
        this.connectedCrystalsIds.clear();
        final double maxDistance = 64 * 64;
        List<EndCrystal> enderCrystals = SpikeFeature.getSpikesForLevel((ServerLevel) getWorld()).stream()
                .flatMap(endSpike -> getWorld().getEntitiesOfClass(EndCrystal.class, endSpike.getTopBoundingBox()).stream())
                .filter(crystal -> crystal.distanceToSqr(Vec3.atCenterOf(getPos())) < maxDistance).toList();

        for (EndCrystal entityEnderCrystal : enderCrystals) {
            BlockPos beamTarget = entityEnderCrystal.getBeamTarget();
            if (beamTarget == null) {
                //if beam target is null, set ourselves as beam target
                entityEnderCrystal.setBeamTarget(getPos());
                this.connectedCrystalsIds.add(entityEnderCrystal.getId());
            } else if (beamTarget.equals(getPos())) {
                //if beam target is ourselves, just add it to list
                this.connectedCrystalsIds.add(entityEnderCrystal.getId());
            }
        }

        for (EnderDragon entityDragon : ((ServerLevel) getWorld()).getEntities(EntityTypeTest.forClass(EnderDragon.class), EntitySelector.ENTITY_STILL_ALIVE)) {
            if (entityDragon.nearestCrystal != null && connectedCrystalsIds.contains(entityDragon.nearestCrystal.getId())) {
                //if dragon is healing from crystal we draw energy from, reset it's healing crystal
                entityDragon.nearestCrystal = null;
                //if dragon is holding pattern, than deal damage and set it's phase to attack ourselves
                if (entityDragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.HOLDING_PATTERN) {
                    entityDragon.hurt(DamageSource.explosion((LivingEntity) null), 10.0f);
                    entityDragon.getPhaseManager().setPhase(EnderDragonPhase.CHARGING_PLAYER);
                    ((DragonChargePlayerPhase) entityDragon.getPhaseManager().getCurrentPhase()).setTarget(Vec3.atCenterOf(getPos()));
                }
            }
        }
    }

    private void resetConnectedEnderCrystals() {
        for (int connectedEnderCrystal : connectedCrystalsIds) {
            EndCrystal entityEnderCrystal = (EndCrystal) getWorld().getEntity(connectedEnderCrystal);
            if (entityEnderCrystal != null && getPos().equals(entityEnderCrystal.getBeamTarget())) {
                //on removal, reset ender crystal beam location so somebody can use it
                entityEnderCrystal.setBeamTarget(null);
            }
        }
        connectedCrystalsIds.clear();
    }

    private void updateDragonEggStatus() {
        BlockState blockState = getWorld().getBlockState(getPos().above());
        this.hasDragonEggAmplifier = blockState.getBlock() instanceof DragonEggBlock;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    protected ModularUI createUI(Player entityPlayer) {
        return null;
    }

    @Override
    public void randomDisplayTick() {
        if (isActive() && !this.hasDragonEggAmplifier) {
            final BlockPos pos = getPos();
            for (int i = 0; i < 4; i++) {
                getWorld().addParticle(ParticleTypes.PORTAL,
                        pos.getX() + 0.5F,
                        pos.getY() + GTValues.RNG.nextFloat(),
                        pos.getZ() + 0.5F,
                        (GTValues.RNG.nextFloat() - 0.5F) * 0.5F,
                        (GTValues.RNG.nextFloat() - 0.5F) * 0.5F,
                        (GTValues.RNG.nextFloat() - 0.5F) * 0.5F);
            }
        }
    }
    @Override
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        tooltip.add(Component.translatable("gregtech.tool_action.screwdriver.access_covers"));
        tooltip.add(Component.translatable("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }
}

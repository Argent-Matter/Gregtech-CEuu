package net.nemezanevem.gregtech.api.tileentity.multiblock;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraftforge.common.capabilities.Capability;
import net.nemezanevem.gregtech.api.capability.IMultiblockController;
import net.nemezanevem.gregtech.api.pattern.TraceabilityPredicate;
import net.nemezanevem.gregtech.api.tileentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.tileentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.util.BlockInfo;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.ICubeRenderer;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class MultiblockControllerBase extends MetaTileEntity implements IMultiblockController {

    public BlockPattern structurePattern;

    private final Map<MultiblockAbility<Object>, List<Object>> multiblockAbilities = new HashMap<>();
    private final List<IMultiblockPart> multiblockParts = new ArrayList<>();
    private boolean structureFormed;

    public MultiblockControllerBase(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public void onPlacement() {
        super.onPlacement();
        reinitializeStructurePattern();
    }

    public void reinitializeStructurePattern() {
        this.structurePattern = createStructurePattern();
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld()..) {
            if (getOffsetTimer() % 20 == 0 || isFirstTick()) {
                checkStructurePattern();
            }
            // DummyWorld is the world for the JEI preview. We do not want to update the Multi in this world,
            // besides initially forming it in checkStructurePattern
            if (isStructureFormed() && !(getWorld() instanceof DummyWorld)) {
                updateFormedValid();
            }
        }
    }

    /**
     * Called when the multiblock is formed and validation predicate is matched
     */
    protected abstract void updateFormedValid();

    /**
     * @return structure pattern of this multiblock
     */
    protected abstract BlockPattern createStructurePattern();

    public abstract ICubeRenderer getBaseTexture(IMultiblockPart sourcePart);

    public boolean shouldRenderOverlay(IMultiblockPart sourcePart) {
        return true;
    }

    /**
     * Override this method to change the Controller overlay
     *
     * @return The overlay to render on the Multiblock Controller
     */
    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_WORKABLE_OVERLAY;
    }

    public TextureAtlasSprite getFrontDefaultTexture() {
        return getFrontOverlay().getParticleSprite();
    }

    public static TraceabilityPredicate tilePredicate(@Nonnull BiFunction<BlockState, MetaTileEntity, Boolean> predicate, @Nullable Supplier<BlockInfo[]> candidates) {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockEntity tileEntity = blockWorldState.getBlockEntity();
            if (!(tileEntity instanceof IGregTechTileEntity gregTechTileEntity))
                return false;
            MetaTileEntity metaTileEntity = gregTechTileEntity.getMetaTileEntity();
            if (predicate.apply(blockWorldState, metaTileEntity)) {
                if (metaTileEntity instanceof IMultiblockPart) {
                    Set<IMultiblockPart> partsFound = blockWorldState.getMatchContext().getOrCreate("MultiblockParts", HashSet::new);
                    partsFound.add((IMultiblockPart) metaTileEntity);
                }
                return true;
            }
            return false;
        }, candidates);
    }

    public static TraceabilityPredicate metaTileEntities(MetaTileEntity... metaTileEntities) {
        ResourceLocation[] ids = Arrays.stream(metaTileEntities).filter(Objects::nonNull).map(tile -> tile.metaTileEntityId).toArray(ResourceLocation[]::new);
        return tilePredicate((state, tile) -> ArrayUtils.contains(ids, tile.metaTileEntityId), getCandidates(metaTileEntities));
    }

    private static Supplier<BlockInfo[]> getCandidates(MetaTileEntity... metaTileEntities){
        return ()->Arrays.stream(metaTileEntities).filter(Objects::nonNull).map(tile -> {
            // TODO
            MetaTileEntityHolder holder = new MetaTileEntityHolder();
            holder.setMetaTileEntity(tile);
            holder.getMetaTileEntity().onPlacement();
            holder.getMetaTileEntity().setFrontFacing(Direction.SOUTH);
            return new BlockInfo(MetaBlocks.MACHINE.getDefaultState(), holder);
        }).toArray(BlockInfo[]::new);
    }

    private static Supplier<BlockInfo[]> getCandidates(BlockState... allowedStates) {
        return () -> Arrays.stream(allowedStates).map(state -> new BlockInfo(state, null)).toArray(BlockInfo[]::new);
    }

    public static TraceabilityPredicate abilities(MultiblockAbility<?>... allowedAbilities) {
        return tilePredicate((state, tile) -> tile instanceof IMultiblockAbilityPart<?> &&
                        ArrayUtils.contains(allowedAbilities, ((IMultiblockAbilityPart<?>) tile).getAbility()),
                getCandidates(Arrays.stream(allowedAbilities).flatMap(ability -> MultiblockAbility.REGISTRY.get(ability).stream()).toArray(MetaTileEntity[]::new)));
    }

    public static TraceabilityPredicate states(BlockState... allowedStates) {
        return new TraceabilityPredicate(blockWorldState -> {
            BlockState state = blockWorldState.getBlockState();
            if (state.getBlock() instanceof VariantActiveBlock) {
                blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            }
            return ArrayUtils.contains(allowedStates, state);
        }, getCandidates(allowedStates));
    }

    /** Use this predicate for Frames in your Multiblock. Allows for Framed Pipes as well as normal Frame blocks. */
    public static TraceabilityPredicate frames(Material... frameMaterials) {
        return states(Arrays.stream(frameMaterials).map(m -> MetaBlocks.FRAMES.get(m).getBlock(m)).toArray(BlockState[]::new))
                .or(new TraceabilityPredicate(blockWorldState -> {
                    TileEntity tileEntity = blockWorldState.getTileEntity();
                    if (!(tileEntity instanceof IPipeTile)) {
                        return false;
                    }
                    IPipeTile<?, ?> pipeTile = (IPipeTile<?, ?>) tileEntity;
                    return ArrayUtils.contains(frameMaterials, pipeTile.getFrameMaterial());
                }));
    }

    public static TraceabilityPredicate blocks(Block... block) {
        return new TraceabilityPredicate(blockWorldState -> ArrayUtils.contains(block, blockWorldState.getBlockState().getBlock()), getCandidates(Arrays.stream(block).map(Block::getDefaultState).toArray(BlockState[]::new)));
    }

    public static TraceabilityPredicate air() {
        return TraceabilityPredicate.AIR;
    }

    public static TraceabilityPredicate any() {
        return TraceabilityPredicate.ANY;
    }

    public static TraceabilityPredicate heatingCoils() {
        return TraceabilityPredicate.HEATING_COILS.get();
    }

    public TraceabilityPredicate selfPredicate() {
        return metaTileEntities(this).setCenter();
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        getBaseTexture(null).render(renderState, translation, ArrayUtils.add(pipeline, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering()))));
    }

    @Override
    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(getBaseTexture(null).getParticleSprite(), getPaintingColorForRendering());
    }

    /**
     * Override to disable Multiblock pattern from being added to Jei
     */
    public boolean shouldShowInJei() {
        return true;
    }

    /**
     * Used if MultiblockPart Abilities need to be sorted a certain way, like
     * Distillation Tower and Assembly Line.
     */
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        return BlockPos::hashCode;
    }

    public void checkStructurePattern() {
        if (structurePattern == null) return;
        PatternMatchContext context = structurePattern.checkPatternFastAt(getWorld(), getPos(), getFrontFacing().getOpposite());
        if (context != null && !structureFormed) {
            Set<IMultiblockPart> rawPartsSet = context.getOrCreate("MultiblockParts", HashSet::new);
            ArrayList<IMultiblockPart> parts = new ArrayList<>(rawPartsSet);
            parts.sort(Comparator.comparing(it -> multiblockPartSorter().apply(((MetaTileEntity) it).getPos())));
            for (IMultiblockPart part : parts) {
                if (part.isAttachedToMultiBlock()) {
                    if (!part.canPartShare()) {
                        return;
                    }
                }
            }
            Map<MultiblockAbility<Object>, List<Object>> abilities = new HashMap<>();
            for (IMultiblockPart multiblockPart : parts) {
                if (multiblockPart instanceof IMultiblockAbilityPart) {
                    IMultiblockAbilityPart<Object> abilityPart = (IMultiblockAbilityPart<Object>) multiblockPart;
                    List<Object> abilityInstancesList = abilities.computeIfAbsent(abilityPart.getAbility(), k -> new ArrayList<>());
                    abilityPart.registerAbilities(abilityInstancesList);
                }
            }
            parts.forEach(part -> part.addToMultiBlock(this));
            this.multiblockParts.addAll(parts);
            this.multiblockAbilities.putAll(abilities);
            this.structureFormed = true;
            writeCustomData(STRUCTURE_FORMED, buf -> buf.writeBoolean(true));
            formStructure(context);
        } else if (context == null && structureFormed) {
            invalidateStructure();
        }
    }

    protected void formStructure(PatternMatchContext context) {
    }

    public void invalidateStructure() {
        this.multiblockParts.forEach(part -> part.removeFromMultiBlock(this));
        this.multiblockAbilities.clear();
        this.multiblockParts.clear();
        this.structureFormed = false;
        writeCustomData(STRUCTURE_FORMED, buf -> buf.writeBoolean(false));
    }

    @Override
    public void onRemoval() {
        super.onRemoval();
        if (!getWorld().isRemote && structureFormed) {
            invalidateStructure();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAbilities(MultiblockAbility<T> ability) {
        List<T> rawList = (List<T>) multiblockAbilities.getOrDefault(ability, Collections.emptyList());
        return Collections.unmodifiableList(rawList);
    }

    public List<IMultiblockPart> getMultiblockParts() {
        return Collections.unmodifiableList(multiblockParts);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.reinitializeStructurePattern();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(structureFormed);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.structureFormed = buf.readBoolean();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == STRUCTURE_FORMED) {
            this.structureFormed = buf.readBoolean();
            if (!structureFormed) {
                GregTechAPI.soundManager.stopTileSound(getPos());
            }
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, Direction side) {
        T result = super.getCapability(capability, side);
        if (result != null)
            return result;
        if (capability == GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER) {
            return GregtechCapabilities.CAPABILITY_MULTIBLOCK_CONTROLLER.cast(this);
        }
        return null;
    }

    public boolean isStructureFormed() {
        return structureFormed;
    }

    @Override
    public void setFrontFacing(Direction frontFacing) {
        super.setFrontFacing(frontFacing);
        if (getWorld() != null && !getWorld().isRemote && structurePattern != null) {
            // clear cache since the cache has no concept of pre-existing facing
            // for the controller block (or any block) in the structure
            structurePattern.clearCache();
            // recheck structure pattern immediately to avoid a slight "lag"
            // on deforming when rotating a multiblock controller
            checkStructurePattern();
        }
    }

    @Override
    public void addToolUsages(ItemStack stack, @Nullable World world, List<String> tooltip, boolean advanced) {
        if (this instanceof IMultipleRecipeMaps) {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.toggle_mode_covers"));
        } else {
            tooltip.add(I18n.format("gregtech.tool_action.screwdriver.access_covers"));
        }
        tooltip.add(I18n.format("gregtech.tool_action.wrench.set_facing"));
        super.addToolUsages(stack, world, tooltip, advanced);
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, Direction facing, CuboidRayTraceResult hitResult) {
        if (super.onRightClick(playerIn, hand, facing, hitResult))
            return true;

        if (this.getWorld().isRemote && !this.isStructureFormed() && playerIn.isSneaking() && playerIn.getHeldItem(hand).isEmpty()) {
            MultiblockPreviewRenderer.renderMultiBlockPreview(this, 60000);
            return true;
        }
        return false;
    }

    public List<MultiblockShapeInfo> getMatchingShapes() {
        if (this.structurePattern == null) {
            this.reinitializeStructurePattern();
            if (this.structurePattern == null) {
                return Collections.emptyList();
            }
        }
        int[][] aisleRepetitions = this.structurePattern.aisleRepetitions;
        return repetitionDFS(new ArrayList<>(), aisleRepetitions, new Stack<>());
    }

    private List<MultiblockShapeInfo> repetitionDFS(List<MultiblockShapeInfo> pages, int[][] aisleRepetitions, Stack<Integer> repetitionStack) {
        if (repetitionStack.size() == aisleRepetitions.length) {
            int[] repetition = new int[repetitionStack.size()];
            for (int i = 0; i < repetitionStack.size(); i++) {
                repetition[i] = repetitionStack.get(i);
            }
            pages.add(new MultiblockShapeInfo(this.structurePattern.getPreview(repetition)));
        } else {
            for (int i = aisleRepetitions[repetitionStack.size()][0]; i <= aisleRepetitions[repetitionStack.size()][1]; i++) {
                repetitionStack.push(i);
                repetitionDFS(pages, aisleRepetitions, repetitionStack);
                repetitionStack.pop();
            }
        }
        return pages;
    }

    @SideOnly(Side.CLIENT)
    public String[] getDescription() {
        String key = String.format("gregtech.multiblock.%s.description", metaTileEntityId.getPath());
        return I18n.hasKey(key) ? new String[]{I18n.format(key)} : new String[0];
    }

    @Override
    public int getDefaultPaintingColor() {
        return 0xFFFFFF;
    }

    public void explodeMultiblock(float explosionPower) {
        List<IMultiblockPart> parts = new ArrayList<>(getMultiblockParts());
        for (IMultiblockPart part : parts) {
            part.removeFromMultiBlock(this);
            ((MetaTileEntity) part).doExplosion(explosionPower);
        }
        doExplosion(explosionPower);
    }
}
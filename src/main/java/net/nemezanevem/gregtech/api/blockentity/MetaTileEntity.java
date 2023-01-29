package net.nemezanevem.gregtech.api.blockentity;

import codechicken.lib.raytracer.IndexedVoxelShape;
import codechicken.lib.raytracer.VoxelShapeBlockHitResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.ColourMultiplier;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.capability.GregtechTileCapabilities;
import net.nemezanevem.gregtech.api.capability.IControllable;
import net.nemezanevem.gregtech.api.capability.IEnergyContainer;
import net.nemezanevem.gregtech.api.capability.impl.*;
import net.nemezanevem.gregtech.api.cover.CoverBehavior;
import net.nemezanevem.gregtech.api.cover.CoverDefinition;
import net.nemezanevem.gregtech.api.cover.ICoverable;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.item.toolitem.ToolClass;
import net.nemezanevem.gregtech.api.recipe.GTRecipeType;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.util.GTTransferUtils;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.renderer.texture.Textures;
import net.nemezanevem.gregtech.common.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.*;

public abstract class MetaTileEntity implements ICoverable, IVoidable {

    public static final IndexedVoxelShape FULL_CUBE_COLLISION = new IndexedVoxelShape(Shapes.block(), null);
    public static final String TAG_KEY_PAINTING_COLOR = "PaintingColor";
    public static final String TAG_KEY_FRAGILE = "Fragile";
    public static final String TAG_KEY_MUFFLED = "Muffled";

    public final ResourceLocation metaTileEntityId;
    IGregTechTileEntity holder;
    public final Item itemForm;


    protected IItemHandlerModifiable importItems;
    protected IItemHandlerModifiable exportItems;

    protected IItemHandler itemInventory;

    protected FluidTankList importFluids;
    protected FluidTankList exportFluids;

    protected IFluidHandler fluidInventory;

    protected final List<MTETrait> mteTraits = new ArrayList<>();

    protected Direction frontFacing = Direction.NORTH;
    private int paintingColor = -1;

    private final int[] sidedRedstoneOutput = new int[6];
    private final int[] sidedRedstoneInput = new int[6];
    private int cachedComparatorValue;
    private int cachedLightValue;
    protected boolean isFragile = false;

    protected final CoverBehavior[] coverBehaviors = new CoverBehavior[6];
    protected List<IItemHandlerModifiable> notifiedItemOutputList = new ArrayList<>();
    protected List<IItemHandlerModifiable> notifiedItemInputList = new ArrayList<>();
    protected List<IFluidHandler> notifiedFluidInputList = new ArrayList<>();
    protected List<IFluidHandler> notifiedFluidOutputList = new ArrayList<>();

    protected boolean muffled = false;

    private int playSoundCooldown = 0;

    public MetaTileEntity(ResourceLocation metaTileEntityId) {
        this.metaTileEntityId = metaTileEntityId;
        initializeInventory();
        this.itemForm = ForgeRegistries.ITEMS.getValue(new ResourceLocation(metaTileEntityId.getNamespace(), "meta_tile_entity" + metaTileEntityId.getPath()));
    }

    protected void initializeInventory() {
        this.importItems = createImportItemHandler();
        this.exportItems = createExportItemHandler();
        this.itemInventory = new ItemHandlerProxy(importItems, exportItems);

        this.importFluids = createImportFluidHandler();
        this.exportFluids = createExportFluidHandler();
        this.fluidInventory = new FluidHandlerProxy(importFluids, exportFluids);
    }

    public IGregTechTileEntity getHolder() {
        return holder;
    }

    public abstract MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity);

    public Level getLevel() {
        return holder == null ? null : holder.world();
    }

    public BlockPos getPos() {
        return holder == null ? null : holder.pos();
    }

    public void markDirty() {
        if (holder != null) {
            holder.markAsDirty();
        }
    }

    public boolean isFirstTick() {
        return holder != null && holder.isFirstTick();
    }

    /**
     * Replacement for former getTimer() call.
     *
     * @return Timer value, starting at zero, with a random offset [0, 20).
     */
    public long getOffsetTimer() {
        return holder == null ? 0L : holder.getOffsetTimer();
    }

    public void writeCustomData(int discriminator, Consumer<FriendlyByteBuf> dataWriter) {
        if (holder != null) {
            holder.writeCustomData(discriminator, dataWriter);
        }
    }

    public void addDebugInfo(List<String> list) {
    }

    public void addInformation(ItemStack stack, @Nullable Level world, @Nonnull List<Component> tooltip, boolean advanced) {
    }

    /**
     * Override this to add extended tool information to the "Hold SHIFT to show Tool Info" tooltip section.
     * ALWAYS CALL SUPER LAST!
     * Intended ordering:
     * - Screwdriver
     * - Wrench
     * - Wire Cutter
     * - Soft Hammer
     * - Hammer
     * - Crowbar
     * - Others
     * <br>
     * The super method automatically handles Hammer muffling and Crowbar cover removal.
     * If you have extended usages of these tools in your addon, let us know and we can amend
     * this default appended tooltip information.
     */
    public void addToolUsages(ItemStack stack, @Nullable Level world, List<Component> tooltip, boolean advanced) {
        if (getSound() != null) {
            tooltip.add(Component.translatable("gregtech.tool_action.hammer"));
        }
        tooltip.add(Component.translatable("gregtech.tool_action.crowbar"));
    }

    /** Override this to completely remove the "Tool Info" tooltip section */
    public boolean showToolUsages() {
        return true;
    }

    public Pair<TextureAtlasSprite, Integer> getParticleTexture() {
        return Pair.of(TextureUtils.getMissingSprite(), 0xFFFFFF);
    }

    /**
     * ItemStack currently being rendered by this meta tile entity
     * Use this to obtain itemstack-specific data like contained fluid, painting color
     * Generally useful in combination with {@link #writeItemStackData(net.minecraft.nbt.CompoundTag)}
     */
    protected ItemStack renderContextStack;

    public void setRenderContextStack(ItemStack itemStack) {
        this.renderContextStack = itemStack;
    }

    /**
     * Renders this meta tile entity
     * Note that you shouldn't refer to world-related information in this method, because it
     * will be called on ItemStacks too
     *
     * @param renderState render state (either chunk batched or item)
     * @param pipeline    default set of pipeline transformations
     */
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        TextureAtlasSprite atlasSprite = TextureUtils.getMissingSprite();
        IVertexOperation[] renderPipeline = ArrayUtils.add(pipeline, new ColourMultiplier(Util.convertRGBtoOpaqueRGBA_CL(getPaintingColorForRendering())));
        for (Direction face : Direction.values()) {
            Textures.renderFace(renderState, translation, renderPipeline, face, Cuboid6.full, atlasSprite, RenderType.cutoutMipped());
        }
    }

    public boolean canRenderInLayer(RenderType renderLayer) {
        return renderLayer == RenderType.cutoutMipped() ||
                renderLayer == BloomEffectUtil.getRealBloomLayer() ||
                (renderLayer == RenderType.translucent() && !getLevel().getBlockState(getPos()).getValue(BlockMachine.OPAQUE));
    }

    public int getPaintingColorForRendering() {
        if (getLevel() == null && renderContextStack != null) {
            CompoundTag tagCompound = renderContextStack.getTag();
            if (tagCompound != null && tagCompound.contains(TAG_KEY_PAINTING_COLOR, Tag.TAG_INT)) {
                return tagCompound.getInt(TAG_KEY_PAINTING_COLOR);
            }
        }
        return isPainted() ? paintingColor : getDefaultPaintingColor();
    }

    /**
     * Used to display things like particles on random display ticks
     * This method is typically used by torches or nether portals, as an example use-case
     */
    public void randomDisplayTick() {

    }

    /**
     * Called from ItemBlock to initialize this MTE with data contained in ItemStack
     *
     * @param itemStack itemstack of itemblock
     */
    public void initFromItemStackData(CompoundTag itemStack) {
        if (itemStack.contains(TAG_KEY_FRAGILE)) {
            setFragile(itemStack.getBoolean(TAG_KEY_FRAGILE));
        }
    }

    /**
     * Called to write MTE specific data when it is destroyed to save it's state
     * into itemblock, which can be placed later to get {@link #initFromItemStackData} called
     *
     * @param itemStack itemstack from which this MTE is being placed
     */
    public void writeItemStackData(CompoundTag itemStack) {
    }

    public void getSubItems(CreativeModeTab creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(getStackForm());
    }

    public String getItemSubTypeId(ItemStack itemStack) {
        return "";
    }

    public ICapabilityProvider initItemStackCapabilities(ItemStack itemStack) {
        return null;
    }

    public final String getMetaName() {
        return String.format("%s.machine.%s", metaTileEntityId.getNamespace(), metaTileEntityId.getPath());
    }

    public final String getMetaFullName() {
        return getMetaName() + ".name";
    }

    public <T> void addNotifiedInput(T input) {
        if (input instanceof IItemHandlerModifiable) {
            if (!notifiedItemInputList.contains(input)) {
                this.notifiedItemInputList.add((IItemHandlerModifiable) input);
            }
        } else if (input instanceof FluidTank) {
            if (!notifiedFluidInputList.contains(input)) {
                this.notifiedFluidInputList.add((FluidTank) input);
            }
        }
    }

    public <T> void addNotifiedOutput(T output) {
        if (output instanceof IItemHandlerModifiable) {
            if (!notifiedItemOutputList.contains(output)) {
                this.notifiedItemOutputList.add((IItemHandlerModifiable) output);
            }
        } else if (output instanceof NotifiableFluidTank) {
            if (!notifiedFluidOutputList.contains(output)) {
                this.notifiedFluidOutputList.add((NotifiableFluidTank) output);
            }
        }
    }

    /**
     * Adds a trait to this meta tile entity
     * traits are objects linked with meta tile entity and performing certain
     * actions. usually traits implement capabilities
     * there can be only one trait for given name
     *
     * @param trait trait object to add
     */
    void addMetaTileEntityTrait(MTETrait trait) {
        mteTraits.removeIf(otherTrait -> {
            if (trait.getName().equals(otherTrait.getName())) {
                return true;
            }
            if (otherTrait.getNetworkID() == trait.getNetworkID()) {
                String message = "Trait %s is incompatible with trait %s, as they both use same network id %d";
                throw new IllegalArgumentException(String.format(message, trait, otherTrait, trait.getNetworkID()));
            }
            return false;
        });
        this.mteTraits.add(trait);
    }

    protected IItemHandlerModifiable createImportItemHandler() {
        return new ItemStackHandler(0);
    }

    protected IItemHandlerModifiable createExportItemHandler() {
        return new ItemStackHandler(0);
    }

    protected FluidTankList createImportFluidHandler() {
        return new FluidTankList(false);
    }

    protected FluidTankList createExportFluidHandler() {
        return new FluidTankList(false);
    }

    protected boolean openGUIOnRightClick() {
        return true;
    }

    /**
     * Creates a UI instance for player opening inventory of this meta tile entity
     *
     * @param Player player opening inventory
     * @return freshly created UI instance
     */
    protected abstract ModularUI createUI(Player Player);

    public ModularUI getModularUI(Player Player) {
        return createUI(Player);
    }

    public final void onCoverLeftClick(Player playerIn, VoxelShapeBlockHitResult result) {
        CoverBehavior coverBehavior = getCoverAtSide(result.getDirection());
        if (coverBehavior == null || !coverBehavior.onLeftClick(playerIn, result)) {
            onLeftClick(playerIn, result.getDirection(), result);
        }
    }

    /**
     * Called when player clicks on specific side of this meta tile entity
     *
     * @return true if something happened, so animation will be played
     */
    public boolean onRightClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (!playerIn.isCrouching() && openGUIOnRightClick()) {
            if (getLevel() != null && !getLevel().isClientSide) {
                MetaTileEntityUIFactory.INSTANCE.openUI(getHolder(), (ServerPlayer) playerIn);
            }
            return true;
        } else if (playerIn.isCrouching() && playerIn.getMainHandItem().isEmpty()) {
            Direction hitFacing = hitResult.getDirection();

            CoverBehavior coverBehavior = getCoverAtSide(hitFacing);

            InteractionResult coverResult = coverBehavior == null ? InteractionResult.PASS :
                    coverBehavior.onScrewdriverClick(playerIn, hand, hitResult);

            return coverResult == InteractionResult.SUCCESS;
        }
        return false;
    }

    /**
     * Called when a player clicks this meta tile entity with a tool
     *
     * @return true if something happened, so tools will get damaged and animations will be played
     */
    public final boolean onToolClick(Player playerIn, @Nonnull Set<String> toolClasses, InteractionHand hand, VoxelShapeBlockHitResult hitResult)  {
        // the side hit from the machine grid
        Direction gridSideHit = ICoverable.determineGridSideHit(hitResult);
        CoverBehavior coverBehavior = gridSideHit == null ? null : getCoverAtSide(gridSideHit);

        // Prioritize covers where they apply (Screwdriver, Soft Mallet)
        if (toolClasses.contains(ToolClass.SCREWDRIVER)) {
            if (coverBehavior != null && coverBehavior.onScrewdriverClick(playerIn, hand, hitResult) == InteractionResult.SUCCESS) {
                return true;
            } else return onScrewdriverClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClass.SOFT_MALLET)) {
            if (coverBehavior != null && coverBehavior.onSoftMalletClick(playerIn, hand, hitResult) == InteractionResult.SUCCESS) {
                return true;
            } else return onSoftMalletClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClass.WRENCH)) {
            return onWrenchClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClass.CROWBAR)) {
            return onCrowbarClick(playerIn, hand, gridSideHit, hitResult);
        }
        if (toolClasses.contains(ToolClass.HARD_HAMMER)) {
            return onHardHammerClick(playerIn, hand, gridSideHit, hitResult);
        }
        return false;
    }

    /**
     * Called when player clicks a wrench on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onWrenchClick(Player playerIn, InteractionHand hand, Direction wrenchSide, VoxelShapeBlockHitResult hitResult) {
        if (!needsSneakToRotate() || playerIn.isCrouching()) {
            if (wrenchSide == getFrontFacing() || !isValidFrontFacing(wrenchSide) || !hasFrontFacing()) {
                return false;
            }
            if (wrenchSide != null && !getLevel().isClientSide) {
                setFrontFacing(wrenchSide);
            }
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks a screwdriver on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onScrewdriverClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        return false;
    }

    /**
     * Called when player clicks a crowbar on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onCrowbarClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        if (getCoverAtSide(facing) != null) {
            return removeCover(facing);
        }
        return false;
    }

    /**
     * Called when player clicks a soft mallet on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onSoftMalletClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        LazyOptional<IControllable> controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null && controllable.isPresent()) {
            var controllableReal = controllable.resolve().get();
            controllableReal.setWorkingEnabled(!controllableReal.isWorkingEnabled());
            if (!getLevel().isClientSide) {
                playerIn.sendSystemMessage(Component.translatable(controllableReal.isWorkingEnabled() ?
                        "behaviour.soft_hammer.enabled" : "behaviour.soft_hammer.disabled"));
            }
            return true;
        }
        return false;
    }

    /**
     * Called when player clicks a hard hammer on specific side of this meta tile entity
     *
     * @return true if something happened, so the tool will get damaged and animation will be played
     */
    public boolean onHardHammerClick(Player playerIn, InteractionHand hand, Direction facing, VoxelShapeBlockHitResult hitResult) {
        toggleMuffled();
        if (!getLevel().isClientSide) {
            playerIn.sendSystemMessage(Component.translatable(isMuffled() ?
                    "gregtech.machine.muffle.on" : "gregtech.machine.muffle.off"));
        }
        return true;
    }

    public void onLeftClick(Player player, Direction facing, VoxelShapeBlockHitResult hitResult) {
    }

    /**
     * @return true if the player must sneak to rotate this metatileentity, otherwise false
     */
    public boolean needsSneakToRotate() {
        return false;
    }

    @Nullable
    public final CoverBehavior getCoverAtSide(Direction side) {
        return coverBehaviors[side.ordinal()];
    }

    public boolean placeCoverOnSide(Direction side, ItemStack itemStack, CoverDefinition coverDefinition, Player player) {
        Preconditions.checkNotNull(side, "side");
        Preconditions.checkNotNull(coverDefinition, "coverDefinition");
        CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, side);
        if (!canPlaceCoverOnSide(side) || !coverBehavior.canAttach()) {
            return false;
        }
        if (coverBehaviors[side.ordinal()] != null) {
            removeCover(side);
        }
        this.coverBehaviors[side.ordinal()] = coverBehavior;
        coverBehavior.onAttached(itemStack, player);
        writeCustomData(COVER_ATTACHED_MTE, buffer -> {
            buffer.writeByte(side.ordinal());
            buffer.writeVarInt(CoverDefinition.getNetworkIdForCover(coverDefinition));
            coverBehavior.writeInitialSyncData(buffer);
        });
        notifyBlockUpdate();
        markDirty();
        onCoverPlacementUpdate();
        AdvancementTriggers.FIRST_COVER_PLACE.trigger((ServerPlayer) player);
        return true;
    }

    public final boolean removeCover(Direction side) {
        Preconditions.checkNotNull(side, "side");
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return false;
        }
        List<ItemStack> drops = coverBehavior.getDrops();
        coverBehavior.onRemoved();
        this.coverBehaviors[side.ordinal()] = null;
        for (ItemStack dropStack : drops) {
            Block.popResource(getLevel(), getPos(), dropStack);
        }
        writeCustomData(COVER_REMOVED_MTE, buffer -> buffer.writeByte(side.ordinal()));
        notifyBlockUpdate();
        markDirty();
        onCoverPlacementUpdate();
        return true;
    }

    protected void onCoverPlacementUpdate() {
    }

    public final void dropAllCovers() {
        for (Direction coverSide : Direction.values()) {
            CoverBehavior coverBehavior = coverBehaviors[coverSide.ordinal()];
            if (coverBehavior == null) continue;
            List<ItemStack> drops = coverBehavior.getDrops();
            coverBehavior.onRemoved();
            for (ItemStack dropStack : drops) {
                Block.popResource(getLevel(), getPos(), dropStack);
            }
        }
    }

    public boolean canPlaceCoverOnSide(Direction side) {
        ArrayList<IndexedVoxelShape> collisionList = new ArrayList<>();
        addCollisionBoundingBox(collisionList);
        //noinspection RedundantIfStatement
        if (ICoverable.doesCoverCollide(side, collisionList, getCoverPlateThickness())) {
            //cover collision box overlaps with meta tile entity collision box
            return false;
        }
        return true;
    }

    /**
     * @return the cover plate thickness. It is used to render cover's base plate
     * if this meta tile entity is not full block length, and also
     * to check whatever cover placement is possible on specified side,
     * because cover cannot be placed if collision boxes of machine and it's plate overlap
     * If zero, it is expected that machine is full block and plate doesn't need to be rendered
     */
    @Override
    public double getCoverPlateThickness() {
        return 0.0;
    }

    @Override
    public boolean shouldRenderBackSide() {
        return !isOpaqueCube();
    }

    public void onLoad() {
        this.cachedComparatorValue = getActualComparatorValue();
        for (Direction side : Direction.values()) {
            this.sidedRedstoneInput[side.ordinal()] = Util.getRedstonePower(getLevel(), getPos(), side);
        }
    }

    public void onUnload() {
    }

    public final boolean canConnectRedstone(@Nullable Direction side) {
        //so far null side means either upwards or downwards redstone wire connection
        //so check both top cover and bottom cover
        if (side == null) {
            return canConnectRedstone(Direction.UP) ||
                    canConnectRedstone(Direction.DOWN);
        }
        CoverBehavior coverBehavior = getCoverAtSide(side);
        if (coverBehavior == null) {
            return canMachineConnectRedstone(side);
        }
        return coverBehavior.canConnectRedstone();
    }

    protected boolean canMachineConnectRedstone(Direction side) {
        return false;
    }

    @Override
    public final int getInputRedstoneSignal(Direction side, boolean ignoreCover) {
        if (!ignoreCover && getCoverAtSide(side) != null) {
            return 0; //covers block input redstone signal for machine
        }
        return sidedRedstoneInput[side.ordinal()];
    }

    public final boolean isBlockRedstonePowered() {
        for (Direction side : Direction.values()) {
            if (getInputRedstoneSignal(side, false) > 0) {
                return true;
            }
        }
        return false;
    }

    public void onNeighborChanged() {
    }

    public void updateInputRedstoneSignals() {
        for (Direction side : Direction.values()) {
            int redstoneValue = Util.getRedstonePower(getLevel(), getPos(), side);
            int currentValue = sidedRedstoneInput[side.ordinal()];
            if (redstoneValue != currentValue) {
                this.sidedRedstoneInput[side.ordinal()] = redstoneValue;
                CoverBehavior coverBehavior = getCoverAtSide(side);
                if (coverBehavior != null) {
                    coverBehavior.onRedstoneInputSignalChange(redstoneValue);
                }
            }
        }
    }

    public int getActualComparatorValue() {
        return 0;
    }

    public int getActualLightValue() {
        return 0;
    }

    public final int getComparatorValue() {
        return cachedComparatorValue;
    }

    public final int getLightValue() {
        return cachedLightValue;
    }

    private void updateComparatorValue() {
        int newComparatorValue = getActualComparatorValue();
        if (cachedComparatorValue != newComparatorValue) {
            this.cachedComparatorValue = newComparatorValue;
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
            }
        }
    }

    private void updateLightValue() {
        int newLightValue = getActualLightValue();
        if (cachedLightValue != newLightValue) {
            this.cachedLightValue = newLightValue;
            if (getLevel() != null) {
                getLevel().getLightEngine().getRawBrightness(getPos(), 0);
            }
        }
    }

    public void tick() {
        if(!getLevel().isClientSide) {
            if (getOffsetTimer() % 5 == 0L) {
                updateComparatorValue();
            }
        } else {
            updateSound();
        }
        for (MTETrait mteTrait : mteTraits) {
            if (shouldUpdate(mteTrait)) {
                mteTrait.tick();
            }
        }

        if (getOffsetTimer() % 5 == 0L) {
            updateLightValue();
        }
    }

    protected boolean shouldUpdate(MTETrait trait) {
        return true;
    }

    private void updateSound() {
        if (!ConfigHolder.machines.machineSounds || isMuffled()) {
            return;
        }
        SoundEvent sound = getSound();
        if (sound == null) {
            return;
        }
        if (isValid() && isActive()) {
            if (--playSoundCooldown > 0) {
                return;
            }
            GregTech.soundManager.startTileSound(sound.getLocation(), 1.0F, getPos());
            playSoundCooldown = 20;
        } else {
            GregTech.soundManager.stopTileSound(getPos());
            playSoundCooldown = 0;
        }
    }

    public final ItemStack getStackForm(int amount) {
        return new ItemStack(this.itemForm, amount);
    }

    public final ItemStack getStackForm() {
        return getStackForm(1);
    }

    /**
     * Add special drops which this meta tile entity contains here
     * Meta tile entity item is ALREADY added into this list
     * Do NOT add inventory contents in this list - it will be dropped automatically when breakBlock is called
     * This will only be called if meta tile entity is broken with proper tool (i.e wrench)
     *
     * @param dropsList list of meta tile entity drops
     * @param harvester harvester of this meta tile entity, or null
     */
    public void getDrops(NonNullList<ItemStack> dropsList, @Nullable Player harvester) {
    }

    public ItemStack getPickItem(VoxelShapeBlockHitResult result, Player player) {
        IndexedVoxelShape hitCuboid = result.shape;
        if (hitCuboid.getData() instanceof CoverSideData coverSideData) {
            CoverBehavior behavior = getCoverAtSide(coverSideData.side());
            return behavior == null ? ItemStack.EMPTY : behavior.getPickItem();
        } else if (hitCuboid.getData() == null || hitCuboid.getData() instanceof PrimaryBoxData) {
            //data is null -> MetaTileEntity hull hit
            CoverBehavior behavior = getCoverAtSide(result.getDirection());
            if (behavior != null) {
                return behavior.getPickItem();
            }
            return getStackForm();
        } else {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Whether this tile entity represents completely opaque cube
     *
     * @return true if machine is opaque
     */
    public boolean isOpaqueCube() {
        return true;
    }

    public int getLightOpacity() {
        return 255;
    }

    /**
     * Called to obtain list of AABB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     */
    public void addCollisionBoundingBox(List<IndexedVoxelShape> collisionList) {
        collisionList.add(FULL_CUBE_COLLISION);
    }

    /**
     * @return tool required to dismantle this meta tile entity properly
     */
    public ToolClass getHarvestTool() {
        return ToolClass.WRENCH;
    }

    /**
     * @return minimal level of tool required to dismantle this meta tile entity properly
     */
    public int getHarvestLevel() {
        return 1;
    }

    public void writeInitialSyncData(FriendlyByteBuf buf) {
        buf.writeByte(this.frontFacing.ordinal());
        buf.writeInt(this.paintingColor);
        buf.writeShort(mteTraits.size());
        for (MTETrait trait : mteTraits) {
            buf.writeVarInt(trait.getNetworkID());
            trait.writeInitialData(buf);
        }
        for (Direction coverSide : Direction.values()) {
            CoverBehavior coverBehavior = getCoverAtSide(coverSide);
            if (coverBehavior != null) {
                int coverId = CoverDefinition.getNetworkIdForCover(coverBehavior.getCoverDefinition());
                buf.writeVarInt(coverId);
                coverBehavior.writeInitialSyncData(buf);
            } else {
                buf.writeVarInt(-1);
            }
        }
        buf.writeBoolean(isFragile);
        buf.writeBoolean(muffled);
    }

    public boolean isPainted() {
        return this.paintingColor != -1;
    }

    public void receiveInitialSyncData(FriendlyByteBuf buf) {
        this.frontFacing = Direction.values()[buf.readByte()];
        this.paintingColor = buf.readInt();
        int amountOfTraits = buf.readShort();
        for (int i = 0; i < amountOfTraits; i++) {
            int traitNetworkId = buf.readVarInt();
            MTETrait trait = mteTraits.stream().filter(otherTrait -> otherTrait.getNetworkID() == traitNetworkId).findAny().get();
            trait.receiveInitialData(buf);
        }
        for (Direction coverSide : Direction.values()) {
            int coverId = buf.readVarInt();
            if (coverId != -1) {
                CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(coverId);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, coverSide);
                coverBehavior.readInitialSyncData(buf);
                this.coverBehaviors[coverSide.ordinal()] = coverBehavior;
            }
        }
        this.isFragile = buf.readBoolean();
        this.muffled = buf.readBoolean();
    }

    public void writeTraitData(MTETrait trait, int internalId, Consumer<FriendlyByteBuf> dataWriter) {
        writeCustomData(SYNC_MTE_TRAITS, buffer -> {
            buffer.writeVarInt(trait.getNetworkID());
            buffer.writeVarInt(internalId);
            dataWriter.accept(buffer);
        });
    }

    public void writeCoverData(CoverBehavior cover, int internalId, Consumer<FriendlyByteBuf> dataWriter) {
        writeCustomData(UPDATE_COVER_DATA_MTE, buffer -> {
            buffer.writeByte(cover.attachedSide.ordinal());
            buffer.writeVarInt(internalId);
            dataWriter.accept(buffer);
        });
    }

    public void receiveCustomData(int dataId, FriendlyByteBuf buf) {
        if (dataId == UPDATE_FRONT_FACING) {
            this.frontFacing = Direction.values()[buf.readByte()];
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_PAINTING_COLOR) {
            this.paintingColor = buf.readInt();
            scheduleRenderUpdate();
        } else if (dataId == SYNC_MTE_TRAITS) {
            int traitNetworkId = buf.readVarInt();
            MTETrait trait = mteTraits.stream().filter(otherTrait -> otherTrait.getNetworkID() == traitNetworkId).findAny().get();
            int internalId = buf.readVarInt();
            trait.receiveCustomData(internalId, buf);
        } else if (dataId == COVER_ATTACHED_MTE) {
            //cover placement event
            Direction placementSide = Direction.values()[buf.readByte()];
            int coverId = buf.readVarInt();
            CoverDefinition coverDefinition = CoverDefinition.getCoverByNetworkId(coverId);
            CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, placementSide);
            this.coverBehaviors[placementSide.ordinal()] = coverBehavior;
            coverBehavior.readInitialSyncData(buf);
            onCoverPlacementUpdate();
            scheduleRenderUpdate();
        } else if (dataId == COVER_REMOVED_MTE) {
            //cover removed event
            Direction placementSide = Direction.values()[buf.readByte()];
            this.coverBehaviors[placementSide.ordinal()] = null;
            onCoverPlacementUpdate();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_COVER_DATA_MTE) {
            //cover custom data received
            Direction coverSide = Direction.values()[buf.readByte()];
            CoverBehavior coverBehavior = getCoverAtSide(coverSide);
            int internalId = buf.readVarInt();
            if (coverBehavior != null) {
                coverBehavior.readUpdateData(internalId, buf);
            }
        } else if (dataId == UPDATE_IS_FRAGILE) {
            this.isFragile = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_SOUND_MUFFLED) {
            this.muffled = buf.readBoolean();
            if (muffled) {
                GregTech.soundManager.stopTileSound(getPos());
            }
        }
    }

    public final <T> LazyOptional<T> getCoverCapability(Capability<T> capability, Direction side) {
        boolean isCoverable = capability == GregtechTileCapabilities.CAPABILITY_COVERABLE;
        CoverBehavior coverBehavior = side == null ? null : getCoverAtSide(side);
        LazyOptional<T> originalCapability = getCapability(capability, side);
        if (coverBehavior != null && !isCoverable) {
            return coverBehavior.getCapability(capability, originalCapability);
        }
        return originalCapability;
    }

    LazyOptional<ICoverable> coverableLazy = LazyOptional.of(() -> this);

    public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_COVERABLE) {
            coverableLazy.cast();
        }
        if (capability == ForgeCapabilities.FLUID_HANDLER &&
                getFluidInventory().getTanks() > 0) {
            return fluidHandlerLazy.cast();
        } else if (capability == ForgeCapabilities.ITEM_HANDLER &&
                getItemInventory().getSlots() > 0) {
            return itemHandlerLazy.cast();
        }
        LazyOptional<T> capabilityResult = null;
        for (MTETrait mteTrait : this.mteTraits) {
            capabilityResult = mteTrait.getCapability(capability);
            if (capabilityResult != null) {
                break;
            }
        }
        if (side != null && capabilityResult instanceof IEnergyContainer) {
            IEnergyContainer energyContainer = (IEnergyContainer) capabilityResult;
            if (!energyContainer.inputsEnergy(side) && !energyContainer.outputsEnergy(side)) {
                return null; //do not provide energy container if it can't input or output energy at all
            }
        }
        return capabilityResult;
    }

    public void fillInternalTankFromFluidContainer() {
        fillInternalTankFromFluidContainer(importFluids);
    }

    public void fillInternalTankFromFluidContainer(IFluidHandler fluidHandler) {
        for (int i = 0; i < importItems.getSlots(); i++) {
            ItemStack inputContainerStack = importItems.extractItem(i, 1, true);
            FluidActionResult result = FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, null, false);
            if (result.isSuccess()) {
                ItemStack remainingItem = result.getResult();
                if (ItemStack.matches(inputContainerStack, remainingItem))
                    continue; //do not fill if item stacks match
                if (!remainingItem.isEmpty() && !GTTransferUtils.insertItem(exportItems, remainingItem, true).isEmpty())
                    continue; //do not fill if can't put remaining item
                FluidUtil.tryEmptyContainer(inputContainerStack, fluidHandler, Integer.MAX_VALUE, null, true);
                importItems.extractItem(i, 1, false);
                GTTransferUtils.insertItem(exportItems, remainingItem, false);
            }
        }
    }

    public void fillContainerFromInternalTank() {
        fillContainerFromInternalTank(exportFluids);
    }

    public void fillContainerFromInternalTank(IFluidHandler fluidHandler) {
        for (int i = 0; i < importItems.getSlots(); i++) {
            ItemStack emptyContainer = importItems.extractItem(i, 1, true);
            FluidActionResult result = FluidUtil.tryFillContainer(emptyContainer, fluidHandler, Integer.MAX_VALUE, null, false);
            if (result.isSuccess()) {
                ItemStack remainingItem = result.getResult();
                if (!remainingItem.isEmpty() && !GTTransferUtils.insertItem(exportItems, remainingItem, true).isEmpty())
                    continue;
                FluidUtil.tryFillContainer(emptyContainer, fluidHandler, Integer.MAX_VALUE, null, true);
                importItems.extractItem(i, 1, false);
                GTTransferUtils.insertItem(exportItems, remainingItem, false);
            }
        }
    }

    public void pushFluidsIntoNearbyHandlers(Direction... allowedFaces) {
        transferToNearby(ForgeCapabilities.FLUID_HANDLER, GTTransferUtils::transferFluids, allowedFaces);
    }

    public void pullFluidsFromNearbyHandlers(Direction... allowedFaces) {
        transferToNearby(ForgeCapabilities.FLUID_HANDLER, (thisCap, otherCap) -> GTTransferUtils.transferFluids(otherCap, thisCap), allowedFaces);
    }

    public void pushItemsIntoNearbyHandlers(Direction... allowedFaces) {
        transferToNearby(ForgeCapabilities.ITEM_HANDLER, GTTransferUtils::moveInventoryItems, allowedFaces);
    }

    public void pullItemsFromNearbyHandlers(Direction... allowedFaces) {
        transferToNearby(ForgeCapabilities.ITEM_HANDLER, (thisCap, otherCap) -> GTTransferUtils.moveInventoryItems(otherCap, thisCap), allowedFaces);
    }

    private <T> void transferToNearby(Capability<T> capability, BiConsumer<LazyOptional<T>, LazyOptional<T>> transfer, Direction... allowedFaces) {
        BlockPos.MutableBlockPos blockPos = getPos().mutable();
        for (Direction nearbyFacing : allowedFaces) {
            blockPos.set(getPos()).move(nearbyFacing);
            BlockEntity tileEntity = getLevel().getBlockEntity(blockPos);
            if (tileEntity == null) {
                continue;
            }
            LazyOptional<T> otherCap = tileEntity.getCapability(capability, nearbyFacing.getOpposite());
            //use getCoverCapability so item/ore dictionary filter covers will work properly
            LazyOptional<T> thisCap = getCoverCapability(capability, nearbyFacing);
            if (otherCap == null || thisCap == null) {
                continue;
            }
            transfer.accept(thisCap, otherCap);
        }
    }

    public final int getOutputRedstoneSignal(@Nullable Direction side) {
        if (side == null) {
            return getHighestOutputRedstoneSignal();
        }
        CoverBehavior behavior = getCoverAtSide(side);
        int sidedOutput = sidedRedstoneOutput[side.ordinal()];
        return behavior == null ? sidedOutput : behavior.getRedstoneSignalOutput();
    }

    public final int getHighestOutputRedstoneSignal() {
        int highestSignal = 0;
        for (Direction side : Direction.values()) {
            CoverBehavior behavior = getCoverAtSide(side);
            int sidedOutput = sidedRedstoneOutput[side.ordinal()];
            int sideResult = behavior == null ? sidedOutput : behavior.getRedstoneSignalOutput();
            highestSignal = Math.max(highestSignal, sideResult);
        }
        return highestSignal;
    }

    public final void setOutputRedstoneSignal(Direction side, int strength) {
        Preconditions.checkNotNull(side, "side");
        this.sidedRedstoneOutput[side.ordinal()] = strength;
        if (getLevel() != null && !getLevel().isClientSide && getCoverAtSide(side) == null) {
            notifyBlockUpdate();
            markDirty();
        }
    }

    @Override
    public void notifyBlockUpdate() {
        if (holder != null) holder.notifyBlockUpdate();
    }

    @Override
    public void scheduleRenderUpdate() {
        if (holder != null) holder.scheduleRenderUpdate();
    }

    public void setFrontFacing(Direction frontFacing) {
        Preconditions.checkNotNull(frontFacing, "frontFacing");
        this.frontFacing = frontFacing;
        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_FRONT_FACING, buf -> buf.writeByte(frontFacing.ordinal()));
            mteTraits.forEach(trait -> trait.onFrontFacingSet(frontFacing));
        }
    }

    public void setPaintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_PAINTING_COLOR, buf -> buf.writeInt(paintingColor));
        }
    }

    public int getDefaultPaintingColor() {
        return ConfigHolder.ClientConfig.defaultPaintingColor;
    }

    public void setFragile(boolean fragile) {
        this.isFragile = fragile;
        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
            writeCustomData(UPDATE_IS_FRAGILE, buf -> buf.writeBoolean(fragile));
        }
    }

    public boolean isValidFrontFacing(Direction facing) {
        if (this.hasFrontFacing() && getFrontFacing() == facing) return false;
        return facing != Direction.UP && facing != Direction.DOWN;
    }

    public boolean hasFrontFacing() {
        return true;
    }

    /**
     * @return true if this meta tile entity should serialize it's export and import inventories
     * Useful when you use your own unified inventory and don't need these dummies to be saved
     */
    protected boolean shouldSerializeInventories() {
        return true;
    }

    public CompoundTag writeToNBT(CompoundTag data) {
        data.putInt("FrontFacing", frontFacing.ordinal());
        if (isPainted()) {
            data.putInt(TAG_KEY_PAINTING_COLOR, paintingColor);
        }
        data.putInt("CachedLightValue", cachedLightValue);

        if (shouldSerializeInventories()) {
            Util.writeItems(importItems, "ImportInventory", data);
            Util.writeItems(exportItems, "ExportInventory", data);

            data.put("ImportFluidInventory", importFluids.serializeNBT());
            data.put("ExportFluidInventory", exportFluids.serializeNBT());
        }

        for (MTETrait mteTrait : this.mteTraits) {
            data.put(mteTrait.getName(), mteTrait.serializeNBT());
        }

        ListTag coversList = new ListTag();
        for (Direction coverSide : Direction.values()) {
            CoverBehavior coverBehavior = coverBehaviors[coverSide.ordinal()];
            if (coverBehavior != null) {
                CompoundTag tagCompound = new CompoundTag();
                ResourceLocation coverId = coverBehavior.getCoverDefinition().getCoverId();
                tagCompound.putString("CoverId", coverId.toString());
                tagCompound.putByte("Side", (byte) coverSide.ordinal());
                coverBehavior.writeToNBT(tagCompound);
                coversList.add(tagCompound);
            }
        }
        data.put("Covers", coversList);
        data.putBoolean(TAG_KEY_FRAGILE, isFragile);
        data.putBoolean(TAG_KEY_MUFFLED, muffled);
        return data;
    }

    public void readFromNBT(CompoundTag data) {
        this.frontFacing = Direction.values()[data.getInt("FrontFacing")];
        if (data.contains(TAG_KEY_PAINTING_COLOR)) {
            this.paintingColor = data.getInt(TAG_KEY_PAINTING_COLOR);
        }
        this.cachedLightValue = data.getInt("CachedLightValue");

        if (shouldSerializeInventories()) {
            Util.readItems(importItems, "ImportInventory", data);
            Util.readItems(exportItems, "ExportInventory", data);

            importFluids.deserializeNBT(data.getCompound("ImportFluidInventory"));
            exportFluids.deserializeNBT(data.getCompound("ExportFluidInventory"));
        }

        for (MTETrait mteTrait : this.mteTraits) {
            CompoundTag traitCompound = data.getCompound(mteTrait.getName());
            mteTrait.deserializeNBT(traitCompound);
        }

        ListTag coversList = data.getList("Covers", Tag.TAG_COMPOUND);
        for (int index = 0; index < coversList.size(); index++) {
            CompoundTag tagCompound = coversList.getCompound(index);
            if (tagCompound.contains("CoverId", Tag.TAG_STRING)) {
                Direction coverSide = Direction.values()[tagCompound.getByte("Side")];
                ResourceLocation coverId = new ResourceLocation(tagCompound.getString("CoverId"));
                CoverDefinition coverDefinition = CoverDefinition.getCoverById(coverId);
                CoverBehavior coverBehavior = coverDefinition.createCoverBehavior(this, coverSide);
                coverBehavior.readFromNBT(tagCompound);
                this.coverBehaviors[coverSide.ordinal()] = coverBehavior;
            }
        }

        this.isFragile = data.getBoolean(TAG_KEY_FRAGILE);
        this.muffled = data.getBoolean(TAG_KEY_MUFFLED);
    }

    @Override
    public boolean isValid() {
        return getHolder() != null && getHolder().isValid();
    }

    public void clearMachineInventory(NonNullList<ItemStack> itemBuffer) {
        clearInventory(itemBuffer, importItems);
        clearInventory(itemBuffer, exportItems);
    }

    public static void clearInventory(NonNullList<ItemStack> itemBuffer, IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                itemBuffer.add(stackInSlot);
            }
        }
    }

    /**
     * Deprecated, use {@link MetaTileEntity#onPlacement()} instead
     */
    @Deprecated(forRemoval = true)
    public void onAttached(Object... data) {
    }

    /**
     * Called whenever a MetaTileEntity is placed in world by {@link Block#setPlacedBy}
     * <p>
     * If placing an MTE with methods such as {@link Level#setBlock(BlockPos, BlockState, int)},
     * this should be manually called immediately afterwards
     */
    public void onPlacement() {

    }

    /**
     * Called from breakBlock right before meta tile entity destruction
     * at this stage tile entity inventory is already dropped on ground, but drops aren't fetched yet
     * tile entity will still get getDrops called after this, if player broke block
     */
    public void onRemoval() {
    }

    public void invalidate() {
        if (getLevel() != null && getLevel().isClientSide) {
            GregTech.soundManager.stopTileSound(getPos());
        }
        this.itemHandlerLazy.invalidate();
        this.fluidHandlerLazy.invalidate();
    }

    public SoundEvent getSound() {
        return null;
    }

    public boolean isActive() {
        return false;
    }

    public Direction getFrontFacing() {
        return frontFacing;
    }

    public int getPaintingColor() {
        return paintingColor;
    }


    private LazyOptional<IItemHandler> itemHandlerLazy = LazyOptional.of(this::getItemInventory);

    public IItemHandler getItemInventory() {
        return itemInventory;
    }

    private LazyOptional<IFluidHandler> fluidHandlerLazy = LazyOptional.of(this::getFluidInventory);

    public IFluidHandler getFluidInventory() {
        return fluidInventory;
    }

    public IItemHandlerModifiable getImportItems() {
        return importItems;
    }

    public IItemHandlerModifiable getExportItems() {
        return exportItems;
    }

    public FluidTankList getImportFluids() {
        return importFluids;
    }

    public FluidTankList getExportFluids() {
        return exportFluids;
    }

    public List<IItemHandlerModifiable> getNotifiedItemOutputList() {
        return notifiedItemOutputList;
    }

    public List<IItemHandlerModifiable> getNotifiedItemInputList() {
        return notifiedItemInputList;
    }

    public List<IFluidHandler> getNotifiedFluidInputList() {
        return notifiedFluidInputList;
    }

    public List<IFluidHandler> getNotifiedFluidOutputList() {
        return notifiedFluidOutputList;
    }

    public boolean isFragile() {
        return isFragile;
    }

    public boolean shouldDropWhenDestroyed() {
        return !isFragile();
    }

    public float getBlockHardness() {
        return 6.0f;
    }

    public float getBlockResistance() {
        return 6.0f;
    }

    /**
     * Override this if the MTE will keep its Item inventory on-break.
     * If this is overridden to return True, you MUST take care to handle
     * the ItemStacks in the MTE's inventory otherwise they will be voided on break.
     *
     * @return True if MTE inventory is kept as an ItemStack, false otherwise
     */
    public boolean keepsInventory() {
        return false;
    }

    public boolean getWitherProof() {
        return false;
    }

    public final void toggleMuffled() {
        muffled = !muffled;
        if (!getLevel().isClientSide) {
            writeCustomData(UPDATE_SOUND_MUFFLED, buf -> buf.writeBoolean(muffled));
        }
    }

    public boolean isMuffled() {
        return muffled;
    }

    public boolean canRenderFrontFaceX() {
        return false;
    }

    public boolean isSideUsed(Direction face) {
        if (getCoverAtSide(face) != null) return true;
        return face == this.getFrontFacing() && this.canRenderFrontFaceX();
    }

    public GTRecipeType<?> getRecipeType() {
        for (int i = 0; i < mteTraits.size(); i++) {
            if (mteTraits.get(i).getName().equals("RecipeTypeWorkable")) {
                return ((AbstractRecipeLogic) mteTraits.get(i)).getRecipeType();
            }
        }
        return null;
    }

    public void checkWeatherOrTerrainExplosion(float explosionPower, double additionalFireChance, IEnergyContainer energyContainer) {
        Level world = getLevel();
        if (!world.isClientSide && ConfigHolder.machines.doTerrainExplosion && !getIsWeatherOrTerrainResistant() && energyContainer.getEnergyStored() != 0) {
            if (GTValues.RNG.nextInt(1000) == 0) {
                for (Direction side : Direction.values()) {
                    BlockPos offset = getPos().offset(side.getNormal());
                    Block block = getLevel().getBlockState(offset).getBlock();
                    Fluid fluid = getLevel().getFluidState(offset).getType();
                    if (block == Blocks.FIRE || fluid == Fluids.WATER || fluid == Fluids.FLOWING_WATER || fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA) {
                        doExplosion(explosionPower);
                        return;
                    }
                }
            }
            if (GTValues.RNG.nextInt(1000) == 0) {
                if (world.isRainingAt(getPos()) || world.isRainingAt(getPos().east()) || world.isRainingAt(getPos().west()) || world.isRainingAt(getPos().north()) || world.isRainingAt(getPos().south())) {
                    if (world.isThundering() && GTValues.RNG.nextInt(3) == 0) {
                        doExplosion(explosionPower);
                    } else if (GTValues.RNG.nextInt(10) == 0) {
                        doExplosion(explosionPower);
                    } else setOnFire(additionalFireChance);
                }
            }
        }
    }

    public void doExplosion(float explosionPower) {
        getLevel().setBlock(getPos(), Blocks.AIR.defaultBlockState(), 11);
        getLevel().explode(null, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5,
                explosionPower, ConfigHolder.machines.doesExplosionDamagesTerrain ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE);
    }

    public void setOnFire(double additionalFireChance) {
        boolean isFirstFireSpawned = false;
        for (Direction side : Direction.values()) {
            if (getLevel().getBlockState(getPos().offset(side.getNormal())).isAir()) {
                if (!isFirstFireSpawned) {
                    getLevel().setBlock(getPos().offset(side.getNormal()), Blocks.FIRE.defaultBlockState(), 11);
                    if (!getLevel().getBlockState(getPos().offset(side.getNormal())).isAir()) {
                        isFirstFireSpawned = true;
                    }
                } else if (additionalFireChance >= GTValues.RNG.nextDouble() * 100) {
                    getLevel().setBlock(getPos().offset(side.getNormal()), Blocks.FIRE.defaultBlockState(), 11);
                }
            }
        }
    }

    /**
     * Whether this tile entity not explode in rain, fire, water or lava
     *
     * @return true if tile entity should not explode in these sources
     */
    public boolean getIsWeatherOrTerrainResistant() {
        return false;
    }

    public boolean doTickProfileMessage() {
        return true;
    }

    @Override
    public boolean canVoidRecipeItemOutputs() {
        return false;
    }

    @Override
    public boolean canVoidRecipeFluidOutputs() {
        return false;
    }

    @Override
    public Level getWorld() {
        return getLevel();
    }
}

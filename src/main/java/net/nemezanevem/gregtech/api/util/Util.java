package net.nemezanevem.gregtech.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.IReverseTag;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.GTValues;
import net.nemezanevem.gregtech.api.block.machine.MachineBlockItem;
import net.nemezanevem.gregtech.api.blockentity.MetaTileEntity;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.api.capability.IMultipleTankHandler;
import net.nemezanevem.gregtech.api.registry.material.MaterialRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialFlagRegistry;
import net.nemezanevem.gregtech.api.registry.material.info.MaterialIconSetRegistry;
import net.nemezanevem.gregtech.api.registry.material.properties.MaterialPropertyRegistry;
import net.nemezanevem.gregtech.api.registry.tileentity.MetaTileEntityRegistry;
import net.nemezanevem.gregtech.api.unification.material.Material;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.PropertyKey;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialFlag;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;

import static net.nemezanevem.gregtech.api.GTValues.V;

public class Util {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();

    private static final NavigableMap<Long, Byte> tierByVoltage = new TreeMap<>();

    static {
        for (int i = 0; i < V.length; i++) {
            tierByVoltage.put(V[i], (byte) i);
        }
    }

    /**
     * Default function for tank sizes, takes a tier input and returns the corresponding size
     */
    public static final Function<Integer, Integer> defaultTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 8000;
        if (tier == GTValues.MV)
            return 12000;
        if (tier == GTValues.HV)
            return 16000;
        if (tier == GTValues.EV)
            return 32000;
        // IV+
        return 64000;
    };

    /**
     * Alternative function for tank sizes, takes a tier input and returns the corresponding size
     * <p>
     * This function scales the same as the default function except it stops scaling past HV
     */
    public static final Function<Integer, Integer> hvCappedTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 8000;
        if (tier == GTValues.MV)
            return 12000;
        // HV+
        return 16000;
    };

    /**
     * Alternative function for tank sizes, takes a tier input and returns the corresponding size
     * <p>
     * This function is meant for use with machines that need very large tanks, it stops scaling past HV
     */
    public static final Function<Integer, Integer> largeTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 32000;
        if (tier == GTValues.MV)
            return 48000;
        // HV+
        return 64000;
    };

    /**
     * Alternative function for tank sizes, takes a tier input and returns the corresponding size
     * <p>
     * This function is meant for use with generators
     */
    public static final Function<Integer, Integer> steamGeneratorTankSizeFunction = tier -> Math.min(16000 * (1 << (tier - 1)), 64000);

    public static final Function<Integer, Integer> genericGeneratorTankSizeFunction = tier -> Math.min(4000 * (1 << (tier - 1)), 16000);


    public static ResourceLocation gtResource(String path) {
        return new ResourceLocation(GregTech.MODID, path);
    }

    /**
     * Does almost the same thing as .to(LOWER_UNDERSCORE, string), but it also inserts underscores between words and numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maragingSteel300" -> "maraging_steel_300"
     */
    public static String toLowerCaseUnderscore(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (i != 0 && (Character.isUpperCase(string.charAt(i)) || (
                    Character.isDigit(string.charAt(i - 1)) ^ Character.isDigit(string.charAt(i)))))
                result.append("_");
            result.append(Character.toLowerCase(string.charAt(i)));
        }
        return result.toString();
    }

    /**
     * Does almost the same thing as LOWER_UNDERSCORE.to(UPPER_CAMEL, string), but it also removes underscores before numbers.
     *
     * @param string Any string with ASCII characters.
     * @return A string that is all lowercase, with underscores inserted before word/number boundaries: "maraging_steel_300" -> "maragingSteel300"
     */
    public static String lowerUnderscoreToUpperCamel(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            if (string.charAt(i) == '_')
                continue;
            if (i == 0 || string.charAt(i - 1) == '_') {
                result.append(Character.toUpperCase(string.charAt(i)));
            } else {
                result.append(string.charAt(i));
            }
        }
        return result.toString();
    }

    /**
     * @return lowest tier that can handle passed voltage
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > V[GTValues.MAX]) return GTValues.MAX;
        return tierByVoltage.ceilingEntry(voltage).getValue();
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest tier below or equal to the voltage value given
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < V[GTValues.ULV]) return GTValues.ULV;
        return tierByVoltage.floorEntry(voltage).getValue();
    }

    /**
     * Attempts to merge given ItemStack with ItemStacks in slot list supplied
     * If it's not possible to merge it fully, it will attempt to insert it into first empty slots
     *
     * @param itemStack item stack to merge. It WILL be modified.
     * @param simulate  if true, stack won't actually modify items in other slots
     * @return if merging of at least one item succeed, false otherwise
     */
    public static boolean mergeItemStack(ItemStack itemStack, List<Slot> slots, boolean simulate) {
        if (itemStack.isEmpty())
            return false; //if we are merging empty stack, return

        boolean merged = false;
        //iterate non-empty slots first
        //to try to insert stack into them
        for (Slot slot : slots) {
            if (!slot.mayPlace(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            ItemStack stackInSlot = slot.getItem();
            if (!ItemStack.matches(itemStack, stackInSlot) ||
                    !ItemStack.tagMatches(itemStack, stackInSlot))
                continue; //if itemstacks don't match, continue
            int slotMaxStackSize = Math.min(stackInSlot.getMaxStackSize(), slot.getMaxStackSize(stackInSlot));
            int amountToInsert = Math.min(itemStack.getCount(), slotMaxStackSize - stackInSlot.getCount());
            // Need to check <= 0 for the PA, which could have this value negative due to slot limits in the Machine Access Interface
            if (amountToInsert <= 0)
                continue; //if we can't insert anything, continue
            //shrink our stack, grow slot's stack and mark slot as changed
            if (!simulate) {
                stackInSlot.grow(amountToInsert);
            }
            itemStack.shrink(amountToInsert);
            slot.setChanged();
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }

        //then try to insert itemstack into empty slots
        //breaking it into pieces if needed
        for (Slot slot : slots) {
            if (!slot.mayPlace(itemStack))
                continue; //if itemstack cannot be placed into that slot, continue
            if (slot.hasItem())
                continue; //if slot contains something, continue
            int amountToInsert = Math.min(itemStack.getCount(), slot.getMaxStackSize(itemStack));
            if (amountToInsert == 0)
                continue; //if we can't insert anything, continue
            //split our stack and put result in slot
            ItemStack stackInSlot = itemStack.split(amountToInsert);
            if (!simulate) {
                slot.set(stackInSlot);
            }
            merged = true;
            if (itemStack.isEmpty())
                return true; //if we inserted all items, return
        }
        return merged;
    }


    public static int getRedstonePower(Level world, BlockPos blockPos, Direction side) {
        BlockPos offsetPos = blockPos.offset(side.getNormal());
        int worldPower = world.getSignal(offsetPos, side);
        if (worldPower < 15) {
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() instanceof RedStoneWireBlock) {
                int wirePower = offsetState.getValue(RedStoneWireBlock.POWER);
                return Math.max(worldPower, wirePower);
            }
        }
        return worldPower;
    }

    /**
     * If pos of this world loaded
     */
    public static boolean isPosChunkLoaded(Level world, BlockPos pos) {
        return !world.getChunkAt(pos).isEmpty();
    }


    /**
     * @return a list of itemstack linked with given item handler
     * modifications in list will reflect on item handler and wise-versa
     */
    public static List<ItemStack> itemHandlerToList(IItemHandlerModifiable inputs) {
        return new AbstractList<ItemStack>() {
            @Override
            public ItemStack set(int index, ItemStack element) {
                ItemStack oldStack = inputs.getStackInSlot(index);
                inputs.setStackInSlot(index, element == null ? ItemStack.EMPTY : element);
                return oldStack;
            }

            @Override
            public ItemStack get(int index) {
                return inputs.getStackInSlot(index);
            }

            @Override
            public int size() {
                return inputs.getSlots();
            }
        };
    }

    /**
     * @return a list of fluidstack linked with given fluid handler
     * modifications in list will reflect on fluid handler and wise-versa
     */
    public static List<FluidStack> fluidHandlerToList(IMultipleTankHandler fluidInputs) {
        List<IFluidTank> backedList = fluidInputs.getFluidTanks();
        return new AbstractList<FluidStack>() {
            @Override
            public FluidStack set(int index, FluidStack element) {
                IFluidTank fluidTank = backedList.get(index);
                FluidStack oldStack = fluidTank.getFluid();
                if (!(fluidTank instanceof FluidTank))
                    return oldStack;
                ((FluidTank) backedList.get(index)).setFluid(element);
                return oldStack;
            }

            @Override
            public FluidStack get(int index) {
                return backedList.get(index).getFluid();
            }

            @Override
            public int size() {
                return backedList.size();
            }
        };
    }

    public static NonNullList<ItemStack> copyStackList(List<ItemStack> itemStacks) {
        ItemStack[] stacks = new ItemStack[itemStacks.size()];
        for (int i = 0; i < itemStacks.size(); i++) {
            stacks[i] = copy(itemStacks.get(i));
        }
        return NonNullList.of(ItemStack.EMPTY, stacks);
    }

    public static NonNullList<FluidStack> copyFluidList(List<FluidStack> fluidStacks) {
        FluidStack[] stacks = new FluidStack[fluidStacks.size()];
        for (int i = 0; i < fluidStacks.size(); i++) stacks[i] = fluidStacks.get(i).copy();
        return NonNullList.of(FluidStack.EMPTY, stacks);
    }

    public static ItemStack copy(ItemStack... stacks) {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) return stack.copy();
        return ItemStack.EMPTY;
    }

    /**
     * Attempts to merge given ItemStack with ItemStacks in list supplied
     * growing up to their max stack size
     *
     * @param stackToAdd item stack to merge.
     * @return a list of stacks, with optimized stack sizes
     */

    public static List<ItemStack> addStackToItemStackList(ItemStack stackToAdd, List<ItemStack> itemStackList) {
        if (!itemStackList.isEmpty()) {
            for (int i = 0; i < itemStackList.size(); i++) {
                ItemStack stackInList = itemStackList.get(i);
                if (ItemStackHashStrategy.comparingAllButCount().equals(stackInList, stackToAdd)) {
                    if (stackInList.getCount() < stackInList.getMaxStackSize()) {
                        int insertable = stackInList.getMaxStackSize() - stackInList.getCount();
                        if (insertable >= stackToAdd.getCount()) {
                            stackInList.grow(stackToAdd.getCount());
                            stackToAdd = ItemStack.EMPTY;
                        } else {
                            stackInList.grow(insertable);
                            stackToAdd = stackToAdd.copy();
                            stackToAdd.setCount(stackToAdd.getCount() - insertable);
                        }
                        if (stackToAdd.isEmpty()) {
                            break;
                        }
                    }
                }
            }
            if (!stackToAdd.isEmpty()) {
                itemStackList.add(stackToAdd);
            }
        } else {
            itemStackList.add(stackToAdd.copy());
        }
        return itemStackList;
    }

    public static boolean isBetweenInclusive(long start, long end, long value) {
        return start <= value && value <= end;
    }

    public static int getExplosionPower(long voltage) {
        return getTierByVoltage(voltage) + 1;
    }


    /**
     * @param values to find the mean of
     * @return the mean value
     */
    public static long mean(@Nonnull long[] values) {
        if (values.length == 0L)
            return 0L;

        long sum = 0L;
        for (long v : values)
            sum += v;
        return sum / values.length;
    }

    /**
     * @param world the {@link Level} to get the average tick time of
     * @return the mean tick time
     */
    public static double getMeanTickTime(@Nonnull Level world) {
        return mean(Objects.requireNonNull(world.getServer()).tickTimes) * 1.0E-6D;
    }

    public static <M> M selectItemInList(int index, M replacement, List<M> list, Class<M> minClass) {
        if (list.isEmpty())
            return replacement;

        M maybeResult;
        if (list.size() <= index) {
            maybeResult = list.get(list.size() - 1);
        } else if (index < 0) {
            maybeResult = list.get(0);
        } else maybeResult = list.get(index);

        if (maybeResult != null) return maybeResult;
        return replacement;
    }


    public static boolean harvestBlock(Level level, BlockPos pos, Player player) {
        BlockState blockState = level.getBlockState(pos);
        BlockEntity tileEntity = level.getBlockEntity(pos);

        if (blockState.isAir()) {
            return false;
        }

        if (!blockState.getBlock().canHarvestBlock(blockState, level, pos, player)) {
            return false;
        }

        int expToDrop = 0;
        if (!level.isClientSide) {
            ServerPlayer playerMP = (ServerPlayer) player;
            expToDrop = ForgeHooks.onBlockBreakEvent(level, playerMP.gameMode.getGameModeForPlayer(), playerMP, pos);
            if (expToDrop == -1) {
                //notify client if block can't be removed because of BreakEvent cancelled on server side
                playerMP.connection.send(new ClientboundBlockUpdatePacket(level, pos));
                return false;
            }
        }

        level.levelEvent(player, 2001, pos, Block.getId(blockState));

        FluidState fluidstate = level.getFluidState(pos);
        boolean wasRemovedByPlayer = blockState.onDestroyedByPlayer(level, pos, player, !player.getAbilities().instabuild, fluidstate);
        if (wasRemovedByPlayer) {
            blockState.getBlock().destroy(level, pos, blockState);

            if (!level.isClientSide && !player.isCreative()) {
                ItemStack stackInHand = player.getMainHandItem();
                int fortuneLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.BLOCK_FORTUNE, stackInHand);
                int silkTouchLevel = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.SILK_TOUCH, stackInHand);
                if (expToDrop > 0) {
                    blockState.getExpDrop(level, level.random, pos, fortuneLevel, silkTouchLevel);
                }
            }
        }

        if (!level.isClientSide) {
            ServerPlayer playerMP = (ServerPlayer) player;
            playerMP.connection.send(new ClientboundBlockUpdatePacket(level, pos));
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, mc.hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult)mc.hitResult).getDirection() : null));
            });
        }
        return wasRemovedByPlayer;
    }

    public static void writeItems(IItemHandler handler, String tagName, CompoundTag tag) {
        ListTag tagList = new ListTag();

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                CompoundTag stackTag = new CompoundTag();
                stackTag.putInt("Slot", i);
                handler.getStackInSlot(i).save(stackTag);
                tagList.add(stackTag);
            }
        }

        tag.put(tagName, tagList);
    }

    public static void readItems(IItemHandlerModifiable handler, String tagName, CompoundTag tag) {
        if (tag.contains(tagName)) {
            ListTag tagList = tag.getList(tagName, Tag.TAG_COMPOUND);

            for (int i = 0; i < tagList.size(); i++) {
                int slot = tagList.getCompound(i).getInt("Slot");

                if (slot >= 0 && slot < handler.getSlots()) {
                    handler.setStackInSlot(slot, ItemStack.of(tagList.getCompound(i)));
                }
            }
        }
    }

    public static MetaTileEntity getMetaTileEntity(BlockGetter world, BlockPos pos) {
        if (world == null || pos == null) return null;
        BlockEntity te = world.getBlockEntity(pos);
        return te instanceof IGregTechTileEntity gtBe ? gtBe.getMetaTileEntity() : null;
    }

    public static MetaTileEntity getMetaTileEntity(ItemStack stack) {
        if (!(stack.getItem() instanceof MachineBlockItem blockItem)) return null;
        return MetaTileEntityRegistry.META_TILE_ENTITIES_BUILTIN.get().getValue(Util.getId(blockItem));
    }

    public static boolean canSeeSunClearly(Level world, BlockPos blockPos) {
        if (!world.canSeeSky(blockPos.above())) {
            return false;
        }
        Holder<Biome> biome = world.getBiome(blockPos.above());
        if (world.isRaining()) {
            if (biome.value().getPrecipitation() != Biome.Precipitation.NONE) {
                return false;
            }
        }
        Optional<IReverseTag<Biome>> biomeTags = ForgeRegistries.BIOMES.tags().getReverseTag(biome.value());
        if (biomeTags.isPresent() && biomeTags.get().containsTag(BiomeTags.IS_END)) {
            return false;
        }
        return world.isDay();
    }

    public static boolean arePosEqual(BlockPos pos1, BlockPos pos2) {
        return pos1.getX() == pos2.getX() & pos1.getY() == pos2.getY() & pos1.getZ() == pos2.getZ();
    }


    public static String formatNumbers(long number) {
        return NUMBER_FORMAT.format(number);
    }

    public static String formatNumbers(double number) {
        return NUMBER_FORMAT.format(number);
    }

    //just because CCL uses a different color format
    //0xRRGGBBAA
    public static int convertRGBtoOpaqueRGBA_CL(int colorValue) {
        return convertRGBtoRGBA_CL(colorValue, 255);
    }

    public static int convertRGBtoRGBA_CL(int colorValue, int opacity) {
        return colorValue << 8 | (opacity & 0xFF);
    }

    public static int convertOpaqueRGBA_CLtoRGB(int colorAlpha) {
        return colorAlpha >>> 8;
    }

    //0xAARRGGBB
    public static int convertRGBtoOpaqueRGBA_MC(int colorValue) {
        return convertRGBtoOpaqueRGBA_MC(colorValue, 255);
    }

    public static int convertRGBtoOpaqueRGBA_MC(int colorValue, int opacity) {
        return opacity << 24 | colorValue;
    }

    public static int convertOpaqueRGBA_MCtoRGB(int alphaColor) {
        return alphaColor & 0xFFFFFF;
    }

    public static boolean isOre(ItemStack item) {
        TagPrefix orePrefix = TagUnifier.getPrefix(item.getItem());
        return orePrefix != null && orePrefix.name().startsWith("ore");
    }

    public static ResourceLocation getId(Material flag) {
        return MaterialRegistry.MATERIALS_BUILTIN.get().getKey(flag);
    }
    public static ResourceLocation getId(MaterialFlag flag) {
        return MaterialFlagRegistry.MATERIAL_FLAGS_BUILTIN.get().getKey(flag);
    }
    public static ResourceLocation getId(MaterialIconSet set) {
        return MaterialIconSetRegistry.MATERIAL_ICON_SETS_BUILTIN.get().getKey(set);
    }
    public static ResourceLocation getId(PropertyKey<?> key) {
        return MaterialPropertyRegistry.MATERIAL_PROPERTIES_BUILTIN.get().getKey(key);
    }
    public static ResourceLocation getId(Item key) {
        return ForgeRegistries.ITEMS.getKey(key);
    }
    public static ResourceLocation getId(Block key) {
        return ForgeRegistries.BLOCKS.getKey(key);
    }
}

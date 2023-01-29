package net.nemezanevem.gregtech.common.item.behavior;

import codechicken.lib.raytracer.RayTracer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.nemezanevem.gregtech.api.block.machine.BlockMachine;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.ModularUI;
import net.nemezanevem.gregtech.api.gui.widgets.ClickButtonWidget;
import net.nemezanevem.gregtech.api.gui.widgets.ImageCycleButtonWidget;
import net.nemezanevem.gregtech.api.gui.widgets.SimpleTextWidget;
import net.nemezanevem.gregtech.api.gui.widgets.TextFieldWidget2;
import net.nemezanevem.gregtech.api.item.gui.ItemUIFactory;
import net.nemezanevem.gregtech.api.item.gui.PlayerInventoryHolder;
import net.nemezanevem.gregtech.api.item.metaitem.stats.IItemBehaviour;
import net.nemezanevem.gregtech.api.blockentity.interfaces.IGregTechTileEntity;
import net.nemezanevem.gregtech.common.metatileentities.MetaTileEntityClipboard;

import java.util.ArrayList;
import java.util.List;

import static net.nemezanevem.gregtech.GregTech.MACHINE;
import static net.nemezanevem.gregtech.common.metatileentities.MetaTileEntities.CLIPBOARD_TILE;

public class ClipboardBehavior implements IItemBehaviour, ItemUIFactory {
    public static final int MAX_PAGES = 25;
    private static final int TEXT_COLOR = 0x1E1E1E;

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, Player entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.CLIPBOARD_BACKGROUND, 186, 263);
        initNBT(holder.getCurrentItem());

        List<TextFieldWidget2> textFields = new ArrayList<>();

        builder.image(28, 28, 130, 12, GuiTextures.CLIPBOARD_TEXT_BOX);
        textFields.add(new TextFieldWidget2(30, 30, 126, 9, () -> getTitle(holder), val -> setTitle(holder, val))
                .setMaxLength(25)
                .setCentered(true)
                .setTextColor(TEXT_COLOR));

        for (int i = 0; i < 8; i++) {
            int finalI = i;
            builder.widget(new ImageCycleButtonWidget(14, 55 + 22 * i, 15, 15, GuiTextures.CLIPBOARD_BUTTON, 4,
                    () -> getButtonState(holder, finalI), (x) -> setButton(holder, finalI, x)));

            builder.image(32, 58 + 22 * i, 140, 12, GuiTextures.CLIPBOARD_TEXT_BOX);
            textFields.add(new TextFieldWidget2(34, 60 + 22 * i, 136, 9, () -> getString(holder, finalI), val -> setString(holder, finalI, val))
                    .setMaxLength(23)
                    .setTextColor(TEXT_COLOR));
        }

        for (TextFieldWidget2 textField : textFields) {
            builder.widget(textField.setOnFocus(textField2 -> textFields.forEach(textField3 -> {
                if (textField3 != textField2) {
                    textField3.unFocus();
                }
            })));
        }

        builder.widget(new ClickButtonWidget(38, 231, 16, 16, "", (x) -> incrPageNum(holder, x.isShiftClick ? -10 : -1))
                .setButtonTexture(GuiTextures.BUTTON_LEFT).setShouldClientCallback(true));
        builder.widget(new ClickButtonWidget(132, 231, 16, 16, "", (x) -> incrPageNum(holder, x.isShiftClick ? 10 : 1))
                .setButtonTexture(GuiTextures.BUTTON_RIGHT).setShouldClientCallback(true));
        builder.widget(new SimpleTextWidget(93, 240, "", TEXT_COLOR,
                () -> (getPageNum(holder) + 1) + " / " + MAX_PAGES, true));

        builder.shouldColor(false);
        return builder.build(holder, entityPlayer);
    }

    public ModularUI createMTEUI(PlayerInventoryHolder holder, Player entityPlayer) { // So that people don't click on any text fields
        initNBT(holder.getCurrentItem());
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.CLIPBOARD_PAPER_BACKGROUND, 170, 238);

        builder.image(18, 8, 130, 14, GuiTextures.CLIPBOARD_TEXT_BOX);
        builder.widget(new SimpleTextWidget(20, 10, "", TEXT_COLOR, () -> getTitle(holder), true).setCenter(false));


        for (int i = 0; i < 8; i++) {
            int finalI = i;
            builder.widget(new ImageCycleButtonWidget(6, 37 + 20 * i, 15, 15, GuiTextures.CLIPBOARD_BUTTON, 4,
                    () -> getButtonState(holder, finalI), (x) -> setButton(holder, finalI, x)));
            builder.image(22, 38 + 20 * i, 140, 12, GuiTextures.CLIPBOARD_TEXT_BOX);
            builder.widget(new SimpleTextWidget(24, 40 + 20 * i, "", TEXT_COLOR, () -> getString(holder, finalI), true).setCenter(false));
        }

        builder.widget(new ClickButtonWidget(30, 200, 16, 16, "", (x) -> incrPageNum(holder, x.isShiftClick ? -10 : -1))
                .setButtonTexture(GuiTextures.BUTTON_LEFT).setShouldClientCallback(true));
        builder.widget(new ClickButtonWidget(124, 200, 16, 16, "", (x) -> incrPageNum(holder, x.isShiftClick ? 10 : 1))
                .setButtonTexture(GuiTextures.BUTTON_RIGHT).setShouldClientCallback(true));
        builder.widget(new SimpleTextWidget(85, 208, "", TEXT_COLOR,
                () -> (getPageNum(holder) + 1) + " / " + MAX_PAGES, true));

        builder.shouldColor(false);
        return builder.build(holder, entityPlayer);
    }

    private static CompoundTag getPageCompound(ItemStack stack) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return null;
        short pageNum = stack.getTag().getShort("PageIndex");
        return stack.getTag().getCompound("Page" + pageNum);
    }

    private static void setPageCompound(ItemStack stack, CompoundTag pageCompound) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        short pageNum = stack.getTag().getShort("PageIndex");
        stack.getTag().put("Page" + pageNum, pageCompound);
    }

    private static void initNBT(ItemStack stack) {
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        CompoundTag tagCompound = stack.getTag();
        if (tagCompound == null) {
            tagCompound = new CompoundTag();
            tagCompound.putShort("PageIndex", (short) 0);
            tagCompound.putShort("TotalPages", (short) 0);

            CompoundTag pageCompound = new CompoundTag();
            pageCompound.putShort("ButStat", (short) 0);
            pageCompound.putString("Title", "");
            for (int i = 0; i < 8; i++) {
                pageCompound.putString("Task" + i, "");
            }

            for (int i = 0; i < MAX_PAGES; i++) {
                tagCompound.put("Page" + i, pageCompound.copy());
            }

            stack.setTag(tagCompound);
        }
    }

    private static void setButton(PlayerInventoryHolder holder, int pos, int newState) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        CompoundTag tagCompound = getPageCompound(stack);
        short buttonState;
        buttonState = tagCompound.getShort("ButStat");

        short clearedState = (short) (buttonState & ~(3 << (pos * 2))); // Clear out the desired slot
        buttonState = (short) (clearedState | (newState << (pos * 2))); // And add the new state back in

        tagCompound.putShort("ButStat", buttonState);
        setPageCompound(stack, tagCompound);
    }

    private static int getButtonState(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return 0;
        CompoundTag tagCompound = getPageCompound(stack);
        short buttonState;
        buttonState = tagCompound.getShort("ButStat");
        return ((buttonState >> pos * 2) & 3);
    }

    private static void setString(PlayerInventoryHolder holder, int pos, String newString) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        CompoundTag tagCompound = getPageCompound(stack);
        tagCompound.putString("Task" + pos, newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getString(PlayerInventoryHolder holder, int pos) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return "";
        CompoundTag tagCompound = getPageCompound(stack);
        return tagCompound.getString("Task" + pos);
    }

    private static void setTitle(PlayerInventoryHolder holder, String newString) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        CompoundTag tagCompound = getPageCompound(stack);
        assert tagCompound != null;
        tagCompound.putString("Title", newString);
        setPageCompound(stack, tagCompound);
    }

    private static String getTitle(PlayerInventoryHolder holder) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return "";
        CompoundTag tagCompound = getPageCompound(stack);
        return tagCompound.getString("Title");
    }

    private static int getPageNum(PlayerInventoryHolder holder) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return 1;
        CompoundTag tagCompound = stack.getTag();
        return tagCompound.getInt("PageIndex");
    }

    private static void incrPageNum(PlayerInventoryHolder holder, int increment) {
        ItemStack stack = holder.getCurrentItem();
        if (!MetaItems.CLIPBOARD.isItemEqual(stack))
            return;
        CompoundTag tagCompound = stack.getTag();
        assert tagCompound != null;

        int currentIndex = tagCompound.getInt("PageIndex");
        // Clamps currentIndex between 0 and MAX_PAGES.
        tagCompound.putInt("PageIndex", Math.max(Math.min(currentIndex + increment, MAX_PAGES - 1), 0));
        stack.setTag(tagCompound);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemRightClick(Level world, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (!world.isClientSide && RayTracer.retrace(player).getType() != HitResult.Type.BLOCK) { // So that the player doesn't place a clipboard before suddenly getting the GUI
            PlayerInventoryHolder holder = new PlayerInventoryHolder(player, hand);
            holder.openUI();
        }
        return InteractionResultHolder.success(heldItem);
    }

    @Override
    public InteractionResultHolder<ItemStack> onItemUse(Player player, Level world, BlockPos pos, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        if (!world.isClientSide && facing.getAxis() != Direction.Axis.Y) {
            ItemStack heldItem = player.getItemInHand(hand).copy();
            heldItem.setCount(1); // don't place multiple items at a time
            // Make sure it's the right block
            BlockState testState = world.getBlockState(pos);
            Block testBlock = testState.getBlock();
            if (!testState.isAir() && testState.isFaceSturdy(world, pos, facing)) {
                // Step away from the block so that you don't replace it, and then give it our fun blockstate
                BlockPos shiftedPos = pos.offset(facing);
                Block shiftedBlock = world.getBlockState(shiftedPos).getBlock();
                if (shiftedBlock.isAir(world.getBlockState(shiftedPos), world, shiftedPos)) {
                    BlockState state = MACHINE.get().defaultBlockState();
                    world.setBlock(shiftedPos, state, 3);
                    // Get new TE
                    ((BlockMachine)shiftedBlock).newBlockEntity(world, state);
                    // And manipulate it to our liking
                    IGregTechTileEntity holder = (IGregTechTileEntity) world.getBlockEntity(shiftedPos);
                    if (holder != null) {
                        MetaTileEntityClipboard clipboard = (MetaTileEntityClipboard) holder.setMetaTileEntity(CLIPBOARD_TILE);
                        if (clipboard != null) {
                            clipboard.initializeClipboard(heldItem);
                            clipboard.setFrontFacing(facing.getOpposite());
                            ItemStack returnedStack = player.getItemInHand(hand);
                            if (!player.isCreative()) {
                                returnedStack.setCount(player.getItemInHand(hand).getCount() - 1);
                            }
                            return InteractionResultHolder.success(returnedStack);
                        }
                    }
                }
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}

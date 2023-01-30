package net.nemezanevem.gregtech.api.gui.widgets;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.ingredient.IIngredientSlot;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;
import net.nemezanevem.gregtech.api.util.FluidTooltipUtil;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;
import net.nemezanevem.gregtech.api.util.TextFormattingUtil;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TankWidget extends Widget implements IIngredientSlot {

    public final IFluidTank fluidTank;

    public int fluidRenderOffset = 1;
    private boolean hideTooltip;
    private boolean alwaysShowFull;
    private boolean drawHoveringText;

    private boolean allowClickFilling;
    private boolean allowClickEmptying;

    private IGuiTexture[] backgroundTexture;
    private IGuiTexture overlayTexture;

    protected FluidStack lastFluidInTank;
    private int lastTankCapacity;
    protected boolean isClient;

    public TankWidget(IFluidTank fluidTank, int x, int y, int width, int height) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.drawHoveringText = true;
    }

    public TankWidget setClient() {
        this.isClient = true;
        this.lastFluidInTank = fluidTank != null ? fluidTank.getFluid() != null ? fluidTank.getFluid().copy() : null : null;
        this.lastTankCapacity = fluidTank != null ? fluidTank.getCapacity() : 0;
        return this;
    }

    public TankWidget setHideTooltip(boolean hideTooltip) {
        this.hideTooltip = hideTooltip;
        return this;
    }

    public TankWidget setDrawHoveringText(boolean drawHoveringText) {
        this.drawHoveringText = drawHoveringText;
        return this;
    }

    public TankWidget setAlwaysShowFull(boolean alwaysShowFull) {
        this.alwaysShowFull = alwaysShowFull;
        return this;
    }

    public TankWidget setBackgroundTexture(IGuiTexture... backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public TankWidget setOverlayTexture(IGuiTexture overlayTexture) {
        this.overlayTexture = overlayTexture;
        return this;
    }

    public TankWidget setFluidRenderOffset(int fluidRenderOffset) {
        this.fluidRenderOffset = fluidRenderOffset;
        return this;
    }

    public TankWidget setContainerClicking(boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        if (!(fluidTank instanceof IFluidHandler))
            throw new IllegalStateException("Container IO is only supported for fluid tanks that implement IFluidHandler");
        this.allowClickFilling = allowClickContainerFilling;
        this.allowClickEmptying = allowClickContainerEmptying;
        return this;
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank;
        }
        return null;
    }

    public String getFormattedFluidAmount() {
        return String.format("%,d", lastFluidInTank == null ? 0 : lastFluidInTank.getAmount());
    }

    public String getFluidLocalizedName() {
        return lastFluidInTank == null ? "" : lastFluidInTank.getTranslationKey();
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            for (IGuiTexture textureArea : backgroundTexture) {
                textureArea.draw(pos.x, pos.y, size.width, size.height);
            }
        }
        //do not draw fluids if they are handled by JEI - it draws them itself
        if (lastFluidInTank != null && !gui.isJEIHandled) {
            RenderSystem.disableBlend();
            FluidStack stackToDraw = lastFluidInTank;
            int drawAmount = alwaysShowFull ? lastFluidInTank.getAmount() : lastTankCapacity;
            if (alwaysShowFull && lastFluidInTank.getAmount() == 0) {
                stackToDraw = lastFluidInTank.copy();
                stackToDraw.setAmount(1);
                drawAmount = 1;
            }
            RenderUtil.drawFluidForGui(stackToDraw, drawAmount,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset,
                    size.width - fluidRenderOffset, size.height - fluidRenderOffset);

            if (alwaysShowFull && !hideTooltip && drawHoveringText) {
                poseStack.pushPose();
                poseStack.scale(0.5f, 0.5f, 1);

                String s = TextFormattingUtil.formatLongToCompactString(lastFluidInTank.getAmount(), 4) + "L";

                Font fontRenderer = Minecraft.getInstance().font;
                fontRenderer.drawShadow(poseStack, s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.width(s) + 21, (pos.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
                poseStack.popPose();
            }
            RenderSystem.enableBlend();
        }
        if (overlayTexture != null) {
            overlayTexture.draw(pos.x, pos.y, size.width, size.height);
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (!hideTooltip && !gui.isJEIHandled && isMouseOverElement(mouseX, mouseY)) {
            List<Component> tooltips = new ArrayList<>();
            if (lastFluidInTank != null) {
                Fluid fluid = lastFluidInTank.getFluid();
                tooltips.add(fluid.getFluidType().getDescription(lastFluidInTank));

                // Amount Tooltip
                tooltips.add(Component.translatable("gregtech.fluid.amount", lastFluidInTank.getAmount(), lastTankCapacity));

                // Add various tooltips from the material
                List<Component> formula = FluidTooltipUtil.getFluidTooltip(lastFluidInTank);
                if (formula != null) {
                    for (Component s : formula) {
                        if (s.getString().matches(s.getContents().visit(Optional::of).get())) continue;
                        tooltips.add(s);
                    }
                }

            } else {
                tooltips.add(Component.translatable("gregtech.fluid.empty"));
                tooltips.add(Component.translatable("gregtech.fluid.amount", 0, lastTankCapacity));
            }
            if(allowClickEmptying && allowClickFilling) {
                tooltips.add(Component.empty()); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(Component.translatable("gregtech.fluid.click_combined"));
            }
            else if (allowClickFilling) {
                tooltips.add(Component.empty()); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(Component.translatable("gregtech.fluid.click_to_fill"));
            }
            else if (allowClickEmptying) {
                tooltips.add(Component.empty()); // Add an empty line to separate from the bottom material tooltips
                tooltips.add(Component.translatable("gregtech.fluid.click_to_empty"));
            }
            drawHoveringText(poseStack, ItemStack.EMPTY, tooltips, 300, mouseX, mouseY);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    @Override
    public void updateScreenOnFrame() {
        if (isClient) {
            FluidStack fluidStack = fluidTank.getFluid();
            if (fluidTank.getCapacity() != lastTankCapacity) {
                this.lastTankCapacity = fluidTank.getCapacity();
            }
            if (fluidStack == null && lastFluidInTank != null) {
                this.lastFluidInTank = null;

            } else if (fluidStack != null) {
                if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                    this.lastFluidInTank = fluidStack.copy();
                } else if (fluidStack.getAmount() != lastFluidInTank.getAmount()) {
                    this.lastFluidInTank.setAmount(fluidStack.getAmount());
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack fluidStack = fluidTank.getFluid();
        if (fluidTank.getCapacity() != lastTankCapacity) {
            this.lastTankCapacity = fluidTank.getCapacity();
            writeUpdateInfo(0, buffer -> buffer.writeVarInt(lastTankCapacity));
        }
        if (fluidStack == null && lastFluidInTank != null) {
            this.lastFluidInTank = null;
            writeUpdateInfo(1, buffer -> {
            });
        } else if (fluidStack != null) {
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                CompoundTag fluidStackTag = fluidStack.writeToNBT(new CompoundTag());
                writeUpdateInfo(2, buffer -> buffer.writeNbt(fluidStackTag));
            } else if (fluidStack.getAmount() != lastFluidInTank.getAmount()) {
                this.lastFluidInTank.setAmount(fluidStack.getAmount());
                writeUpdateInfo(3, buffer -> buffer.writeVarInt(lastFluidInTank.getAmount()));
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            CompoundTag fluidStackTag;
            fluidStackTag = buffer.readAnySizeNbt();
            this.lastFluidInTank = FluidStack.loadFluidStackFromNBT(fluidStackTag);
        } else if (id == 3 && lastFluidInTank != null) {
            this.lastFluidInTank.setAmount(buffer.readVarInt());
        }

        if (id == 4) {
            ItemStack currentStack = gui.player.getInventory().getSelected();
            int newStackSize = buffer.readVarInt();
            currentStack.setCount(newStackSize);
            gui.player.getInventory().setPickedItem(currentStack);
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            boolean isShiftKeyDown = buffer.readBoolean();
            int clickResult = tryClickContainer(isShiftKeyDown);
            if (clickResult >= 0) {
                writeUpdateInfo(4, buf -> buf.writeVarInt(clickResult));
            }
        }
    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        Player player = gui.player;
        ItemStack currentStack = player.getInventory().getSelected();
        if (!currentStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent())
            return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (allowClickFilling && fluidTank.getFluidAmount() > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = fluidTank.getFluid();
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.getInventory().add(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryFillContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedFill = true;
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                        soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                gui.player.getInventory().setPickedItem(currentStack);
                return currentStack.getCount();
            }
        }

        if (allowClickEmptying) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack,
                        (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = result.getResult();
                if (!remainingStack.isEmpty() && !player.getInventory().add(remainingStack))
                    break; //do not continue if we can't add resulting container into inventory
                FluidUtil.tryEmptyContainer(currentStack, (IFluidHandler) fluidTank, Integer.MAX_VALUE, null, true);
                currentStack.shrink(1);
                performedEmptying = true;
            }
            FluidStack filledFluid = fluidTank.getFluid();
            if (performedEmptying && filledFluid != null) {
                SoundEvent soundevent = filledFluid.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                player.level.playSound(null, player.getX(), player.getY() + 0.5, player.getZ(),
                        soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                gui.player.getInventory().setPickedItem(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ItemStack currentStack = gui.player.getInventory().getSelected();
            if (button == 0 && (allowClickEmptying || allowClickFilling) &&
                    currentStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).isPresent()) {
                boolean isShiftKeyDown = Screen.hasShiftDown();
                writeClientAction(1, writer -> writer.writeBoolean(isShiftKeyDown));
                playButtonClickSound();
                return true;
            }
        }
        return false;
    }
}

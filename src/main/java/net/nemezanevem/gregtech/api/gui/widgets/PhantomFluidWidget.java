package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nemezanevem.gregtech.api.gui.GuiTextures;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.Widget;
import net.nemezanevem.gregtech.api.gui.ingredient.IGhostIngredientTarget;
import net.nemezanevem.gregtech.api.gui.ingredient.IIngredientSlot;
import net.nemezanevem.gregtech.api.gui.resources.IGuiTexture;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;
import net.nemezanevem.gregtech.api.util.TextFormattingUtil;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PhantomFluidWidget extends Widget implements IIngredientSlot, IGhostIngredientTarget {

    private FluidTank fluidTank = null;
    protected IGuiTexture backgroundTexture = GuiTextures.FLUID_SLOT;

    private Supplier<FluidStack> fluidStackSupplier;
    private Consumer<FluidStack> fluidStackUpdater;
    private Supplier<Boolean> showTipSupplier;
    private boolean isClient;
    private boolean showTip;
    protected FluidStack lastFluidStack;

    public PhantomFluidWidget(int xPosition, int yPosition, int width, int height, Supplier<FluidStack> fluidStackSupplier, Consumer<FluidStack> fluidStackUpdater) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.fluidStackSupplier = fluidStackSupplier;
        this.fluidStackUpdater = fluidStackUpdater;
    }

    public PhantomFluidWidget(int xPosition, int yPosition, int width, int height, FluidTank fluidTank) {
        super(new Position(xPosition, yPosition), new Size(width, height));
        this.fluidTank = fluidTank;
        this.fluidStackSupplier = fluidTank::getFluid;
        this.fluidStackUpdater = fluidTank::setFluid;
    }

    private FluidStack drainFrom(Object ingredient) {
        if (ingredient instanceof ItemStack itemStack) {
            LazyOptional<IFluidHandlerItem> fluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
            if (fluidHandler.isPresent())
                return fluidHandler.resolve().get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        }
        return null;
    }

    public PhantomFluidWidget showTip(boolean showTip) {
        this.showTip = showTip;
        return this;
    }

    public PhantomFluidWidget showTipSupplier(Supplier<Boolean> showTipSupplier) {
        this.showTipSupplier = showTipSupplier;
        return this;
    }

    public PhantomFluidWidget setFluidStackSupplier(Supplier<FluidStack> fluidStackSupplier, boolean isClient) {
        this.fluidStackSupplier = fluidStackSupplier;
        this.isClient = isClient;
        return this;
    }

    public PhantomFluidWidget setFluidStackUpdater(Consumer<FluidStack> fluidStackUpdater, boolean isClient) {
        this.fluidStackUpdater = fluidStackUpdater;
        this.isClient = isClient;
        return this;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null) {
            return Collections.emptyList();
        }

        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<>() {
            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack ingredientStack;
                if (ingredient instanceof FluidStack)
                    ingredientStack = (FluidStack) ingredient;
                else
                    ingredientStack = drainFrom(ingredient);

                if (ingredientStack != null) {
                    CompoundTag tagCompound = ingredientStack.writeToNBT(new CompoundTag());
                    writeClientAction(2, buffer -> buffer.writeNbt(tagCompound));
                }

                if (isClient && fluidStackUpdater != null) {
                    fluidStackUpdater.accept(ingredientStack);
                }
            }
        });
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidStack;
        }
        return null;
    }

    public PhantomFluidWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    @Override
    public void updateScreenOnFrame() {
        super.updateScreenOnFrame();
        if (isClient && fluidStackSupplier != null) {
            this.lastFluidStack = fluidStackSupplier.get();
        }
    }

    @Override
    public void detectAndSendChanges() {
        FluidStack currentStack = fluidStackSupplier.get();
        if (currentStack == null && lastFluidStack != null) {
            this.lastFluidStack = null;
            writeUpdateInfo(1, buffer -> buffer.writeBoolean(false));
        } else if (currentStack != null && !currentStack.isFluidStackIdentical(lastFluidStack)) {
            this.lastFluidStack = currentStack;
            writeUpdateInfo(1, buffer -> {
                buffer.writeBoolean(true);
                buffer.writeNbt(currentStack.writeToNBT(new CompoundTag()));
            });
        }
        if (showTipSupplier != null && showTip != showTipSupplier.get()) {
            showTip = showTipSupplier.get();
            writeUpdateInfo(2, buffer -> buffer.writeBoolean(showTip));
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            if (buffer.readBoolean()) {
                CompoundTag tagCompound = buffer.readAnySizeNbt();
                this.lastFluidStack = FluidStack.loadFluidStackFromNBT(tagCompound);
            } else {
                this.lastFluidStack = null;
            }
        } else if (id == 2) {
            this.showTip = buffer.readBoolean();
        }
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        if (id == 1) {
            ClickData clickData = ClickData.readFromBuf(buffer);
            ItemStack itemStack = gui.player.getInventory().getSelected().copy();
            if (!itemStack.isEmpty()) {
                itemStack.setCount(1);
                LazyOptional<IFluidHandlerItem> fluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
                if (fluidHandler.isPresent()) {
                    FluidStack resultFluid = fluidHandler.resolve().get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                    fluidStackUpdater.accept(resultFluid);
                }
            } else {
                if (showTip) {
                    if (clickData.button == 2) {
                        fluidStackUpdater.accept(null);
                    } else if (clickData.button == 0) {
                        if (fluidStackSupplier.get() != null) {
                            FluidStack fluid = fluidStackSupplier.get().copy();
                            if (clickData.isShiftClick)
                                fluid.setAmount((fluid.getAmount() + 1) / 2);
                            else fluid.setAmount(fluid.getAmount() - 1);
                            if (fluid.getAmount() < 0) {
                                fluid.setAmount(Integer.MAX_VALUE / 2);
                            }
                            fluid.setAmount(Mth.clamp(fluid.getAmount(), 1, fluidTank.getCapacity()));
                            fluidStackUpdater.accept(fluid);
                        }
                    } else if (clickData.button == 1) {
                        if (fluidStackSupplier.get() != null) {
                            FluidStack fluid = fluidStackSupplier.get().copy();
                            if (clickData.isShiftClick)
                                fluid.setAmount(fluid.getAmount() * 2);
                            else fluid.setAmount(fluid.getAmount() + 1);
                            if (fluid.getAmount() < 0) {
                                fluid.setAmount(Integer.MAX_VALUE);
                            }
                            fluid.setAmount(Mth.clamp(fluid.getAmount(), 1, fluidTank.getCapacity()));
                            fluidStackUpdater.accept(fluid);
                        }
                    }
                } else {
                    fluidStackUpdater.accept(null);
                }
            }
        } else if (id == 2) {
            FluidStack fluidStack;
            fluidStack = FluidStack.loadFluidStackFromNBT(buffer.readAnySizeNbt());
            fluidStackUpdater.accept(fluidStack);
        } else if (id == 3) {
            WheelData wheelData = WheelData.readFromBuf(buffer);
            if (fluidStackSupplier.get() != null && fluidStackUpdater != null && showTip) {
                int multiplier = wheelData.isCtrlClick ? 100 : 1;
                multiplier *= wheelData.isShiftClick ? 10 : 1;
                FluidStack currentFluid = fluidStackSupplier.get().copy();
                int amount = wheelData.wheelDelta * multiplier;
                currentFluid.setAmount(Mth.clamp(currentFluid.getAmount() + amount, 1, fluidTank.getCapacity()));
                fluidStackUpdater.accept(currentFluid);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            ClickData clickData = new ClickData(button, Screen.hasShiftDown(), Screen.hasControlDown(), true);
            writeClientAction(1, clickData::writeToBuf);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (showTip) {
                WheelData wheelData = new WheelData(Mth.clamp(wheelDelta, -1, 1),
                        Screen.hasShiftDown(), Screen.hasControlDown(), true);
                writeClientAction(3, wheelData::writeToBuf);
            }
            return true;
        }
        return false;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        Position pos = getPosition();
        Size size = getSize();
        if (backgroundTexture != null) {
            backgroundTexture.draw(pos.x, pos.y, size.width, size.height);
        }
        if (lastFluidStack != null) {
            RenderSystem.disableBlend();
            RenderUtil.drawFluidForGui(lastFluidStack, lastFluidStack.getAmount(), pos.x + 1, pos.y + 1, size.width - 1, size.height - 1);
            if (showTip) {
                poseStack.pushPose();
                poseStack.scale(0.5f, 0.5f, 1);
                String s = TextFormattingUtil.formatLongToCompactString(lastFluidStack.getAmount(), 4) + "L";
                Font fontRenderer = Minecraft.getInstance().font;
                fontRenderer.drawShadow(poseStack, s, (pos.x + (size.width / 3f)) * 2 - fontRenderer.width(s) + 21, (pos.y + (size.height / 3f) + 6) * 2, 0xFFFFFF);
                poseStack.popPose();
            }
            RenderSystem.enableBlend();
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            if (lastFluidStack != null) {
                Component fluidName = lastFluidStack.getDisplayName();
                List<Component> hoverStringList = new ArrayList<>();
                hoverStringList.add(fluidName);
                if (showTip) {
                    hoverStringList.add(Component.literal(lastFluidStack.getAmount() + " L"));
                    hoverStringList.add(Component.translatable("cover.fluid_filter.config_amount"));
                }
                drawHoveringText(poseStack, ItemStack.EMPTY, hoverStringList, -1, mouseX, mouseY);
            }
        }
    }
}

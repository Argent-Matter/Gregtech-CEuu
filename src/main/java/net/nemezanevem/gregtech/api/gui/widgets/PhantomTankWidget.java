package net.nemezanevem.gregtech.api.gui.widgets;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.nemezanevem.gregtech.api.gui.IRenderContext;
import net.nemezanevem.gregtech.api.gui.ingredient.IGhostIngredientTarget;
import net.nemezanevem.gregtech.api.util.Position;
import net.nemezanevem.gregtech.api.util.Size;
import net.nemezanevem.gregtech.api.util.Util;
import net.nemezanevem.gregtech.client.util.RenderUtil;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static net.nemezanevem.gregtech.api.capability.GregtechDataCodes.*;

/**
 * Class Designed for the Quantum Tank. Could be used elsewhere, but is very specialized.
 */
public class PhantomTankWidget extends TankWidget implements IGhostIngredientTarget {

    private final FluidTank phantomTank;

    protected FluidStack lastPhantomStack;

    public PhantomTankWidget(IFluidTank fluidTank, int x, int y, int width, int height, FluidTank phantomTank) {
        super(fluidTank, x, y, width, height);
        this.phantomTank = phantomTank;
    }

    @Override
    public PhantomTankWidget setClient() {
        super.setClient();
        this.lastPhantomStack = phantomTank != null ? phantomTank.getFluid() != null ? phantomTank.getFluid().copy() : null : null;
        return this;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (lastFluidInTank != null || (!(ingredient instanceof FluidStack) && drainFrom(ingredient) == null)) {
            return Collections.emptyList();
        }

        Rect2i rectangle = toRectangleBox();
        return Lists.newArrayList(new Target<Object>() {

            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack stack;
                if (ingredient instanceof FluidStack) {
                    stack = (FluidStack) ingredient;
                } else {
                    stack = drainFrom(ingredient);
                }

                if (stack != null) {
                    CompoundTag compound = stack.writeToNBT(new CompoundTag());
                    writeClientAction(LOAD_PHANTOM_FLUID_STACK_FROM_NBT, buf -> buf.writeNbt(compound));
                }

                if (isClient) {
                    phantomTank.setFluid(stack);
                }
            }
        });
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buf) {
        super.handleClientAction(id, buf);
        if (id == VOID_PHANTOM_FLUID) {
            ItemStack stack = gui.player.getInventory().getSelected().copy();
            if (!stack.isEmpty()) {
                stack.setCount(1);
                LazyOptional<IFluidHandlerItem> fluidHandler = stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
                if (fluidHandler.isPresent()) {
                    FluidStack resultStack = fluidHandler.resolve().get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
                    phantomTank.setFluid(resultStack);
                }
            } else {
                phantomTank.setFluid(null);
            }
        } else if (id == LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            FluidStack stack;
            stack = FluidStack.loadFluidStackFromNBT(buf.readAnySizeNbt());
            phantomTank.setFluid(stack);
        }
    }

    @Override
    public Object getIngredientOverMouse(int mouseX, int mouseY) {
        if (isMouseOverElement(mouseX, mouseY)) {
            return lastFluidInTank == null ? lastPhantomStack : lastFluidInTank;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY)) {
            writeClientAction(VOID_PHANTOM_FLUID, buf -> {});
            if (isClient) {
                phantomTank.setFluid(null);
            }
            return true;
        }
        return false;
    }

    private FluidStack drainFrom(Object ingredient) {
        if (ingredient instanceof ItemStack) {
            ItemStack itemStack = (ItemStack) ingredient;
            LazyOptional<IFluidHandlerItem> fluidHandler = itemStack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null);
            if (fluidHandler.isPresent())
                return fluidHandler.resolve().get().drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
        }
        return null;
    }

    @Override
    public void drawInBackground(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        if (this.lastFluidInTank != null) {
            super.drawInBackground(poseStack, mouseX, mouseY, partialTicks, context);
            return;
        }
        Position pos = getPosition();
        Size size = getSize();
        if (lastPhantomStack != null && !gui.isJEIHandled) {
            RenderSystem.disableBlend();
            FluidStack stackToDraw = lastPhantomStack;
            if (stackToDraw.getAmount() == 0) {
                stackToDraw = Util.copyAmount(1, stackToDraw);
            }
            RenderUtil.drawFluidForGui(stackToDraw, 1,
                    pos.x + fluidRenderOffset, pos.y + fluidRenderOffset,
                    size.width - fluidRenderOffset, size.height - fluidRenderOffset);
            RenderSystem.enableBlend();
        }
    }

    @Override
    public void drawInForeground(PoseStack poseStack, int mouseX, int mouseY) {
        if (this.lastFluidInTank == null) return;
        super.drawInForeground(poseStack, mouseX, mouseY);
    }

    @Override
    public void updateScreenOnFrame() {
        super.updateScreenOnFrame();
        if (isClient) {
            FluidStack stack = phantomTank.getFluid();
            if (stack == null && lastPhantomStack != null) {
                lastPhantomStack = null;
            } else if (stack != null) {
                if (!stack.isFluidEqual(lastPhantomStack)) {
                    lastPhantomStack = stack.copy();
                } else if (stack.getAmount() != 0) {
                    lastPhantomStack.setAmount(0);
                }
            }
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        FluidStack stack = phantomTank.getFluid();
        if (stack == null && lastPhantomStack != null) {
            lastPhantomStack = null;
            writeUpdateInfo(REMOVE_PHANTOM_FLUID_TYPE, buf -> {});
        } else if (stack != null) {
            if (!stack.isFluidEqual(lastPhantomStack)) {
                lastPhantomStack = stack.copy();
                CompoundTag stackTag = stack.writeToNBT(new CompoundTag());
                writeUpdateInfo(CHANGE_PHANTOM_FLUID, buf -> buf.writeNbt(stackTag));
            } else if (stack.getAmount() != 0) {
                lastPhantomStack.setAmount(0);
                writeUpdateInfo(VOID_PHANTOM_FLUID, buf -> {});
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buf) {
        super.readUpdateInfo(id, buf);
        if (id == REMOVE_PHANTOM_FLUID_TYPE) {
            lastPhantomStack = null;
        } else if (id == CHANGE_PHANTOM_FLUID) {
            CompoundTag stackTag;
            stackTag = buf.readAnySizeNbt();
            lastPhantomStack = FluidStack.loadFluidStackFromNBT(stackTag);
        } else if (id == VOID_PHANTOM_FLUID) {
            lastPhantomStack.setAmount(0);
        }
    }

    public String getFormattedFluidAmount() {
        if (lastFluidInTank == null) {
            return "0";
        }
        return super.getFormattedFluidAmount();
    }

    public String getFluidLocalizedName() {
        if (lastFluidInTank == null) {
            return lastPhantomStack == null ? "" : lastPhantomStack.getTranslationKey();
        }
        return super.getFluidLocalizedName();
    }
}

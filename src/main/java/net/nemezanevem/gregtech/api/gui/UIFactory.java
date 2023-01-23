package net.nemezanevem.gregtech.api.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.network.NetworkDirection;
import net.nemezanevem.gregtech.GregTech;
import net.nemezanevem.gregtech.api.gui.impl.ModularUIContainer;
import net.nemezanevem.gregtech.api.gui.impl.ModularUIGui;
import net.nemezanevem.gregtech.api.registry.gui.UIFactoryRegistry;
import net.nemezanevem.gregtech.common.network.packets.PacketUIWidgetUpdate;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement and register in the {@link UIFactoryRegistry#UI_FACTORIES} to be able to create and open ModularUI's
 * createUITemplate should return equal gui both on server and client side, or sync will break!
 *
 * @param <E> UI holder type
 */
public abstract class UIFactory<E extends IUIHolder> {

    public final void openUI(E holder, ServerPlayer player) {
        if (player instanceof FakePlayer) {
            return;
        }
        ModularUI uiTemplate = createUITemplate(holder, player);

        if(uiTemplate == null) {
            // Central monitor Screen can return null if clicked when not powered, maybe other multis too
            return;
        }
        uiTemplate.initWidgets();

        player.nextContainerCounter();
        player.closeContainer();
        int currentWindowId = player.containerCounter;

        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        writeHolderToSyncData(serializedHolder, holder);

        ModularUIContainer container = new ModularUIContainer(uiTemplate);
        container.windowId = currentWindowId;
        //accumulate all initial updates of widgets in open packet
        container.accumulateWidgetUpdateData = true;
        uiTemplate.guiWidgets.values().forEach(Widget::detectAndSendChanges);
        container.accumulateWidgetUpdateData = false;
        ArrayList<PacketUIWidgetUpdate> updateData = new ArrayList<>(container.accumulatedUpdates);
        container.accumulatedUpdates.clear();

        ClientboundOpenScreenPacket packet = new PacketUIOpen(uiFactoryId, serializedHolder, currentWindowId, updateData);
        GregTech.NETWORK_HANDLER.sendTo(packet, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);

        container.addListener(player);
        player.openContainer = container;

        //and fire forge event only in the end
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    public final void initClientUI(FriendlyByteBuf serializedHolder, int windowId, List<PacketUIWidgetUpdate> initialWidgetUpdates) {
        E holder = readHolderFromSyncData(serializedHolder);
        Minecraft minecraft = Minecraft.getMinecraft();
        LocalPlayer player = minecraft.player;

        ModularUI uiTemplate = createUITemplate(holder, player);
        uiTemplate.initWidgets();
        ModularUIGui modularUIGui = new ModularUIGui(uiTemplate);
        modularUIGui.inventorySlots.windowId = windowId;
        for (PacketUIWidgetUpdate packet : initialWidgetUpdates) {
            modularUIGui.handleWidgetUpdate(packet);
        }
        minecraft.addScheduledTask(() -> {
            minecraft.displayGuiScreen(modularUIGui);
            minecraft.player.openContainer.windowId = windowId;
        });
    }

    protected abstract ModularUI createUITemplate(E holder, Player Player);

    protected abstract E readHolderFromSyncData(FriendlyByteBuf syncData);

    protected abstract void writeHolderToSyncData(FriendlyByteBuf syncData, E holder);

}
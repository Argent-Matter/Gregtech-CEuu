package net.nemezanevem.gregtech.api.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement and register on the {@link GregTechAPI.RegisterEvent<UIFactory>} event to be able to create and open ModularUI's
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

        player.getNextWindowId();
        player.closeContainer();
        int currentWindowId = player.currentWindowId;

        FriendlyByteBuf serializedHolder = new FriendlyByteBuf(Unpooled.buffer());
        writeHolderToSyncData(serializedHolder, holder);
        int uiFactoryId = GregTechAPI.UI_FACTORY_REGISTRY.getIDForObject(this);

        ModularUIContainer container = new ModularUIContainer(uiTemplate);
        container.windowId = currentWindowId;
        //accumulate all initial updates of widgets in open packet
        container.accumulateWidgetUpdateData = true;
        uiTemplate.guiWidgets.values().forEach(Widget::detectAndSendChanges);
        container.accumulateWidgetUpdateData = false;
        ArrayList<PacketUIWidgetUpdate> updateData = new ArrayList<>(container.accumulatedUpdates);
        container.accumulatedUpdates.clear();

        PacketUIOpen packet = new PacketUIOpen(uiFactoryId, serializedHolder, currentWindowId, updateData);
        GregTechAPI.networkHandler.sendTo(packet, player);

        container.addListener(player);
        player.openContainer = container;

        //and fire forge event only in the end
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, container));
    }

    public final void initClientUI(FriendlyByteBuf serializedHolder, int windowId, List<PacketUIWidgetUpdate> initialWidgetUpdates) {
        E holder = readHolderFromSyncData(serializedHolder);
        Minecraft minecraft = Minecraft.getMinecraft();
        PlayerSP Player = minecraft.player;

        ModularUI uiTemplate = createUITemplate(holder, Player);
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
package net.nemezanevem.gregtech.api.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ITeleporter;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.OptionalInt;
import java.util.UUID;

public class GregFakePlayer extends FakePlayer {

    private static final GameProfile GREGTECH = new GameProfile(UUID.fromString("518FDF18-EC2A-4322-832A-58ED1721309B"), "[GregTech]");
    private static WeakReference<FakePlayer> GREGTECH_PLAYER = null;

    public static FakePlayer get(ServerLevel world) {
        FakePlayer ret = GREGTECH_PLAYER != null ? GREGTECH_PLAYER.get() : null;
        if (ret == null) {
            ret = FakePlayerFactory.get(world, GREGTECH);
            GREGTECH_PLAYER = new WeakReference<>(ret);
        }
        return ret;
    }

    public GregFakePlayer(ServerLevel worldIn) {
        super(worldIn, GREGTECH);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider pMenu) {
        return OptionalInt.empty();
    }

    @Override
    public Entity changeDimension(ServerLevel pServer, ITeleporter teleporter) {
        return this;
    }

    @Override
    protected void playEquipSound(ItemStack stack) { }
}

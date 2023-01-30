package net.nemezanevem.gregtech.api.util;

public class CapesRegistry {

    private static final Map<UUID, List<ResourceLocation>> UNLOCKED_CAPES = new HashMap<>();
    private static final Map<UUID, ResourceLocation> WORN_CAPES = new HashMap<>();
    private static final Map<Advancement, ResourceLocation> CAPE_ADVANCEMENTS = new HashMap<>();

    public static void registerDevCapes() {
        unlockCape(UUID.fromString("2fa297a6-7803-4629-8360-7059155cf43e"), Textures.GREGTECH_CAPE_TEXTURE); // KilaBash
        unlockCape(UUID.fromString("a82fb558-64f9-4dd6-a87d-84040e84bb43"), Textures.GREGTECH_CAPE_TEXTURE); // Dan
        unlockCape(UUID.fromString("5c2933b3-5340-4356-81e7-783c53bd7845"), Textures.GREGTECH_CAPE_TEXTURE); // Tech22
        unlockCape(UUID.fromString("56bd41d0-06ef-4ed7-ab48-926ce45651f9"), Textures.GREGTECH_CAPE_TEXTURE); // Zalgo239
        unlockCape(UUID.fromString("aaf70ec1-ac70-494f-9966-ea5933712750"), Textures.GREGTECH_CAPE_TEXTURE); // Bruberu
        unlockCape(UUID.fromString("a24a9108-23d2-43fc-8db7-43f809d017db"), Textures.GREGTECH_CAPE_TEXTURE); // ALongString
        unlockCape(UUID.fromString("77e2129d-8f68-4025-9394-df946f1f3aee"), Textures.GREGTECH_CAPE_TEXTURE); // Brachy84
        save();
    }

    public static ResourceLocation getPlayerCape(UUID uuid) {
        return WORN_CAPES.get(uuid);
    }

    public static void save() {
        CompoundTag comp = new CompoundTag();
        ListTag unlockedCapesTag = new ListTag();
        for (Map.Entry<UUID, List<ResourceLocation>> entry : UNLOCKED_CAPES.entrySet()) {
            for (ResourceLocation cape : entry.getValue()) {
                String capeLocation = cape.toString();

                CompoundTag tag = new CompoundTag();

                tag.setString("Cape", capeLocation);
                tag.setUniqueId("UUID", entry.getKey());

                unlockedCapesTag.appendTag(tag);

            }
        }
        comp.put("UnlockedCapesValList", unlockedCapesTag);

        ListTag wornCapesTag = new ListTag();
        for (Map.Entry<UUID, ResourceLocation> entry : WORN_CAPES.entrySet()) {
            if (entry.getValue() == null)
                continue;
            String capeLocation = entry.getValue().toString();

            CompoundTag tag = new CompoundTag();

            tag.setString("Cape", capeLocation);
            tag.setUniqueId("UUID", entry.getKey());

            wornCapesTag.appendTag(tag);
        }
        comp.put("WornCapesValList", wornCapesTag);
        try {
            CompressedStreamTools.safeWrite(comp, new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "gregtech_cape.dat"));
        } catch (IOException exception) {
            GregTech.LOGGER.error(exception);
        }
    }

    public static void load() {
        CompoundTag comp = null;
        try {
            comp = CompressedStreamTools.read(new File(FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0).getSaveHandler().getWorldDirectory(), "gregtech_cape.dat"));
        } catch (IOException exception) {
            GregTech.LOGGER.error(exception);
        }
        clearMaps();
        if (comp == null) {
            registerDevCapes();
            return;
        }
        ListTag unlockedCapesTag = comp.getTagList("UnlockedCapesValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < unlockedCapesTag.tagCount(); i++) {
            CompoundTag tag = unlockedCapesTag.getCompoundAt(i);
            String capeLocation = tag.getString("Cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");

            List<ResourceLocation> capes = UNLOCKED_CAPES.get(uuid);
            if (capes == null) {
                capes = new ArrayList<>();
            }
            capes.add(new ResourceLocation(capeLocation));
            UNLOCKED_CAPES.put(uuid, capes);
        }

        ListTag wornCapesTag = comp.getTagList("WornCapesValList", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < wornCapesTag.tagCount(); i++) {
            CompoundTag tag = wornCapesTag.getCompoundAt(i);
            String capeLocation = tag.getString("Cape");
            if (capeLocation.isEmpty())
                continue;
            UUID uuid = tag.getUniqueId("UUID");
            WORN_CAPES.put(uuid, new ResourceLocation(capeLocation));
        }
        registerDevCapes();
    }

    public static void checkAdvancements(Level world) {
        registerCape(new ResourceLocation(GregTech.MODID, "ultimate_voltage/74_wetware_mainframe"), Textures.GREGTECH_CAPE_TEXTURE, world);
        registerCape(new ResourceLocation(GregTech.MODID, "steam/12_electronic_circuit"), Textures.RED_CAPE_TEXTURE, world);
        registerCape(new ResourceLocation(GregTech.MODID, "high_voltage/82_large_chemical_reactor"), Textures.YELLOW_CAPE_TEXTURE, world);
        registerCape(new ResourceLocation(GregTech.MODID, "ludicrous_voltage/60_fusion"), Textures.GREEN_CAPE_TEXTURE, world);
        for (Tuple<ResourceLocation, ResourceLocation> tuple : ctRegisterCapes) {
            registerCape(tuple.getFirst(), tuple.getSecond(), world);
        }
    }

    /**
     * Allows one to check what capes a specific player has unlocked through CapesRegistry.
     * @param uuid The player data used to get what capes the player has through internal maps.
     * @return A list of ResourceLocations containing the cape textures that the player has unlocked.
     */
    public static List<ResourceLocation> getUnlockedCapes(UUID uuid) {
        return UNLOCKED_CAPES.getOrDefault(uuid, Collections.emptyList());
    }


    /**
     * Links an advancement with a cape, which allows a player to unlock it when they receive the advancement.
     * This should only be called on world load, since advancements are only accessible then.
     * @param advancement A ResourceLocation pointing to the advancement that is to be used for getting a cape.
     * @param cape        The ResourceLocation that points to the cape that can be unlocked through the advancement.
     * @param world       The world that may contain the advancement used for getting a cape.
     */
    public static void registerCape(ResourceLocation advancement, ResourceLocation cape, Level world) {
        if (!world.isClientSide) {
            AdvancementManager advManager = ObfuscationReflectionHelper.getPrivateValue(World.class, world, "field_191951_C");
            Advancement advObject = advManager.getAdvancement(advancement);
            if (advObject != null) {
                CAPE_ADVANCEMENTS.put(advObject, cape);
            }
        }
    }

    private static List<Tuple<ResourceLocation, ResourceLocation>> ctRegisterCapes = new ArrayList<>();

    @Optional.Method(modid = GregTech.MODID_CT)
    @ZenMethod
    public static void registerCape(String advancement, String cape) {
        ctRegisterCapes.add(new Tuple<>(new ResourceLocation(advancement), new ResourceLocation(cape)));
    }

    /**
     * Automatically gives a cape to a player, which may be used for a reward for something other than an advancement
     * DOES NOT SAVE AUTOMATICALLY; PLEASE CALL SAVE AFTER THIS FUNCTION IS USED IF THIS DATA IS MEANT TO PERSIST.
     * @param uuid The UUID of the player to be given the cape.
     * @param cape The ResourceLocation that holds the cape used here.
     */
    public static void unlockCape(UUID uuid, ResourceLocation cape) {
        List<ResourceLocation> capes = UNLOCKED_CAPES.get(uuid);
        if (capes == null) {
            capes = new ArrayList<>();
        } else if (capes.contains(cape))
            return;
        capes.add(cape);
        UNLOCKED_CAPES.put(uuid, capes);
    }

    public static void unlockCapeOnAdvancement(Player player, Advancement advancement) {
        if (CAPE_ADVANCEMENTS.containsKey(advancement)) {
            unlockCape(player.getPersistentID(), CAPE_ADVANCEMENTS.get(advancement));
            player.sendSystemMessage(Component.translatable("gregtech.chat.cape"));
            save();
        }
    }

    public static void clearMaps() {
        UNLOCKED_CAPES.clear();
        WORN_CAPES.clear();
    }

    @SideOnly(Side.CLIENT)
    public static void giveRawCape(UUID uuid, ResourceLocation cape) {
        WORN_CAPES.put(uuid, cape);
    }

    public static void giveCape(UUID uuid, ResourceLocation cape) {
        WORN_CAPES.put(uuid, cape);
        GregTechAPI.networkHandler.sendToAll(new PacketNotifyCapeChange(uuid, cape));
        save();
    }

    // For loading capes when the player logs in, so that it's synced to the clients.
    public static void loadWornCapeOnLogin(Player player) {
        if (player instanceof PlayerMP) {
            UUID uuid = player.getPersistentID();
            GregTechAPI.networkHandler.sendToAll(new PacketNotifyCapeChange(uuid, WORN_CAPES.get(uuid))); // sync to others
            for (PlayerMP otherPlayer : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) { // sync to login
                uuid = otherPlayer.getPersistentID();
                GregTechAPI.networkHandler.sendTo(new PacketNotifyCapeChange(uuid, WORN_CAPES.get(uuid)), (PlayerMP) player);
            }
        }
    }

    // Runs on login, and looks for any advancements that give the player a cape that the player doesn't already have.
    public static void detectNewCapes(Player player) {
        if (player instanceof PlayerMP) {
            for (Map.Entry<Advancement, ResourceLocation> capeEntry : CAPE_ADVANCEMENTS.entrySet()) {
                if ((UNLOCKED_CAPES.get(player.getPersistentID()) == null || !UNLOCKED_CAPES.get(player.getPersistentID()).contains(capeEntry.getValue())) &&
                        ((PlayerMP) player).getAdvancements().getProgress(capeEntry.getKey()).isDone()) {
                    unlockCapeOnAdvancement(player, capeEntry.getKey());
                }
            }
        }
    }

}
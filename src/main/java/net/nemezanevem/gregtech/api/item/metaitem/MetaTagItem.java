package net.nemezanevem.gregtech.api.item.metaitem;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.nemezanevem.gregtech.api.unification.material.TagUnifier;
import net.nemezanevem.gregtech.api.unification.material.properties.info.GtMaterialIconTypes;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconSet;
import net.nemezanevem.gregtech.api.unification.material.properties.info.MaterialIconType;
import net.nemezanevem.gregtech.api.unification.tag.TagPrefix;
import net.nemezanevem.gregtech.api.util.SmallDigits;
import net.nemezanevem.gregtech.api.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetaTagItem extends StandardMetaItem {

    public final Map<String, String> OREDICT_TO_FORMULA = new HashMap<>();
    private final Map<ResourceLocation, OreDictValueItem> ITEMS = new HashMap<>();
    private static final List<MaterialIconType> DISALLOWED_TYPES = ImmutableList.of(
            GtMaterialIconTypes.block.get(), GtMaterialIconTypes.ore.get(), GtMaterialIconTypes.oreSmall.get(),
            GtMaterialIconTypes.frame.get());
    private static final ModelResourceLocation MISSING_LOCATION = new ModelResourceLocation("builtin/missing", "inventory");

    public MetaTagItem() {
        super(new ExtendedProperties(""));
    }

    @Override
    public void registerSubItems() {
        for (OreDictValueItem item : ITEMS.values()) {
            addItem(item.id, item.getName());
            TagUnifier.registerTag(this, item.getOre());
        }
    }

    @Override
    protected int getColorForItemStack(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) {
            OreDictValueItem item = ITEMS.get(Util.getId(stack.getItem()));
            return item == null ? 0xFFFFFF : item.materialRGB;
        }
        return super.getColorForItemStack(stack, tintIndex);
    }

    @Override
    public void registerModels() {
        Object2ObjectOpenHashMap<String, ModelResourceLocation> alreadyRegistered = new Object2ObjectOpenHashMap<>();
        for (Map.Entry<ResourceLocation, OreDictValueItem> metaItem : ITEMS.entrySet()) {
            TagPrefix prefix = metaItem.getValue().tagPrefix;
            MaterialIconSet materialIconSet = metaItem.getValue().materialIconSet;
            if (prefix.materialIconType == null || DISALLOWED_TYPES.contains(prefix.materialIconType))
                continue;
            String registrationKey = materialIconSet.id.toString() + "_" + prefix.name;
            if (!alreadyRegistered.containsKey(registrationKey)) {
                prefix.materialIconType.getItemModelPath(materialIconSet);
                ResourceLocation resourceLocation = prefix.materialIconType.getItemModelPath(materialIconSet);
                alreadyRegistered.put(registrationKey, new ModelResourceLocation(resourceLocation, "inventory"));
            }
            ModelResourceLocation resourceLocation = alreadyRegistered.get(registrationKey);
            metaItemsModels.put(metaItem.getKey(), resourceLocation);
        }
    }

    @SuppressWarnings("unused")
    public OreDictValueItem addOreDictItem(ResourceLocation id, String materialName, int rgb, MaterialIconSet materialIconSet, TagPrefix orePrefix) {
        return this.addOreDictItem(id, materialName, rgb, materialIconSet, orePrefix, null);
    }

    public OreDictValueItem addOreDictItem(ResourceLocation id, String materialName, int materialRGB, MaterialIconSet materialIconSet, TagPrefix orePrefix, String chemicalFormula) {
        return new OreDictValueItem(id, materialName, materialRGB, materialIconSet, orePrefix, chemicalFormula);
    }

    public class OreDictValueItem {

        private final String materialName;
        private final int materialRGB;
        private final MaterialIconSet materialIconSet;
        private final ResourceLocation id;
        private final TagPrefix tagPrefix;

        protected String chemicalFormula;

        private OreDictValueItem(ResourceLocation id, String materialName, int materialRGB, MaterialIconSet materialIconSet, TagPrefix orePrefix, String chemicalFormula) {
            this.id = id;
            this.materialName = materialName;
            this.materialRGB = materialRGB;
            this.materialIconSet = materialIconSet;
            this.tagPrefix = orePrefix;
            this.chemicalFormula = chemicalFormula;
            MetaTagItem.this.ITEMS.put(this.id, this);
            MetaTagItem.this.OREDICT_TO_FORMULA.put(this.getOre(), calculateChemicalFormula(chemicalFormula));
        }

        public String getOre() {
            return tagPrefix.name() + CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, materialName);
        }

        public ItemStack getItemStack(int amount) {
            return new ItemStack(TagUnifier.get(getOre()), amount);
        }

        public ItemStack getItemStack() {
            return getItemStack(1);
        }

        public String getName() {
            return materialName + '_' + Util.toLowerCaseUnderscore(tagPrefix.name());
        }

        protected String calculateChemicalFormula(String unformattedFormula) {
            StringBuilder sb = new StringBuilder();
            if (unformattedFormula != null && !unformattedFormula.isEmpty()) {
                for (char c : unformattedFormula.toCharArray()) {
                    if (Character.isDigit(c))
                        sb.append(SmallDigits.toSmallDownNumbers(Character.toString(c)));
                    else
                        sb.append(c);
                }
            }
            return sb.toString(); // returns "" if no formula, like other method
        }

        public String getFormula() {
            return chemicalFormula;
        }

        public int getMaterialRGB() {
            return materialRGB;
        }
    }

}

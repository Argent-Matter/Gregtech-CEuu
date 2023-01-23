package net.nemezanevem.gregtech.api.unification.material.properties.properties;

import net.nemezanevem.gregtech.api.unification.material.properties.GtMaterialProperties;
import net.nemezanevem.gregtech.api.unification.material.properties.IMaterialProperty;
import net.nemezanevem.gregtech.api.unification.material.properties.MaterialProperties;

import java.util.Objects;

public class ItemPipeProperty implements IMaterialProperty<ItemPipeProperty> {

    /**
     * Items will try to take the path with the lowest priority
     */
    private int priority;

    /**
     * rate in stacks per sec
     */
    private float transferRate;

    public ItemPipeProperty(int priority, float transferRate) {
        this.priority = priority;
        this.transferRate = transferRate;
    }

    /**
     * Default property constructor.
     */
    public ItemPipeProperty() {
        this(1, 0.25f);
    }

    /**
     * Retrieves the priority of the item pipe
     *
     * @return The item pipe priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the Priority of the item pipe
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Retrieve the transfer rate of the item pipe
     *
     * @return The transfer rate of the item pipe
     */
    public float getTransferRate() {
        return transferRate;
    }

    /**
     * Sets the transfer rate of the item pipe
     *
     * @param transferRate The transfer rate
     */
    public void setTransferRate(float transferRate) {
        this.transferRate = transferRate;
    }

    @Override
    public void verifyProperty(MaterialProperties properties) {
        properties.ensureSet(GtMaterialProperties.INGOT.get(), true);

        if (properties.hasProperty(GtMaterialProperties.FLUID_PIPE.get())) {
            throw new IllegalStateException(
                    "Material " + properties.getMaterial() +
                            " has both Fluid and Item Pipe Property, which is not allowed!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemPipeProperty that = (ItemPipeProperty) o;
        return priority == that.priority && Float.compare(that.transferRate, transferRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, transferRate);
    }

    @Override
    public String toString() {
        return "ItemPipeProperty{" +
                "priority=" + priority +
                ", transferRate=" + transferRate +
                '}';
    }
}
package net.nemezanevem.gregtech.api.unification.material;

public class Element {
    public final String symbol;
    public final long protons;
    public final long neutrons;

    public final boolean isIsotope;
    public final long halfLifeSeconds;
    public final String decayTo;

    /**
     * @param protons         Amount of Protons
     * @param neutrons        Amount of Neutrons (I could have made mistakes with the Neutron amount calculation, please tell me if I did something wrong)
     * @param halfLifeSeconds Amount of Half Life this Material has in Seconds. -1 for stable Materials
     * @param decayTo         String representing the Elements it decays to. Separated by an '&' Character
     * @param symbol          Symbol of the Element
     */
    public Element(long protons, long neutrons, long halfLifeSeconds, String decayTo, String symbol, boolean isIsotope) {
        this.protons = protons;
        this.neutrons = neutrons;
        this.halfLifeSeconds = halfLifeSeconds;
        this.decayTo = decayTo;
        this.symbol = symbol;
        this.isIsotope = isIsotope;
    }

    public String getSymbol() {
        return symbol;
    }

    public long getProtons() {
        return protons;
    }

    public long getNeutrons() {
        return neutrons;
    }

    public long getMass() {
        return protons + neutrons;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

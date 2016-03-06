/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

/**
 *
 * @author Paul
 */
public enum HelmType {

    NONE("None", 0, "CFLNSWTM", 0, 0, 0, null, -1),
    HELM("Helm", 100, "FLNS", 0, 1, 0, null, 237),
    HELM_OF_EVIL("Helm of Evil", 8000, "FLNS", 0, 3, 0, Spells.BADIOS, 251),
    HELM_OF_HANGOVERS("Helm of Hangovers", 50000, "FLNS", -2, -2, 5, null, 238),
    HELM_OF_HARDINESS("Helm of Hardiness", 3000, "FLNS", 0, 2, 0, null, 250);

    private final String name;
    private final int cost;
    private final String usableMask;
    private final int thmod;
    private final int ac;
    private final int magicAdjust;
    private final Spells spell;
    private final int iconId;

    private HelmType(String name, int cost, String usableMask, int thmod, int ac, int magicAdjust, Spells spell, int iconId) {
        this.name = name;
        this.cost = cost;
        this.usableMask = usableMask;
        this.thmod = thmod;
        this.ac = ac;
        this.magicAdjust = magicAdjust;
        this.spell = spell;
        this.iconId = iconId;

    }

    public boolean canUse(ClassType ct) {
        return this.usableMask.contains(ct.getAbbr());
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public int getThmod() {
        return thmod;
    }

    public int getAc() {
        return ac;
    }

    public Spells getSpell() {
        return spell;
    }

    public int getIconId() {
        return iconId;
    }

}

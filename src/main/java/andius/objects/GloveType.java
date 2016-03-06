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
public enum GloveType {

    NONE("None", 0, "CFLNSWTM", 0, 0, 0, -1),
    GLOVES_OF_COPPER("Gloves of Copper", 6000, "FLNS", 0, 1, 0, 234),
    GLOVES_OF_SILVER("Gloves of Silver", 60000, "FLNS", 0, 3, 6, 233);

    private final String name;
    private final int cost;
    private final String usableMask;
    private final int thmod;
    private final int ac;
    private final int magicAdjust;
    private final int iconId;

    private GloveType(String name, int cost, String usableMask, int thmod, int ac, int magicAdjust, int iconId) {
        this.name = name;
        this.cost = cost;
        this.usableMask = usableMask;
        this.thmod = thmod;
        this.ac = ac;
        this.magicAdjust = magicAdjust;
        this.iconId = iconId;

    }

    public String getName() {
        return name;
    }

    public int getThmod() {
        return thmod;
    }

    public int getAc() {
        return ac;
    }

    public int getMagicAdjust() {
        return magicAdjust;
    }

    public boolean canUse(ClassType ct) {
        return this.usableMask.contains(ct.getAbbr());
    }

    public int getIconId() {
        return iconId;
    }

}

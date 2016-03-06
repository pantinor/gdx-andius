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
public enum ShieldType {
    NONE("None", 0, "CFLNSTWM", 0, 0, -1),
    SMALL_SHIELD("Small Shield", 20, "CFLNSTW", 2, 0, 270),
    LARGE_SHIELD("Large Shield", 40, "CFLNS", 3, 0, 271),
    IRON_SHIELD("Iron Shield", 1500, "CFLNST", 4, 0, 269),
    SCREWY_SHIELD("Screwy Shield", 1500, "CFLST", 1, 0, 270),
    SHIELD_OF_SUPPORT("Shield Of Support", 7000, "CFLNS", 5, 0, 272),
    SHIELD_OF_NOTHING("Shield Of Nothing", 8000, "CFLNST", 0, 0, 269),
    SHIELD_OF_EVIL("Shield Of Evil", 25000, "CFLNST", 5, 2, 271),
    SHIELD_OF_DEFENSE("Shield Of Defense", 250000, "CFLNS", 6, 25, 272);

    private final String name;
    private final int cost;
    private final String usableMask;
    private final int ac;
    private final int magicAdjust;
    private final int iconId;

    private ShieldType(String name, int cost, String usableMask, int ac, int magicAdjust, int iconId) {
        this.name = name;
        this.cost = cost;
        this.usableMask = usableMask;
        this.ac = ac;
        this.magicAdjust = magicAdjust;
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

    public int getAc() {
        return ac;
    }

    public int getMagicAdjust() {
        return magicAdjust;
    }

    public int getIconId() {
        return iconId;
    }

}

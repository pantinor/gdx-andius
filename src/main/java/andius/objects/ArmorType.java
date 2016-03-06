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
public enum ArmorType {
    NONE("None", 0, 0, "FMTCBSLN", 0, 0, -1),
    ROBES("Robes", 15, 0, "CFLNSTWM", 1, 0, 192),
    LEATHER_ARMOR("Leather Armor", 50, 0, "CFLNSTW", 2, 0, 196),
    CHAIN_MAIL("Chain Mail", 90, 0, "CFLNS", 3, 0, 198),
    BREAST_PLATE("Breast Plate", 200, 0, "FLNS", 4, 0, 198),
    PLATE_MAIL("Plate Mail", 750, 0, "FLNS", 5, 0, 194),
    BODY_ARMOR("Body Armor", 1500, 0, "FLNS", 5, 0, 193),
    BROKEN_BREAST_PLATE("Broken Breast Plate", 1500, 0, "CFLNS", 3, 0, 194),
    CORRODED_CHAIN("Corroded Chain", 1500, 0, "CFLNS", 2, 0, 198),
    PADDED_LEATHER("Padded Leather", 1500, 0, "CFLNSTW", 3, 0, 196),
    ROTTEN_LEATHER("Rotten Leather", 1500, 0, "CFLNSTW", 1, 0, 196),
    SHINY_CHAIN("Shiny Chain", 1500, 0, "CFLNS", 4, 0, 198),
    FIRST_CLASS_PLATE("1st Class Plate", 6000, 0, "CFLNS", 7, 0, 194),
    ELVEN_CHAIN("Elven Chain", 6000, 0, "CFLNS", 5, 0, 198),
    STURDY_PLATE("Sturdy Plate", 6000, 0, "FLNS", 6, 0, 194),
    TREATED_LEATHER("Treated Leather", 6000, 0, "CFLNSTW", 4, 0, 196),
    BREAST_PLATE_OF_FIENDS("Breast Plate Of Fiends", 8000, 0, "FLNS", 2, 0, 198),
    CHAIN_OF_CURSES("Chain Of Curses", 8000, 0, "CFLNS", 1, 0, 195),
    CHAIN_OF_EVIL("Chain Of Evil", 8000, 0, "CFLNS", 5, 0, 195),
    LEATHER_OF_LOSS("Leather Of Loss", 8000, 0, "CFLNSTW", 0, 0, 196),
    NEUTRAL_PLATE("Neutral Plate", 8000, 0, "FLNS", 7, 0, 194),
    ROBE_OF_CURSES("Robe Of Curses", 8000, -2, "CFLNSTWM", 2, 0, 216),
    BREAST_PLATE_OF_BOONS("Breast Plate Of Boons", 10000, 0, "CFLNS", 6, 1, 198),
    ARMOR_OF_HEROES("Armor Of Heroes", 100000, 0, "CFLNS", 7, 10, 267),
    ARMOR_OF_EVIL("Armor Of Evil", 150000, 0, "CFLNS", 9, 15, 195),
    ARMOR_OF_FREO("Armor Of Freo", 150000, 0, "CFLNS", 6, 15, 197),
    ARMOR_OF_LORDS("Armor Of Lords", 1000000, 0, "L", 10, 100, 193);

    private final String name;
    private final int cost;
    private final int thmod;
    private final String usableMask;
    private final int ac;
    private final int magicAdjust;
    private final int iconId;

    private ArmorType(String name, int cost, int thmod, String usableMask, int ac, int magicAdjust, int iconId) {
        this.name = name;
        this.cost = cost;
        this.thmod = thmod;
        this.usableMask = usableMask;
        this.ac = ac;
        this.magicAdjust = magicAdjust;
        this.iconId = iconId;

    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public static ArmorType get(int v) {
        for (ArmorType x : values()) {
            if (x.ordinal() == (v & 0xff)) {
                return x;
            }
        }
        return null;
    }

    public int getAC() {
        return this.ac;
    }

    public boolean canUse(ClassType ct) {
        return this.usableMask.contains(ct.getAbbr());
    }

    public int getIconId() {
        return iconId;
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

}

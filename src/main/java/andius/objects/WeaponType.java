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
public enum WeaponType {
    NONE("None", 0, 0, 0, 0, 0, 0, 0, 0, "", "CFLNSWTM", 165),
    DAGGER("Dagger", 5, 1, 4, 1, 0, 0, 0, 0, "", "FLMNST", 329),
    STAFF("Staff", 10, 1, 5, 0, 0, 0, 0, 0, "", "CFLNSWTM", 344),
    SHORT_SWORD("Short Sword", 15, 1, 6, 3, 0, 0, 0, 0, "", "FLNST", 288),
    LONG_SWORD("Long Sword", 25, 1, 8, 4, 0, 0, 0, 0, "", "FLNS", 289),
    ANNOINTED_MACE("Annointed Mace", 30, 2, 6, 2, 0, 0, 0, 0, "", "CFLNSW", 305),
    ANNOINTED_FLAIL("Annointed Flail", 150, 1, 7, 3, 0, 0, 0, 0, "", "CFLNS", 380),
    EPEE_OF_DISMAY("Epee Of Dismay", 1000, 1, 6, -1, 1, 0, 0, 0, "", "FLNST", 334),
    MACE_OF_MISFORTUNE("Mace Of Misfortune", 1000, 2, 3, -1, 1, 0, 0, 0, "", "CFLNSW", 304),
    SWORD_OF_SWISHES("Sword Of Swishes", 1000, 1, 8, -1, 0, 0, 0, 0, "", "FLNS", 297),
    STUDLY_STAFF("Studly Staff", 2500, 3, 6, 2, 1, 0, 0, 0, "", "CFLNSWTM", 343),
    ROD_OF_IRON("Rod Of Iron", 3000, 1, 6, 1, 0, 0, 0, 0, "", "MW", 337),
    EPEE_OF_EXCELLENCE("Epee Of Excellence", 4000, 3, 8, 5, 3, 0, 0, 0, "", "FLNST", 334),
    MACE_OF_POWER("Mace Of Power", 4000, 3, 10, 4, 2, 0, 0, 0, "", "CLNSW", 323),
    SWORD_OF_SLASHING("Sword Of Slashing", 4000, 3, 12, 6, 3, 0, 0, 0, "", "FLNS", 247),
    BENT_STAFF("Bent Staff", 8000, 1, 4, -2, 1, 0, 0, 0, "", "CFLNSWTM", 344),
    DAGGER_OF_SLICING("Dagger Of Slicing", 8000, 3, 6, 3, 2, 0, 0, 0, "", "FLMNST", 332),
    EPEE_OF_DISASTER("Epee Of Disaster", 8000, 1, 6, 1, 1, 0, 0, 0, "", "FLNST", 334),
    MORBID_MACE("Morbid Mace", 8000, 1, 8, 0, 0, 0, 0, 0, "", "CFLNSW", 367),
    MACE_OF_SNAKES("Mace Of Snakes", 10000, 1, 8, 3, 2, 0, 0, 0, "IP", "CFLNSW", 246),
    MASHER_OF_MAGES("Masher Of Mages", 10000, 2, 7, 5, 2, 0, 0, 0, "M", "FLNS", 378),
    SLAYER_OF_DRAGONS("Slayer Of Dragons", 10000, 2, 11, 1, 1, 0, 1, 0, "D", "FLNS", 298),
    SWORD_OF_SLICING("Sword Of Slicing", 10000, 2, 9, 5, 2, 0, 1, 0, "", "FLNS", 297),
    WERESLAYER("Wereslayer", 10000, 2, 11, 5, 2, 0, 1, 0, "W", "FLNS", 314),
    MACE_OF_POUNDING("Mace Of Pounding", 12500, 3, 9, 3, 2, 0, 1, 0, "", "CFLNS", 377),
    BLADE_CUSINART("Blade Cusinart", 15000, 10, 12, 6, 4, 0, 1, 0, "", "FLNS", 291),
    BLADE_OF_BITING("Blade Of Biting", 15000, 2, 7, 4, 2, 0, 1, 0, "", "FLNST", 296),
    ROD_OF_SILENCE("Rod Of Silence", 15000, 2, 6, 1, 1, 0, 1, 0, "", "CFLNSWTM", 340),
    ROD_OF_FLAME("Rod Of Flame", 25000, 0, 0, 0, 0, 0, 2, 0, "F", "MSW", 215),
    DAGGER_OF_SPEED("Dagger Of Speed", 30000, 1, 4, -1, 7, -3, 3, 0, "", "MN", 328),
    DAGGER_OF_THIEVES("Dagger Of Thieves", 50000, 1, 6, 5, 4, 0, 5, 0, "N", "NT", 333),
    SABER_OF_EVIL("Saber Of Evil", 50000, 4, 13, 7, 4, 0, 5, 0, "", "FLNS", 249),
    SHURIKEN("Shuriken", 50000, 11, 15, 7, 3, 0, 5, 0, "PLH", "N", 382),
    SOUL_SLAYER("Soul Slayer", 50000, 1, 6, 6, 4, 0, 5, 0, "", "FLNST", 347),
    MURAMASA_BLADE("Muramasa Blade", 1000000, 5, 50, 8, 3, 0, 100, 0, "S", "S", 252)
    ;

    private final String name;
    private final int cost;
    private final int dmin;
    private final int dmax;
    private final int thmod;
    private final int swings;
    private final int ac;
    private final int magicAdjust;
    private final int regenerate;
    private final String attributesMask;
    private final String usableMask;
    private final int iconId;

    private WeaponType(String name, int cost, int dmin, int dmax, int thmod, int swings, int ac, int magicAdjust,
            int regenerate, String attributesMask, String usableMask, int iconId) {
        this.name = name;
        this.cost = cost;
        this.dmin = dmin;
        this.dmax = dmax;
        this.thmod = thmod;
        this.swings = swings;
        this.ac = ac;
        this.magicAdjust = magicAdjust;
        this.regenerate = regenerate;
        this.attributesMask = attributesMask;
        this.usableMask = usableMask;
        this.iconId = iconId;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public static WeaponType get(int v) {
        for (WeaponType x : values()) {
            if (x.ordinal() == (v & 0xff)) {
                return x;
            }
        }
        return null;
    }

    public boolean canUse(ClassType ct) {
        return this.usableMask.contains(ct.getAbbr());
    }

    public int getDmin() {
        return dmin;
    }

    public int getDmax() {
        return dmax;
    }

    public int getThmod() {
        return thmod;
    }

    public int getSwings() {
        return swings;
    }

    public int getIconId() {
        return iconId;
    }

    public int getAc() {
        return ac;
    }

    public int getMagicAdjust() {
        return magicAdjust;
    }

    public int getRegenerate() {
        return regenerate;
    }

    public String getAttributesMask() {
        return attributesMask;
    }

}

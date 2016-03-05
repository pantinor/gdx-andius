/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 *
 * @author Paul
 */
public enum WeaponType {
    NONE("None", 0, "FMTCBSLN", 1, 3),
    DAGGER("Dagger", 5, "FMTSLN", 1, 4),
    STAFF("Staff", 10, "FMTCBSLN", 1, 5),
    SHORT_SWD("Short Sword", 15, "FTSLN", 1, 6),
    LONG_SWD("Long Sword", 25, "FSLN", 1, 8),
    ANOINT_MACE("Anointed Mace", 30, "FPBSLN", 2, 6),
    ANOINT_FLAIL("Anointed Flail", 150, "FPSLN", 1, 7),
    STAFF_P2("Staff +2", 2500, "FMTCBSLN", 3, 6),
    STAFF_MOG("Staff of Mogref", 3000, "MB", 1, 6),
    MACE_P2("Mace +2", 4000, "FPBSLN", 3, 10),
    SHORT_SWD_P2("Short Sword +2", 4000, "FTSLN", 3, 8),
    LONG_SWD_P2("Long Sword +2", 4000, "FSLN", 3, 12),
    DAGGER_P2("Dagger +2", 8000, "FMTSLN", 3, 6),
    SHORT_SWD_M2("Short Sword -2", 8000, "FTSLN", 1, 6),
    LONG_SWD_P1("Long Sword +1", 10000, "FSLN", 2, 9),
    DRAGON_SLAYER("Dragon Slayer", 10000, "FSLN", 2, 11),
    WERE_SLAYER("Were Slayer", 10000, "FSLN", 2, 11),
    MAGE_MASHER("Mage Masher", 10000, "FTSLN", 2, 7),
    MACE_PRO_POISON("Mace Pro Poison", 10000, "FPBSLN", 1, 8),
    MACE_P1("Mace +1", 12500, "FPBSLN", 3, 9),
    SHORT_SWD_P1("Short Sword +1", 15000, "FTSLN", 2, 7),
    STAFF_MONTINO("Staff of Montino", 15000, "FMTCBSLN", 2, 6),
    BLADE_CUSINART("Blade Cusinart", 15000, "FSLN", 10, 12),
    DAGGER_SPEED("Dagger of Speed", 30000, "MN", 1, 4),
    EVIL_SWD_P3("Evil Sword +3", 50000, "FSLN", 4, 13),
    THIEVES_DAGGER("Thieves Dagger", 50000, "TN", 1, 6),
    SHURIKEN("Shuriken", 50000, "N", 11, 16),
    MURASAMA_BLADE("Murasama Blade", 1000000, "S", 10, 50);

    private final String name;
    private final int cost;
    private final String usableMask;
    private final int dmin;
    private final int dmax;
    private TextureRegion icon;

    private WeaponType(String name, int cost, String usableMask, int dmin, int dmax) {
        this.name = name;
        this.cost = cost;
        this.usableMask = usableMask;
        this.dmin = dmin;
        this.dmax = dmax;
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

    public TextureRegion getIcon() {
        return icon;
    }

    public void setIcon(TextureRegion icon) {
        this.icon = icon;
    }
}

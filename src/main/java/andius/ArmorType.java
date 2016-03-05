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
public enum ArmorType {
    NONE("None", 0, "FMTCBSLN", 0),
    ROBES("Robes", 15, "FMTCBSLN", 1),
    LEATHER("Leather Armor", 50, "FPTBSLN", 2),
    CHAIN_MAIL("Chain Mail", 90, "FPSLN", 3),
    BREAST_PLATE("Breast Plate", 200, "FPSLN", 4),
    PLATE("Plate Mail", 750, "FSLN", 5),
    CHAIN_P1("Chain Mail +1", 1500, "FPSLN", 4),
    LEATHER_P1("Leather +1", 1500, "FPTBSLN", 3),
    PLATE_P1("Plate Mail +1", 1500, "FSLN", 6),
    BREAST_PLATE_P1("Breast Plate +1", 1500, "FPSLN", 5),
    LEATHER_P2("Leather +2", 6000, "FPTBSLN", 4),
    CHAIN_P2("Chain +2", 6000, "FPSLN", 5),
    PLATE_P2("Plate Mail +2", 6000, "FPSLN", 7),
    EVIL_CHAIN_P2("Evil Chain +2", 8000, "FPSLN", 5),
    BR_PLATE_P2("Breast Plate +2", 10000, "FPSLN", 6),
    BR_PLATE_P3("Breast Plate +3", 100000, "FPSLN", 7),
    CHAIN_FIRE("Chain Pro Fire", 150000, "FPSLN", 6),
    EVIL_PLATE_P3("Evil Plate +3", 150000, "FPSLN", 9),
    LORDS_GARB("Lords Garb", 1000000, "L", 10);

    private final String name;
    private final int cost;
    private final String usableMask;
    private final int ac;
    private TextureRegion icon;

    private ArmorType(String name, int cost, String usableMask, int ac) {
        this.name = name;
        this.cost = cost;
        this.usableMask = usableMask;
        this.ac = ac;
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

    public TextureRegion getIcon() {
        return icon;
    }

    public void setIcon(TextureRegion icon) {
        this.icon = icon;
    }

}

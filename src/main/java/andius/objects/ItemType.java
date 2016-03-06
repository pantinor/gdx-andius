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
public enum ItemType {
    NONE("None", 0, null, -1, 0, 0, 0, ""),
    BLUE_RIBBON("Blue Ribbon", 0, null, 17, 0, 0, 0, ""),
    KEY_OF_BRONZE("Key Of Bronze", 0, null, 167, 0, 0, 0, ""),
    KEY_OF_GOLD("Key Of Gold", 0, null, 167, 0, 0, 0, ""),
    KEY_OF_SILVER("Key Of Silver", 0, null, 167, 0, 0, 0, ""),
    STATUE_OF_BEAR("Statue Of Bear", 0, null, 27, 0, 0, 0, ""),
    STATUE_OF_FROG("Statue Of Frog", 0, null, 31, 0, 0, 0, ""),
    POT_OF_NEUTRALIZING("Pot Of Neutralizing", 300, Spells.LATUMOFIS, 12, 0, 0, 0, ""),
    POT_OF_CURING("Pot Of Curing", 500, Spells.DIOS, 12, 0, 0, 0, ""),
    SCROLL_OF_AGONY("Scroll Of Agony", 500, Spells.BADIOS, 48, 0, 0, 0, ""),
    SCROLL_OF_FIRE("Scroll Of Fire", 500, Spells.HALITO, 48, 0, 0, 0, ""),
    SCROLL_OF_PAIN("Scroll Of Pain", 500, Spells.BADIOS, 48, 0, 0, 0, ""),
    SCROLL_OF_SLEEP("Scroll Of Sleep", 500, Spells.KATINO, 48, 0, 0, 0, ""),
    POTION_OF_GLASS("Potion Of Glass", 1500, Spells.SOPIC, 94, 0, 0, 0, ""),
    SCROLL_OF_BRIGHTNESS("Scroll Of Brightness", 2500, Spells.LOMILWA, 48, 0, 0, 0, ""),
    SCROLL_OF_DARKNESS("Scroll Of Darkness", 2500, Spells.DILTO, 48, 0, 0, 0, ""),
    POTION_OF_HEALING("Potion Of Healing", 5000, Spells.DIAL, 93, 0, 0, 0, ""),
    SCROLL_OF_AFFLICTION("Scroll Of Affliction", 8000, Spells.BADIAL, 48, 0, 0, 0, ""),
    RING_OF_JEWELS("Ring Of Jewels", 5000, null, 259, 0, 0, 0, ""),
    RING_OF_SHIELDING("Ring Of Shielding", 10000, null, 260, 0, 0, 0, ""),
    RING_OF_SUFFOCATION("Ring Of Suffocation", 20000, null, 261, 0, 2, 0, ""),
    RING_OF_MOVEMENT("Ring Of Movement", 25000, null, 262, 2, 2, 0, ""),
    RING_OF_HEALING("Ring Of Healing", 300000, null, 259, 0, 30, 1, ""),
    RING_OF_DEATH("Ring Of Death", 500000, null, 260, 0, 50, -3, ""),
    RING_OF_RIGIDITY("Ring Of Rigidity", 15000, null, 261, 0, 0, 0, ""),
    RING_OF_DISPELLING("Ring Of Dispelling", 500000, null, 262, 0, 50, 0, "U"),
    WERDNAS_AMULET("Werdnas Amulet", 49000000, Spells.MALOR, 257, 10, 9999, 5, "A");

    private final String name;
    private final int cost;
    private final Spells spell;
    private final int iconId;
    private final int ac;
    private final int magicAdjust;
    private final int regenerate;
    private final String attributesMask;

    private ItemType(String name, int cost, Spells spell, int iconId, int ac, int magicAdjust, int regenerate, String attributesMask) {
        this.name = name;
        this.cost = cost;
        this.spell = spell;
        this.iconId = iconId;
        this.ac = ac;
        this.magicAdjust = magicAdjust;
        this.regenerate = regenerate;
        this.attributesMask = attributesMask;
    }

}

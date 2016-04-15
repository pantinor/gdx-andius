/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.SpellArea;
import andius.Constants.SpellTarget;

/**
 *
 * @author Paul
 */
public enum Spells {

    HALITO("Little Fire", ClassType.WIZARD, 1, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8, 51),
    MOGREF("Body Iron", ClassType.WIZARD, 1, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 95),
    KATINO("Bad Air", ClassType.WIZARD, 1, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 86),
    DUMAPIC("Clarity", ClassType.WIZARD, 1, SpellTarget.NONE, SpellArea.CAMP, 0, 0, 33),
    DILTO("Darkness", ClassType.WIZARD, 2, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 88),
    SOPIC("Glass", ClassType.WIZARD, 2, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 85),
    MAHALITO("Big fire", ClassType.WIZARD, 3, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 4, 24, 79),
    MOLITO("Spark storm", ClassType.WIZARD, 3, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 3, 18, 46),
    MORLIS("Fear", ClassType.WIZARD, 4, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 89),
    DALTO("Blizzard Blast", ClassType.WIZARD, 4, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 1),
    LAHALITO("Flame storm", ClassType.WIZARD, 4, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 6, 36, 81),
    MAMORLIS("Terror", ClassType.WIZARD, 5, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 0, 0, 90),
    MAKANITO("Deadly Air", ClassType.WIZARD, 5, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 0, 0, 43),
    MADALTO("Frost", ClassType.WIZARD, 5, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 8, 64, 21),
    LAKANITO("Suffocation", ClassType.WIZARD, 6, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 28),
    ZILWAN("Dispel", ClassType.WIZARD, 6, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0, 14),
    MASOPIC("Big glass", ClassType.WIZARD, 6, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 5),
    HAMAN("Change", ClassType.WIZARD, 6, SpellTarget.VARIABLE, SpellArea.COMBAT, 0, 0, 72),
    MALOR("Apport", ClassType.WIZARD, 7, SpellTarget.PARTY, SpellArea.COMBAT_OR_CAMP, 0, 0, 94),
    MAHAMAN("Great Change", ClassType.WIZARD, 7, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 18),
    TILTOWAIT("(Untranslatable)", ClassType.WIZARD, 7, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 10, 100, 63),
    KALKI("Blessings", ClassType.CLERIC, 1, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 95),
    DIOS("Heal", ClassType.CLERIC, 1, SpellTarget.PERSON, SpellArea.ANY_TIME, 1, 8, 25),
    BADIOS("Harm", ClassType.CLERIC, 1, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8, 61),
    MILWA("Light", ClassType.CLERIC, 1, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 31),
    PORFIC("Shield", ClassType.CLERIC, 1, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 5),
    MATU("Blessing & Zeal", ClassType.CLERIC, 2, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 38),
    CALFO("X-ray vision", ClassType.CLERIC, 2, SpellTarget.CASTER, SpellArea.LOOTING, 0, 0, 40),
    MANIFO("Statue", ClassType.CLERIC, 2, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 91),
    MONTINO("Still air", ClassType.CLERIC, 2, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 7),
    LOMILWA("More light", ClassType.CLERIC, 3, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 31),
    DIALKO("Softness/supple", ClassType.CLERIC, 3, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 30),
    LATUMAPIC("Identification", ClassType.CLERIC, 3, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 32),
    BAMATU("Prayer", ClassType.CLERIC, 3, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 38),
    DIAL("Heal (more)", ClassType.CLERIC, 4, SpellTarget.PERSON, SpellArea.ANY_TIME, 2, 16, 41),
    BADIAL("Hurt (more)", ClassType.CLERIC, 4, SpellTarget.MONSTER, SpellArea.COMBAT, 2, 16, 44),
    LATUMOFIS("Cure poison", ClassType.CLERIC, 4, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 15),
    MAPORFIC("Shield (big)", ClassType.CLERIC, 4, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 352),
    DIALMA("Heal (greatly)", ClassType.CLERIC, 5, SpellTarget.PERSON, SpellArea.ANY_TIME, 3, 24, 27),
    BADIALMA("Hurt (greatly)", ClassType.CLERIC, 5, SpellTarget.MONSTER, SpellArea.COMBAT, 3, 24, 50),
    LITOKAN("Flame tower", ClassType.CLERIC, 5, SpellTarget.PARTY, SpellArea.COMBAT, 3, 24, 65),
    KANDI("Location", ClassType.CLERIC, 5, SpellTarget.PERSON, SpellArea.CAMP, 0, 0, 365),
    DI("Life", ClassType.CLERIC, 5, SpellTarget.PERSON, SpellArea.CAMP, 0, 0, 637),
    BADI("Death", ClassType.CLERIC, 5, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0, 35),
    LORTO("Blades", ClassType.CLERIC, 6, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 6, 36, 62),
    MADI("Healing", ClassType.CLERIC, 6, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 25),
    MABADI("Harm (incredibly)", ClassType.CLERIC, 6, SpellTarget.MONSTER, SpellArea.COMBAT, 18, 58, 54),
    LOKTOFEIT("Recall", ClassType.CLERIC, 6, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 76),
    MALIKTO("The Word of Death", ClassType.CLERIC, 7, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 12, 72, 59),
    KADORTO("Resurrection", ClassType.CLERIC, 7, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 24);

    private final String name;
    private final ClassType type;
    private final int level;
    private final SpellTarget target;
    private final SpellArea area;
    private final int damageMin;
    private final int damageMax;
    private final int icon;

    private Spells(String name, ClassType type, int level, SpellTarget target, SpellArea area, int damageMin, int damageMax, int icon) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.target = target;
        this.area = area;
        this.damageMin = damageMin;
        this.damageMax = damageMax;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }
    
    public String getDesc() {
        return "LVL " + this.level + " - " + this.toString().toUpperCase() + " - " + name;
    }

    public ClassType getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    public SpellTarget getTarget() {
        return target;
    }

    public SpellArea getArea() {
        return area;
    }

    public int getDamageMin() {
        return damageMin;
    }

    public int getDamageMax() {
        return damageMax;
    }

    public int getIcon() {
        return icon;
    }

}

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

    HALITO("Little Fire", ClassType.WIZARD, 1, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8),
    MOGREF("Body Iron", ClassType.WIZARD, 1, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0),
    KATINO("Bad Air", ClassType.WIZARD, 1, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    DUMAPIC("Clarity", ClassType.WIZARD, 1, SpellTarget.NONE, SpellArea.CAMP, 0, 0),
    DILTO("Darkness", ClassType.WIZARD, 2, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    SOPIC("Glass", ClassType.WIZARD, 2, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0),
    MAHALITO("Big fire", ClassType.WIZARD, 3, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 4, 24),
    MOLITO("Spark storm", ClassType.WIZARD, 3, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 3, 18),
    MORLIS("Fear", ClassType.WIZARD, 4, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    DALTO("Blizzard blast", ClassType.WIZARD, 4, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    LAHALITO("Flame storm", ClassType.WIZARD, 4, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 6, 36),
    MAMORLIS("Terror", ClassType.WIZARD, 5, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 0, 0),
    MAKANITO("Deadly air", ClassType.WIZARD, 5, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 0, 0),
    MADALTO("Frost", ClassType.WIZARD, 5, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 8, 64),
    LAKANITO("Suffocation", ClassType.WIZARD, 6, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    ZILWAN("Dispell", ClassType.WIZARD, 6, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0),
    MASOPIC("Big glass", ClassType.WIZARD, 6, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    HAMAN("Change", ClassType.WIZARD, 6, SpellTarget.VARIABLE, SpellArea.COMBAT, 0, 0),
    MALOR("Apport", ClassType.WIZARD, 7, SpellTarget.PARTY, SpellArea.COMBAT_OR_CAMP, 0, 0),
    MAHAMAN("Great change", ClassType.WIZARD, 7, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    TILTOWAIT("(untranslatable)", ClassType.WIZARD, 7, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 10, 100),
    KALKI("Blessings", ClassType.CLERIC, 1, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    DIOS("Heal", ClassType.CLERIC, 1, SpellTarget.PERSON, SpellArea.ANY_TIME, 1, 8),
    BADIOS("Harm", ClassType.CLERIC, 1, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8),
    MILWA("Light", ClassType.CLERIC, 1, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0),
    PORFIC("Shield", ClassType.CLERIC, 1, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0),
    MATU("Blessing & zeal", ClassType.CLERIC, 2, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    CALFO("X-ray vision", ClassType.CLERIC, 2, SpellTarget.CASTER, SpellArea.LOOTING, 0, 0),
    MANIFO("Statue", ClassType.CLERIC, 2, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    MONTINO("Still air", ClassType.CLERIC, 2, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0),
    LOMILWA("More light", ClassType.CLERIC, 3, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0),
    DIALKO("Softness/supple", ClassType.CLERIC, 3, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0),
    LATUMAPIC("Identification", ClassType.CLERIC, 3, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    BAMATU("Prayer", ClassType.CLERIC, 3, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    DIAL("Heal (more)", ClassType.CLERIC, 4, SpellTarget.PERSON, SpellArea.ANY_TIME, 2, 16),
    BADIAL("Hurt (more)", ClassType.CLERIC, 4, SpellTarget.MONSTER, SpellArea.COMBAT, 2, 16),
    LATUMOFIS("Cure poison", ClassType.CLERIC, 4, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0),
    MAPORFIC("Shield (big)", ClassType.CLERIC, 4, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0),
    DIALMA("Heal (greatly)", ClassType.CLERIC, 5, SpellTarget.PERSON, SpellArea.ANY_TIME, 3, 24),
    BADIALMA("Hurt (greatly)", ClassType.CLERIC, 5, SpellTarget.MONSTER, SpellArea.COMBAT, 3, 24),
    LITOKAN("Flame tower", ClassType.CLERIC, 5, SpellTarget.PARTY, SpellArea.COMBAT, 3, 24),
    KANDI("Location", ClassType.CLERIC, 5, SpellTarget.PERSON, SpellArea.CAMP, 0, 0),
    DI("Life", ClassType.CLERIC, 5, SpellTarget.PERSON, SpellArea.CAMP, 0, 0),
    BADI("Death", ClassType.CLERIC, 5, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0),
    LORTO("Blades", ClassType.CLERIC, 6, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 6, 36),
    MADI("Healing", ClassType.CLERIC, 6, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0),
    MABADI("Harm (incredibly)", ClassType.CLERIC, 6, SpellTarget.MONSTER, SpellArea.COMBAT, 18, 58),
    LOKTOFEIT("Recall", ClassType.CLERIC, 6, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0),
    MALIKTO("The Word of Death", ClassType.CLERIC, 7, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 12, 72),
    KADORTO("Resurrection", ClassType.CLERIC, 7, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0);

    private final String name;
    private final ClassType type;
    private final int level;
    private final SpellTarget target;
    private final SpellArea area;
    private final int damageMin;
    private final int damageMax;

    private Spells(String name, ClassType type, int level, SpellTarget target, SpellArea area, int damageMin, int damageMax) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.target = target;
        this.area = area;
        this.damageMin = damageMin;
        this.damageMax = damageMax;
    }

    public String getName() {
        return name;
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

}

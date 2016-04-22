/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.SpellArea;
import andius.Constants.SpellTarget;
import andius.Sound;
import java.util.HashMap;

/**
 *
 * @author Paul
 */
public enum Spells {

    HALITO("Little Fire", ClassType.MAGE, 1, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8, 51),
    MOGREF("Body Iron", ClassType.MAGE, 1, Sound.GAZE, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 95),
    KATINO("Bad Air", ClassType.MAGE, 1, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 86),
    DUMAPIC("Clarity", ClassType.MAGE, 1, Sound.GAZE, SpellTarget.NONE, SpellArea.CAMP, 0, 0, 33),
    DILTO("Darkness", ClassType.MAGE, 2, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 88),
    SOPIC("Glass", ClassType.MAGE, 2, Sound.GAZE, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 85),
    MAHALITO("Big fire", ClassType.MAGE, 3, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 4, 24, 79),
    MOLITO("Spark storm", ClassType.MAGE, 3, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 3, 18, 46),
    MORLIS("Fear", ClassType.MAGE, 4, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 89),
    DALTO("Blizzard Blast", ClassType.MAGE, 4, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 1),
    LAHALITO("Flame storm", ClassType.MAGE, 4, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 6, 36, 81),
    MAMORLIS("Terror", ClassType.MAGE, 5, Sound.GAZE, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 0, 0, 90),
    MAKANITO("Deadly Air", ClassType.MAGE, 5, Sound.GAZE, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 0, 0, 43),
    MADALTO("Frost", ClassType.MAGE, 5, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 8, 64, 21),
    LAKANITO("Suffocation", ClassType.MAGE, 6, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 28),
    ZILWAN("Dispel", ClassType.MAGE, 6, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0, 14),
    MASOPIC("Big glass", ClassType.MAGE, 6, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 5),
    HAMAN("Change", ClassType.MAGE, 6, Sound.GAZE, SpellTarget.VARIABLE, SpellArea.COMBAT, 0, 0, 72),
    MALOR("Apport", ClassType.MAGE, 7, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT_OR_CAMP, 0, 0, 94),
    MAHAMAN("Great Change", ClassType.MAGE, 7, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 18),
    TILTOWAIT("(Untranslatable)", ClassType.MAGE, 7, Sound.GAZE, SpellTarget.ALL_MONSTERS, SpellArea.COMBAT, 10, 100, 63),
    //
    KALKI("Blessings", ClassType.CLERIC, 1, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 95),
    DIOS("Heal", ClassType.CLERIC, 1, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 1, 8, 25),
    BADIOS("Harm", ClassType.CLERIC, 1, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8, 61),
    MILWA("Light", ClassType.CLERIC, 1, Sound.GAZE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 31),
    PORFIC("Shield", ClassType.CLERIC, 1, Sound.GAZE, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 5),
    MATU("Blessing & Zeal", ClassType.CLERIC, 2, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 38),
    CALFO("X-ray vision", ClassType.CLERIC, 2, Sound.GAZE, SpellTarget.CASTER, SpellArea.LOOTING, 0, 0, 40),
    MANIFO("Statue", ClassType.CLERIC, 2, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 91),
    MONTINO("Still air", ClassType.CLERIC, 2, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 0, 0, 7),
    LOMILWA("More light", ClassType.CLERIC, 3, Sound.GAZE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 31),
    DIALKO("Softness", ClassType.CLERIC, 3, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 30),
    LATUMAPIC("Identification", ClassType.CLERIC, 3, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 32),
    BAMATU("Prayer", ClassType.CLERIC, 3, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 38),
    DIAL("More Heal", ClassType.CLERIC, 4, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 2, 16, 41),
    BADIAL("More Hurt", ClassType.CLERIC, 4, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 2, 16, 44),
    LATUMOFIS("Cure poison", ClassType.CLERIC, 4, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 15),
    MAPORFIC("Big Shield", ClassType.CLERIC, 4, Sound.GAZE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 352),
    DIALMA("Greatly Heal", ClassType.CLERIC, 5, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 3, 24, 27),
    BADIALMA("Greatly Hurt", ClassType.CLERIC, 5, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 3, 24, 50),
    LITOKAN("Flame tower", ClassType.CLERIC, 5, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 3, 24, 65),
    KANDI("Location", ClassType.CLERIC, 5, Sound.GAZE, SpellTarget.PERSON, SpellArea.CAMP, 0, 0, 365),
    DI("Life", ClassType.CLERIC, 5, Sound.GAZE, SpellTarget.PERSON, SpellArea.CAMP, 0, 0, 637),
    BADI("Death", ClassType.CLERIC, 5, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0, 35),
    LORTO("Blades", ClassType.CLERIC, 6, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 6, 36, 62),
    MADI("Healing", ClassType.CLERIC, 6, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 25),
    MABADI("Incredibly Harm", ClassType.CLERIC, 6, Sound.GAZE, SpellTarget.MONSTER, SpellArea.COMBAT, 18, 58, 54),
    LOKTOFEIT("Recall", ClassType.CLERIC, 6, Sound.GAZE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 76),
    MALIKTO("The Word of Death", ClassType.CLERIC, 7, Sound.GAZE, SpellTarget.MONSTER_GROUP, SpellArea.COMBAT, 12, 72, 59),
    KADORTO("Resurrection", ClassType.CLERIC, 7, Sound.GAZE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 24);

    private final static HashMap<Spells, String> desc = new HashMap<>();

    static {
        desc.put(Spells.BADIOS, "Causes 1-8 points of damage to a monster");
        desc.put(Spells.DIOS, "Restores 1-8 points of health to a party member");
        desc.put(Spells.KALKI, "Reduces the armor class of all party members by 1 during combat");
        desc.put(Spells.MILWA, "Summons a softly glowing light that increases vision and reveals secret doors");
        desc.put(Spells.PORFIC, "Lowers the armor class of the caster by 4 during combat");
        desc.put(Spells.CALFO, "Caster can determine the trap on a chest is 95% of the time");
        desc.put(Spells.MANIFO, "Causes some of the monsters to become temporarily paralyzed");
        desc.put(Spells.MATU, "Lowers armor class of all party members by 2 during combat");
        desc.put(Spells.MONTINO, "Silences the air around a group of monsters, making it impossible for them to cast spells");
        desc.put(Spells.BAMATU, "Lowers the party's armor class by 4 in combat");
        desc.put(Spells.DIALKO, "Cures paralysis, and cures the effects of MANIFO and KATINO");
        desc.put(Spells.LATUMAPIC, "Tells you exactly what the monsters really are");
        desc.put(Spells.LOMILWA, "A more powerful MILWA spell that lasts the entire expedition, but gets terminated in darkness areas");
        desc.put(Spells.BADIAL, "Causes 2 to 16 points of damage");
        desc.put(Spells.DIAL, "Restores 2 to 16 points of health");
        desc.put(Spells.LATUMOFIS, "Removes the effects of poison");
        desc.put(Spells.MAPORFIC, "Lowers the party's armor class by 2, and lasts for the entire expedition");
        desc.put(Spells.BADI, "Attempt to kill one monster");
        desc.put(Spells.BADIALMA, "Causes 3 to 24 points of damage");
        desc.put(Spells.DI, "Causes a dead person to be resurrected. If it works, character has only 1 hit point and decreased vitality. If it fails, dead character is turned to ashes.");
        desc.put(Spells.DIALMA, "Restores 3 to 24 points of health");
        desc.put(Spells.KANDI, "Gives the direction of the person the party is attempting to locate, relative to the position of the caster");
        desc.put(Spells.LITOKAN, "Causes a pillar of flame to strike a group of monsters, doing 3 to 24 points of damage");
        desc.put(Spells.LOKTOFEIT, "Causes all party members to be transported back to the castle, minus all of their equipment and most of their gold");
        desc.put(Spells.LORTO, "Causes sharp blades to slice through a group, causing 6 to 36 points of damage");
        desc.put(Spells.MABADI, "Causes all but 1 to 8 hit points to be removed from a target");
        desc.put(Spells.MADI, "Causes all hit points to be restored and cures any condition except death");
        desc.put(Spells.KADORTO, "Restores the dead to life, and restores all hit points, even if the character is ashes. However, if the spell fails, the character is permanently lost.");
        desc.put(Spells.MALIKTO, "Causes 12 to 72 hit points of damage to all monsters");
        desc.put(Spells.DUMAPIC, "Informs you of the party's exact position from the stairs to the castle");
        desc.put(Spells.HALITO, "Causes a fireball to hit a monster for 1-8 points of fire damage");
        desc.put(Spells.KATINO, "Causes most of the monsters in a group to fall asleep");
        desc.put(Spells.MOGREF, "Reduces the casters armor class by 2 for the encounter");
        desc.put(Spells.DILTO, "Causes one group of monsters to be enveloped in darkness, lowering their defense");
        desc.put(Spells.SOPIC, "Causes the caster to become transparent, thus reducing their armor class by 4");
        desc.put(Spells.MAHALITO, "Causes an explosion in a monster group, doing 4-24 points of fire damage");
        desc.put(Spells.MOLITO, "Causes sparks to damage half of the monsters in a group for 3-18 points damage");
        desc.put(Spells.DALTO, "Does 6-36 points of cold damage");
        desc.put(Spells.LAHALITO, "Does 6-36 points of fire damage");
        desc.put(Spells.MORLIS, "Causes a group of monsters to fear the party, twice as powerful as Dilto");
        desc.put(Spells.MADALTO, "Causes 8-64 points of ice damage");
        desc.put(Spells.MAKANITO, "Kills any monsters of less than 8th level (about 35-40 hit points)");
        desc.put(Spells.MAMORLIS, "Causes all monsters to fear the party");
        desc.put(Spells.HAMAN, "Has random effects, and drains the caster one level (See below)");
        desc.put(Spells.LAKANITO, "Kills all monsters affected by this spell, some monsters are immune");
        desc.put(Spells.MASOPIC, "Reduces the armor class of the entire party by 4");
        desc.put(Spells.ZILWAN, "Will destroy any one undead monster");
        desc.put(Spells.MAHAMAN, "Does something random, stronger than Haman. Drains the caster one experience level, and is forgotten when cast.");
        desc.put(Spells.MALOR, "Teleports the party randomly within the current level when used in melee, but when cast in camp, you can decide exactly where you want to go. If a party teleports into stone it is LOST forever, so the spell is best used in conjunction with DUMAPIC.");
        desc.put(Spells.TILTOWAIT, "Does 10-100 hit points of damage to all monsters.");
    }

    private final String name;
    private final ClassType type;
    private final int level;
    private final SpellTarget target;
    private final SpellArea area;
    private final int hitCount;
    private final int hitRange;
    private final int icon;
    private final Sound snd;

    private Spells(String name, ClassType type, int level, Sound snd, SpellTarget target, SpellArea area, int hitCount, int hitRange, int icon) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.target = target;
        this.area = area;
        this.hitCount = hitCount;
        this.hitRange = hitRange;
        this.icon = icon;
        this.snd = snd;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
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

    public int getHitCount() {
        return hitCount;
    }

    public int getHitRange() {
        return hitRange;
    }

    public int getIcon() {
        return icon;
    }

    public Sound getSound() {
        return this.snd;
    }
    
    public String getDescription() {
        return desc.get(this);
    }

}

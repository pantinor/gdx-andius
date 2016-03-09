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

    public static final String[] descriptions
            = {
                "KALKI reduces the AC of all party members by one, and thus makes"
                + " them harder to hit.",
                "DIOS restores from one to eight hit points of damage from a party"
                + "member. It will not bring dead back to life.",
                "BADIOS causes one to eight hit points of damage to a monster, and"
                + " may kill it. It is the reverse of dios. Note the BA prefix which"
                + " means 'not'.",
                "MILWA causes a softly glowing light to follow the party, allowing"
                + " them to see further into the maze, and also revealing all secret"
                + " doors. See also LOMILWA. This spell lasts only a short while.",
                "PORFIC lowers the AC of the caster considerably. The effects last"
                + " for the duration of combat.",
                "MATU has the same effects as KALKI, but at double the strength.",
                "CALFO allows the caster to determine the exact nature of a trap"
                + " on a chest 95% of the time.",
                "MANIFO causes some of the monsters in a group to become stiff as"
                + " statues for one or more melee rounds. The chance of success,"
                + " and the duration of the effects, depend on the power of the"
                + " target monsters.",
                "MONTINO causes the air around a group of monsters to stop"
                + " transmitting sound. Like MANIFO, only some of the monsters will"
                + " be affected, and for varying lengths of time. Monsters and"
                + " Party members under the influence of this spell cannot cast"
                + " spells, as they cannot utter the spell words!",
                "LOMILWA is a MILWA spell with a much longer life span. Note that"
                + " when this spell, or MILWA are active, the Q option while"
                + " moving through the maze is active. If Q)UICK PLOTTING is on,"
                + " only the square you are in, and the next two squares, will"
                + " plot. Normally you might see five or six squares ahead with"
                + " LOMILWA on. Quick Plotting lets you move fast through known"
                + " areas. Note that it will be turned off when you enter camp or"
                + " combat mode.",
                "DIALKO cures paralysis, and removes the effects of MANIFO and"
                + " KATINO from one member of the party.",
                "LATUMAPIC makes it readily apparent exactly what the opposing" + " monsters really are.",
                "BAMATU has the effects of MATU at twice the effectiveness.",
                "DIAL restores two to 16 hit points of damage, and is similar to" + " DIOS.",
                "BADIAL causes two to 16 hit points of damage in the same way as" + " BADIOS.",
                "LATUMOFIS makes a poisoned person whole and fit again. Note that"
                + " poison causes a person to lose hit points steadily during"
                + " movement and combat.",
                "MAPORFIC is an improved PORFIC, with effects that last for the" + " entire expedition.",
                "DIALMA restores three to 24 hit points.",
                "BADIALMA causes three to 24 hit points of damage.",
                "LITOKAN causes a pillar of flame to strike a group of monsters,"
                + " doing three to 24 hits of damage to each. However, as with"
                + " many spells that affect entire groups, there is a chance that"
                + " individual monsters will be able to avoid or minimise its"
                + " effects. And some monsters will be resistant to it.",
                "KANDI allows the user to locate characters in the maze. It tells on"
                + " which level, and in which rough area the dead one can be found.",
                "DI causes a dead person to be resurrected. However, the renewed"
                + " character has but one hit point. Also, this spell is not as"
                + " effective or as safe as using the Temple.",
                "BADI gives the affected monster a coronary attack. It may or may"
                + " not cause death to occur.",
                "LORTO causes sharp blades to slice through a group, causing six to"
                + " 36 points of damage.",
                "MADI causes all hit points to be restored and cures any condition" + " but death.",
                "MABADI causes all but one to eight hit points to be removed from" + " the target.",
                "LOKTOFEIT causes all party members to be teleported back to the"
                + " castle, minus all their equipment and most of their gold. There"
                + " is also a good chance this spell will not function.",
                "MALIKTO causes 12 to 72 hit points of damage to all monsters. None"
                + " can escape or minimise its effects.",
                "KADORTO restores the dead to life as does DI, but also restores all"
                + " hit points. However, it has the same drawbacks as the DI spell."
                + " KADORTO can be used to resurrect people even if they are ashes.",
                "HALITO causes a flame ball the size of a baseball to hit a monster,"
                + " doing from one to eight points of damage.",
                "MOGREF reduces the caster's AC by two. The effect lasts the entire" + " encounter.",
                "KATINO causes most of the monsters in a group to fall asleep."
                + " Katino only effects normal, animal or humanoid monsters. The"
                + " chance of the spell affecting an individual monster, and the"
                + " duration of the effect, is inversely proportional to the power"
                + " of the monster. While asleep, monsters are easier to hit and"
                + " successful strikes do double damage.",
                "DUMAPIC informs you of the party's exact displacement from the"
                + " stairs to the castle, vertically, and North and East, and also"
                + " tells you what direction you are facing.",
                "DILTO causes one group of monsters to be enveloped in darkness,"
                + " which reduces their ability to defend against your attacks.",
                "SOPIC causes the caster to become transparent. This means that"
                + " he is harder to see, and thus his AC is reduced by four.",
                "MAHALITO causes a fiery explosion in a monster group, doing four"
                + " to 24 hit points of damage. As with other similar spells,"
                + " monsters may be able to minimise the damage done.",
                "MOLITO causes sparks to fly out and damage about half of the"
                + " monsters in a group. Three to 18 hit points of damage are done"
                + " with no chance of avoiding the sparks.",
                "MORLIS causes one group of monsters to fear the party greatly. The"
                + " effects are the same as a double strength DILTO spell.",
                "DALTO is similar to MAHALITO except that cold replaces flames."
                + " Also, six to 36 hit points of damage are done.",
                "LAHALITO is an improved MAHALITO, doing the same damage as DALTO.",
                "MAMORLIS is similar to MORLIS, except that all monster groups are" + " affected.",
                "Any monsters of less than eigth level (i.e. about 35-40 hit points)"
                + " are killed by this spell outright.",
                "An improved DALTO causing eight to 64 hit points of damage.",
                "All monsters in the group affected by this spell die. Of course,"
                + " there is a chance that some of the monsters will not be affected.",
                "This spell will destroy any one monster that is of the Undead" + " variety",
                "This spell duplicates the effects of SOPIC for the entire party.",
                "This spell is indeed terrible, and may backfire on the caster."
                + " First, to even cast it, you must be of the thirteenth level or"
                + " higher, and casting it will cost you one level of experience."
                + " The effects of HAMAN are random, and usually help the party.",
                "This spell's effects depend on the situation the party is in when it"
                + " is cast.Basically, MALOR will teleport the entire party from one"
                + " location to another. When used in melee, the teleport is random,"
                + " but when used in camp, where there is more chance for concentration"
                + ", it can be used to move the party anywhere in the maze. Be warned,"
                + " however, that if you teleport outside of the maze, or into an"
                + " area that is solid rock, you will be lost forever, so this spell"
                + " is to be used with the greatest of care. Combat use of MALOR will"
                + " never put you outside of the maze, but it may move you deeper in,"
                + " so it should be used only in panic situations.",
                "The same restrictions and qualifications apply to this spell as do"
                + " to HAMAN. However, the effects are even greater. Generally these"
                + " spells are only used when there is no other hope for survival.",
                "The effect of this spell can be described as similar to that of a"
                + " nuclear fusion explosion. Luckily the party is shielded from its"
                + " effects. Unluckily (for them) the monsters are not. This spell"
                + " will do from 10-100 hit points of damage."};

}

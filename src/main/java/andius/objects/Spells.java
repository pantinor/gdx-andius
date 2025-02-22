package andius.objects;

import andius.Constants.SpellArea;
import andius.Constants.SpellTarget;
import com.badlogic.gdx.graphics.Color;
import java.util.HashMap;

public enum Spells {

    HALITO("Little Fire", ClassType.MAGE, 1, Sound.FIREBALL, Color.RED, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8, 0, 51),
    MOGREF("Body Iron", ClassType.MAGE, 1, Sound.POWER_CHAINS, Color.BLUE, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 2, 95),
    KATINO("Bad Air", ClassType.MAGE, 1, Sound.SLEEP, Color.PURPLE, SpellTarget.GROUP, SpellArea.COMBAT, 0, 0, 0, 86),
    DUMAPIC("Clarity", ClassType.MAGE, 1, Sound.MEDITATION, Color.BLUE, SpellTarget.NONE, SpellArea.CAMP, 0, 0, 0, 33),
    DILTO("Darkness", ClassType.MAGE, 2, Sound.STEAL_ESSENCE, Color.PURPLE, SpellTarget.GROUP, SpellArea.COMBAT, 0, 0, 2, 88),
    SOPIC("Glass", ClassType.MAGE, 2, Sound.MAGIC, Color.BLUE, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 4, 85),
    MAHALITO("Big fire", ClassType.MAGE, 3, Sound.INFERNO, Color.RED, SpellTarget.GROUP, SpellArea.COMBAT, 4, 6, 0, 79),
    MOLITO("Spark storm", ClassType.MAGE, 3, Sound.FLAME_WAVE, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 3, 6, 0, 46),
    MORLIS("Fear", ClassType.MAGE, 4, Sound.SPIRITS, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 0, 0, 3, 89),
    DALTO("Blizzard Blast", ClassType.MAGE, 4, Sound.WIND, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 6, 6, 2, 1),
    LAHALITO("Flame storm", ClassType.MAGE, 4, Sound.INFERNO, Color.RED, SpellTarget.GROUP, SpellArea.COMBAT, 6, 6, 0, 81),
    MAMORLIS("Terror", ClassType.MAGE, 5, Sound.SPIRITS, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 0, 0, 4, 90),
    MAKANITO("Deadly Air", ClassType.MAGE, 5, Sound.SLEEP, Color.PURPLE, SpellTarget.GROUP, SpellArea.COMBAT, 8, 1, 0, 43),
    MADALTO("Frost", ClassType.MAGE, 5, Sound.WIND, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 8, 8, 2, 21),
    LAKANITO("Suffocation", ClassType.MAGE, 6, Sound.WEAKNESS, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 8, 8, 0, 28),
    ZILWAN("Turn Undead", ClassType.MAGE, 6, Sound.RAGE, Color.YELLOW, SpellTarget.MONSTER, SpellArea.COMBAT, 10, 10, 0, 14),
    MASOPIC("Big glass", ClassType.MAGE, 6, Sound.MAGIC, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 4, 5),
    HAMAN("Change", ClassType.MAGE, 6, Sound.MEDITATION, Color.BLUE, SpellTarget.VARIABLE, SpellArea.COMBAT, 0, 0, 0, 72),
    MALOR("Teleport", ClassType.MAGE, 7, Sound.MEDITATION, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT_OR_CAMP, 0, 0, 0, 94),
    MAHAMAN("Great Change", ClassType.MAGE, 7, Sound.MEDITATION, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 0, 18),
    TILTOWAIT("(Untranslatable)", ClassType.MAGE, 7, Sound.TREMOR, Color.RED, SpellTarget.GROUP, SpellArea.COMBAT, 10, 15, 0, 63),
    //
    KALKI("Blessings", ClassType.PRIEST, 1, Sound.HEALING, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 1, 95),
    DIOS("Heal", ClassType.PRIEST, 1, Sound.HEALING, Color.BLUE, SpellTarget.PERSON, SpellArea.ANY_TIME, 1, 8, 0, 25),
    BADIOS("Harm", ClassType.PRIEST, 1, Sound.POISON_EFFECT, Color.YELLOW, SpellTarget.MONSTER, SpellArea.COMBAT, 1, 8, 0, 61),
    MILWA("Light", ClassType.PRIEST, 1, Sound.MAGIC, Color.BLUE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 0, 31),
    PORFIC("Shield", ClassType.PRIEST, 1, Sound.POWER_CHAINS, Color.BLUE, SpellTarget.CASTER, SpellArea.COMBAT, 0, 0, 4, 5),
    MATU("Blessing & Zeal", ClassType.PRIEST, 2, Sound.HEALING, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 2, 38),
    CALFO("X-ray vision", ClassType.PRIEST, 2, Sound.DIVINE_MEDITATION, Color.BLUE, SpellTarget.CASTER, SpellArea.LOOTING, 0, 0, 0, 40),
    MANIFO("Statue", ClassType.PRIEST, 2, Sound.POWER_CHAINS, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 0, 0, 0, 91),
    MONTINO("Still air", ClassType.PRIEST, 2, Sound.SLEEP, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 0, 0, 0, 7),
    LOMILWA("More light", ClassType.PRIEST, 3, Sound.MAGIC, Color.BLUE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 0, 31),
    DIALKO("Dispel effects", ClassType.PRIEST, 3, Sound.DIVINE_INTERVENTION, Color.BLUE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 0, 30),
    LATUMAPIC("Dispel group effects", ClassType.PRIEST, 3, Sound.DIVINE_INTERVENTION, Color.BLUE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 0, 32),
    BAMATU("Prayer", ClassType.PRIEST, 3, Sound.HEALING, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 4, 38),
    DIAL("More Heal", ClassType.PRIEST, 4, Sound.HEALING, Color.BLUE, SpellTarget.PERSON, SpellArea.ANY_TIME, 2, 16, 0, 41),
    BADIAL("More Hurt", ClassType.PRIEST, 4, Sound.SPIRITS, Color.YELLOW, SpellTarget.MONSTER, SpellArea.COMBAT, 2, 8, 0, 44),
    LATUMOFIS("Cure poison", ClassType.PRIEST, 4, Sound.HEALING, Color.GREEN, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 0, 15),
    MAPORFIC("Big Shield", ClassType.PRIEST, 4, Sound.POWER_CHAINS, Color.BLUE, SpellTarget.PARTY, SpellArea.ANY_TIME, 0, 0, 2, 352),
    DIALMA("Greatly Heal", ClassType.PRIEST, 5, Sound.HEALING, Color.YELLOW, SpellTarget.PERSON, SpellArea.ANY_TIME, 3, 24, 0, 27),
    BADIALMA("Greatly Hurt", ClassType.PRIEST, 5, Sound.SPIRITS, Color.RED, SpellTarget.MONSTER, SpellArea.COMBAT, 3, 8, 0, 50),
    LITOKAN("Flame tower", ClassType.PRIEST, 5, Sound.FIREBALL, Color.RED, SpellTarget.PARTY, SpellArea.COMBAT, 3, 8, 1, 65),
    KANDI("Location", ClassType.PRIEST, 5, Sound.MEDITATION, Color.BLUE, SpellTarget.PERSON, SpellArea.CAMP, 0, 0, 0, 365),
    DI("Life", ClassType.PRIEST, 5, Sound.DIVINE_INTERVENTION, Color.GREEN, SpellTarget.PERSON, SpellArea.CAMP, 0, 0, 0, 637),
    BADI("Death", ClassType.PRIEST, 5, Sound.SPIRITS, Color.PURPLE, SpellTarget.MONSTER, SpellArea.COMBAT, 0, 0, 0, 35),
    LORTO("Blades", ClassType.PRIEST, 6, Sound.CROSSBOW, Color.GRAY, SpellTarget.GROUP, SpellArea.COMBAT, 6, 6, 0, 62),
    MADI("Healing", ClassType.PRIEST, 6, Sound.HEALING, Color.BLUE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 0, 25),
    MABADI("Incredibly Harm", ClassType.PRIEST, 6, Sound.TREMOR, Color.GRAY, SpellTarget.MONSTER, SpellArea.COMBAT, 18, 58, 0, 54),
    LOKTOFEIT("Recall", ClassType.PRIEST, 6, Sound.MEDITATION, Color.BLUE, SpellTarget.PARTY, SpellArea.COMBAT, 0, 0, 0, 76),
    MALIKTO("The Word of Death", ClassType.PRIEST, 7, Sound.RAGE, Color.BLUE, SpellTarget.GROUP, SpellArea.COMBAT, 12, 6, 0, 59),
    KADORTO("Resurrection", ClassType.PRIEST, 7, Sound.DIVINE_INTERVENTION, Color.BLUE, SpellTarget.PERSON, SpellArea.ANY_TIME, 0, 0, 0, 24);

    private final static HashMap<Spells, String> desc = new HashMap<>();

    static {
        desc.put(Spells.BADIOS, "Causes 1-8 points of damage to a monster");
        desc.put(Spells.DIOS, "Restores 1-8 points of health to a party member");
        desc.put(Spells.KALKI, "Reduces the armor class of all party members by 1 during combat");
        desc.put(Spells.MILWA, "Summons a softly glowing light that increases vision and reveals secret doors");
        desc.put(Spells.PORFIC, "Lowers the armor class of the caster by 4 during combat");
        desc.put(Spells.CALFO, "Caster can determine the trap on a chest 95% of the time");
        desc.put(Spells.MANIFO, "Causes some of the monsters to become temporarily paralyzed");
        desc.put(Spells.MATU, "Lowers armor class of all party members by 2 during combat");
        desc.put(Spells.MONTINO, "Silences the air around a group of monsters, making it impossible for them to cast spells");
        desc.put(Spells.BAMATU, "Lowers the party's armor class by 4 in combat");
        desc.put(Spells.DIALKO, "Cures paralysis and wakes the sleeping");
        desc.put(Spells.LATUMAPIC, "Dispels effects of silence, fear and paralysis.");
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
        desc.put(Spells.KADORTO, "Restores the dead to life, and restores all hit points, even if the character is ashes.");
        desc.put(Spells.MALIKTO, "Causes 12 to 72 hit points of damage to all monsters");
        desc.put(Spells.DUMAPIC, "Informs you of the party's exact position.");
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
        desc.put(Spells.HAMAN, "Has random effects, and drains the caster one level");
        desc.put(Spells.LAKANITO, "Kills all monsters affected by this spell, some monsters are immune");
        desc.put(Spells.MASOPIC, "Reduces the armor class of the entire party by 4");
        desc.put(Spells.ZILWAN, "Will destroy any one undead monster");
        desc.put(Spells.MAHAMAN, "Does something random, stronger than Haman. Drains the caster one experience level, and is forgotten when cast.");
        desc.put(Spells.MALOR, "Teleports the party to given coordinates or randomly if cast in combat.  Best used in conjunction with DUMAPIC.");
        desc.put(Spells.TILTOWAIT, "Does 10-100 hit points of damage to all monsters.");
    }

    private final String name;
    private final ClassType type;
    private final int level;
    private final SpellTarget target;
    private final SpellArea area;
    private final int icon;
    private final Sound snd;
    private final Color color;
    private final Dice damage;
    private final int hitBonus;

    private Spells(String name, ClassType type, int level, Sound snd, Color color,
            SpellTarget target, SpellArea area, int hitCount, int hitRange, int hitBonus, int icon) {
        this.name = name;
        this.type = type;
        this.level = level;
        this.target = target;
        this.area = area;
        this.damage = new Dice(hitCount, hitRange, 0);
        this.hitBonus = hitBonus;
        this.icon = icon;
        this.snd = snd;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return this.level + " " + this.toString();
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

    public int damage() {
        return this.damage.roll();
    }

    public int getHitBonus() {
        return hitBonus;
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

    public Color getColor() {
        return color;
    }
    
    public String label() {
        return String.format("%d %s %s %s", this.level, this, this.damage, this.target == SpellTarget.GROUP ? "GRP" : "");
    }

}

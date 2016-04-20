package andius.objects;

import andius.Constants.Ability;
import andius.Constants.Resistance;
import java.util.ArrayList;
import java.util.List;

public class Monster implements Comparable<Monster> {

    public String genericName;
    public String name;
    int monsterID;
    int type;
    public Icons icon;
    int goldReward;
    int chestReward;
    int partnerID;
    int partnerOdds;
    int armourClass;
    int speed;
    int mageSpellLevel;
    int priestSpellLevel;
    int levelDrain;
    int healpts;
    int breath;
    int unaffected;
    List<Resistance> resists;
    List<Ability> ability;
    int exp;
    Dice groupSize;
    Dice hitPoints;
    List<Dice> damage;

    public final static String[] monsterClass
            = {"Fighter", "Mage", "Priest", "Thief", "Midget", "Giant", "Mythical", "Dragon", "Animal",
                "Were", "Undead", "Demon", "Insect", "Enchanted"};

    public void clone(Monster m) {
        this.genericName = m.genericName;
        this.name = m.name;
        this.monsterID = m.monsterID;
        this.type = m.type;
        this.icon = m.icon;
        this.goldReward = m.goldReward;
        this.chestReward = m.chestReward;
        this.partnerID = m.partnerID;
        this.partnerOdds = m.partnerOdds;
        this.armourClass = m.armourClass;
        this.speed = m.speed;
        this.mageSpellLevel = m.mageSpellLevel;
        this.priestSpellLevel = m.priestSpellLevel;
        this.levelDrain = m.levelDrain;
        this.healpts = m.healpts;
        this.breath = m.breath;
        this.unaffected = m.unaffected;
        this.resists = m.resists;
        this.ability = m.ability;
        this.exp = m.exp;
        this.groupSize = m.groupSize;
        this.hitPoints = m.hitPoints;
        this.damage = m.damage;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getName() {
        return name;
    }

    public int getMonsterID() {
        return monsterID;
    }

    public String getType() {
        return monsterClass[this.type];
    }

    public Icons getIcon() {
        return icon;
    }

    public int getGoldReward() {
        return goldReward;
    }

    public int getChestReward() {
        return chestReward;
    }

    public int getPartnerID() {
        return partnerID;
    }

    public int getPartnerOdds() {
        return partnerOdds;
    }

    public int getArmourClass() {
        return armourClass;
    }

    public int getSpeed() {
        return speed;
    }

    public int getMageSpellLevel() {
        return mageSpellLevel;
    }

    public int getPriestSpellLevel() {
        return priestSpellLevel;
    }

    public int getLevelDrain() {
        return levelDrain;
    }

    public int getHealpts() {
        return healpts;
    }

    public int getBreath() {
        return breath;
    }

    public int getUnaffected() {
        return unaffected;
    }

    public int getExp() {
        return exp;
    }

    public Dice getGroupSize() {
        return groupSize;
    }

    public Dice getHitPoints() {
        return hitPoints;
    }

    public List<Dice> getDamage() {
        return damage;
    }


    @Override
    public int compareTo(Monster o) {
        if (this.exp != o.exp) {
            return Integer.compare(this.exp, o.exp);
        }
        return Integer.compare(this.hitPoints.getMax(), o.hitPoints.getMax());
    }

    @Override
    public String toString() {
        return String.format("%s %d %d %s %s", name, exp, armourClass, hitPoints, damage);
    }

}

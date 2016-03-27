package andius.objects;

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
    int bonus1;
    int bonus2;
    int bonus3;
    int resistance;
    int abilities;
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
        this.bonus1 = m.bonus1;
        this.bonus2 = m.bonus2;
        this.bonus3 = m.bonus3;
        this.resistance = m.resistance;
        this.abilities = m.abilities;
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

    public int getBonus1() {
        return bonus1;
    }

    public int getBonus2() {
        return bonus2;
    }

    public int getBonus3() {
        return bonus3;
    }

    public int getResistance() {
        return resistance;
    }

    public int getAbilities() {
        return abilities;
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

package andius.objects;

import java.util.List;

public class Monster {

    private String genericName;
    private String name;
    private int monsterID;
    private int type;
    private int imageID;
    private int rewardTable1;
    private int rewardTable2;
    private int partnerID;
    private int partnerOdds;
    private int armourClass;
    private int speed;
    private int mageSpellLevel;
    private int priestSpellLevel;
    private int levelDrain;
    private int bonus1;
    private int bonus2;
    private int bonus3;
    private int resistance;
    private int abilities;
    private int exp;
    private Dice groupSize;
    private Dice hitPoints;
    private List<Dice> damage;

    public static String[] monsterClass
            = {"Fighter", "Mage", "Priest", "Thief", "Midget", "Giant", "Mythical", "Dragon", "Animal",
                "Were", "Undead", "Demon", "Insect", "Enchanted"};

    public String getGenericName() {
        return genericName;
    }

    public String getName() {
        return name;
    }

    public int getMonsterID() {
        return monsterID;
    }

    public int getType() {
        return type;
    }

    public int getImageID() {
        return imageID;
    }

    public int getRewardTable1() {
        return rewardTable1;
    }

    public int getRewardTable2() {
        return rewardTable2;
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

    public static String[] getMonsterClass() {
        return monsterClass;
    }

}

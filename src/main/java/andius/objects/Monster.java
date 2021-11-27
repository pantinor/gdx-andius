package andius.objects;

import andius.Constants.Ability;
import andius.Constants.Resistance;
import java.util.List;

public class Monster implements Comparable<Monster> {

    public String genericName;
    public String name;
    public int iconId;
    int type;
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

    public enum Type {
        FIGHTER, MAGE, PRIEST, THIEF, MIDGET, GIANT, MYTHICAL, DRAGON, ANIMAL,
        WERE, UNDEAD, DEMON, INSECT, ENCHANTED;
    }

    public void clone(Monster m) {
        this.genericName = m.genericName;
        this.name = m.name;
        this.iconId = m.iconId;
        this.type = m.type;
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

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public Type getType() {
        return Type.values()[this.type];
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

    public int getLevel() {
        if (exp > 0 && this.exp <= 599) {
            return 0;
        } else if (this.exp >= 600 && this.exp <= 999) {
            return 1;
        } else if (this.exp >= 1000 && this.exp <= 1399) {
            return 2;
        } else if (this.exp >= 1400 && this.exp <= 1999) {
            return 3;
        } else if (this.exp >= 2000 && this.exp <= 2999) {
            return 4;
        } else if (this.exp >= 3000 && this.exp <= 3999) {
            return 5;
        } else if (this.exp >= 4000 && this.exp <= 4999) {
            return 6;
        } else if (this.exp >= 5000 && this.exp <= 5999) {
            return 7;
        } else if (this.exp >= 6000 && this.exp <= 7999) {
            return 8;
        } else if (this.exp >= 8000 && this.exp <= 9999) {
            return 9;
        } else {
            return 10;
        }
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

    public List<Resistance> getResists() {
        return resists;
    }

    public List<Ability> getAbility() {
        return ability;
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

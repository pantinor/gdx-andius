package andius.objects;

import andius.Constants.Ability;
import andius.Constants.Breath;
import andius.Constants.CharacterType;
import andius.Constants.Resistance;
import java.util.ArrayList;
import java.util.List;

public class Monster implements Comparable<Monster> {

    public String genericName;
    public String name;
    public int monsterId;
    public String iconId;
    public int type;
    public int goldReward;
    public int chestReward;
    public int partnerID;
    public int partnerOdds;
    public int armourClass;
    public int speed;
    public int mageSpellLevel;
    public int priestSpellLevel;
    public int levelDrain;
    public int healpts;
    public int breath;
    public int unaffected;
    public List<Resistance> resistance = new ArrayList<>();
    public List<Ability> ability = new ArrayList<>();
    public int exp;
    public Dice groupSize;
    public Dice hitPoints;
    public List<Dice> damage;

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
        this.resistance = m.resistance;
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

    public int getMonsterId() {
        return monsterId;
    }

    public String getIconId() {
        return iconId;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public CharacterType getType() {
        return CharacterType.values()[this.type];
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

    public Breath breath() {
        return Breath.values()[this.breath];
    }

    public int getUnaffected() {
        return unaffected;
    }

    public int getExp() {
        return exp;
    }

    public int getLevel() {
        return this.hitPoints.getQty();
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
        return Integer.compare(this.monsterId, o.monsterId);
    }

    @Override
    public String toString() {
        return String.format("%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%d\t%s",
                monsterId,
                String.format("%-15s", name),
                String.format("%-10s", CharacterType.values()[this.type]),
                String.format("%-4s", "" + getLevel()),
                String.format("%-4s", "" + exp),
                String.format("%-3s", "" + hitPoints.max()),
                String.format("%-3s", "" + armourClass),
                String.format("%-25s", damage),
                mageSpellLevel,
                priestSpellLevel,
                speed,
                goldReward,
                chestReward,
                levelDrain,
                healpts,
                breath,
                partnerID,
                groupSize
        );
    }

}

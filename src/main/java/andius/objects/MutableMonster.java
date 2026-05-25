package andius.objects;

import andius.Constants.Breath;
import andius.Constants.CharacterType;
import andius.Constants.Resistance;
import andius.Constants.Status;
import java.util.List;
import utils.Utils;

public class MutableMonster implements Mutable {

    private int acmodifier;
    private int currentHitPoints;
    private int currentMageSpellLevel;
    private int currentPriestSpellLevel;
    private final State status = new State();
    private final int maxHitPoints;
    private transient HealthCursor healthCursor;
    private Monster monster;

    public MutableMonster(Monster m) {
        this.monster = m;
        this.maxHitPoints = this.monster.hitPoints.roll();
        this.currentHitPoints = this.maxHitPoints;
        this.currentMageSpellLevel = this.monster.mageSpellLevel;
        this.currentPriestSpellLevel = this.monster.priestSpellLevel;
    }

    @Override
    public Object baseType() {
        return this.monster;
    }

    public Monster monster() {
        return this.monster;
    }

    @Override
    public boolean isUnaffected(Spells spell, CharacterType type) {
        if (this.monster.resistance.contains(Resistance.FIRE)) {
            if (spell.equals(Spells.LITOKAN) || spell.equals(Spells.MAHAMAN) || spell.equals(Spells.LAHALITO)) {
                return true;
            }
        }
        if (this.monster.resistance.contains(Resistance.COLD)) {
            if (spell.equals(Spells.DALTO) || spell.equals(Spells.MADALTO)) {
                return true;
            }
        }
        if (this.monster.resistance.contains(Resistance.NONE)) {
            if (spell.equals(Spells.LORTO) || spell.equals(Spells.MALIKTO) || spell.equals(Spells.MOLITO) || spell.equals(Spells.TILTOWAIT)) {
                return true;
            }
        }
        return this.monster.getUnaffected() > Utils.RANDOM.nextInt(100);
    }

    @Override
    public String name() {
        return this.monster.getName();
    }

    @Override
    public int icon() {
        return this.monster.getIconId();
    }

    @Override
    public CharacterType getMonsterType() {
        return this.monster.getType();
    }

    @Override
    public ClassType getType() {
        return null;
    }

    @Override
    public List<Dice> getDamage() {
        return this.monster.getDamage();
    }

    @Override
    public int hitModifier() {
        return getLevel();
    }

    @Override
    public int getArmourClass() {
        return this.monster.armourClass;
    }

    @Override
    public int getLevel() {
        return this.monster.getLevel();
    }

    @Override
    public Breath breath() {
        return this.monster.breath();
    }

    @Override
    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    @Override
    public void adjustHitPoints(int amt) {
        this.currentHitPoints = Utils.adjustValue(this.currentHitPoints, amt, this.getMaxHitPoints(), 0);
    }

    @Override
    public int getCurrentMageSpellLevel() {
        return currentMageSpellLevel;
    }

    @Override
    public void decrementSpellPoints(Spells spell) {
        if (spell.getType() == ClassType.MAGE) {
            this.currentMageSpellLevel--;
            if (currentMageSpellLevel < 0) {
                this.currentMageSpellLevel = 0;
            }
        } else {
            this.currentPriestSpellLevel--;
            if (currentPriestSpellLevel < 0) {
                this.currentPriestSpellLevel = 0;
            }
        }
    }

    @Override
    public int getCurrentPriestSpellLevel() {
        return currentPriestSpellLevel;
    }

    @Override
    public boolean isDead() {
        return this.currentHitPoints <= 0;
    }

    @Override
    public State status() {
        return status;
    }

    @Override
    public void processStatusAffects() {

        if (this.monster.healpts > 0) {
            int roll = Utils.RANDOM.nextInt(100);
            boolean heal = roll < Math.max(this.getLevel() * 20, 50);
            if (heal) {
                adjustHitPoints(this.monster.healpts);
                adjustHealthCursor();
            }
        }

        for (Status s : Status.values()) {
            int roll = Utils.RANDOM.nextInt(100);
            boolean decr = false;
            switch (s) {
                case AFRAID:
                    decr = roll < Math.max(this.getLevel() * 10, 50);
                    break;
                case SILENCED:
                case ASLEEP:
                    decr = roll < Math.max(this.getLevel() * 20, 50);
                    break;
                case POISONED:
                case PARALYZED:
                    decr = roll < Math.max(this.getLevel() * 7, 50);
                    break;
                case STONED:
                    break;
                case ASHES:
                    break;

            }
            if (decr) {
                this.status.decrement(s);
            }
        }
    }

    @Override
    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    @Override
    public HealthCursor getHealthCursor() {
        return healthCursor;
    }

    @Override
    public void setHealthCursor(HealthCursor healthCursor) {
        this.healthCursor = healthCursor;
    }

    @Override
    public void adjustHealthCursor() {
        if (this.healthCursor != null) {
            this.healthCursor.adjust(currentHitPoints, maxHitPoints);
        }
    }

    public double getPercentDamaged() {
        return (double) currentHitPoints / maxHitPoints;
    }

    @Override
    public void setACModifier(int acmodifier) {
        this.acmodifier = acmodifier;
    }

    @Override
    public int getACModifier() {
        return acmodifier;
    }

    @Override
    public Spells castMageSpell() {
        Spells spell = Utils.monsterMageSpell(this.currentMageSpellLevel);
        int depletionRange = Math.max(1, this.monster.groupSize.roll() + 2);
        if (Utils.RANDOM.nextInt(depletionRange) == 0) {
            this.currentMageSpellLevel--;
            if (this.currentMageSpellLevel < 0) {
                this.currentMageSpellLevel = 0;
            }
        }
        return spell;
    }

    @Override
    public Spells castPriestSpell() {
        Spells spell = Utils.monsterPriestSpell(this.currentPriestSpellLevel);
        int depletionRange = Math.max(1, this.monster.groupSize.roll() + 2);
        if (Utils.RANDOM.nextInt(depletionRange) == 0) {
            this.currentPriestSpellLevel--;
            if (this.currentPriestSpellLevel < 0) {
                this.currentPriestSpellLevel = 0;
            }
        }
        return spell;
    }

}

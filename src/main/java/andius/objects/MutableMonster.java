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
        return 0;
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
        int roll = Utils.RANDOM.nextInt(100);
        int value = 0;
        if (roll <= 70) {
            value = 0;
        } else if (roll <= 80) {
            value = 1;
        } else if (roll <= 90) {
            value = 2;
        } else if (roll <= 94) {
            value = 3;
        } else if (roll <= 96) {
            value = 4;
        } else if (roll <= 98) {
            value = 5;
        } else if (roll <= 100) {
            value = 6;
        }

        int spellLevel = Math.max(1, this.currentMageSpellLevel - value);
        int roll2 = Utils.RANDOM.nextInt(100);
        Spells spell = null;

        if (spellLevel == 1) {
            spell = roll2 < 66 ? Spells.KATINO : Spells.HALITO;
        }
        if (spellLevel == 2) {
            spell = roll2 < 66 ? Spells.DILTO : Spells.HALITO;
        }
        if (spellLevel == 3) {
            spell = roll2 < 66 ? Spells.MOLITO : Spells.MAHALITO;
        }
        if (spellLevel == 4) {
            spell = roll2 < 66 ? Spells.DALTO : Spells.LAHALITO;
        }
        if (spellLevel == 5) {
            spell = roll2 < 66 ? Spells.LAHALITO : Spells.MADALTO;
        }
        if (spellLevel == 6) {
            spell = roll2 < 66 ? Spells.MADALTO : Spells.ZILWAN;
        }
        if (spellLevel == 7) {
            spell = roll2 < 66 ? Spells.TILTOWAIT : Spells.TILTOWAIT;
        }

        double tmp = ((double) 1 / (double) (this.monster.groupSize.roll() + 2)) * 100;
        int roll3 = Utils.RANDOM.nextInt(100);
        if (roll3 <= tmp) {
            this.currentMageSpellLevel--;
        }

        return spell;
    }

    @Override
    public Spells castPriestSpell() {

        int spellLevel = Math.max(1, this.currentPriestSpellLevel);
        int roll2 = Utils.RANDOM.nextInt(100);
        Spells spell = null;

        if (spellLevel == 1) {
            spell = roll2 < 66 ? Spells.BADIOS : Spells.BADIOS;
        }
        if (spellLevel == 2) {
            spell = roll2 < 66 ? Spells.MONTINO : Spells.MONTINO;
        }
        if (spellLevel == 3) {
            spell = roll2 < 66 ? Spells.BADIOS : Spells.BADIAL;
        }
        if (spellLevel == 4) {
            spell = roll2 < 66 ? Spells.BADIAL : Spells.BADIAL;
        }
        if (spellLevel == 5) {
            spell = roll2 < 66 ? Spells.BADIALMA : Spells.BADI;
        }
        if (spellLevel == 6) {
            spell = roll2 < 66 ? Spells.LORTO : Spells.MABADI;
        }
        if (spellLevel == 7) {
            spell = roll2 < 66 ? Spells.MABADI : Spells.MABADI;
        }

        double tmp = ((double) 1 / (double) (this.monster.groupSize.roll() + 2)) * 100;
        int roll3 = Utils.RANDOM.nextInt(100);
        if (roll3 <= tmp) {
            this.currentPriestSpellLevel--;
        }

        return spell;
    }

}

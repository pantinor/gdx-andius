package andius.objects;

import andius.Constants.Breath;
import andius.Constants.CharacterType;
import andius.Constants.Status;
import static andius.WizardryData.WER_ITEMS;
import andius.objects.Item.ItemType;
import java.util.ArrayList;
import java.util.List;
import utils.Utils;

public class MutableCharacter implements Mutable {

    private int acmodifier;
    private int currentHitPoints;
    private final int[] mageSpellsLevels = new int[7];
    private final int[] priestSpellLevels = new int[7];
    private final State status = new State();
    private HealthCursor healthCursor;
    private DoGooder dogooder;

    public MutableCharacter(DoGooder c) {
        this.dogooder = c;
        this.currentHitPoints = this.dogooder.hpMax;
        for (int i = 0; i < 7; i++) {
            mageSpellsLevels[i] = this.dogooder.spellAllowance[0][i];
            priestSpellLevels[i] = this.dogooder.spellAllowance[1][i];
        }
    }

    @Override
    public Object baseType() {
        return this.dogooder;
    }

    @Override
    public String name() {
        return this.dogooder.name;
    }

    @Override
    public ClassType getType() {
        return this.dogooder.characterClass;
    }

    @Override
    public CharacterType getMonsterType() {
        return CharacterType.valueOf(getType().toString());
    }

    @Override
    public int icon() {
        return 0;
    }

    @Override
    public int getArmourClass() {
        return this.dogooder.armourClass;
    }

    @Override
    public int hitModifier() {
        for (int i : this.dogooder.items) {
            for (Item it : WER_ITEMS) {
                if (it.id == i && it.type == ItemType.WEAPON) {
                    return it.wephitmd;
                }
            }
        }
        return 0;
    }

    @Override
    public List<Dice> getDamage() {
        List<Dice> d = new ArrayList<>();
        if (this.dogooder.swingCount > 0) {
            for (int i = 0; i < this.dogooder.swingCount; i++) {
                d.add(this.dogooder.hpdamrc);
            }
        } else {
            for (int i : this.dogooder.items) {
                for (Item it : WER_ITEMS) {
                    if (it.id == i && it.type == ItemType.WEAPON) {
                        d.add(it.damage);
                        return d;
                    }
                }
            }
            if (d.isEmpty()) {
                d.add(Item.HANDS.damage);
            }
        }
        return d;
    }

    @Override
    public int getLevel() {
        return this.dogooder.charlev;
    }

    @Override
    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    @Override
    public void adjustHitPoints(int amt) {
        this.currentHitPoints = Utils.adjustValue(this.currentHitPoints, amt, this.getMaxHitPoints(), 0);
    }

    public int[] getMageSpellLevels() {
        return this.mageSpellsLevels;
    }

    public int[] getPriestSpellLevels() {
        return this.priestSpellLevels;
    }

    @Override
    public void decrementSpellPoints(Spells spell) {
        if (spell.getType() == ClassType.MAGE) {
            if (mageSpellsLevels[spell.getLevel() - 1] > 0) {
                mageSpellsLevels[spell.getLevel() - 1]--;
            }
        } else if (priestSpellLevels[spell.getLevel() - 1] > 0) {
            priestSpellLevels[spell.getLevel() - 1]--;
        }
    }

    @Override
    public int getCurrentMageSpellLevel() {
        for (int i = 6; i >= 0; i--) {
            if (mageSpellsLevels[i] > 0) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getCurrentPriestSpellLevel() {
        for (int i = 6; i >= 0; i--) {
            if (priestSpellLevels[i] > 0) {
                return i;
            }
        }
        return 0;
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

        if (this.dogooder.healPts > 0) {
            int roll = Utils.RANDOM.nextInt(100);
            boolean heal = roll < Math.max(this.getLevel() * 20, 50);
            if (heal) {
                adjustHitPoints(this.dogooder.healPts);
                adjustHealthCursor();
            }
        }

        for (Status s : Status.values()) {
            int roll = Utils.RANDOM.nextInt(100);
            boolean decr = false;
            switch (s) {
                case AFRAID:
                    decr = roll < Math.max(this.dogooder.charlev * 10, 50);
                    break;
                case SILENCED:
                case ASLEEP:
                    decr = roll < Math.max(this.dogooder.charlev * 20, 50);
                    break;
                case POISONED:
                case PARALYZED:
                    decr = roll < Math.max(this.dogooder.charlev * 7, 50);
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
        return this.dogooder.hpMax;
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
        this.healthCursor.adjust(currentHitPoints, this.dogooder.hpMax);
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
        int spLvl = getCurrentMageSpellLevel();
        for (int i = spLvl; i >= 0; i--) {
            for (Spells s : this.dogooder.knownSpells) {
                if (s.getType() == ClassType.MAGE && s.getLevel() == i + 1) {
                    mageSpellsLevels[i]--;
                    return s;
                }
            }

            Spells tmp = Spells.randomCombatSpell(ClassType.MAGE, i + 1);
            if (tmp != null) {
                mageSpellsLevels[i]--;
                return tmp;
            }
        }
        return null;
    }

    @Override
    public Spells castPriestSpell() {
        int spLvl = getCurrentPriestSpellLevel();
        for (int i = spLvl; i >= 0; i--) {
            for (Spells s : this.dogooder.knownSpells) {
                if (s.getType() == ClassType.PRIEST && s.getLevel() == i + 1) {
                    priestSpellLevels[i]--;
                    return s;
                }
            }
            Spells tmp = Spells.randomCombatSpell(ClassType.PRIEST, i + 1);
            if (tmp != null) {
                priestSpellLevels[i]--;
                return tmp;
            }
        }
        return null;
    }

    @Override
    public Breath breath() {
        return Breath.NONE;
    }

    @Override
    public boolean isUnaffected(Spells spell, CharacterType type) {
        if (this.dogooder.protection.contains(type)) {
            return Utils.RANDOM.nextInt(100) < 50;
        } else if (this.dogooder.purposed.contains(type)) {
            return Utils.RANDOM.nextInt(100) < 50;
        }
        return false;
    }
}

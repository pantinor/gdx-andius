package andius.objects;

import static andius.Constants.DEATHMSGS;
import andius.Constants.Status;
import utils.Utils;

public class MutableMonster extends Monster {

    private int acmodifier;
    private int currentHitPoints;
    private int currentMageSpellLevel;
    private int currentPriestSpellLevel;
    private final State status = new State();
    private final int maxHitPoints;
    private MonsterCursor monsterCursor;

    public MutableMonster(Monster m) {
        clone(m);
        this.maxHitPoints = this.hitPoints.roll();
        this.currentHitPoints = this.maxHitPoints;
        this.currentMageSpellLevel = this.mageSpellLevel;
        this.currentPriestSpellLevel = this.priestSpellLevel;
    }

    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    public void setCurrentHitPoints(int currentHitPoints) {
        this.currentHitPoints = currentHitPoints;
    }

    public int getCurrentMageSpellLevel() {
        return currentMageSpellLevel;
    }

    public int getCurrentPriestSpellLevel() {
        return currentPriestSpellLevel;
    }

    public State status() {
        return status;
    }

    public void processStatusAffects() {

        if (this.healpts > 0) {
            setCurrentHitPoints(this.healpts);
            adjustHealthBar();
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

    public int getMaxHitPoints() {
        return maxHitPoints;
    }

    public MonsterCursor getMonsterCursor() {
        return monsterCursor;
    }

    public void setMonsterCursor(MonsterCursor monsterCursor) {
        this.monsterCursor = monsterCursor;
    }

    public void adjustHealthBar() {
        this.monsterCursor.adjust(currentHitPoints, maxHitPoints);
    }

    public String getDamageTag() {
        double percent = (double) currentHitPoints / maxHitPoints;
        if (percent > 0.99) {
            if (this.type > 4) {
                return ", unharmed, growls ominously";
            } else {
                return "chortles merrily as the armor takes the full blow";
            }
        } else if (percent > 0.75) {
            return "still has lots of fight left";
        } else if (percent > 0.50) {
            if (this.type > 4) {
                return "tough hide softens the blow";
            } else {
                return "armor takes some of the impact";
            }
        } else if (percent < 0.00) {
            return DEATHMSGS[Utils.RANDOM.nextInt(DEATHMSGS.length)];
        } else {
            return "is feeling rather weak";
        }
    }

    public void setACModifier(int acmodifier) {
        this.acmodifier = acmodifier;
    }

    public int getACModifier() {
        return acmodifier;
    }

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
            spell = roll2 < 66 ? Spells.MAHALITO : Spells.MADALTO;
        }
        if (spellLevel == 6) {
            spell = roll2 < 66 ? Spells.MADALTO : Spells.ZILWAN;
        }
        if (spellLevel == 7) {
            spell = roll2 < 66 ? Spells.TILTOWAIT : Spells.TILTOWAIT;
        }

        double tmp = ((double) 1 / (double) (this.groupSize.roll() + 2)) * 100;
        int roll3 = Utils.RANDOM.nextInt(100);
        if (roll3 <= tmp) {
            this.currentMageSpellLevel--;
        }

        return spell;
    }

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

        double tmp = ((double) 1 / (double) (this.groupSize.roll() + 2)) * 100;
        int roll3 = Utils.RANDOM.nextInt(100);
        if (roll3 <= tmp) {
            this.currentPriestSpellLevel--;
        }

        return spell;
    }

}

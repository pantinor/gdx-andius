package andius.objects;

import andius.Constants.Breath;
import andius.Constants.CharacterType;
import static andius.Constants.DEATHMSGS;
import andius.Constants.Resistance;
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
    private MonsterCursor characterCursor;
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
    public CharacterType getType() {
        return this.dogooder.characterClass;
    }

    @Override
    public String icon() {

        switch (this.dogooder.characterClass) {
            case FIGHTER:
                return Utils.randomBoolean() ? "Burning_Gladiator" : (Utils.randomBoolean() ? "Barbarian_Headsplitter" : "Barbarian_Skullhunter");
            case MAGE:
                return Utils.randomBoolean() ? "Infernalist" : (Utils.randomBoolean() ? "Dark_Sorcerer" : "Raging_Mage");
            case PRIEST:
                return Utils.randomBoolean() ? "The_Hag" : "Barbaria";
            case THIEF:
                return Utils.randomBoolean() ? "Deadeye_Devious" : "Ron_The_Ripper";
            case MIDGET:
                return "Foreman_Kneebiter";
            case GIANT:
                return Utils.randomBoolean() ? "Malofur_Mangrinder" : "Angry_King_Chuck";
            case MYTHICAL:
                return Utils.randomBoolean() ? "The_Rootkraken" : "Timira_the_ManyHeaded";
            case DRAGON:
                return Utils.randomBoolean() ? "Kalyassa" : "Vemiath";
            case ANIMAL:
                return Utils.randomBoolean() ? "Tiger" : "Bear";
            case WERE:
                return Utils.randomBoolean() ? "Werebear" : "Bloodback";
            case UNDEAD:
                return Utils.randomBoolean() ? "Grand_Master_Oberon" : "Grand_Commander_Soeren";
            case DEMON:
                return Utils.randomBoolean() ? "Demon" : "Morgaroth";
            case INSECT:
                return Utils.randomBoolean() ? "Crawler" : "Giant_Spider";
            case ENCHANTED:
                return Utils.randomBoolean() ? "Void_Watcher" : "Energy_Elemental";
        }

        return "Paladin_Hunter_Male";
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

        for (int i : this.dogooder.items) {
            for (Item it : WER_ITEMS) {
                if (it.id == i && it.type == ItemType.WEAPON) {
                    d.add(it.damage);
                    return d;
                }
            }
        }

        d.add(Item.HANDS.damage);
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
    public void setCurrentHitPoints(int currentHitPoints) {
        this.currentHitPoints = currentHitPoints;
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
    public State status() {
        return status;
    }

    @Override
    public void processStatusAffects() {

        if (this.dogooder.healPts > 0) {
            setCurrentHitPoints(this.dogooder.healPts);
            adjustHealthBar();
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
    public MonsterCursor getMonsterCursor() {
        return characterCursor;
    }

    @Override
    public void setMonsterCursor(MonsterCursor monsterCursor) {
        this.characterCursor = monsterCursor;
    }

    @Override
    public void adjustHealthBar() {
        this.characterCursor.adjust(currentHitPoints, this.dogooder.hpMax);
    }

    @Override
    public String getDamageTag() {
        double percent = (double) currentHitPoints / this.dogooder.hpMax;
        if (percent > 0.99) {
            return "chortles merrily as the armor takes the full blow";
        } else if (percent > 0.75) {
            return "still has lots of fight left";
        } else if (percent > 0.50) {
            return "armor takes some of the impact";
        } else if (percent < 0.00) {
            return DEATHMSGS[Utils.RANDOM.nextInt(DEATHMSGS.length)];
        } else {
            return "is feeling rather weak";
        }
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
                    mageSpellsLevels[spLvl]--;
                    return s;
                }
            }
        }

        return null;
    }

    @Override
    public Spells castPriestSpell() {

        int spLvl = getCurrentPriestSpellLevel();

        for (int i = spLvl; i >= 0; i--) {
            for (Spells s : this.dogooder.knownSpells) {
                if (s.getType() == ClassType.CLERIC && s.getLevel() == i + 1) {
                    priestSpellLevels[spLvl]--;
                    return s;
                }
            }
        }

        return null;
    }

    @Override
    public Breath breath() {
        return Breath.NONE;
    }

    @Override
    public int getUnaffected() {
        for (int i = 0; i < this.dogooder.resistance.length; i++) {
            if (this.dogooder.resistance[i] == Resistance.MAGIC) {
                return 0;
            }
        }
        return 15;
    }
}

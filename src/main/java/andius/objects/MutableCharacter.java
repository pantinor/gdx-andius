package andius.objects;

import andius.Constants.Breath;
import andius.Constants.CharacterType;
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
    private int[] items;

    private final State status = new State();
    private HealthCursor healthCursor;
    private final DoGooder dogooder;

    private Item pendingSpellItem;
    private int pendingSpellItemIndex = -1;

    public MutableCharacter(DoGooder c) {
        this.dogooder = c;
        this.currentHitPoints = this.dogooder.hpMax;
        for (int i = 0; i < 7; i++) {
            mageSpellsLevels[i] = this.dogooder.spellAllowance[0][i];
            priestSpellLevels[i] = this.dogooder.spellAllowance[1][i];
        }
        this.items = this.dogooder.items.clone();
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
        for (int i : this.items) {
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
        if (this.dogooder.hpdamrc != null) {
            d.add(this.dogooder.hpdamrc);
        } else {
            for (int i : this.items) {
                for (Item it : WER_ITEMS) {
                    if (it.id == i && it.type == ItemType.WEAPON) {
                        d.add(it.damage);
                        return d;
                    }
                }
            }
        }
        if (d.isEmpty()) {
            d.add(Item.HANDS.damage);
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
            if (mageSpellsLevels[i] > 0 || hasItemSpell(ClassType.MAGE, i + 1)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public int getCurrentPriestSpellLevel() {
        for (int i = 6; i >= 0; i--) {
            if (priestSpellLevels[i] > 0 || hasItemSpell(ClassType.PRIEST, i + 1)) {
                return i;
            }
        }
        return 0;
    }

    private boolean hasItemSpell(ClassType spellType, int spellLevel) {
        return !getItemSpells(spellType, spellLevel).isEmpty();
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

    public int[] getItems() {
        return this.items;
    }

    @Override
    public Spells castMageSpell() {
        return castSpell(ClassType.MAGE, getCurrentMageSpellLevel());
    }

    @Override
    public Spells castPriestSpell() {
        return castSpell(ClassType.PRIEST, getCurrentPriestSpellLevel());
    }

    private Spells castSpell(ClassType spellType, int spLvl) {
        clearPendingSpellItem();

        for (int i = spLvl; i >= 0; i--) {
            int spellLevel = i + 1;

            for (Spells s : this.dogooder.knownSpells) {
                if (s.getType() == spellType && s.getLevel() == spellLevel) {
                    return s;
                }
            }

            List<ItemSpell> itemSpells = getItemSpells(spellType, spellLevel);
            if (!itemSpells.isEmpty()) {
                ItemSpell itemSpell = itemSpells.get(Utils.RANDOM.nextInt(itemSpells.size()));
                this.pendingSpellItem = itemSpell.item;
                this.pendingSpellItemIndex = itemSpell.index;
                return itemSpell.item.spell;
            }

            Spells tmp = Utils.randomCombatSpell(spellType, spellLevel);
            if (tmp != null) {
                return tmp;
            }
        }
        return null;
    }

    private List<ItemSpell> getItemSpells(ClassType spellType, int spellLevel) {
        List<ItemSpell> spells = new ArrayList<>();
        for (int i = 0; i < this.items.length; i++) {
            Item item = WER_ITEMS.get(this.items[i]);
            if (item.spell != null
                    && item.spell.getType() == spellType
                    && item.spell.getLevel() == spellLevel) {
                spells.add(new ItemSpell(item, i));
            }
        }
        return spells;
    }

    public boolean isPendingSpellFromItem(Spells spell) {
        return this.pendingSpellItem != null && this.pendingSpellItem.spell == spell;
    }

    public void resolvePendingSpellItemDecay() {
        if (this.pendingSpellItem == null || this.pendingSpellItemIndex < 0) {
            return;
        }

        Item usedItem = this.pendingSpellItem;
        int usedItemIndex = this.pendingSpellItemIndex;
        clearPendingSpellItem();

        if (usedItem.changeChance <= 0 || !Utils.percentChance(usedItem.changeChance)) {
            return;
        }

        Item changeTo = WER_ITEMS.get(usedItem.changeTo);
        if (changeTo.id != 0) {
            this.items[usedItemIndex] = changeTo.id;
        } else {
            removeItemAt(usedItemIndex);
        }
    }

    private void clearPendingSpellItem() {
        this.pendingSpellItem = null;
        this.pendingSpellItemIndex = -1;
    }

    private void removeItemAt(int index) {
        int[] updated = new int[this.items.length - 1];
        for (int src = 0, dst = 0; src < this.items.length; src++) {
            if (src != index) {
                updated[dst++] = this.items[src];
            }
        }
        this.items = updated;
    }

    private static class ItemSpell {

        final Item item;
        final int index;

        ItemSpell(Item item, int index) {
            this.item = item;
            this.index = index;
        }
    }

    @Override
    public Breath breath() {
        return Breath.NONE;
    }

    public boolean isUnaffectedByBreath(Breath breath, CharacterType type) {
        if (hasProtectionAgainst(type) && Utils.percentChance(50)) {
            return true;
        }

        if (breath == null || breath == Breath.NONE) {
            return false;
        }

        Resistance resistance = Resistance.NONE;

        switch (breath) {
            case FLAME:
                resistance = Resistance.FIRE;
                break;

            case COLD:
                resistance = Resistance.COLD;
                break;

            case POISON:
                resistance = Resistance.POISON;
                break;

            case DRAIN_BREATH:
                resistance = Resistance.LVLDRAIN;
                break;

            case STONE:
                resistance = Resistance.STONING;
                break;

            default:
                resistance = Resistance.NONE;
                break;
        }

        return resists(resistance);
    }

    @Override
    public boolean isUnaffected(Spells spell, CharacterType type) {
        if (hasProtectionAgainst(type) && Utils.percentChance(50)) {
            return true;
        }

        return spell != null && resists(Spells.resistanceForSpell(spell));
    }

    public boolean savingThrowDeath() {
        return this.dogooder.savingThrowDeath();
    }

    public boolean savingThrowPetrify() {
        return this.dogooder.savingThrowPetrify();
    }

    public boolean savingThrowBreath() {
        return this.dogooder.savingThrowBreath();
    }

    public boolean savingThrowSpell() {
        return this.dogooder.savingThrowSpell();
    }

    public boolean savingThrowPoison() {
        return savingThrowDeath();
    }

    public boolean savingThrowParalyze() {
        return savingThrowDeath();
    }

    public boolean hasPurposeAgainst(CharacterType type) {
        if (type == null) {
            return false;
        }

        if (this.dogooder.purposed.contains(type)) {
            return true;
        }

        for (int itemId : this.items) {
            Item item = WER_ITEMS.get(itemId);
            if (item.purposed(type)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasProtectionAgainst(CharacterType type) {
        if (type == null) {
            return false;
        }

        if (this.dogooder.protection.contains(type)) {
            return true;
        }

        for (int itemId : this.items) {
            Item item = WER_ITEMS.get(itemId);
            if (item.protection(type)) {
                return true;
            }
        }

        return false;
    }

    public boolean resists(Resistance resistance) {
        if (resistance == Resistance.NONE) {
            return false;
        }

        for (int itemId : this.items) {
            Item item = WER_ITEMS.get(itemId);
            if (item.resistance(resistance)) {
                return true;
            }
        }

        return false;
    }
}

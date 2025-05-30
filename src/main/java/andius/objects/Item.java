package andius.objects;

import andius.Constants.CharacterType;
import andius.Constants.Resistance;
import java.util.Objects;

public class Item implements Comparable<Item> {

    public enum ItemType {
        ANY, WEAPON, ARMOR, SHIELD, HELMET, GAUNTLET, SPECIAL, MISC;
    }

    public Item() {

    }

    public static final Item HANDS = new Item();

    static {
        Dice d = new Dice(2, 1);
        HANDS.name = "HANDS";
        HANDS.damage = d;
        HANDS.genericName = "HANDS";
        HANDS.usable = "FMCTWSLN";
    }

    public int id;
    public int scenarioID;
    public String name;
    public String genericName;
    public int iconID;
    public ItemType type;
    public long cost;
    public Dice damage;
    public int armourClass;
    public int extraSwings;
    public int stock;
    public boolean cursed;
    public String usable;
    public Spells spell;
    public int regeneration;
    public boolean autokill;
    public int range;
    public int wephitmd;
    public int special;
    public String alignment;
    public int changeTo;
    public int changeChance;
    public int wepvsty2Flags;             // protection
    public int wepvsty3Flags;             // resistance
    public int wepvstyFlags;              // purposed

    @Override
    public Item clone() {
        Item i = new Item();
        i.id = this.id;
        i.iconID = this.iconID;
        i.type = this.type;
        i.cost = this.cost;
        i.genericName = this.genericName;
        i.damage = this.damage;
        i.armourClass = this.armourClass;
        i.extraSwings = this.extraSwings;
        i.name = this.name;
        i.stock = this.stock;
        i.cursed = this.cursed;
        i.usable = this.usable;
        i.spell = this.spell;
        i.autokill = this.autokill;
        i.regeneration = this.regeneration;
        i.range = this.range;
        i.wephitmd = this.wephitmd;
        i.special = this.special;
        i.alignment = this.alignment;
        i.changeTo = this.changeTo;
        i.changeChance = this.changeChance;
        i.wepvsty2Flags = this.wepvsty2Flags;
        i.wepvsty3Flags = this.wepvsty3Flags;
        i.wepvstyFlags = this.wepvstyFlags;
        return i;
    }

    public boolean canUse(ClassType ct) {
        return this.usable.indexOf(ct.getAbbr()) != -1;
    }

    public boolean purposed(CharacterType type) {
        int bitIndex = type.ordinal();
        return ((wepvstyFlags >>> bitIndex) & 1) != 0;
    }

    public boolean protection(CharacterType type) {
        int bitIndex = type.ordinal();
        return ((wepvsty2Flags >>> bitIndex) & 1) != 0;
    }

    public boolean resistance(Resistance type) {
        int bitIndex = type.ordinal();
        return ((wepvsty3Flags >>> bitIndex) & 1) != 0;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + this.id;
        hash = 19 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Item other = (Item) obj;
        return this.id == other.id && this.name.equals(other.name);
    }

    @Override
    public int compareTo(Item o) {
        return Long.compare(this.id, o.id);
    }

    public String briefDescription() {
        return String.format("%s %s - %s",
                this.type,
                this.cursed ? "- Cursed" : "",
                spell != null ? spell + " - " + spell.getDescription() : "None");
    }

    public String vendorDescription() {
        switch (this.type) {
            case WEAPON:
                return String.format("%s %s %s %s %s",
                        damage,
                        extraSwings > 0 ? "xs:" + extraSwings : "",
                        wephitmd > 0 ? "hitmod:" + wephitmd : "",
                        regeneration > 0 ? "regen:" + regeneration : "",
                        spell != null ? "sp:" + spell : "");
            case ARMOR:
            case SHIELD:
            case HELMET:
            case GAUNTLET:
                return String.format("AC:%s %s %s",
                        armourClass,
                        regeneration > 0 ? "regen:" + regeneration : "",
                        spell != null ? "sp:" + spell : "");
            case SPECIAL:
            case MISC:
                return String.format("%s %s",
                        regeneration > 0 ? "regen:" + regeneration : "",
                        spell != null ? "sp:" + spell : "");
        }
        return String.format("%s %s",
                regeneration > 0 ? "regen:" + regeneration : "",
                spell != null ? "sp:" + spell : "");
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s",
                String.format("%-20s", this.name),
                String.format("%-8s", this.type),
                String.format("%-8s", "" + this.cost),
                String.format("%-2s", "" + this.damage),
                String.format("%-2s", "" + this.armourClass),
                String.format("%-2s", "" + this.extraSwings),
                String.format("%-8s", spell != null ? spell : "NA"),
                String.format("%-2s", "" + this.wephitmd),
                String.format("%-2s", "" + this.regeneration),
                String.format("%-2s", "" + this.stock),
                usable);
    }

}

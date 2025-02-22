package andius.objects;

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
        return i;
    }

    public boolean canUse(ClassType ct) {
        return this.usable.indexOf(ct.getAbbr()) != -1;
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

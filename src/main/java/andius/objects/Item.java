package andius.objects;

public class Item {

    public enum ItemType {
        WEAPON,
        ARMOR,
        SHIELD,
        HELM,
        GLOVES,
        POTION_SCROLL_SPECIAL,
        RING_AMULET;
    }

    String name;
    String genericName;
    int iconID;
    int type;
    long cost;
    Dice damage;
    int armourClass;
    int extraSwings;
    int stock;
    boolean cursed;
    String usable;
    Spells spell;
    int numberUses;
    int regeneration;
    int range;

    @Override
    public Item clone() {
        Item i = new Item();
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
        i.numberUses = this.numberUses;
        i.regeneration = this.regeneration;
        return i;
    }

    public boolean canUse(ClassType ct) {
        return this.usable.contains(ct.getAbbr());
    }

    public int getIconID() {
        return iconID;
    }

    public int getType() {
        return type;
    }

    public long getCost() {
        return cost;
    }

    public String getGenericName() {
        return genericName;
    }

    public Dice getDamage() {
        return damage;
    }

    public int getArmourClass() {
        return armourClass;
    }

    public int getExtraSwings() {
        return extraSwings;
    }

    public String getName() {
        return name;
    }

    public int getStock() {
        return stock;
    }

    public boolean isCursed() {
        return cursed;
    }

    public Spells getSpell() {
        return spell;
    }

    public int getNumberUses() {
        return numberUses;
    }

    public void use() {
        this.numberUses --;
    }

    public int getRegeneration() {
        return regeneration;
    }

    public int getRange() {
        return range;
    }
    
    @Override
    public String toString() {
        return String.format("%s\t%d\t%s\t%d\t%d\t%s\t%d\t%d", name, type,damage, armourClass, extraSwings,spell,numberUses,regeneration);
    }

}

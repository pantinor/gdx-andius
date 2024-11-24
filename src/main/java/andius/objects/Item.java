package andius.objects;

public class Item implements Comparable<Item> {

    public enum ItemType {
        WEAPON,
        ARMOR,
        SHIELD,
        HELM,
        GLOVES,
        POTION_SCROLL_SPECIAL,
        RING_AMULET;
    }
    
    public Item() {
        
    }

    public static final Item HANDS = new Item();

    static {
        Dice d = new Dice(2, 1);
        HANDS.damage = d;
        HANDS.genericName = "HANDS";
    }

    public String name;
    public String genericName;
    public int iconID;
    public int type;
    public int cost;
    public Dice damage;
    public int armourClass;
    public int extraSwings;
    public int stock;
    public boolean cursed;
    public String usable;
    public Spells spell;
    public int numberUses;
    public int regeneration;
    public int range;

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
        //System.out.printf("classtype %s for item %s can use=%s\n", ct, this.name, this.usable.indexOf(ct.getAbbr()) != -1);
        return this.usable.indexOf(ct.getAbbr()) != -1;
    }

    public void use() {
        this.numberUses--;
    }

    @Override
    public int compareTo(Item o) {
        if (this.type != o.type) {
            return Integer.compare(this.type, o.type);
        }
        if (this.armourClass != o.armourClass) {
            return Integer.compare(this.armourClass, o.armourClass);
        }
        return Long.compare(this.cost, o.cost);
    }

    @Override
    public String toString() {
        //return String.format("%s %d %s %s", name, cost, armourClass, spell != null ? spell : "");
        return String.format("%s\t%s\t%d\t%s\t%d\t%d\t%s\t%d\t%d\t%d\t%s", 
                name, 
                ItemType.values()[this.type],
                cost, 
                damage, 
                armourClass, 
                extraSwings, spell != null ? spell : "", 
                numberUses, 
                regeneration, 
                stock,
                usable);
    }

}

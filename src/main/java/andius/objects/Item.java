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

    int itemID;
    int type;
    long cost;
    int partyOwns;
    String genericName;
    Dice damage;
    int armourClass;
    int speed;
    String name;
    int stock;
    boolean cursed;
    String usable;
    int spellAffect;
    int numberUses;
    int regeneration;

    public void clone(Item i) {
        this.itemID = i.itemID;
        this.type = i.type;
        this.cost = i.cost;
        this.partyOwns = i.partyOwns;
        this.genericName = i.genericName;
        this.damage = i.damage;
        this.armourClass = i.armourClass;
        this.speed = i.speed;
        this.name = i.name;
        this.stock = i.stock;
        this.cursed = i.cursed;
        this.usable = i.usable;
        this.spellAffect = i.spellAffect;
        this.numberUses = i.numberUses;
        this.regeneration = i.regeneration;
    }

    public boolean canUse(ClassType ct) {
        return this.usable.contains(ct.getAbbr());
    }

    public int getItemID() {
        return itemID;
    }

    public int getType() {
        return type;
    }

    public long getCost() {
        return cost;
    }

    public int getPartyOwns() {
        return partyOwns;
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

    public int getSpeed() {
        return speed;
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

    public int getSpellAffect() {
        return spellAffect;
    }

    public int getNumberUses() {
        return numberUses;
    }

    public int getRegeneration() {
        return regeneration;
    }

}

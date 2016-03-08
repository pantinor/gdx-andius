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

    private int itemID;
    private int type;
    private long cost;
    private int partyOwns;
    private String genericName;
    private Dice damage;
    private int armourClass;
    private int speed;
    private String name;
    private int stock;
    private boolean cursed;
    private String usable;

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

}

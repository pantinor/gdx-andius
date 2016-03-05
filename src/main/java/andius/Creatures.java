/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

/**
 *
 * @author Paul
 */
public enum Creatures {
    NONE("", 0, 0, false, 0),
    ZOMBIE("Zombie", 48, 3, false, 50),;

    private final String name;
    private final int basehp;
    private final int exp;
    private final boolean ranged;
    private final int gold;

    private Creatures(String name, int basehp, int exp, boolean ranged, int gold) {
        this.name = name;
        this.basehp = basehp;
        this.exp = exp;
        this.ranged = ranged;
        this.gold = gold;
    }

    public String getName() {
        return name;
    }

    public int getBasehp() {
        return basehp;
    }

    public int getExp() {
        return exp;
    }

    public boolean isRanged() {
        return ranged;
    }

    public int getGold() {
        return gold;
    }

}

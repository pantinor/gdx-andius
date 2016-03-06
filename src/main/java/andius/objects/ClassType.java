/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

/**
 *
 * @author Paul
 */
public enum ClassType {

    FIGHTER("F", 10, 11, 0, 0, 0, 0, 0),
    MAGE("M", 4, 0, 11, 0, 0, 0, 0),
    CLERIC("C", 8, 0, 0, 11, 0, 0, 0),
    THIEF("T", 6, 0, 0, 0, 0, 11, 0),
    WIZARD("W", 6, 0, 12, 12, 0, 0, 0),
    SAMURAI("S", 8, 15, 11, 10, 14, 10, 0),
    LORD("L", 10, 15, 12, 12, 15, 14, 15),
    NINJA("N", 6, 17, 17, 17, 17, 17, 17);

    private final int minStr, minIntell, minPiety, minVitality, minAgility, minLuck;
    private final String abbr;
    private final int hitDie;

    private ClassType(String abbr, int hitDie, int minStr, int minIntell, int minPiety, int minVitality, int minAgility, int minLuck) {
        this.abbr = abbr;
        this.minStr = minStr;
        this.minIntell = minIntell;
        this.minPiety = minPiety;
        this.minVitality = minVitality;
        this.minAgility = minAgility;
        this.minLuck = minLuck;
        this.hitDie = hitDie;
    }

    public String getAbbr() {
        return this.abbr;
    }

    public int getMinStr() {
        return minStr;
    }

    public int getMinIntell() {
        return minIntell;
    }

    public int getMinPiety() {
        return minPiety;
    }

    public int getMinVitality() {
        return minVitality;
    }

    public int getMinAgility() {
        return minAgility;
    }

    public int getMinLuck() {
        return minLuck;
    }

    public int getHitDie() {
        return hitDie;
    }

}

package andius.objects;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum ClassType {

    FIGHTER("F", 10, 11, 0, 0, 0, 0, 0, UltimaSprite.icon(42)),
    MAGE("M", 4, 0, 11, 0, 0, 0, 0, UltimaSprite.icon(32)),
    PRIEST("C", 8, 0, 0, 11, 0, 0, 0, UltimaSprite.icon(38)),
    THIEF("T", 6, 0, 0, 0, 0, 11, 0, UltimaSprite.icon(44)),
    BISHOP("W", 6, 0, 12, 12, 0, 0, 0, UltimaSprite.icon(46)),
    SAMURAI("S", 8, 15, 11, 10, 14, 10, 0, UltimaSprite.icon(40)),
    LORD("L", 10, 15, 12, 12, 15, 14, 15, UltimaSprite.icon(42)),
    NINJA("N", 6, 17, 17, 17, 17, 17, 17, UltimaSprite.icon(44));

    private final int minStr, minIntell, minPiety, minVitality, minAgility, minLuck;
    private final String abbr;
    private final int hitDie;
    private final TextureRegion tr;

    private ClassType(String abbr, int hitDie, int minStr, int minIntell, int minPiety, int minVitality, int minAgility, int minLuck, TextureRegion tr) {
        this.abbr = abbr;
        this.minStr = minStr;
        this.minIntell = minIntell;
        this.minPiety = minPiety;
        this.minVitality = minVitality;
        this.minAgility = minAgility;
        this.minLuck = minLuck;
        this.hitDie = hitDie;
        this.tr = tr;
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

    public TextureRegion getIcon() {
        return tr;
    }

}

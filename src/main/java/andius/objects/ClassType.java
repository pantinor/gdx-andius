package andius.objects;

import com.badlogic.gdx.graphics.g2d.Animation;

public enum ClassType {

    FIGHTER("F", 10, 11, 0, 0, 0, 0, 0, TibianSprite.animation("Knight_Knight_Male")),
    MAGE("M", 4, 0, 11, 0, 0, 0, 0, TibianSprite.animation("Sorcerer_Conjurer_Male")),
    PRIEST("C", 8, 0, 0, 11, 0, 0, 0, TibianSprite.animation("Druid_Grove_Keeper_Male")),
    THIEF("T", 6, 0, 0, 0, 0, 11, 0, TibianSprite.animation("Paladin_Demon_Hunter_Male")),
    BISHOP("W", 6, 0, 12, 12, 0, 0, 0, TibianSprite.animation("Sorcerer_Wizard_Male")),
    SAMURAI("S", 8, 15, 11, 10, 14, 10, 0, TibianSprite.animation("Knight_Warmaster_Male")),
    LORD("L", 10, 15, 12, 12, 15, 14, 15, TibianSprite.animation("Knight_Arena_Champion_Male")),
    NINJA("N", 6, 17, 17, 17, 17, 17, 17, TibianSprite.animation("Paladin_Assassin_Male"));

    private final int minStr, minIntell, minPiety, minVitality, minAgility, minLuck;
    private final String abbr;
    private final int hitDie;
    private final Animation anim;

    private ClassType(String abbr, int hitDie, int minStr, int minIntell, int minPiety, int minVitality, int minAgility, int minLuck, Animation anim) {
        this.abbr = abbr;
        this.minStr = minStr;
        this.minIntell = minIntell;
        this.minPiety = minPiety;
        this.minVitality = minVitality;
        this.minAgility = minAgility;
        this.minLuck = minLuck;
        this.hitDie = hitDie;
        this.anim = anim;
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

    public Animation getAnimation() {
        return anim;
    }

}

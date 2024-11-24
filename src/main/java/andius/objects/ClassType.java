/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.TibianSprite;
import com.badlogic.gdx.graphics.g2d.Animation;

/**
 *
 * @author Paul
 */
public enum ClassType {

    FIGHTER("F", 10, 11, 0, 0, 0, 0, 0, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Knight_Arena_Champion_Male)),
    MAGE("M", 4, 0, 11, 0, 0, 0, 0, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Sorcerer_Battle_Mage_Male)),
    CLERIC("C", 8, 0, 0, 11, 0, 0, 0, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Druid_Shaman_Male)),
    THIEF("T", 6, 0, 0, 0, 0, 11, 0, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Paladin_Assassin_Male)),
    WIZARD("W", 6, 0, 12, 12, 0, 0, 0, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Sorcerer_Wizard_Male)),
    SAMURAI("S", 8, 15, 11, 10, 14, 10, 0, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Paladin_Veteran_Paladin_Male)),
    LORD("L", 10, 15, 12, 12, 15, 14, 15, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Knight_Warmaster_Male)),
    NINJA("N", 6, 17, 17, 17, 17, 17, 17, TibianSprite.Type.characters, TibianSprite.characterAnimation(TibianSprite.Character.Paladin_Demon_Hunter_Male));

    private final int minStr, minIntell, minPiety, minVitality, minAgility, minLuck;
    private final String abbr;
    private final int hitDie;
    private final Animation anim;
    private final TibianSprite.Type spriteType;

    private ClassType(String abbr, int hitDie, int minStr, int minIntell, int minPiety, int minVitality, int minAgility, int minLuck, TibianSprite.Type spriteType, Animation anim) {
        this.abbr = abbr;
        this.minStr = minStr;
        this.minIntell = minIntell;
        this.minPiety = minPiety;
        this.minVitality = minVitality;
        this.minAgility = minAgility;
        this.minLuck = minLuck;
        this.hitDie = hitDie;
        this.spriteType = spriteType;
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

    public TibianSprite.Type getSpriteType() {
        return spriteType;
    }

}

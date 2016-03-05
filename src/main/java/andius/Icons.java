/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import utils.Utils;

/**
 *
 * @author Paul
 */
public enum Icons {

    WIZARD,
    CLERIC,
    PALADIN,
    RANGER,
    BARBARIAN,
    THIEF,
    DRUID,
    TORTURER,
    FIGHTER,
    SWASHBUCKLER,
    KNIGHT,
    WITCH,
    BAT_MAJOR,
    BAT_MINOR,
    SPIDER_MAJOR,
    SPIDER_MINOR,
    BLACK_WIDOW_MAJOR,
    BLACK_WIDOW_MINOR,
    DWARF_FIGHTER,
    SKELETON,
    SKELETON_SWORDSMAN,
    LICHE,
    SKELETON_ARCHER,
    ORC,
    ORC_SHIELDSMAN,
    TROLL,
    OGRE_SHAMAN,
    OGRE,
    ORC_SHAMAN,
    RAT_MAJOR,
    RAT_MINOR,
    ZOMBIE_GREEN,
    ZOMBIE_BLUE,
    WRAITH,
    DWARF_CLERIC,
    DWARF_LORD,
    MINOTAUR,
    VAMPIRE_RED,
    VAMPIRE_BLUE,
    SORCERER,
    SORCERER_EVIL,
    WOLF_BLACK,
    WOLF_BROWN,
    MERMAN_SWORDSMAN,
    MERMAN_PIKE,
    MERMAN_SHAMAN,
    MERMAN_SWORDSMAN_BLUE,
    MERMAN_PIKE_BLUE,
    MERMAN_SHAMAN_BLUE,
    GAZER,
    GAZER_BLUE,
    PHANTOM_BLUE,
    PHANTOM_RED,
    PHANTOM_GREY,
    PIXIE,
    PIXIE_RED,
    DEMON_RED,
    DEMON_BLUE,
    DEMON_GREEN,
    ANGEL,
    DARK_ANGEL,
    HALFLING,
    HALFLING_RANGER,
    HALFLING_SHIELDSMAN,
    HALFLING_WIZARD,
    WISP_MAJOR,
    WISP_MINOR,
    DRAGON_BLACK,
    DRAGON_RED,
    DRAGON_BLUE,
    DRAGON_GREEN,
    HAWK_WHITE,
    HAWK_BROWN,
    CROW,
    MUMMY,
    MUMMY_KING,
    GOLEM_STONE,
    GOLEM_FIRE,
    GOLEM_EARTH,
    GOLEM_ICE,
    GOLEM_MUD,
    COBRA_MAJOR,
    COBRA_MINOR,
    KING_RED,
    QUEEN_RED,
    KING_BLUE,
    QUEEN_BLUE,
    BEETLE_BLACK,
    BEETLE_RED,
    BEETLE_BLACK_MINOR,
    BEETLE_RED_MINOR,
    GHOST_MINOR,
    GHOST_MAJOR,
    SLIME_GREEN,
    SLIME_RED,
    SLIME_PURPLE,
    GRUB_MINOR,
    GRUB_MAJOR,
    ELEMENTAL_PURPLE,
    ELEMENTAL_BLUE,
    ELEMENTAL_ORANGE,
    ELEMENTAL_CYAN,
    ELEMENTAL_BROWN,
    BUTTERFLY_WHITE,
    BUTTERFLY_RED,
    BUTTERFLY_BLACK,
    FROG_GREEN,
    FROG_BLUE,
    FROG_BROWN,
    INSECT_SWARM,
    MIMIC,
    SHOPKEEPER_BROWN,
    SHOPKEEPER_BLOND,
    BLOOD_PRIEST,
    BARBARIAN_AXE,
    DEMON_LORD,
    DARK_WIZARD,
    FIGHTER_RED,
    HOLY_AVENGER,
    SWASHBUCKLER_BLUE,
    DEATH_KNIGHT,
    BRAWLER,
    BRAWLER_DARK,
    BRAWLER_BLOND,
    ELVEN_SWORDSMAN_GREEN,
    ELVEN_WIZARD_GREEN,
    ELVEN_ARCHER_GREEN,
    ELVEN_SWORDSMAN_BLUE,
    ELVEN_WIZARD_BLUE,
    ELVEN_ARCHER_BLUE,;

    private Animation animation;

    public Animation getAnimation() {
        return animation;
    }

    public static void init(TextureAtlas atlas) {
        for (Icons hero : Icons.values()) {
            int frameRate = Utils.getRandomBetween(3, 5);
            hero.animation = new Animation(frameRate, atlas.findRegions(hero.toString()));
        }
    }

}

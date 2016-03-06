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
public enum Creatures {

    BUBBLY_SLIME("Bubbly Slime", 12, "1d3+1", "1d1", "M", "S"),
    GAS_CLOUD("Gas Cloud", 10, "2d5+1", "1d4", "-", "Z,R"),
    LVL_10_MAGE("Lvl 10 Mage", 10, "10d4", "1d4", "-", "-"),
    LVL_4_THIEF("Lvl 4 Thief", 10, "4d8+3", "1d6,2d6", "-", "-"),
    ORC("Orc", 10, "1d4", "1d4", "F", "S,R"),
    RAVER_LORD("Raver Lord", 10, "15d10", "3d12,3d12", "F", "-"),
    ROGUE("Rogue", 10, "2d5+1", "1d4,2d2+1", "-", "S,R"),
    UNDEAD_KOBOLD("Undead Kobold", 10, "2d3+2", "1d4+1", "F,C", "-"),
    ARCH_MAGE("Arch Mage", 9, "8d4+2", "1d4", "-", "-"),
    EARTH_GIANTS("Earth Giants", 9, "1d1+40", "2d8,2d8", "M", "-"),
    BUSHWACKER("Bushwacker", 8, "3d6+1", "1d6+1,2d4+1", "-", "S,R"),
    CAPYBARA("Capybara", 8, "4d4", "1d10", "-", "P,R"),
    COYOTE("Coyote", 8, "4d6", "4d4", "P,L,ST", "R"),
    KOBOLD("Kobold", 8, "2d3+1", "1d2+1,1d2+1", "C", "S,R"),
    LVL_7_MAGE("Lvl 7 Mage", 8, "7d4", "1d4", "-", "S"),
    GIANT_TOAD("Giant Toad", 7, "4d5", "1d4,1d6,2d4+2", "F", "P,R"),
    LVL_6_MAGE("Lvl 6 Mage", 7, "6d4", "1d4", "-", "S"),
    SHADE("Shade", 7, "3d8+3", "1d4+1", "M", "-"),
    CREEPING_CRUD("Creeping Crud", 6, "3d4", "1d3+1", "C,M", "P"),
    FROST_GIANT("Frost Giant", 6, "1d8+50", "3d10", "C", "-"),
    HIGHWAYMAN("Highwayman", 6, "3d4+2", "1d2+1,1d2+1,1d2+1,1d2+1", "-", "K,S,R"),
    HUGE_SPIDER("Huge Spider", 6, "2d8+2", "1d6", "-", "P,S"),
    LVL_5_MAGE("Lvl 5 Mage", 6, "5d4", "1d4", "-", "S"),
    LVL_6_NINJA("Lvl 6 Ninja", 6, "6d10", "1d6,1d6,1d6", "-", "K"),
    MEDUSALIZARD("Medusalizard", 6, "5d8", "1d3", "-", "ST"),
    ROTTING_CORPSE("Rotting Corpse", 6, "2d8", "1d3,1d3,1d6", "-", "Z"),
    VORPAL_BUNNY("Vorpal Bunny", 6, "1d6,1d8", "1d6", "C", "K,R"),
    WERE_BEAR("Were Bear", 6, "5d8", "3d6+1", "C,P", "Z,R"),
    WERERAT("Wererat", 6, "3d8+1", "1d8", "M", "S"),
    GARGOYLE("Gargoyle", 5, "4d8+4", "1d3,1d3,1d6,1d4", "M", "-"),
    LVL_1_NINJA("Lvl 1 Ninja", 5, "2d4+2", "1d4,1d4,1d4", "-", "K,S"),
    LVL_1_PRIEST("Lvl 1 Priest", 5, "1d8", "1d8", "-", "S,R"),
    LVL_3_SAMURAI("Lvl 3 Samurai", 5, "3d6+4", "1d4+1,1d6+1,1d4+1", "-", "R"),
    LVL_4_MAGE("Lvl 4 Mage", 5, "4d4", "1d4", "-", "-"),
    OGRE("Ogre", 5, "4d8+1", "2d6", "-", "S,R"),
    WEREWOLF("Werewolf", 5, "4d8+3", "2d4,2d4", "M", "-"),
    BISHOP("Bishop", 4, "4d8", "1d3,1d3,1d6", "-", "S"),
    CREEPING_COIN("Creeping Coin", 4, "1d1", "1d1", "F,C,P,L,ST", "H,B"),
    DRAGON_FLY("Dragon Fly", 4, "2d8", "1d4,1d4,1d6", "F", "S,B"),
    DRAGON_PUPPY("Dragon Puppy", 4, "5d10", "1d10", "-", "-"),
    GIANT_SPIDER("Giant Spider", 4, "4d8+4", "2d4", "-", "P"),
    GRAVE_MIST("Grave Mist", 4, "4d8", "1d4,1d4,1d8", "-", "Z"),
    HIGH_WIZARD("High Wizard", 4, "12d4", "1d4", "F", "S"),
    LESSER_DEMON("Lesser Demon", 4, "10d8", "2d6,2d6,1d3,1d3,1d4+4", "-", "H,L"),
    LVL_1_MAGE("Lvl 1 Mage", 4, "1d4+1", "2d2", "-", "S,R"),
    LVL_3_PRIEST("Lvl 3 Priest", 4, "3d8+1", "1d8+2", "-", "P,S,R"),
    LVL_5_PRIEST("Lvl 5 Priest", 4, "5d8", "1d6+2", "-", "-"),
    LVL_7_THIEF("Lvl 7 Thief", 4, "7d6", "1d8,3d8", "-", "R"),
    LVL_8_NINJA("Lvl 8 Ninja", 4, "8d4", "2d6,1d6", "-", "K"),
    MASTER_THIEF("Master Thief", 4, "4d6", "1d6,1d6,2d6", "-", "S,R"),
    NIGHTSTALKER("Nightstalker", 4, "5d8+3", "1d6", "M", "L"),
    OGRE_LORD("Ogre Lord", 4, "8d8", "1d12", "-", "-"),
    PRIESTESS("Priestess", 4, "3d8+1", "1d6+2", "-", "S"),
    THIEF("Thief", 4, "9d6", "1d8,1d3,3d8,2d10", "-", "R"),
    TROLL("Troll", 4, "6d8+6", "1d4,1d4,1d4", "-", "-"),
    WERETIGER("Weretiger", 4, "5d8", "2d6,2d6,1d4", "M", "Z,S"),
    ZOMBIE("Zombie", 4, "1d10+1", "1d6", "-", "Z"),
    BORING_BEETLE("Boring Beetle", 3, "5d8", "5d4", "-", "-"),
    FIRE_GIANT("Fire Giant", 3, "11d8+4", "5d6", "F", "-"),
    GAS_DRAGON("Gas Dragon", 3, "5d8", "1d4,1d4,3d6", "-", ",B"),
    HIGH_PRIEST_1("High Priest 1", 3, "8d8", "1d8+2", "-", "-"),
    LIFESTEALER("Lifestealer", 3, "5d8+3", "1d4", "P,L,ST,M", "L"),
    LVL_3_NINJA("Lvl 3 Ninja", 3, "3d8", "1d4,1d4,1d4,1d4,1d4", "-", "P,K"),
    LVL_8_PRIEST("Lvl 8 Priest", 3, "7d8", "1d8", "-", "-"),
    MASTER_NINJA("Master Ninja", 3, "10d4", "1d10+3,1d10+3,1d10+3", "-", "K"),
    POISON_GIANT("Poison Giant", 3, "1d1+80", "4d10", "-", "B"),
    SPIDERLING("Spiderling", 3, "1d8+1", "1d4", "-", "P,S"),
    SWORDSMAN("Swordsman", 3, "3d10", "2d7", "-", "S"),
    WYVERN("Wyvern", 3, "7d8+7", "2d8,1d6", "-", "P"),
    CHAMP_SAMURAI("Champ Samurai", 2, "5d4", "1d12+2", "-", "-"),
    CHIMERA("Chimera", 2, "9d6", "1d3,1d3,1d4,1d4,2d4,3d4", "F", "B"),
    GORGON("Gorgon", 2, "8d8", "2d6", "-", "B"),
    HIGH_PRIEST_2("High Priest 2", 2, "8d8", "1d8+4", "-", "-"),
    HIGH_PRIEST_3("High Priest 3", 2, "11d8", "1d8,1d8", "-", "-"),
    LVL_8_BISHOP("Lvl 8 Bishop", 2, "8d8", "1d8+4", "-", "-"),
    ARCH_THIEF("Arch Thief", 2, "12d6", "1d8,5d8", "P", "R"),
    MINOR_DAIMYO("Minor Daimyo", 2, "4d10", "1d12", "-", "S,H"),
    SPIRIT("Spirit", 2, "7d3+2", "1d4", "M", "P"),
    ATTACK_DOG("Attack Dog", 1, "4d8", "1d6", "-", "S,R"),
    SUPREME_ARCH_MAGE("Supreme Arch Mage", 0, "20d4", "1d4", "-", "-"),
    BLEEB("Bleeb", 0, "10d8", "1d8+1,1d8+1", "F,C,P,L,ST,M", "R"),
    KILLER_WOLF("Killer Wolf", 0, "6d8", "2d4,2d4", "-", "-"),
    LVL_10_FIGHTER("Lvl 10 Fighter", 0, "7d10", "1d12,1d12", "-", "-"),
    LVL_7_FIGHTER("Lvl 7 Fighter", 0, "7d10", "1d12,1d12", "-", "-"),
    MAJOR_DAIMYO("Major Daimyo", 0, "7d12", "1d10,1d4", "-", "H"),
    FIRE_DRAGON("Fire Dragon", -1, "12d8", "1d4,1d4,4d4", "-", "B"),
    GAZE_HOUND("Gaze Hound", -1, "4d8", "1d2", "M", "Z,R"),
    HATAMOTO("Hatamoto", -1, "12d4", "3d8,3d8,3d8", "-", "K"),
    HIGH_NINJA("High Ninja", -1, "12d4", "3d8,3d8,3d8", "-", "K"),
    LVL_8_FIGHTER("Lvl 8 Fighter", -1, "8d10", "1d12+2,1d12+2", "-", "-"),
    VAMPIRE("Vampire", -1, "11d8", "2d8,1d4,1d4", "P,L,ST", "Z,L"),
    DRAGON_ZOMBIE("Dragon Zombie", -2, "12d8", "1d8,1d8,3d12", "-", "-"),
    THE_HIGH_MASTER("The High Master", -2, "15d4", "3d12,3d12,3d6", "F,C,P,L,ST,M", "K"),
    FLACK("Flack", -3, "15d12", "4d8+3", "F,C,P,L,ST,M", "ST,P,Z,K"),
    GREATER_DEMON("Greater Demon", -3, "11d8", "2d12,1d6,1d4,1d4,1d4", "-", "P,H,Z,L"),
    MURPHYS_GHOST("Murphys Ghost", -3, "10d10+10", "1d1+1", "F,C,P,L,ST,M", "S"),
    MAELIFIC("Maelific", -5, "25d4", "1d4,1d0+1", "-", "P,Z"),
    VAMPIRE_LORD("Vampire Lord", -5, "20d8", "1d4", "-", "Z"),
    WERDNA("Werdna", -7, "10d10+20", "8d5,8d5", "F,C,P", "ST,P,Z,L,K"),
    WILL_O_WISP("Will O Wisp", -8, "10d8", "2d8", "-", "-");

    private final String name;
    private final int ac;
    private final String hd;
    private final String damage;
    private final String resistances;
    private final String abilities;

    private Creatures(String name, int ac, String hd, String damage, String resistances, String abilities) {
        this.name = name;
        this.ac = ac;
        this.hd = hd;
        this.damage = damage;
        this.resistances = resistances;
        this.abilities = abilities;
    }

}

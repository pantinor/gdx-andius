/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package andius.objects;

import andius.Constants.SpellArea;

/**
 *
 * @author Paul
 */
public enum Spells {

    DUMAPIC(ClassType.WIZARD, 1, SpellArea.CASTER, 0, 0, "Clarity"),
    HALITO(ClassType.WIZARD, 1, SpellArea.FOE, 1, 8, "Little Fire"),
    KATINO(ClassType.WIZARD, 1, SpellArea.GROUP, 0, 0, "Bad Air"),
    MOGREF(ClassType.WIZARD, 1, SpellArea.CASTER, 0, 0, "Body Iron"),
    //
    BOLATU(ClassType.WIZARD, 2, SpellArea.FOE, 0, 0, "Heart of Stone"),
    DESTO(ClassType.WIZARD, 2, SpellArea.CASTER, 0, 0, "Unlock"),
    DILTO(ClassType.WIZARD, 2, SpellArea.GROUP, 0, 0, "Darkness"),
    MELITO(ClassType.WIZARD, 2, SpellArea.GROUP, 1, 8, "Little Sparks"),
    PONTI(ClassType.WIZARD, 2, SpellArea.CHAR, 0, 0, "Speed"),
    SOPIC(ClassType.WIZARD, 2, SpellArea.CASTER, 0, 0, "Glass"),
    //
    CALIFIC(ClassType.WIZARD, 3, SpellArea.CASTER, 0, 0, "Reveal"),
    CORTU(ClassType.WIZARD, 3, SpellArea.PARTY, 0, 0, "Magic Screen"),
    KANTIOS(ClassType.WIZARD, 3, SpellArea.GROUP, 0, 0, "Disruption"),
    MAHALITO(ClassType.WIZARD, 3, SpellArea.GROUP, 4, 24, "Big Fire"),
    MOLITO(ClassType.WIZARD, 3, SpellArea.GROUP, 3, 18, "Spark Storm"),
    //
    DALTO(ClassType.WIZARD, 4, SpellArea.GROUP, 6, 36, "Blizzard Blast"),
    LAHALITO(ClassType.WIZARD, 4, SpellArea.GROUP, 6, 36, "Flame Storm"),
    LITOFEIT(ClassType.WIZARD, 4, SpellArea.PARTY, 0, 0, "Levitate"),
    MORLIS(ClassType.WIZARD, 4, SpellArea.GROUP, 0, 0, "Fear"),
    ROKDO(ClassType.WIZARD, 4, SpellArea.GROUP, 0, 0, "Stun"),
    TZALIK(ClassType.WIZARD, 4, SpellArea.FOE, 24, 58, "Fist of God"),
    //
    BACORTU(ClassType.WIZARD, 5, SpellArea.GROUP, 0, 0, "Fizzle Field"),
    MADALTO(ClassType.WIZARD, 5, SpellArea.GROUP, 8, 64, "Frost"),
    MAKANITO(ClassType.WIZARD, 5, SpellArea.ALL, 0, 0, "Deadly Air"),
    MAMORLIS(ClassType.WIZARD, 5, SpellArea.ALL, 0, 0, "Terror"),
    SOCORDI(ClassType.WIZARD, 5, SpellArea.SUMMON, 0, 0, "Conjuring"),
    PALIOS(ClassType.WIZARD, 5, SpellArea.ALL, 0, 0, "Anti-Magic"),
    VASKYRE(ClassType.WIZARD, 5, SpellArea.GROUP, 0, 0, "Rainbow Rays"),
    //
    HAMAN(ClassType.WIZARD, 6, SpellArea.NONE, 0, 0, "Change"),
    LADALTO(ClassType.WIZARD, 6, SpellArea.GROUP, 34, 98, "Ice Storm"),
    LAKANITO(ClassType.WIZARD, 6, SpellArea.GROUP, 0, 0, "Suffocation"),
    LOKARA(ClassType.WIZARD, 6, SpellArea.ALL, 0, 0, "Earth Feast"),
    MAMOGREF(ClassType.WIZARD, 6, SpellArea.CHAR, 0, 0, "Wall of Force"),
    MASOPIC(ClassType.WIZARD, 6, SpellArea.PARTY, 0, 0, "Big Glass"),
    ZILWAN(ClassType.WIZARD, 6, SpellArea.FOE, 0, 0, "Dispel"),
    //
    ABRIEL(ClassType.WIZARD, 7, SpellArea.DIVINE, 0, 0, "Divine WIsh"),
    MAHAMAN(ClassType.WIZARD, 7, SpellArea.PARTY, 0, 0, "Great Change"),
    MALOR(ClassType.WIZARD, 7, SpellArea.PARTY, 0, 0, "Apport"),
    MAWXIWTZ(ClassType.WIZARD, 7, SpellArea.ALL, 0, 0, "Madhouse"),
    TILTOWAIT(ClassType.WIZARD, 7, SpellArea.ALL, 10, 100, "Nuke 'em 'till they glow"),
    //
    BADIOS(ClassType.CLERIC, 1, SpellArea.FOE, 1, 8, "Harm"),
    DIOS(ClassType.CLERIC, 1, SpellArea.CHAR, 1, 8, "Heal"),
    KALKI(ClassType.CLERIC, 1, SpellArea.PARTY, 0, 0, "Blessings"),
    MILWA(ClassType.CLERIC, 1, SpellArea.PARTY, 0, 0, "Light"),
    PORFIC(ClassType.CLERIC, 1, SpellArea.CASTER, 0, 0, "Shield"),
    //
    CALFO(ClassType.CLERIC, 2, SpellArea.CASTER, 0, 0, "X-ray Vision"),
    KATU(ClassType.CLERIC, 2, SpellArea.GROUP, 0, 0, "Charm"),
    MANIFO(ClassType.CLERIC, 2, SpellArea.GROUP, 0, 0, "Statue"),
    MATU(ClassType.CLERIC, 2, SpellArea.PARTY, 0, 0, "Blessing"),
    MONTINO(ClassType.CLERIC, 2, SpellArea.GROUP, 0, 0, "Still Air"),
    //
    BAMATU(ClassType.CLERIC, 3, SpellArea.PARTY, 0, 0, "Prayer"),
    DIALKO(ClassType.CLERIC, 3, SpellArea.CHAR, 0, 0, "Softness"),
    HAKANIDO(ClassType.CLERIC, 3, SpellArea.FOE, 0, 0, "Magic Drain"),
    LATUMAPIC(ClassType.CLERIC, 3, SpellArea.GROUP, 0, 0, "Identification"),
    LOMILWA(ClassType.CLERIC, 3, SpellArea.PARTY, 0, 0, "More Light"),
    //
    BADIAL(ClassType.CLERIC, 4, SpellArea.FOE, 2, 16, "More Hurt"),
    BARIKO(ClassType.CLERIC, 4, SpellArea.GROUP, 6, 15, "Razor Wind"),
    DIAL(ClassType.CLERIC, 4, SpellArea.CHAR, 2, 16, "More Heal"),
    LATUMOFIS(ClassType.CLERIC, 4, SpellArea.CHAR, 0, 0, "Cure Poison"),
    MAPORFIC(ClassType.CLERIC, 4, SpellArea.PARTY, 0, 0, "Big Shield"),
    //
    BADI(ClassType.CLERIC, 5, SpellArea.FOE, 0, 0, "Death"),
    BADIALMA(ClassType.CLERIC, 5, SpellArea.FOE, 3, 24, "Great Hurt"),
    BAMORDI(ClassType.CLERIC, 5, SpellArea.SUMMON, 0, 0, "Summoning"),
    DI(ClassType.CLERIC, 5, SpellArea.CHAR, 0, 0, "Life"),
    DIALMA(ClassType.CLERIC, 5, SpellArea.CHAR, 3, 24, "Great Heal"),
    KANDI(ClassType.CLERIC, 5, SpellArea.CASTER, 0, 0, "Locate Soul"),
    MOGATO(ClassType.CLERIC, 5, SpellArea.FOE, 0, 0, "Astral Gate"),
    LITOKAN(ClassType.CLERIC, 5, SpellArea.GROUP, 3, 24, "Flame Tower"),
    //
    KAKAMEN(ClassType.CLERIC, 6, SpellArea.GROUP, 18, 38, "Fire Wind"),
    LABADI(ClassType.CLERIC, 6, SpellArea.FOE, 0, 0, "Life Steal"),
    LOKTOFEIT(ClassType.CLERIC, 6, SpellArea.PARTY, 0, 0, "Recall"),
    LORTO(ClassType.CLERIC, 6, SpellArea.GROUP, 6, 36, "Blades"),
    MABADI(ClassType.CLERIC, 6, SpellArea.FOE, 0, 0, "Harming"),
    MADI(ClassType.CLERIC, 6, SpellArea.CHAR, 0, 0, "Healing"),
    //
    BAKADI(ClassType.CLERIC, 7, SpellArea.GROUP, 0, 0, "Death Wind"),
    IHALON(ClassType.CLERIC, 7, SpellArea.CHAR, 0, 0, "Blessed Favor"),
    KADORTO(ClassType.CLERIC, 7, SpellArea.CHAR, 0, 0, "Resurrection"),
    MABARIKO(ClassType.CLERIC, 7, SpellArea.ALL, 18, 58, "Meteor Winds"),
    MALIKTO(ClassType.CLERIC, 7, SpellArea.ALL, 12, 72, "Word of Death");

    private final ClassType type;
    private final int level;
    private final SpellArea area;
    private final int damageMin;
    private final int damageMax;
    private final String desc;
    //private final int iconId;

    private Spells(ClassType type, int level, SpellArea area, int damageMin, int damageMax, String desc) {
        this.type = type;
        this.level = level;
        this.area = area;
        this.damageMin = damageMin;
        this.damageMax = damageMax;
        this.desc = desc;
        //this.iconId = iconId;

    }

//    public int getIconId() {
//        return iconId;
//    }
}

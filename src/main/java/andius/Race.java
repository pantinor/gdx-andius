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
public enum Race {

    HUMAN(8, 8, 5, 8, 8, 9),
    ELF(7, 10, 10, 6, 9, 6),
    DWARF(10, 7, 10, 10, 5, 6),
    GNOME(7, 7, 10, 8, 10, 7),
    HOBBIT(5, 7, 7, 6, 10, 12);

    private final int initialStrength, initialIntell, initialPiety, initialVitality, initialAgility, initialLuck;

    private Race(int initialStrength, int initialIntell, int initialPiety, int initalVitality, int initialAgility, int initialLuck) {
        this.initialStrength = initialStrength;
        this.initialIntell = initialIntell;
        this.initialPiety = initialPiety;
        this.initialVitality = initalVitality;
        this.initialAgility = initialAgility;
        this.initialLuck = initialLuck;
    }

    public int getInitialStrength() {
        return initialStrength;
    }

    public int getInitialIntell() {
        return initialIntell;
    }

    public int getInitialPiety() {
        return initialPiety;
    }

    public int getInitialVitality() {
        return initialVitality;
    }

    public int getInitialAgility() {
        return initialAgility;
    }

    public int getInitialLuck() {
        return initialLuck;
    }

}

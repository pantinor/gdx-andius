package andius.objects;

import java.util.Random;

public class Dice {

    private static final Random random = new Random();

    public int qty;       // how many dice
    public int sides;       // faces per die
    public int bonus;      // plus

    public Dice() {

    }

    public Dice(int qty, int sides) {
        this.qty = qty;
        this.sides = sides;
        this.bonus = 0;
    }

    public Dice(int qty, int sides, int bonus) {
        this.qty = qty;
        this.sides = sides;
        this.bonus = bonus;
    }

    public int max() {
        return qty * sides + bonus;      // each die rolls the maximum
    }

    public int min() {
        return qty + bonus;              // each die rolls 1
    }

    public int roll() {
        int total = bonus;

        if (sides > 0) {
            for (int die = 0; die < qty; die++) {
                total += random.nextInt(sides) + 1;
            }
        }

        return total;
    }

    @Override
    public String toString() {
        if (qty == 0) {
            return "";
        }

        if (bonus == 0) {
            return String.format("%dd%d", qty, sides);
        }

        if (bonus < 0) {
            return String.format("%dd%d%d", qty, sides, bonus);
        }

        return String.format("%dd%d+%d", qty, sides, bonus);
    }
}

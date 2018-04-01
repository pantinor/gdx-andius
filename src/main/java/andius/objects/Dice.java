package andius.objects;

import utils.XORShiftRandom;

public class Dice {
    
    public final static XORShiftRandom rand = new XORShiftRandom();

    private int qty;
    private int sides;
    private int bonus;
    
    public Dice() {
    }
    
    public Dice(int sides, int qty) {
        this.sides = sides;
        this.qty = qty;
    }
    
    public int roll() {
        int roll = 0;
        for (int i=0;i<qty;i++) {
            roll += rand.nextInt(sides) + 1;
        }
        roll += bonus;
        return roll;
    }
    
    public int getMax() {
        int roll = 0;
        for (int i=0;i<qty;i++) {
            roll += sides;
        }
        roll += bonus;
        return roll;
    }

    @Override
    public String toString() {
        if (qty == 0) {
            return "";
        }
        StringBuilder text = new StringBuilder();
        text.append(String.format("%dd%d", qty, sides));
        if (bonus > 0) {
            text.append("+" + bonus);
        }
        return text.toString();
    }
}

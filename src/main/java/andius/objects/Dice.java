package andius.objects;

import utils.XORShiftRandom;

public class Dice {
    
    private final static XORShiftRandom rand = new XORShiftRandom();

    private int qty;
    private int sides;
    private int bonus;
    
    public int roll() {
        int roll = 0;
        for (int i=0;i<qty;i++) {
            roll += rand.nextInt(sides) + 1;
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

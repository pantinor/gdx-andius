package andius.objects;

class Dice {

    int qty;
    int sides;
    int bonus;

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

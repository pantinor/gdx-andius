package andius.objects;

import java.util.List;

public class Reward {

    public final static String[] TYPE = {"gold", "item"};
    private String name;
    private int id;
    private List<RewardElement> elements;
    private Dice goldAmt;

    public class RewardElement {

        private int type;
        private int odds;
        private List<Integer> itemIds;

        public int getType() {
            return type;
        }

        public int getOdds() {
            return odds;
        }

        public List<Integer> getItemIds() {
            return itemIds;
        }

    }

    public Dice getGoldAmt() {
        return goldAmt;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public List<RewardElement> getElements() {
        return elements;
    }

}

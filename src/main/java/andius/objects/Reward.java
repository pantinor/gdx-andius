package andius.objects;

import java.util.List;

public class Reward {

    public final static String[] types = {"gold", "item"};
    private String name;
    private int id;
    private List<RewardElement> elements;

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

    public static String[] getTypes() {
        return types;
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

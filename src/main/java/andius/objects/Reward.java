package andius.objects;

import andius.Andius;
import java.util.List;

public class Reward {

    public int id;
    public boolean isChest;
    public int trapTypeFlags;

    List<RewardDetails> rewardDetails;

    public int getId() {
        return id;
    }

    public boolean isIsChest() {
        return isChest;
    }

    public int getTrapTypeFlags() {
        return trapTypeFlags;
    }

    public int goldAmount() {
        int amt = 0;
        for (RewardDetails d : this.rewardDetails) {
            if (d.goldReward != null) {
                amt += d.goldReward.dice1.roll() * d.goldReward.base * d.goldReward.dice2.roll();
            }
        }
        return amt;
    }

    public List<RewardDetails> getRewardDetails() {
        return rewardDetails;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        for (RewardDetails rd : rewardDetails) {
            sb.append("\n").append("\t").append(rd);
        }

        return String.format("%s\t%s\t%s\t%s\t%s",
                String.format("%-2s", this.id),
                String.format("%-2s", "" + this.isChest),
                String.format("%-2s", "" + this.trapTypeFlags),
                String.format("%-2s", "" + this.goldAmount()),
                sb);
    }

    public class RewardDetails {

        public int odds;
        public int type;
        public GoldReward goldReward; // if type == 0
        public ItemReward itemReward; // if type == 1

        public class GoldReward {

            public Dice dice1, dice2;
            public int base;
        }

        public class ItemReward {

            public int min, size, max, range;

            public int getMin() {
                return range == 0 ? min : min + 0;
            }

            public int getMax() {
                return range == 0 ? min : min + range + 1;
            }

            @Override
            public String toString() {

                return String.format("%s\t%s",
                        String.format("%-2s", Andius.ITEMS.get(min).name),
                        String.format("%-2s", Andius.ITEMS.get(max).name));
            }
        }

        @Override
        public String toString() {

            return String.format("%s\t%s\t%s",
                    String.format("%-2s", "" + odds),
                    String.format("%-2s", "" + type),
                    String.format("%-2s", goldReward != null ? goldReward : itemReward));
        }
    }

}

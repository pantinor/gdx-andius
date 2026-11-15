package andius;

import andius.objects.ClassType;
import andius.objects.DoGooder;
import andius.objects.Item;
import andius.objects.Reward;
import andius.objects.Reward.TrapType;
import andius.objects.SaveGame;
import andius.objects.Spells;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.ScreenUtils;

public class RewardUiSmokeMain extends Game {
    private int frames, rewardId, initialCount;
    private Context context;
    private RewardScreen chest;
    private Wiz4RewardScreen spoils;

    public static void main(String[] args) {
        var cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024; cfg.height = 768; cfg.forceExit = false;
        new LwjglApplication(new RewardUiSmokeMain(), cfg);
    }
    private Object field(Object object, String name) throws Exception {
        var field = object.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }
    private void click(Actor actor) {
        Stage stage = actor.getStage();
        stage.draw();
        Vector2 point = stage.stageToScreenCoordinates(actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2)));
        stage.touchDown(Math.round(point.x), Math.round(point.y), 0, 0);
        stage.touchUp(Math.round(point.x), Math.round(point.y), 0, 0);
    }
    @SuppressWarnings("unchecked")
    private void action(String choice) throws Exception { ((List<String>) field(chest, "action")).setSelected(choice); }
    private void snapshot(String name) {
        Pixmap image = ScreenUtils.getFrameBufferPixmap(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        PixmapIO.writePNG(Gdx.files.local("target/" + name + ".png"), image, -1, true);
        image.dispose();
    }
    @Override public void create() {
        try {
            new Andius().create();
            context = new Context();
            SaveGame save = new SaveGame();
            save.players = new SaveGame.CharacterRecord[6];
            for (int i = 0; i < 6; i++) {
                var player = SaveGame.generatePlayer(10, ClassType.BISHOP, "Alexandria " + (i + 1));

                player.gold = 0; player.inventory.clear(); player.agility = 30;
                player.knownSpells.add(Spells.CALFO);
                java.util.Arrays.fill(player.clericPoints, 9);
                save.players[i] = player;
            }
            context.setSaveGame(save); Andius.CTX = context;
            Reward fixture = new com.google.gson.Gson().fromJson("""
                {"isChest":true,"trapTypeFlags":2,"rewardDetails":[
                  {"goldReward":{"base":600,"dice1":{"qty":0,"bonus":1},"dice2":{"qty":0,"bonus":1}}},
                  {"odds":100,"itemReward":{"min":1,"range":0}}]}
                """, Reward.class);
            rewardId = Constants.Map.BRITAIN.scenario().rewards().size();
            Constants.Map.BRITAIN.scenario().rewards().add(fixture);
            chest = new RewardScreen(context, Constants.Map.BRITAIN, rewardId);
            chest.rand = new java.util.Random() { @Override public int nextInt(int bound) { return 0; } };
            setScreen(chest);
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
    @Override @SuppressWarnings("unchecked") public void render() {
        try {
            super.render(); frames++;
            if (frames == 10) { snapshot("reward-redesign"); action("INSPECT"); }
            if (frames == 11) {
                click((Actor) field(chest, "go"));
                if (((java.util.List<?>) field(chest, "whoTried")).size() != 1) throw new AssertionError("Inspection failed");
                action("DISARM");
                ((SelectBox<TrapType>) field(chest, "trapSelection")).setSelected(TrapType.POISON_NEEDLE);
            }
            if (frames == 12) {
                click((Actor) field(chest, "go"));
                if (!(boolean) field(chest, "chestDisarmed")) throw new AssertionError("Disarm failed");
                action("OPEN");
            }
            if (frames == 13) {
                click((Actor) field(chest, "go"));
                for (var player : context.players()) if (player.gold != 100) throw new AssertionError("Gold distribution changed");
                if (context.players()[0].inventory.size() != 1) throw new AssertionError("Item distribution failed");
                ((Actor) field(chest, "go")).fire(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent());
                if (context.players()[0].gold != 100 || context.players()[0].inventory.size() != 1)
                    throw new AssertionError("Chest can be collected twice");
            }
            if (frames == 20) {
                snapshot("reward-opened");
                chest = new RewardScreen(context, Constants.Map.BRITAIN, rewardId);
                setScreen(chest); action("CALFO");
            }
            if (frames == 21) {
                int before = context.players()[0].clericPoints[Spells.CALFO.getLevel() - 1];
                click((Actor) field(chest, "go"));
                if (!(boolean) field(chest, "chestDisarmed") || context.players()[0].clericPoints[Spells.CALFO.getLevel() - 1] != before - 1)
                    throw new AssertionError("Calfo failed");
            }
            if (frames == 30) {
                DoGooder opponent = new DoGooder(); opponent.name = "Company of the Silver Road";
                opponent.partyMembers = java.util.stream.IntStream.range(0, WizardryData.WER4_CHARS.size())
                        .filter(i -> WizardryData.WER4_CHARS.get(i).items != null && WizardryData.WER4_CHARS.get(i).items.length > 0).limit(6).toArray();
                spoils = new Wiz4RewardScreen(context.players()[0], opponent);
                setScreen(spoils);
                initialCount = ((List<?>) field(spoils, "items")).getItems().size;
                if (initialCount == 0) throw new AssertionError("Fixture needs loot");
            }
            if (frames == 40) {
                snapshot("werdna-reward-redesign");
                var list = (List<?>) field(spoils, "items");
                Item template = (Item) field(list.getSelected(), "item");
                click((Actor) field(spoils, "take"));
                if (list.getItems().size != initialCount - 1 || context.players()[0].inventory.size() != 2
                        || context.players()[0].inventory.get(1) == template)
                    throw new AssertionError("Taking loot must update selection and clone the template");
                while (!list.getItems().isEmpty()) click((Actor) field(spoils, "take"));
            }
            if (frames == 41) {
                if (!((TextButton) field(spoils, "take")).isDisabled() || context.players()[0].inventory.size() != initialCount + 1)
                    throw new AssertionError("Empty loot state failed");
                Gdx.graphics.setWindowedMode(1280, 800);
            }
            if (frames == 55) {
                snapshot("werdna-reward-collected");
                System.out.println("PASS: chest inspection/disarm/Calfo/gold/items/no duplicate collection; Werdna loot selection/taking/template isolation/empty state; resize");
                Gdx.app.exit();
            }
        } catch (Throwable failure) { failure.printStackTrace(); System.exit(1); }
    }
}

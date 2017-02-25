
import andius.Andius;
import static andius.Andius.ITEMS_MAP;
import andius.CombatScreen;
import andius.Constants;
import static andius.Constants.CLASSPTH_RSLVR;
import andius.Constants.Role;
import andius.Constants.SpellTarget;
import andius.Context;
import andius.objects.Actor;
import andius.objects.Item;
import andius.objects.Monster;
import andius.objects.MutableMonster;
import andius.objects.Spells;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class CombatTestMain extends Game {

    public static void main(String[] args) {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "CombatTestMain";
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new CombatTestMain(), cfg);
    }

    @Override
    public void create() {

        try {

            Andius a = new Andius();
            a.create();

            Context ctx = new Context();

            for (int j = 0; j < 6; j++) {
//                ctx.saveGame.players[j].level = 1;
//                ctx.saveGame.players[j].hp = 12;
//                ctx.saveGame.players[j].maxhp = 12;
//                ctx.saveGame.players[j].gold = 3000;
//
//                ctx.saveGame.players[j].exp = 12000;
                ctx.saveGame.players[j].magePoints = new int[]{5, 5, 5, 5, 5, 5, 5};
                ctx.saveGame.players[j].clericPoints = new int[]{5, 5, 5, 5, 5, 5, 5};
                for (Spells s : Spells.values()) {
                    if (s.getTarget() == SpellTarget.MONSTER) {
                        ctx.saveGame.players[j].knownSpells.add(s);
                    }
                }
//
//                ctx.saveGame.players[j].str = 8;
//                ctx.saveGame.players[j].intell = 8;
//                ctx.saveGame.players[j].piety = 8;
//                ctx.saveGame.players[j].vitality = 8;
//                ctx.saveGame.players[j].agility = 8;
//                ctx.saveGame.players[j].luck = 8;

                ctx.saveGame.players[j].armor = ITEMS_MAP.get("CHAIN MAIL +1").clone();
                ctx.saveGame.players[j].weapon = ITEMS_MAP.get("MACE +1").clone();
                ctx.saveGame.players[j].helm = ITEMS_MAP.get("HELM").clone();
                ctx.saveGame.players[j].shield = ITEMS_MAP.get("LARGE SHIELD").clone();
                ctx.saveGame.players[j].glove = ITEMS_MAP.get("SILVER GLOVES").clone();
                ctx.saveGame.players[j].item1 = ITEMS_MAP.get("ROD OF FLAME").clone();
                ctx.saveGame.players[j].item2 = ITEMS_MAP.get("WERDNAS AMULET").clone();

            }

            TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
            TiledMap tm = loader.load("assets/data/combat1.tmx");

            Monster monster = Andius.MONSTER_LEVELS.get(2).get(1);
            Actor actor = new Actor(monster.icon, monster.name);
            MutableMonster mm = new MutableMonster(monster);
            mm.name = monster.name;
            mm.setIcon(monster.icon);
            actor.set(mm, Role.MONSTER, 1, 1, 1, 1, Constants.MovementBehavior.ATTACK_AVATAR);
            CombatScreen cs = new CombatScreen(ctx, Constants.Map.WIWOLD, tm, actor);
            setScreen(cs);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

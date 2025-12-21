package andius.objects;

import com.badlogic.gdx.graphics.g3d.Model;
import utils.ObjLoader;

public enum MonsterModels {

    //begin default wizadry icon ids
    SLIME("assets/graphics/monsters/blob.obj", "blob"),
    SMALL_HUMANOID("assets/graphics/monsters/kobold.obj", "kobold"),
    SKELETON("assets/graphics/monsters/skeleton.obj", "skeleton"),
    LEATHER_MAN("assets/graphics/monsters/thief.obj", "thief"),
    ARMOR_MAN("assets/graphics/monsters/armored-man.obj", "armored-man"),
    WEIRD_HUMANOID("assets/graphics/monsters/humanoid.obj", "humanoid"),//5
    GAS_CLOUD("assets/graphics/monsters/elemental.obj", "elemental"),
    ROBES_MAN("assets/graphics/monsters/wizard.obj", "wizard"),
    PRIEST("assets/graphics/monsters/priest.obj", "priest"),
    KIMONO_MAN("assets/graphics/monsters/ninja.obj", "ninja"),
    BEAR("assets/graphics/monsters/bear.obj", "bear"),//10
    RODENT("assets/graphics/monsters/rat.obj", "rat"),
    AMPHIBIAN("assets/graphics/monsters/toad.obj", "toad"),
    FLY("assets/graphics/monsters/wasp.obj", "wasp"),
    INSECT("assets/graphics/monsters/spider.obj", "spider"),
    DRAGON("assets/graphics/monsters/dragon.obj", "dragon"),//15
    LARGE_HUMANOID("assets/graphics/monsters/ogre.obj", "ogre"),
    DEMON("assets/graphics/monsters/demon.obj", "demon"),
    CHEST("assets/graphics/chest.obj", "chest"),
    COINS("assets/graphics/chest.obj", "chest"),
    //end of default wizardry icon id range

    //extra icon ids below
    ORC("assets/graphics/monsters/orc.obj", "orc"),//20
    PLANT("assets/graphics/monsters/plant.obj", "plant"),
    WARLOCK("assets/graphics/monsters/warlock1.obj", "warlock1"),
    DARK_WIZARD("assets/graphics/monsters/dark-wizard.obj", "dark-wizard"),
    WARRIOR("assets/graphics/monsters/warrior.obj", "warrior"),
    GHOUL("assets/graphics/monsters/ghoul.obj", "ghoul"),//25
    HARPY("assets/graphics/monsters/harpy.obj", "harpy"),
    VIKING("assets/graphics/monsters/viking.obj", "viking");

    private Model model;
    private final String path, name;

    private MonsterModels(String path, String name) {
        this.name = name;
        this.path = path;
    }

    public Model model() {
        if (this.model == null) {
            this.model = ObjLoader.loadModel(path, name, .1f);
        }
        return this.model;
    }

}

package andius.objects;

import com.badlogic.gdx.graphics.g3d.Model;
import utils.ObjLoader;

public enum MonsterModels {

    //begin default wizadry icon ids
    SLIME_SLUG("assets/graphics/monsters/blob.obj", "slug"),
    SMALL_HUMANOID("assets/graphics/monsters/kobold.obj", "kobold"),//small humanoid creatures
    SKELETON("assets/graphics/monsters/skeleton.obj", "skeleton"),//undead skeleton
    LEATHER_MAN("assets/graphics/monsters/thief.obj", "thief"),//rogues
    ARMOR_MAN("assets/graphics/monsters/armored-man.obj", "armored-man"),//man in armor with shield and axe, guardsman
    WEIRD_HUMANOID("assets/graphics/monsters/humanoid.obj", "humanoid"),//5 mummy zombie etc undead
    GAS_CLOUD("assets/graphics/monsters/elemental.obj", "elemental"),
    ROBES_MAN("assets/graphics/monsters/wizard.obj", "wizard"), //any mage magic users
    PRIEST("assets/graphics/monsters/priest.obj", "priest"), //any priests low level
    NINJA("assets/graphics/monsters/ninja.obj", "ninja"),
    BEAR("assets/graphics/monsters/bear.obj", "bear"),//10 
    RODENT("assets/graphics/monsters/rat.obj", "rat"),
    AMPHIBIAN("assets/graphics/monsters/toad.obj", "toad"),
    FLY("assets/graphics/monsters/wasp.obj", "wasp"), //winged insects
    INSECT("assets/graphics/monsters/spider.obj", "spider"),
    DRAGON("assets/graphics/monsters/dragon.obj", "dragon"),//15 large dragons
    LARGE_HUMANOID("assets/graphics/monsters/ogre.obj", "ogre"),//giants, ogres
    DEMON("assets/graphics/monsters/demon.obj", "demon"),//devils demons
    CHEST("assets/graphics/chest.obj", "chest"),
    COINS("assets/graphics/chest.obj", "chest"),
    //end of default wizardry icon id range

    //extra icon ids below
    ORC("assets/graphics/monsters/orc.obj", "orc"),//20
    PLANT("assets/graphics/monsters/plant.obj", "plant"),//any plant like things
    WARLOCK("assets/graphics/monsters/warlock1.obj", "warlock1"),//bishop priest warlock higher level
    DARK_WIZARD("assets/graphics/monsters/dark-wizard.obj", "dark-wizard"), //special evil wizard higher level than regular wizard
    WARRIOR("assets/graphics/monsters/warrior.obj", "warrior"),//leader warrior etc
    GHOUL("assets/graphics/monsters/ghoul.obj", "ghoul"),//25  //undead ghoul or ghast
    HARPY("assets/graphics/monsters/harpy.obj", "harpy"),
    WOLF("assets/graphics/monsters/wolf.obj", "wolf"), //canine dog coyote wolf
    VIKING("assets/graphics/monsters/viking.obj", "viking"), //a viking with a mace dwarf warriors
    SNAKE("assets/graphics/monsters/snake.obj", "snake"),
    TIGER("assets/graphics/monsters/tiger.obj", "tiger"), //30 felines
    GOBLIN("assets/graphics/monsters/goblin.obj", "goblin"), //goblins
    SAMURAI("assets/graphics/monsters/samurai.obj", "samurai"), //samurai or ronin
    VAMPIRE("assets/graphics/monsters/vampire.obj", "vampire"),
    WYVERN("assets/graphics/monsters/wyvern.obj", "wyvern");//any small baby dragons

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

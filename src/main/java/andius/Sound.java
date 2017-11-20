package andius;

public enum Sound {

    BLOCKED("blocked.ogg", false, 0.3f),
    FLEE("flee.ogg", false, 0.3f),
    ERROR("error.ogg", false, 0.3f),
    TRIGGER("trigger.ogg", false, 0.3f),
    PC_ATTACK("pc_attack.ogg", false, 0.3f),
    PC_STRUCK("pc_struck.ogg", false, 0.3f),
    NPC_ATTACK("npc_attack.ogg", false, 0.3f),
    NPC_STRUCK("npc_struck.ogg", false, 0.3f),
    EVADE("evade.ogg", false, 0.3f),
    POISON_EFFECT("Poison.ogg", false, 0.3f),
    SLEEP("Hypnosis.ogg", false, 0.3f),
    BOOM("boom.ogg", false, 0.3f),
    EXPLOSION("Explosion.ogg", false, 0.3f),
    CROSSBOW("crossbow.ogg", false, 0.3f),
    GAZE("Gaze.ogg", false, 0.3f),
    HEALING("HealingMini.ogg", false, 0.3f),
    POSITIVE_EFFECT("PositiveEffect.ogg", false, 0.3f),
    NEGATIVE_EFFECT("NegativeEffect.ogg", false, 0.3f),
    STEAL_ESSENCE("StealEssence.ogg", false, 0.3f),
    TREMOR("Armageddon.ogg", false, 0.3f),
    ACID("AcidicRain.ogg", false, 0.3f),
    LIGHTNING("LightningBolt.ogg", false, 0.3f),
    FIREBALL("Fireball.ogg", false, 0.3f),
    ROCKS("StoneRain.ogg", false, 0.3f),
    WIND("tornado.ogg", false, 0.3f),
    CANNON("cannon.mp3", false, 0.3f),
    SPIRITS("AncientSpirits.ogg", false, 0.3f),
    RAGE("RageOfGod.ogg", false, 0.3f),
    DIVINE_INTERVENTION("divineint.ogg", false, 0.3f),
    DIVINE_MEDITATION("DivineMed.ogg", false, 0.3f),
    WAVE("chaowave.ogg", false, 0.3f),
    GIGGLE("giggle.mp3", false, 0.3f),
    POWER_CHAINS("PowerChains.ogg", false, 0.3f),
    MAGIC("magic.ogg", false, 0.3f),
    INFERNO("Inferno.ogg", false, 0.3f),
    MEDITATION("Meditation.ogg", false, 0.3f),
    FLAME_WAVE("flamewave.ogg", false, 0.3f),
    WEAKNESS("weakness.ogg", false, 0.3f),;

    String file;
    boolean looping;
    float volume;

    private Sound(String name, boolean looping, float volume) {
        this.file = name;
        this.looping = looping;
        this.volume = volume;
    }

    public String getFile() {
        return this.file;
    }

    public boolean getLooping() {
        return this.looping;
    }

    public float getVolume() {
        return this.volume;
    }

}

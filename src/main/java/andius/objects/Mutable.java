package andius.objects;

import andius.Constants.Breath;
import andius.Constants.CharacterType;
import java.util.List;

public interface Mutable {

    public String name();

    public String icon();

    public int getArmourClass();

    public int getLevel();

    public Object baseType();

    public CharacterType getType();

    public List<Dice> getDamage();
    
    public int hitModifier();

    public int getCurrentHitPoints();

    public void setCurrentHitPoints(int currentHitPoints);

    public int getCurrentMageSpellLevel();

    public int getCurrentPriestSpellLevel();

    public Breath breath();

    public int getUnaffected();

    public State status();

    public void processStatusAffects();

    public int getMaxHitPoints();

    public MonsterCursor getMonsterCursor();

    public void setMonsterCursor(MonsterCursor monsterCursor);

    public void adjustHealthBar();

    public String getDamageTag();

    public void setACModifier(int acmodifier);

    public int getACModifier();

    public Spells castMageSpell();

    public Spells castPriestSpell();

}

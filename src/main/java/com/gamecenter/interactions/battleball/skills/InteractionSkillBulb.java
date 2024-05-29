package com.gamecenter.interactions.battleball.skills;

import com.eu.habbo.habbohotel.items.Item;
import com.gamecenter.games.battleball.skils.SkillsType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionSkillBulb extends InteractionSkill{
    public InteractionSkillBulb(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.skillType = SkillsType.BULB;
    }

    public InteractionSkillBulb(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.skillType = SkillsType.BULB;
    }
}

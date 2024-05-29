package com.gamecenter.utils;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;

import ch.qos.logback.core.boolex.Matcher;
import gnu.trove.map.hash.THashMap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;


public class UtilityMethods {
    public static Habbo getUserWithSSO(String sso) {
        Iterator iterator = Emulator.getGameServer().getGameClientManager().getSessions().values().iterator();

        GameClient client;
        do {
            if (!iterator.hasNext())
                return null;

            client = (GameClient)iterator.next();
        } while(client.getHabbo() == null || !client.getHabbo().getHabboInfo().getSso().equals(sso));

        return client.getHabbo();
    }

    public static String GenerateRandomNumber(int charLength) {
        return String.valueOf(charLength < 1 ? 0 : new Random()
                .nextInt((9 * (int) Math.pow(10, charLength - 1)) - 1)
                + (int) Math.pow(10, charLength - 1));
    }

    public static void sendBubble(Habbo habbo, String message){
        THashMap<String, String> keys = new THashMap<>();
        keys.put("display", "BUBBLE");
        keys.put("message", message);
        habbo.getClient().sendResponse(new BubbleAlertComposer(BubbleAlertKeys.RECEIVED_BADGE.key, keys));
    }

    public static Set<String> getUserMentionedFromChat(String chat) {
        Set<String> Mentioned = new HashSet<>();
        Pattern compiledPattern = Pattern.compile("@(\\w+)");
        java.util.regex.Matcher matcher = compiledPattern.matcher(chat);
        while (matcher.find()) {
            if (Mentioned.size() < 5)
                Mentioned.add(matcher.group(1));
        }
        return Mentioned;
    }
}

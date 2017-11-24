package fun.rubicon.listener;

import fun.rubicon.RubiconBot;
import fun.rubicon.core.Main;
import fun.rubicon.util.Logger;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * Rubicon Discord bot
 *
 * @author Yannick Seeger / ForYaSee
 * @copyright RubiconBot Dev Team 2017
 * @license MIT License <http://rubicon.fun/license>
 * @package fun.rubicon.listener
 */

/**
 * Listener if the RubiconBot leaves a guild
 */
public class BotLeaveListener extends ListenerAdapter {

    /**
     * Removes the guild from the database
     * @param e
     */
    @Override
    public void onGuildLeave(GuildLeaveEvent e) {
        try {
            if(RubiconBot.getMySQL().ifGuildExits(e.getGuild())) {
                RubiconBot.getMySQL().deleteGuild(e.getGuild());
            }
        } catch (Exception ex) {
            Logger.error(ex);
        }
    }
}



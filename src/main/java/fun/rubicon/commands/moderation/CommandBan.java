package fun.rubicon.commands.moderation;

import fun.rubicon.RubiconBot;
import fun.rubicon.command.CommandCategory;
import fun.rubicon.command.CommandHandler;
import fun.rubicon.command.CommandManager;
import fun.rubicon.core.entities.RubiconMember;
import fun.rubicon.core.entities.RubiconUser;
import fun.rubicon.features.PunishmentHandler;
import fun.rubicon.permission.PermissionRequirements;
import fun.rubicon.permission.UserPermissions;
import fun.rubicon.util.*;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;

import java.util.Arrays;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class CommandBan extends CommandHandler implements PunishmentHandler {
    public CommandBan() {
        super(new String[] {"ban", "tempban"}, CommandCategory.MODERATION, new PermissionRequirements("ban", false, false), "Easily (temp)ban your members YEAHHHHHH WHOOOOO BANNING PEROPLE IS NICE", "<@User> [time]");
    }

    @Override
    protected Message execute(CommandManager.ParsedCommandInvocation invocation, UserPermissions userPermissions) {
        String[] args = invocation.getArgs();
        Message message = invocation.getMessage();
        Member member = invocation.getMember();
        Guild guild = invocation.getGuild();

        if(args.length == 0)
            return createHelpMessage();
        if(args[0].equals("settings"))
            return new MessageBuilder().setEmbed(EmbedUtil.info("Work in progress", "This feature is still work in progress").build()).build();
        if(message.getMentionedMembers().isEmpty())
            return new MessageBuilder().setEmbed(EmbedUtil.error(invocation.translate("command.ban.unknowuser.title"), invocation.translate("command.ban.unknownuser.description")).build()).build();
        RubiconMember victim = RubiconMember.fromMember(message.getMentionedMembers().get(0));
        Member victimMember = victim.getMember();
        if(victimMember.equals(guild.getSelfMember()) || Arrays.asList(Info.BOT_AUTHOR_IDS).contains(victimMember.getUser().getIdLong()))
            return new MessageBuilder().setEmbed(EmbedUtil.error(invocation.translate("command.ban.donotbanrubicon.title"), invocation.translate("command.ban.donotbanrubicon.description")).build()).build();
        if(!member.canInteract(victimMember) && !Arrays.asList(Info.BOT_AUTHOR_IDS).contains(victimMember.getUser().getIdLong()))
            return new MessageBuilder().setEmbed(EmbedUtil.error(invocation.translate("command.ban.nopermissions.user.title"), invocation.translate("command.ban.nopermissions.user.description")).build()).build();
        if(!invocation.getSelfMember().canInteract(victimMember))
            return new MessageBuilder().setEmbed(EmbedUtil.error(invocation.translate("command.ban.nopermissions.bot.title"), invocation.translate("command.ban.nopermissions.bot.description")).build()).build();
        if(args.length == 1){
            if(!new PermissionRequirements("ban.permanent", false, false).coveredBy(invocation.getPerms()))
                return new MessageBuilder().setEmbed(EmbedUtil.error(invocation.translate("command.ban.nopermissions.user.title"), invocation.translate("command.ban.permanent.noperms.descriptio")).build()).build();
            victim.ban();
            return new MessageBuilder().setEmbed(EmbedUtil.success(invocation.translate("command.ban.banned.permanent.title"), String.format(invocation.translate("command.ban.banned.permanent.description"), victimMember.getAsMention())).build()).build();
        } else if (args.length > 1){
            Date expiry = StringUtil.parseDate(args[1]);
            if(expiry == null)
                return new MessageBuilder().setEmbed(EmbedUtil.error(invocation.translate("general.punishment.invalidnumber.title"), invocation.translate("general.punishment.invalidnumber.description")).build()).build();
            victim.ban(expiry);
            return new MessageBuilder().setEmbed(EmbedUtil.success(invocation.translate("command.ban.banned.temporary.title"), invocation.translate("command.ban.banned.temporary.permanent").replace("%mention%", victimMember.getAsMention()).replace("%date%", DateUtil.formatDate(expiry, invocation.translate("date.format")))).build()).build();
        }

        return createHelpMessage();
    }

    @Override
    public void loadPunishments() {
        try{
            PreparedStatement ps = RubiconBot.getMySQL().getConnection().prepareStatement("SELECT serverid, userid, expiry FROM punishments WHERE type='ban'");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                RubiconUser user = RubiconUser.fromUser(RubiconBot.getShardManager().getUserById(rs.getLong("userid")));
                long guildid = rs.getLong("serverid");
                if(rs.getLong("expiry") == 0L) return;
                if(new Date(rs.getLong("expiry")).before(new Date())) user.unban(RubiconBot.getShardManager().getGuildById(guildid));
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        user.unban(RubiconBot.getShardManager().getGuildById(guildid));
                    }
                }, new Date(rs.getLong("expiry")));
            }
        } catch (SQLException e){
            Logger.error(e);
        }
    }
}
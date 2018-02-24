package fun.rubicon.commands.general;

import fun.rubicon.RubiconBot;
import fun.rubicon.command.CommandCategory;
import fun.rubicon.command.CommandHandler;
import fun.rubicon.command.CommandManager;
import fun.rubicon.permission.PermissionRequirements;
import fun.rubicon.permission.UserPermissions;
import fun.rubicon.util.Info;
import fun.rubicon.util.SafeMessage;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 * Rubicon Discord bot
 *
 * @author Leon Kappes / Lee
 * @copyright Rubicon Dev Team 2018
 * @license MIT License <http://rubicon.fun/license>
 * @package fun.rubicon.commands.general
 */
public class CommandGitBug extends CommandHandler {
    private static final HashMap<TextChannel, Titel> CHANNEL_MSG = new HashMap<>();
    private static final Timer TIMER = new Timer();

    private static final String ISSUE_HEADER = "<p><strong>Bugreport</strong><br><br><strong>Bug report by ";
    private static final String ISSUE_SUFFIX = " </strong><br><br><strong>Description</strong><br><br></p>";


    public CommandGitBug() {
        super(new String[]{"bug", "bugreport"}, CommandCategory.GENERAL, new PermissionRequirements("command.gitbug", false, true), "Report an Bug", "<Bug title>");
    }


    @Override
    protected Message execute(CommandManager.ParsedCommandInvocation parsedCommandInvocation, UserPermissions userPermissions) {
        String title = parsedCommandInvocation.getMessage().getContentDisplay().replace(parsedCommandInvocation.getPrefix() + parsedCommandInvocation.getCommandInvocation(), "");
        Titel tite1 = new Titel(title, parsedCommandInvocation.getAuthor(), parsedCommandInvocation.getTextChannel(), parsedCommandInvocation.getMessage().getContentDisplay());
        CHANNEL_MSG.put(parsedCommandInvocation.getTextChannel(), tite1);
        SafeMessage.sendMessage(parsedCommandInvocation.getTextChannel(), new EmbedBuilder().setTitle("Set Bug Description").setDescription("Please write a short Description about the Bug in this Channel").setFooter("Will abort in 30sec.", null).build(), 30);
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                CHANNEL_MSG.remove(parsedCommandInvocation.getTextChannel());
                parsedCommandInvocation.getTextChannel().sendMessage("Setup abort").queue(message -> {
                    message.delete().queueAfter(7L, TimeUnit.SECONDS);
                });
            }
        }, 30000);
        return null;
    }

    public static void handle(MessageReceivedEvent event) {
        if (!CHANNEL_MSG.containsKey(event.getTextChannel()))
            return;
        Titel titel = CHANNEL_MSG.get(event.getTextChannel());
        if (event.getMessage().getContentDisplay().equals(titel.getMessage()))
            return;
        if (event.getAuthor().equals(RubiconBot.getJDA().getSelfUser()))
            return;
        if (!event.getAuthor().equals(titel.getAuthor()))
            return;
        try {
            GitHub gitHub = GitHub.connectUsingOAuth(Info.GITHUB_TOKEN);
            GHRepository repository = gitHub.getOrganization("Rubicon-Bot").getRepository("Rubicon");
            GHIssue Issue = repository.createIssue(titel.getTitle()).body(ISSUE_HEADER + event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + ISSUE_SUFFIX + event.getMessage().getContentDisplay()).label("Bug").label("Requires Testing").create();
            CHANNEL_MSG.remove(event.getTextChannel());
            event.getMessage().delete().queue();
            SafeMessage.sendMessage(event.getTextChannel(), new EmbedBuilder().setTitle("Bug successfully send!").setDescription("Bug is available at: " + Issue.getHtmlUrl()).build());
            TIMER.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Titel {
        private final String title;
        private final User author;
        private final TextChannel channel;
        private final String message;


        private Titel(String title, User author, TextChannel channel, String message) {
            this.title = title;
            this.author = author;
            this.channel = channel;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public String getTitle() {
            return title;
        }

        public User getAuthor() {
            return author;
        }

        public TextChannel getChannel() {
            return channel;
        }
    }

}


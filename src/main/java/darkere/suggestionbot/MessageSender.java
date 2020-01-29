package darkere.suggestionbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MessageSender {
    static void callOutExistingSuggestion(SuggestionPool pool, Long l, Suggestion s, String id) {
        MessageBuilder builder = new MessageBuilder();
        builder.append(s.url);
        builder.append('\n');
        builder.append("has already been suggested here: ");
        builder.append('\n');
        builder.append(s.messageLink);
        builder.append('\n');
        builder.append("Link to Vote: ");
        builder.append("https://discordapp.com/channels/");
        builder.append(pool.serverID);
        builder.append("/");
        builder.append(pool.getSuggestionChannel().getId());
        builder.append("/");
        builder.append(l);
        Message message = builder.build();
        Objects.requireNonNull(SuggestionsBot.jda.getTextChannelById(id)).sendMessage(message).queue();
    }

    static void askForReevaluation(SuggestionPool pool, Suggestion s, String id) {
        MessageBuilder builder = new MessageBuilder();
        builder.append(s.url);
        builder.append('\n');
        builder.append("Has been suggested before with the result: ").append(s.result);
        builder.append('\n');
        builder.append("Suggested here: <").append(s.messageLink).append(">");
        builder.append('\n');
        builder.append("Do you want to reevaluate the suggestion?");
        CompletableFuture<Message> future = Objects.requireNonNull(SuggestionsBot.jda.getTextChannelById(id)).sendMessage(builder.build()).submit();
        future.thenAccept((message) -> {
            ReactionHandler.addEditingReactions(message);
            pool.suggestionsToReevaluate.put(message.getIdLong(), s);
        });
    }

    static void writeSuggestionToChannel(SuggestionPool pool, Suggestion suggestion) {

        MessageAction action = pool.getSuggestionChannel().sendMessage(buildEmbedForSuggestion(suggestion));
        CompletableFuture<Message> future = action.submit();
        future.thenAccept((message) -> {
            pool.currentSuggestions.put(message.getIdLong(), suggestion);
            pool.allSuggestions.put(message.getIdLong(), suggestion);
            FileHandler.writeSuggestionToFile(pool, message.getId(), suggestion);
            ReactionHandler.addVoteReactions(message);
            if (pool.isInEditMode) {
                ReactionHandler.addEditingReactions(message);
            }
        });


    }

    public static MessageEmbed buildEmbedForSuggestion(Suggestion suggestion) {
        EmbedBuilder builder = new EmbedBuilder();
        String name = suggestion.url;
        if (name.contains("https://www.curseforge.com/minecraft/mc-mods/")) {
            name = name.replace("https://www.curseforge.com/minecraft/mc-mods/", "");
            name = name.replace('-', ' ');
            name = name.toUpperCase();
            builder.setTitle(name, suggestion.url);
        } else {
            builder.setTitle(name);
        }

        if (suggestion.result.equals("TBD")) {
            builder.addField(new String(Character.toChars(0x1f44d)), Integer.toString(suggestion.yesVote), true);
            builder.addField(new String(Character.toChars(0x1f44e)), Integer.toString(suggestion.noVote), true);
            builder.addField(new String(Character.toChars(0x1f937)), Integer.toString(suggestion.dontCare), true);
        } else {
            builder.addField(suggestion.result + " " +
                            (suggestion.result.equals("Accepted") ? new String(Character.toChars(0x2705)) :
                                    new String(Character.toChars(0x274c)))
                    , "Pending", false);
        }

        builder.setDescription("[Link to Message](" + suggestion.messageLink + ")");
        builder.setFooter("suggested by " + SuggestionsBot.jda.getUserById(suggestion.authorID).getName());
        return builder.build();
    }

    public static void updateSuggestionMessage(SuggestionPool pool, long messageID) {
        pool.getSuggestionChannel().editMessageById(messageID, buildEmbedForSuggestion(pool.currentSuggestions.get(messageID))).queue();
    }

    public static void removeMessagesInBulk(SuggestionPool pool, List<Long> ids) {
        List<String> list = new ArrayList<>();
        for (Long id : ids) {
            list.add(Long.toString(id));
        }
        if (list.size() == 1) {
            pool.getSuggestionChannel().deleteMessageById(list.get(0)).queue();
        } else {
            pool.getSuggestionChannel().deleteMessagesByIds(list).queue();
        }

    }

    public static void sendFileMessage(SuggestionPool pool, List<Suggestion> acc, List<Suggestion> deny) {
        File Accepted = FileHandler.writeFileForSending(acc, "Accepted.csv");
        File Denied = FileHandler.writeFileForSending(deny, "Rejected.csv");
        MessageBuilder builder = new MessageBuilder();
        builder.append("Finished Editing!");
        builder.append("\n");
        builder.append("These mods have been Accepted/Rejected");
        MessageAction action = pool.getCommandChannel().sendMessage(builder.build());
        if (Accepted != null) action = action.addFile(Accepted);
        if (Denied != null) action = action.addFile(Denied);
        action.queue();
    }
}

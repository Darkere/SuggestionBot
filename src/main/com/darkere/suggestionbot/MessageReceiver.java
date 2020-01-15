package darkere.suggestionbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.*;

public class MessageReceiver extends ListenerAdapter {

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        String rawMessage = event.getMessage().getContentRaw();
        if (!rawMessage.startsWith("%")) return;
        String authorID = event.getAuthor().getId();
        String serverID = event.getGuild().getId();
        String channelID = event.getChannel().getId();
        String messageID = event.getMessageId();
        SuggestionPool pool = SuggestionHandler.getPool(channelID);
        if (rawMessage.startsWith("%testreaction")) {
            ReactionHandler.addVoteReactions(event.getMessage());
        }
        if (rawMessage.startsWith("%newpool")) {
            String[] args = rawMessage.substring(8).split(" ");
            SuggestionHandler.newPool(serverID, channelID, args);
        }
        if (rawMessage.startsWith("%pools")) {
            StringBuilder poolnames = new StringBuilder();
            for (SuggestionPool pools : SuggestionHandler.getAllPools().values()) {
                poolnames.append(pools.name).append(", ");
            }
            Objects.requireNonNull(SuggestionsBot.jda.getTextChannelById(channelID)).sendMessage("PoolNames:" + poolnames.toString()).queue();
        }

        reactToCommand(rawMessage, messageID, authorID, serverID, channelID, pool, event);


    }

    private void reactToCommand(String entirecommand, String messageID, String authorID, String serverID, String channelID, SuggestionPool pool, MessageReceivedEvent event) {
        if (pool == null) return;
        String[] commandArray = entirecommand.split("[ \n]", 2);
        String command = commandArray[0];
        command = command.replaceFirst("%", "");
        String content = "";
        if (commandArray.length > 1) {
            content = commandArray[1];
        }

        switch (command) {
            case "sugg": {
                String[] array = content.split("[ \n]");
                List<String> list = Arrays.asList(array);
                list.forEach((message) -> SuggestionHandler.createNewSuggestion(pool, message, authorID, serverID, channelID, messageID));
                break;
            }
            case "editmode": {
                SuggestionHandler.createEditingOptions(pool);
                SuggestionHandler.addEditor(pool, authorID);
                pool.addCommandToDelete(Long.parseLong(messageID));
                break;
            }
            case "deletequeue": {
                MessageSender.removeMessagesInBulk(pool, pool.pendingSuggestions);
                MessageSender.removeMessagesInBulk(pool, pool.commandsToDelete);
                break;
            }
            case "removeeditor": {
                SuggestionHandler.addEditor(pool, authorID);
                pool.addCommandToDelete(Long.parseLong(messageID));
                break;
            }
            case "finishedit": {
                pool.addCommandToDelete(Long.parseLong(messageID));
                SuggestionHandler.finishEditing(pool, authorID);
                break;
            }
            case "refreshreactions": {
                ReactionHandler.removeEditingReactions(pool);
                break;
            }
            case "delsugg": {
                long id = Long.parseLong(content);
                pool.currentSuggestions.remove(id);
                List<Long> list = new ArrayList<>();
                list.add(id);
                MessageSender.removeMessagesInBulk(pool, list);
                FileHandler.rewriteCurrentSuggestions(pool);
                break;
            }
            case "approveall": {
                if (!pool.editors.contains(authorID)) return;
                pool.currentSuggestions.forEach((l, s) -> {
                    s.result = "Accepted";
                    pool.pendingSuggestions.add(l);
                });
                pool.commandsToDelete.add(Long.parseLong(messageID));
                SuggestionHandler.finishEditing(pool, authorID);
                break;
            }
            case "save": {
                FileHandler.saveAllSuggestions(pool);
                break;
            }
            case "reject": {
                Map.Entry<Long, Suggestion> found = SuggestionHandler.findSuggestion(content, pool.allSuggestions);
                if (found == null) {
                    pool.allSuggestions.put(Long.parseLong(messageID), new Suggestion(content, authorID, "https://discordapp.com/channels/" + serverID + "/" + channelID + "/" + messageID));
                } else {
                    found.getValue().result = "Rejected";
                }
                FileHandler.saveAllSuggestions(pool);
                break;
            }
            default: {
            }

        }
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        SuggestionPool pool = SuggestionHandler.getPool(event.getChannel().getId());
        if (pool == null) return;
        ReactionHandler.manageReaction(pool, true, event.getReactionEmote().getAsCodepoints(), event.getMessageIdLong(), event.getUserId());
    }

    @Override
    public void onMessageReactionRemove(@Nonnull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot()) return;
        SuggestionPool pool = SuggestionHandler.getPool(event.getChannel().getId());
        if (pool == null) return;
        ReactionHandler.manageReaction(pool, false, event.getReactionEmote().getAsCodepoints(), event.getMessageIdLong(), event.getUserId());
    }
}

package darkere.suggestionbot;

import net.dv8tion.jda.api.entities.Message;

public class ReactionHandler {


    public static void addVoteReactions(Message message) {
        try {
            message.addReaction("U+1f44d").queue();
            message.addReaction("U+1f44e").queue();
            message.addReaction("U+1f937").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addEditingReactions(Message message) {
        try {
            message.addReaction("U+2705").queue();
            message.addReaction("U+274c").queue();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void manageReaction(SuggestionPool pool, boolean add, String emoteCode, long messageID, String authorID) {
        Suggestion suggestion = pool.currentSuggestions.get(messageID);
        boolean isEditor = pool.editors.contains(authorID);
        boolean isEvaluation = pool.suggestionsToReevaluate.containsKey(messageID);
        if (isEvaluation) {
            suggestion = pool.suggestionsToReevaluate.get(messageID);
        }
        if (suggestion == null) return;
        switch (emoteCode) {
            case "U+1f44d"://thumbsup
                if (add) suggestion.yesVote++;
                else suggestion.yesVote--;
                break;
            case "U+1f44e"://thumbsdown
                if (add) suggestion.noVote++;
                else suggestion.noVote--;
                break;
            case "U+1f937":
                if (add) suggestion.dontCare++;
                else suggestion.dontCare--;
                break;
            case "U+2705": // checkmark
                if (isEvaluation) {
                    suggestion.result = "TBD";
                    MessageSender.writeSuggestionToChannel(pool, suggestion);
                    pool.getCommandChannel().clearReactionsById(messageID).queue();
                    return;
                }
                if (!isEditor) return;
                if (add) {
                    suggestion.result = "Accepted";
                    pool.pendingSuggestions.add(messageID);
                } else {
                    suggestion.result = "TBD";
                    pool.pendingSuggestions.remove(messageID);
                }
                break;
            case "U+274c":// X
                if (isEvaluation) {
                    pool.getCommandChannel().clearReactionsById(messageID).queue();
                    return;
                }
                if (!isEditor) return;
                if (add) {
                    suggestion.result = "Rejected";
                    pool.pendingSuggestions.add(messageID);
                } else {
                    suggestion.result = "TBD";
                    pool.pendingSuggestions.remove(messageID);
                }
                break;
            default:
                return;
        }
        MessageSender.updateSuggestionMessage(pool, messageID);
    }

    static void removeEditingReactions(SuggestionPool pool) {

        for (long id : pool.currentSuggestions.keySet()) {
            try {
                pool.getSuggestionChannel().removeReactionById(id, "U+2705").queue();
                pool.getSuggestionChannel().removeReactionById(id, "U+274c").queue();
            } catch (Exception e) {
                System.err.println("Message " + id + " Not Found in current suggestions");
            }
        }
    }
}

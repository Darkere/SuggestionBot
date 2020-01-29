package darkere.suggestionbot;

import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SuggestionPool {
    String name;
    Map<Long, Suggestion> currentSuggestions = new HashMap<>();
    Map<Long, Suggestion> allSuggestions = new HashMap<>();
    List<Long> pendingSuggestions = new ArrayList<>();
    Map<Long, Suggestion> suggestionsToReevaluate = new HashMap<>();
    String suggestionChannel;
    String commandChannel;
    boolean isInEditMode = false;
    List<String> editors = new ArrayList<>();
    String serverID;
    List<Long> commandsToDelete = new ArrayList<>();

    public TextChannel getSuggestionChannel(){
        return SuggestionsBot.jda.getTextChannelById(suggestionChannel);
    }
    public TextChannel getCommandChannel(){
        return SuggestionsBot.jda.getTextChannelById(commandChannel);
    }

    public SuggestionPool(String name, String suggestionChannel, String commandChannel,String serverID) {
        this.name = name;
        this.suggestionChannel = suggestionChannel;
        this.commandChannel = commandChannel;
        this.serverID = serverID;
    }
    public void addCommandToDelete(long messageId){
        commandsToDelete.add(messageId);
    }


    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SuggestionPool)){
            return false;
        }
        SuggestionPool o = (SuggestionPool) obj;
        return this.commandChannel.equals(o.commandChannel)
                && this.suggestionChannel.equals(o.suggestionChannel)
                && this.name.equals(o.name);
    }

    public String writeAllSuggestions(){
        return getString(allSuggestions);
    }
    public String writeCurrentSuggestions(){
        return getString(currentSuggestions);
    }

    @NotNull
    private String getString(Map<Long, Suggestion> suggestions) {
        String linesep = System.getProperty("line.separator");
        StringBuilder b= new StringBuilder();
        suggestions.forEach((l, s)->{
            b.append(l);
            b.append(",");
            b.append(s.toString());
            b.append(linesep);
        });
        return b.toString();
    }

    public String writePendingSuggestions(){
        String linesep = System.getProperty("line.separator");
        StringBuilder b= new StringBuilder();
        pendingSuggestions.forEach((l)->{
            b.append(l);
            b.append(linesep);
        });
        return b.toString();
    }

    @Override
    public String toString() {
        String sep = System.getProperty("line.separator");
        String b = name +
                sep +
                suggestionChannel +
                sep +
                commandChannel +
                sep +
                serverID;
        return b;


    }
}

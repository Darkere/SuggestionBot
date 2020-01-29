package darkere.suggestionbot;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SuggestionHandler {
    private static Map<Long, SuggestionPool> suggestionPools = new HashMap<>();


    public static void createNewSuggestion(SuggestionPool pool, String url, String authorID, String serverID, String channelID, String messageID) {
        Suggestion suggestion = new Suggestion(url, authorID, "https://discordapp.com/channels/" + serverID + "/" + channelID + "/" + messageID);
        if (!checkForExistingSuggestions(pool, suggestion, channelID)) {
            MessageSender.writeSuggestionToChannel(pool, suggestion);
        }
    }

    private static boolean checkForExistingSuggestions(SuggestionPool pool, Suggestion suggestion, String channelID) {
        Map.Entry<Long, Suggestion> found = findSuggestion(suggestion.url, pool.currentSuggestions);
        if (found != null) {
            MessageSender.callOutExistingSuggestion(pool, found.getKey(), found.getValue(), channelID);
            return true;
        }
        found = findSuggestion(suggestion.url, pool.allSuggestions);
        if (found != null) {
            MessageSender.askForReevaluation(pool, found.getValue(), channelID);
            return true;
        }
        return false;
    }

    public static Map.Entry<Long, Suggestion> findSuggestion(String url, Map<Long, Suggestion> suggestions) {
        for (Map.Entry<Long, Suggestion> entry : suggestions.entrySet()) {
            if (entry.getValue().url.equals(url)) {
                return entry;
            }
        }
        return null;
    }

    public static void createEditingOptions(SuggestionPool pool) {
        if (pool.isInEditMode) return;
        pool.isInEditMode = true;
        for (Map.Entry<Long, Suggestion> entry : pool.currentSuggestions.entrySet()) {
            CompletableFuture<Message> future = pool.getSuggestionChannel().retrieveMessageById(entry.getKey()).submit();
            future.thenAccept(ReactionHandler::addEditingReactions);
        }
    }

    public static void addEditor(SuggestionPool pool, String id) {
        pool.editors.add(id);
    }

    public static void finishEditing(SuggestionPool pool, String authorID) {
        if (!pool.editors.contains(authorID)) return;
        pool.isInEditMode = false;
        if (pool.pendingSuggestions.isEmpty()) return;

        List<Suggestion> acc = new ArrayList<>();
        List<Suggestion> deny = new ArrayList<>();
        for (long id : pool.pendingSuggestions) {
            Suggestion sugg = pool.currentSuggestions.get(id);
            if (sugg.result.equals("Accepted")) {
                acc.add(sugg);
            } else {
                deny.add(sugg);
            }
            pool.currentSuggestions.remove(id);
        }
        MessageSender.sendFileMessage(pool, acc, deny);
        ReactionHandler.removeEditingReactions(pool);
        pool.pendingSuggestions.addAll(pool.commandsToDelete);
        MessageSender.removeMessagesInBulk(pool, pool.pendingSuggestions);

        pool.pendingSuggestions.clear();
        FileHandler.rewriteCurrentSuggestions(pool);
        FileHandler.saveAllSuggestions(pool);
    }

    public static void newPool(String serverID, String channelID, String[] args) {
        SuggestionPool pool = new SuggestionPool(args[1], args[2], args[3], serverID);
        suggestionPools.put(Long.parseLong(args[3]), pool);
        suggestionPools.put(Long.parseLong(args[2]), pool);
        MessageBuilder builder = new MessageBuilder();
        builder.append("New pool created with name \"").append(pool.name).append("\"");
        builder.append('\n');
        builder.append("Suggestions go here: ").append(pool.getCommandChannel().getAsMention());
        builder.append('\n');
        builder.append("Bot will post in: ").append(pool.getSuggestionChannel().getAsMention());
        Objects.requireNonNull(SuggestionsBot.jda.getTextChannelById(channelID)).sendMessage(builder.build()).queue();
        FileHandler.writePoolToFile(pool);
    }

    public static SuggestionPool getPool(String ID) {
        return suggestionPools.get(Long.parseLong(ID));
    }

    public static Map<Long, SuggestionPool> getAllPools() {
        return suggestionPools;
    }

    private static BufferedReader openReader(String path) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
        } catch (Exception e) {
            System.out.println(path + " File not found");
        }
        return br;
    }

    public static void readAndCreateSuggestions() throws IOException {
        File[] directories = new File("data/").listFiles(File::isDirectory);
        if (directories == null) return;
        for (File file : directories) {
            String path = file.getPath() + "/";
            BufferedReader br = openReader(path + "meta");
            String name = br.readLine();
            String suggestionChannel = br.readLine();
            String commandChannel = br.readLine();
            String serverID = br.readLine();
            br.close();
            SuggestionPool pool = new SuggestionPool(name, suggestionChannel, commandChannel, serverID);
            suggestionPools.put(Long.parseLong(suggestionChannel), pool);
            suggestionPools.put(Long.parseLong(commandChannel), pool);
            BufferedReader br1 = openReader(path + "AllSuggestions.csv");
            if (br1 != null) {
                while (true) {
                    String line = br1.readLine();
                    if (line == null) break;
                    if (line.isEmpty()) continue;
                    List<String> list = Arrays.asList(line.split(","));
                    long messageID = Long.parseLong(list.get(0));
                    Suggestion sugg = new Suggestion(list);
                    pool.allSuggestions.put(messageID, sugg);
                }
                br1.close();
            }

            BufferedReader br2 = openReader(path + "CurrentSuggestions.csv");
            if (br2 != null) {
                while (true) {
                    String line = br2.readLine();
                    if (line == null) break;
                    if (line.isEmpty()) continue;
                    List<String> list = Arrays.asList(line.split(","));
                    long messageID = Long.parseLong(list.get(0));
                    Suggestion sugg = new Suggestion(list);
                    pool.currentSuggestions.put(messageID, sugg);
                }
                br2.close();
            }

            BufferedReader br3 = openReader(path + "PendingSuggestions.csv");
            if (br3 != null) {
                while (true) {
                    String line = br3.readLine();
                    if (line == null) break;
                    if (line.isEmpty()) continue;
                    pool.pendingSuggestions.add(Long.parseLong(line));
                }
                br3.close();
            }


        }

    }
}

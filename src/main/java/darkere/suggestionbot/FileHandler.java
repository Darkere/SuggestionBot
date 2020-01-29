package darkere.suggestionbot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileHandler {

    public static void saveAllPools(){
        for(SuggestionPool pool : SuggestionHandler.getAllPools().values()){
            rewriteCurrentSuggestions(pool);
            saveAllSuggestions(pool);
        }
    }

    public static void rewriteCurrentSuggestions(SuggestionPool pool) {
        String path = "data/" + pool.name;
        File f = new File(path + "/CurrentSuggestions.csv");
        f.delete();
        if (pool.currentSuggestions.isEmpty()) return;
        addStringTofile(pool.writeCurrentSuggestions(), path + "/CurrentSuggestions.csv", false);

    }

    public static File writeFileForSending(List<Suggestion> suggestions, String name) {
        if (suggestions.isEmpty()) return null;
        File f = new File(name);
        f.delete();
        for (Suggestion s : suggestions) {
            addStringTofile(s.writeReadable(), name, true);
        }
        return f;
    }
    public static void saveAllSuggestions(SuggestionPool pool){
        if(pool.allSuggestions.isEmpty())return;
        String path = "data/" + pool.name;
        File f = new File(path + "/AllSuggestions.csv");
        f.delete();
        addStringTofile(pool.writeAllSuggestions(), path + "/AllSuggestions.csv", false);

    }

    public static void writeSuggestionToFile(SuggestionPool pool, String messageID, Suggestion suggestion) {
        String path = "data/" + pool.name;
        makeFolder("data");
        makeFolder(path);
        addStringTofile(messageID + "," + suggestion.toString(), path + "/CurrentSuggestions.csv", true);
        addStringTofile(messageID + "," + suggestion.toString(), path + "/AllSuggestions.csv", true);
    }

    private static void makeFolder(String path) {
        File file = new File(path);
        file.mkdir();
    }

    private static void addStringTofile(String data, String path, boolean appendempty) {
        File file = new File(path);
        FileWriter fr = null;
        try {
            fr = new FileWriter(file, true);
            fr.write(data);
            if (appendempty) fr.write(System.getProperty("line.separator"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close resources
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void writePoolToFile(SuggestionPool pool) {
        String path = "data/" + pool.name;
        makeFolder("data");
        makeFolder(path);
        addStringTofile(pool.toString(), path + "/meta", true);
    }
}

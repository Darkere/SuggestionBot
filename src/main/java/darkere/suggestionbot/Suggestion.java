package darkere.suggestionbot;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class Suggestion {

    int yesVote;
    int noVote;
    int dontCare;
    String url;
    String authorID;
    String messageLink;
    String result = "TBD";

    public Suggestion(List<String> string) {
        url = string.get(1);
        result = string.get(2);
        yesVote = Integer.parseInt(string.get(3));
        noVote = Integer.parseInt(string.get(4));
        dontCare = Integer.parseInt(string.get(5));
        messageLink = string.get(6);
        authorID = string.get(7);

    }

    public Suggestion(String url, String authorID, String messageLink) {
        this.url = url;
        this.authorID = authorID;
        this.messageLink = messageLink;
    }

    public String getSuggestionHeader() {
        String sep = ",";
        return "url" +
                sep +
                "result" +
                sep +
                "yesVote" +
                sep +
                "noVote" +
                sep +
                "dontCare" +
                sep +
                "messageLink" +
                sep +
                "authorID"+
                sep +
                "messageID";
    }

    @Override
    public String toString() {
        String sep = ",";
        return url +
                sep +
                result +
                sep +
                yesVote +
                sep +
                noVote +
                sep +
                dontCare +
                sep +
                messageLink +
                sep +
                authorID;
    }
    public String writeReadable(){
        return url;

    }
}

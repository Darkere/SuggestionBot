package darkere.suggestionbot;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SuggestionsBot {
    static JDA jda;

    public static void main(String[] args) throws LoginException {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("token"));
            token = br.readLine();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (token.equals("")) {
            System.out.println("NO TOKEN FOUND add a file called token with the bot token inside");
            System.exit(0);
        }
        builder.setToken(token);
        try {
            SuggestionHandler.readAndCreateSuggestions();
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.addEventListeners(new MessageReceiver());

        jda = builder.build();
    }
}

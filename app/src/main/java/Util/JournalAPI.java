package Util;

import android.app.Application;

public class JournalAPI extends Application {
    private String userName;
    private String userID;
    private static JournalAPI instance;

    public static JournalAPI getInstance()
    {
        if (instance == null)
            instance = new JournalAPI();
        return instance;
    }

    public JournalAPI() {}

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}

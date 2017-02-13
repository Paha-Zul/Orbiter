package com.quickbite.spaceslingshot.interfaces;

import com.quickbite.spaceslingshot.guis.GameScreenGUI;

/**
 * Created by Paha on 1/28/2016.
 */
public interface ActionResolver {
    boolean getSignedInGPGS();
    void loginGPGS();
    void logoutGPGS();
    void submitLeaderboardScore(String tableID, long score);

    /**
     * Submits an event to analytics
     * @param eventID The EventID for google play games. If blank, will not send anything.
     * @param GA_ID The EventID for GameAnalytics. If blank, will not send anything.
     */
    void submitEvent(String eventID, String GA_ID);

    /**
     * Submits the game structure settings to GameAnalytics. Call this only when the start game button is hit.
     */
    void submitGameStructure();

    void unlockAchievementGPGS(String achievementId);
    void getCurrentRankInLeaderboards(String tableID, GameScreenGUI gameOverGUI);
    void getLeaderboardGPGS(String leaderboardID);
    void getAchievementsGPGS();
    void getLeaderboardScore(String leaderboardID, int timeSpan);
    void getCenteredLeaderboardScore(String leaderboardID, int timeSpan, int leaderboardType, float timeoutSeconds);
    void getTopLeaderboardScores(String leaderboardID, int timeSpan, int numScores);

}

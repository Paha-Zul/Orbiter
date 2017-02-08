package com.quickbite.spaceslingshot.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.quickbite.spaceslingshot.MyGame;
import com.quickbite.spaceslingshot.guis.GameScreenGUI;
import com.quickbite.spaceslingshot.interfaces.ActionResolver;
import com.quickbite.spaceslingshot.interfaces.AdInterface;
import com.quickbite.spaceslingshot.interfaces.Transactions;

public class DesktopLauncher implements ActionResolver, AdInterface, Transactions {
	public static void main (String[] arg) {
		DesktopLauncher instance = new DesktopLauncher();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 480;
		config.height = 800;

		new LwjglApplication(new MyGame(instance, instance, instance), config);
	}

	@Override
	public boolean getSignedInGPGS() {
		return false;
	}

	@Override
	public void loginGPGS() {

	}

	@Override
	public void logoutGPGS() {

	}

	@Override
	public void submitScoreGPGS(String tableID, long score) {

	}

	@Override
	public void submitEvent(String eventID, String GA_ID) {

	}

	@Override
	public void submitGameStructure() {

	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {

	}

	@Override
	public void getCurrentRankInLeaderboards(String tableID, GameScreenGUI gameOverGUI) {

	}

	@Override
	public void getLeaderboardGPGS(String leaderboardID) {

	}

	@Override
	public void getAchievementsGPGS() {

	}

	@Override
	public void getLeaderboardScore(String leaderboardID, int timeSpan) {

	}

	@Override
	public void getCenteredLeaderboardScore(String leaderboardID, int timeSpan, int leaderboardType, float timeoutSeconds) {

	}

	@Override
	public void getTopLeaderboardScores(String leaderboardID, int timeSpan, int numScores) {

	}

	@Override
	public void showAdmobBannerAd() {

	}

	@Override
	public void hideAdmobBannerAd() {

	}

	@Override
	public void loadAdmobInterAd() {

	}

	@Override
	public void showAdmobInterAd() {

	}

	@Override
	public void hideAdmobInterAd() {

	}

	@Override
	public boolean showAds() {
		return false;
	}

	@Override
	public void purchaseNoAds() {

	}
}

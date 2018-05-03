package com.quickbite.spaceslingshot.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.quickbite.spaceslingshot.MyGame;
import com.quickbite.spaceslingshot.guis.MainMenuGUI;
import com.quickbite.spaceslingshot.interfaces.ActionResolver;
import com.quickbite.spaceslingshot.interfaces.AdInterface;
import com.quickbite.spaceslingshot.interfaces.Transactions;

public class DesktopLauncher implements ActionResolver, AdInterface, Transactions {
	boolean loggedInGPG = false; //Fake logged in value for testing.

	public static void main (String[] arg) {
		DesktopLauncher instance = new DesktopLauncher();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 480;
		config.height = 800;

		new LwjglApplication(new MyGame(instance, instance, instance), config);
	}

	@Override
	public boolean getSignedInGPGS() {
		return this.loggedInGPG;
	}

	@Override
	public void loginGPGS() {
		System.out.println("[DesktopLauncher] Logged in");
		this.loggedInGPG = true;
	}

	@Override
	public void logoutGPGS() {
		System.out.println("[DesktopLauncher] Logged out");
		this.loggedInGPG = false;
	}

	@Override
	public void submitLeaderboardScore(String tableID, long score) {
		System.out.println("Submitted score of "+score+" to "+tableID);
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
	public void showLeaderboard(String leaderboardID) {

	}

	@Override
	public void showAchievements() {

	}

	@Override
	public void showBannerAd() {

	}

	@Override
	public void hideBannerAd() {

	}

	@Override
	public void loadAdmobInterAd() {

	}

	@Override
	public void showInterAd() {

	}

	@Override
	public void hideInterAd() {

	}

	@Override
	public boolean showAds() {
		return false;
	}

	@Override
	public void purchaseNoAds() {
		MainMenuGUI.Companion.removeAdsButton();
	}
}

package com.quickbite.spaceslingshot;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import com.appodeal.gdx.GdxAppodeal;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Timer;
import com.example.android.trivialdrivesample.util.IabHelper;
import com.example.android.trivialdrivesample.util.IabResult;
import com.example.android.trivialdrivesample.util.Inventory;
import com.example.android.trivialdrivesample.util.Purchase;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.GameHelper;
import com.quickbite.spaceslingshot.guis.GameScreenGUI;
import com.quickbite.spaceslingshot.guis.MainMenuGUI;
import com.quickbite.spaceslingshot.interfaces.ActionResolver;
import com.quickbite.spaceslingshot.interfaces.AdInterface;
import com.quickbite.spaceslingshot.interfaces.Transactions;

public class AndroidLauncher extends AndroidApplication implements GameHelper.GameHelperListener, ActionResolver, AdInterface, Transactions {
	GameHelper gameHelper;
	final private static String TAG = "AndroidLauncher";

	private boolean resumed = false, changedFocus = false;

	private View gameView;
//	private InterstialAdMediator interAds;
	private boolean showingBannerAd = false, showAds = false, bannerAdLoaded = false;

	private boolean dailyGood = false, weeklyGood = false, allTimeGood = false;
	private IabHelper mHelper;


	@Override
	protected void onCreate (Bundle savedInstanceState) {
		setupUpGameHelper();

		Game game = new MyGame(this, this, this);


//		GameAnalytics.configureBuild("1.0.2");
//		GameAnalytics.initializeWithGameKey(this, "06fb38014af7b80c56da048dc58621f1", "33294fa657a1b0ccca1fecac2c80ae1b2e7e1ff8");

		initBilling();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(game, config);

        setupAds(game);

        super.onCreate(savedInstanceState);
	}

	/**
	 * Sets up the gameHelper for Google Play Games
	 */
	private void setupUpGameHelper(){
		if (gameHelper == null) {
			gameHelper = new GameHelper(this, GameHelper.CLIENT_GAMES);
			gameHelper.setMaxAutoSignInAttempts(0);
			gameHelper.enableDebugLog(true);
		}

		gameHelper.setup(this);
	}

	private void setupAds(Game game){
		String appKey = "ecc3499bf389398ad32f0cbe07263654765899535432107b";
		GdxAppodeal.disableLocationPermissionCheck();
        GdxAppodeal.disableNetwork("cheetah");
        GdxAppodeal.setAutoCache(GdxAppodeal.INTERSTITIAL, true);
        GdxAppodeal.initialize(appKey, GdxAppodeal.INTERSTITIAL | GdxAppodeal.BANNER);

//		AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
//		cfg.useAccelerometer = false;
//		cfg.useCompass = false;
//
//		// Do the stuff that initialize() would do for you
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//
//		FrameLayout fLayout = new FrameLayout(this);
//		FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT,
//				Gravity.BOTTOM|android.view.Gravity.CENTER_HORIZONTAL);
//		fLayout.setLayoutParams(fParams);
//
//		AdView admobView = createAdView();
//
//		View gameView = createGameView(cfg, game);
//
//		fLayout.addView(gameView);
//		fLayout.addView(admobView);
//
//		setContentView(fLayout);
//		startAdvertising(admobView);
//
//		interAds = new InterstialAdMediator(this);
	}

//	private AdView createAdView() {
//		adView = new AdView(this);
//		adView.setAdSize(AdSize.SMART_BANNER);
//		adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
//		adView.setId(R.id.adViewId); // this is an arbitrary id, allows for relative positioning in createGameView()
//		FrameLayout.LayoutParams fParams = new FrameLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT,
//				Gravity.BOTTOM|android.view.Gravity.CENTER_HORIZONTAL);
//		adView.setLayoutParams(fParams);
//		adView.setBackgroundColor(Color.TRANSPARENT);
//		return adView;
//	}
//
//	private View createGameView(AndroidApplicationConfiguration cfg, Game game) {
//		gameView = initializeForView(game, cfg);
//		return gameView;
//	}
//
//	private void startAdvertising(AdView adView) {
//		AdRequest.Builder builder = new AdRequest.Builder();
//		builder.addTestDevice("BE119EDAB7342FD3CF2C0405E4AFF269");
//		AdRequest adRequest = builder.build();
//		adView.loadAd(adRequest);
//		adView.setVisibility(View.GONE);
//	}

	@Override
	public void showBannerAd() {
        if(showAds)
		    GdxAppodeal.show(GdxAppodeal.BANNER_BOTTOM);

//		if(showAds) {
//			showingBannerAd = true;
//			runOnUiThread(new Runnable() {
//				@Override
//				public void run() {
//					adView.setVisibility(View.VISIBLE);
//				}
//			});
//		}
	}

	@Override
	public void hideBannerAd() {
		GdxAppodeal.hide(GdxAppodeal.BANNER_BOTTOM);

//		showingBannerAd = false;
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				adView.setVisibility(View.GONE);
//			}
//		});
	}

	@Override
	public void loadAdmobInterAd() {

	}

	@Override
	public void showInterAd() {
        GdxAppodeal.show(GdxAppodeal.INTERSTITIAL);
//		if(showAds) {
//			try {
//				runOnUiThread(new Runnable() {
//					public void run() {
//						if (interAds.isLoaded()) {
//							interAds.show();
//						}
//					}
//				});
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public void hideInterAd() {

	}

	@Override
	public boolean showAds() {
		return showAds;
	}

	@Override
	public void onStart(){
		super.onStart();

		gameHelper.onStart(this);
	}

	@Override
	public void onStop(){
		super.onStop();

		gameHelper.onStop();
	}

	@Override
	public void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);
		gameHelper.onActivityResult(request, response, data);
//		mHelper.handleActivityResult(request, response, data);

		/** Don't uncomment below? */

//		if ((request == 100) && response == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
//			gameHelper.disconnect();
//			// update your logic here (show login btn, hide logout btn).
//		} else {
//			try {
//				gameHelper.onActivityResult(request, response, data);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//		}
	}

	@Override
	public void onResume() {
		super.onResume();
		resumed = true;

//		if(changedFocus)
//			SoundManager.playMusic();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		changedFocus = hasFocus;

//		if(changedFocus && resumed)
//			SoundManager.playMusic();
	}

	@Override
	public void onPause() {
		super.onPause();
		resumed = false;

//		SoundManager.pauseMusic();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	public boolean getSignedInGPGS() {
		return gameHelper.isSignedIn();
	}

	@Override
	public void loginGPGS() {
		try {
			runOnUiThread(new Runnable(){
				public void run() {
					gameHelper.beginUserInitiatedSignIn();
				}
			});
		} catch (final Exception ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void logoutGPGS() {
		runOnUiThread(new Runnable() {
			public void run() {
				gameHelper.signOut();
			}
		});
	}

	@Override
	public void submitLeaderboardScore(String tableID, long score) {
		if (getSignedInGPGS()) {
			Games.Leaderboards.submitScore(gameHelper.getApiClient(), tableID, score);
		}
	}

	/**
	 * Cancels the timout Timer for getting ranks.
	 * @param timeOutTimer
	 */
	private void checkToCancelTimer(Timer timeOutTimer){
		if(dailyGood && weeklyGood && allTimeGood){
			timeOutTimer.clear();
			dailyGood = weeklyGood = allTimeGood = false;
		}
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
		if (getSignedInGPGS()) {
			Games.Achievements.unlock(gameHelper.getApiClient(), achievementId);
		}
	}

	@Override
	public void getCurrentRankInLeaderboards(String tableID, final GameScreenGUI gameOverGUI) {
		dailyGood = weeklyGood = allTimeGood = false;

		if (getSignedInGPGS()) {
			final Timer timeOutTimer = new Timer();

			Log.i("AndroidGame", "Trying to get ranks");

			final PendingResult<Leaderboards.LoadPlayerScoreResult> dailyResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(gameHelper.getApiClient(), tableID, LeaderboardVariant.TIME_SPAN_DAILY, LeaderboardVariant.COLLECTION_PUBLIC);
			final PendingResult<Leaderboards.LoadPlayerScoreResult> weekylResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(gameHelper.getApiClient(), tableID, LeaderboardVariant.TIME_SPAN_WEEKLY, LeaderboardVariant.COLLECTION_PUBLIC);
			final PendingResult<Leaderboards.LoadPlayerScoreResult> allTimeResult = Games.Leaderboards.loadCurrentPlayerLeaderboardScore(gameHelper.getApiClient(), tableID, LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC);

			dailyResult.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
				@Override
				public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadScoresResult) {
					String status = loadScoresResult.getStatus().getStatusMessage();
					boolean okay = loadScoresResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK;
					boolean notNull = loadScoresResult.getScore() != null;
					Log.i("AndroidGame", "Received all time rank. Okay/notNull? "+okay+"/"+notNull);
					if(okay && notNull) {
						Log.i("AndroidGame", "Implementing daily rank");
//						gameOverGUI.setDailyRank(loadScoresResult.getScore().getDisplayRank());
						dailyGood = true;
						checkToCancelTimer(timeOutTimer);
					}else{
						Log.i("AndroidGame", "[daily] Something went wrong, status: "+status);
//						gameOverGUI.setDailyRank("Error");
					}
				}
			});

			weekylResult.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
				@Override
				public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadScoresResult) {
					String status = loadScoresResult.getStatus().getStatusMessage();
					boolean okay = loadScoresResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK;
					boolean notNull = loadScoresResult.getScore() != null;
					Log.i("AndroidGame", "Received weekly rank. Okay/notNull? "+okay+"/"+notNull);
					if(okay && notNull) {
						Log.i("AndroidGame", "Implementing weekly rank");
//						gameOverGUI.setWeeklyRank(loadScoresResult.getScore().getDisplayRank());
						weeklyGood = true;
						checkToCancelTimer(timeOutTimer);
					}else{
						Log.i("AndroidGame", "[weekly] Something went wrong, status: "+status);
//						gameOverGUI.setWeeklyRank("Error");
					}
				}
			});

			allTimeResult.setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
				@Override
				public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult loadScoresResult) {
					String status = loadScoresResult.getStatus().getStatusMessage();
					boolean okay = loadScoresResult.getStatus().getStatusCode() == GamesStatusCodes.STATUS_OK;
					boolean notNull = loadScoresResult.getScore() != null;
					Log.i("AndroidGame", "Received all time rank. Okay/notNull? "+okay+"/"+notNull);
					if(okay && notNull) {
						Log.i("AndroidGame", "Implementing all time rank");
//						gameOverGUI.setAllTimeRank(loadScoresResult.getScore().getDisplayRank());
						allTimeGood = true;
						checkToCancelTimer(timeOutTimer);
					}else{
						Log.i("AndroidGame", "[all time] Something went wrong, status: "+status);
//						gameOverGUI.setAllTimeRank("Error");
					}
				}
			});

			final double timeStarted = TimeUtils.millis();
			final double _timeout = 15000; //In millis, 15 seconds

			Log.i("AndroidGame", "Started getting scores at "+timeStarted);

			//If we wait too long, cancel the result.
			timeOutTimer.scheduleTask(new Timer.Task() {
				@Override
				public void run() {
//					if(TimeUtils.millis() >= timeStarted + _timeout || !gameOverGUI.isShowing()) {
//						Log.i("AndroidGame", "Getting ranks expired after "+(_timeout/1000)+" seconds, at "+(timeStarted + _timeout));
//
//						dailyResult.cancel();
//						weekylResult.cancel();
//						allTimeResult.cancel();
//						timeOutTimer.clear();
//
//						if(!dailyGood) gameOverGUI.setDailyRank("NA");
//						if(!weeklyGood) gameOverGUI.setWeeklyRank("NA");
//						if(!allTimeGood) gameOverGUI.setAllTimeRank("NA");
//
//						dailyGood = weeklyGood = allTimeGood = false;
//					}
				}
			}, 0, 0.5f);
		}else {
//			gameOverGUI.setDailyRank("Log In");
//			gameOverGUI.setWeeklyRank("Log In");
//			gameOverGUI.setAllTimeRank("Log In");
		}
	}

	@Override
	public void getLeaderboardGPGS(String leaderboardID) {
		if (getSignedInGPGS()) {
			startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(gameHelper.getApiClient()), 100);
		}
	}

	@Override
	public void getAchievementsGPGS() {
		//startActivityForResult(gameHelper.getGamesClient().getAchievementsIntent(), 100);
	}

	@Override
	public void getLeaderboardScore(String leaderboardID, int timeSpan) {
		Games.Leaderboards.loadCurrentPlayerLeaderboardScore(gameHelper.getApiClient(), leaderboardID,
				timeSpan, LeaderboardVariant.COLLECTION_PUBLIC);
	}

	@Override
	public void getCenteredLeaderboardScore(String leaderboardID, int timeSpan, int leaderboardType, float timeoutMillis) {
		final double timeStarted = TimeUtils.millis();
		final double _timeout = timeoutMillis; //In millis

		final Timer timeOutTimer = new Timer();

		final PendingResult<Leaderboards.LoadScoresResult> pendingResult = Games.Leaderboards.loadPlayerCenteredScores(gameHelper.getApiClient(),
				leaderboardID, timeSpan, leaderboardType, 1);

		//Create the callback. If valid, call the GUI to load the scores.
		pendingResult.setResultCallback(new ResultCallback<Leaderboards.LoadScoresResult>() {
			@Override
			public void onResult(@NonNull Leaderboards.LoadScoresResult loadScoresResult) {
				timeOutTimer.clear(); //Clear the timeOutTimer

				if(loadScoresResult.getStatus().getStatusCode() != GamesStatusCodes.STATUS_OK)
					return;

				for(LeaderboardScore score : loadScoresResult.getScores()){
					score.getDisplayRank();
				}
			}
		});

		//If we wait too long, cancel the result.
		timeOutTimer.scheduleTask(new Timer.Task() {
			@Override
			public void run() {
				if(TimeUtils.millis() >= timeStarted + _timeout) {
					pendingResult.cancel();
					timeOutTimer.clear();
				}
			}
		}, 0, 0.5f);

	}

	@Override
	public void getTopLeaderboardScores(String leaderboardID, int timeSpan, int numScores) {
		Games.Leaderboards.loadTopScores(gameHelper.getApiClient(), leaderboardID, timeSpan, LeaderboardVariant.COLLECTION_PUBLIC, 10);
	}

	@Override
	public void submitEvent(String eventID, String GA_ID) {
//		if(!eventID.isEmpty())
//			Games.Events.increment(gameHelper.getApiClient(), eventID, 1);
//
//		if(!GA_ID.isEmpty())
//			GameAnalytics.addDesignEventWithEventId(GA_ID);
	}

	@Override
	public void submitGameStructure() {
//		GameAnalytics.addDesignEventWithEventId(GH.getCurrGameConfig());
	}

	private void initBilling(){
		mHelper = new IabHelper(this, getString(R.string.d2) + getString(R.string.d1) + getString(R.string.d3));

		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
			@Override
			public void onIabSetupFinished(IabResult result) {
				if (!result.isSuccess()) {
					// Oh noes, there was a problem.
				}else {
					//Let's then query the inventory...
					try {
						mHelper.queryInventoryAsync(mGotInventoryListener);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void purchaseNoAds() {
		try {
			//Launch the purchase flow with a test SKU for now.
			mHelper.launchPurchaseFlow(this, SKU_NOADS, RC_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
				@Override
				public void onIabPurchaseFinished(IabResult result, Purchase info) {

					//If it is successful, hide the banner ad and remove the ad button
					if(result.isSuccess()){
						showAds = false; //Toggle off the ads
						hideBannerAd();
						MainMenuGUI.Companion.removeAdsButton();
//						GameAnalytics.addBusinessEventWithCurrency("USD", 99, info.getItemType(), "no_ads", "menu", "", "Google Play", info.getSignature());
					}else{
						Log.e(TAG, "Purchase was not successful");
					}
				}
			}, "hmm");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * This queries the inventory for items. We can use this to check for purchases.
     */
	private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		@Override
		public void onQueryInventoryFinished(IabResult result, Inventory inv) {
			if(result.isFailure() || mHelper == null || inv == null) {
				return;
			}

            //Check if we have the NO_ADS purchase
			if (inv.hasPurchase(SKU_NOADS)) {
				showAds = false;
				hideBannerAd();

				//If it matches my test device, consume it for now. (the equals('number') is my device id)
				if(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).equals("93c5883d462d97e9")){
					try {
                        //Try to consume it
						mHelper.consumeAsync(inv.getPurchase(SKU_NOADS), new IabHelper.OnConsumeFinishedListener() {
							@Override
							public void onConsumeFinished(Purchase purchase, IabResult result) {

							}
						});
					} catch (Exception e) {
						Log.e(TAG, "Error", e);
						e.printStackTrace();
					}
				}
			} else {
				showAds = true;
			}
		}
	};

	@Override
	public void onSignInFailed() {
//		SoundManager.playMusic();
	}

	@Override
	public void onSignInSucceeded() {
//		SoundManager.playMusic();
	}


	/**
	 * Handles custom interstitial ad mediating. For instance, trying to display a limited (every 10 mins) vide
	 * ad and falling back when it can't load.
	 */
//	private class InterstialAdMediator{
//		private InterstitialAd interstitialAd, interstitialAdVideo;
//		private InterstitialAd currLoadedAd;
//		private final String tag = "InterAdMediator";
//
//		public InterstialAdMediator(Context context){
//			 /* This is the interstitial ad that plays a video. Prioritize this.*/
//			interstitialAdVideo = new InterstitialAd(context);
//			interstitialAdVideo.setAdUnitId(getString(R.string.inter_video_ad_unit_id));
//			interstitialAdVideo.setAdListener(new AdListener() {
//				@Override
//				public void onAdLoaded() {
//					currLoadedAd = interstitialAdVideo;
//					Log.i(tag, "Loaded video inter ad");
//				}
//
//				@Override
//				public void onAdClosed() {
//					interstitialAdVideo.loadAd(new AdRequest.Builder().build());
//				}
//
//				@Override
//				public void onAdFailedToLoad(int i) {
//					super.onAdFailedToLoad(i);
//					//When this fails, let's load the regular inter ad
//					interstitialAd.loadAd(new AdRequest.Builder().build());
//					Log.i(tag, "Failed to load interVideoAd, loading regular");
//				}
//			});
//
//			 /* This is the interstitial ad that doesn't have a video*/
//			interstitialAd = new InterstitialAd(context);
//			interstitialAd.setAdUnitId(getString(R.string.inter_ad_unit_id));
//			interstitialAd.setAdListener(new AdListener() {
//				@Override
//				public void onAdLoaded() {
//					currLoadedAd = interstitialAd;
//					Log.i(tag, "Loaded regular inter ad");
//				}
//
//				@Override
//				public void onAdClosed() {
//					//On closing the ad, try to load a new video ad.
//					interstitialAdVideo.loadAd(new AdRequest.Builder().build());
//				}
//
//				@Override
//				public void onAdFailedToLoad(int i) {
//					super.onAdFailedToLoad(i);
//					Log.i(tag, "Failed to load regular interAd, fix this?");
//				}
//			});
//
//			//Initially load the video ad
//			interstitialAdVideo.loadAd(new AdRequest.Builder().build());
//		}
//
//		public void show(){
//			currLoadedAd.show();
//		}
//
//		public void hide(){
//
//		}
//
//		public boolean isLoaded(){
//			return currLoadedAd != null && currLoadedAd.isLoaded();
//		}
//	}
}

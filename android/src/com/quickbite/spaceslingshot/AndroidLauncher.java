package com.quickbite.spaceslingshot;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.utils.Timer;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.*;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.quickbite.spaceslingshot.interfaces.ActionResolver;
import com.quickbite.spaceslingshot.interfaces.AdInterface;
import com.quickbite.spaceslingshot.interfaces.Transactions;

public class AndroidLauncher extends AndroidApplication implements ActionResolver, AdInterface, Transactions {
	private static final int RC_UNUSED = 5001;
	private static final int RC_SIGN_IN = 9001;

	final private static String TAG = "AndroidLauncher";

	private boolean resumed = false, changedFocus = false;

	private View gameView;

//	private InterstialAdMediator interAds;
	private boolean showingBannerAd = false, showAds = false, bannerAdLoaded = false;

	// Client used to sign in with Google APIs
	private GoogleSignInClient mGoogleSignInClient;
	private AchievementsClient mAchievementsClient;
	private LeaderboardsClient mLeaderboardsClient;
	private EventsClient mEventsClient;
	private PlayersClient mPlayersClient;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		setupUpGameHelper();

		Game game = new MyGame(this, this, this);

		initBilling();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(game, config);

        setupAds(game);

		mGoogleSignInClient = GoogleSignIn.getClient(this,
				new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
						.requestEmail()
						.build());

		super.onCreate(savedInstanceState);
	}

	/**
	 * Sets up the gameHelper for Google Play Games
	 */
	private void setupUpGameHelper(){
		// Create the client used to sign in to Google services.
	}

	private void setupAds(Game game){
		String appKey = "ecc3499bf389398ad32f0cbe07263654765899535432107b";
//		GdxAppodeal.disableLocationPermissionCheck();
//        GdxAppodeal.disableNetwork("cheetah");
//        GdxAppodeal.disableNetwork("mopub");
//        GdxAppodeal.setAutoCache(GdxAppodeal.INTERSTITIAL, true);
//        GdxAppodeal.initialize(appKey, GdxAppodeal.INTERSTITIAL | GdxAppodeal.BANNER);

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
//        if(showAds)
//		    GdxAppodeal.show(GdxAppodeal.BANNER_BOTTOM);

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
//		GdxAppodeal.hide(GdxAppodeal.BANNER_BOTTOM);

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
//        GdxAppodeal.show(GdxAppodeal.INTERSTITIAL);

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

//		gameHelper.onStart(this);
	}

	@Override
	public void onStop(){
		super.onStop();

//		gameHelper.onStop();
	}



	@Override
	public void onActivityResult(int request, int response, Intent data) {
		super.onActivityResult(request, response, data);

		if (request == RC_SIGN_IN) {
			Task<GoogleSignInAccount> task =
					GoogleSignIn.getSignedInAccountFromIntent(getIntent());

			try {
				GoogleSignInAccount account = task.getResult(ApiException.class);
				onConnected(account);
			} catch (ApiException apiException) {
				String message = apiException.getMessage();
				if (message == null || message.isEmpty()) {
					message = getString(R.string.signin_other_error);
				}

				onDisconnected();

				new AlertDialog.Builder(this)
						.setMessage(message)
						.setNeutralButton(android.R.string.ok, null)
						.show();
			}
		}
	}

	private void onConnected(GoogleSignInAccount googleSignInAccount) {
		Log.d(TAG, "onConnected(): connected to Google APIs");

		mAchievementsClient = Games.getAchievementsClient(this, googleSignInAccount);
		mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
		mEventsClient = Games.getEventsClient(this, googleSignInAccount);
		mPlayersClient = Games.getPlayersClient(this, googleSignInAccount);
	}

	private void onDisconnected() {
		Log.d(TAG, "onDisconnected()");

		mAchievementsClient = null;
		mLeaderboardsClient = null;
		mPlayersClient = null;
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
//		return gsClient.isSessionActive();
		return GoogleSignIn.getLastSignedInAccount(this) != null;
	}

	@Override
	public void loginGPGS() {

//		if (!GoogleSignIn.hasPermissions(
////				GoogleSignIn.getLastSignedInAccount(getContext()),Drive.SCOPE_APPFOLDER)) {
////			GoogleSignIn.requestPermissions(
////					MyExampleActivity.this,
////					RC_REQUEST_PERMISSION_SUCCESS_CONTINUE_FILE_CREATION,
////					GoogleSignIn.getLastSignedInAccount(getActivity()),
////					Drive.SCOPE_APPFOLDER);
////		} else {
////			saveToDriveAppFolder();
////		}

		startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
	}

	/**
	 * Signs in to GPG silently. We can use this to silently sign back in when the game changes state (minimized)
	 */
	private void signInSilently(){
		mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
				new OnCompleteListener<GoogleSignInAccount>() {
					@Override
					public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
						if (task.isSuccessful()) {
							Log.d(TAG, "signInSilently(): success");
							onConnected(task.getResult());
						} else {
							Log.d(TAG, "signInSilently(): failure", task.getException());
							onDisconnected();
						}
					}
				});
	}

	@Override
	public void logoutGPGS() {
//		runOnUiThread(new Runnable() {
//			public void run() {
//				gsClient.logOff();
//			}
//		});
	}

	@Override
	public void submitLeaderboardScore(String tableID, long score) {
//		if (getSignedInGPGS()) {
//			gsClient.submitToLeaderboard(tableID, score, null);
////			Games.Leaderboards.submitScore(gameHelper.getApiClient(), tableID, score);
//		}
	}

	/**
	 * Cancels the timout Timer for getting ranks.
	 * @param timeOutTimer
	 */
	private void checkToCancelTimer(Timer timeOutTimer){
//		if(dailyGood && weeklyGood && allTimeGood){
//			timeOutTimer.clear();
//			dailyGood = weeklyGood = allTimeGood = false;
//		}
	}

	@Override
	public void unlockAchievementGPGS(String achievementId) {
//		if (getSignedInGPGS())
//			gsClient.unlockAchievement(achievementId);
	}

	@Override
	public void showLeaderboard(String leaderboardID) {
		if(!getSignedInGPGS())
			return;

		//Call to get the leaderboard
		mLeaderboardsClient.getAllLeaderboardsIntent()
				//Add a success listener
				.addOnSuccessListener(new OnSuccessListener<Intent>() {
					@Override
					public void onSuccess(Intent intent) {
						//Show the leaderboard!
						startActivityForResult(intent, RC_UNUSED);
					}
				})
				//Add a failure listener
				.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						//Handle it!
//						handleException(e, getString(R.string.leaderboards_exception));
					}
				});
	}

	@Override
	public void showAchievements() {
//		try {
//			gsClient.showAchievements();
//		} catch (GameServiceException e) {
//			e.printStackTrace();
//		}

//		//startActivityForResult(gameHelper.getGamesClient().getAchievementsIntent(), 100);
	}

	@Override
	public void submitEvent(String eventID, String GA_ID) {
		//TODO Do we need this?
	}

	@Override
	public void submitGameStructure() {
		//TODO Do we need this?
	}

	private void initBilling(){
//		mHelper = new IabHelper(this, getString(R.string.d2) + getString(R.string.d1) + getString(R.string.d3));
//
//		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
//			@Override
//			public void onIabSetupFinished(IabResult result) {
//				if (!result.isSuccess()) {
//					// Oh noes, there was a problem.
//				}else {
//					//Let's then query the inventory...
//					try {
//						mHelper.queryInventoryAsync(mGotInventoryListener);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		});
	}

	@Override
	public void purchaseNoAds() {
//		try {
//			//Launch the purchase flow with a test SKU for now.
//			mHelper.launchPurchaseFlow(this, SKU_NOADS, RC_REQUEST, new IabHelper.OnIabPurchaseFinishedListener() {
//				@Override
//				public void onIabPurchaseFinished(IabResult result, Purchase info) {
//
//					//If it is successful, hide the banner ad and remove the ad button
//					if(result.isSuccess()){
//						showAds = false; //Toggle off the ads
//						hideBannerAd();
//						MainMenuGUI.Companion.removeAdsButton();
////						GameAnalytics.addBusinessEventWithCurrency("USD", 99, info.getItemType(), "no_ads", "menu", "", "Google Play", info.getSignature());
//					}else{
//						Log.e(TAG, "Purchase was not successful");
//					}
//				}
//			}, "hmm");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}


//    /**
//     * This queries the inventory for items. We can use this to check for purchases.
//     */
//	private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
//		@Override
//		public void onQueryInventoryFinished(IabResult result, Inventory inv) {
//			if(result.isFailure() || mHelper == null || inv == null) {
//				return;
//			}
//
//            //Check if we have the NO_ADS purchase
//			if (inv.hasPurchase(SKU_NOADS)) {
//				showAds = false;
//				hideBannerAd();
//
//				//If it matches my test device, consume it for now. (the equals('number') is my device id)
//				if(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID).equals("93c5883d462d97e9")){
//					try {
//                        //Try to consume it
//						mHelper.consumeAsync(inv.getPurchase(SKU_NOADS), new IabHelper.OnConsumeFinishedListener() {
//							@Override
//							public void onConsumeFinished(Purchase purchase, IabResult result) {
//
//							}
//						});
//					} catch (Exception e) {
//						Log.e(TAG, "Error", e);
//						e.printStackTrace();
//					}
//				}
//			} else {
//				showAds = true;
//			}
//		}
//	};



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

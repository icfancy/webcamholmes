/**
 * Copyright (C) 2010 Alfredo Morresi
 * 
 * This file is part of WebcamHolmes project.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package it.rainbowbreeze.webcamholmes.common;

import it.rainbowbreeze.libs.common.AppGlobalBag;
import it.rainbowbreeze.libs.common.BaseResultOperation;
import it.rainbowbreeze.libs.common.ServiceLocator;
import it.rainbowbreeze.libs.log.BaseLogFacility;
import it.rainbowbreeze.libs.logic.BaseCrashReporter;
import it.rainbowbreeze.libs.media.BaseImageMediaHelper;
import it.rainbowbreeze.webcamholmes.R;
import it.rainbowbreeze.webcamholmes.data.AppPreferencesDao;
import it.rainbowbreeze.webcamholmes.data.IImageUrlProvider;
import it.rainbowbreeze.webcamholmes.data.ImageUrlProvider;
import it.rainbowbreeze.webcamholmes.data.ItemsDao;
import it.rainbowbreeze.webcamholmes.logic.LogicManager;
import it.rainbowbreeze.webcamholmes.ui.ActivityHelper;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import static it.rainbowbreeze.libs.common.ContractHelper.*;

/**
 * @author Alfredo "Rainbowbreeze" Morresi
 *
 */
public class App
	extends Application implements AppGlobalBag
{
	//---------- Constructor
	public App()
	{
		super();
		//this is the first instruction, so no fear that mInstance is null is following calls
		mInstance = this;
	}

	//---------- Private fields
	private BaseLogFacility mLogFacility;

	private Class<? extends ImageUrlProvider> mImageUrlProvider;




	//---------- Public properties
	//singleton
    private static App mInstance;
    public static App i()
    { return mInstance; }
    
	/** keys for application preferences */
	public final static String APP_PREFERENCES_KEY = "WebcamHolmesPrefs";

	/** Application name */
	public final static String APP_DISPLAY_NAME = "WebcamHolmes";

	/** Application version displayed to the user (about activity etc) */
	public final static String APP_DISPLAY_VERSION = "0.6b";

	/** Application name used during the ping of update site */
	public final static String APP_INTERNAL_NAME = "WebcamHolmes-Android";
    
	/** Application version for internal use (update, crash report etc) */
	public final static String APP_INTERNAL_VERSION = "00.05.00b";

	/** address where send log */
	public final static String EMAIL_FOR_LOG = "devel@rainbowbreeze.it";
	
	/** Tag to use in the log */
	public final static String LOG_TAG = "WebcamHolmes";

	/** url where send statistics about application */
	public final static String STATISTICS_WEBSERVER_URL = "http://www.rainbowbreeze.it/devel/getlatestversion.php";
	
	public final static String WEBCAM_IMAGE_DUMP_FILE = "webcamDump.png";
	

	/** the application was correctly initialized */
    private boolean mIsCorrectlyInitialized;
	public boolean isCorrectlyInitialized()
	{ return mIsCorrectlyInitialized; }
	public void setCorrectlyInitialized(boolean newValue)
	{ mIsCorrectlyInitialized = newValue; }

	/** First run after an update of the application */
	protected boolean mFirstRunAfterUpdate;
	public void setFirstRunAfterUpdate(boolean newValue)
	{ mFirstRunAfterUpdate = newValue; }
	public boolean isFirstRunAfterUpdate()
	{ return mFirstRunAfterUpdate; }



    
	//---------- Events
	@Override
	public void onCreate() {
		super.onCreate();

		//initialize (and automatically register) crash reporter
		BaseCrashReporter crashReport = new BaseCrashReporter(getApplicationContext());
		ServiceLocator.put(crashReport);
		
		//set the log tag
		mLogFacility = new BaseLogFacility(LOG_TAG);
		ServiceLocator.put(mLogFacility);
		mLogFacility.i("App started");
		
		//create services and helper respecting IoC dependencies
		ItemsDao itemsDao = new ItemsDao(getApplicationContext(), mLogFacility);
		ServiceLocator.put(itemsDao);
		ActivityHelper activityHelper = new ActivityHelper(mLogFacility, getApplicationContext());
		ServiceLocator.put(activityHelper);
		AppPreferencesDao appPreferencesDao = new AppPreferencesDao(getApplicationContext(), APP_PREFERENCES_KEY);
		ServiceLocator.put(appPreferencesDao);
		BaseImageMediaHelper imageMediaHelper = new BaseImageMediaHelper(mLogFacility);
		ServiceLocator.put(imageMediaHelper);
		LogicManager logicManager = new LogicManager(mLogFacility, appPreferencesDao, this, APP_INTERNAL_VERSION, itemsDao);
		ServiceLocator.put(logicManager);
		
		mImageUrlProvider = ImageUrlProvider.class;

	}

	@Override
	public void onTerminate() {
		LogicManager logicManager = checkNotNull(ServiceLocator.get(LogicManager.class), "LogicManager");
		//execute end tasks
		BaseResultOperation<Void> res = logicManager.executeEndTast(this);
		if (res.hasErrors()) {
			ServiceLocator.get(ActivityHelper.class).reportError(this, res.getException(), res.getReturnCode());
		}
		super.onTerminate();
	}
    
    
    
	//---------- Public methods

	
	/**
	 * Set a new {@link IImageUrlProvider} class to return
	 * when the getImageUrlProvider method is called.
	 * 
	 * @param newValue
	 */
	public void setMockImageUrlProvider(Class<? extends ImageUrlProvider> newValue)
	{
		mImageUrlProvider = newValue;
	}
	
	/**
	 * Factory method to obtain a {@link IImageUrlProvider} object.
	 * The object has one shot.
	 *  
	 * @return
	 * @throws InstantiationException 
	 * @throws IllegalAccessException 
	 */
	public IImageUrlProvider getImageUrlProvider()
		throws IllegalAccessException, InstantiationException
	{
		return mImageUrlProvider.newInstance();
	}
	
	/**
	 * The error bitmap to display when webcam image isn't available
	 * @return
	 */
	protected Bitmap mFailBitmap;
	public Bitmap getFailWebcamBitmap() {
		if (null == mFailBitmap)
			mFailBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.no_connection);
		return mFailBitmap;
	}
	
    
    


	//---------- Private methods
}

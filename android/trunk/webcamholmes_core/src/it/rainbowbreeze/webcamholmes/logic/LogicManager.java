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
package it.rainbowbreeze.webcamholmes.logic;

import java.io.File;

import it.rainbowbreeze.libs.R;
import it.rainbowbreeze.libs.common.RainbowResultOperation;
import it.rainbowbreeze.libs.logic.RainbowLogicManager;
import it.rainbowbreeze.webcamholmes.common.LogFacility;
import it.rainbowbreeze.webcamholmes.common.ResultOperation;
import it.rainbowbreeze.webcamholmes.data.AppPreferencesDao;
import it.rainbowbreeze.webcamholmes.data.ItemsDao;
import android.content.Context;
import android.text.TextUtils;

import static it.rainbowbreeze.libs.common.RainbowContractHelper.*;

/**
 * @author Alfredo "Rainbowbreeze" Morresi
 */
public class LogicManager extends RainbowLogicManager {

	//---------- Private fields
	private ItemsDao mItemsDao;
	private AppPreferencesDao mAppPreferencesDao;
	
	

	//---------- Constructor
	/**
	 * @param logFacility
	 * @param appPreferencesDao
	 * @param globalBag
	 * @param currentAppVersion
	 * @param itemsDao
	 */
	public LogicManager(
			LogFacility logFacility,
			AppPreferencesDao appPreferencesDao,
            String currentAppVersion,
			ItemsDao itemsDao)
	{
		super(logFacility, appPreferencesDao, currentAppVersion);
		mItemsDao = checkNotNull(itemsDao);
		mAppPreferencesDao = appPreferencesDao;
	}




	//---------- Public properties




	//---------- Public methods
	/* (non-Javadoc)
	 * @see it.rainbowbreeze.libs.logic.BaseLogicManager#executeBeginTask(android.content.Context)
	 */
	@Override
	public RainbowResultOperation<Void> executeBeginTasks(Context context) {
		RainbowResultOperation<Void> res;

		res = super.executeBeginTasks(context);

		if (res.hasErrors()) {
			//TODO
		}

		//remove temporary resources not removed by previous application shutdown
		res = deleteTempResources(context);
		
		//TODO remove when tests finish
		if (mItemsDao.isDatabaseEmpty()) {
			//test deleted all webcams
			createSystemWebcam(context);
		}
		
		return res;
	}


	/* (non-Javadoc)
	 * @see it.rainbowbreeze.libs.logic.BaseLogicManager#executeEndTast(android.content.Context)
	 */
	@Override
	public RainbowResultOperation<Void> executeEndTasks(Context context) {
		RainbowResultOperation<Void> res;
		
		res = super.executeEndTasks(context);
		if (res.hasErrors()) {
			//TODO
		}
		
		res = deleteTempResources(context);
		
		return res;
	}


	/**
	 * Remove temp resources
	 * 
	 * @param context
	 * @param res
	 * @return
	 */
	public RainbowResultOperation<Void> deleteTempResources(Context context) {
		RainbowResultOperation<Void> res = new ResultOperation<Void>();
		
		int resourcesRemoved = 0;
		String[] resourcesToRemove = mAppPreferencesDao.getResourcesToRemove();
		
		for (int i=0; i<resourcesToRemove.length; i++) {
			boolean deleted = false;
			String resource = resourcesToRemove[i];
			//find if the file is an absolute path or if a file inside app private storage folder
			if (resource.contains(File.separator)) {
				File file = new File(resource);
				deleted = file.delete();
			} else {
				deleted = context.deleteFile(resource);
			}
			if (deleted) {
				resourcesToRemove[i] = "";
				resourcesRemoved++;
			}
		}
		
		//compact resources not removed
		int resourcesToRemoveNewLength = resourcesToRemove.length - resourcesRemoved;
		if (0 == resourcesToRemoveNewLength) {
			mAppPreferencesDao.cleanResourcesToRemove();
		} else {
			String[] newResources = new String[resourcesToRemoveNewLength];
			int index = 0;
			for (String resource:resourcesToRemove) {
				if (!TextUtils.isEmpty(resource)) newResources[index++] = resource;
			}
			mAppPreferencesDao.setResourcesToRemove(newResources);
		}
		
		return res;
	}

	
	
	
	//---------- Private methods
	/* (non-Javadoc)
	 * @see it.rainbowbreeze.libs.logic.BaseLogicManager#executeUpgradeTasks(java.lang.String)
	 */
	@Override
	protected RainbowResultOperation<Void> executeUpgradeTasks(Context context, String startingAppVersion) {
		
		RainbowResultOperation<Void> res = createSystemWebcam(context);
		return res;
	}
	

	/**
	 * Creates webcam for version 01.00.00 of the app
	 */
	private RainbowResultOperation<Void> createSystemWebcam(Context context) {
//		ItemCategory category;
//		long categoryId;

		mBaseLogFacility.v("Deleting old system webcams");
		mItemsDao.clearDatabaseComplete();

		mBaseLogFacility.v("Adding new system webcams");
		
		//add new item to the list
		ResultOperation<Integer> res = mItemsDao.importFromResource(context, R.xml.items);
		
		if (res.hasErrors()) {
			return new RainbowResultOperation<Void>();
		}
		
		//update current selected category
		mAppPreferencesDao.setLatestCategory(0);
		mAppPreferencesDao.save();
		
		return new RainbowResultOperation<Void>();

//		
//		categoryId = mItemsDao.insertCategory(ItemCategory.Factory.getSystemCategory(0, "Traffic - Italy"));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "A4 Torino-Trieste uscita Bergamo", "http://get.edidomus.it/vp/cam1/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "A14 Trezzo sull'Adda", "http://get.edidomus.it/vp/cam23/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Est Rubattino", "http://get.edidomus.it/vp/cam4/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Est Mecenate", "http://get.edidomus.it/vp/cam6/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Est Carugate", "http://get.edidomus.it/vp/cam8/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Est Cologno Monzese", "http://get.edidomus.it/vp/cam9/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Est Palmanova", "http://get.edidomus.it/vp/cam10/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Ovest San Giuliano", "http://get.edidomus.it/vp/cam3/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Ovest raccordo A7", "http://get.edidomus.it/vp/cam5/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Ovest Vecchia Vigevanese", "http://get.edidomus.it/vp/cam2/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Ovest Nuova Vigevanese", "http://get.edidomus.it/vp/cam7/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Milano - Tang Ovest uscita SS11 Novara", "http://get.edidomus.it/vp/cam12/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "A4 Torino-Trieste (Cinisello)", "http://get.edidomus.it/vp/cam20/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "A8 Milano-Varese bivio con A9", "http://get.edidomus.it/vp/cam18/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "A8 Milano-Varese direzione Varese", "http://get.edidomus.it/vp/cam19/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "A8 Cerro Maggiore direzione Milano", "http://get.edidomus.it/vp/cam21/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Torino - uscita Corso Francia", "http://get.edidomus.it/vp/cam24/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Torino - A32 zona Rivoli direz Frejus", "http://get.edidomus.it/vp/cam25/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Torino - A32 zona Rivoli direz Tang. O", "http://get.edidomus.it/vp/cam26/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Autofiori - Savona", "http://get.edidomus.it/vp/cam33/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Autofiori - Finale Ligure", "http://get.edidomus.it/vp/cam32/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Autofiori - Albenga", "http://get.edidomus.it/vp/cam31/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Autofiori - Ventimiglia", "http://get.edidomus.it/vp/cam30/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Firenze - via De Nicola", "http://get.edidomus.it/vp/cam14/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Firenze - via Senese", "http://get.edidomus.it/vp/cam16/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Firenze - via Baccio", "http://get.edidomus.it/vp/cam15/image.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Firenze - via Rosselli", "http://get.edidomus.it/vp/cam13/image.jpg", 5));
//
//		categoryId = mItemsDao.insertCategory(ItemCategory.Factory.getSystemCategory(0, "Italy places"));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(AO) Valtournenche - piste da sci", "http://www.regione.vda.it/Bollettino_neve/Images/valtour.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(BO) Bologna - piazza Le Due Torri", "http://www.baskerville.it/webcam/live.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(CT) Etna", "http://www.albanetcom.com/etnaimg/cam_00001.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(GE) Genova - Piazza Verdi", "http://www.tu6genova.it/immagini/13.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(GE) Genova - via Diaz", "http://www.tu6genova.it/immagini/4.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(GE) Genova - corso Europa", "http://www.tu6genova.it/immagini/16.jpg", 60));

//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(FO) Foligno panoramica", "http://www.umbriameteo.com/web2/images/last640.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(LT) Ponza panoramica", "http://www.agropontino.it/webcam/test71.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(IM) Ventimiglia - Torre comunale", "http://www.comune.ventimiglia.it/webcamcomune/webcam.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(IM) Ventimiglia - Forte dell'Annunziata", "http://www.comune.ventimiglia.it/webcamcomune/museo.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(MC) Monte Prata", "http://www.meteoappennino.it/webprata/images/last1024.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(MC) Frontignano", "http://www.meteoappennino.it/webfrontignano/images/zoom_b1024.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(NA) Napoli - Golfo e Vesuvio", "http://www.salernometeo.it/Webcam/napoli/currentsmall.jpg", 30));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(NA) Ischia Ponte", "http://www.ischiaonline.it/cams/heuropa/heuropa.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(PG) Castelluccio di Norcia - Monte Vettore", "http://www.umbriameteo.com/web1/images/last800.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(PG) Perugia, piazza IV Novembre", "http://www.comune.perugia.it/livecams/hugesize.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(PU) Monte Nerone", "http://www.meteoappennino.it/webmontenerone/images/montenerone_1024.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(RM) Roma - panoramica San Piertro", "http://www.barcello.it/images/meteo/axis.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(RO) Adria - Circuito internazionale", "http://80.206.235.242/record/current.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(SO) Passo dello Stelvio - funivia", "http://jpeg.popso.it/webcam/webcam_online/stelviolive_01.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(TE) Prati di Tivo", "http://www.meteoappennino.it/webpratiditivo/images/pratiditivo_zoom_b1024.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(TE) Prato Selva", "http://www.meteoappennino.it/webpratoselva/images/pratoselva_1024.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(TO) Torino - Mole Antonelliana", "http://danielemeteo.altervista.org/webcam.php", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(TR) Trieste - porto", "http://151.8.71.28/~www/COMMON/WEBCAM/WebcamTrieste.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(TR) Trieste piazza Verdi", "http://www.commissariato.fvg.it/webcam/piazzaverdi.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(PI) Pisa - Torre dell'Orologio", "http://www.comune.pisa.it/webcam/img/pisa.jpg", 5));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(SI) San Gimignano - Piazza dell'Erbe", "http://www.divineria.it/IPwwebcam/webcam.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "(VE) Venezia - Canal Grande", "http://turismo.regione.veneto.it/webcam/huge.jpg", 60));
//		//mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "", "", 60));
//
//		categoryId = mItemsDao.insertCategory(ItemCategory.Factory.getSystemCategory(0, "Beautiful places"));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Canarie - Jandia Beach", "http://www.restaurantecoronado.com/webcams/coronadobeach2.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Canada - Niagara Waterfalls", "http://www.fallsview.com/Stream/camera0.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Australia - Parkers Radio Telescope", "http://outreach.atnf.csiro.au/visiting/parkes/webcam/parkes.full.jpg", 30));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "South Dakota - Mount Rushmore", "http://media.sd.gov:88/webcam/rushmore_00001.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "St. Barths - St-Jean's Bay", "http://www.st-barths.com/a_cam/view1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Yosemite - Glacier Point", "http://www.yosemite.org/vryos/turtleback1.jpg", 60));

//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Grand Canyon - Yavapai Point", "http://www2.nature.nps.gov/air/webcams/parks/grcacam/grca.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Yellow Stone - Old Faithfull Geyser", "http://64.241.25.110/yell/webcams/oldfaith2.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Pikes Peak Cam", "http://www.pikespeakcam.com/images/cam.jpg", 60));
//		//mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "", "", 60));
//
//
//		categoryId = mItemsDao.insertCategory(ItemCategory.Factory.getSystemCategory(0, "World Cities"));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Honk Kong - the Peak", "http://www.discoverhongkong.com/eng/interactive/webcam/images/ig_webc_peak1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Honk Kong - Victoria Harbour", "http://www.discoverhongkong.com/eng/interactive/webcam/images/ig_webc_harb1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Hong Kong - skyline from Admiralty", "http://www.discoverhongkong.com/eng/interactive/webcam/images/ig_webc_petr1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Shangai - Grand Gateway", "http://www.vuille.com/photo.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Shanghai - panorama", "http://www.ds-shanghai.org.cn/webcam/image.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Paris - Tour Eiffel", "http://www.parislive.net/eiffelwebcam1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Paris - Tour Eiffel big", "http://www.parislive.net/eiffelcam3.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Paris - Pantheon", "http://webcam.ville.woob2.com/Pantheon_full.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Parigi - panorama", "http://www-compat.tf1.fr/webcam/file222.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "New York - Fifth Avenue", "http://www.mte.com/webcam/5thave.jpg", 30));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "New York - Manhattan", "http://www.nyvibe.net/nyvibescam/view.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "London - Covent Garden", "http://londonwebcam.virtual-london.com/cam.jpg", 30));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Barcelona - panorama", "http://www.tvcatalunya.com/webcams/noves/arts2.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Dallas - skyline", "http://www.wfaa.com/sharedcontent/dws/img/standing/cams/wfaahuge.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Moscow - Kremlin", "http://webcam.mdmbank.ru/webcam/images/fullsize.jpg", 60));		
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Monaco di Baviera - Marienplatz webcam", "http://www.muenchner-freiheit.net/fiveminutes/marienplatz-1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Dublino - O'Connell Bridge", "http://www.ireland.com/includes/webcam/liveview.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Vienna - Rathausplatz", "http://www.wien.gv.at/camera/rathausplatz.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Riyadh (K.S.A.) - King Fahd Road", "http://my.saudi.net.sa/IMAGES/road.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Toronto skyline", "http://www.2ontario.com/webcam/oissouth.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Ibiza - panorama", "http://www.toibiza.com/html/images/webcam/capture1.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Buenos Aire - sky view", "http://itaucam.itau.com.ar/fullsize.jpg", 60));
//		mItemsDao.insertWebcam(ItemWebcam.Factory.getSystemWebcam(categoryId, "Washington, D.C.", "http://media.washingtonpost.com/media/webcams/webcam32.jpg", 60));
	}
}

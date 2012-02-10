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

/**
 * 
 */

package it.rainbowbreeze.webcamholmes.data;

import java.util.List;

import it.rainbowbreeze.webcamholmes.R;
import it.rainbowbreeze.webcamholmes.common.ResultOperation;
import it.rainbowbreeze.webcamholmes.domain.ItemCategory;
import it.rainbowbreeze.webcamholmes.domain.ItemToDisplay;
import it.rainbowbreeze.webcamholmes.domain.ItemWebcam;
import android.test.AndroidTestCase;
import android.text.TextUtils;

/**
 * @author Alfredo "Rainbowbreeze" Morresi
 *
 */
public class ItemsXmlParserTest extends AndroidTestCase {
	//---------- Constructor




	//---------- Private fields
	private ItemsXmlParser mParser;

	
	
	
	//---------- Test initialization
	/* (non-Javadoc)
	 * @see android.test.AndroidTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mParser = new ItemsXmlParser();
	}

	
	
	
	//---------- Test cases	
	public void testParseResource() {
		ResultOperation<List<ItemToDisplay>> res;
		List<ItemToDisplay> elements;
		ItemToDisplay itemToDisplay;
		ItemWebcam webcam;
		ItemCategory category;
		
		res = mParser.parseResource(getContext(), R.xml.testimportdata1);
		assertFalse("Operation has error", res.hasErrors());
		
		elements = res.getResult();
		assertNotNull("No elements", elements);
		assertEquals("Wrong number of returned elements", 6, elements.size());
		
		//first elements
		itemToDisplay = elements.get(0);
		assertTrue("Item isn't a webcam", itemToDisplay instanceof ItemWebcam);
		webcam = (ItemWebcam) itemToDisplay;
        assertEquals("Wrong id", 0, webcam.getId());
        assertEquals("Wrong parentId", 0, webcam.getParentId());
        assertEquals("Wrong parent alias Id", 0, webcam.getParentAliasId());
        assertEquals("Wrong name", "Root Element 1", webcam.getName());
        assertEquals("Wrong webcam type", 1, webcam.getType());
        assertEquals("Wrong url", "http://www.sitewebcam.org/imageroot1.jpg", webcam.getImageUrl());
        assertEquals("Wrong interval", 0, webcam.getReloadInterval());
        assertFalse("Wrong preferred", webcam.isPreferred());
        assertFalse("Wrong user created", webcam.isUserCreated());		
		assertTrue("Wrong freeData1", TextUtils.isEmpty(webcam.getFreeData1()));
		assertTrue("Wrong freeData2", TextUtils.isEmpty(webcam.getFreeData2()));
		assertTrue("Wrong freeData3", TextUtils.isEmpty(webcam.getFreeData3()));
		
        //second elements
		itemToDisplay = elements.get(1);
		assertTrue("Item isn't a webcam", itemToDisplay instanceof ItemWebcam);
		webcam = (ItemWebcam) itemToDisplay;
        assertEquals("Wrong id", 0, webcam.getId());
        assertEquals("Wrong parentId", 0, webcam.getParentId());
        assertEquals("Wrong parent alias Id", 0, webcam.getParentAliasId());
        assertEquals("Wrong name", "Root Element 2", webcam.getName());
        assertEquals("Wrong webcam type", 1, webcam.getType());
        assertEquals("Wrong url", "http://www.sitewebcam.org/imageroot2.jpg", webcam.getImageUrl());
        assertEquals("Wrong interval", 60, webcam.getReloadInterval());
        assertTrue("Wrong preferred", webcam.isPreferred());
        assertFalse("Wrong user created", webcam.isUserCreated());		
		assertTrue("Wrong freeData1", TextUtils.isEmpty(webcam.getFreeData1()));
		assertTrue("Wrong freeData2", TextUtils.isEmpty(webcam.getFreeData2()));
		assertTrue("Wrong freeData3", TextUtils.isEmpty(webcam.getFreeData3()));
		
        //third elements
		itemToDisplay = elements.get(2);
		assertTrue("Item isn't a category", itemToDisplay instanceof ItemCategory);
		category = (ItemCategory) itemToDisplay;
        assertEquals("Wrong id", 0, category.getId());
        assertEquals("Wrong aliasId", 100, category.getAliasId());
        assertEquals("Wrong parentId", 0, category.getParentId());
        assertEquals("Wrong parent aliasId", 0, category.getParentAliasId());
        assertEquals("Wrong name", "Traffic", category.getName());
        assertTrue("Wrong user created", category.isUserCreated());		

        //forth elements
		itemToDisplay = elements.get(3);
		assertTrue("Item isn't a webcam", itemToDisplay instanceof ItemWebcam);
		webcam = (ItemWebcam) itemToDisplay;
        assertEquals("Wrong id", 0, webcam.getId());
        assertEquals("Wrong parentId", 0, webcam.getParentId());
        assertEquals("Wrong parent alias Id", 100, webcam.getParentAliasId());
        assertEquals("Wrong name", "Paris - Tour Eiffel", webcam.getName());
        assertEquals("Wrong webcam type", 1, webcam.getType());
        assertEquals("Wrong url", "http://www.parislive.net/eiffelwebcam01.jpg", webcam.getImageUrl());
        assertEquals("Wrong interval", 5, webcam.getReloadInterval());
        assertFalse("Wrong preferred", webcam.isPreferred());
        assertFalse("Wrong user created", webcam.isUserCreated());
		assertTrue("Wrong freeData1", TextUtils.isEmpty(webcam.getFreeData1()));
		assertTrue("Wrong freeData2", TextUtils.isEmpty(webcam.getFreeData2()));
		assertTrue("Wrong freeData3", TextUtils.isEmpty(webcam.getFreeData3()));

        //fifth elements
		itemToDisplay = elements.get(4);
		assertTrue("Item isn't a category", itemToDisplay instanceof ItemWebcam);
		webcam = (ItemWebcam) itemToDisplay;
        assertEquals("Wrong id", 0, webcam.getId());
        assertEquals("Wrong parentId", 0, webcam.getParentId());
        assertEquals("Wrong parent aliasId", 100, webcam.getParentAliasId());
        assertEquals("Wrong name", "Paris from webcam.travel", webcam.getName());
        assertEquals("Wrong webcam type", 2, webcam.getType());
        assertEquals("Wrong url", "http://webcam.travel/2323523.jpg", webcam.getImageUrl());
        assertEquals("Wrong interval", 30, webcam.getReloadInterval());
        assertFalse("Wrong preferred", webcam.isPreferred());
        assertFalse("Wrong user created", webcam.isUserCreated());
		assertEquals("Wrong freeData1", "User2", webcam.getFreeData1());
		assertEquals("Wrong freeData2", "http://forum.webcam.travel/user2", webcam.getFreeData2());
		assertEquals("Wrong freeData3", "http://webcam.travel/13243432", webcam.getFreeData3());

        //sixth elements
		itemToDisplay = elements.get(5);
		assertTrue("Item isn't a category", itemToDisplay instanceof ItemCategory);
		category = (ItemCategory) itemToDisplay;
        assertEquals("Wrong id", 0, category.getId());
        assertEquals("Wrong aliasId", 200, category.getAliasId());
        assertEquals("Wrong parentId", 0, category.getParentId());
        assertEquals("Wrong parent aliasId", 100, category.getParentAliasId());
        assertEquals("Wrong name", "Major road", category.getName());
        assertTrue("Wrong user created", category.isUserCreated());		

	}

	

	
	//---------- Private methods

}

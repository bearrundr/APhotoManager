/*
 * Copyright (c) 2017 by k3b.
 *
 * This file is part of AndroFotoFinder / #APhotoManager
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>
 */

package de.k3b.media;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import de.k3b.io.DateUtil;
import de.k3b.io.ListUtils;

/**
 * Created by k3b on 28.03.2017.
 */

public class ImageMetaReaderIntegrationTests {
    private static final Logger logger = LoggerFactory.getLogger(ImageMetaReaderIntegrationTests.class);

    private IMetaApi sut = null;
    @Before
    public void setup() throws IOException {
        ImageMetaReader.DEBUG = true;
        sut = getMeta("test-WitExtraData.jpg");
    }

    @Test
    public void shouldDump() throws IOException
    {
        // System.out.printf(sut.toString());
        logger.info(sut.toString());
    }

    @Test
    public void shouldGetDescription() throws IOException
    {
        Assert.assertEquals("ImageDescription", sut.getDescription());
    }

    @Test
    public void shouldGetTitle() throws IOException
    {
        Assert.assertEquals("XPTitle", sut.getTitle());
    }

    @Test
    public void shouldGetDateTimeTaken() throws IOException
    {
        Assert.assertEquals("1962-11-07T09:38:46", DateUtil.toIsoDateString(sut.getDateTimeTaken()));
    }

    @Test
    public void shouldGetLatitude() throws IOException
    {
        Assert.assertEquals(27.8186, sut.getLatitude(), 0.01);
    }
    @Test
    public void shouldGetLongitude() throws IOException
    {
        Assert.assertEquals(-15.764, sut.getLongitude(), 0.01);
    }

    @Test
    public void shouldGetTags() throws IOException
    {
        Assert.assertEquals("Marker1, Marker2", ListUtils.toString(sut.getTags(),", "));
    }

    @Test
    public void shouldGetRating() throws IOException
    {
        Assert.assertEquals(3, sut.getRating().intValue());
    }

    protected IMetaApi getMeta(String fileName) throws IOException {
        InputStream inputStream = ImageMetaReaderIntegrationTests.class.getResourceAsStream("images/" + fileName);
        Assert.assertNotNull("open images/" + fileName, inputStream);
        IMetaApi result = new ImageMetaReader().load(fileName, inputStream, null, "JUnit");
        return result;
    }
}
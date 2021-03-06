/*
 * Copyright (c) 2015-2017 by k3b.
 *
 * This file is part of AndroFotoFinder / #APhotoManager.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import de.k3b.FotoLibGlobal;
import de.k3b.transactionlog.TransactionLoggerBase;

/**
 * apply meta data changes to jpg and/or xmp file.
 *
 * Created by k3b on 25.08.2015.
 */
public class JpgMetaWorkflow {
    private static final Logger logger = LoggerFactory.getLogger(FotoLibGlobal.LOG_TAG);

    private static StringBuilder debugExif(StringBuilder sb, String context, MetaWriterExifXml exif, File filePath) {
        if (sb != null) {
            sb.append("\n\t").append(context).append("\t: ");

            if (exif != null) {
                if (exif.getExif() != null) {
                    sb.append(exif.getExif().getDebugString(" "));
                } else {
                    sb.append(MediaUtil.toString(exif, false, MediaUtil.FieldID.path));
                }
            }
        }
        return sb;
    }

    public static MetaWriterExifXml saveLatLon(File filePath, Double latitude, Double longitude) {
        IMetaApi changedData = new MediaDTO().setLatitudeLongitude(latitude, longitude);
        MediaDiffCopy metaDiffCopy = new MediaDiffCopy()
                .setDiff(changedData, MediaUtil.FieldID.latitude_longitude);
        MetaWriterExifXml exif = applyChanges(filePath, metaDiffCopy, null);
        metaDiffCopy.close();
        return exif;
    }

    /** writes either (changes + _affectedFields) or metaDiffCopy to jpg/xmp-filePath.
     * Returns new values or null if no change. */
    public static MetaWriterExifXml applyChanges(File filePath,
                                                 MediaDiffCopy metaDiffCopy, TransactionLoggerBase logger) {
        StringBuilder sb = (FotoLibGlobal.debugEnabled)
                ? createDebugStringBuilder(filePath)
                : null;
        if (filePath.canWrite()) {
            MetaWriterExifXml exif = null;
            try {
                long lastModified = filePath.lastModified();
                exif = MetaWriterExifXml.create (filePath.getAbsolutePath(), "MetaWriterExifXml: load");
                debugExif(sb, "old", exif, filePath);
                List<String> oldTags = exif.getTags();

                List<MediaUtil.FieldID> changed = metaDiffCopy.applyChanges(exif);

                if (changed != null) {
                    debugExif(sb, "assign ", exif, filePath);

                    exif.save("MetaWriterExifXml save");

                    if (FotoLibGlobal.preserveJpgFileModificationDate) {
                        // preseve file modification date
                        filePath.setLastModified(lastModified);
                    }
                    if (sb != null) {
                        MetaWriterExifXml exifVerify = MetaWriterExifXml.create (filePath.getAbsolutePath(),
                                "dbg in MetaWriterExifXml", true, true, false);
                        debugExif(sb, "new ", exifVerify, filePath);
                    }

                    if(logger != null) {
                        logger.addChanges(exif, EnumSet.copyOf(changed), oldTags);
                    }
                } else {
                    if (sb != null) sb.append("no changes ");
                    exif = null;
                }

                if (sb != null) {
                    JpgMetaWorkflow.logger.info(sb.toString());
                }
                return exif;
            } catch (IOException e) {
                if (sb == null) {
                    sb = createDebugStringBuilder(filePath);
                    debugExif(sb, "err content", exif, filePath);
                }

                sb.append("error='").append(e.getMessage()).append("' ");
                JpgMetaWorkflow.logger.error(sb.toString(), e);
                return null;
            }
        } else {
            if (sb == null) {
                sb = createDebugStringBuilder(filePath);
            }

            sb.append("error='file is write protected' ");
            JpgMetaWorkflow.logger.error(sb.toString());
            return null;
        }
    }

    private static StringBuilder createDebugStringBuilder(File filePath) {
        return new StringBuilder("Set Exif to file='").append(filePath.getAbsolutePath()).append("'\n\t");
    }


    // Translate exif-orientation code (0..8) to exifOrientationCode2RotationDegrees (clockwise)
    // http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/EXIF.html
    private static final short[] exifOrientationCode2RotationDegrees = {
            0,     // EXIF Orientation constants:
            0,     // 1 = Horizontal (normal)
            0,     // 2 = (!) Mirror horizontal
            180,   // 3 = Rotate 180
            180,   // 4 = (!) Mirror vertical
            90,    // 5 = (!) Mirror horizontal and rotate 270 CW
            90,    // 6 = Rotate 90 CW
            270,   // 7 = (!) Mirror horizontal and rotate 90 CW
            270};  // 8 = Rotate 270 CW

    /**
     * Get necessary rotation for image file from exif.
     *
     * @param fullPathToImageFile The filename.
     * @return right-rotate (in degrees) image according to exifdata.
     */
    public static int getRotationFromExifOrientation(String fullPathToImageFile) {
        try {
            ExifInterfaceEx exif = new ExifInterfaceEx(fullPathToImageFile, null, null, "getRotationFromExifOrientation");
            int orientation = exif.getAttributeInt(ExifInterfaceEx.TAG_ORIENTATION, 0);
            if ((orientation >= 0) && (orientation < exifOrientationCode2RotationDegrees.length))
                return exifOrientationCode2RotationDegrees[orientation];
        }
        catch (Exception e) {
        }
        return 0;
    }


}

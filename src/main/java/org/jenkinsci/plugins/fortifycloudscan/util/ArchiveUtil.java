/*
 * This file is part of Fortify CloudScan Jenkins plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jenkinsci.plugins.fortifycloudscan.util;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveUtil {

    private ArchiveUtil() {}


    public static void unzip(File directory, File zipFile) throws FileNotFoundException, IOException {
        if(!directory.exists()) {
            directory.mkdirs();
        }
        byte[] buffer = new byte[2048];

        FileInputStream fInput = null;
        ZipInputStream zipInput = null;
        try {
            fInput = new FileInputStream(zipFile);
            zipInput = new ZipInputStream(fInput);
            ZipEntry entry = zipInput.getNextEntry();
            while (entry != null) {
                String entryName = entry.getName();
                File file = new File(directory.getAbsolutePath() + File.separator + entryName);
                if (entry.isDirectory()) {
                    File newDir = new File(file.getAbsolutePath());
                    if (!newDir.exists()) {
                        newDir.mkdirs();
                    }
                } else {
                    if (!file.getParentFile().isDirectory() && !file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    FileOutputStream fOutput = new FileOutputStream(file);
                    int count;
                    while ((count = zipInput.read(buffer)) > 0) {
                        fOutput.write(buffer, 0, count);
                    }
                    fOutput.close();
                }
                zipInput.closeEntry();
                entry = zipInput.getNextEntry();
            }
            zipInput.closeEntry();
            zipInput.close();
            fInput.close();
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(e.getMessage());
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            try { zipInput.closeEntry(); } catch (IOException e) {}
            IOUtils.closeQuietly(zipInput);
            IOUtils.closeQuietly(fInput);
        }
    }

}

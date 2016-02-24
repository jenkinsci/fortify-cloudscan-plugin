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
package org.jenkinsci.plugins.fortifycloudscan;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.fortifycloudscan.util.ArchiveUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class RulepackResolver {

    private ConsoleLogger logger;
    private String tempDir;

    public RulepackResolver(ConsoleLogger logger) {
        this.logger = logger;
        tempDir = System.getProperty("java.io.tmpdir");
    }

    /**
     * Locally resolve a rulepack from the specified location. The location is a String representing
     * the path to the rulepack on the filesystem, or a remote URL. In the case of a URL, the rulepack
     * will be downloaded to a temporary directory first.
     * @param location a filesystem or URL location
     * @return a File object containing the resolved file. Null if file is not resolved
     */
    public File resolve(String location) {
        try {
            URL url = new URL(location);
            File download = download(url);
            if (download != null) {
                return extractArchive(download);
            }
        } catch (MalformedURLException e) {
            File file = new File(location);
            if (file.exists()) {
                return file;
            }
        }
        return null;
    }

    /**
     * Download a file from the specified URL and place into a temporary directory.
     * Attempts to guess the filename from the HTTP response. If not successful, the
     * filename will be derived from the URL.
     * @param url the URL to download the file from
     * @return a File object where the downloaded file is saved
     */
    private File download(URL url) {
        String urlString = url.toExternalForm();
        File temp = new File(tempDir + File.separator +
                FortifyCloudScanPlugin.PLUGIN_NAME + File.separator + UUID.randomUUID());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(urlString);

        CloseableHttpResponse response;
        File downloadedFile;
        try {
            logger.log("Downloading rulepack from " + urlString);
            response = httpclient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                if (temp.mkdirs()) {
                    logger.log("Created temporary rulepack download directory");
                }
                String suggestedFilename = getSuggestedFilename(response);
                String filename = (suggestedFilename != null) ? suggestedFilename : FilenameUtils.getName(urlString);
                downloadedFile = new File(temp + File.separator + filename);
            } else {
                logger.log("ERROR: Remote file cannot be downloaded");
                logger.log("ERROR: Status Code: " + response.getStatusLine().getStatusCode() + " - " + response.getStatusLine().getReasonPhrase());
                return null;
            }
        } catch (IOException e) {
            logger.log("ERROR: An error occurred while attempting to download rulepack");
            logger.log(e.getMessage());
            return null;
        }
        HttpEntity entity = response.getEntity();
        try {
            if (entity != null) {
                FileOutputStream outstream = new FileOutputStream(downloadedFile);
                entity.writeTo(outstream);
                logger.log("Rulepack saved to " + downloadedFile.getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            logger.log("ERROR: The download file location cannot be found");
            logger.log(e.getMessage());
        } catch (IOException e) {
            logger.log("ERROR: An error occurred while saving the rulepack");
            logger.log(e.getMessage());
        }
        return downloadedFile;
    }

    /**
     * Attempts to retrieve the filename specified from the HTTP response header.
     * @param response the HTTP response to parse
     * @return the suggested filename parses from the HTTP header. Returns null if parsing is not successful or header is not present.
     */
    private String getSuggestedFilename(HttpResponse response) {
        Header header = response.getFirstHeader("Content-Disposition");
        if (header == null) {
            return null;
        }
        HeaderElement[] headerElements = header.getElements();
        if (headerElements.length > 0) {
            HeaderElement headerElement = headerElements[0];
            if ("attachment".equalsIgnoreCase(headerElement.getName())) {
                NameValuePair pair = headerElement.getParameterByName("filename");
                if (pair != null) {
                    return pair.getValue();
                }
            }
        }
        return null;
    }

    /**
     * Determines if the specified file is a ZIP archive
     * @param file the file to check
     * @return true if file is a ZIP archive, false if not
     */
    private boolean isArchive(File file) {
        String filename = FilenameUtils.getName(file.getAbsolutePath());
        return FilenameUtils.isExtension(filename, "zip");
    }

    /**
     * If the specified file is a ZIP archive, the archive will be extracted to the
     * parent directory and the original file deleted.
     * @param file the archive to extract
     * @return the parent directory where the file was extracted to
     */
    private File extractArchive(File file) {
        if (!isArchive(file)) {
            return file;
        }
        try {
            logger.log("Extracting rulepack archive");
            File extractedDir = new File(file.getParentFile().getAbsolutePath());
            ArchiveUtil.unzip(extractedDir, file);
            return extractedDir;
        } catch (FileNotFoundException e) {
            logger.log("ERROR: The file to extract could not be found");
            logger.log(e.getMessage());
        } catch (IOException e) {
            logger.log("ERROR: An unknown error occurred while extracting archive");
            logger.log(e.getMessage());
        } finally {
            if (file.delete()) {
                logger.log("Removed original archive");
            }
        }
        return null;
    }

    public void setTempDir(String directory) {
        File file = new File(directory);
        if (!file.exists()) {
            file.mkdirs();
        }
        if (file.isDirectory()) {
            this.tempDir = directory;
        }
    }

}

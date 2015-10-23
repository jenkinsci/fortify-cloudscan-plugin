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

import com.fortify.schema.fws.ActiveProjectVersionListRequestDocument;
import com.fortify.schema.fws.ActiveProjectVersionListResponseDocument;
import com.fortify.schema.fws.ProjectListRequestDocument;
import com.fortify.schema.fws.ProjectListResponseDocument;
import com.fortifysoftware.schema.wsTypes.Project;
import com.fortifysoftware.schema.wsTypes.ProjectVersionLite;
import org.apache.xmlbeans.XmlException;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A wrapper class that queries SSC and returns various supported objects.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifySsc {


    FortifySscClient client;

    public FortifySsc(URL url, String token) {
        this.client = new FortifySscClient(url, token);
    }

    /**
     * Returns a list of all Project defined on SSC.
     */
    public List<Project> getProjects() throws SOAPException, IOException, XmlException, NoSuchFieldException,
            IllegalAccessException, FortifySscClientException {

        ProjectListRequestDocument requestDocument = ProjectListRequestDocument.Factory.newInstance();
        requestDocument.addNewProjectListRequest();
        SOAPMessage soapRequest = client.createSoapMessage(requestDocument);
        SOAPMessage soapResponse = client.callEndpoint(soapRequest);
        ProjectListResponseDocument responseDocument = client.parseMessage(soapResponse, ProjectListResponseDocument.class);
        ProjectListResponseDocument.ProjectListResponse projectList = responseDocument.getProjectListResponse();
        return Arrays.asList(projectList.getProjectArray());
    }

    /**
     * Returns a list of all project versions for the specified project id defined on SSC.
     */
    public List<ProjectVersionLite> getActiveProjectVersions(long projectId) throws SOAPException, IOException,
            XmlException, NoSuchFieldException, IllegalAccessException, FortifySscClientException {

        /*
        todo: investigate is there's a better way to do this. This method queries on all project versions,
        iterates through them and matches the specified project id. There should be a SOAP call that makes
        SSC do the hard work for us.
         */
        List<ProjectVersionLite> projectVersions = new ArrayList<ProjectVersionLite>();
        ActiveProjectVersionListRequestDocument requestDocument = ActiveProjectVersionListRequestDocument.Factory.newInstance();
        requestDocument.addNewActiveProjectVersionListRequest();
        SOAPMessage soapRequest = client.createSoapMessage(requestDocument);
        SOAPMessage soapResponse = client.callEndpoint(soapRequest);
        ActiveProjectVersionListResponseDocument responseDocument = client.parseMessage(soapResponse, ActiveProjectVersionListResponseDocument.class);
        ActiveProjectVersionListResponseDocument.ActiveProjectVersionListResponse activeProjectVersions = responseDocument.getActiveProjectVersionListResponse();
        List<ProjectVersionLite> plist = Arrays.asList(activeProjectVersions.getProjectVersionArray());
        for (ProjectVersionLite projectVersion: plist) {
           if (projectVersion.getProjectId() == projectId) {
               projectVersions.add(projectVersion);
           }
        }
        return projectVersions;
    }

}

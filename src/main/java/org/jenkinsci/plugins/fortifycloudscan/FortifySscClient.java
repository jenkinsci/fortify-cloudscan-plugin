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

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;


/**
 * A Client connector class for communicating with Software Security Center.
 * Supports user and token based authentication. Only minimal error handeling
 * is supported.
 *
 * Note to HP. Please open source wsclient and publish to central. It's a waste
 * of time recreating a client library for SSC when SSC already includes the
 * source code for one, but cannot be distributed because of licensing.
 *
 * @author Steve Springett (steve.springett@owasp.org)
 */
public class FortifySscClient {

    private URL endpointUrl;
    private String sscToken;
    private String sscUsername;
    private String sscPassword;

    /**
     * Constructs a new FortifySscClient object using traditional username/password authentication
     * @param endpointUrl The URL to the Fortify SSC server's SOAP endpoint
     * @param sscUsername The username to authenticate with
     * @param sscPassword The password to authenticate with
     */
    public FortifySscClient(URL endpointUrl, String sscUsername, String sscPassword) {
        this.endpointUrl = endpointUrl;
        this.sscUsername = sscUsername;
        this.sscPassword = sscPassword;
    }

    /**
     * Constructs a new FortifySscClient object using traditional username/password authentication
     * @param endpointUrl The URL to the Fortify SSC server's SOAP endpoint
     * @param sscToken The token to authenticate with
     */
    public FortifySscClient(URL endpointUrl, String sscToken) {
        this.endpointUrl = endpointUrl;
        this.sscToken = sscToken;
    }

    /**
     * Create a new SOAP message from the specified XMLBeans object. The XmlObject
     * will be injected into the SOAP Body.
     * @param xmlObject The XmlObject to create a SOAP message from
     * @return a SOAPMessage containing the contents of the specified XmlObject
     * @throws SOAPException
     */
    public SOAPMessage createSoapMessage(XmlObject xmlObject) throws SOAPException {
        MessageFactory msgFactory = MessageFactory.newInstance();

        SOAPMessage soapMessage = msgFactory.createMessage();
        SOAPPart prt = soapMessage.getSOAPPart();
        SOAPEnvelope env = prt.getEnvelope();
        addWssHeader(env);
        SOAPBody soapBody = env.getBody();
        org.w3c.dom.Node node = xmlObject.getDomNode();
        soapBody.addDocument((Document) node);
        return soapMessage;
    }

    /**
     * Adds a webservices security header containing username/password credentials and
     * optionally a Fortify authentication token
     * @param envelope The soap envelope to add the header to
     * @throws SOAPException
     */
    private void addWssHeader(SOAPEnvelope envelope) throws SOAPException {
        SOAPHeader header;
        if (envelope.getHeader() == null)
            header = envelope.addHeader();
        else
            header = envelope.getHeader();

        if (sscToken != null) {
            header.addAttribute(new QName("xmlns:axis2ns2"), "www.fortify.com/schema");
            header.addAttribute(new QName("axis2ns2:token"), sscToken);
        }

        SOAPElement security = header.addChildElement("Security", "wsse", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");

        SOAPElement usernameToken = security.addChildElement("UsernameToken", "wsse");
        usernameToken.addAttribute(new QName("xmlns:wsu"), "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

        if (sscUsername != null) {
            SOAPElement username = usernameToken.addChildElement("Username", "wsse");
            username.addTextNode(sscUsername);
        }

        if (sscPassword != null) {
            SOAPElement password = usernameToken.addChildElement("Password", "wsse");
            password.setAttribute("Type", "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
            password.addTextNode(sscPassword);
        }
    }

    /**
     * Makes a remote call to the SOAP endpoint.
     * @param soapMessage The SOAP message to send to the endpoint
     * @return a SOAPMessage as a response
     * @throws SOAPException
     * @throws IOException
     */
    public SOAPMessage callEndpoint(SOAPMessage soapMessage) throws SOAPException, IOException {
        SOAPConnectionFactory fact;
        fact = SOAPConnectionFactory.newInstance();
        SOAPConnection con = fact.createConnection();
        SOAPMessage response = con.call(soapMessage, endpointUrl);
        con.close();
        return response;
    }

    /**
     * Parses a SOAP message using XMLBeans and casting the resulting XmlObject to
     * the specified XmlObject implementation class.
     * @param soapMessage The SOAP message to parse
     * @param clazz The XmlObject class (in XMLBeans) for parsing and casting
     * @return A parsed SOAP message as a XmlObject implementation
     */
    public <T> T parseMessage(SOAPMessage soapMessage, Class<T> clazz)
            throws SOAPException, XmlException, NoSuchFieldException, IllegalAccessException, FortifySscClientException {

        XmlObject b = XmlObject.Factory.parse(soapMessage.getSOAPBody().getFirstChild());
        Field typeField = clazz.getDeclaredField("type");
        org.apache.xmlbeans.SchemaType schemaType = (org.apache.xmlbeans.SchemaType)typeField.get(null);
        SOAPFault fault = soapMessage.getSOAPBody().getFault();
        if (fault != null) {
            throw new FortifySscClientException(fault.getFaultString());
        }
        XmlObject c = org.apache.xmlbeans.XmlBeans.getContextTypeLoader().parse(b.getDomNode(), schemaType, null);
        return clazz.cast(c);
    }

}

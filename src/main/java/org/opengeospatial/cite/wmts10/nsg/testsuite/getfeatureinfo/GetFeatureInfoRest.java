package org.opengeospatial.cite.wmts10.nsg.testsuite.getfeatureinfo;

import static de.latlon.ets.core.assertion.ETSAssert.assertUrl;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import org.opengeospatial.cite.wmts10.ets.core.domain.ProtocolBinding;
import org.opengeospatial.cite.wmts10.ets.core.domain.WMTS_Constants;
import org.opengeospatial.cite.wmts10.ets.core.util.ServiceMetadataUtils;
import org.opengeospatial.cite.wmts10.ets.testsuite.getfeatureinfo.AbstractBaseGetFeatureInfoFixture;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.util.Strings;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.latlon.ets.core.assertion.ETSAssert;

/**
 *
 * @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 *
 */
public class GetFeatureInfoRest extends AbstractBaseGetFeatureInfoFixture {
    /**
     * --- NSG Requirement 10: An NSG WMTS server shall provide standard endpoints from which representation of the
     * GetFeatureInfo resources can be obtained. ---
     */

    private URI getFeatureInfoURI = null;

    private boolean _debug = false;

    @Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 10", dependsOnMethods = "verifyGetFeatureInfoSupported")
    public void wmtsGetFeatureInfoRESTCapable()
                            throws XPathExpressionException, XPathFactoryConfigurationException {
        getFeatureInfoURI = ServiceMetadataUtils.getOperationEndpoint_REST( wmtsCapabilities,
                                                                            WMTS_Constants.GET_FEATURE_INFO,
                                                                            ProtocolBinding.GET );

        assertTrue( getFeatureInfoURI != null,
                    "GetFeatureInfo (GET) endpoint not found or REST is not supported in ServiceMetadata capabilities document." );
    }

    @Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 10", dependsOnMethods = "wmtsGetFeatureInfoRESTCapable")
    public void wmtsGetFeatureInfoRequestParametersSupported( ITestContext testContext ) {
        String requestFormat = null;

        try {
            String layerName = this.reqEntity.getKvpValue( WMTS_Constants.LAYER_PARAM );
            if ( layerName == null ) {
                NodeList layers = ServiceMetadataUtils.getNodeElements( wmtsCapabilities,
                                                                        "//wmts:Contents/wmts:Layer/ows:Identifier" );
                if ( layers.getLength() > 0 ) {
                    layerName = ( (Node) layers.item( 0 ) ).getTextContent().trim();
                }
            }

            XPath xPath = createXPath();

            // --- get the prepopulated KVP parameters, for the SOAP parameters

            String style = this.reqEntity.getKvpValue( WMTS_Constants.STYLE_PARAM );
            String tileMatrixSet = this.reqEntity.getKvpValue( WMTS_Constants.TILE_MATRIX_SET_PARAM );
            String tileMatrix = this.reqEntity.getKvpValue( WMTS_Constants.TILE_MATRIX_PARAM );
            String tileRow = this.reqEntity.getKvpValue( WMTS_Constants.TILE_ROW_PARAM );
            String tileCol = this.reqEntity.getKvpValue( WMTS_Constants.TILE_COL_PARAM );

            requestFormat = this.reqEntity.getKvpValue( WMTS_Constants.FORMAT_PARAM );

            // if ( getFeatureInfoURI == null )
            {
                NodeList resourceURLs = ServiceMetadataUtils.getNodeElements( wmtsCapabilities,
                                                                              "//wmts:Contents/wmts:Layer[ows:Identifier = '"
                                                                                                      + layerName
                                                                                                      + "']/wmts:ResourceURL" );

                Assert.assertTrue( ( ( resourceURLs != null ) && ( resourceURLs.getLength() > 0 ) ),
                                   "WMTS apparently does not support REST or contains no REST endpoints for layer: "
                                                           + layerName );

                Random random = new Random();
                int randomIndx = random.nextInt( resourceURLs.getLength() );

                Node resourceNode = resourceURLs.item( randomIndx );

                String templateURL = (String) ServiceMetadataUtils.getNodeText( xPath, resourceNode, "@template" );
                requestFormat = (String) ServiceMetadataUtils.getNodeText( xPath, resourceNode, "@format" );
                if ( Strings.isNullOrEmpty( templateURL ) || Strings.isNullOrEmpty( requestFormat ) ) {
                    throw new XPathExpressionException( "Invalid or corrupt Resource URL image format" );
                }

                try {
                    templateURL = templateURL.replaceAll( "\\{(?i)Style\\}", style );
                    templateURL = templateURL.replaceAll( "\\{(?i)FeatureInfoMatrixSet\\}", tileMatrixSet );
                    templateURL = templateURL.replaceAll( "\\{(?i)FeatureInfoMatrix\\}", tileMatrix );
                    templateURL = templateURL.replaceAll( "\\{(?i)FeatureInfoRow\\}", tileRow );
                    templateURL = templateURL.replaceAll( "\\{(?i)FeatureInfoCol\\}", tileCol );
                    templateURL = templateURL.replaceAll( "\\{(?i)I\\}", "0" );
                    templateURL = templateURL.replaceAll( "\\{(?i)J\\}", "0" );
                    getFeatureInfoURI = new URI( templateURL );
                } catch ( URISyntaxException ue ) {
                    getFeatureInfoURI = null;
                }
            }
            assertUrl( getFeatureInfoURI.toString() );
            // assertUriIsResolvable(restURIstr);

            /*--
            assertTrue(WMTS_Constants.GET_FeatureInfo.equals( responseDoc.getDocumentElement().getLocalName() ),
            	"Invalid REST request for WMTS ServeiceMetadata capabilities document: " + responseDoc.getDocumentElement().getNodeName() );
            --*/

            // --- Example of valid URL
            {
                Client client = Client.create();
                WebResource webRes = client.resource( getFeatureInfoURI );
                ClientResponse rsp = webRes.get( ClientResponse.class );

                Assert.assertTrue( rsp != null, "Error processing REST GetFeatureInfo request" );

                // storeResponseImage( rsp, "Requirement10", "simple", requestFormat );

                ETSAssert.assertContentType( rsp.getHeaders(), requestFormat );
                ETSAssert.assertStatusCode( rsp.getStatus(), 200 );
            }
            // --- Example of invalid URL
            {
                String erroneousURL = getFeatureInfoURI.toString();
                int indx = erroneousURL.lastIndexOf( "/" );
                erroneousURL = erroneousURL.substring( 0, indx + 1 ) + "X" + erroneousURL.substring( indx + 1 );

                URI invalidURI = null;
                try {
                    invalidURI = new URI( erroneousURL );
                } catch ( URISyntaxException ue ) {
                    System.out.println( ue.getMessage() );
                    invalidURI = null;
                }

                Client client = Client.create();
                WebResource webRes = client.resource( invalidURI );
                ClientResponse rsp = webRes.get( ClientResponse.class );

                Assert.assertTrue( rsp != null, "Error processing invalid REST GetFeatureInfo request" );
                Assert.assertFalse( rsp.getStatus() == 200,
                                    "Expected status code from Invalid REST GetFeatureInfo request is not expected to be 200. " );
                ETSAssert.assertContentType( rsp.getHeaders(), WMTS_Constants.TEXT_XML );

            }
        } catch ( XPathExpressionException | XPathFactoryConfigurationException xpe ) {
            System.out.println( xpe.getMessage() );
            if ( this._debug ) {
                xpe.printStackTrace();
            }
            assertTrue( false, "Error found when retrieving REST GetFeatureInfo request: " + xpe.getMessage() );
        }
    }

    private XPath createXPath()
                            throws XPathFactoryConfigurationException {
        XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
        XPath xpath = factory.newXPath();
        xpath.setNamespaceContext( NS_BINDINGS );
        return xpath;
    }

}
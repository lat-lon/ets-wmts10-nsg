package org.opengeospatial.cite.wmts10.nsg.testsuite.gettile;

import org.opengeospatial.cite.wmts10.ets.testsuite.gettile.AbstractBaseGetTileFixture;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.testng.ITestContext;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Iterator;
import java.util.logging.Level;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFactoryConfigurationException;

import static de.latlon.ets.core.assertion.ETSAssert.assertUrl;

import de.latlon.ets.core.util.TestSuiteLogger;
import org.opengeospatial.cite.wmts10.ets.core.domain.ProtocolBinding;
import org.opengeospatial.cite.wmts10.ets.core.domain.WMTS_Constants;
import org.opengeospatial.cite.wmts10.ets.core.domain.WmtsNamespaces;
import org.opengeospatial.cite.wmts10.ets.core.util.ServiceMetadataUtils;
import org.opengeospatial.cite.wmts10.ets.core.util.WMTS_SOAPcontainer;

/*
*
* @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
* @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
*
*/
// -------

public class GetTileParametersSoap extends AbstractBaseGetTileFixture
{
/*---
	NSG Requirement 6: 
		An NSG WMTS server shall respond to a SOAP encoded GetTile operation request with an image in the MIME type 
		specified by the Format parameter of the request. 	
---*/
	
	private URI getTileURI = null;
	
	private boolean _debug = false;
	
	//--	private String _debugSOAPurl = "http://www.opengis.uab.es/cgi-bin/world/MiraMon5_0.cgi?";
	//--	private String _debugSOAPurl = "https://basemap.nationalmap.gov:443/arcgis/services/USGSTopo/MapServer";

	// ---
	
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 6", dependsOnMethods = "verifyGetTileSupported")
	public void wmtsGetTileSOAPRequestsExists()
	{
		getTileURI = ServiceMetadataUtils.getOperationEndpoint_SOAP(wmtsCapabilities, WMTS_Constants.GET_TILE, ProtocolBinding.POST);
/*--
if ( soapURI==null)
{
soapURI = URI.create(this._debugSOAPurl);
}
--*/			
		assertTrue(this.getTileURI != null, "GetTile (GET) endpoint not found in ServiceMetadata capabilities document or WMTS does not support SOAP.");
	}
	
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 6", dependsOnMethods = "wmtsGetTileSOAPRequestsExists")
	public void wmtsGetTileRequestFormatParameters( ITestContext testContext ) 
	{
		if ( getTileURI == null )
		{
			getTileURI = ServiceMetadataUtils.getOperationEndpoint_SOAP( this.wmtsCapabilities, WMTS_Constants.GET_TILE, ProtocolBinding.POST );
		}
		String requestFormat = null;
		
		String soapURIstr = getTileURI.toString(); 
		assertUrl(soapURIstr);
		
		try
		{
			XPath xPath = createXPath();
			
			String layerName = this.reqEntity.getKvpValue(WMTS_Constants.LAYER_PARAM);
			if ( layerName == null)
			{
				NodeList layers = ServiceMetadataUtils.getNodeElements( wmtsCapabilities, "//wmts:Contents/wmts:Layer/ows:Identifier");
				if ( layers.getLength() > 0)
				{
					layerName = ((Node)layers.item(0)).getTextContent().trim();
				}			
			}
		
		// --- get the prepopulated KVP parameters, for the SOAP parameters
		
			String style = this.reqEntity.getKvpValue(WMTS_Constants.STYLE_PARAM);
			String tileMatrixSet = this.reqEntity.getKvpValue(WMTS_Constants.TILE_MATRIX_SET_PARAM);
			String tileMatrix = this.reqEntity.getKvpValue(WMTS_Constants.TILE_MATRIX_PARAM);
			String tileRow = this.reqEntity.getKvpValue(WMTS_Constants.TILE_ROW_PARAM);
			String tileCol = this.reqEntity.getKvpValue(WMTS_Constants.TILE_COL_PARAM);
			
			requestFormat = this.reqEntity.getKvpValue(WMTS_Constants.FORMAT_PARAM);  // --- default setting
		
			NodeList imageFormats = ServiceMetadataUtils.getNodeElements( wmtsCapabilities, "//wmts:Contents/wmts:Layer[ows:Identifier = '" + layerName + "']/wmts:Format");
		
			SoftAssert sa = new SoftAssert();		

			for(int i=0; i<imageFormats.getLength(); i++)
			{
				requestFormat = imageFormats.item(i).getTextContent().trim();
			
				WMTS_SOAPcontainer soap = new WMTS_SOAPcontainer(WMTS_Constants.GET_TILE, soapURIstr);
				
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.LAYER_PARAM, layerName);
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.STYLE_PARAM, style);
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.FORMAT_PARAM, requestFormat);
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_MATRIX_SET_PARAM, tileMatrixSet);
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_MATRIX_PARAM, tileMatrix);
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_ROW_PARAM, tileRow);
				soap.AddParameter(WmtsNamespaces.serviceOWS, WMTS_Constants.TILE_COL_PARAM, tileCol);
			
				SOAPMessage soapResponse = soap.getSOAPresponse(true);
				sa.assertTrue(soapResponse != null, "SOAP reposnse came back null");
		
				Document soapDocument = (Document)soap.getResponseDocument();
		
					//this.parseNodes(soapDocument,0);
				if ((soapResponse.getAttachments()!= null) && (soapResponse.countAttachments() > 0))
				{
					  //SOAPMessage response = connection.call(requestMessage, serviceURL);
					Iterator attachmentsIterator = soapResponse.getAttachments();
				    while (attachmentsIterator.hasNext()) {
				        AttachmentPart attachment = (AttachmentPart) attachmentsIterator.next();
					        //do something with attachment 
				    }
				}
				else
				{
						//String imageString = (String)createXPath().evaluate("//wmts:BinaryPayload/wmts:BinaryContent", soapDocument,XPathConstants.STRING);

					String formatStr = ServiceMetadataUtils.getNodeText(xPath, soapDocument, "//wmts:BinaryPayload/wmts:Format");				
					storeSoapResponseImage( soapResponse, "Requirement6", "simple", formatStr );
					sa.assertEquals(formatStr, requestFormat, "SOAP response received format: " + formatStr + " but expected: " + requestFormat);
				}
//			 assertXPath( "//wmts:Capabilities/@version = '1.0.0'", soapDocument, NS_BINDINGS );
//				WmtsAssertion.assertXPath( sa, "//wmts:BinaryPayload/wmts:Format = '" + requestFormat + "'", soapDocument, NS_BINDINGS );
			}		
			sa.assertAll();
		}
		catch ( XPathExpressionException | XPathFactoryConfigurationException xpe)
		{
			System.out.println(xpe);
			TestSuiteLogger.log(Level.WARNING, "Invalid or corrupt SOAP content or XML format", xpe);
			if ( this._debug )
			{
				xpe.printStackTrace();
			}
		} 
	}
	
	// ---

	// ---
	   	
	// --- --------

    // ---  
/*---   
    private void parseNodes(Node n, int level)
    {       	
    	if ( n != null)
    	{
    		String nam = n.getNodeName();
    		String val = n.getNodeValue();
    		String lnm = n.getLocalName();
    		//String txt = n.getTextContent().trim();
    		
    		for (int i=0; i<level; i++)
    			System.out.print("\t");
    		System.out.println("Node: " + nam + " = " + val );//+ "( or:  " + txt + " )");
    		parseNodes(n.getFirstChild(), level+1);
    		
    		parseNodes(n.getNextSibling(), level);
    	}
    }
  ---*/
	// --- 
/*---	  */
	private XPath createXPath()
               throws XPathFactoryConfigurationException
	{
		XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext( NS_BINDINGS );
		return xpath;
	}
/* ---*/
}
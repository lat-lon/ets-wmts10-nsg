package nsg.wmts10.testsuite.getcapabilities;

import ets.wmts10.testsuite.getcapabilities.AbstractBaseGetCapabilitiesFixture;

import org.testng.SkipException;
import org.testng.annotations.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.testng.Assert.assertTrue;

import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import ets.wmts10.core.domain.BoundingBox;
import ets.wmts10.core.domain.LayerInfo;

import ets.wmts10.core.util.ServiceMetadataUtils;

/*
*
* @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
* @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
*
*/
public class GetCapabilitiesProjectionTest extends AbstractBaseGetCapabilitiesFixture 
{
/*---
	NSG Requirement 12: 
			An NSG WMTS server shall support the following projections whose validity zones overlap data 
			published by the service:
			•	World Mercator Projection…EPSG:3395
			•	UPS projection over WGS84 (north zone)……  EPSG:5041
			•	UPS projection over WGS84 (south zone)……  EPSG:5042

---*/
	
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 12", dependsOnMethods="verifyGetCapabilitiesSupported")
	public void wmtsCapabilitiesExists() 
	{
		assertTrue(this.wmtsCapabilities != null, "No ServerMetadata Capabilities document");
	}
	
	// ---
	   
	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 12", dependsOnMethods="wmtsCapabilitiesExists")
	public void wmtsCapabilitiesEPSG3395Test() 
	{
		try 
		{
			boolean EPSG3395 = false;
			
			NodeList crsList = (NodeList)ServiceMetadataUtils.getNodeElements(wmtsCapabilities, "//ows:SupportedCRS");
			for (int crsI=0; (crsI<crsList.getLength() && !EPSG3395); crsI++)
			{
				Node supportedCRS = crsList.item(crsI);
				String crsName = supportedCRS.getTextContent();
				
				if ( !EPSG3395 && crsName.contains("EPSG:") && crsName.contains(":3395"))
				{
					int indx0 = crsName.lastIndexOf("EPSG:");
					int indx1 = crsName.lastIndexOf(":3395");
					if ( indx0 < indx1 )
					{
						String modCrsName = crsName.substring(indx0, indx0+4) + crsName.substring(indx1, indx1+5);
						EPSG3395 = (modCrsName.equals("EPSG:3395"));
					}
				}
			}
			
			// --- not in TileMatrixSet, check if defined in the Layers
			if ( !(EPSG3395) )
			{
				for (int layerI=0; (layerI<layerInfo.size() && !EPSG3395); layerI++)
				{
					LayerInfo layer = layerInfo.get(layerI);
					
					List<BoundingBox> bbox = layer.getBboxes();
					
					for (int bboxI =-1; (bboxI<bbox.size() && !EPSG3395); bboxI++)
					{
						String crsName = null;
						if ( bboxI < 0 )
						{
							crsName = layer.getGeographicBbox().getCrs();
						}
						else
						{
							crsName = bbox.get(bboxI).getCrs();
						}
						
						if ( !EPSG3395 && crsName.contains("EPSG:") && crsName.contains(":3395"))
						{
							int indx0 = crsName.lastIndexOf("EPSG:");
							int indx1 = crsName.lastIndexOf(":3395");
							if ( indx0 < indx1 )
							{
								String modCrsName = crsName.substring(indx0, indx0+4) + crsName.substring(indx1, indx1+5);
								EPSG3395 = (modCrsName.equals("EPSG:3395"));
							}
						}
					}
				}
			}
			
			assertTrue(EPSG3395, "WMTS does not support EPSG:3395 (World Mercator Projection) in none of its <Layer>s or <TileMatrixSet>s.");
		}
		catch (XPathExpressionException xpe) 
		{
			//xpe.printStackTrace();
		}
	}
	
	// ---

	@Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 12", dependsOnMethods="wmtsCapabilitiesExists")
	public void wmtsCapabilitiesUPSTest() 
	{
	// --- TODO - check for overlap areas, then check for appropriate UPS projection zones
		throw new SkipException("Test for UPS projections currenly not implemented");
	}
	
	// --- -------
/*--	   
	private XPath createXPath()
               throws XPathFactoryConfigurationException
	{
		XPathFactory factory = XPathFactory.newInstance( XPathConstants.DOM_OBJECT_MODEL );
		XPath xpath = factory.newXPath();
		xpath.setNamespaceContext( NS_BINDINGS );
		return xpath;
	}
	--*/
}
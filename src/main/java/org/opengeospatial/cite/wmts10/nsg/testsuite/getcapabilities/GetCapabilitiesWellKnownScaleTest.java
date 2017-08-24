package org.opengeospatial.cite.wmts10.nsg.testsuite.getcapabilities;

import static org.testng.Assert.assertTrue;

import org.opengeospatial.cite.wmts10.ets.testsuite.getcapabilities.AbstractBaseGetCapabilitiesFixture;
import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 *
 * @author Jim Beatty (Jun/Jul-2017 for WMTS; based on original work of:
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 *
 */
public class GetCapabilitiesWellKnownScaleTest extends AbstractBaseGetCapabilitiesFixture {
    /**
     * --- NSG Requirement 13: An NSG WMTS server shall employ the Well-Known Scale Sets identified in Annex B (based
     * upon World Mercator projection EPSG 3395 and WGS 84 Geodetic EPSG 4326) ---
     */

    @Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 13", dependsOnMethods = "verifyGetCapabilitiesSupported")
    public void wmtsCapabilitiesExists() {
        assertTrue( this.wmtsCapabilities != null, "No ServerMetadata Capabilities document" );
    }

    @Test(description = "NSG Web Map Tile Service (WMTS) 1.0.0, Requirement 13", dependsOnMethods = "wmtsCapabilitiesExists")
    public void wmtsCapabilitiesUPSTest() {
        // --- TODO - check for well-known scale per Appendix B
        throw new SkipException( "Test for Well-Known Scales currenly not implemented" );
    }

}
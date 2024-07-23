package ephemeris;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.bodies.CelestialBody;
import fr.cnes.sirius.patrius.bodies.CelestialBodyFactory;
import fr.cnes.sirius.patrius.bodies.JPLCelestialBodyLoader;
import fr.cnes.sirius.patrius.bodies.EphemerisType;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.orbits.pvcoordinates.PVCoordinates;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class LoadingSunEphemeris {
	 
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        //String[] fileNames = { "unxp2000.405", "unxp1950.405", "unxp2700.406" };
        final String[] fileNames = { "unxp.*.405", "unxp.*.406" };
 
        // data for Sun coordinates output
        final TimeScale tuc = TimeScalesFactory.getUTC();       
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", tuc);
        final Frame icrf = FramesFactory.getICRF();
 
        for (int i = 0; i < fileNames.length; i++) {
 
            final JPLCelestialBodyLoader loader = new JPLCelestialBodyLoader(fileNames[i], EphemerisType.SUN);
 
            CelestialBodyFactory.clearCelestialBodyLoaders();
            CelestialBodyFactory.addCelestialBodyLoader(CelestialBodyFactory.SUN, loader);
 
            // Using the loading theory
            final CelestialBody sun = loader.loadCelestialBody(CelestialBodyFactory.SUN);
 
            // Coordinates of the Sun at a given date and reference frame
            final PVCoordinates pv = sun.getPVCoordinates(date, icrf);
 
            System.out.println("");
            System.out.println(pv.getPosition().getX());
            System.out.println(pv.getPosition().getY());
            System.out.println(pv.getPosition().getZ());
        }
 
    }
 
}

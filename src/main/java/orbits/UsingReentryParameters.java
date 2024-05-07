package orbits;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.CartesianParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.KeplerianParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ReentryParameters;
import fr.cnes.sirius.patrius.orbits.pvcoordinates.PVCoordinates;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class UsingReentryParameters  {
	 
    public static void main(final String[] args) throws PatriusException, IOException, ParseException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Constants that will be used for conversions
        final double REQ  = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        final double APLA = Constants.WGS84_EARTH_FLATTENING;
        final double MU   = Constants.WGS84_EARTH_MU;
 
        // Initialization of keplerian parameters
        final double dga = REQ + 250.e3;
        final double exc = 0.;
        final double inc = FastMath.toRadians(45.);
        final double gom = FastMath.toRadians(0.);
        final double pom = FastMath.toRadians(0.);
        final double ano = FastMath.toRadians(90.);
        final KeplerianParameters kep = new KeplerianParameters(dga, exc, inc, pom, gom, ano, PositionAngle.MEAN, MU);
 
        // Corresponding "absolute" reentry parameters
        final ReentryParameters renAbs = kep.getReentryParameters(REQ, 0.);
 
        System.out.println("ABSOLUTE GEOCENTRIC PARAMETERS ...");
        System.out.println("alt = "+renAbs.getAltitude()/1000.+" km");
        System.out.println("lat = "+FastMath.toDegrees(renAbs.getLatitude())+" deg");
        System.out.println("lon = "+FastMath.toDegrees(renAbs.getLongitude())+" deg");
        System.out.println("vit = "+renAbs.getVelocity()+" m/s");
        System.out.println("pen = "+FastMath.toDegrees(renAbs.getSlope())+" deg");
        System.out.println("azi = "+FastMath.toDegrees(renAbs.getAzimuth())+" deg");
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        // Getting frames
        final Frame GCRF = FramesFactory.getGCRF();
        final Frame ITRF = FramesFactory.getITRF();
        final Transform transform = GCRF.getTransformTo(ITRF, date);
 
        // Getting PV coordinates, considering them in GCRF frame
        final PVCoordinates pvGCRF = kep.getCartesianParameters().getPVCoordinates();
        // Same PV coordinates but defined in a rotative frame ...
        final PVCoordinates pvITRF = transform.transformPVCoordinates(pvGCRF);
        // Getting corresponding (relative) cartesian parameters
        final CartesianParameters carRel = new CartesianParameters(pvITRF, MU);
        // Getting relative geocentric reentry parameters
        final ReentryParameters relRen = carRel.getReentryParameters(REQ, 0.);
 
        System.out.println("\nRELATIVE GEOCENTRIC REENTRY PARAMETERS ...");
        System.out.println("alt = "+relRen.getAltitude()/1000.+" km");
        System.out.println("lat = "+FastMath.toDegrees(relRen.getLatitude())+" deg");
        System.out.println("lon = "+FastMath.toDegrees(relRen.getLongitude())+" deg");
        System.out.println("vit = "+relRen.getVelocity()+" m/s");
        System.out.println("pen = "+FastMath.toDegrees(relRen.getSlope())+" deg");
        System.out.println("azi = "+FastMath.toDegrees(relRen.getAzimuth())+" deg");
 
        // Getting relative geocentric reentry parameters
        final ReentryParameters relRenGeod = carRel.getReentryParameters(REQ, APLA);
 
        System.out.println("\nRELATIVE GEODETIC REENTRY PARAMETERS ...");
        System.out.println("alt = "+relRenGeod.getAltitude()/1000.+" km");
        System.out.println("lat = "+FastMath.toDegrees(relRenGeod.getLatitude())+" deg");
        System.out.println("lon = "+FastMath.toDegrees(relRenGeod.getLongitude())+" deg");
        System.out.println("vit = "+relRenGeod.getVelocity()+" m/s");
        System.out.println("pen = "+FastMath.toDegrees(relRenGeod.getSlope())+" deg");
        System.out.println("azi = "+FastMath.toDegrees(relRenGeod.getAzimuth())+" deg");
 
    }
 
}
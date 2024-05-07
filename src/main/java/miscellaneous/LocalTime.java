package miscellaneous;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.bodies.CelestialBody;
import fr.cnes.sirius.patrius.bodies.MeeusSun;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.ApsisOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisRadiusParameters;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.LocalTimeAngle;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class LocalTime {
	 
    /**
     * @param args
     * @throws PatriusException 
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2000-06-21T12:00:00.000", TUC);
 
        // Getting the frame with wich will defined the orbit parameters
        // As for time scale, we will use also a "factory".
        final Frame CIRF = FramesFactory.getCIRF();
 
        // Initial orbit
        final double sma = 7200.e+3;
        final double exc = 0.01;
        final double per = sma*(1.-exc);
        final double apo = sma*(1.+exc);
        final double inc = FastMath.toRadians(98.);
        final double pa = FastMath.toRadians(0.);
        final double raan = FastMath.toRadians(90.);
        final double anm = FastMath.toRadians(0.);
        final double MU = Constants.WGS84_EARTH_MU;
 
        final ApsisRadiusParameters par = new ApsisRadiusParameters(per, apo, inc, pa, raan, anm, PositionAngle.MEAN, MU);
        final Orbit iniOrbit = new ApsisOrbit(par, CIRF, date);
 
        // Sun ephemeris
        final CelestialBody sunEphemeris = new MeeusSun();
 
        // Local times
        final LocalTimeAngle localTime = new LocalTimeAngle(sunEphemeris);
 
        final double tlt = localTime.computeTrueLocalTimeAngle(iniOrbit);
        final double mlt = localTime.computeMeanLocalTimeAngle(iniOrbit);
 
        System.out.println("TLT = "+angleToHour(tlt)+" h"+" ("+FastMath.toDegrees(tlt)+" deg)");
        System.out.println("MLT = "+angleToHour(mlt)+" h"+" ("+FastMath.toDegrees(mlt)+" deg)");
 
    }
 
    /**
     * Method to transform local time given as an angle to local time in hours.
     * @param angle local time as an angle (rad)
     * @return      local time in hours
     */
    private static double angleToHour ( final double angle ) {
 
        final double hour = 12. + angle*12./FastMath.PI;
        return hour;
 
    }
 
}

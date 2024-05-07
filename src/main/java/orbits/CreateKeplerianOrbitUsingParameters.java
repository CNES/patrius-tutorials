package orbits;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.KeplerianOrbit;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisRadiusParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.KeplerianParameters;
import fr.cnes.sirius.patrius.orbits.pvcoordinates.PVCoordinates;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class CreateKeplerianOrbitUsingParameters {
	 
    public static void main(final String[] args) throws PatriusException, IOException, ParseException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        // Getting the frame with wich will defined the orbit parameters
        // As for time scale, we will use also a "factory".
        final Frame GCRF = FramesFactory.getGCRF();
 
        // Creation of a keplerian orbit
        final double sma = 7200.e+3;
        final double exc = 0.01;
        final double inc = FastMath.toRadians(98.);
        final double pa = FastMath.toRadians(0.);
        final double raan = FastMath.toRadians(0.);
        final double anm = FastMath.toRadians(0.);
        final double MU = Constants.WGS84_EARTH_MU;
 
        // We use firstly an orbital parameters object
        final KeplerianParameters kep = new KeplerianParameters(sma, exc, inc, pa, raan, anm, PositionAngle.MEAN, MU);
 
        // Then, we create the orbit
        final KeplerianOrbit iniOrbit = new KeplerianOrbit(kep, GCRF, date);
 
        // Printing the Keplerian period
        System.out.println("Tper = "+iniOrbit.getKeplerianPeriod()+" s");
 
        // We may use other set of parameters ...
        final double per = sma*(1.-exc);
        final double apo = sma*(1.+exc);
        final ApsisRadiusParameters aps = new ApsisRadiusParameters(per, apo, inc, pa, raan, anm, PositionAngle.MEAN, MU);
 
        // Then, we create again the orbit
        final KeplerianOrbit sameOrbit = new KeplerianOrbit(aps, GCRF, date);
 
        // We compare each PV coordinates
        final PVCoordinates pvOrbit = iniOrbit.getPVCoordinates();
        final PVCoordinates pvSameOrbit = sameOrbit.getPVCoordinates();
 
        System.out.println();
        System.out.println("Is both orbits equals : "+pvSameOrbit.getPosition().equals(pvOrbit.getPosition()));
        System.out.println("Is both orbits equals : "+pvSameOrbit.getVelocity().equals(pvOrbit.getVelocity()));
 
    }
 
}
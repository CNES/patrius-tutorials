package attitudes;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.attitudes.Attitude;
import fr.cnes.sirius.patrius.attitudes.AttitudeLaw;
import fr.cnes.sirius.patrius.attitudes.TwoDirectionAttitudeLaw;
import fr.cnes.sirius.patrius.attitudes.directions.CelestialBodyPolesAxisDirection;
import fr.cnes.sirius.patrius.attitudes.directions.GenericTargetDirection;
import fr.cnes.sirius.patrius.attitudes.directions.IDirection;
import fr.cnes.sirius.patrius.bodies.CelestialBody;
import fr.cnes.sirius.patrius.bodies.MeeusSun;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Rotation;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.RotationOrder;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.KeplerianOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.KeplerianParameters;
import fr.cnes.sirius.patrius.orbits.pvcoordinates.PVCoordinates;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class TwoDirectionAttitudeLaws {
	 
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        // Getting the frame with wich will defined the orbit parameters
        // As for time scale, we will use also a "factory".
        final Frame GCRF = FramesFactory.getGCRF();
 
        // Initial orbit
        final double sma = 7200.e+3;
        final double exc = 0.01;
        final double inc = FastMath.toRadians(98.);
        final double pa = FastMath.toRadians(0.);
        final double raan = FastMath.toRadians(0.);
        final double anm = FastMath.toRadians(0.);
        final double MU = Constants.WGS84_EARTH_MU;
 
        final KeplerianParameters par = new KeplerianParameters(sma, exc, inc, pa, raan, anm, PositionAngle.MEAN, MU);
        final Orbit iniOrbit = new KeplerianOrbit(par, GCRF, date);
 
        // Using the Meeus model for the Sun.
        final CelestialBody sun = new MeeusSun();
 
        // Sun directions
        final IDirection dirSun = new GenericTargetDirection(sun);
        final CelestialBodyPolesAxisDirection dirPole = new CelestialBodyPolesAxisDirection(sun);
 
        // Building an attitude law
        final Vector3D firstAxis = new Vector3D(1., 0., 0.);
        final Vector3D secondAxis = new Vector3D(0., 1., 0.);
        final TwoDirectionAttitudeLaw attitudeLaw = new TwoDirectionAttitudeLaw(dirSun, dirPole, firstAxis, secondAxis);
        final Attitude att = attitudeLaw.getAttitude(iniOrbit);
 
        // Printing attitude
        final double psi  = att.getRotation().getAngles(RotationOrder.ZYX)[0];
        final double teta = att.getRotation().getAngles(RotationOrder.ZYX)[1];
 
        System.out.println("Psi  / GCRF  = "+FastMath.toDegrees(psi)+" deg");
        System.out.println("Teta / GCRF = "+FastMath.toDegrees(teta)+" deg");
 
        // Coordinates of the Sun vs GCRF at the same date
        final PVCoordinates pv = sun.getPVCoordinates(date, GCRF);
        final Vector3D sunPos = pv.getPosition();
 
        // Direction of the Sun from the cdg of the satellite
        final Vector3D satPos = iniOrbit.getPVCoordinates(GCRF).getPosition();
        final Rotation sunDir = new Rotation(Vector3D.PLUS_I, sunPos.subtract(satPos));
 
        final double psiSun  = sunDir.getAngles(RotationOrder.ZYX)[0];
        final double tetaSun = sunDir.getAngles(RotationOrder.ZYX)[1];
 
        System.out.println();
        System.out.println("Psi  / GCRF  = "+FastMath.toDegrees(psiSun)+" deg");
        System.out.println("Teta / GCRF = "+FastMath.toDegrees(tetaSun)+" deg");
 
        System.out.println();
        System.out.println("Delta Psi  = "+FastMath.toDegrees(psiSun-psi)+" deg");
        System.out.println("Delta Teta = "+FastMath.toDegrees(tetaSun-teta)+" deg");
 
    }
 
}

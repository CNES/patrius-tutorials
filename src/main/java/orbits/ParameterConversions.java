package orbits;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisAltitudeParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisRadiusParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.CircularParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.EquatorialParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.EquinoctialParameters;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.KeplerianParameters;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class ParameterConversions {
	 
    public static void main(final String[] args) throws PatriusException, IOException, ParseException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Constants that will be used for conversions
        final double REQ  = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        final double MU   = Constants.WGS84_EARTH_MU;
 
        // Initialization of keplerian parameters
        final double dga = REQ + 250.e3;
        final double exc = 0.;
        final double inc = FastMath.toRadians(45.);
        final double gom = FastMath.toRadians(10.);
        final double pom = FastMath.toRadians(0.);
        final double ano = FastMath.toRadians(180.);
        final KeplerianParameters kep = new KeplerianParameters(dga, exc, inc, pom, gom, ano, PositionAngle.MEAN, MU);
 
        // Same in circular parameters
        final CircularParameters cir1 = new CircularParameters(dga, exc*FastMath.cos(pom), exc*FastMath.sin(pom),
                                                         inc, gom, pom+ano, PositionAngle.MEAN, MU);
        // Same in circular parameters but coming from conversion
        final CircularParameters cir2 = kep.getCircularParameters();
 
        // Display of potential differences
        System.out.println("TEST CIRCULAR PARAMETERS ...");
        System.out.println("Delta dga = "+(cir2.getA() - cir1.getA()));
        System.out.println("Delta ex  = "+(cir2.getCircularEx() - cir1.getCircularEx()));
        System.out.println("Delta ey  = "+(cir2.getCircularEy() - cir1.getCircularEy()));
        System.out.println("Delta inc = "+(cir2.getI() - cir1.getI()));
        System.out.println("Delta gom = "+(cir2.getRightAscensionOfAscendingNode() - cir1.getRightAscensionOfAscendingNode()));
        System.out.println("Delta pso = "+(cir2.getAlpha(PositionAngle.MEAN) - cir1.getAlpha(PositionAngle.MEAN)));
 
        // Same in equatorial parameters
        final EquatorialParameters equ1 = new EquatorialParameters(dga, exc, pom+gom,
                2.*FastMath.sin(inc/2.)*FastMath.cos(gom), 2.*FastMath.sin(inc/2.)*FastMath.sin(gom), ano, PositionAngle.MEAN, MU);
        // Same in equatorial parameters but coming from conversion
        final EquatorialParameters equ2 = kep.getEquatorialParameters();
 
        // Display of potential differences
        System.out.println("TEST EQUATORIAL PARAMETERS ...");
        System.out.println("Delta dga = "+(equ2.getA() - equ1.getA()));
        System.out.println("Delta exc = "+(equ2.getE() - equ1.getE()));
        System.out.println("Delta pom = "+(equ2.getPomega() - equ1.getPomega()));
        System.out.println("Delta ix  = "+(equ2.getIx() - equ1.getIx()));
        System.out.println("Delta iy  = "+(equ2.getIy() - equ1.getIy()));
        System.out.println("Delta ano = "+(equ2.getAnomaly(PositionAngle.MEAN) - equ1.getAnomaly(PositionAngle.MEAN)));
 
        // Same in equinoctial parameters
        final EquinoctialParameters eqx1 = new EquinoctialParameters(dga, exc*FastMath.cos(pom+gom), exc*FastMath.sin(pom+gom),
                FastMath.tan(inc/2.)*FastMath.cos(gom), FastMath.tan(inc/2.)*FastMath.sin(gom), ano+pom+gom, PositionAngle.MEAN, MU);
        // Same in equinoctial parameters but coming from conversion
        final EquinoctialParameters eqx2 = kep.getEquinoctialParameters();
 
        // Display of potential differences
        System.out.println("TEST EQUINOCTIAL PARAMETERS ...");
        System.out.println("Delta dga = "+(eqx2.getA() - eqx1.getA()));
        System.out.println("Delta ex  = "+(eqx2.getEquinoctialEx() - eqx1.getEquinoctialEx()));
        System.out.println("Delta ey  = "+(eqx2.getEquinoctialEy() - eqx1.getEquinoctialEy()));
        System.out.println("Delta hx  = "+(eqx2.getHx() - eqx1.getHx()));
        System.out.println("Delta hy  = "+(eqx2.getHy() - eqx1.getHy()));
        System.out.println("Delta lon = "+(eqx2.getL(PositionAngle.MEAN) - eqx1.getL(PositionAngle.MEAN)));
 
        final double rpe = dga*(1.-exc);
        final double rap = dga*(1.+exc);
 
        // Same in apsis radius parameters
        final ApsisRadiusParameters apr1 = new ApsisRadiusParameters(rpe, rap, inc, pom, gom, ano, PositionAngle.MEAN, MU);
        // Same in apsis radius parameters but coming from conversion
        final ApsisRadiusParameters apr2 = kep.getApsisRadiusParameters();
 
        // Display of potential differences
        System.out.println("TEST APSIS RADIUS PARAMETERS ...");
        System.out.println("Delta rpe = "+(apr2.getPeriapsis() - apr1.getPeriapsis()));
        System.out.println("Delta rap = "+(apr2.getApoapsis() - apr1.getApoapsis()));
        System.out.println("Delta inc = "+(apr2.getI() - apr1.getI()));
        System.out.println("Delta pom = "+(apr2.getPerigeeArgument() - apr1.getPerigeeArgument()));
        System.out.println("Delta gom = "+(apr2.getRightAscensionOfAscendingNode() - apr1.getRightAscensionOfAscendingNode()));
        System.out.println("Delta ano = "+(apr2.getAnomaly(PositionAngle.MEAN) - apr1.getAnomaly(PositionAngle.MEAN)));
 
        // Same in apsis altitude
        final ApsisAltitudeParameters apa1 = new ApsisAltitudeParameters(rpe-REQ, rap-REQ, inc, pom, gom, ano, PositionAngle.MEAN, MU, REQ);
        // Same in apsis altitude parameters but coming from conversion
        final ApsisAltitudeParameters apa2 = kep.getApsisAltitudeParameters(REQ);
 
        // Display of potential differences
        System.out.println("TEST APSIS ALTITUDE PARAMETERS ...");
        System.out.println("Delta hpe = "+(apa2.getPeriapsisAltitude() - apa1.getPeriapsisAltitude()));
        System.out.println("Delta hap = "+(apa2.getApoapsisAltitude() - apa1.getApoapsisAltitude()));
        System.out.println("Delta inc = "+(apa2.getI() - apa1.getI()));
        System.out.println("Delta pom = "+(apa2.getPerigeeArgument() - apa1.getPerigeeArgument()));
        System.out.println("Delta gom = "+(apa2.getRightAscensionOfAscendingNode() - apa1.getRightAscensionOfAscendingNode()));
        System.out.println("Delta ano = "+(apa2.getAnomaly(PositionAngle.MEAN) - apa1.getAnomaly(PositionAngle.MEAN)));
 
    }
 
}
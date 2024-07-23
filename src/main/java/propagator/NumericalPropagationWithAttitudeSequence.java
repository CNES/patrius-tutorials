package propagator;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.attitudes.AttitudeLaw;
import fr.cnes.sirius.patrius.attitudes.AttitudesSequence;
import fr.cnes.sirius.patrius.attitudes.ConstantAttitudeLaw;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Rotation;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.RotationOrder;
import fr.cnes.sirius.patrius.math.ode.FirstOrderIntegrator;
import fr.cnes.sirius.patrius.math.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.ApsisOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.OrbitType;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisRadiusParameters;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.propagation.events.AOLDetector;
import fr.cnes.sirius.patrius.propagation.events.EventDetector;
import fr.cnes.sirius.patrius.propagation.events.EventDetector.Action;
import fr.cnes.sirius.patrius.propagation.numerical.NumericalPropagator;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class NumericalPropagationWithAttitudeSequence {
	 
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
        final double per = sma*(1.-exc);
        final double apo = sma*(1.+exc);
        final double inc = FastMath.toRadians(98.);
        final double pa = FastMath.toRadians(0.);
        final double raan = FastMath.toRadians(0.);
        final double anm = FastMath.toRadians(0.);
        final double MU = Constants.WGS84_EARTH_MU;
 
        final ApsisRadiusParameters par = new ApsisRadiusParameters(per, apo, inc, pa, raan, anm, PositionAngle.MEAN, MU);
        final Orbit iniOrbit = new ApsisOrbit(par, GCRF, date);
 
        // We create a spacecratftstate
        final SpacecraftState iniState = new SpacecraftState(iniOrbit);
 
        // Initialization of the Runge Kutta integrator with a 2 s step
        final double pasRk = 2.;
        final FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(pasRk);
 
        // Initialization of the propagator
        final NumericalPropagator propagator = new NumericalPropagator(integrator, iniState.getFrame(), 
        		OrbitType.CARTESIAN, PositionAngle.TRUE);
        propagator.resetInitialState(iniState);
 
        //SPECIFIC
        // Adding attitude sequence
        final AttitudesSequence seqAtt = new AttitudesSequence();
 
        // Laws to be taken into account in the sequence
        final AttitudeLaw law1 = new ConstantAttitudeLaw(GCRF, new Rotation(RotationOrder.ZYX, 0., 0., 0.));
        final AttitudeLaw law2 = new ConstantAttitudeLaw(GCRF, new Rotation(RotationOrder.ZYX, FastMath.toRadians(45.), FastMath.toRadians(45.), FastMath.toRadians(45.)));
 
        // Events that will switch from a law to another
        final double maxCheck = 10.;
        final double threshold = 1.e-3;
        final EventDetector event1 = new AOLDetector(0., PositionAngle.MEAN, GCRF, maxCheck, threshold, Action.RESET_STATE);
        final EventDetector event2 = new AOLDetector(FastMath.toRadians(180.), PositionAngle.MEAN, GCRF, maxCheck, threshold, Action.RESET_STATE);
 
        //Adding switches
        seqAtt.addSwitchingCondition(law1, event1, true, false, law2);
        seqAtt.addSwitchingCondition(law2, event2, true, false, law1);
 
        propagator.setAttitudeProvider(seqAtt);
        seqAtt.registerSwitchEvents(propagator);
        //SPECIFIC
 
        // Propagating 100s
        final double dt = 0.25*iniOrbit.getKeplerianPeriod();
        System.out.println(dt);
        final AbsoluteDate finalDate = date.shiftedBy(dt);
        final SpacecraftState finalState = propagator.propagate(finalDate);
        final Orbit finalOrbit = finalState.getOrbit();
 
        // Printing new date and true latitude argument
        System.out.println();
        System.out.println("Initial true latitude argument = "+FastMath.toDegrees(iniOrbit.getLv())+" deg");
        System.out.println("New date = "+finalOrbit.getDate().toString(TUC)+" deg");
        System.out.println("True latitude argument = "+FastMath.toDegrees(finalOrbit.getLv())+" deg");
        // Printing attitude
        final double psi  = finalState.getAttitude().getRotation().getAngles(RotationOrder.ZYX)[0];
        final double teta = finalState.getAttitude().getRotation().getAngles(RotationOrder.ZYX)[1];
        final double phi  = finalState.getAttitude().getRotation().getAngles(RotationOrder.ZYX)[2];
        System.out.println("Psi / GCRF  = "+FastMath.toDegrees(psi)+" deg");
        System.out.println("Teta / GCRF = "+FastMath.toDegrees(teta)+" deg");
        System.out.println("Phi / GCRF  = "+FastMath.toDegrees(phi)+" deg");
 
    }
 
}
package propagator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.ode.FirstOrderIntegrator;
import fr.cnes.sirius.patrius.math.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.ApsisOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.OrbitType;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisRadiusParameters;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.propagation.numerical.NumericalPropagator;
import fr.cnes.sirius.patrius.propagation.sampling.PatriusFixedStepHandler;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;
import fr.cnes.sirius.patrius.utils.exception.PropagationException;

public class NumericalPropagationWithFixedStepHandler {
	 
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
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
        // Creation of a fixed step handler
        final ArrayList<SpacecraftState> listOfStates = new ArrayList<SpacecraftState>();
        final PatriusFixedStepHandler myStepHandler = new PatriusFixedStepHandler() {
            private static final long serialVersionUID = 1L;
            public void init(final SpacecraftState s0, final AbsoluteDate t) {
                // Nothing to do ...
            }
            public void handleStep(final SpacecraftState currentState, final boolean isLast)
                    throws PropagationException {
                // Adding S/C to the list
                listOfStates.add(currentState);
            }
        };
        // The handler frequency is set to 10S
        propagator.setMasterMode(10., myStepHandler);
        //SPECIFIC
 
        // Propagating 100s
        final double dt = 101.;
        final AbsoluteDate finalDate = date.shiftedBy(dt);
        final SpacecraftState finalState = propagator.propagate(finalDate);
 
        // Display data at each step
        System.out.println(iniState.getDate().toString(TUC)+" ; LV = "+FastMath.toDegrees(iniState.getLv())+ " deg");
        for (final SpacecraftState sc : listOfStates) {
            System.out.println(sc.getDate().toString(TUC)+" ; LV = "+FastMath.toDegrees(sc.getLv())+ " deg");
        }
        System.out.println(finalState.getDate().toString(TUC)+" ; LV = "+FastMath.toDegrees(finalState.getLv())+ " deg");
 
    }
 
}

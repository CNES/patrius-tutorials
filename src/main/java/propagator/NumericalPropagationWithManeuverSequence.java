package propagator;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.AssemblyBuilder;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.properties.MassProperty;
import fr.cnes.sirius.patrius.assembly.properties.PropulsiveProperty;
import fr.cnes.sirius.patrius.assembly.properties.TankProperty;
import fr.cnes.sirius.patrius.attitudes.AttitudeLaw;
import fr.cnes.sirius.patrius.attitudes.LofOffset;
import fr.cnes.sirius.patrius.forces.maneuvers.ContinuousThrustManeuver;
import fr.cnes.sirius.patrius.forces.maneuvers.ImpulseManeuver;
import fr.cnes.sirius.patrius.forces.maneuvers.ManeuversSequence;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.frames.LOFType;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.RotationOrder;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.math.ode.FirstOrderIntegrator;
import fr.cnes.sirius.patrius.math.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.ApsisOrbit;
import fr.cnes.sirius.patrius.orbits.Orbit;
import fr.cnes.sirius.patrius.orbits.OrbitType;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.orbits.orbitalparameters.ApsisRadiusParameters;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.propagation.events.DateDetector;
import fr.cnes.sirius.patrius.propagation.events.EventDetector;
import fr.cnes.sirius.patrius.propagation.numerical.NumericalPropagator;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class NumericalPropagationWithManeuverSequence {
	 
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
 
        //SPECIFIC
        // Creating a mass model (see also specific example)
        final AssemblyBuilder builder = new AssemblyBuilder();
 
        // Main part
        final double iniMass = 900.;
        builder.addMainPart("MAIN");
        builder.addProperty(new MassProperty(iniMass), "MAIN");
 
        // Tank part (ergols mass)
        final double ergolsMass = 100.;
        final TankProperty tank = new TankProperty(ergolsMass);
        builder.addPart("TANK", "MAIN", Transform.IDENTITY);
        builder.addProperty(tank, "TANK");
 
        // Engine part
        final double isp = 300.;
        final double thrust = 400.;
        final PropulsiveProperty prop = new PropulsiveProperty(thrust, isp); // au lieu de new PropulsiveProperty("PROP", thrust, isp);
 
        builder.addPart("PROP", "MAIN", Transform.IDENTITY);
        builder.addProperty(prop, "PROP");
 
        final Assembly assembly = builder.returnAssembly();
        final MassProvider mm = new MassModel(assembly);
 
        // We create a spacecratftstate
        final SpacecraftState iniState = new SpacecraftState(iniOrbit, mm);
        //SPECIFIC
 
        // Initialization of the Runge Kutta integrator with a 2 s step
        final double pasRk = 2.;
        final FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(pasRk);
 
        // Initialization of the propagator
        final NumericalPropagator propagator = new NumericalPropagator(integrator);
        propagator.resetInitialState(iniState);
 
        // Forcing integration using cartesian equations
        propagator.setOrbitType(OrbitType.CARTESIAN);
 
        //SPECIFIC
        // Event corresponding to the criteria to trigger the impulsive maneuver
        final EventDetector event = new DateDetector(date);
        // Creation of the impulsive maneuver
        final Vector3D deltaV = new Vector3D(20., 0., 0.);
        final ImpulseManeuver imp = new ImpulseManeuver(event, deltaV, prop, mm, tank, LOFType.TNW);
 
        // Duration of the maneuver to reach the initial semi major axis
        final double duration = 51.03781404091;
        // Creation of the continuous thrust maneuver
        final AbsoluteDate startDate = date.shiftedBy(0.5*(iniOrbit.getKeplerianPeriod()-duration));
        final EventDetector eventStart = new DateDetector(startDate);
        final EventDetector eventEnd = new DateDetector(startDate.shiftedBy(duration));
        final Vector3D direction = new Vector3D(-1., 0., 0.);
        final ContinuousThrustManeuver man = new ContinuousThrustManeuver(eventStart, eventEnd, prop, direction, mm, tank);
 
        // Creation of the sequence of maneuver
        final ManeuversSequence seq = new ManeuversSequence(0., 0.);
        seq.add(imp);
        seq.add(man);
 
        // Adding the maneuver sequence to the propagator
        seq.applyTo(propagator);
        // Adding additional state (change name add to set for V3.3)
        propagator.setMassProviderEquation(mm);
 
        // Adding an attitude law (or attitude sequence : mandatory)
        final AttitudeLaw attitudeLaw = new LofOffset(LOFType.TNW, RotationOrder.ZYX, 0., 0., 0.);
        propagator.setAttitudeProvider(attitudeLaw);
        //SPECIFIC
 
        // Propagating 100s
        final double dt = iniOrbit.getKeplerianPeriod();
        final AbsoluteDate finalDate = date.shiftedBy(dt);
        final SpacecraftState finalState = propagator.propagate(finalDate);
        final Orbit finalOrbit = finalState.getOrbit();
 
        // Printing new date and semi major axis
        System.out.println();
        System.out.println("Initial semi major axis = "+iniOrbit.getA()/1000.+" km");
        System.out.println("New date = "+finalOrbit.getDate().toString(TUC)+" deg");
        System.out.println("Final semi major axis = "+finalOrbit.getA()/1000.+" km");
        // Printing mass
        System.out.println();
        System.out.println("Dry Mass = "+finalState.getMass("MAIN")+" kg");
        System.out.println("Ergols Mass = "+finalState.getMass("TANK")+" kg");
 
        System.out.println(((DateDetector)event).getDate().toString(TUC));
        System.out.println(((DateDetector)eventStart).getDate().toString(TUC));
        System.out.println(((DateDetector)eventEnd).getDate().toString(TUC));
 
    }
 
}

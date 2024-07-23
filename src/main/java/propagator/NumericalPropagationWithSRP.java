package propagator;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.AssemblyBuilder;
import fr.cnes.sirius.patrius.assembly.models.DirectRadiativeModel;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.models.RediffusedRadiativeModel;
import fr.cnes.sirius.patrius.assembly.properties.MassProperty;
import fr.cnes.sirius.patrius.assembly.properties.RadiativeIRProperty;
import fr.cnes.sirius.patrius.assembly.properties.RadiativeProperty;
import fr.cnes.sirius.patrius.assembly.properties.RadiativeSphereProperty;
import fr.cnes.sirius.patrius.bodies.CelestialBody;
import fr.cnes.sirius.patrius.bodies.EllipsoidBodyShape;
import fr.cnes.sirius.patrius.bodies.OneAxisEllipsoid;
import fr.cnes.sirius.patrius.bodies.MeeusSun;
import fr.cnes.sirius.patrius.forces.radiation.IEmissivityModel;
import fr.cnes.sirius.patrius.forces.radiation.KnockeRiesModel;
import fr.cnes.sirius.patrius.forces.radiation.RediffusedRadiationPressure;
import fr.cnes.sirius.patrius.forces.gravity.GravityModel;
import fr.cnes.sirius.patrius.forces.gravity.ThirdBodyAttraction;
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
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.propagation.numerical.NumericalPropagator;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.Constants;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class NumericalPropagationWithSRP {
	
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
 
        // Initial orbit
        final double sma = 7000.e+3;
        final double exc = 0.;
        final double per = sma*(1.-exc);
        final double apo = sma*(1.+exc);
        final double inc = FastMath.toRadians(98.);
        final double pa = FastMath.toRadians(0.);
        final double raan = FastMath.toRadians(0.);
        final double anm = FastMath.toRadians(0.);
        final double MU = Constants.WGS84_EARTH_MU;
 
        final ApsisRadiusParameters par = new ApsisRadiusParameters(per, apo, inc, pa, raan, anm, PositionAngle.MEAN, MU);
        final Orbit iniOrbit = new ApsisOrbit(par, GCRF, date);
 
        // Mass model using an Assembly
 
        final AssemblyBuilder builder = new AssemblyBuilder();
 
        // Initial mass (mandatory to take into account mass for atmospheric force computation)
        final double dryMass = 100.;
        builder.addMainPart("MAIN");
        builder.addProperty(new MassProperty(dryMass), "MAIN");
 
        //SPECIFIC
        final double ka = 1.0;
        final double ks = 0.0;
        final double kd = 0.0;
        builder.addProperty(new RadiativeProperty(ka, ks, kd), "MAIN");
        builder.addProperty(new RadiativeIRProperty(ka, ks, kd), "MAIN");
        final double radius = 10.;
        builder.addProperty(new RadiativeSphereProperty(radius), "MAIN");
        //SPECIFIC
 
        final Assembly assembly = builder.returnAssembly();
 
//SPECIFIC
        // Sun ephemeris
        final CelestialBody sun = new MeeusSun();
 
        // Definition of the Earth ellipsoid for later SRP computation
        final Frame ITRF = FramesFactory.getITRF();
        final double AE = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        final EllipsoidBodyShape EARTH = new OneAxisEllipsoid(AE, Constants.WGS84_EARTH_FLATTENING, ITRF, "EARTH");
 
        // Direct SRP data
        final double dRef = 1.4959787E11;
        final double pRef = 4.5605E-6;
        final DirectRadiativeModel rm = new DirectRadiativeModel(assembly);
        final GravityModel radPres = new MeeusSun().getGravityModel();
        final ThirdBodyAttraction sunForceModel = new ThirdBodyAttraction(radPres);
 
        // Rediffused DRP data
        final int inCorona = 1;
        final int inMeridian = 10;
        final IEmissivityModel inEmissivityModel = new KnockeRiesModel();
        final boolean inAlbedo = true;
        final boolean inIr = true;
        final double coefAlbedo = 1.;
        final double coefIr = 1.;
        final RediffusedRadiativeModel rdm = new RediffusedRadiativeModel(inAlbedo, inIr, coefAlbedo, coefIr, assembly);
        final RediffusedRadiationPressure reDiff =  new RediffusedRadiationPressure(sun, GCRF, inCorona, inMeridian, inEmissivityModel, rdm);
        //SPECIFIC
 
        final MassProvider mm = new MassModel(assembly);
 
        // We create a spacecratftstate
        final SpacecraftState iniState = new SpacecraftState(iniOrbit, mm);
 
        // Initialization of the Runge Kutta integrator with a 2 s step
        final double pasRk = 2.;
        final FirstOrderIntegrator integrator = new ClassicalRungeKuttaIntegrator(pasRk);
 
        // Initialization of the propagator
        final NumericalPropagator propagator = new NumericalPropagator(integrator, iniState.getFrame(), 
        		OrbitType.CARTESIAN, PositionAngle.TRUE);
        propagator.resetInitialState(iniState);
 
        // Adding additional state (change name add to set for V3.3)
        propagator.setMassProviderEquation(mm);
 
        //SPECIFIC
        // Adding SRP forces
        propagator.addForceModel(sunForceModel);        
        propagator.addForceModel(reDiff);        
        //SPECIFIC
 
        // Propagating 5 periods
        final double dt = 5.*iniOrbit.getKeplerianPeriod();
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
        System.out.println("Mass = "+finalState.getMass("MAIN")+" kg");
 
    }
 
}


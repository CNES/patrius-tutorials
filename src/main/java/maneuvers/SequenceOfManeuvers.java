package maneuvers;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.AssemblyBuilder;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.properties.MassProperty;
import fr.cnes.sirius.patrius.assembly.properties.PropulsiveProperty;
import fr.cnes.sirius.patrius.assembly.properties.TankProperty;
import fr.cnes.sirius.patrius.forces.maneuvers.ContinuousThrustManeuver;
import fr.cnes.sirius.patrius.forces.maneuvers.ImpulseManeuver;
import fr.cnes.sirius.patrius.forces.maneuvers.ManeuversSequence;
import fr.cnes.sirius.patrius.frames.LOFType;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.events.detectors.AnomalyDetector;
import fr.cnes.sirius.patrius.events.EventDetector;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class SequenceOfManeuvers {
	 
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Creating a mass model with a main part and with a tank
        final AssemblyBuilder builder = new AssemblyBuilder();
 
        // Main part (dry mass)
        final double dryMass = 1000.;
        builder.addMainPart("MAIN");
        builder.addProperty(new MassProperty(dryMass), "MAIN");
 
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
 
        // SPECIFIC IMPULSIVE MANEUVER
        // Event corresponding to the criteria to trigger the impulsive maneuver
        // (when the S/C is at the apogee)
        final EventDetector event = new AnomalyDetector(PositionAngle.TRUE, FastMath.PI);
        // Creation of the impulsive maneuver (20 m/s int the x vehicle direction)
        final Vector3D deltaV = new Vector3D(20., 0., 0.);
        final ImpulseManeuver imp = new ImpulseManeuver(event, deltaV, prop, mm, tank, LOFType.TNW);
 
        // SPECIFIC CONTINUOUS MANEUVER
        // Duration of the continuous maneuver to get a 20 m/s boost
        final AbsoluteDate startDate = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
        final double G0 = 9.80665;
        final double duration = G0*isp*mm.getTotalMass()*(1. - FastMath.exp(-20/(G0*isp)))/thrust;
        // Direction of the thrust in the X vehicle axis
        final Vector3D direction = new Vector3D(1., 0., 0.);
        // Creation of the continuous thrust maneuver
        final ContinuousThrustManeuver man = new ContinuousThrustManeuver(startDate, duration, prop, direction, mm, tank, LOFType.TNW);
 
        // SPECIFIC SEQUENCE OF MANEUVERS
        final ManeuversSequence seq = new ManeuversSequence(10., 10.);
        seq.add(imp);
        seq.add(man);
 
        System.out.println("Amount of maneuvers: "+seq.getSize());
 
    }
 
}
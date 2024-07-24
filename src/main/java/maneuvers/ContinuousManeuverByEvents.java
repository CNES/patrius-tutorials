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
import fr.cnes.sirius.patrius.frames.LOFType;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.events.detectors.DateDetector;
import fr.cnes.sirius.patrius.events.EventDetector;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class ContinuousManeuverByEvents {
	 
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
 
        //SPECIFIC
        // Duration of the maneuver to get a 20 m/s boost
        final AbsoluteDate startDate = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
        final double G0 = 9.80665;
        final double duration = G0*isp*mm.getTotalMass()*(1. - FastMath.exp(-20/(G0*isp)))/thrust;
        // Start and end thrust events
        final EventDetector eventStart = new DateDetector(startDate);
        final EventDetector eventEnd = new DateDetector(startDate.shiftedBy(duration));
        // Direction of the thrust in the X vehicle axis
        final Vector3D direction = new Vector3D(1., 0., 0.);
        // Creation of the continuous thrust maneuver
        final ContinuousThrustManeuver man = new ContinuousThrustManeuver(eventStart, eventEnd, prop, direction, mm, tank, LOFType.TNW);
        //SPECIFIC
 
        System.out.println("End of the thrust: "+man.getEndDate());
        System.out.println("Duration of the thrust: "+duration+" s");
        System.out.println("Duration of the thrust: "+man.getEndDate().durationFrom(startDate)+" s");
        // The getFrame() method is returning "null" as a LOF frame is not define as a frame.
        // Nevertheless, an attitude law will not be mandatory when propagating the orbit.
        System.out.println("Maneuver frame: "+man.getFrame());
 
    }
 
}

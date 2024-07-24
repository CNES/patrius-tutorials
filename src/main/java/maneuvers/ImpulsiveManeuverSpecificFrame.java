package maneuvers;

import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.AssemblyBuilder;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.properties.MassProperty;
import fr.cnes.sirius.patrius.forces.maneuvers.ImpulseManeuver;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.events.detectors.AnomalyDetector;
import fr.cnes.sirius.patrius.events.EventDetector;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class ImpulsiveManeuverSpecificFrame {
	 
    public static void main(final String[] args) throws PatriusException {
 
        // Creating a mass model with a main part and with a tank
        final AssemblyBuilder builder = new AssemblyBuilder();
 
        // Main part (dry mass)
        final double dryMass = 1000.;
        builder.addMainPart("MAIN");
        builder.addProperty(new MassProperty(dryMass), "MAIN");
 
        // Tank part (ergols mass)
        final double ergolsMass = 100.;
        builder.addPart("TANK", "MAIN", Transform.IDENTITY);
        builder.addProperty(new MassProperty(ergolsMass), "TANK");
 
        final Assembly assembly = builder.returnAssembly();
        final MassProvider mm = new MassModel(assembly);
 
        // Event corresponding to the criteria to trigger the impulsive maneuver
        // (when the S/C is at the apogee)
        final EventDetector event = new AnomalyDetector(PositionAngle.TRUE, FastMath.PI);
 
        // Creation of the impulsive maneuver (20 m/s int the x vehicle direction)
        final Vector3D deltaV = new Vector3D(20., 0., 0.);
        final double isp = 300.;
        final Frame GCRF = FramesFactory.getGCRF();
        // SPECIFIC
        final ImpulseManeuver imp = new ImpulseManeuver(event, deltaV, GCRF, isp, mm, "TANK");
        // SPECIFIC
 
        System.out.println("DV components: "+imp.getDeltaVSat());
        System.out.println("Maneuver frame: "+imp.getFrame());
 
    }
 
}

package vehicle;

import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.Vehicle;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Sphere;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class UsingVehicleClass {
	 
    public static void main(final String[] args) throws PatriusException {
 
        final Vehicle veh = new Vehicle();
 
        // Dry mass
        final double dryMass = 1000.;
        veh.setDryMass(dryMass);
 
        // Shape : A vehicle object must have a main shape !
        final double lref = 1.;
        veh.setMainShape(new Sphere(Vector3D.ZERO, lref));
 
        // Getting the corresponding assembly
        final Assembly assembly = veh.createAssembly(FramesFactory.getCIRF());
 
        // Getting the corresponding mass model (useful for propagation, maneuvres, ...)
        final MassProvider mm = new MassModel(assembly);
 
        System.out.println("Name of the main part: " + assembly.getMainPart().getName());        
        System.out.println("Total mass: " + mm.getTotalMass());
 
    }
 
}

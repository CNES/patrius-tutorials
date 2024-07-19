package vehicle;

import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.Vehicle;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.properties.PropulsiveProperty;
import fr.cnes.sirius.patrius.assembly.properties.TankProperty;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Sphere;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.Vector3D;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class UsingVehicleClassWithPropulsiveAndTankProperty {
	 
    public static void main(final String[] args) throws PatriusException {
 
        final Vehicle veh = new Vehicle();
 
        // Dry mass
        final double dryMass = 1000.;
        veh.setDryMass(dryMass);
 
        // Shape : A vehicle object must have a main shape !
        final double lref = 1.;
        veh.setMainShape(new Sphere(Vector3D.ZERO, lref));
 
        // Propulsive properties
 
        // Tanks
        final double merg1 = 100.;
        final TankProperty tank1 = new TankProperty(merg1);
        final double merg2 = 200.;
        final TankProperty tank2 = new TankProperty(merg2);
        veh.addTank("TANK1", tank1);
        veh.addTank("TANK2", tank2);
 
        // Engine
        final double thrust = 400.;
        final double isp = 320.;
        final PropulsiveProperty engine = new PropulsiveProperty(thrust, isp);
        veh.addEngine("PROP", engine);
 
        // Getting the corresponding assembly
        final Assembly assembly = veh.createAssembly(FramesFactory.getCIRF());
 
        // Getting the corresponding mass model (useful for propagation, maneuvres, ...)
        final MassProvider mm = new MassModel(assembly);
 
        System.out.println("Name of the main part: " + assembly.getMainPart().getName());        
        System.out.println("Total mass: " + mm.getTotalMass());
 
        for (int i = 0; i < veh.getEnginesList().size(); i++) {
            System.out.println(veh.getEnginesList().get(i).getPartName());
            System.out.println("Thrust = "+veh.getEnginesList().get(i).getThrust(null)+" N");
            System.out.println("Isp = "+veh.getEnginesList().get(i).getIsp(null)+ "s");
        }
 
        for (int i = 0; i < veh.getTanksList().size(); i++) {
            System.out.println(veh.getTanksList().get(i).getPartName()+": "+veh.getTanksList().get(i).getMass()+" kg");
        }
 
    }
 
}

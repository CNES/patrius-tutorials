package vehicle;

import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.AssemblyBuilder;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.properties.MassProperty;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class VehicleWithMassProperty {
	 
    /**
     * @param args
     * @throws OrekitException 
     */
    public static void main(final String[] args) throws PatriusException {
 
        // Using an assembly builder
        final AssemblyBuilder builder = new AssemblyBuilder();
 
        // Main part (the only one in this example)
        final double dryMass = 1000.;
        builder.addMainPart("MAIN");
        builder.addProperty(new MassProperty(dryMass), "MAIN");
 
        // Getting the corresponding assembly
        final Assembly assembly = builder.returnAssembly();
 
        // Getting the corresponding mass model (useful for propagation, maneuvres, ...)
        final MassProvider mm = new MassModel(assembly);
 
        System.out.println("Name of the main part: " + assembly.getMainPart().getName());        
        System.out.println("Total mass: " + mm.getTotalMass());
 
    }
 
}

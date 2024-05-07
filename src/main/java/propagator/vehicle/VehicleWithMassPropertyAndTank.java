package vehicle;

import fr.cnes.sirius.patrius.assembly.Assembly;
import fr.cnes.sirius.patrius.assembly.AssemblyBuilder;
import fr.cnes.sirius.patrius.assembly.models.MassModel;
import fr.cnes.sirius.patrius.assembly.properties.MassProperty;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.propagation.MassProvider;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class VehicleWithMassPropertyAndTank {
	 
    public static void main(final String[] args) throws PatriusException {
 
        // Using an assembly builder
        final AssemblyBuilder builder = new AssemblyBuilder();
 
        // Main part (for example dry mass)
        final double dryMass = 1000.;
        builder.addMainPart("MAIN");
        builder.addProperty(new MassProperty(dryMass), "MAIN");
 
        // SPECIFIC
        // Tank part (ergols mass) ; considering centered vs the main part
        final double ergolsMass = 100.;
        builder.addPart("TANK", "MAIN", Transform.IDENTITY);
        builder.addProperty(new MassProperty(ergolsMass), "TANK");
        // SPECIFIC
 
        // Getting the corresponding assembly
        final Assembly assembly = builder.returnAssembly();
 
        // Getting the corresponding mass model (useful for propagation, maneuvres, ...)
        final MassProvider mm = new MassModel(assembly);
 
        int iPart = 0;
        for (final String name : mm.getAllPartsNames()) {
            System.out.println("Part #"+ (iPart++));
            System.out.println("  Name :" + name);
            System.out.println("  Mass :" + mm.getMass(name));
        }
 
        System.out.println("\nTotal mass: " + mm.getTotalMass());
 
    }
 
}
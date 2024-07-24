package frames;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.frames.Frame;
import fr.cnes.sirius.patrius.frames.FramesFactory;
import fr.cnes.sirius.patrius.frames.configuration.DiurnalRotation;
import fr.cnes.sirius.patrius.frames.configuration.FramesConfiguration;
import fr.cnes.sirius.patrius.frames.configuration.FramesConfigurationBuilder;
import fr.cnes.sirius.patrius.frames.configuration.PolarMotion;
import fr.cnes.sirius.patrius.frames.configuration.precessionnutation.PrecessionNutation;
import fr.cnes.sirius.patrius.frames.configuration.eop.NoEOP2000History;
import fr.cnes.sirius.patrius.frames.configuration.libration.LibrationCorrectionModel;
import fr.cnes.sirius.patrius.frames.configuration.libration.LibrationCorrectionModelFactory;
import fr.cnes.sirius.patrius.frames.configuration.precessionnutation.PrecessionNutationModelFactory;
import fr.cnes.sirius.patrius.frames.configuration.sp.SPrimeModelFactory;
import fr.cnes.sirius.patrius.frames.configuration.tides.TidalCorrectionModel;
import fr.cnes.sirius.patrius.frames.configuration.tides.TidalCorrectionModelFactory;
import fr.cnes.sirius.patrius.frames.transformations.Transform;
import fr.cnes.sirius.patrius.math.geometry.euclidean.threed.RotationOrder;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class ConfigureFrames {
	 
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
        PatriusDataset.addResourcesFromPatriusDataset();
 
        // Date of the orbit (given in UTC time scale)
        final TimeScale TUC = TimeScalesFactory.getUTC();
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        // Storing by default configuration
        final FramesConfiguration configDefault = FramesFactory.getConfiguration();
 
        // First configuration as simple as possible ...
        FramesConfiguration config = getSimplifiedConfiguration(false);
        FramesFactory.setConfiguration(config);
 
        // Corresponding GCRF frame
        final Frame gcrfNoPN = FramesFactory.getGCRF();      
        // Corresponding GCRF frame
        final Frame icrfNoPN = FramesFactory.getCIRF();
 
        // Printing transform frame information
        print (gcrfNoPN, icrfNoPN, date);
 
        // Second configuration with precession and nutation ...
        config = getSimplifiedConfiguration(true);
        FramesFactory.setConfiguration(config);
 
        // GCRF frame
        final Frame gcrfPN = FramesFactory.getGCRF();        
        // GCRF frame
        final Frame icrfPN = FramesFactory.getCIRF();
 
        // Printing frame information
        print (gcrfPN, icrfPN, date);
 
        //Setting by default configuration
        FramesFactory.setConfiguration(configDefault);
 
        // GCRF frame
        final Frame gcrfDef = FramesFactory.getGCRF();        
        // GCRF frame
        final Frame icrfDef = FramesFactory.getCIRF();
 
        // Printing frame information
        print (gcrfDef, icrfDef, date);
 
    }
 
    /**
     * Method to print results (transformation from frame1 to frame2).
     * @param frame1        First frame
     * @param frame2        Second frame
     * @param date          Date when we want to compare
     * @throws OrekitException
     */
    private static void print ( final Frame frame1, final Frame frame2, final AbsoluteDate date ) throws PatriusException {
        final Transform transform = frame1.getTransformTo(frame2, date);
        System.out.println();
        System.out.println("Psi: "+transform.getRotation().getAngles(RotationOrder.ZYX)[0]);
        System.out.println("Teta: "+transform.getRotation().getAngles(RotationOrder.ZYX)[1]);
        System.out.println("Phi: "+transform.getRotation().getAngles(RotationOrder.ZYX)[2]);
    }
 
    /**
     * Method to configure frames configuration.
     * @param isPrecNut  if true => precession & nutation
     * @return frame configuration object
     */
    private static FramesConfiguration getSimplifiedConfiguration( final boolean isPrecNut ) {
 
        // Configurations builder
        final FramesConfigurationBuilder builder = new FramesConfigurationBuilder();
 
        // Tides and libration
        final TidalCorrectionModel tides = TidalCorrectionModelFactory.NO_TIDE;
        final LibrationCorrectionModel lib = LibrationCorrectionModelFactory.NO_LIBRATION;
 
        // Polar Motion
        final PolarMotion defaultPolarMotion = new PolarMotion(false, tides, lib, SPrimeModelFactory.NO_SP);
 
        // Diurnal rotation
        final DiurnalRotation defaultDiurnalRotation = new DiurnalRotation(tides, lib);
 
        // Precession Nutation
        PrecessionNutation precNut = null;
        if ( isPrecNut ) {
            precNut = new PrecessionNutation(false,
                    PrecessionNutationModelFactory.PN_IERS2010_INTERPOLATED_NON_CONSTANT_OLD);
        } else {
            precNut = new PrecessionNutation(false,
                    PrecessionNutationModelFactory.NO_PN);
        }
 
        builder.setDiurnalRotation(defaultDiurnalRotation);
        builder.setPolarMotion(defaultPolarMotion);
        builder.setCIRFPrecessionNutation(precNut);
        builder.setEOPHistory(new NoEOP2000History());
 
        return builder.getConfiguration();
 
    }
 
}

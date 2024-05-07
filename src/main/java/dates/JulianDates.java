package dates;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;


public class JulianDates {
	 
    public static void main(final String[] args) throws PatriusException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit (given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        // Printing date in TUC and TAI scale (by default)
        System.out.println();
        System.out.println(date.toString(TUC));
        System.out.println(date.toString());
 
        final double dateJJ = date.toCNESJulianDate(TUC);        
        System.out.println();
        System.out.println(dateJJ);
 
        // Other way to initialize the absolute date
        final AbsoluteDate dateBis = new AbsoluteDate(dateJJ, TUC);
        System.out.println();
        System.out.println("Comparizon between both dates = "+dateBis.compareTo(date) +" s");
 
    }
 
}
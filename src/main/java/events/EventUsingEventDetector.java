package events;

import java.io.IOException;
import java.net.URISyntaxException;

import fr.cnes.sirius.addons.patriusdataset.PatriusDataset;
import fr.cnes.sirius.patrius.propagation.SpacecraftState;
import fr.cnes.sirius.patrius.events.AbstractDetector;
import fr.cnes.sirius.patrius.events.EventDetector;
import fr.cnes.sirius.patrius.time.AbsoluteDate;
import fr.cnes.sirius.patrius.time.TimeScale;
import fr.cnes.sirius.patrius.time.TimeScalesFactory;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class EventUsingEventDetector implements EventDetector {
	 
    private static final long serialVersionUID = 1L;
    private final AbsoluteDate date;
 
    /**
     * Constructor
     * @param date absolute date when event will occured.
     */
    public EventUsingEventDetector( final AbsoluteDate date ) {
        this.date = date;
    }
 
    public Action eventOccurred(final SpacecraftState s, final boolean increasing,
            final boolean forward) throws PatriusException {
        return EventDetector.Action.STOP;
    }
 
    public double g(final SpacecraftState s) throws PatriusException {
        return s.getDate().durationFrom(date);
    }
 
    public double getMaxCheckInterval() {
        return AbstractDetector.DEFAULT_MAXCHECK;
    }
 
    public int getMaxIterationCount() {
        return 20;
    }
 
    public int getSlopeSelection() {
        // TODO Auto-generated method stub
        return AbstractDetector.INCREASING_DECREASING;
    }
 
    public double getThreshold() {
        return AbstractDetector.DEFAULT_THRESHOLD;
    }
 
    public void init(final SpacecraftState s0, final AbsoluteDate t) {
        // Nothing specific to do ...
    }
 
    public SpacecraftState resetState(final SpacecraftState oldState)
            throws PatriusException {
        return oldState;
    }
 
    public boolean shouldBeRemoved() {
        return false;
    }
 
    public EventDetector copy() {
        final AbsoluteDate newDate = new AbsoluteDate(date, 0.);
        return new EventUsingEventDetector(newDate);
    }
 
    /**
     * @param args
     * @throws PatriusException 
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public static void main(final String[] args) throws PatriusException, IOException, URISyntaxException {
 
        // Patrius Dataset initialization (needed for example to get the UTC time)
        PatriusDataset.addResourcesFromPatriusDataset() ;
 
        // Recovery of the UTC time scale using a "factory" (not to duplicate such unique object)
        final TimeScale TUC = TimeScalesFactory.getUTC();
 
        // Date of the orbit (given in UTC time scale)
        final AbsoluteDate date = new AbsoluteDate("2010-01-01T12:00:00.000", TUC);
 
        final EventUsingEventDetector event = new EventUsingEventDetector(date);
 
        System.out.println("Max check interval of the event: " + event.getMaxCheckInterval() + " s");
        System.out.println("Threshold of the event: " + event.getThreshold() + " s");
        System.out.println("Remove the event after occuring: " + event.shouldBeRemoved());
 
    }

	@Override
	public boolean filterEvent(SpacecraftState state, boolean increasing, boolean forward) throws PatriusException {
		return false;
	}
 
}
package events;

import java.util.ArrayList;

import fr.cnes.sirius.patrius.math.util.FastMath;
import fr.cnes.sirius.patrius.orbits.PositionAngle;
import fr.cnes.sirius.patrius.events.AbstractDetector;
import fr.cnes.sirius.patrius.events.detectors.AnomalyDetector;
import fr.cnes.sirius.patrius.events.EventDetector.Action;
import fr.cnes.sirius.patrius.utils.exception.PatriusException;

public class AnomalyEvent {
	 
    public static void main(final String[] args) throws PatriusException {
 
        final ArrayList<AnomalyDetector> detectorList = new ArrayList<AnomalyDetector>();
 
        // AOL event with default values
        final AnomalyDetector anoEvent1 = new AnomalyDetector(PositionAngle.TRUE, FastMath.PI);
        detectorList.add(anoEvent1);
 
        // Same event with customized convergence values
        final double maxCheck = 0.5 * AbstractDetector.DEFAULT_MAXCHECK;
        final double threshold = 2. * AbstractDetector.DEFAULT_THRESHOLD;
        final AnomalyDetector anoEvent2 = new AnomalyDetector(PositionAngle.TRUE, FastMath.PI, maxCheck, threshold);
        detectorList.add(anoEvent2);
 
        // Same event with customized convergence values and specifying the action to be done when events occured
        final AnomalyDetector anoEvent3 = new AnomalyDetector(PositionAngle.TRUE, FastMath.PI, maxCheck, threshold, Action.STOP);
        detectorList.add(anoEvent3);
 
        // Same event with customized convergence values, specifying the action to be done when events occured
        // and specifying the event is removed once it is occured
        final AnomalyDetector anoEvent4 = new AnomalyDetector(PositionAngle.TRUE, FastMath.PI, maxCheck, threshold, Action.STOP, true);
        detectorList.add(anoEvent4);
 
        for ( final AnomalyDetector anomalyDetector : detectorList ) {
            System.out.println("Anomaly of the event: " + FastMath.toDegrees(anomalyDetector.getAnomaly()) + " deg");
            System.out.println("Anomaly of the event: " + anomalyDetector.getAnomalyType());
            System.out.println("Max check interval of the event: " + anomalyDetector.getMaxCheckInterval() + " s");
            System.out.println("Threshold of the event: " + anomalyDetector.getThreshold() + " s");
            System.out.println("Remove the event after occuring: " + anomalyDetector.shouldBeRemoved());
            System.out.println();
            System.out.println("Slope selection of the event: " + anomalyDetector.getSlopeSelection());
            System.out.println("Max iteration count of the event: " + anomalyDetector.getMaxIterationCount());
            System.out.println();
        }
 
    }
 
}

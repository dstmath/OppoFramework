package android.location;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@SystemApi
public class GpsMeasurementsEvent implements Parcelable {
    public static final Parcelable.Creator<GpsMeasurementsEvent> CREATOR = new Parcelable.Creator<GpsMeasurementsEvent>() {
        /* class android.location.GpsMeasurementsEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public GpsMeasurementsEvent createFromParcel(Parcel in) {
            GpsMeasurement[] measurementsArray = new GpsMeasurement[in.readInt()];
            in.readTypedArray(measurementsArray, GpsMeasurement.CREATOR);
            return new GpsMeasurementsEvent((GpsClock) in.readParcelable(getClass().getClassLoader()), measurementsArray);
        }

        @Override // android.os.Parcelable.Creator
        public GpsMeasurementsEvent[] newArray(int size) {
            return new GpsMeasurementsEvent[size];
        }
    };
    public static final int STATUS_GPS_LOCATION_DISABLED = 2;
    public static final int STATUS_NOT_SUPPORTED = 0;
    public static final int STATUS_READY = 1;
    private final GpsClock mClock;
    private final Collection<GpsMeasurement> mReadOnlyMeasurements;

    @SystemApi
    public interface Listener {
        void onGpsMeasurementsReceived(GpsMeasurementsEvent gpsMeasurementsEvent);

        void onStatusChanged(int i);
    }

    public GpsMeasurementsEvent(GpsClock clock, GpsMeasurement[] measurements) {
        if (clock == null) {
            throw new InvalidParameterException("Parameter 'clock' must not be null.");
        } else if (measurements == null || measurements.length == 0) {
            throw new InvalidParameterException("Parameter 'measurements' must not be null or empty.");
        } else {
            this.mClock = clock;
            this.mReadOnlyMeasurements = Collections.unmodifiableCollection(Arrays.asList(measurements));
        }
    }

    public GpsClock getClock() {
        return this.mClock;
    }

    public Collection<GpsMeasurement> getMeasurements() {
        return this.mReadOnlyMeasurements;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeParcelable(this.mClock, flags);
        GpsMeasurement[] measurementsArray = (GpsMeasurement[]) this.mReadOnlyMeasurements.toArray(new GpsMeasurement[this.mReadOnlyMeasurements.size()]);
        parcel.writeInt(measurementsArray.length);
        parcel.writeTypedArray(measurementsArray, flags);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("[ GpsMeasurementsEvent:\n\n");
        builder.append(this.mClock.toString());
        builder.append("\n");
        for (GpsMeasurement measurement : this.mReadOnlyMeasurements) {
            builder.append(measurement.toString());
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }
}

package qcom.fmradio;

import java.util.ArrayList;
import java.util.List;

public class Spur {
    private byte NoOfSpursToTrack;
    private int SpurFreq;
    private List<SpurDetails> spurDetailsList;

    Spur() {
    }

    Spur(int SpurFreq, byte NoOfSpursToTrack, List<SpurDetails> spurDetailsList) {
        this.SpurFreq = SpurFreq;
        this.NoOfSpursToTrack = NoOfSpursToTrack;
        this.spurDetailsList = spurDetailsList;
    }

    public int getSpurFreq() {
        return this.SpurFreq;
    }

    public void setSpurFreq(int spurFreq) {
        this.SpurFreq = spurFreq;
    }

    public byte getNoOfSpursToTrack() {
        return this.NoOfSpursToTrack;
    }

    public void setNoOfSpursToTrack(byte noOfSpursToTrack) {
        this.NoOfSpursToTrack = noOfSpursToTrack;
    }

    public List<SpurDetails> getSpurDetailsList() {
        return this.spurDetailsList;
    }

    public void setSpurDetailsList(List<SpurDetails> spurDetailsList) {
        this.spurDetailsList = spurDetailsList;
    }

    public void addSpurDetails(SpurDetails spurDetails) {
        if (this.spurDetailsList == null) {
            this.spurDetailsList = new ArrayList();
        }
        this.spurDetailsList.add(spurDetails);
    }
}

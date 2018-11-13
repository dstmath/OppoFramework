package qcom.fmradio;

import java.util.ArrayList;
import java.util.List;

public class SpurTable {
    private byte mode = (byte) -1;
    private byte spurNoOfFreq = (byte) 0;
    private List<Spur> spurs = null;

    SpurTable() {
    }

    public List<Spur> GetSpurList() {
        return this.spurs;
    }

    public void SetspurNoOfFreq(byte spurNoOfFreq) {
        this.spurNoOfFreq = spurNoOfFreq;
    }

    public void SetMode(byte mode) {
        this.mode = mode;
    }

    public void InsertSpur(Spur s) {
        if (this.spurs == null) {
            this.spurs = new ArrayList();
        }
        this.spurs.add(s);
    }

    public byte GetMode() {
        return this.mode;
    }

    public byte GetspurNoOfFreq() {
        return this.spurNoOfFreq;
    }
}

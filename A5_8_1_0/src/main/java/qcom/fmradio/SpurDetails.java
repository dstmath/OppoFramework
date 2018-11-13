package qcom.fmradio;

public class SpurDetails {
    private byte FilterCoefficeint;
    private byte IsEnableSpur;
    private byte LsbOfIntegrationLength;
    private int RotationValue;
    private byte SpurLevel;

    public int getRotationValue() {
        return this.RotationValue;
    }

    public void setRotationValue(int rotationValue) {
        this.RotationValue = rotationValue;
    }

    public byte getLsbOfIntegrationLength() {
        return this.LsbOfIntegrationLength;
    }

    public void setLsbOfIntegrationLength(byte lsbOfIntegrationLength) {
        this.LsbOfIntegrationLength = lsbOfIntegrationLength;
    }

    public byte getFilterCoefficeint() {
        return this.FilterCoefficeint;
    }

    public void setFilterCoefficeint(byte filterCoefficeint) {
        this.FilterCoefficeint = filterCoefficeint;
    }

    public byte getIsEnableSpur() {
        return this.IsEnableSpur;
    }

    public void setIsEnableSpur(byte isEnableSpur) {
        this.IsEnableSpur = isEnableSpur;
    }

    public byte getSpurLevel() {
        return this.SpurLevel;
    }

    public void setSpurLevel(byte spurLevel) {
        this.SpurLevel = spurLevel;
    }

    SpurDetails(int RotationValue, byte LsbOfIntegrationLength, byte FilterCoefficeint, byte IsEnableSpur, byte SpurLevel) {
        this.RotationValue = RotationValue;
        this.LsbOfIntegrationLength = LsbOfIntegrationLength;
        this.IsEnableSpur = IsEnableSpur;
        this.SpurLevel = SpurLevel;
    }

    public SpurDetails(SpurDetails spurDetails) {
        if (spurDetails != null) {
            this.RotationValue = spurDetails.RotationValue;
            this.LsbOfIntegrationLength = spurDetails.LsbOfIntegrationLength;
            this.IsEnableSpur = spurDetails.IsEnableSpur;
            this.SpurLevel = spurDetails.SpurLevel;
        }
    }
}

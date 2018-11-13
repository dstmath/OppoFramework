package cm.android.mdm.manager;

import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.INetworkManager;
import cm.android.mdm.util.MethodSignature;
import java.util.List;
import java.util.Map;

public class NetworkBaseManager implements INetworkManager {
    public void addApn(Map<String, String> map) {
        throw new MdmException("Not implement yet");
    }

    public void removeApn(String apnId) {
        throw new MdmException("Not implement yet");
    }

    public void updateApn(Map<String, String> map, String apnId) {
        throw new MdmException("Not implement yet");
    }

    public void setPreferApn(String apnId) {
        throw new MdmException("Not implement yet");
    }

    public List<String> queryApn(Map<String, String> map) {
        throw new MdmException("Not implement yet");
    }

    public Map<String, String> getApnInfo(String apnId) {
        throw new MdmException("Not implement yet");
    }

    public void setNetworkRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }

    public void addNetworkRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    public void removeNetworkRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    public void removeNetworkRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }

    public List<String> getSupportMethods() {
        return MethodSignature.getMethodSignatures(getClass());
    }
}

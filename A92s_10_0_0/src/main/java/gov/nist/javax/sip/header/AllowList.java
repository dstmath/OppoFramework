package gov.nist.javax.sip.header;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class AllowList extends SIPHeaderList<Allow> {
    private static final long serialVersionUID = -4699795429662562358L;

    @Override // gov.nist.core.GenericObject, java.lang.Object, gov.nist.javax.sip.header.SIPHeaderList, javax.sip.header.Header
    public Object clone() {
        AllowList retval = new AllowList();
        retval.clonehlist(this.hlist);
        return retval;
    }

    public AllowList() {
        super(Allow.class, "Allow");
    }

    public ListIterator<String> getMethods() {
        LinkedList<String> ll = new LinkedList<>();
        for (Allow a : this.hlist) {
            ll.add(a.getMethod());
        }
        return ll.listIterator();
    }

    public void setMethods(List<String> methods) throws ParseException {
        ListIterator<String> it = methods.listIterator();
        while (it.hasNext()) {
            Allow allow = new Allow();
            allow.setMethod(it.next());
            add((SIPHeader) allow);
        }
    }
}

package com.alibaba.fastjson;

import java.io.IOException;

public interface JSONStreamAware {
    void writeJSONString(Appendable appendable) throws IOException;
}

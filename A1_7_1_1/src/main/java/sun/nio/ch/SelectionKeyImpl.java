package sun.nio.ch;

import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;

class SelectionKeyImpl extends AbstractSelectionKey {
    final SelChImpl channel;
    private int index;
    private volatile int interestOps;
    private int readyOps;
    final SelectorImpl selector;

    SelectionKeyImpl(SelChImpl ch, SelectorImpl sel) {
        this.channel = ch;
        this.selector = sel;
    }

    public SelectableChannel channel() {
        return (SelectableChannel) this.channel;
    }

    public Selector selector() {
        return this.selector;
    }

    int getIndex() {
        return this.index;
    }

    void setIndex(int i) {
        this.index = i;
    }

    private void ensureValid() {
        if (!isValid()) {
            throw new CancelledKeyException();
        }
    }

    public int interestOps() {
        ensureValid();
        return this.interestOps;
    }

    public SelectionKey interestOps(int ops) {
        ensureValid();
        return nioInterestOps(ops);
    }

    public int readyOps() {
        ensureValid();
        return this.readyOps;
    }

    void nioReadyOps(int ops) {
        this.readyOps = ops;
    }

    int nioReadyOps() {
        return this.readyOps;
    }

    SelectionKey nioInterestOps(int ops) {
        if (((~channel().validOps()) & ops) != 0) {
            throw new IllegalArgumentException();
        }
        this.channel.translateAndSetInterestOps(ops, this);
        this.interestOps = ops;
        return this;
    }

    int nioInterestOps() {
        return this.interestOps;
    }
}

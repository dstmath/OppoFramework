package android.icu.text;

import android.icu.lang.UCharacter;
import android.icu.lang.UProperty;
import java.text.CharacterIterator;

final class UnhandledBreakEngine implements LanguageBreakEngine {
    private final UnicodeSet[] fHandled = new UnicodeSet[5];

    public UnhandledBreakEngine() {
        for (int i = 0; i < this.fHandled.length; i++) {
            this.fHandled[i] = new UnicodeSet();
        }
    }

    public boolean handles(int c, int breakType) {
        if (breakType < 0 || breakType >= this.fHandled.length) {
            return false;
        }
        return this.fHandled[breakType].contains(c);
    }

    public int findBreaks(CharacterIterator text, int startPos, int endPos, boolean reverse, int breakType, DequeI foundBreaks) {
        text.setIndex(endPos);
        return 0;
    }

    public synchronized void handleChar(int c, int breakType) {
        if (breakType >= 0) {
            if (!(breakType >= this.fHandled.length || c == Integer.MAX_VALUE || this.fHandled[breakType].contains(c))) {
                this.fHandled[breakType].applyIntPropertyValue(UProperty.SCRIPT, UCharacter.getIntPropertyValue(c, UProperty.SCRIPT));
            }
        }
    }
}

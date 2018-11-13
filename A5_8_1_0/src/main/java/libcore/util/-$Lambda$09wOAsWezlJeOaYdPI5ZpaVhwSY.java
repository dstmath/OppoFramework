package libcore.util;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

final /* synthetic */ class -$Lambda$09wOAsWezlJeOaYdPI5ZpaVhwSY implements ReaderSupplier {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f136-$f0;

    /* renamed from: libcore.util.-$Lambda$09wOAsWezlJeOaYdPI5ZpaVhwSY$1 */
    final /* synthetic */ class AnonymousClass1 implements ReaderSupplier {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f137-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f138-$f1;

        private final /* synthetic */ Reader $m$0() {
            return Files.newBufferedReader((Path) this.f137-$f0, (Charset) this.f138-$f1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.f137-$f0 = obj;
            this.f138-$f1 = obj2;
        }

        public final Reader get() {
            return $m$0();
        }
    }

    private final /* synthetic */ Reader $m$0() {
        return new StringReader((String) this.f136-$f0);
    }

    public /* synthetic */ -$Lambda$09wOAsWezlJeOaYdPI5ZpaVhwSY(Object obj) {
        this.f136-$f0 = obj;
    }

    public final Reader get() {
        return $m$0();
    }
}

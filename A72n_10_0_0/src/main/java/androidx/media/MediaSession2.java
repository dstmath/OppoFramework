package androidx.media;

import android.annotation.TargetApi;
import android.os.Bundle;
import androidx.media.MediaInterface2;
import com.alibaba.fastjson.parser.JSONToken;

@TargetApi(JSONToken.FIELD_NAME)
public class MediaSession2 extends MediaInterface2.SessionPlayer implements AutoCloseable {
    private final SupportLibraryImpl mImpl;

    static abstract class SupportLibraryImpl extends MediaInterface2.SessionPlayer implements AutoCloseable {
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        try {
            this.mImpl.close();
        } catch (Exception e) {
        }
    }

    public static final class CommandButton {
        private SessionCommand2 mCommand;
        private String mDisplayName;
        private boolean mEnabled;
        private Bundle mExtras;
        private int mIconResId;

        private CommandButton(SessionCommand2 command, int iconResId, String displayName, Bundle extras, boolean enabled) {
            this.mCommand = command;
            this.mIconResId = iconResId;
            this.mDisplayName = displayName;
            this.mExtras = extras;
            this.mEnabled = enabled;
        }

        public static CommandButton fromBundle(Bundle bundle) {
            if (bundle == null) {
                return null;
            }
            Builder builder = new Builder();
            builder.setCommand(SessionCommand2.fromBundle(bundle.getBundle("android.media.media_session2.command_button.command")));
            builder.setIconResId(bundle.getInt("android.media.media_session2.command_button.icon_res_id", 0));
            builder.setDisplayName(bundle.getString("android.media.media_session2.command_button.display_name"));
            builder.setExtras(bundle.getBundle("android.media.media_session2.command_button.extras"));
            builder.setEnabled(bundle.getBoolean("android.media.media_session2.command_button.enabled"));
            try {
                return builder.build();
            } catch (IllegalStateException e) {
                return null;
            }
        }

        public static final class Builder {
            private SessionCommand2 mCommand;
            private String mDisplayName;
            private boolean mEnabled;
            private Bundle mExtras;
            private int mIconResId;

            public Builder setCommand(SessionCommand2 command) {
                this.mCommand = command;
                return this;
            }

            public Builder setIconResId(int resId) {
                this.mIconResId = resId;
                return this;
            }

            public Builder setDisplayName(String displayName) {
                this.mDisplayName = displayName;
                return this;
            }

            public Builder setEnabled(boolean enabled) {
                this.mEnabled = enabled;
                return this;
            }

            public Builder setExtras(Bundle extras) {
                this.mExtras = extras;
                return this;
            }

            public CommandButton build() {
                return new CommandButton(this.mCommand, this.mIconResId, this.mDisplayName, this.mExtras, this.mEnabled);
            }
        }
    }
}

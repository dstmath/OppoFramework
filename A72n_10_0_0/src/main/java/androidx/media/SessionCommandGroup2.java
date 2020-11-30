package androidx.media;

import android.os.Bundle;
import android.os.Parcelable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SessionCommandGroup2 {
    private Set<SessionCommand2> mCommands = new HashSet();

    public void addCommand(SessionCommand2 command) {
        if (command != null) {
            this.mCommands.add(command);
            return;
        }
        throw new IllegalArgumentException("command shouldn't be null");
    }

    public static SessionCommandGroup2 fromBundle(Bundle commands) {
        List<Parcelable> list;
        SessionCommand2 command;
        if (commands == null || (list = commands.getParcelableArrayList("android.media.mediasession2.commandgroup.commands")) == null) {
            return null;
        }
        SessionCommandGroup2 commandGroup = new SessionCommandGroup2();
        for (int i = 0; i < list.size(); i++) {
            Parcelable parcelable = list.get(i);
            if ((parcelable instanceof Bundle) && (command = SessionCommand2.fromBundle((Bundle) parcelable)) != null) {
                commandGroup.addCommand(command);
            }
        }
        return commandGroup;
    }
}

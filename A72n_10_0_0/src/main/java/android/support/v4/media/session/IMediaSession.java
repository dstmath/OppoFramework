package android.support.v4.media.session;

import android.app.PendingIntent;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.IMediaControllerCallback;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.alibaba.fastjson.asm.Opcodes;
import com.alibaba.fastjson.parser.JSONToken;
import java.util.List;

public interface IMediaSession extends IInterface {
    void addQueueItem(MediaDescriptionCompat mediaDescriptionCompat) throws RemoteException;

    void addQueueItemAt(MediaDescriptionCompat mediaDescriptionCompat, int i) throws RemoteException;

    void adjustVolume(int i, int i2, String str) throws RemoteException;

    void fastForward() throws RemoteException;

    Bundle getExtras() throws RemoteException;

    long getFlags() throws RemoteException;

    PendingIntent getLaunchPendingIntent() throws RemoteException;

    MediaMetadataCompat getMetadata() throws RemoteException;

    String getPackageName() throws RemoteException;

    PlaybackStateCompat getPlaybackState() throws RemoteException;

    List<MediaSessionCompat.QueueItem> getQueue() throws RemoteException;

    CharSequence getQueueTitle() throws RemoteException;

    int getRatingType() throws RemoteException;

    int getRepeatMode() throws RemoteException;

    int getShuffleMode() throws RemoteException;

    String getTag() throws RemoteException;

    ParcelableVolumeInfo getVolumeAttributes() throws RemoteException;

    boolean isCaptioningEnabled() throws RemoteException;

    boolean isShuffleModeEnabledRemoved() throws RemoteException;

    boolean isTransportControlEnabled() throws RemoteException;

    void next() throws RemoteException;

    void pause() throws RemoteException;

    void play() throws RemoteException;

    void playFromMediaId(String str, Bundle bundle) throws RemoteException;

    void playFromSearch(String str, Bundle bundle) throws RemoteException;

    void playFromUri(Uri uri, Bundle bundle) throws RemoteException;

    void prepare() throws RemoteException;

    void prepareFromMediaId(String str, Bundle bundle) throws RemoteException;

    void prepareFromSearch(String str, Bundle bundle) throws RemoteException;

    void prepareFromUri(Uri uri, Bundle bundle) throws RemoteException;

    void previous() throws RemoteException;

    void rate(RatingCompat ratingCompat) throws RemoteException;

    void rateWithExtras(RatingCompat ratingCompat, Bundle bundle) throws RemoteException;

    void registerCallbackListener(IMediaControllerCallback iMediaControllerCallback) throws RemoteException;

    void removeQueueItem(MediaDescriptionCompat mediaDescriptionCompat) throws RemoteException;

    void removeQueueItemAt(int i) throws RemoteException;

    void rewind() throws RemoteException;

    void seekTo(long j) throws RemoteException;

    void sendCommand(String str, Bundle bundle, MediaSessionCompat.ResultReceiverWrapper resultReceiverWrapper) throws RemoteException;

    void sendCustomAction(String str, Bundle bundle) throws RemoteException;

    boolean sendMediaButton(KeyEvent keyEvent) throws RemoteException;

    void setCaptioningEnabled(boolean z) throws RemoteException;

    void setRepeatMode(int i) throws RemoteException;

    void setShuffleMode(int i) throws RemoteException;

    void setShuffleModeEnabledRemoved(boolean z) throws RemoteException;

    void setVolumeTo(int i, int i2, String str) throws RemoteException;

    void skipToQueueItem(long j) throws RemoteException;

    void stop() throws RemoteException;

    void unregisterCallbackListener(IMediaControllerCallback iMediaControllerCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IMediaSession {
        public static IMediaSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.support.v4.media.session.IMediaSession");
            if (iin == null || !(iin instanceof IMediaSession)) {
                return new Proxy(obj);
            }
            return (IMediaSession) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RatingCompat _arg0;
            Bundle _arg1;
            Uri _arg02;
            Uri _arg03;
            Bundle _arg12 = null;
            MediaDescriptionCompat _arg04 = null;
            MediaDescriptionCompat _arg05 = null;
            MediaDescriptionCompat _arg06 = null;
            Bundle _arg13 = null;
            Bundle _arg14 = null;
            Bundle _arg15 = null;
            Bundle _arg16 = null;
            RatingCompat _arg07 = null;
            Bundle _arg17 = null;
            Bundle _arg18 = null;
            Bundle _arg19 = null;
            KeyEvent _arg08 = null;
            MediaSessionCompat.ResultReceiverWrapper _arg2 = null;
            if (code == 51) {
                data.enforceInterface("android.support.v4.media.session.IMediaSession");
                if (data.readInt() != 0) {
                    _arg0 = RatingCompat.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg12 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                }
                rateWithExtras(_arg0, _arg12);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                boolean _arg09 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _arg010 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = MediaSessionCompat.ResultReceiverWrapper.CREATOR.createFromParcel(data);
                        }
                        sendCommand(_arg010, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg08 = (KeyEvent) KeyEvent.CREATOR.createFromParcel(data);
                        }
                        boolean sendMediaButton = sendMediaButton(_arg08);
                        reply.writeNoException();
                        reply.writeInt(sendMediaButton ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        registerCallbackListener(IMediaControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        unregisterCallbackListener(IMediaControllerCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        boolean isTransportControlEnabled = isTransportControlEnabled();
                        reply.writeNoException();
                        reply.writeInt(isTransportControlEnabled ? 1 : 0);
                        return true;
                    case JSONToken.TRUE /* 6 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _result = getPackageName();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case JSONToken.FALSE /* 7 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _result2 = getTag();
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case JSONToken.NULL /* 8 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        PendingIntent _result3 = getLaunchPendingIntent();
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        long _result4 = getFlags();
                        reply.writeNoException();
                        reply.writeLong(_result4);
                        return true;
                    case 10:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        ParcelableVolumeInfo _result5 = getVolumeAttributes();
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        adjustVolume(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case JSONToken.LBRACE /* 12 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        setVolumeTo(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case JSONToken.RBRACE /* 13 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        play();
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _arg011 = data.readString();
                        if (data.readInt() != 0) {
                            _arg19 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        playFromMediaId(_arg011, _arg19);
                        reply.writeNoException();
                        return true;
                    case JSONToken.RBRACKET /* 15 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _arg012 = data.readString();
                        if (data.readInt() != 0) {
                            _arg18 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        playFromSearch(_arg012, _arg18);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg02 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg17 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        playFromUri(_arg02, _arg17);
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        skipToQueueItem(data.readLong());
                        reply.writeNoException();
                        return true;
                    case JSONToken.IDENTIFIER /* 18 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        pause();
                        reply.writeNoException();
                        return true;
                    case JSONToken.FIELD_NAME /* 19 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        stop();
                        reply.writeNoException();
                        return true;
                    case JSONToken.EOF /* 20 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        next();
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        previous();
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        fastForward();
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        rewind();
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        seekTo(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg07 = RatingCompat.CREATOR.createFromParcel(data);
                        }
                        rate(_arg07);
                        reply.writeNoException();
                        return true;
                    case JSONToken.HEX /* 26 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _arg013 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        sendCustomAction(_arg013, _arg16);
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        MediaMetadataCompat _result6 = getMetadata();
                        reply.writeNoException();
                        if (_result6 != null) {
                            reply.writeInt(1);
                            _result6.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 28:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        PlaybackStateCompat _result7 = getPlaybackState();
                        reply.writeNoException();
                        if (_result7 != null) {
                            reply.writeInt(1);
                            _result7.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 29:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        List<MediaSessionCompat.QueueItem> _result8 = getQueue();
                        reply.writeNoException();
                        reply.writeTypedList(_result8);
                        return true;
                    case 30:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        CharSequence _result9 = getQueueTitle();
                        reply.writeNoException();
                        if (_result9 != null) {
                            reply.writeInt(1);
                            TextUtils.writeToParcel(_result9, reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 31:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        Bundle _result10 = getExtras();
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case Opcodes.ACC_SUPER /* 32 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        int _result11 = getRatingType();
                        reply.writeNoException();
                        reply.writeInt(_result11);
                        return true;
                    case 33:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        prepare();
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _arg014 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        prepareFromMediaId(_arg014, _arg15);
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        String _arg015 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        prepareFromSearch(_arg015, _arg14);
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg03 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        prepareFromUri(_arg03, _arg13);
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        int _result12 = getRepeatMode();
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 38:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        boolean isShuffleModeEnabledRemoved = isShuffleModeEnabledRemoved();
                        reply.writeNoException();
                        reply.writeInt(isShuffleModeEnabledRemoved ? 1 : 0);
                        return true;
                    case 39:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        setRepeatMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 40:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg09 = true;
                        }
                        setShuffleModeEnabledRemoved(_arg09);
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg06 = MediaDescriptionCompat.CREATOR.createFromParcel(data);
                        }
                        addQueueItem(_arg06);
                        reply.writeNoException();
                        return true;
                    case 42:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg05 = MediaDescriptionCompat.CREATOR.createFromParcel(data);
                        }
                        addQueueItemAt(_arg05, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg04 = MediaDescriptionCompat.CREATOR.createFromParcel(data);
                        }
                        removeQueueItem(_arg04);
                        reply.writeNoException();
                        return true;
                    case 44:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        removeQueueItemAt(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 45:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        boolean isCaptioningEnabled = isCaptioningEnabled();
                        reply.writeNoException();
                        reply.writeInt(isCaptioningEnabled ? 1 : 0);
                        return true;
                    case 46:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        if (data.readInt() != 0) {
                            _arg09 = true;
                        }
                        setCaptioningEnabled(_arg09);
                        reply.writeNoException();
                        return true;
                    case 47:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        int _result13 = getShuffleMode();
                        reply.writeNoException();
                        reply.writeInt(_result13);
                        return true;
                    case 48:
                        data.enforceInterface("android.support.v4.media.session.IMediaSession");
                        setShuffleMode(data.readInt());
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString("android.support.v4.media.session.IMediaSession");
                return true;
            }
        }

        private static class Proxy implements IMediaSession {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void sendCommand(String command, Bundle args, MediaSessionCompat.ResultReceiverWrapper cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeString(command);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (cb != null) {
                        _data.writeInt(1);
                        cb.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public boolean sendMediaButton(KeyEvent mediaButton) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    boolean _result = true;
                    if (mediaButton != null) {
                        _data.writeInt(1);
                        mediaButton.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void registerCallbackListener(IMediaControllerCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void unregisterCallbackListener(IMediaControllerCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public boolean isTransportControlEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public String getPackageName() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public String getTag() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public PendingIntent getLaunchPendingIntent() throws RemoteException {
                PendingIntent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (PendingIntent) PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public long getFlags() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public ParcelableVolumeInfo getVolumeAttributes() throws RemoteException {
                ParcelableVolumeInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParcelableVolumeInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void adjustVolume(int direction, int flags, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(direction);
                    _data.writeInt(flags);
                    _data.writeString(packageName);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void setVolumeTo(int value, int flags, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(value);
                    _data.writeInt(flags);
                    _data.writeString(packageName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public MediaMetadataCompat getMetadata() throws RemoteException {
                MediaMetadataCompat _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = MediaMetadataCompat.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public PlaybackStateCompat getPlaybackState() throws RemoteException {
                PlaybackStateCompat _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = PlaybackStateCompat.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public List<MediaSessionCompat.QueueItem> getQueue() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(MediaSessionCompat.QueueItem.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public CharSequence getQueueTitle() throws RemoteException {
                CharSequence _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public Bundle getExtras() throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public int getRatingType() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public boolean isCaptioningEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    boolean _result = false;
                    this.mRemote.transact(45, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public int getRepeatMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public boolean isShuffleModeEnabledRemoved() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    boolean _result = false;
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public int getShuffleMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void addQueueItem(MediaDescriptionCompat description) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (description != null) {
                        _data.writeInt(1);
                        description.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(41, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void addQueueItemAt(MediaDescriptionCompat description, int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (description != null) {
                        _data.writeInt(1);
                        description.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(index);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void removeQueueItem(MediaDescriptionCompat description) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (description != null) {
                        _data.writeInt(1);
                        description.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void removeQueueItemAt(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(index);
                    this.mRemote.transact(44, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void prepare() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void prepareFromMediaId(String uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeString(uri);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void prepareFromSearch(String string, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeString(string);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void prepareFromUri(Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void play() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void playFromMediaId(String uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeString(uri);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void playFromSearch(String string, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeString(string);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void playFromUri(Uri uri, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void skipToQueueItem(long id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeLong(id);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void next() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void previous() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void fastForward() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void rewind() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void seekTo(long pos) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeLong(pos);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void rate(RatingCompat rating) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (rating != null) {
                        _data.writeInt(1);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void rateWithExtras(RatingCompat rating, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    if (rating != null) {
                        _data.writeInt(1);
                        rating.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(51, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void setCaptioningEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(enabled ? 1 : 0);
                    this.mRemote.transact(46, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void setRepeatMode(int repeatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(repeatMode);
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void setShuffleModeEnabledRemoved(boolean shuffleMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(shuffleMode ? 1 : 0);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void setShuffleMode(int shuffleMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeInt(shuffleMode);
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaSession
            public void sendCustomAction(String action, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaSession");
                    _data.writeString(action);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}

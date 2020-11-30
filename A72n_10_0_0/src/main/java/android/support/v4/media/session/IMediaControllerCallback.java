package android.support.v4.media.session;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.text.TextUtils;
import com.alibaba.fastjson.parser.JSONToken;
import java.util.List;

public interface IMediaControllerCallback extends IInterface {
    void onCaptioningEnabledChanged(boolean z) throws RemoteException;

    void onEvent(String str, Bundle bundle) throws RemoteException;

    void onExtrasChanged(Bundle bundle) throws RemoteException;

    void onMetadataChanged(MediaMetadataCompat mediaMetadataCompat) throws RemoteException;

    void onPlaybackStateChanged(PlaybackStateCompat playbackStateCompat) throws RemoteException;

    void onQueueChanged(List<MediaSessionCompat.QueueItem> list) throws RemoteException;

    void onQueueTitleChanged(CharSequence charSequence) throws RemoteException;

    void onRepeatModeChanged(int i) throws RemoteException;

    void onSessionDestroyed() throws RemoteException;

    void onSessionReady() throws RemoteException;

    void onShuffleModeChanged(int i) throws RemoteException;

    void onShuffleModeChangedRemoved(boolean z) throws RemoteException;

    void onVolumeInfoChanged(ParcelableVolumeInfo parcelableVolumeInfo) throws RemoteException;

    public static abstract class Stub extends Binder implements IMediaControllerCallback {
        public Stub() {
            attachInterface(this, "android.support.v4.media.session.IMediaControllerCallback");
        }

        public static IMediaControllerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("android.support.v4.media.session.IMediaControllerCallback");
            if (iin == null || !(iin instanceof IMediaControllerCallback)) {
                return new Proxy(obj);
            }
            return (IMediaControllerCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                Bundle _arg1 = null;
                ParcelableVolumeInfo _arg02 = null;
                Bundle _arg03 = null;
                CharSequence _arg04 = null;
                MediaMetadataCompat _arg05 = null;
                PlaybackStateCompat _arg06 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onEvent(_arg07, _arg1);
                        return true;
                    case 2:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        onSessionDestroyed();
                        return true;
                    case 3:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg06 = PlaybackStateCompat.CREATOR.createFromParcel(data);
                        }
                        onPlaybackStateChanged(_arg06);
                        return true;
                    case 4:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg05 = MediaMetadataCompat.CREATOR.createFromParcel(data);
                        }
                        onMetadataChanged(_arg05);
                        return true;
                    case 5:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        onQueueChanged(data.createTypedArrayList(MediaSessionCompat.QueueItem.CREATOR));
                        return true;
                    case JSONToken.TRUE /* 6 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg04 = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        }
                        onQueueTitleChanged(_arg04);
                        return true;
                    case JSONToken.FALSE /* 7 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        }
                        onExtrasChanged(_arg03);
                        return true;
                    case JSONToken.NULL /* 8 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg02 = ParcelableVolumeInfo.CREATOR.createFromParcel(data);
                        }
                        onVolumeInfoChanged(_arg02);
                        return true;
                    case 9:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        onRepeatModeChanged(data.readInt());
                        return true;
                    case 10:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onShuffleModeChangedRemoved(_arg0);
                        return true;
                    case 11:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onCaptioningEnabledChanged(_arg0);
                        return true;
                    case JSONToken.LBRACE /* 12 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        onShuffleModeChanged(data.readInt());
                        return true;
                    case JSONToken.RBRACE /* 13 */:
                        data.enforceInterface("android.support.v4.media.session.IMediaControllerCallback");
                        onSessionReady();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString("android.support.v4.media.session.IMediaControllerCallback");
                return true;
            }
        }

        private static class Proxy implements IMediaControllerCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onEvent(String event, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    _data.writeString(event);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onSessionDestroyed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onPlaybackStateChanged(PlaybackStateCompat state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    if (state != null) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onMetadataChanged(MediaMetadataCompat metadata) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    if (metadata != null) {
                        _data.writeInt(1);
                        metadata.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    _data.writeTypedList(queue);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onQueueTitleChanged(CharSequence title) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    if (title != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(title, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onExtrasChanged(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onVolumeInfoChanged(ParcelableVolumeInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onRepeatModeChanged(int repeatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    _data.writeInt(repeatMode);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onShuffleModeChangedRemoved(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    _data.writeInt(enabled ? 1 : 0);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onCaptioningEnabledChanged(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    _data.writeInt(enabled ? 1 : 0);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onShuffleModeChanged(int shuffleMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    _data.writeInt(shuffleMode);
                    this.mRemote.transact(12, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.support.v4.media.session.IMediaControllerCallback
            public void onSessionReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("android.support.v4.media.session.IMediaControllerCallback");
                    this.mRemote.transact(13, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}

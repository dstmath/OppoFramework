package com.coloros.eventhub.sdk.aidl;

import java.util.ArrayList;

public class EventType {
    public static final int ACTIVITY_MODE_IN_ELEVATOR = 312;
    public static final int ACTIVITY_MODE_IN_FOUR_WHEELER_VEHICLE = 311;
    public static final int ACTIVITY_MODE_IN_RAIL_VEHICLE = 309;
    public static final int ACTIVITY_MODE_IN_ROAD_VEHICLE = 308;
    public static final int ACTIVITY_MODE_IN_TWO_WHEELER_VEHICLE = 310;
    public static final int ACTIVITY_MODE_IN_VEHICLE = 300;
    public static final int ACTIVITY_MODE_ON_BICYCLE = 301;
    public static final int ACTIVITY_MODE_ON_FOOT = 302;
    public static final int ACTIVITY_MODE_RUNNING = 307;
    public static final int ACTIVITY_MODE_STILL = 303;
    public static final int ACTIVITY_MODE_TILTING = 305;
    public static final int ACTIVITY_MODE_UNKNOWN_ACTIVITY = 304;
    public static final int ACTIVITY_MODE_WALKING = 306;
    public static final int DEVICE_EVENT_BATTERY_CHANGED = 101;
    public static final int DEVICE_EVENT_CHARGING = 102;
    public static final int DEVICE_EVENT_EBOOK_FRONT = 110;
    public static final int DEVICE_EVENT_GPS = 103;
    public static final int DEVICE_EVENT_NFC = 108;
    public static final int DEVICE_EVENT_NOTIFICATION = 106;
    public static final int DEVICE_EVENT_OTG = 107;
    public static final int DEVICE_EVENT_PACKAGE_ADD = 109;
    public static final int DEVICE_EVENT_POWER_SAVING = 100;
    public static final int DEVICE_EVENT_SENSOR = 104;
    public static final int DEVICE_EVENT_WAKELOCK = 105;
    public static final int INVALID = -1;
    public static final int NEXT_APP_EVENT = 10001;
    public static final int SCENE_MODE_AUDIO_CALL = 208;
    public static final int SCENE_MODE_AUDIO_IN = 203;
    public static final int SCENE_MODE_AUDIO_OUT = 202;
    public static final int SCENE_MODE_CAMERA = 204;
    public static final int SCENE_MODE_DOWNLOAD = 206;
    public static final int SCENE_MODE_FILE_DOWNLOAD = 210;
    public static final int SCENE_MODE_FILE_UPLOAD = 214;
    public static final int SCENE_MODE_GAME = 211;
    public static final int SCENE_MODE_HOLIDAY = 213;
    public static final int SCENE_MODE_LOCATION = 201;
    public static final int SCENE_MODE_READING = 207;
    public static final int SCENE_MODE_VIDEO = 205;
    public static final int SCENE_MODE_VIDEO_CALL = 209;
    public static final int SCENE_MODE_VIDEO_LIVE = 212;
    public static final ArrayList<Integer> sEventTypes = new ArrayList<Integer>() {
        /* class com.coloros.eventhub.sdk.aidl.EventType.AnonymousClass1 */

        {
            add(100);
            add(101);
            add(102);
            add(103);
            add(104);
            add(105);
            add(106);
            add(107);
            add(108);
            add(109);
            add(110);
            add(201);
            add(202);
            add(203);
            add(204);
            add(205);
            add(206);
            add(207);
            add(208);
            add(209);
            add(210);
            add(211);
            add(212);
            add(213);
            add(214);
            add(300);
            add(301);
            add(302);
            add(303);
            add(304);
            add(305);
            add(306);
            add(307);
            add(308);
            add(309);
            add(310);
            add(311);
            add(312);
        }
    };

    public class State {
        public static final int ENTER = 0;
        public static final int EXIT = 1;
        public static final int UPDATE = 2;

        public State() {
        }
    }
}

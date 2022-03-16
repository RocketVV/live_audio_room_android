package im.zego.liveaudioroom;

import android.app.Application;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import im.zego.liveaudioroom.callback.ZegoRoomCallback;
import im.zego.liveaudioroom.service.ZegoGiftService;
import im.zego.liveaudioroom.service.ZegoMessageService;
import im.zego.liveaudioroom.service.ZegoRoomService;
import im.zego.liveaudioroom.service.ZegoSpeakerSeatService;
import im.zego.liveaudioroom.service.ZegoUserService;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.callback.IZegoEventHandler;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.constants.ZegoStreamQualityLevel;
import im.zego.zegoexpress.constants.ZegoUpdateType;
import im.zego.zegoexpress.entity.ZegoEngineProfile;
import im.zego.zegoexpress.entity.ZegoStream;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRoomAttributesUpdateInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMRoomEvent;
import im.zego.zim.enums.ZIMRoomState;

/**
 * Class LiveAudioRoom business logic management.
 * <p> Description: This class contains the LiveAudioRoom business logics, manages the service instances of different
 * modules, and also distributing the data delivered by the SDK.
 */
public class ZegoRoomManager {

    private static volatile ZegoRoomManager singleton = null;

    private ZegoRoomManager() {
    }

    /**
     * Get the ZegoRoomManager singleton instance.
     * <p> Description: This method can be used to get the ZegoRoomManager singleton instance.
     * <p> Call this method at: Any time
     *
     * @return
     */
    public static ZegoRoomManager getInstance() {
        if (singleton == null) {
            synchronized (ZegoRoomManager.class) {
                if (singleton == null) {
                    singleton = new ZegoRoomManager();
                }
            }
        }
        return singleton;
    }

    /**
     * The room information management instance, contains the room information, room status and other business logics.
     */
    public ZegoRoomService roomService;
    /**
     * The user information management instance, contains the in-room user information management, logged-in user
     * information and other business logics.
     */
    public ZegoUserService userService;
    /**
     * The room speaker seat management instance, contains the speaker seat management logic.
     */
    public ZegoSpeakerSeatService speakerSeatService;
    /**
     * The message management instance, contains the IM messages management logic.
     */
    public ZegoMessageService messageService;
    /**
     * The gift management instance, contains the gift sending and receiving logics.
     */
    public ZegoGiftService giftService;

    /**
     * Initialize the SDK.
     * <p>Call this method at: Before you log in. We recommend you call this method when the application starts.
     *
     * @param appID       refers to the project ID. To get this, go to ZEGOCLOUD Admin Console:
     *                    https://console.zego.im/dashboard?lang=en
     * @param application th app context
     */
    public void init(long appID, Application application) {
        roomService = new ZegoRoomService();
        userService = new ZegoUserService();
        speakerSeatService = new ZegoSpeakerSeatService();
        messageService = new ZegoMessageService();
        giftService = new ZegoGiftService();

        ZegoEngineProfile profile = new ZegoEngineProfile();
        profile.appID = appID;
        profile.scenario = ZegoScenario.COMMUNICATION;
        profile.application = application;
        ZegoExpressEngine.createEngine(profile, new IZegoEventHandler() {
            @Override
            public void onNetworkQuality(String userID, ZegoStreamQualityLevel upstreamQuality,
                ZegoStreamQualityLevel downstreamQuality) {
                super.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
                if (speakerSeatService != null) {
                    speakerSeatService.onNetworkQuality(userID, upstreamQuality, downstreamQuality);
                }
            }

            @Override
            public void onCapturedSoundLevelUpdate(float soundLevel) {
                super.onCapturedSoundLevelUpdate(soundLevel);
                if (speakerSeatService != null) {
                    speakerSeatService.updateLocalUserSoundLevel(soundLevel);
                }
            }


            @Override
            public void onRemoteSoundLevelUpdate(HashMap<String, Float> soundLevels) {
                super.onRemoteSoundLevelUpdate(soundLevels);
                if (speakerSeatService != null) {
                    speakerSeatService.updateRemoteUsersSoundLevel(soundLevels);
                }
            }

            @Override
            public void onRoomStreamUpdate(String roomID, ZegoUpdateType updateType, ArrayList<ZegoStream> streamList,
                JSONObject extendedData) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData);
                if (roomService != null) {
                    roomService.onRoomStreamUpdate(roomID, updateType, streamList);
                }
            }
        });

        ZegoZIMManager.getInstance().createZIM(appID, application);
        // distribute to specific services which listening what they want
        ZegoZIMManager.getInstance().zim.setEventHandler(new ZIMEventHandler() {
            @Override
            public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
                JSONObject extendedData) {
                super.onConnectionStateChanged(zim, state, event, extendedData);
                if (userService != null) {
                    userService.onConnectionStateChanged(zim, state, event, extendedData);
                }
            }

            @Override
            public void onError(ZIM zim, ZIMError errorInfo) {
                super.onError(zim, errorInfo);
            }

            @Override
            public void onTokenWillExpire(ZIM zim, int second) {
                super.onTokenWillExpire(zim, second);
            }

            @Override
            public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
                super.onReceivePeerMessage(zim, messageList, fromUserID);
                if (userService != null) {
                    userService.onReceivePeerMessage(zim, messageList, fromUserID);
                }
            }

            @Override
            public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
                super.onReceiveRoomMessage(zim, messageList, fromRoomID);
                if (giftService != null) {
                    giftService.onReceiveRoomMessage(zim, messageList, fromRoomID);
                }
                if (messageService != null) {
                    messageService.onReceiveRoomMessage(zim, messageList, fromRoomID);
                }
            }

            @Override
            public void onRoomMemberJoined(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberJoined(zim, memberList, roomID);
                if (userService != null) {
                    userService.onRoomMemberJoined(zim, memberList, roomID);
                }
            }

            @Override
            public void onRoomMemberLeft(ZIM zim, ArrayList<ZIMUserInfo> memberList, String roomID) {
                super.onRoomMemberLeft(zim, memberList, roomID);
                if (userService != null) {
                    userService.onRoomMemberLeft(zim, memberList, roomID);
                }
            }

            @Override
            public void onRoomStateChanged(ZIM zim, ZIMRoomState state, ZIMRoomEvent event, JSONObject extendedData,
                String roomID) {
                super.onRoomStateChanged(zim, state, event, extendedData, roomID);
                if (roomService != null) {
                    roomService.onRoomStateChanged(zim, state, event, extendedData, roomID);
                }
            }

            @Override
            public void onRoomAttributesUpdated(ZIM zim, ZIMRoomAttributesUpdateInfo info, String roomID) {
                super.onRoomAttributesUpdated(zim, info, roomID);
                if (roomService != null) {
                    roomService.onRoomAttributesUpdated(zim, info, roomID);
                }
                if (speakerSeatService != null) {
                    speakerSeatService.onRoomAttributesUpdated(zim, info, roomID);
                }
            }

            @Override
            public void onRoomAttributesBatchUpdated(ZIM zim, ArrayList<ZIMRoomAttributesUpdateInfo> infos,
                String roomID) {
                super.onRoomAttributesBatchUpdated(zim, infos, roomID);
            }
        });
    }

    /**
     * The method to deinitialize the SDK.
     * <p> Description: This method can be used to deinitialize the SDK and release the resources it occupies.</>
     * <p> Call this method at: When the SDK is no longer be used. We recommend you call this method when the
     * application exits.</>
     */
    public void unInit() {
        ZegoZIMManager.getInstance().destroyZIM();
        ZegoExpressEngine.destroyEngine(null);
    }

    /**
     * Upload local logs to the ZEGOCLOUD server.
     * <p>Description: You can call this method to upload the local logs to the ZEGOCLOUD Server for troubleshooting
     * when exception occurs.</>
     * <p>Call this method at: When exceptions occur </>
     *
     * @param callback refers to the callback that be triggered when the logs are upload successfully or failed to
     *                 upload logs.
     */
    public void uploadLog(final ZegoRoomCallback callback) {
        ZegoZIMManager.getInstance().zim
            .uploadLog(errorInfo -> callback.roomCallback(errorInfo.code.value()));
    }
}

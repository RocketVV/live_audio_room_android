package im.zego.liveaudioroom.callback;

import im.zego.liveaudioroom.emus.ZegoLiveAudioRoomErrorCode;

public interface EnterSeatCallback {
    void enterSeat(ZegoLiveAudioRoomErrorCode error);
}
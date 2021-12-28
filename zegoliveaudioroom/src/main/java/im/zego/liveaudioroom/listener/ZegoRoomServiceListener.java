package im.zego.liveaudioroom.listener;

import im.zego.liveaudioroom.model.ZegoRoomInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

/**
 * The delegate related to room status callbacks.
 * <p>Description: Callbacks that be triggered when room status changes.</>
 */
public interface ZegoRoomServiceListener {

    /**
     * Callback for the room status update
     * <p>Description: This callback will be triggered when the text chat is disabled or there is a speaker seat be
     * closed in the room. And all uses in the room receive a notification through this callback.</>
     *
     * @param roomInfo refers to the updated room information.
     */
    void onReceiveRoomInfoUpdate(ZegoRoomInfo roomInfo);

    /**
     * Callbacks related to the user connection status.
     * <p>Description: This callback will be triggered when user gets disconnected due to network error, or gets
     * offline
     * due to the operations in other clients.</>
     *
     * @param state refers to the current connection state.
     * @param event refers to the the event that causes the connection status changes.
     */
    void onConnectionStateChanged(ZIMConnectionState state, ZIMConnectionEvent event);
}
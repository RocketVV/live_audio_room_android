package im.zego.liveaudioroom.refactor.listener;

import java.util.List;

import im.zego.liveaudioroom.refactor.model.ZegoUserInfo;

/**
 * Created by rocket_wang on 2021/12/14.
 */
public interface ZegoUserServiceListener {
    // room info update
    void userInfoUpdate(ZegoUserInfo userInfo);

    // receive user join room command
    void roomUserJoin(List<ZegoUserInfo> memberList);

    // receive user leave room command
    void roomUserLeave(List<ZegoUserInfo> memberList);
}
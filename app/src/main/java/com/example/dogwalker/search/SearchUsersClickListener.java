package com.example.dogwalker.search;

public interface SearchUsersClickListener {
    void onClickProfile(String targetUserId);
    void onClickRequestWalk(String targetUserId, String targetUserName, boolean targetIsOwner, boolean targetIsWalker);
}

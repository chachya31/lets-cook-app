package com.cookingapp.domain.service;

/**
 * ユーザーグループ管理サービスインターフェース
 * ユーザーをグループに追加・削除する機能を提供
 */
public interface UserGroupService {

    /**
     * ユーザーをグループに追加
     * 
     * @param username  ユーザー名（メールアドレス）
     * @param groupName グループ名
     */
    void addUserToGroup(String username, String groupName);

    /**
     * ユーザーをグループから削除
     * 
     * @param username  ユーザー名（メールアドレス）
     * @param groupName グループ名
     */
    void removeUserFromGroup(String username, String groupName);
}

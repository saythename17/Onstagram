package com.icandothisallday2020.onstagram.navigation.model

data class FollowDTO(
    var followerCounter:Int = 0,
    var follwers : MutableMap<String,Boolean> = HashMap(),
    var followingCount:Int = 0,
    var followings : MutableMap<String,Boolean> = HashMap()
)
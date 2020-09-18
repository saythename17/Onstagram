package com.icandothisallday2020.onstagram.navigation.model

data class ContentDTO (var explain : String? = null,
                       var imageUrl : String? = null,
                       var uid : String? = null,
                       var userID :  String? = null,
                       var timeStamp : Long? = null,
                       var favoriteCount : Int = 0,
                       var favorites : Map<String,Boolean> = HashMap()) {
                                data class Comment(var uid : String? = null,
                                var userID: String? = null,
                                var timeStamp : Long? = null,
                                var comment : String? = null)
                                }
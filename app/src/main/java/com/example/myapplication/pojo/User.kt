package com.example.myapplication.pojo

import java.io.Serializable

data class User(val uid:String ,val name:String ,val email:String ,val password:String ,val imagePath:String ,val job:String ,val country:String ,val gender:String):Serializable
{
    constructor():this( "" ,"" ,"","","" ,"" ,"" ,"")
}
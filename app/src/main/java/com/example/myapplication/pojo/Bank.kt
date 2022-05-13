package com.example.myapplication.pojo

import java.io.Serializable

data class Bank(val uid:String, val email:String, val password:String, val name:String, val phoneNumber:String, val country:String, val city:String):Serializable
{
    constructor():this( "" ,"" ,"","","" ,"" ,"")
}
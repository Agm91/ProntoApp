package com.agm91.prontoapp

interface MapView{
    fun setupMap()
    fun addOnLocationSuccessListener()
    fun askUserToTurnOnLocationIfNeeded()
}
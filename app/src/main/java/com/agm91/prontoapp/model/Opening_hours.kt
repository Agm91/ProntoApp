package com.agm91.prontoapp.model

import com.google.gson.annotations.SerializedName

data class Opening_hours (
	@SerializedName("open_now") val open_now : Boolean
)
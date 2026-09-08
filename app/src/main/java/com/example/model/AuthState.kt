package com.example.model

data class AuthState(
    val isAuthenticated: Boolean = true, // Defaults to authenticated for seamless immediate use, can log out or lock
    val employeeId: String = "9771",
    val loginTime: Long = System.currentTimeMillis()
)

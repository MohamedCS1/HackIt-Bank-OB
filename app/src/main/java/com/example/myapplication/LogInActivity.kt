package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.example.myapplication.databinding.ActivityLogInBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class LogInActivity : AppCompatActivity() {

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var mAuth: FirebaseAuth

    lateinit var binding: ActivityLogInBinding

    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.buLogIn.setOnClickListener {
            logIn()
        }

        binding.buSingUn.setOnClickListener {
            startActivity(Intent(this ,SignUpActivity::class.java))
        }


    }

    fun logIn()
    {
        val email = binding.etEmailOrNumber.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            binding.etEmailOrNumber.error = "Please enter a valid email"
            binding.etEmailOrNumber.requestFocus()
            return
        }

        if (password.length < 6)
        {
            binding.etPassword.error = "Password 6 char required"
            binding.etPassword.requestFocus()
            return
        }
        signInWithEmailAndPassword(email ,password)

    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email ,password).addOnCompleteListener {
            if(it.isSuccessful)
            {
                emailIsVerify()
            }
            else
            {
                binding.tvHintFailure.text = "${it.exception!!.message}"
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun emailIsVerify()
    {
        GlobalScope.launch(Dispatchers.Main) {

            val user = mAuth.currentUser

            val reloadFirebase = async { user!!.reload() }

            reloadFirebase.await().addOnCompleteListener {
                if (it.isSuccessful)
                {
                    if (user!!.isEmailVerified)
                    {
                        startActivity(Intent(this@LogInActivity ,MainActivity::class.java))
                    }
                    else
                    {
                        binding.tvHintFailure.text = "Please check your email to verify it"
                    }
                }
                else
                {
                    binding.tvHintFailure.text = "Please check your internet"
                }
            }
        }
    }

}
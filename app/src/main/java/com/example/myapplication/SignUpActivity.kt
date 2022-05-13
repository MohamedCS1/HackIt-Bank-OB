package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.example.myapplication.pojo.Bank
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)

        setContentView(binding.root)
        

       binding.buSignUp.setOnClickListener {
          signUp()
       }
    }

    fun signUp()
    {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val nameOfBank = binding.etNameOfBank.text.toString()
        val phoneNumber = binding.etPhoneNumber.text.toString()
        val country = binding.etCountry.text.toString()
        val city = binding.etCity.text.toString()



        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            binding.etEmail.error = "Please enter a valid email"
            binding.etEmail.requestFocus()
            return
        }

        if (password.length < 6)
        {
            binding.etPassword.error = "Password 6 char required"
            binding.etPassword.requestFocus()
            return
        }

        if (nameOfBank.isEmpty() || nameOfBank.isBlank())
        {
            binding.etNameOfBank.error = "Name of bank required"
            binding.etNameOfBank.requestFocus()
            return
        }

        if (phoneNumber.isEmpty() || phoneNumber.isBlank())
        {
            binding.etPhoneNumber.error = "Phone number required"
            binding.etPhoneNumber.requestFocus()
            return
        }

        if (country.isEmpty() || country.isBlank())
        {
            binding.etCountry.error = "Country required"
            binding.etCountry.requestFocus()
            return
        }

        if (city.isEmpty() || city.isBlank())
        {
            binding.etCity.error = "city required"
            binding.etCity.requestFocus()
            return
        }
        createNewAccount(Bank("" ,email ,password ,nameOfBank ,phoneNumber ,country ,city))
    }

    fun createNewAccount(bank: Bank)
    {
        mAuth.createUserWithEmailAndPassword(bank.email ,bank.password).addOnCompleteListener(object :
            OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if (task.isSuccessful)
                {
                    sendEmailVerification()
                    currentUserDocRef.set(bank)
                    val intentToMainActivity = Intent(this@SignUpActivity ,LogInActivity::class.java)
                    intentToMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intentToMainActivity)
                }
                else
                {
                    binding.tvHintFailure.text = task.exception?.message.toString()
                }
            }
        })
    }

    fun sendEmailVerification() {
        val user = mAuth.currentUser
        user!!.sendEmailVerification().addOnCompleteListener {
            if (it.isSuccessful)
            {
               binding.tvHintFailure.text = "check your email"
            }
            else
            {
                binding.tvHintFailure.text = it.exception?.message.toString()
            }
        }
    }
}
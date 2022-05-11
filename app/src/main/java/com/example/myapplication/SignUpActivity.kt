package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.Toast
import com.example.myapplication.databinding.ActivitySignUpBinding
import com.example.myapplication.pojo.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity(), TextWatcher {

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

        binding.etEmailOrNumber.addTextChangedListener(this)
        binding.etPassword.addTextChangedListener(this)
        binding.buSignUp.isEnabled = false

        binding.buSignUp.setOnClickListener {
            signUp()
        }
    }

    fun signUp()
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
        createNewAccount(User("","" ,email ,password ,"" ,"" ,"" ,""))
    }

    fun createNewAccount(user: User)
    {
        mAuth.createUserWithEmailAndPassword(user.email ,user.password).addOnCompleteListener(object :
            OnCompleteListener<AuthResult> {
            override fun onComplete(task: Task<AuthResult>) {
                if (task.isSuccessful)
                {
                    sendEmailVerification()
                    currentUserDocRef.set(user)
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


    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (binding.etEmailOrNumber.text.isNotBlank() && binding.etPassword.text.isNotBlank())
        {
            binding.buSignUp.setCardBackgroundColor(Color.parseColor("#01C5C4"))
            binding.buSignUp.isEnabled = true
        }
        else
        {
            binding.buSignUp.setCardBackgroundColor(Color.parseColor("#27000000"))
            binding.buSignUp.isEnabled = false
        }

        binding.tvHintFailure.text = ""
    }

    override fun afterTextChanged(p0: Editable?) {
    }


}
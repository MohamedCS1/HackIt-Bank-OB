package com.example.myapplication

import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.myapplication.databinding.ActivityLogInBinding
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class LogInActivity : AppCompatActivity() {

    private val REQ_ONE_TAP: Int = 2
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest

    private val fireStore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var mAuth: FirebaseAuth

    lateinit var binding: ActivityLogInBinding

    val  currentUserDocRef get() =  fireStore.document("users/${mAuth.currentUser!!.uid}")

    lateinit var callbackManager: CallbackManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        binding.buLogIn.setOnClickListener {
            logInWithEmailAndPassword()
        }

        binding.buLoginGoogle.setOnClickListener {
            googleAuth()
        }

        binding.buLoginFacebook.setOnClickListener {
            facebookAuth()
        }
    }

    fun logInWithEmailAndPassword()
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

    fun googleAuth()
    {
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                .setSupported(true)
                .build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(com.firebase.ui.auth.R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .setAutoSelectEnabled(true)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null)
                } catch (e: IntentSender.SendIntentException) {
                    Toast.makeText(baseContext,"something went wrong try again" ,Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener(this) { e ->
                Toast.makeText(baseContext,e.toString() ,Toast.LENGTH_LONG).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.displayName
                    val password = credential.password
                    val photoProfileUri = credential.profilePictureUri
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    mAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val currentUser = mAuth.currentUser
                                Toast.makeText(this ,"$username",Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(baseContext,
                                    task.exception!!.message.toString(),Toast.LENGTH_LONG).show()
                            }
                        }

                } catch (e: ApiException) {
                    Toast.makeText(baseContext,"something went wrong try again" ,Toast.LENGTH_LONG).show()
                }
            }
        }
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }catch (ex:Exception)
        {
            Toast.makeText(this ,ex.message.toString() ,Toast.LENGTH_LONG).show()
        }
    }


    private fun facebookAuth(){

        FacebookSdk.fullyInitialize()
        AppEventsLogger.activateApp(application)

        LoginManager.getInstance().logInWithReadPermissions(this , listOf("email"))

        callbackManager =  CallbackManager.Factory.create()

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {

            override fun onCancel() {
                Toast.makeText(this@LogInActivity ,"Cancel" ,Toast.LENGTH_LONG).show()
            }

            override fun onError(exception: FacebookException) {
                Toast.makeText(this@LogInActivity ,exception.message.toString() ,Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(result: LoginResult) {
                handleFacebookToken(result.accessToken)
            }
        })
    }

    fun handleFacebookToken(accessToken: AccessToken)
    {
        val credential = FacebookAuthProvider.getCredential(accessToken.token)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful)
            {
                val user = mAuth.currentUser
                Toast.makeText(this ,user?.displayName ,Toast.LENGTH_LONG).show()
            }
            else
            {
                Toast.makeText(baseContext,"${it.exception!!.message}" ,Toast.LENGTH_LONG).show()
            }
        }
    }

}
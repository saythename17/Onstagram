package com.icandothisallday2020.onstagram

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSingInClient : GoogleSignInClient? = null
    val GOOGLE_LOGIN_CODE = 9001
    var callbackManager : CallbackManager? =null

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth=FirebaseAuth.getInstance()
        email_LoginBtn.setOnClickListener { signinEmail() }
        google_signIn_button.setOnClickListener {
            //Step.1 : Google Login-- connect to platform
            googleLogin()
        }
        facebook_signIn_button.setOnClickListener { facebookLogin() }//Step:1



        var googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1091113408067-u2utl6atovhs3p0dp63kctp2iuqmlv1i.apps.googleusercontent.com").requestEmail().build()
        googleSingInClient=GoogleSignIn.getClient(this,googleSignInOptions)



        //get facebook login hash key
        //printHashKey()//QLd68EsgmeKBvsybbLxQBFYYQz8=
        callbackManager = CallbackManager.Factory.create()

    }


    fun printHashKey() {
        try {
                val info= packageManager.getPackageInfo(packageName,PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md: MessageDigest = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val hashKey = String(Base64.encode(md.digest(), 0))
                    Log.i("TAGff", "printHashKey() Hash Key: $hashKey")
                }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAGff", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAGff", "printHashKey()", e)
        }
    }

    
    
    
    
    fun googleLogin(){
        var singInIntent = googleSingInClient?.signInIntent
        startActivityForResult(singInIntent,GOOGLE_LOGIN_CODE)
    }



    fun facebookLogin(){
        LoginManager.getInstance().logInWithReadPermissions(this,
            Arrays.asList("public_profile","email"))

        LoginManager.getInstance().registerCallback(callbackManager,
            object  : FacebookCallback<LoginResult>{
                override fun onSuccess(result: LoginResult?) {
                    //Step.2
                    handleFacebookAccessToken(result?.accessToken)
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                }

            })
    }
    fun handleFacebookAccessToken(token: AccessToken?){
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    //Step.3
                    moveMainPage(task.result?.user)
                } else {
                    //Show the message Login fail-- password/email not mach
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }













    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //facebook intent
        callbackManager?.onActivityResult(requestCode,resultCode,data)




        //google intent
        if(requestCode==GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if(result.isSuccess){
                    var account = result.signInAccount
                    //Step.2 : Email Login in Firebase
                    firebaseAuthWithGoogle(account)
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    moveMainPage(task.result?.user)
                } else {
                    //Show the message Login fail-- password/email not mach
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }

    
    
    
    
    
    
    

    fun signInAndroid() {
        auth?.createUserWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //creating a user account
                    moveMainPage(task.result?.user)
                } else if (!(task.exception?.message.isNullOrEmpty())) {
                    //Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    //Login if you have account

                }
            }
    }




    fun signinEmail() {
//        auth?.signInWithEmailAndPassword(et_email.text.toString(), et_password.text.toString())
//            ?.addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    //Login
//                    moveMainPage(task.result?.user)
//                } else {
//                    //Show the message Login fail-- password/email not mach
//                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
//                }
//            }
        auth?.createUserWithEmailAndPassword(et_email.text.toString(),et_password.text.toString())
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Login
                    moveMainPage(task.result?.user)
                } else {
                    //Show the message Login fail-- password/email not mach
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_LONG).show()
                }
            }
    }


    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

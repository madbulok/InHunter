package com.uzlov.inhunter.test

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.uzlov.inhunter.R
import com.uzlov.inhunter.ui.dialogs.DialogLoadedMapsFragment

class TestActivity : AppCompatActivity(){


    private val TAG: String = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         setContentView(R.layout.test_layout)
        findViewById<Button>(R.id.btnInvoke).setOnClickListener {
            DialogLoadedMapsFragment().show(supportFragmentManager, "createFragment")
//            DialogInputNameLoadedMap().show(supportFragmentManager, "createFragment")
//            DialogAcceptLoadMap().show(supportFragmentManager, "createFragment")
        }

    }

    override fun onDestroy() {
        super.onDestroy()

    }
}

fun Fragment.showMessage(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}